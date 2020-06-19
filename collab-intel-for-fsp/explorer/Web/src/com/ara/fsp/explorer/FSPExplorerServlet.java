//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.explorer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import las.fsp.api.FspEdge;
import las.fsp.api.FspFeatureMgr;
import las.fsp.api.FspRunTime;
import las.fsp.api.FspState;
import las.fsp.api.FspStateId;
import las.fsp.api.FspStateMgr;
import las.fsp.araruntime.RunTime;
import las.fsp.araruntime.mysql.DataLayer;
import las.fsp.araruntime.mysql.FeatureMgr;
import las.fsp.araruntime.mysql.StateId;
import las.fsp.araruntime.mysql.StateMgr;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import com.ara.fsp.FSPServletBase;
import com.ara.fsp.WebCommon;
import com.ara.fsp.data.web.Condition;
import com.ara.fsp.data.web.ConditionOption;
import com.ara.fsp.data.web.Edge;
import com.ara.fsp.data.web.Entity;
import com.ara.fsp.data.web.EntityType;
import com.ara.fsp.data.web.EntityWithFeatures;
import com.ara.fsp.data.web.FSPDatabaseWeb;
import com.ara.fsp.data.web.Feature;
import com.ara.fsp.data.web.FeatureMap;
import com.ara.fsp.data.web.OldConditionEvent;
import com.ara.fsp.data.web.OldFeature;
import com.ara.fsp.data.web.OldState;
import com.ara.fsp.data.web.State;
import com.ara.fsp.exceptions.ArgumentException;

/**
 * Servlet to handle the backend for the FSP Explorer
 */
public class FSPExplorerServlet extends FSPServletBase
{
	private static final long serialVersionUID = 1L;

	private static final int NODE_TYPE_STATE = 1;
	private static final int NODE_TYPE_CONDITION_EVENT = 2;

	private static final String REQUEST_PARAMETER_QUERY_ACTION = "q";
	private static final String REQUEST_PARAMETER_IS_DEBUGGING = "debug";
	private static final String REQUEST_PARAMETER_ID = "id";
	private static final String REQUEST_PARAMETER_NODE_TYPE = "nodeType";
	// private static final String REQUEST_PARAMETER_ENTITY_NAME = "entityName";
	// private static final String REQUEST_PARAMETER_KEY = "key";
	private static final String REQUEST_PARAMETER_VALUE = "value";
	// private static final String REQUEST_PARAMETER_VALUE_TYPE = "valueType";
	private static final String REQUEST_PARAMETER_FEATURES_JSON = "featuresJSON";
	private static final String REQUEST_PARAMETER_LABEL = "label";
	private static final String REQUEST_PARAMETER_DESCRIPTION = "description";
	private static final String REQUEST_PARAMETER_START = "start";
	private static final String REQUEST_PARAMETER_END = "end";
	private static final String REQUEST_PARAMETER_ACTIVE = "active";
	private static final String REQUEST_PARAMETER_COLOR = "color";
	private static final String REQUEST_PARAMETER_P = "p";
	private static final String REQUEST_PARAMETER_TIME_AT = "timeAt";
	private static final String REQUEST_PARAMETER_STATE_ID = "stateId";
	private static final String REQUEST_PARAMETER_CONDITION_EVENT_ID = "conditionEventId";

	private static final String REQUEST_GET_STATE_SPACE = "getStateSpace";
	private static final String REQUEST_GET_NEW_STATE_SPACE = "getNewStateSpace";
	private static final String REQUEST_GET_MISSIONS = "getMissions";
	private static final String REQUEST_GET_FEATURES = "getFeatures";

	private static final String REQUEST_SAVE_FEATURES = "saveFeatures";
	private static final String REQUEST_SAVE_NODE_PROPERTIES = "saveNodeProperties";
	private static final String REQUEST_ADD_NODE = "addNode";

	// private static final String REQUEST_DELETE_FEATURE = "deleteFeature";
	// private static final String REQUEST_DELETE_FEATURE_ENTITY = "deleteFeatureEntity";
	// private static final String REQUEST_DELETE_NODE = "deleteNode";

	// private static final String REQUEST_CREATE_MISSION = "createMission";

	/**
	 * log4j logger
	 */
	private static final Logger LOGGER = Logger.getLogger( FSPExplorerServlet.class );

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FSPExplorerServlet()
	{
		super();

		return;
	}

	/**
	 * @see Servlet#init(ServletConfig)
	 */
	@Override
	public void init( ServletConfig config ) throws ServletException
	{
		return;
	}

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doGet( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		final boolean isDebugging = getOptionalParameterBoolean( request, REQUEST_PARAMETER_IS_DEBUGGING, false );

		try
		{
			final String queryAction = getRequiredParameterString( request, REQUEST_PARAMETER_QUERY_ACTION );

			JSONObject data = null;

			switch( queryAction )
			{
			case REQUEST_GET_STATE_SPACE:
				data = handleGetStateSpace( request );
				break;

			case REQUEST_GET_NEW_STATE_SPACE:
				data = handleGetNewStateSpace( request );
				break;

			case REQUEST_GET_MISSIONS:
				data = handleGetMissions( request );
				break;

			case REQUEST_GET_FEATURES:
				data = handleGetFeatures( request );
				break;

			default:
				throw new ArgumentException( "Unknown query action: " + queryAction );
			}

			sendSuccessfulResponse( response, data, isDebugging );
		}
		catch( ArgumentException e )
		{
			LOGGER.warn( "Argument exception:", e );
			LOGGER.warn( "Request: " + request.getQueryString() );

			sendFailureResponse( response, e.getMessage(), isDebugging );
		}
		catch( Exception e )
		{
			LOGGER.error( "Server error:", e );
			LOGGER.error( "Request: " + request.getQueryString() );

			sendFailureResponse( response, "Server error: " + e.getMessage(), isDebugging );
		}

		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	@Override
	protected void doPost( HttpServletRequest request, HttpServletResponse response ) throws ServletException, IOException
	{
		final boolean isDebugging = getOptionalParameterBoolean( request, REQUEST_PARAMETER_IS_DEBUGGING, false );

		try
		{
			final String queryAction = getRequiredParameterString( request, REQUEST_PARAMETER_QUERY_ACTION );

			JSONObject data = null;

			switch( queryAction )
			{
			case REQUEST_SAVE_FEATURES:
				data = handleSaveFeatures( request );
				break;

			case REQUEST_SAVE_NODE_PROPERTIES:
				data = handleSaveNodeProperties( request );
				break;

			case REQUEST_ADD_NODE:
				data = handleAddNode( request );
				break;

			// case REQUEST_CREATE_MISSION:
			// data = handleCreateRootNode( request );
			// break;

			// case REQUEST_DELETE_NODE:
			// data = handleDeleteNode( request );
			// break;

			default:
				throw new ArgumentException( "Unknown query action: " + queryAction );
			}

			sendSuccessfulResponse( response, data, isDebugging );
		}
		catch( ArgumentException e )
		{
			LOGGER.warn( "Argument exception:", e );

			sendFailureResponse( response, e.getMessage(), isDebugging );
		}
		catch( Exception e )
		{
			LOGGER.error( "Server error:", e );

			sendFailureResponse( response, "Server error: " + e.getMessage(), isDebugging );
		}

		return;
	}

	private JSONObject handleGetMissions( HttpServletRequest request ) throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		final JSONObject response = new JSONObject();

		response.put( "missions", toJSONArray( db.getMissions() ) );

		return response;
	}

	private JSONObject handleGetStateSpace( HttpServletRequest request ) throws Exception
	{
		final int missionRootStateId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );

		final FSPDatabaseWeb db = getDatabase();

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

		int nextGeneratedConditionEventId = 1000000;

		// process the queue
		while( !stateIdQueue.isEmpty() )
		{
			final int stateId = stateIdQueue.remove();

			final State stateAt = stateLookup.get( stateId );

			if( edgePreviousLookup.containsKey( stateId ) )
			{
				final Iterator< Edge > iterNextEdge = edgePreviousLookup.get( stateId ).iterator();
				while( iterNextEdge.hasNext() )
				{
					final Edge nextEdge = iterNextEdge.next();

					final State stateNext = stateLookup.get( nextEdge.getNext() );

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
						final String color = "0,255,0"; // TODO fix this hack

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
							"200,200,200",
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

		// create the response object
		final JSONObject response = new JSONObject();
		response.put( "states", toJSONArray( states ) );
		response.put( "conditionEvents", toJSONArray( addedConditionEventLookup.values() ) );

		return response;
	}

	private JSONObject handleGetNewStateSpace( HttpServletRequest request ) throws Exception
	{
		final int missionRootStateId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );

		final FSPDatabaseWeb db = getDatabase();

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

			if( edgePreviousLookup.containsKey( stateId ) )
			{
				final Iterator< Edge > iterNextEdge = edgePreviousLookup.get( stateId ).iterator();
				while( iterNextEdge.hasNext() )
				{
					final Edge nextEdge = iterNextEdge.next();

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

		// create the response object
		final JSONObject response = new JSONObject();
		response.put( "states", toJSONArray( includedStates ) );
		response.put( "edges", toJSONArray( includedEdges ) );
		response.put( "conditionOptions", toJSONArray( includedConditionOptions ) );
		response.put( "conditions", toJSONArray( includedConditions ) );

		return response;
	}

	private JSONObject handleGetFeatures( HttpServletRequest request ) throws Exception
	{
		final int stateId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );

		final FSPDatabaseWeb db = getDatabase();

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

		final List< OldFeature > oldFeatures = new ArrayList< OldFeature >();

		final Iterator< EntityWithFeatures > iterEntitiesWithFeatures = includedEntitiesLookup.values().iterator();
		while( iterEntitiesWithFeatures.hasNext() )
		{
			final EntityWithFeatures entityWithFeatures = iterEntitiesWithFeatures.next();

			final Entity entity = entityWithFeatures.getEntity();
			final EntityType entityType = entityTypeLookup.get( entity.getTypeId() );

			final String combinedEntityLabel = entityType.getLabel() + " - " + entity.getLabel();

			final Iterator< Feature > iterEntityFeatures = entityWithFeatures.getFeatures().iterator();
			while( iterEntityFeatures.hasNext() )
			{
				final Feature feature = iterEntityFeatures.next();
				final FeatureMap featureMap = featureMapLookup.get( feature.getFeatureMapId() );

				oldFeatures
					.add( new OldFeature( feature.getId(), combinedEntityLabel, featureMap.getLabel(), feature.getValue(), featureMap.getFeatureTypeId() ) );
			}
		}

		Collections.sort( oldFeatures );

		// create the response object
		final JSONObject response = new JSONObject();
		response.put( "features", toJSONArray( oldFeatures ) );

		return response;

	}

	private JSONObject handleSaveFeatures( HttpServletRequest request ) throws Exception
	{
		final String featuresJSON = getRequiredParameterString( request, REQUEST_PARAMETER_FEATURES_JSON );

		final Map< Integer, String > changes = new HashMap< Integer, String >();
		try
		{
			final JSONArray parsedJSON = new JSONArray( new JSONTokener( featuresJSON ) );
			for( int i = 0; i < parsedJSON.length(); ++i )
			{
				final JSONObject entry = parsedJSON.getJSONObject( i );

				changes.put( entry.getInt( REQUEST_PARAMETER_ID ), entry.getString( REQUEST_PARAMETER_VALUE ) );
			}
		}
		catch( JSONException e )
		{
			throw new Exception( "Error parsing Features JSON: " + e.getMessage() );
		}

		final FSPDatabaseWeb db = getDatabase();

		final Iterator< Integer > iterChanges = changes.keySet().iterator();
		while( iterChanges.hasNext() )
		{
			final int featureId = iterChanges.next();

			db.updateFeature( featureId, changes.get( featureId ) );
		}

		return null;
	}

	private JSONObject handleSaveNodeProperties( HttpServletRequest request ) throws Exception
	{
		final int nodeType = getRequiredParameterInt( request, REQUEST_PARAMETER_NODE_TYPE );

		final FSPDatabaseWeb db = getDatabase();

		switch( nodeType )
		{
		case NODE_TYPE_STATE:
			db.updateStateNodeProperties(
				getRequiredParameterInt( request, REQUEST_PARAMETER_ID ),
				getRequiredParameterString( request, REQUEST_PARAMETER_LABEL ),
				getRequiredParameterString( request, REQUEST_PARAMETER_DESCRIPTION ),
				getRequiredParameterCalendar( request, REQUEST_PARAMETER_START ),
				getRequiredParameterCalendar( request, REQUEST_PARAMETER_END ),
				getRequiredParameterBoolean( request, REQUEST_PARAMETER_ACTIVE ),
				getRequiredParameterString( request, REQUEST_PARAMETER_COLOR ),
				getRequiredParameterDouble( request, REQUEST_PARAMETER_P ) );
			break;

		case NODE_TYPE_CONDITION_EVENT:
			db.updateConditionEventNodeProperties(
				getRequiredParameterInt( request, REQUEST_PARAMETER_ID ),
				getRequiredParameterString( request, REQUEST_PARAMETER_LABEL ),
				getRequiredParameterString( request, REQUEST_PARAMETER_DESCRIPTION ),
				// getRequiredParameterCalendar( request, REQUEST_PARAMETER_TIME_AT ),
				getRequiredParameterBoolean( request, REQUEST_PARAMETER_ACTIVE ) );
			break;

		default:
			throw new Exception( "Unknown node type: " + nodeType );
		}

		return null;
	}

	private JSONObject handleAddNode( HttpServletRequest request ) throws Exception
	{
		final int nodeType = getRequiredParameterInt( request, REQUEST_PARAMETER_NODE_TYPE );

		final DataLayer dataLayer = DataLayer.getInstance();
		dataLayer.setConnectionFromJNDI( getRawDatabaseConnection() );

		final FspRunTime runtime = new RunTime();
		runtime.init();

		final FspFeatureMgr featureManager = new FeatureMgr( dataLayer, runtime );
		featureManager.init();

		final FspStateMgr stateManager = new StateMgr( dataLayer, runtime, featureManager );
		stateManager.init();

		final int newNodeId;
		switch( nodeType )
		{
		case NODE_TYPE_STATE:
			// final String label = ;
			// final String description = ;
			// final String active = getRequiredParameterString( request, REQUEST_PARAMETER_ACTIVE );
			// final String color = getRequiredParameterString( request, REQUEST_PARAMETER_COLOR );
			// final String start = ;
			// final String end = ;
			// final String conditionEventId = getRequiredParameterString( request, REQUEST_PARAMETER_CONDITION_EVENT_ID );

			// final FspStateId stateId = new StateId( rawStateId );
			// final FspState previousState = stateManager.getState( stateId );
			//
			// final FspEdge edge =
			// stateManager.addState(
			// previousState,
			// WebCommon.parseDateTime( getRequiredParameterString( request, REQUEST_PARAMETER_START ) ).getTime(),
			// WebCommon.parseDateTime( getRequiredParameterString( request, REQUEST_PARAMETER_END ) ).getTime(),
			// getRequiredParameterString( request, REQUEST_PARAMETER_LABEL ),
			// getRequiredParameterString( request, REQUEST_PARAMETER_DESCRIPTION ) );

			// newNodeId = ( (StateId)edge.getNext() ).value;
			newNodeId = 123456;
			break;

		case NODE_TYPE_CONDITION_EVENT:
			// final String label = getRequiredParameterString( request, REQUEST_PARAMETER_LABEL );
			// final String description = getRequiredParameterString( request, REQUEST_PARAMETER_DESCRIPTION );
			// final String active = getRequiredParameterString( request, REQUEST_PARAMETER_ACTIVE );
			// final String color = getRequiredParameterString( request, REQUEST_PARAMETER_COLOR );
			// final String timeAt = getRequiredParameterString( request, REQUEST_PARAMETER_TIME_AT ); // ignore, not used in new database
			// final String conditionEventId = getRequiredParameterString( request, REQUEST_PARAMETER_CONDITION_EVENT_ID );

			// TODO finish Add Condition event!

			// stateManager.addCondition( type, label, desc );
			// stateManager.addConditionOption( option, edge );

			newNodeId = 123456;
			break;

		default:
			throw new Exception( "Unknown node type: " + nodeType );
		}

		final JSONObject response = new JSONObject();

		response.put( "newNodeId", newNodeId );

		return response;
	}

	// private JSONObject handleDeleteNode( HttpServletRequest request ) throws Exception
	// {
	// final int nodeType = getRequiredParameterInt( request, REQUEST_PARAMETER_NODE_TYPE );
	//
	// final FSPDatabaseWeb db = getDatabase();
	//
	// switch( nodeType )
	// {
	// case NODE_TYPE_STATE:
	// db.deleteState( getRequiredParameterInt( request, REQUEST_PARAMETER_ID ) );
	// break;
	//
	// case NODE_TYPE_CONDITION_EVENT:
	// db.deleteConditionEvent( getRequiredParameterInt( request, REQUEST_PARAMETER_ID ) );
	// break;
	//
	// default:
	// throw new Exception( "Unknown node type: " + nodeType );
	// }
	//
	// return null;
	// }

	// private JSONObject handleCreateMission( HttpServletRequest request ) throws Exception
	// {
	// final String title = getRequiredParameterString( request, REQUEST_PARAMETER_TITLE );
	//
	// final FSPDatabaseWeb db = getDatabase();
	//
	// final int newStateId = db.createMission( title );
	//
	// final JSONObject response = new JSONObject();
	// response.put( REQUEST_PARAMETER_ID, newStateId );
	//
	// return response;
	// }
}
