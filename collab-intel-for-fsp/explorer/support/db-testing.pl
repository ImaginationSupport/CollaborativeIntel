use strict;
use warnings;

use CGI;
use DBI;
use DBD::mysql;

$| = 1;

####################################################################################################

use constant DB_SERVER =>			'localhost';
use constant DB_NAME =>				'fspexplorer';
use constant DB_USERNAME =>			'fspexplorer';
use constant DB_PASSWORD =>			'g0w0lfp@ck';

use constant REQUEST_MAX_DEPTH =>	3;

use constant ROOT_STATE_ID =>		1;

####################################################################################################

my $dbh = DBI->connect( 'DBI:mysql:host=' . DB_SERVER . ';dbname=' . DB_NAME, DB_USERNAME, DB_PASSWORD, { RaiseError => 1 } );

print "fetching states...\n";
my $sthSelectStates = $dbh->prepare( 'SELECT Id,Title,TimeStart,TimeEnd,IsActive,ColorRGB,DecisionEventId FROM States' );
$sthSelectStates->execute();
my $refStateData = $sthSelectStates->fetchall_arrayref();
printf( "\tdone (%d found).\n\n", scalar @$refStateData );

print "fetching decision events...\n";
my $sthSelectDecisionEvents = $dbh->prepare( 'SELECT Id,Title,TimeAt,StateId FROM DecisionEvents' );
$sthSelectDecisionEvents->execute();
my $refDecisionEvents = $sthSelectDecisionEvents->fetchall_arrayref();
printf( "\tdone (%d found).\n\n", scalar @$refDecisionEvents );

$dbh->disconnect();

####################################################################################################

print "parsing row data...\n";

my %hStates;
my %hDecisionEvents;

my %hStatesUnderDecisionEvent;
foreach my $refStateRow ( @$refStateData )
{
	my $refState = {};

	$refState->{ 'id' } = $refStateRow->[ 0 ];
	$refState->{ 'title' } = $refStateRow->[ 1 ];
	$refState->{ 'start' } = $refStateRow->[ 2 ];
	$refState->{ 'end' } = $refStateRow->[ 3 ];
	$refState->{ 'active' } = $refStateRow->[ 4 ] == 1 ? 1 : 0;
	$refState->{ 'color' } = $refStateRow->[ 5 ];
	$refState->{ 'decisionEventId' } = defined $refStateRow->[ 6 ] ? int( $refStateRow->[ 6 ] ) : undef;

	$hStates{ 'state-' . $refState->{ 'id' } } = $refState;

	my $sKey = 'decision-event-';
	if( defined $refState->{ 'decisionEventId' } )
	{
		$sKey .= $refState->{ 'decisionEventId' };
	}
	else
	{
		$sKey .= 'root';
	}

	$hStatesUnderDecisionEvent{ $sKey } = [] unless defined $hStatesUnderDecisionEvent{ $sKey };
	push( @{ $hStatesUnderDecisionEvent{ $sKey } }, $refState->{ 'id' } );
}
printf( "\tdecision events with states: %6d\n", scalar keys %hStatesUnderDecisionEvent );

my %hDecisionEventsUnderState;
foreach my $refDecisionEventRow ( @$refDecisionEvents )
{
	my $refDecisionEvent = {};

	$refDecisionEvent->{ 'id' } = $refDecisionEventRow->[ 0 ];
	$refDecisionEvent->{ 'title' } = $refDecisionEventRow->[ 1 ];
	$refDecisionEvent->{ 'timeAt' } = $refDecisionEventRow->[ 2 ];
	$refDecisionEvent->{ 'stateId' } = $refDecisionEventRow->[ 3 ];

	$hDecisionEvents{ 'decision-event-' . $refDecisionEvent->{ 'id' } } = $refDecisionEvent;

	my $sKey = 'state-' . $refDecisionEvent->{ 'stateId' };

	$hDecisionEventsUnderState{ $sKey } = [] unless defined $hDecisionEventsUnderState{ $sKey };
	push( @{ $hDecisionEventsUnderState{ $sKey } }, $refDecisionEvent->{ 'id' } );

}
printf( "\tstates with decision events: %6d\n", scalar keys %hDecisionEventsUnderState );

print "\tdone.\n\n";

####################################################################################################

print "processing queues...\n";

my @aQueue = ( ROOT_STATE_ID );

my @aAddedStates;
my @aAddedDecisionEvents;

do
{
	my $iStateId = shift @aQueue;
#	print "\tprocessing state $iStateId...\n";

	die "Could not find state with id $iStateId!" unless defined $hStates{ 'state-' . $iStateId };
	push( @aAddedStates, $hStates{ 'state-' . $iStateId } );

	# now find all the decision events for this state
	my $sDecisionEventsUnderStateKey = 'state-' . $iStateId;
	if( defined $hDecisionEventsUnderState{ $sDecisionEventsUnderStateKey } )
	{
		foreach my $iDecisionEventId ( @{ $hDecisionEventsUnderState{ $sDecisionEventsUnderStateKey } } )
		{
			die "Could not find state with id $iStateId!" unless defined $hDecisionEvents{ 'decision-event-' . $iDecisionEventId };
			push( @aAddedDecisionEvents, $hDecisionEvents{ 'decision-event-' . $iDecisionEventId } );

			my $sStatesUnderDecisionEventKey = 'decision-event-' . $iDecisionEventId;

			if( defined $hStatesUnderDecisionEvent{ $sStatesUnderDecisionEventKey } )
			{
				foreach my $iChildStateId ( @{ $hStatesUnderDecisionEvent{ $sStatesUnderDecisionEventKey } } )
				{
					push( @aQueue, $iChildStateId );
				}
			}
		}
	}
}
while( scalar( @aQueue ) > 0 );

printf( "\tstates:          %6d\n", scalar @aAddedStates );
printf( "\tdecision events: %6d\n", scalar @aAddedDecisionEvents );

print "\tdone.\n";

####################################################################################################

print "\nall tasks complete.\n";

####################################################################################################