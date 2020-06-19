//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.util.Calendar;

import org.json.JSONObject;

import com.ara.fsp.WebCommon;
import com.ara.fsp.exceptions.ArgumentException;

public class OldConditionEvent implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";
	public static final String JSONKEY_TIME_AT = "timeAt";
	public static final String JSONKEY_ACTIVE = "active";
	public static final String JSONKEY_COLOR = "color";
	public static final String JSONKEY_STATE_ID = "stateId";

	private final int m_id;
	private final String m_label;
	private final String m_description;
	private final Calendar m_timeAt;
	private final boolean m_active;
	private final String m_color;
	private final int m_stateId;

	public int getId()
	{
		return m_id;
	}

	public String getLabel()
	{
		return m_label;
	}

	public String getDescription()
	{
		return m_description;
	}

	public Calendar getTimeAt()
	{
		return m_timeAt;
	}

	public boolean isActive()
	{
		return m_active;
	}

	public String getColor()
	{
		return m_color;
	}

	public int getStateId()
	{
		return m_stateId;
	}

	public OldConditionEvent(
		final int id,
		final String label,
		final String description,
		final Calendar timeAt,
		final boolean active,
		final String color,
		final int stateId ) throws ArgumentException
	{
		m_id = id;
		m_label = label;
		m_description = description;
		m_timeAt = timeAt;
		m_active = active;
		m_color = color;
		m_stateId = stateId;

		return;
	}

	public OldConditionEvent( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );
		m_timeAt = WebCommon.parseDateTime( sourceJSON.getString( JSONKEY_TIME_AT ) );
		m_active = sourceJSON.getBoolean( JSONKEY_ACTIVE );
		m_color = sourceJSON.getString( JSONKEY_COLOR );
		m_stateId = sourceJSON.getInt( JSONKEY_STATE_ID );
		
		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );
		json.put( JSONKEY_TIME_AT, WebCommon.formatDateTime( m_timeAt ) );
		json.put( JSONKEY_ACTIVE, m_active );
		json.put( JSONKEY_COLOR, m_color );
		json.put( JSONKEY_STATE_ID, m_stateId );
		
		return json;
	}
}
