//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.ara.fsp.data.web.FSPDatabaseWeb;
import com.ara.fsp.data.web.FSPWebObject;
import com.ara.fsp.data.web.Identifiable;
import com.ara.fsp.exceptions.ArgumentException;

public abstract class FSPServletBase extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/**
	 * Holds the JNDI name of the database
	 */
	private static final String JNDI_FSP = "...filled in by ant during build...";

	/**
	 * log4j logger
	 */
	private static final Logger LOGGER = Logger.getLogger( FSPServletBase.class );

	private static final String RESPONSE_JSON_PARAM_SUCCESS = "success";
	private static final String RESPONSE_JSON_PARAM_MESSAGE = "message";

	/**
	 * Updates the given HTTP response for a successful method
	 *
	 * @param response
	 *            The outgoing HTTP response to update
	 * @param responseData
	 *            the JSON response to send
	 * @param isDebugging
	 *            true if the debugging flag was set
	 *
	 * @throws IOException
	 */
	protected void sendSuccessfulResponse( HttpServletResponse response, JSONObject responseData, boolean isDebugging ) throws IOException
	{
		if( responseData == null )
			responseData = new JSONObject();

		responseData.put( RESPONSE_JSON_PARAM_SUCCESS, true );

		sendResponse( response, responseData, isDebugging );

		return;

	}

	/**
	 * Updates the given HTTP response for a successful method
	 *
	 * @param response
	 *            The outgoing HTTP response to update
	 * @param errorMessage
	 *            The error message to send
	 * @param isDebugging
	 *            True if the debugging flag was set
	 *
	 * @throws IOException
	 */
	protected void sendFailureResponse( HttpServletResponse response, String errorMessage, boolean isDebugging ) throws IOException
	{
		JSONObject json = new JSONObject();

		json.put( RESPONSE_JSON_PARAM_SUCCESS, false );
		json.put( RESPONSE_JSON_PARAM_MESSAGE, errorMessage );

		sendResponse( response, json, isDebugging );

		return;
	}

	protected void sendResponse( HttpServletResponse response, JSONObject json, boolean isDebugging ) throws IOException
	{
		try
		{
			response.setContentType( "application/json" );
			final PrintWriter out = response.getWriter();

			if( isDebugging )
				out.write( json.toString( 4 ) );
			else
				out.write( json.toString() );

			out.close();
		}
		catch( Exception e )
		{
			LOGGER.error( "Error generating JSON and sending response: " + e );
			response.sendError( HttpServletResponse.SC_INTERNAL_SERVER_ERROR );
		}

		return;
	}

	/**
	 * Gets a string value coming in from the HTTP request, or throws an exception if the parameter could not be found
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter
	 * @throws Exception
	 */
	protected static String getRequiredParameterString( final HttpServletRequest request, final String parameterName ) throws ArgumentException
	{
		final String parameterValue = request.getParameter( parameterName );

		if( parameterValue == null )
			throw new ArgumentException( "Missing required parameter: " + parameterName );

		return parameterValue;
	}

	/**
	 * Gets an integer value coming in from the HTTP request, or throws an exception if the parameter could not be found or parsed
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter
	 * @throws Exception
	 */
	protected static int getRequiredParameterInt( final HttpServletRequest request, final String parameterName ) throws ArgumentException
	{
		final String parameterValue = request.getParameter( parameterName );

		if( parameterValue == null )
			throw new ArgumentException( "Missing required parameter: " + parameterName );

		return Integer.parseInt( parameterValue );
	}

	/**
	 * Gets an double value coming in from the HTTP request, or throws an exception if the parameter could not be found or parsed
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter
	 * @throws Exception
	 */
	protected static double getRequiredParameterDouble( final HttpServletRequest request, final String parameterName ) throws ArgumentException
	{
		final String parameterValue = request.getParameter( parameterName );

		if( parameterValue == null )
			throw new ArgumentException( "Missing required parameter: " + parameterName );

		return Double.parseDouble( parameterValue );
	}

	/**
	 * Gets a boolean value coming in from the HTTP request, or throws an exception if the parameter could not be found or parsed
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter
	 * @throws Exception
	 */
	protected static boolean getRequiredParameterBoolean( final HttpServletRequest request, final String parameterName ) throws ArgumentException
	{
		final String parameterValue = request.getParameter( parameterName );

		if( parameterValue == null )
			throw new ArgumentException( "Missing required parameter: " + parameterName );

		return Boolean.parseBoolean( parameterValue );
	}

	/**
	 * Gets a calendar value coming in from the HTTP request, or throws an exception if the parameter could not be found or parsed
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter
	 * @throws Exception
	 */
	protected static Calendar getRequiredParameterCalendar( final HttpServletRequest request, final String parameterName ) throws ArgumentException
	{
		final String parameterValue = request.getParameter( parameterName );

		if( parameterValue == null )
			throw new ArgumentException( "Missing required parameter: " + parameterName );

		return WebCommon.parseDateTime( parameterValue );
	}

	/**
	 * Gets a string value coming in from the HTTP request, or null if the parameter could not be found
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @return the value of the parameter, or null if it could not be found
	 */
	protected static String getOptionalParameterString( final HttpServletRequest request, final String parameterName )
	{
		return request.getParameter( parameterName );
	}

	/**
	 * Gets an integer value coming in from the HTTP request, or the default value if the parameter could not be found
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @param defaultValue
	 *            the default if the parameter was not in the request
	 * @return the value of the parameter, or the default value if it could not be found
	 */
	protected static int getOptionalParameterInt( final HttpServletRequest request, final String parameterName, final int defaultValue )
	{
		if( request.getParameter( parameterName ) == null )
			return defaultValue;
		else
			return Integer.parseInt( request.getParameter( parameterName ) );
	}

	/**
	 * Gets a double value coming in from the HTTP request, or the default value if the parameter could not be found
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @param defaultValue
	 *            the default if the parameter was not in the request
	 * @return the value of the parameter, or the default value if it could not be found
	 */
	protected static double getOptionalParameterDouble( final HttpServletRequest request, final String parameterName, final double defaultValue )
	{
		if( request.getParameter( parameterName ) == null )
			return defaultValue;
		else
			return Double.parseDouble( request.getParameter( parameterName ) );
	}

	/**
	 * Gets an integer value coming in from the HTTP request, or the default value if the parameter could not be found
	 *
	 * @param request
	 *            the HttpServletRequest coming in
	 * @param parameterName
	 *            the parameter name to find
	 * @param defaultValue
	 *            the default if the parameter was not in the request
	 * @return the value of the parameter, or the default value if it could not be found
	 */
	protected static boolean getOptionalParameterBoolean( final HttpServletRequest request, final String parameterName, final boolean defaultValue )
	{
		if( request.getParameter( parameterName ) == null )
			return defaultValue;
		else
			return Boolean.parseBoolean( request.getParameter( parameterName ) );
	}

	protected static < T extends FSPWebObject > JSONArray toJSONArray( final Collection< T > collection ) throws ArgumentException
	{
		final JSONArray jsonArray = new JSONArray();

		final Iterator< T > iter = collection.iterator();
		while( iter.hasNext() )
		{
			jsonArray.put( iter.next().toJSON() );
		}

		return jsonArray;
	}

	protected static < T extends Identifiable > Map< Integer, T > createLookupTable( final List< T > list )
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
	protected static FSPDatabaseWeb getDatabase() throws Exception
	{

		return new FSPDatabaseWeb( getRawDatabaseConnection() );
	}

	protected static Connection getRawDatabaseConnection() throws Exception
	{
		final Context context = new InitialContext();
		final DataSource ds = (DataSource)context.lookup( JNDI_FSP );
		return ds.getConnection();
	}
}
