use strict;
use warnings;

use DBI;
use DBD::mysql;

$| = 1;

####################################################################################################

use constant DB_SERVER =>			'localhost';
use constant DB_NAME =>				'fsp';
#use constant DB_NAME =>				'fspexplorer';
use constant DB_USERNAME =>			'fspexplorer';
use constant DB_PASSWORD =>			'g0w0lfp@ck';

use constant SHOW_ROW_NUMBERS =>	0;

####################################################################################################

my $dbh = DBI->connect( 'DBI:mysql:host=' . DB_SERVER . ';dbname=' . DB_NAME, DB_USERNAME, DB_PASSWORD, { RaiseError => 1 } );

####################################################################################################

#	my $sSQL = 'SELECT * FROM States';
#	my $sSQL = 'SELECT * FROM DecisionEvents';
#	my $sSQL = 'SELECT * FROM StateEntities LIMIT 1000';

#	my $sSQL = 'SELECT Id,DecisionEventId FROM States';
#	my $sSQL = 'SELECT Id,StateId FROM DecisionEvents';

#	my $sSQL = 'SELECT * FROM States WHERE TimeEnd<TimeStart';

#	my $sSQL = 'SELECT * FROM TreeRootNodes';

####################################################################################################

	RunQuery( 'SELECT * FROM CrowdQuestions' );
#	RunQuery( 'SELECT * FROM CrowdAnswers ORDER BY questionid' );

#	my $sSQL = 'SELECT
#			Q.Id,
#			Q.QuestionType,
#			Q.Priority,
#			Q.Context,
#			Q.QuestionType,
#			Q.QuestionType,
#			COUNT(A.Id)
#		FROM
#			CrowdQuestions AS Q,
#			CrowdAnswers AS A
#		WHERE
#			A.questionid = Q.id
#		GROUP BY
#			A.QuestionId
#		';

	RunQuery(
		'SELECT
			Q.Id,
			Q.QuestionType,
			Q.Priority,
			Q.Context,
			Q.QuestionType,
			Q.QuestionType,
			( SELECT COUNT(*) FROM CrowdAnswers AS A WHERE A.questionid=Q.id)
		FROM
			CrowdQuestions AS Q
		ORDER BY
			Q.Id
		' );

SELECT
Q.id,
Q.questiontype,
Q.priority,
Q.context,
Q.questiontype,
Q.questiontype,
( SELECT COUNT(*) FROM CrowdAnswers AS A WHERE A.questionid=Q.id)
FROM
CrowdQuestions AS Q
ORDER BY Q.Id


####################################################################################################

$dbh->disconnect();

####################################################################################################

sub RunQuery
{
	my $sSQL = shift;

	my @aRows;
	my @aColumnTitles;
	my @aColumnWidths;
	my @aColumnAlignments;

	# execute the SQL
	my $sth = $dbh->prepare( $sSQL );
	$sth->execute();

	# download all the data and figure out the column widths
	my $iNumColumns = 0;
	while( my @aRow = $sth->fetchrow_array() )
	{
		# if first row, initialize the rows
		if( scalar @aColumnWidths == 0 )
		{
			$iNumColumns = scalar @aRow;

			# first figure out the column title and alignment
			for( my $i = 0; $i < $iNumColumns; ++$i )
			{
				push( @aColumnTitles, $sth->{ NAME_lc }->[ $i ] );

				#  4 = integer
				# 12 = varchar
				# -4 = TEXT

				# print $sth->{ TYPE }->[ $i ] . "\n";

				my $bLeftAlign = 0;
				if( $sth->{ TYPE }->[ $i ] == 12 || $sth->{ TYPE }->[ $i ] == -4 )
				{
					$bLeftAlign = 1;
				}

				push( @aColumnAlignments, $bLeftAlign );
			}

			for( my $i = 0; $i < $iNumColumns; ++$i )
			{
				push( @aColumnWidths, length( $aColumnTitles[ $i ] ) );
			}
		}

		push( @aRows, \@aRow );

		# adjust the column widths
		for( my $i = 0; $i < $iNumColumns; ++$i )
		{
			my $sColumnValue = defined( $aRow[ $i ] ) ? $aRow[ $i ] : '[null]';

			my $iColumnLength = length( '' . $sColumnValue );

			$aColumnWidths[ $i ] = $iColumnLength if $iColumnLength > $aColumnWidths[ $i ];
		}
	}

	# print the header row
	print ' ';
	print '   # | ' if SHOW_ROW_NUMBERS;

	for( my $i = 0; $i < $iNumColumns; ++$i )
	{
		print ' | ' unless $i == 0;

		printf( '%-*s', $aColumnWidths[ $i ], $aColumnTitles[ $i ] );
	}
	print "\n";

	# print the divider line
	print '-';
	print '-----+-' if SHOW_ROW_NUMBERS;

	for( my $i = 0; $i < $iNumColumns; ++$i )
	{
		print '-+-' unless $i == 0;
		for( my $j = 0; $j < $aColumnWidths[ $i ]; ++$j )
		{
			print '-';
		}
	}
	print "-\n";

	# display the data
	my $iRowIndex = 0;
	foreach my $refRow ( @aRows )
	{
		print ' ';
		printf( '%4d | ', $iRowIndex ) if SHOW_ROW_NUMBERS;
		++$iRowIndex;

		for( my $i = 0; $i < $iNumColumns; ++$i )
		{
			print ' | ' unless $i == 0;

			my $sColumnValue = defined( $refRow->[ $i ] ) ? $refRow->[ $i ] : '[null]';

			printf( '%' . ( $aColumnAlignments[ $i ] ? '-' : '' ). '*s', $aColumnWidths[ $i ], $sColumnValue );
		}
		print "\n";
	}

	$sth->finish();

	print "\n";

	return;
}

####################################################################################################
