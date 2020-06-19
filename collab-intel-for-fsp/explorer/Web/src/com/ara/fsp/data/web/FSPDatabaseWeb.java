//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import com.ara.fsp.WebCommon;
import com.ara.fsp.data.web.CrowdQuestion.QuestionType;

public class FSPDatabaseWeb
{
	private final Connection m_conn;

	/**
	 * log4j logger
	 */
	private static final Logger LOGGER = Logger.getLogger( FSPDatabaseWeb.class );

	public FSPDatabaseWeb( final Connection conn ) throws Exception
	{
		if( conn == null )
			throw new Exception( "Database connection cannot be null!" );

		m_conn = conn;

		return;
	}

	public List< Mission > getMissions() throws Exception
	{
		try
		{
			final List< Mission > missions = new ArrayList< Mission >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT Id,Label,Description,Root,Horizon FROM missions ORDER BY Label" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				missions.add( new Mission( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getInt( 4 ), WebCommon.parseDateTime( rs
					.getTimestamp( 5 )
					.getTime() ) ) );
			}

			rs.close();
			sth.close();

			return missions;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< State > getStates() throws Exception
	{
		try
		{
			final List< State > states = new ArrayList< State >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,start,end,p,label,active,color,description FROM states ORDER BY id" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				states.add( new State( rs.getInt( 1 ), WebCommon.parseDateTime( rs.getTimestamp( 2 ).getTime() ), WebCommon.parseDateTime( rs
					.getTimestamp( 3 )
					.getTime() ), rs.getDouble( 4 ), rs.getString( 5 ), rs.getInt( 6 ) == 1, rs.getString( 7 ), rs.getString( 8 ) ) );
			}

			rs.close();
			sth.close();

			return states;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public State getState( final int stateId ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT start,end,p,label,active,color,description FROM states WHERE id=?" );
			sth.setInt( 1, stateId );

			final State state;

			final ResultSet rs = sth.executeQuery();
			if( rs.next() )
			{
				state =
					new State(
						stateId,
						WebCommon.parseDateTime( rs.getTimestamp( 1 ).getTime() ),
						WebCommon.parseDateTime( rs.getTimestamp( 2 ).getTime() ),
						rs.getDouble( 3 ),
						rs.getString( 4 ),
						rs.getInt( 5 ) == 1,
						rs.getString( 6 ),
						rs.getString( 7 ) );
			}
			else
			{
				state = null;
			}

			rs.close();
			sth.close();

			return state;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< Edge > getEdges() throws Exception
	{
		try
		{
			final List< Edge > edges = new ArrayList< Edge >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,label,description,p,prev,next FROM edges" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				edges.add( new Edge( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getDouble( 4 ), rs.getInt( 5 ), rs.getInt( 6 ) ) );
			}

			rs.close();
			sth.close();

			return edges;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< ConditionOption > getConditionOptions() throws Exception
	{
		try
		{
			final List< ConditionOption > conditionOptions = new ArrayList< ConditionOption >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,conditionid,edgeid,label,p FROM conditionoptions" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				conditionOptions.add( new ConditionOption( rs.getInt( 1 ), rs.getInt( 2 ), rs.getInt( 3 ), rs.getString( 4 ), rs.getDouble( 5 ) ) );
			}

			rs.close();
			sth.close();

			return conditionOptions;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public ConditionOption getConditionOption( final int conditionOptionId ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT conditionid,edgeid,label,p FROM conditionoptions WHERE id=?" );
			sth.setInt( 1, conditionOptionId );

			final ConditionOption conditionOption;

			final ResultSet rs = sth.executeQuery();
			if( rs.next() )
			{
				conditionOption = new ConditionOption( conditionOptionId, rs.getInt( 1 ), rs.getInt( 2 ), rs.getString( 3 ), rs.getDouble( 4 ) );
			}
			else
			{
				conditionOption = null;
			}

			rs.close();
			sth.close();

			return conditionOption;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< Condition > getConditions() throws Exception
	{
		try
		{
			final List< Condition > conditions = new ArrayList< Condition >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,type,label,description FROM conditions" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				conditions.add( new Condition( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ) ) );
			}

			rs.close();
			sth.close();

			return conditions;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public Condition getCondition( final int conditionId ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT type,label,description FROM conditions where id=?" );
			sth.setInt( 1, conditionId );

			final Condition condition;

			final ResultSet rs = sth.executeQuery();
			if( rs.next() )
			{
				condition = new Condition( conditionId, rs.getString( 1 ), rs.getString( 2 ), rs.getString( 3 ) );
			}
			else
			{
				condition = null;
			}

			rs.close();
			sth.close();

			return condition;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< EntityType > getEntityTypes() throws Exception
	{
		try
		{
			final List< EntityType > entityTypes = new ArrayList< EntityType >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,label FROM entitytypes" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				entityTypes.add( new EntityType( rs.getInt( 1 ), rs.getString( 2 ) ) );
			}

			rs.close();
			sth.close();

			return entityTypes;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< Entity > getEntities() throws Exception
	{
		try
		{
			final List< Entity > entities = new ArrayList< Entity >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,typeid,label,description FROM entities" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				entities.add( new Entity( rs.getInt( 1 ), rs.getInt( 2 ), rs.getString( 3 ), rs.getString( 4 ) ) );
			}

			rs.close();
			sth.close();

			return entities;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< FeatureMap > getFeatureMaps() throws Exception
	{
		try
		{
			final List< FeatureMap > featureMaps = new ArrayList< FeatureMap >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,label,featuretypeid,units FROM featuremaps" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				featureMaps.add( new FeatureMap( rs.getInt( 1 ), rs.getString( 2 ), rs.getInt( 3 ), rs.getString( 4 ) ) );
			}

			rs.close();
			sth.close();

			return featureMaps;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< Feature > getFeatures( final int stateId ) throws Exception
	{
		try
		{
			final List< Feature > features = new ArrayList< Feature >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,entityid,featuremapid,stateid,value,confidence FROM features WHERE StateId=?" );
			sth.setInt( 1, stateId );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				features.add( new Feature( rs.getInt( 1 ), rs.getInt( 2 ), rs.getInt( 3 ), rs.getInt( 4 ), rs.getString( 5 ), rs.getDouble( 6 ) ) );
			}

			rs.close();
			sth.close();

			return features;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< FeatureSlot > getFeatureSlot() throws Exception
	{
		try
		{
			final List< FeatureSlot > featuresSlots = new ArrayList< FeatureSlot >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,entitytypeid,featuremapid FROM featureslots" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				featuresSlots.add( new FeatureSlot( rs.getInt( 1 ), rs.getInt( 2 ), rs.getInt( 3 ) ) );
			}

			rs.close();
			sth.close();

			return featuresSlots;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< FeatureType > getFeatureTypes() throws Exception
	{
		try
		{
			final List< FeatureType > featureTypes = new ArrayList< FeatureType >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth = m_conn.prepareStatement( "SELECT id,jar,className,label,description FROM featureslots" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				featureTypes.add( new FeatureType( rs.getInt( 1 ), rs.getString( 2 ), rs.getString( 3 ), rs.getString( 4 ), rs.getString( 5 ) ) );
			}

			rs.close();
			sth.close();

			return featureTypes;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public void updateFeature( final int featureId, final String value ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			final PreparedStatement updateStatement = m_conn.prepareStatement( "UPDATE features SET value=? WHERE id=?" );
			updateStatement.setString( 1, value );
			updateStatement.setInt( 2, featureId );
			updateStatement.executeUpdate();
			updateStatement.close();
			
			// Also deactivate any feature questions that are looking to populate this feature
			final PreparedStatement deactiveStatement = m_conn.prepareStatement( "UPDATE ciquestions as q, citargetfeatures as f SET q.active=false WHERE f.featureid=? AND q.id=f.questionid;" );
			deactiveStatement.setInt(1, featureId );
			deactiveStatement.executeUpdate();
			deactiveStatement.close();
			
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}

		return;
	}

	public void updateStateNodeProperties(
		final int stateId,
		final String label,
		final String description,
		final Calendar start,
		final Calendar end,
		final boolean isActive,
		final String color,
		final double p ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			final PreparedStatement updateStatement =
				m_conn.prepareStatement( "UPDATE states SET label=?,description=?,start=?,end=?,active=?,color=?,p=? WHERE Id=?" );
			updateStatement.setString( 1, label );
			updateStatement.setString( 2, description );
			updateStatement.setString( 3, WebCommon.formatDateTime( start ) );
			updateStatement.setString( 4, WebCommon.formatDateTime( end ) );
			updateStatement.setInt( 5, isActive ? 1 : 0 );
			updateStatement.setString( 6, color );
			updateStatement.setDouble( 7, p );
			updateStatement.setInt( 8, stateId );

			updateStatement.executeUpdate();

			updateStatement.close();
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}

		return;
	}

	public void updateConditionEventNodeProperties( final int conditionEventId, final String label, final String description, final boolean isActive )
		throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			// final PreparedStatement updateStatement =
			// m_conn.prepareStatement( "UPDATE ConditionEvents SET Title=?,Description=?,TimeAt=?,IsActive=?,ColorRGB=? WHERE Id=?" );
			// updateStatement.setString( 1, title );
			// updateStatement.setString( 2, description );
			// updateStatement.setString( 3, WebCommon.formatDateTime( timeAt ) );
			// updateStatement.setInt( 4, isActive ? 1 : 0 );
			// updateStatement.setString( 5, color );
			// updateStatement.setInt( 6, conditionEventId );
			//
			// updateStatement.executeUpdate();
			//
			// updateStatement.close();
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}

		return;
	}

	public void deleteState( final int stateId ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			final PreparedStatement updateStatement = m_conn.prepareStatement( "DELETE FROM States WHERE id=?" );
			updateStatement.setInt( 1, stateId );

			updateStatement.executeUpdate();

			updateStatement.close();
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}

		return;
	}

	// public void deleteConditionEvent( final int conditionEventId ) throws Exception
	// {
	// try
	// {
	// m_conn.setAutoCommit( true );
	//
	// final PreparedStatement updateStatement = m_conn.prepareStatement( "UPDATE ConditionEvents SET SoftDeleted=1 WHERE Id=?" );
	// updateStatement.setInt( 1, conditionEventId );
	//
	// updateStatement.executeUpdate();
	//
	// updateStatement.close();
	// }
	// catch( final SQLException e )
	// {
	// LOGGER.error( e );
	// throw new Exception( "Database error: " + e.getMessage() );
	// }
	//
	// return;
	// }

	// public int createRootNode( final String title ) throws Exception
	// {
	// try
	// {
	// m_conn.setAutoCommit( false );
	//
	// // create the actual node
	// final Calendar start = Calendar.getInstance();
	// final Calendar end = Calendar.getInstance();
	// end.add( Calendar.DAY_OF_YEAR, 31 );
	//
	// final PreparedStatement insertStatesStatement =
	// m_conn.prepareStatement(
	// "INSERT INTO States(Title,Description,TimeStart,TimeEnd,IsActive,ColorRGB,SoftDeleted,ConditionEventId) VALUES(?,?,?,?,?,?,0,NULL)",
	// Statement.RETURN_GENERATED_KEYS );
	// insertStatesStatement.setString( 1, "Initial state" );
	// insertStatesStatement.setString( 2, "Initial state" );
	// insertStatesStatement.setString( 3, DATE_FORMATTER.format( start.getTime() ) );
	// insertStatesStatement.setString( 4, DATE_FORMATTER.format( end.getTime() ) );
	// insertStatesStatement.setInt( 5, 1 );
	// insertStatesStatement.setString( 6, "255,0,0" );
	//
	// insertStatesStatement.executeUpdate();
	//
	// final ResultSet rs = insertStatesStatement.getGeneratedKeys();
	// if( !rs.next() )
	// throw new Exception( "Failure getting the inserted feature id!" );
	// final int stateId = rs.getInt( 1 );
	//
	// // create the root node entry
	// final PreparedStatement insertTreeRootNodeStatement = m_conn.prepareStatement(
	// "INSERT INTO TreeRootNodes(Title,RootStateId) VALUES(?,?)" );
	// insertTreeRootNodeStatement.setString( 1, title );
	// insertTreeRootNodeStatement.setInt( 2, stateId );
	//
	// insertTreeRootNodeStatement.executeUpdate();
	//
	// m_conn.commit();
	//
	// insertStatesStatement.close();
	//
	// return stateId;
	// }
	// catch( final SQLException e )
	// {
	// LOGGER.error( e );
	//
	// m_conn.rollback();
	//
	// throw new Exception( "Database error: " + e.getMessage() );
	// }
	// }

	public List< CrowdQuestionAndAnswerCount > getCrowdQuestionsAndAnswerCounts() throws Exception
	{
		try
		{
			final List< CrowdQuestionAndAnswerCount > questions = new ArrayList< CrowdQuestionAndAnswerCount >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth =
				m_conn.prepareStatement( "SELECT "
					+ "Q.id,"
					+ "Q.questiontype,"
					+ "Q.label,"
					+ "Q.context,"
					+ "Q.question,"
					+ "Q.active,"
					+ "(SELECT COUNT(*) FROM cicrowdinput AS A WHERE A.questionid=Q.id) "
					+ "FROM ciquestions AS Q "
					+ "ORDER BY Q.Id" );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				final CrowdQuestion question =
					new CrowdQuestion(
						rs.getInt( 1 ),
						QuestionType.fromString( rs.getString( 2 ) ),
						rs.getString( 3 ),
						rs.getString( 4 ),
						rs.getString( 5 ),
						rs.getInt( 6 ) == 1 );

				final int currentAnswerCount = rs.getInt( 7 );

				questions.add( new CrowdQuestionAndAnswerCount( question, currentAnswerCount ) );
			}

			rs.close();
			sth.close();

			return questions;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public List< CrowdAnswer > getCrowdAnswers( final int questionId ) throws Exception
	{
		try
		{
			final List< CrowdAnswer > answers = new ArrayList< CrowdAnswer >();

			m_conn.setAutoCommit( false );

			final PreparedStatement sth =
				m_conn.prepareStatement( "SELECT id,site,user,date,value,confidence FROM cicrowdinput WHERE questionid=? ORDER BY date,id" );
			sth.setInt( 1, questionId );

			final ResultSet rs = sth.executeQuery();
			while( rs.next() )
			{
				answers.add( new CrowdAnswer( rs.getInt( 1 ), questionId, rs.getString( 2 ), rs.getInt( 3 ), WebCommon.parseDateTime( rs
					.getTimestamp( 4 )
					.getTime() ), rs.getString( 5 ), rs.getDouble( 6 ) ) );
			}

			rs.close();
			sth.close();

			return answers;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public int addCrowdAnswer( final int questionId, final String site, final int userId, final Calendar date, final String value, final double confidence )
		throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			final PreparedStatement insertStatement =
				m_conn.prepareStatement(
					"INSERT INTO cicrowdinput(questionid,site,user,date,value,confidence) VALUES(?,?,?,?,?,?)",
					Statement.RETURN_GENERATED_KEYS );
			insertStatement.setInt( 1, questionId );
			insertStatement.setString( 2, site );
			insertStatement.setInt( 3, userId );
			insertStatement.setString( 4, WebCommon.formatDateTime( date ) );
			insertStatement.setString( 5, value );
			insertStatement.setDouble( 6, confidence );

			insertStatement.executeUpdate();

			final ResultSet rs = insertStatement.getGeneratedKeys();
			if( !rs.next() )
				throw new Exception( "Failure getting the inserted feature id!" );
			final int answerId = rs.getInt( 1 );

			rs.close();
			insertStatement.close();

			return answerId;
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}
	}

	public void deleteCrowdAnswers( final int questionId ) throws Exception
	{
		try
		{
			m_conn.setAutoCommit( true );

			final PreparedStatement deleteStatement = m_conn.prepareStatement( "DELETE FROM cicrowdinput WHERE questionid=?" );
			deleteStatement.setInt( 1, questionId );

			deleteStatement.executeUpdate();

			deleteStatement.close();
		}
		catch( final SQLException e )
		{
			LOGGER.error( e );
			throw new Exception( "Database error: " + e.getMessage() );
		}

		return;
	}
}
