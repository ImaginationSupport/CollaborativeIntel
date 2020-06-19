//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.util.Calendar;

import org.json.JSONObject;

import com.ara.fsp.WebCommon;
import com.ara.fsp.exceptions.ArgumentException;

public final class CrowdAnswer implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_QUESTIONID = "questionid";
	public static final String JSONKEY_SITE = "site";
	public static final String JSONKEY_USER = "user";
	public static final String JSONKEY_DATE = "date";
	public static final String JSONKEY_VALUE = "value";
	public static final String JSONKEY_CONFIDENCE = "confidence";

	private final int m_id;
	private final int m_questionId;
	private final String m_site;
	private final int m_user;
	private final Calendar m_date;
	private final String m_value;
	private final double m_confidence;

	public int getId()
	{
		return m_id;
	}

	public int getQuestionId()
	{
		return m_questionId;
	}

	public String getSite()
	{
		return m_site;
	}

	public int getUser()
	{
		return m_user;
	}

	public Calendar getDate()
	{
		return m_date;
	}

	public String getValue()
	{
		return m_value;
	}

	public double getConfidence()
	{
		return m_confidence;
	}

	public CrowdAnswer( final int id, final int questionId, final String site, final int user, final Calendar date, final String value, final double confidence )
		throws ArgumentException
	{
		m_id = id;
		m_questionId = questionId;
		m_site = site;
		m_user = user;
		m_date = date;
		m_value = value;
		m_confidence = confidence;

		return;
	}

	public CrowdAnswer( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_questionId = sourceJSON.getInt( JSONKEY_QUESTIONID );
		m_site = sourceJSON.getString( JSONKEY_SITE );
		m_user = sourceJSON.getInt( JSONKEY_USER );
		m_date = WebCommon.parseDateTime( sourceJSON.getString( JSONKEY_DATE ) );
		m_value = sourceJSON.getString( JSONKEY_VALUE );
		m_confidence = sourceJSON.getDouble( JSONKEY_CONFIDENCE );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_QUESTIONID, m_questionId );
		json.put( JSONKEY_SITE, m_site );
		json.put( JSONKEY_USER, m_user );
		json.put( JSONKEY_DATE, WebCommon.formatDateTime( m_date ) );
		json.put( JSONKEY_VALUE, m_value );
		json.put( JSONKEY_CONFIDENCE, m_confidence );

		return json;
	}
}
