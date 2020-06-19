//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.ara.fsp.data.web.Condition;
import com.ara.fsp.data.web.ConditionOption;
import com.ara.fsp.data.web.Edge;
import com.ara.fsp.data.web.Entity;
import com.ara.fsp.data.web.EntityType;
import com.ara.fsp.data.web.EntityWithFeatures;
import com.ara.fsp.data.web.FSPDatabaseWeb;
import com.ara.fsp.data.web.FSPWebObject;
import com.ara.fsp.data.web.Feature;
import com.ara.fsp.data.web.FeatureMap;
import com.ara.fsp.data.web.Identifiable;
import com.ara.fsp.data.web.OldConditionEvent;
import com.ara.fsp.data.web.OldFeature;
import com.ara.fsp.data.web.OldState;
import com.ara.fsp.data.web.State;
import com.ara.fsp.exceptions.ArgumentException;

public final class ConsoleTestApp
{

	public static void main( String[] args )
	{
		try
		{
			// testGenerateCrowdData();
			// testGenerateNewStateSpace();
			// testGenerateOldStateSpace();

			testGenerateFeatures();
		}
		catch( Exception e )
		{
			System.out.println( e.getMessage() );
			e.printStackTrace();
		}

		return;
	}

	private static < T extends FSPWebObject > JSONArray toJSONArray( final Collection< T > list ) throws ArgumentException
	{
		JSONArray out = new JSONArray();

		final Iterator< T > iter = list.iterator();
		while( iter.hasNext() )
		{
			out.put( iter.next().toJSON() );
		}

		return out;
	}

	private static < T extends Identifiable > Map< Integer, T > createLookupTable( final List< T > list )
	{
		final Map< Integer, T > map = new HashMap< Integer, T >();

		final Iterator< T > iter = list.iterator();
		while( iter.hasNext() )
		{
			final T next = iter.next();
			map.put( next.getId(), next );
		}

		return map;
	}

	/**
	 * Gets an instance of the database
	 *
	 * @return the instance created
	 * @throws Exception
	 */
	private static FSPDatabaseWeb getDatabase() throws Exception
	{
		final Connection conn = DriverManager.getConnection( "jdbc:mysql://localhost:3306/fsp?user=root&password=mfc2k1win32" );

		return new FSPDatabaseWeb( conn );
	}

	private static void testGenerateCrowdData() throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		// final List< FSPObject > x = db.getCrowdQuestions();

		final JSONObject response = new JSONObject();

		response.put( "questions", toJSONArray( db.getCrowdQuestionsAndAnswerCounts() ) );
		System.out.println( response.toString( 4 ) );

		return;
	}

	private static void testGenerateNewStateSpace() throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		final int missionRootStateId = 1;

		// create a few lookup tables
		final Map< Integer, State > stateLookup = createLookupTable( db.getStates() );
		final Map< Integer, Condition > conditionLookup = createLookupTable( db.getConditions() );

		// create a lookup table from the previous edge
		final Map< Integer, Set< Edge > > edgePreviousLookup = new HashMap< Integer, Set< Edge > >();
		final List< Edge > allEdges = db.getEdges();
		final Iterator< Edge > iterEdge = allEdges.iterator();
		while( iterEdge.hasNext() )
		{
			final Edge edge = iterEdge.next();

			if( !edgePreviousLookup.containsKey( edge.getPrev() ) )
			{
				edgePreviousLookup.put( edge.getPrev(), new HashSet< Edge >() );
			}

			edgePreviousLookup.get( edge.getPrev() ).add( edge );
		}

		// create a lookup table from the condition option edge
		final Map< Integer, Set< ConditionOption > > conditionOptionEdgeLookup = new HashMap< Integer, Set< ConditionOption > >();
		final List< ConditionOption > allConditionOptions = db.getConditionOptions();
		final Iterator< ConditionOption > iterConditionOption = allConditionOptions.iterator();
		while( iterConditionOption.hasNext() )
		{
			final ConditionOption conditionOption = iterConditionOption.next();

			if( !conditionOptionEdgeLookup.containsKey( conditionOption.getEdgeId() ) )
			{
				conditionOptionEdgeLookup.put( conditionOption.getEdgeId(), new HashSet< ConditionOption >() );
			}

			conditionOptionEdgeLookup.get( conditionOption.getEdgeId() ).add( conditionOption );
		}

		// create the output lists that will be sent to the GUI
		final List< State > includedStates = new ArrayList< State >();
		final List< Edge > includedEdges = new ArrayList< Edge >();
		final List< ConditionOption > includedConditionOptions = new ArrayList< ConditionOption >();
		final List< Condition > includedConditions = new ArrayList< Condition >();

		// set up the queue that will be processed to generate the tree, starting with the root state
		final Queue< Integer > stateIdQueue = new LinkedList< Integer >();
		stateIdQueue.add( missionRootStateId );

		// process the queue
		while( !stateIdQueue.isEmpty() )
		{
			final int stateId = stateIdQueue.remove();

			// add this state to the list
			includedStates.add( stateLookup.get( stateId ) );

			System.out.printf( "at: %d\n", stateId );

			if( edgePreviousLookup.containsKey( stateId ) )
			{
				final Iterator< Edge > iterNextEdge = edgePreviousLookup.get( stateId ).iterator();
				while( iterNextEdge.hasNext() )
				{
					final Edge nextEdge = iterNextEdge.next();

					System.out.printf( "\tnext: %d\n", nextEdge.getNext() );

					// add this edge to the output list
					includedEdges.add( nextEdge );

					// add the next state to the queue
					stateIdQueue.add( nextEdge.getNext() );

					// now find the condition options for this edge
					if( conditionOptionEdgeLookup.containsKey( nextEdge.getId() ) )
					{
						final Iterator< ConditionOption > iterEdgeConditionOptions = conditionOptionEdgeLookup.get( nextEdge.getId() ).iterator();
						while( iterEdgeConditionOptions.hasNext() )
						{
							ConditionOption edgeConditionOption = iterEdgeConditionOptions.next();

							includedConditionOptions.add( edgeConditionOption );

							includedConditions.add( conditionLookup.get( edgeConditionOption.getConditionId() ) );
						}
					}
				}
			}
		}

		System.out.println();
		System.out.printf( "states:            %6d\n", includedStates.size() );
		System.out.printf( "edges:             %6d\n", includedEdges.size() );
		System.out.printf( "condition options: %6d\n", includedConditionOptions.size() );
		System.out.printf( "conditions:        %6d\n", includedConditions.size() );

		return;
	}

	private static void testGenerateOldStateSpace() throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		final int missionRootStateId = 1;

		// create a few lookup tables
		final Map< Integer, State > stateLookup = createLookupTable( db.getStates() );
		final Map< Integer, Condition > conditionLookup = createLookupTable( db.getConditions() );

		// create a lookup table from the previous edge
		final Map< Integer, Set< Edge > > edgePreviousLookup = new HashMap< Integer, Set< Edge > >();
		final List< Edge > allEdges = db.getEdges();
		final Iterator< Edge > iterEdge = allEdges.iterator();
		while( iterEdge.hasNext() )
		{
			final Edge edge = iterEdge.next();

			if( !edgePreviousLookup.containsKey( edge.getPrev() ) )
			{
				edgePreviousLookup.put( edge.getPrev(), new HashSet< Edge >() );
			}

			edgePreviousLookup.get( edge.getPrev() ).add( edge );
		}

		// create a lookup table from the condition option edge
		final Map< Integer, Set< ConditionOption > > conditionOptionEdgeLookup = new HashMap< Integer, Set< ConditionOption > >();
		final List< ConditionOption > allConditionOptions = db.getConditionOptions();
		final Iterator< ConditionOption > iterConditionOption = allConditionOptions.iterator();
		while( iterConditionOption.hasNext() )
		{
			final ConditionOption conditionOption = iterConditionOption.next();

			if( !conditionOptionEdgeLookup.containsKey( conditionOption.getEdgeId() ) )
			{
				conditionOptionEdgeLookup.put( conditionOption.getEdgeId(), new HashSet< ConditionOption >() );
			}

			conditionOptionEdgeLookup.get( conditionOption.getEdgeId() ).add( conditionOption );
		}

		// create the output lists that will be sent to the GUI
		final List< OldState > states = new ArrayList< OldState >();
		final Map< Integer, OldConditionEvent > addedConditionEventLookup = new HashMap< Integer, OldConditionEvent >();

		// set up the queue that will be processed to generate the tree, starting with the root state
		final Queue< Integer > stateIdQueue = new LinkedList< Integer >();
		stateIdQueue.add( missionRootStateId );

		final State rootState = stateLookup.get( missionRootStateId );
		states.add( new OldState( rootState.getId(), rootState.getStart(), rootState.getEnd(), rootState.getLabel(), rootState.getDescription(), rootState
			.isActive(), rootState.getColor(), -1 ) );

		System.out.printf( "root: %d\n", missionRootStateId );

		int nextGeneratedConditionEventId = 1000000;

		// process the queue
		while( !stateIdQueue.isEmpty() )
		{
			final int stateId = stateIdQueue.remove();

			System.out.printf( "at: %d\n", stateId );

			final State stateAt = stateLookup.get( stateId );

			if( edgePreviousLookup.containsKey( stateId ) )
			{
				final Iterator< Edge > iterNextEdge = edgePreviousLookup.get( stateId ).iterator();
				while( iterNextEdge.hasNext() )
				{
					final Edge nextEdge = iterNextEdge.next();

					final State stateNext = stateLookup.get( nextEdge.getNext() );

					// System.out.printf( "\tnext: %d\n", nextEdge.getNext() );

					// TODO fix this hack!
					// set the time of the condition event to be halfway between the two states
					final Calendar conditionEventTime = Calendar.getInstance();
					conditionEventTime.setTimeInMillis( ( stateAt.getEnd().getTimeInMillis() + stateNext.getStart().getTimeInMillis() ) / 2 );

					int conditionEventId = -1;

					// find the condition option for this edge
					if( conditionOptionEdgeLookup.containsKey( nextEdge.getId() ) )
					{
						// condition exists, so use that option

						final Set< ConditionOption > edgeConditionOptions = conditionOptionEdgeLookup.get( nextEdge.getId() );
						if( edgeConditionOptions.size() != 1 )
							throw new Exception( "Unexpected number of edge condition options: " + edgeConditionOptions.size() );
						final ConditionOption conditionOption = edgeConditionOptions.iterator().next();
						final Condition condition = conditionLookup.get( conditionOption.getConditionId() );

						final boolean isActive = true; // TODO fix this hack
						final String color = "100,255,100"; // TODO fix this hack

						conditionEventId = condition.getId();

						if( !addedConditionEventLookup.containsKey( conditionEventId ) )
						{
							addedConditionEventLookup.put(
								conditionEventId,
								new OldConditionEvent(
									conditionEventId,
									condition.getLabel(),
									condition.getDescription(),
									conditionEventTime,
									isActive,
									color,
									stateId ) );
						}
					}
					else
					{
						// condition does NOT exist, so fake one

						conditionEventId = nextGeneratedConditionEventId;
						++nextGeneratedConditionEventId;

						addedConditionEventLookup.put( conditionEventId, new OldConditionEvent(
							conditionEventId,
							"(generated condition event)",
							"(generated condition event)",
							conditionEventTime,
							true,
							"255,255,0",
							stateId ) );
					}

					// add this state to the list
					states.add( new OldState(
						stateNext.getId(),
						stateNext.getStart(),
						stateNext.getEnd(),
						stateNext.getLabel(),
						stateNext.getDescription(),
						stateNext.isActive(),
						stateNext.getColor(),
						conditionEventId ) );

					// add the next state to the queue
					stateIdQueue.add( stateNext.getId() );
				}
			}
		}

		System.out.println();

		System.out.println( "States:" );
		System.out.println( toJSONArray( states ).toString( 4 ) );

		System.out.println();

		System.out.println( "Condition Events:" );
		System.out.println( toJSONArray( addedConditionEventLookup.values() ).toString( 4 ) );

		System.out.println();
		System.out.printf( "states:           %6d\n", states.size() );
		System.out.printf( "condition events: %6d\n", addedConditionEventLookup.size() );

		return;
	}

	private static void testGenerateFeatures() throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		final int stateId = 1;

		final JSONObject response = new JSONObject();

		final Map< Integer, EntityType > entityTypeLookup = createLookupTable( db.getEntityTypes() );
		final Map< Integer, Entity > entityLookup = createLookupTable( db.getEntities() );
		final Map< Integer, FeatureMap > featureMapLookup = createLookupTable( db.getFeatureMaps() );

		final List< Feature > featuresForState = db.getFeatures( stateId );

		final Map< Integer, EntityWithFeatures > includedEntitiesLookup = new HashMap< Integer, EntityWithFeatures >();

		final Iterator< Feature > iterFeatures = featuresForState.iterator();
		while( iterFeatures.hasNext() )
		{
			final Feature feature = iterFeatures.next();

			if( !includedEntitiesLookup.containsKey( feature.getEntityId() ) )
			{
				// entity not in map yet, so get it and add it
				final Entity entity = entityLookup.get( feature.getEntityId() );

				includedEntitiesLookup.put( entity.getId(), new EntityWithFeatures( entity, new ArrayList< Feature >() ) );
			}

			// add this feature to the entity
			includedEntitiesLookup.get( feature.getEntityId() ).getFeatures().add( feature );
		}

		// final List< Entity > allEntities = db.getEntities();
		// final Iterator< Entity > iterEntities = allEntities.iterator();
		// while( iterEntities.hasNext() )
		// {
		// final Entity entity = iterEntities.next();
		//
		// final EntityType entityType = entityTypeLookup.get( entity.getTypeId() );
		// }

		final List< OldFeature > oldFeatures = new ArrayList< OldFeature >();

		final Iterator< EntityWithFeatures > iterEntitiesWithFeatures = includedEntitiesLookup.values().iterator();
		while( iterEntitiesWithFeatures.hasNext() )
		{
			final EntityWithFeatures entityWithFeatures = iterEntitiesWithFeatures.next();

			final Entity entity = entityWithFeatures.getEntity();
			final EntityType entityType = entityTypeLookup.get( entity.getTypeId() );

			System.out.printf( "%-30s %s\n", entityType.getLabel(), entity.getLabel() );

			final Iterator< Feature > iterEntityFeatures = entityWithFeatures.getFeatures().iterator();
			while( iterEntityFeatures.hasNext() )
			{
				final Feature feature = iterEntityFeatures.next();
				final FeatureMap featureMap = featureMapLookup.get( feature.getFeatureMapId() );

				System.out.printf( "\t%-50s %s\n", featureMap.getLabel(), feature.getValue() );

				oldFeatures.add( new OldFeature( feature.getId(), entityType.getLabel(), featureMap.getLabel(), feature.getValue(), featureMap.getFeatureTypeId() ) );
			}
		}

		Collections.sort( oldFeatures );

		response.put( "success", true );
		response.put( "entities", toJSONArray( oldFeatures ) );
		System.out.println( response.toString( 4 ) );

		return;
	}
}
