//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.ara.fsp.exceptions.ArgumentException;

public class WebCommon
{
	/**
	 * Holds the formatter for date/time values
	 */
	public static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat( "yyyy-MM-dd HH:mm:ss" );

	public static final Calendar parseDateTime( final String source ) throws ArgumentException
	{
		Calendar cal = new GregorianCalendar();

		try
		{
			cal.setTime( DATE_FORMATTER.parse( source ) );
		}
		catch( ParseException e )
		{
			throw new ArgumentException( "Error parsing datetime \"" + source + "\": " + e.getMessage() );
		}

		return cal;
	}

	public static final Calendar parseDateTime( final long fromTimeMilliseconds )
	{
		Calendar cal = new GregorianCalendar();

		cal.setTimeInMillis( fromTimeMilliseconds );

		return cal;
	}

	public static final String formatDateTime( final Calendar source ) throws ArgumentException
	{
		if( source == null )
			throw new ArgumentException( "Source cannot be null!" );

		return DATE_FORMATTER.format( source.getTime() );
	}

	public static final String formatDateTime( final long fromTimeMilliseconds ) throws ArgumentException
	{
		final Calendar source = Calendar.getInstance();

		source.setTimeInMillis( fromTimeMilliseconds );

		return DATE_FORMATTER.format( source.getTime() );
	}
}
