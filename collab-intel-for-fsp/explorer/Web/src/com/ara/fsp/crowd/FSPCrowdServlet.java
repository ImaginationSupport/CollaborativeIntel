//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.crowd;

import java.io.IOException;
import java.util.Calendar;
import java.util.Random;

import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.ara.fsp.FSPServletBase;
import com.ara.fsp.data.web.FSPDatabaseWeb;
import com.ara.fsp.exceptions.ArgumentException;

/**
 * Servlet to handle the backend for the FSP Explorer
 */
public class FSPCrowdServlet extends FSPServletBase
{
	private static final long serialVersionUID = 1L;

	private static final String REQUEST_PARAMETER_QUERY_ACTION = "q";
	private static final String REQUEST_PARAMETER_IS_DEBUGGING = "debug";
	private static final String REQUEST_PARAMETER_ID = "id";
	private static final String REQUEST_PARAMETER_COUNT = "count";
	private static final String REQUEST_PARAMETER_MIN = "min";
	private static final String REQUEST_PARAMETER_MAX = "max";
	private static final String REQUEST_PARAMETER_REMOVE_EXISTING = "removeExisting";

	private static final String REQUEST_GET_QUESTIONS = "getQuestions";

	private static final String REQUEST_GET_EXISTING_ANSWERS = "getExistingAnswers";

	private static final String REQUEST_GENERATE_ANSWERS_INTEGER = "generateAnswersInteger";
	// private static final String REQUEST_GENERATE_ANSWERS_CHOICES = "generateAnswersChoices";

	/**
	 * log4j logger
	 */
	private static final Logger LOGGER = Logger.getLogger( FSPCrowdServlet.class );

	/**
	 * @see HttpServlet#HttpServlet()
	 */
	public FSPCrowdServlet()
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
		LOGGER.debug( "FSP Explorer servlet initialized!" );

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
			case REQUEST_GET_QUESTIONS:
				data = handleGetQuestions( request );
				break;

			case REQUEST_GET_EXISTING_ANSWERS:
				data = handleGetExistingAnswers( request );
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
			case REQUEST_GENERATE_ANSWERS_INTEGER:
				data = handleGenerateAnswersInteger( request );
				break;

			// case REQUEST_GENERATE_ANSWERS_CHOICES:
			// data = handleGenerateAnswersChoices( request );
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

	private JSONObject handleGetQuestions( HttpServletRequest request ) throws Exception
	{
		final FSPDatabaseWeb db = getDatabase();

		final JSONObject response = new JSONObject();

		response.put( "questions", toJSONArray( db.getCrowdQuestionsAndAnswerCounts() ) );

		return response;
	}

	private JSONObject handleGetExistingAnswers( HttpServletRequest request ) throws Exception
	{
		final int questionId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );

		final FSPDatabaseWeb db = getDatabase();

		final JSONObject response = new JSONObject();

		response.put( "answers", toJSONArray( db.getCrowdAnswers( questionId ) ) );

		return response;
	}

	private JSONObject handleGenerateAnswersInteger( HttpServletRequest request ) throws Exception
	{
		final int questionId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );
		final int count = getRequiredParameterInt( request, REQUEST_PARAMETER_COUNT );
		final int min = getRequiredParameterInt( request, REQUEST_PARAMETER_MIN );
		final int max = getRequiredParameterInt( request, REQUEST_PARAMETER_MAX );
		final boolean removeExisting = getRequiredParameterBoolean( request, REQUEST_PARAMETER_REMOVE_EXISTING );

		final String site = "auto-generated";
		final int userId = 0;

		final FSPDatabaseWeb db = getDatabase();

		if( removeExisting )
			db.deleteCrowdAnswers( questionId );

		final Calendar cal = Calendar.getInstance();
		final Random rng = new Random();

		for( int i = 0; i < count; ++i )
			db.addCrowdAnswer( questionId, site, userId, cal, Integer.toString( min + rng.nextInt( max - min + 1 ) ), rng.nextDouble() );

		final JSONObject response = new JSONObject();

		response.put( "answers", toJSONArray( db.getCrowdAnswers( questionId ) ) );

		return response;
	}

	// private JSONObject handleGenerateAnswersChoices( HttpServletRequest request ) throws Exception
	// {
	// final int questionId = getRequiredParameterInt( request, REQUEST_PARAMETER_ID );
	// final int count = getRequiredParameterInt( request, REQUEST_PARAMETER_COUNT );
	// final boolean removeExisting = getRequiredParameterBoolean( request, REQUEST_PARAMETER_REMOVE_EXISTING );
	//
	// final FSPDatabaseWeb db = getDatabase();
	//
	// if( removeExisting )
	// db.deleteCrowdAnswers( questionId );
	//
	// final Calendar cal = Calendar.getInstance();
	// final Random rng = new Random();
	//
	// final List< JSONObject > choices = db.getCrowdAnswerChoices( questionId );
	//
	// for( int i = 0; i < count; ++i )
	// db.addCrowdAnswer( questionId, choices.get( rng.nextInt( choices.size() ) ).getString( "value" ), rng.nextInt( 101 ), cal );
	//
	// final JSONObject response = new JSONObject();
	//
	// response.put( "answers", db.getCrowdAnswers( questionId ) );
	//
	// return response;
	// }
}
