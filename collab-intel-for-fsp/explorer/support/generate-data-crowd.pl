use strict;
use warnings;

use DBI;
use DBD::mysql;

use POSIX;

#
# install DBD:mysql using ppm (as admin)
#

####################################################################################################

use constant QUESTION_TYPE_UNKNOWN =>			0;
use constant QUESTION_TYPE_INTEGER =>			1;
use constant QUESTION_TYPE_MULTIPLE_CHOICE =>	2;


use constant READ_ONLY =>						0;

use constant CLEAN_EXISTING_DATA =>				1;

use constant DB_SERVER =>						'localhost';
use constant DB_NAME =>							'fsp';
use constant DB_USERNAME =>						'fspexplorer';
use constant DB_PASSWORD =>						'g0w0lfp@ck';

use constant NUM_QUESTIONS_TO_CREATE =>			20;

use constant MIN_CHOICES =>						2;
use constant MAX_CHOICES =>						5;

####################################################################################################
# connect to the database

$| = 1;

printf( "Connecting to database %s on %s as user %s...\n", DB_NAME, DB_SERVER, DB_USERNAME );

my $dbh = DBI->connect( 'DBI:mysql:host=' . DB_SERVER . ';dbname=' . DB_NAME, DB_USERNAME, DB_PASSWORD, { RaiseError => 1, AutoCommit => 0 } );

print "  done.\n\n";

####################################################################################################
# clean existing entries

if( CLEAN_EXISTING_DATA )
{
	print "Cleaning database...\n";

	if( !READ_ONLY )
	{
		print "  Deleting from Questions (cascades to choices and answers)...\n";
		$dbh->do( 'DELETE FROM crowdquestions' );
		print "    done.\n";
	}
	else
	{
		print "  (skipping due to read only)\n";
	}

	$dbh->commit() unless READ_ONLY;

	print "  done.\n\n";
}

####################################################################################################
# create the new entries

my $iQuestionsCreated = 0;
my $iChoicesCreated = 0;
my $iAnsweredCreated = 0;

my $sthInsertQuestion = $dbh->prepare( 'INSERT INTO crowdquestions(questiontype,priority,context,contexthtml,question,questionhtml) VALUES(?,?,?,?,?,?)' );
my $sthInsertQuestionChoice = $dbh->prepare( 'INSERT INTO crowdquestionchoices(questionid,choice,choicehtml,value) VALUES(?,?,?,?)' );
#my $sthInsertAnswer = $dbh->prepare( 'INSERT INTO crowdanswers() VALUES()' );

for( my $i = 0; $i < NUM_QUESTIONS_TO_CREATE; ++$i )
{
	# choose the question type
	my $iQuestionType = 1 + int( rand( 2 ) );

	$sthInsertQuestion->execute(
		$iQuestionType,
		1,
		'context for question ' . ( $i + 1 ),
		'<b>context for question ' . ( $i + 1 ) . '</b>',
		'question ' . ( $i + 1 ) . ':',
		'<b>question ' . ( $i + 1 ) . ':</b>',
		) unless READ_ONLY;
	++$iQuestionsCreated;

	my $iQuestionId = READ_ONLY ? $i : $dbh->{ 'mysql_insertid' };

	if( $iQuestionType == QUESTION_TYPE_INTEGER )
	{
		# nothing else to do
	}
	elsif( $iQuestionType == QUESTION_TYPE_MULTIPLE_CHOICE )
	{
		GenerateAnswers( $iQuestionId );
	}
	else
	{
		die "Unknown question type: $iQuestionType";
	}
}

$dbh->commit() unless READ_ONLY;

####################################################################################################

sub GenerateAnswers
{
	my $iQuestionId = shift;

	my $iNumChoicesToCreate = MIN_CHOICES + int( rand( MAX_CHOICES - MIN_CHOICES + 1 ) );
	for( my $i = 0; $i < $iNumChoicesToCreate; ++$i )
	{
		$sthInsertQuestionChoice->execute(
			$iQuestionId,
			chr( 65 + $i ),
			'<b>' . chr( 65 + $i ) . '</b>',
			$i
			) unless READ_ONLY;

		++$iChoicesCreated;
	}

	return;
}

####################################################################################################
# finish up

print "disconnecting...\n";

#$sthInsertAnswer->finish() if $sthInsertAnswer;
$sthInsertQuestionChoice->finish() if $sthInsertQuestionChoice;
$sthInsertQuestion->finish() if $sthInsertQuestion;

$dbh->disconnect();

print "  done.\n\n";

####################################################################################################

print "stats:\n";
printf( "  questions created: %8d\n", $iQuestionsCreated );
printf( "  choices created:   %8d\n", $iChoicesCreated );
printf( "  answers created:   %8d\n", $iAnsweredCreated );

exit();

####################################################################################################
