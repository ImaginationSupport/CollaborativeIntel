use strict;
use warnings;

$| = 1;

####################################################################################################

use constant READ_ONLY =>			0;

use constant FILEPATH_IN =>			'../data/create-tables.sql';
use constant FILEPATH_OUT =>		'../data/create-tables-altered.sql';

use constant JAVA_CLASSES_OUT =>	'java-class-definitions.txt';

use constant DATABASE_NAME =>		'FSP';
use constant DATABASE_USER =>		'fspexplorer';

use constant USE_DROP_DATABASE =>	0;

use constant CREATE_JAVA_CLASSES =>	1;

####################################################################################################
# read in the file

my $sFileInContents = '';

my $FILEIN = undef;
open( FILEIN, FILEPATH_IN ) || die 'Error opening input file(' . FILEPATH_IN . ') : ' . $!;
while( my $sLine = <FILEIN> )
{
	chomp $sLine;

	# trim whitespace
	$sLine =~ s/^\s+//;
	$sLine =~ s/\s+$//;

	if( length( $sLine ) > 0 && !( $sLine =~ /^-- / )  && !( $sLine =~ /^#/ ) )
	{
		$sFileInContents .= $sLine;

		$sFileInContents .= ' ' unless $sLine =~ /,$/;
	}
}
close( FILEIN );

$sFileInContents =~ s/\s+$//;

####################################################################################################
# parse the lines

my %hAllTableLines;
my %hTableReferences;
my %hTableColumns;

foreach my $sStatement ( split( /;/, $sFileInContents ) )
{
	$sStatement =~ s/^\s+//;

	if( $sStatement =~ /^#/
		|| $sStatement =~ /^SET /
		|| $sStatement =~ /^DROP SCHEMA /
		|| $sStatement =~ /^CREATE SCHEMA /
		|| $sStatement =~ /^USE /
		|| $sStatement =~ /^DROP TABLE /
		)
	{
		# ignore these lines
	}
	elsif( $sStatement =~ /^CREATE TABLE( IF NOT EXISTS)? (`?\w+`?\.)?`?(?<table_name>\w+)`?\s*\((?<table_definition>.*)\).*$/ )
	{
		my $sTableName = $+{ table_name };
		my $sTableDefinition = $+{ table_definition };

		my @aAllTableLines;
		my @aTableReferences;
		my @aTableColumns;

		push( @aAllTableLines, 'CREATE TABLE ' . $sTableName );
		push( @aAllTableLines, '(' );

		my $bFirst = 1;
		my $bLastWasColumn = 0;
		my @aTableDefinitionSplit = split( /,/, $sTableDefinition );

		for( my $iTableDefinitionLine = 0; $iTableDefinitionLine < scalar( @aTableDefinitionSplit ); ++$iTableDefinitionLine )
		{
			my $sTableDefinitionLine = $aTableDefinitionSplit[ $iTableDefinitionLine ];

			# spliting on the commas can break up lines incorrectly, for instance in a foreign key definition or inside a quoted string, so re-combine if needed

			my $bKeepGoing = 0;
			do
			{
				$bKeepGoing = 0;

				my $iCountOpenParenthesis = 0;
				my $iCountCloseParenthesis = 0;
				my $iCountQuotes = 0;
				for( my $i = 0; $i < length $sTableDefinitionLine; ++$i )
				{
					my $sChar = substr( $sTableDefinitionLine, $i, 1 );
					if( $sChar eq '(' )
					{
						++$iCountOpenParenthesis;
					}
					elsif( $sChar eq ')' )
					{
						++$iCountCloseParenthesis;
					}
					elsif( $sChar eq '\'' )
					{
						++$iCountQuotes;
					}
				}

				if( $iCountOpenParenthesis != $iCountCloseParenthesis || $iCountQuotes % 2 != 0 )
				{
					++$iTableDefinitionLine;
					$sTableDefinitionLine .= ',' . $aTableDefinitionSplit[ $iTableDefinitionLine ];

					$bKeepGoing = 1;
				}
			}
			while( $bKeepGoing );

			# remove the leading whitespace
			$sTableDefinitionLine =~ s/^\s+//;

			# remove the backticks
			$sTableDefinitionLine =~ s/`//g;

			# remove the schema
			$sTableDefinitionLine =~ s/\w+\.//g;

			# update the whitespace
			$sTableDefinitionLine =~ s/\s*\(/( /g;
			$sTableDefinitionLine =~ s/\)/ )/g;

			# update the cascading
			$sTableDefinitionLine =~ s/ON DELETE NO ACTION/ON DELETE CASCADE/g;
			$sTableDefinitionLine =~ s/ON UPDATE NO ACTION/ON UPDATE CASCADE/g;

			# add the table the foreign key references

			if( $sTableDefinitionLine =~ /^CONSTRAINT\s*(?<constraint_name>.*?)\s*(?<constraint_body>FOREIGN KEY\(\s*(\w+)\s*\) REFERENCES\s*(?<referenced_table>\w+)\(.*?)$/ )
			{
				my $sConstraintName = $+{ constraint_name };
				my $sConstraintBody = $+{ constraint_body };
				my $sReferencedTable = $+{ referenced_table };

				$sConstraintName = lc( $sTableName ) . '_' . $sConstraintName if substr( $sConstraintName, 0, length( $sTableName ) + 1 ) ne lc( $sTableName . '_' );

				#print "before: $sTableDefinitionLine\n";

				$sTableDefinitionLine = 'CONSTRAINT ' . $sConstraintName . ' ' . $sConstraintBody;

				#print "after:  $sTableDefinitionLine\n\n";

				push( @aTableReferences, $sReferencedTable );
			}
			elsif( $sTableDefinitionLine =~ /^INDEX (?<index_name>.*?)(?<index_body>\(.*)$/ )
			{
				my $sIndexName = $+{ index_name };
				my $sIndexBody = $+{ index_body };

				$sIndexName = lc( $sTableName ) . '_' . $sIndexName if substr( $sIndexName, 0, length( $sTableName ) + 1 ) ne lc( $sTableName . '_' );

				#print "before: $sTableDefinitionLine\n";

				$sTableDefinitionLine = 'INDEX ' . $sIndexName . $sIndexBody;

				#print "after:  $sTableDefinitionLine\n\n";
			}

			my $bIsColumn = 1;
			if(
				$sTableDefinitionLine =~ /^PRIMARY KEY/
				|| $sTableDefinitionLine =~ /^INDEX/
				|| $sTableDefinitionLine =~ /^CONSTRAINT/
				|| $sTableDefinitionLine =~ /^INDEX/
				)
			{
				# update the whitespace
				$sTableDefinitionLine =~ s/\(\s*/( /g;
				$sTableDefinitionLine =~ s/\s*\)/ )/g;

				$bIsColumn = 0;
			}
			else
			{
				# remove a trailing ') NULL'
				$sTableDefinitionLine =~ s/\) NULL$/)/;

				die sprintf( 'Invalid column line for table %s: "%s"', $sTableName, $sTableDefinitionLine) unless $sTableDefinitionLine =~ /^(\w+) (\w+)/;

				my $sColumnName = $1;
				my $sColumnType = $2;
				#print "[$sTableDefinitionLine] => [$sColumnName][$sColumnType]\n";
				push( @aTableColumns, [ $sColumnName, $sColumnType ] );
			}

			push( @aAllTableLines, '' ) if $bLastWasColumn && !$bIsColumn;

			push( @aAllTableLines, "\t" . $sTableDefinitionLine . ( $iTableDefinitionLine == scalar( @aTableDefinitionSplit ) - 1 ? '' : ',' ) );

			$bFirst = 0;
			$bLastWasColumn = $bIsColumn;
		}

		push( @aAllTableLines, ');' );

		$hAllTableLines{ $sTableName } = \@aAllTableLines;
		$hTableReferences{ $sTableName } = \@aTableReferences;
		$hTableColumns{ $sTableName } = \@aTableColumns;
	}
	else
	{
		die "Unknown line: \"$sStatement\"\n";
	}
}

####################################################################################################

if( CREATE_JAVA_CLASSES )
{
	my $FILEOUT = undef;
	open( FILEOUT, '>' . JAVA_CLASSES_OUT ) || die 'Error opening output file(' . JAVA_CLASSES_OUT . ') : ' . $!;

	foreach my $sTableName ( sort keys %hAllTableLines )
	{
		my $sClassName = capitalizeFirst( capitalizeEach( $sTableName ) );

		# remove a trailing 's' if found
		$sClassName =~ s/s$//;

		my @aJsonKeyLines;
		my @aColumnDefinitionLines;
		my @aGetterLines;

		my $sNormalConstructorParameterList;
		my @aNormalConstructorInnerLines;
		my @aCombinedNormalConstructorLines;

		my @aDeserializeConstructorInnerLines;
		my @aCombinedDeserializeConstructorLines;

		my @aToJsonInnerLines;
		my @aCombinedToJsonLines;

		my $bFirstColumn = 1;
		foreach my $refColumnInfo ( @{ $hTableColumns{ $sTableName } } )
		{
			my $sColumnName = $refColumnInfo->[ 0 ];
			my $sSQLType = $refColumnInfo->[ 1 ];

			my $sVariableName = capitalizeEach( $sColumnName );

			my $sJavaType = undef;
			if( $sSQLType eq 'VARCHAR' || $sSQLType eq 'TEXT' )
			{
				$sJavaType = 'String';
			}
			elsif( $sSQLType eq 'INT' )
			{
				$sJavaType = 'int';
			}
			elsif( $sSQLType eq 'DOUBLE' )
			{
				$sJavaType = 'double';
			}
			elsif( $sSQLType eq 'TINYINT' )
			{
				$sJavaType = 'boolean';
			}
			elsif( $sSQLType eq 'DATETIME' )
			{
				$sJavaType = 'Calendar';
			}
			else
			{
				die 'Unknown type: ' . $sSQLType;
			}

			# JSON key
			my $sJSONKey = 'JSONKEY_' . uc( $sColumnName );
			push( @aJsonKeyLines, sprintf( "public static final String %s = \"%s\";", $sJSONKey, $sColumnName ) );

			# column definition
			push( @aColumnDefinitionLines, sprintf( "private final %s %s;", $sJavaType, 'm_' . $sVariableName ) );

			# getter
			push( @aGetterLines, sprintf( "public %s %s%s()", $sJavaType, $sJavaType eq 'boolean' ? 'is' : 'get', capitalizeFirst( capitalizeEach( $sColumnName ) ) ) );
			push( @aGetterLines, '{' );
			push( @aGetterLines, sprintf( "\treturn %s;", 'm_' . $sVariableName ) );
			push( @aGetterLines, '}' );
			push( @aGetterLines, '' );

			# normal constructor parameter list
			$sNormalConstructorParameterList .= ', ' unless $bFirstColumn;
			$sNormalConstructorParameterList .= sprintf( 'final %s %s', $sJavaType, $sVariableName );

			# normal constructor inner line
			push( @aNormalConstructorInnerLines, sprintf( '%s = %s;', 'm_' . $sVariableName, $sVariableName ) );

			# deserialize constructor inner line
			if( $sJavaType eq 'Calendar' )
			{
				push( @aDeserializeConstructorInnerLines, sprintf( '%s = WebCommon.parseDateTime( sourceJSON.getString( %s ) );', 'm_' . $sVariableName, $sJSONKey ) );
			}
			else
			{
				push( @aDeserializeConstructorInnerLines, sprintf( '%s = sourceJSON.get%s( %s );', 'm_' . $sVariableName, capitalizeFirst( $sJavaType ), $sJSONKey ) );
			}

			# toJSON inner line
			if( $sJavaType eq 'Calendar' )
			{
				push( @aToJsonInnerLines, sprintf( 'json.put( %s, WebCommon.formatDateTime( %s ) );', $sJSONKey, 'm_' . $sVariableName ) );
			}
			else
			{
				push( @aToJsonInnerLines, sprintf( 'json.put( %s, %s );', $sJSONKey, 'm_' . $sVariableName ) );
			}

			$bFirstColumn = 0;
		}

		# create the normal constructor
		push( @aCombinedNormalConstructorLines, sprintf( 'public %s( %s ) throws ArgumentException', $sClassName, $sNormalConstructorParameterList ) );
		push( @aCombinedNormalConstructorLines, '{' );
		foreach my $sInnerLine ( @aNormalConstructorInnerLines )
		{
			push( @aCombinedNormalConstructorLines, "\t" . $sInnerLine );
		}
		push( @aCombinedNormalConstructorLines, '' );
		push( @aCombinedNormalConstructorLines, "\treturn;" );
		push( @aCombinedNormalConstructorLines, '}' );

		# create the deserialize constructor
		push( @aCombinedDeserializeConstructorLines, sprintf( 'public %s( final JSONObject sourceJSON ) throws ArgumentException', $sClassName ) );
		push( @aCombinedDeserializeConstructorLines, '{' );
		foreach my $sInnerLine ( @aDeserializeConstructorInnerLines )
		{
			push( @aCombinedDeserializeConstructorLines, "\t" . $sInnerLine );
		}
		push( @aCombinedDeserializeConstructorLines, '' );
		push( @aCombinedDeserializeConstructorLines, "\treturn;" );
		push( @aCombinedDeserializeConstructorLines, '}' );

		# create the toJSON override
		push( @aCombinedToJsonLines, '@Override' );
		push( @aCombinedToJsonLines, 'public JSONObject toJSON() throws ArgumentException' );
		push( @aCombinedToJsonLines, '{' );
		push( @aCombinedToJsonLines, "\tJSONObject json = new JSONObject();" );
		push( @aCombinedToJsonLines, '' );
		foreach my $sInnerLine ( @aToJsonInnerLines )
		{
			push( @aCombinedToJsonLines, "\t" . $sInnerLine );
		}
		push( @aCombinedToJsonLines, '' );
		push( @aCombinedToJsonLines, "\treturn json;" );
		push( @aCombinedToJsonLines, '}' );

		# create the class definition
		print FILEOUT "// ####################################################################################################\n\n";
		print FILEOUT "package com.ara.fsp.data.web;\n\n";
		print FILEOUT "public final class $sClassName implements FSPWebObject, Identifiable\n";
		print FILEOUT "{\n";
		print FILEOUT "\t" . join( "\n\t", @aJsonKeyLines ) . "\n\n";
		print FILEOUT "\t" . join( "\n\t", @aColumnDefinitionLines ) . "\n\n";
		print FILEOUT "\t" . join( "\n\t", @aGetterLines ) . "\n\n";
		print FILEOUT "\t" . join( "\n\t", @aCombinedNormalConstructorLines ) . "\n\n";
		print FILEOUT "\t" . join( "\n\t", @aCombinedDeserializeConstructorLines ) . "\n\n";
		print FILEOUT "\t" . join( "\n\t", @aCombinedToJsonLines ) . "\n";
		print FILEOUT "}\n\n";
	}

	close( FILEOUT );
}

####################################################################################################
# sequence the tables appropriately

my @aDropTableLines;
my @aCreateTableLines;

my %hTableNamesAlreadyDone;

my @aOrderedTableNamesForCreateTables;

while( scalar keys %hAllTableLines > 0 )
{
	foreach my $sTableName ( sort keys %hAllTableLines )
	{
		my @aRemainingTableReferences = @{ $hTableReferences{ $sTableName } };

		for( my $i = 0; $i < scalar @aRemainingTableReferences; ++$i )
		{
			if( $hTableNamesAlreadyDone{ $aRemainingTableReferences[ $i ] } )
			{
				splice( @aRemainingTableReferences, $i, 1 );
				--$i;
			}
		}

		if( scalar @aRemainingTableReferences == 0 )
		{
			unshift( @aDropTableLines, 'DROP TABLE IF EXISTS ' . $sTableName . ';' );
			push( @aCreateTableLines, @{ $hAllTableLines{ $sTableName } }, '' );

			push( @aOrderedTableNamesForCreateTables, $sTableName );

			# remove it from the lists
			delete $hAllTableLines{ $sTableName };
			delete $hTableReferences{ $sTableName };

			# add it to the list already processed
			$hTableNamesAlreadyDone{ $sTableName } = 1;
		}
	}
}

####################################################################################################
# write out the cleaned file

if( !READ_ONLY )
{
	my $sCleanedFilePathOut = FILEPATH_OUT;

	$sCleanedFilePathOut = $1 if $sCleanedFilePathOut =~ /([^\/]+)$/;

	my $FILEOUT = undef;
	open( FILEOUT, '>' . FILEPATH_OUT ) || die 'Error opening output file(' . FILEPATH_OUT . ') : ' . $!;

	print FILEOUT "####################################################################################################\n";
	print FILEOUT "#\n";
	print FILEOUT "# To run this file:\n";
	print FILEOUT "#\n";
	print FILEOUT '# mysql --host=localhost --user=root -p < ' . $sCleanedFilePathOut . "\n";
	print FILEOUT "#\n";
	print FILEOUT "####################################################################################################\n\n";

	print FILEOUT 'USE ' . DATABASE_NAME . "\n";

	print FILEOUT "\n####################################################################################################\n\n";

	if( USE_DROP_DATABASE )
	{
		print FILEOUT 'DROP DATABASE IF EXISTS ' . DATABASE_NAME . ";\n";
		print FILEOUT 'CREATE DATABASE ' . DATABASE_NAME . ";\n";
		print FILEOUT 'GRANT ALTER,CREATE,DELETE,DROP,INDEX,INSERT,SELECT,UPDATE ON ' . DATABASE_NAME . '.* to ' . DATABASE_USER . '@localhost;' . "\n";
		print FILEOUT "FLUSH PRIVILEGES;\n";
	}
	else
	{
		print FILEOUT join( "\n", @aDropTableLines ) . "\n";
	}

	print FILEOUT "\n####################################################################################################\n\n";

	print FILEOUT join( "\n", @aCreateTableLines );

	print FILEOUT "\n####################################################################################################\n\n";

	close( FILEOUT );
}

print join( "\n", @aCreateTableLines );

print "\nTables: (ordered by create table)\n\t" . join( "\n\t", @aOrderedTableNamesForCreateTables ) . "\n";

####################################################################################################

print "\nall tasks complete.\n";

exit();

####################################################################################################

sub capitalizeFirst
{
	return ucfirst shift;
}

####################################################################################################

sub capitalizeEach
{
	my $sIn = shift;
	my $sOut = $sIn;

	my @aKnownWords = (
		'Comment',
		'Condition',
		'Context',
		'Map',
		'Option',
		'Type',
		);

	foreach my $sTarget ( @aKnownWords )
	{
		$sOut =~ s/$sTarget/$sTarget/gie if ( lc( $sOut ) ne lc( $sTarget ) ) && !( $sOut =~ /^$sTarget/i );
	}

	# special cases
	$sOut = substr( $sOut, 0, length( $sOut ) - 2 ) . 'Id' if( $sOut ne 'id' ) && ( $sOut =~ /id$/ );

#	printf( "%-30s %s\n", $sIn, $sOut );

	return $sOut;
}

####################################################################################################
