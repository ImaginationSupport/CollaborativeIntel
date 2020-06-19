use strict;
use warnings;

use DBI;
use DBD::mysql;

use POSIX;

#
# install DBD:mysql using ppm (as admin)
#

####################################################################################################

use constant READ_ONLY =>						0;

use constant CLEAN_EXISTING_DATA =>				0;

use constant DB_SERVER =>						'localhost';
use constant DB_NAME =>							'fspexplorer';
use constant DB_USERNAME =>						'fspexplorer';
use constant DB_PASSWORD =>						'g0w0lfp@ck';

use constant COLOR_CHOICES =>					[
													'250,70,75',
													'155,200,40',
													'60,180,225'
												];

use constant CONDITION_EVENT_COUNT_CHOICES =>	[ 1, 1, 1, 1, 1, 2, 2, 3 ];

use constant MAX_BATCH_SIZE =>					1000;

use constant STATE_MIN_DAYS =>					30;
use constant STATE_MAX_DAYS =>					120;

use constant MIN_AFTER_STATE =>					7;
use constant MAX_AFTER_STATE =>					21;

use constant MIN_AFTER_CONDITION_EVENT =>		10;
use constant MAX_AFTER_CONDITION_EVENT =>		30;

use constant NUM_STATES_TO_CREATE =>			10000;

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
	my 	$sthDeleteStates = $dbh->prepare( 'DELETE FROM States' );
	my 	$sthUpdateStatesForeignKeys = $dbh->prepare( 'UPDATE States SET ConditionEventId=NULL' );

	print "Cleaning database...\n";

	if( !READ_ONLY )
	{
		print "  Updating States foreign keys...\n";
		$sthUpdateStatesForeignKeys->execute();
		print "    done.\n";

		print "  Deleting from States (cascading to ConditionEvents, StateFeatures and TreeRootNodes)...\n";
		$sthDeleteStates->execute();
		print "    done.\n";

		print "  Resetting States Auto Increment...\n";
		$dbh->do( 'ALTER TABLE States AUTO_INCREMENT = 1' );
		print "    done.\n";

		print "  Resetting ConditionEvents Auto Increment...\n";
		$dbh->do( 'ALTER TABLE ConditionEvents AUTO_INCREMENT = 1' );
		print "    done.\n";

		print "  Resetting StateFeatures Auto Increment...\n";
		$dbh->do( 'ALTER TABLE StateFeatures AUTO_INCREMENT = 1' );
		print "    done.\n";

		print "  Resetting TreeRootNodes Auto Increment...\n";
		$dbh->do( 'ALTER TABLE TreeRootNodes AUTO_INCREMENT = 1' );
		print "    done.\n";
	}
	else
	{
		print "  (skipping due to read only)\n";
	}

	$dbh->commit() unless READ_ONLY;

	$sthDeleteStates->finish() if $sthDeleteStates;
	$sthUpdateStatesForeignKeys->finish() if $sthUpdateStatesForeignKeys;

	print "  done.\n\n";
}

####################################################################################################
# create the new entries

my $sthInsertStates = $dbh->prepare( 'INSERT INTO States(Title,Description,TimeStart,TimeEnd,IsActive,ColorRGB,SoftDeleted,ConditionEventId) VALUES(?,?,?,?,?,?,?,?)' );
my $sthInsertConditionEvents = $dbh->prepare( 'INSERT INTO ConditionEvents(Title,Description,TimeAt,IsActive,ColorRGB,SoftDeleted,StateId) VALUES(?,?,?,?,?,?,?)');
my $sthInsertStateFeatures = $dbh->prepare( 'INSERT INTO StateFeatures(StateId,EntityName,FeatureKey,FeatureValue,FeatureValueType) VALUES(?,?,?,?,?)' );
my $sthInsertTreeRootNodes = $dbh->prepare( 'INSERT INTO TreeRootNodes(Title,RootStateId) VALUES(?,?)' );

my $iStateFeaturesCreated = 0;
my $iInBatch = 0;

printf( "Generating %d states...\n", NUM_STATES_TO_CREATE );

my %hStates;
my %hConditionEvents;
my %hConditionEventsForState;
my %hStatesForConditionEvent;

my $iRootId = undef;

my $iLastPercentComplete = 0;
for( my $i = 0; $i < NUM_STATES_TO_CREATE; ++$i )
{
	my $iIdCreated = GenerateAnotherState();

	if( $i == 0 )
	{
		$iRootId = $iIdCreated;

		$sthInsertTreeRootNodes->execute(
			sprintf(
				'Generated Data: %d nodes on %s',
				NUM_STATES_TO_CREATE,
				POSIX::strftime( '%Y-%m-%d %H:%M', localtime( time() ) )
				),
			$iRootId
			);
	}

	if( NUM_STATES_TO_CREATE >= 100 )
	{
		my $iPercentComplete = int( 100.0 * $i / NUM_STATES_TO_CREATE );
		if( $iPercentComplete > $iLastPercentComplete && $iPercentComplete % 10 == 0 )
		{
			printf( "\t%d%% complete.\n", $iPercentComplete );
			$iLastPercentComplete = $iPercentComplete;
		}
	}
}

print "  done.\n\n";

$dbh->commit() unless READ_ONLY or $iInBatch == 0;

####################################################################################################
# finish up

print "disconnecting...\n";

$sthInsertTreeRootNodes->finish() if $sthInsertTreeRootNodes;
$sthInsertStates->finish() if $sthInsertStates;
$sthInsertConditionEvents->finish() if $sthInsertConditionEvents;
$sthInsertStateFeatures->finish() if $sthInsertStateFeatures;

$dbh->disconnect();

print "  done.\n\n";

####################################################################################################

print "Tree created:\n";
DisplayTree( undef );

print "\n";
printf( "  states created:           %8d\n", scalar keys %hStates );
printf( "  condition events created: %8d\n", scalar keys %hConditionEvents );
printf( "  state features created:   %8d\n", $iStateFeaturesCreated );

exit();

####################################################################################################

sub GenerateAnotherState
{
	my $iNumStatesSoFar = scalar( keys( %hStates ) );

	my $refState = {};

	$refState->{ 'title' } = 'state ' . ( $iNumStatesSoFar + 1 );
	$refState->{ 'description' } = 'description for state ' . ( $iNumStatesSoFar + 1 );
	$refState->{ 'color' } = COLOR_CHOICES->[ int( rand( scalar @{ (COLOR_CHOICES) } ) ) ];
	$refState->{ 'active' } = 1;
	$refState->{ 'softDeleted' } = 0;

	if( $iNumStatesSoFar == 0 )
	{
		# root node
		$refState->{ 'timeStart' } = time();
		$refState->{ 'conditionEventId' } = undef;

		$refState->{ 'depth' } = 0;
	}
	else
	{
		# pick a random child node
		my $refParentState = $hStates{ (keys %hStates)[ rand keys %hStates ] };

		my $refConditionEvent = ChooseConditionEventForState( $refParentState );

		$refState->{ 'timeStart' } = GenerateRandomNextTime( $refConditionEvent->{ 'timeAt' }, MIN_AFTER_CONDITION_EVENT, MAX_AFTER_CONDITION_EVENT );
		$refState->{ 'conditionEventId' } = $refConditionEvent->{ 'id' };

		$refState->{ 'depth' } = $refConditionEvent->{ 'depth' } + 1;
	}

	$refState->{ 'timeEnd' } = GenerateRandomNextTime( $refState->{ 'timeStart' }, STATE_MIN_DAYS, STATE_MAX_DAYS );

	$sthInsertStates->execute(
		$refState->{ 'title' },
		$refState->{ 'description' },
		POSIX::strftime( '%Y-%m-%d %H:%M:%S', localtime( $refState->{ 'timeStart' } ) ),
		POSIX::strftime( '%Y-%m-%d %H:%M:%S', localtime( $refState->{ 'timeEnd' } ) ),
		$refState->{ 'active' },
		$refState->{ 'color' },
		$refState->{ 'softDeleted' },
		$refState->{ 'conditionEventId' }
		) unless READ_ONLY;

	# get the id of this state
	$refState->{ 'id' } = $dbh->{ 'mysql_insertid' };

	# make sure the id created is what we were expecting
	die sprintf( 'unexpected state id %d (expected %d)', $refState->{ 'id' }, $iNumStatesSoFar + 1 ) unless ( $refState->{ 'id' } == $iNumStatesSoFar + 1 ) || !CLEAN_EXISTING_DATA;

	$hStates{ 'state-' . $refState->{ 'id' } } = $refState;

	if( defined $refState->{ 'conditionEventId' } )
	{
		$hStatesForConditionEvent{ 'condition-event-' . $refState->{ 'conditionEventId' } } = [] unless $hStatesForConditionEvent{ 'condition-event-' . $refState->{ 'conditionEventId' } };
		push( @{ $hStatesForConditionEvent{ 'condition-event-' . $refState->{ 'conditionEventId' } } }, $refState->{ 'id' } );
	}

	GenerateStateFeatures( $refState );

	++$iInBatch;
	if( !READ_ONLY and $iInBatch >= MAX_BATCH_SIZE )
	{
		$dbh->commit();
		$iInBatch = 0;
	}

	return $refState->{ 'id' };
}

####################################################################################################

sub ChooseConditionEventForState
{
	my $refState = shift;

	# get the condition events for this state
	my $refConditionEventsForParentState = $hConditionEventsForState{ 'state-' . $refState->{ 'id' } };

	# pick a random condition event, or choose to create a new one
	my $iConditionEventCountChoice = CONDITION_EVENT_COUNT_CHOICES->[ int( rand( scalar @{ (CONDITION_EVENT_COUNT_CHOICES) } ) ) ];

	my $refConditionEvent;

	if( defined $refConditionEventsForParentState and $iConditionEventCountChoice <= scalar( @$refConditionEventsForParentState ) )
	{
		# use an existing choice

		my $iConditionEventId = $refConditionEventsForParentState->[ int( rand( scalar @{ $refConditionEventsForParentState } ) ) ];

		my $sKey = 'condition-event-' . $iConditionEventId;
		die "could not find condition event with id $iConditionEventId" unless $hConditionEvents{ $sKey };

		$refConditionEvent = $hConditionEvents{ $sKey };
	}
	else
	{
		my $iNumConditionEventsSoFar = scalar( keys( %hConditionEvents ) );

		# create a new one
		$refConditionEvent->{ 'title' } = 'condition ' . ( scalar( keys( %hConditionEvents) ) + 1 );
		$refConditionEvent->{ 'description' } = 'description for condition event ' . ( scalar( keys( %hConditionEvents) ) + 1 );
		$refConditionEvent->{ 'timeAt' } = GenerateRandomNextTime( $refState->{ 'timeEnd' }, MIN_AFTER_STATE, MAX_AFTER_STATE );
		$refConditionEvent->{ 'color' } = COLOR_CHOICES->[ int( rand( scalar @{ (COLOR_CHOICES) } ) ) ];
		$refConditionEvent->{ 'active' } = 1;
		$refConditionEvent->{ 'softDeleted' } = 0;
		$refConditionEvent->{ 'stateId' } = $refState->{ 'id' };
		$refConditionEvent->{ 'depth' } = $refState->{ 'depth' };

		$sthInsertConditionEvents->execute(
			$refConditionEvent->{ 'title' },
			$refConditionEvent->{ 'description' },
			POSIX::strftime( '%Y-%m-%d %H:%M:%S', localtime( $refConditionEvent->{ 'timeAt' } ) ),
			$refConditionEvent->{ 'active' },
			$refConditionEvent->{ 'color' },
			$refConditionEvent->{ 'softDeleted' },
			$refConditionEvent->{ 'stateId' }
			) unless READ_ONLY;

		# get the id of this condition event
		$refConditionEvent->{ 'id' } = $dbh->{ 'mysql_insertid' };

		# make sure the id created is what we were expecting
		die sprintf( 'unexpected condition state id %d (expected %d)', $refConditionEvent->{ 'id' }, $iNumConditionEventsSoFar + 1 ) unless ( $refConditionEvent->{ 'id' } == $iNumConditionEventsSoFar + 1 ) || !CLEAN_EXISTING_DATA;

		# add it to the list of condition events
		$hConditionEvents{ 'condition-event-' . $refConditionEvent->{ 'id' } } = $refConditionEvent;

		my $sConditionEventsForStateKey = 'state-' . $refState->{ 'id' };
		$hConditionEventsForState{ $sConditionEventsForStateKey } = [] unless defined $hConditionEventsForState{ $sConditionEventsForStateKey };
		push( @{ $hConditionEventsForState{ $sConditionEventsForStateKey } }, $refConditionEvent->{ 'id' } );
	}

	return $refConditionEvent;
}

####################################################################################################

sub GenerateStateFeatures
{
	my $refState = shift;

#	printf( "id: %d / depth: %d\n", $refState->{ 'id' }, $refState->{ 'depth' } );

	for( my $iLevel = 0; $iLevel <= $refState->{ 'depth' }; ++$iLevel )
	{
		my $sEntityName = $iLevel == 0 ? 'Global' : ( 'Entity ' . chr( 65 + $iLevel - 1 ) );

#		print "\t[$sEntityName]\n";
		my $iEntriesToCreate = 2 + int( rand( 10 ) );
		for( my $i = 0; $i < $iEntriesToCreate; ++$i )
		{
			$sthInsertStateFeatures->execute(
				$refState->{ 'id' },
				$sEntityName,
				'feature' . $i,
				'text value ' . $i,
				0
				) unless READ_ONLY;

			++$iStateFeaturesCreated;
		}
	}

	return;
}

####################################################################################################

sub GenerateRandomNextTime
{
	my $timeAfter = shift;
	my $iMinDaysLater = shift;
	my $iMaxDaysLater = shift;

	my @aLocalTimeAfter = localtime( $timeAfter );

	# seconds
	$aLocalTimeAfter[ 0 ] += 1 + int( rand( 60 ) );

	# minutes
	$aLocalTimeAfter[ 1 ] += 1 + int( rand( 60 ) );

	# hours
	$aLocalTimeAfter[ 2 ] += 24 * $iMinDaysLater + int( rand( 24 * ( $iMaxDaysLater - $iMinDaysLater ) ) );

	return POSIX::mktime( @aLocalTimeAfter );
}

####################################################################################################

sub DisplayTree
{
	my $iStateId = shift || $iRootId;

	die "could not find state with id $iStateId" unless defined $hStates{ 'state-' . $iStateId };
	my $refState = $hStates{ 'state-' . $iStateId };

	printf( '%*d', ( $refState->{ 'depth' } + 1 ) * 8, $iStateId );

	my $refConditionEventsForState = $hConditionEventsForState{ 'state-' . $refState->{ 'id' } };

#	if( $refConditionEventsForState )
#	{
#		print ' [' . join( ',', @$refConditionEventsForState ) . "]\n";
#	}
#	else
#	{
#		print " (none)\n";
#		return;
#	}
	print "\n";

	foreach my $iConditionEventId ( @$refConditionEventsForState )
	{
		if( defined $hStatesForConditionEvent{ 'condition-event-' . $iConditionEventId } )
		{
			foreach my $iChildStateId ( @{ $hStatesForConditionEvent{ 'condition-event-' . $iConditionEventId } } )
			{
				DisplayTree( $iChildStateId );
			}
		}
	}

	return;
}

####################################################################################################
