//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.util.Calendar;

import org.json.JSONObject;

import com.ara.fsp.WebCommon;
import com.ara.fsp.exceptions.ArgumentException;

public final class OldState implements FSPWebObject, Identifiable, Comparable< OldState >
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_START = "start";
	public static final String JSONKEY_END = "end";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";
	public static final String JSONKEY_ACTIVE = "active";
	public static final String JSONKEY_COLOR = "color";
	public static final String JSONKEY_CONDITION_EVENT_ID = "conditionEventId";

	private final int m_id;
	private final Calendar m_start;
	private final Calendar m_end;
	private final String m_label;
	private final String m_description;
	private final boolean m_active;
	private final String m_color;
	private final int m_conditionEventId;

	public int getId()
	{
		return m_id;
	}

	public Calendar getStart()
	{
		return m_start;
	}

	public Calendar getEnd()
	{
		return m_end;
	}

	public String getLabel()
	{
		return m_label;
	}

	public String getDescription()
	{
		return m_description;
	}

	public boolean isActive()
	{
		return m_active;
	}

	public String getColor()
	{
		return m_color;
	}

	public int getConditionEventId()
	{
		return m_conditionEventId;
	}

	public OldState(
		final int id,
		final Calendar start,
		final Calendar end,
		final String label,
		final String description,
		final boolean active,
		final String color,
		final int conditionEventId ) throws ArgumentException
	{
		m_id = id;
		m_start = start;
		m_end = end;
		m_label = label;
		m_description = description;
		m_active = active;
		m_color = color;
		m_conditionEventId = conditionEventId;

		return;
	}

	public OldState( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_start = WebCommon.parseDateTime( sourceJSON.getString( JSONKEY_START ) );
		m_end = WebCommon.parseDateTime( sourceJSON.getString( JSONKEY_END ) );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );
		m_active = sourceJSON.getBoolean( JSONKEY_ACTIVE );
		m_color = sourceJSON.getString( JSONKEY_COLOR );
		m_conditionEventId = sourceJSON.getInt( JSONKEY_CONDITION_EVENT_ID );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_START, WebCommon.formatDateTime( m_start ) );
		json.put( JSONKEY_END, WebCommon.formatDateTime( m_end ) );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );
		json.put( JSONKEY_ACTIVE, m_active );
		json.put( JSONKEY_COLOR, m_color );
		json.put( JSONKEY_CONDITION_EVENT_ID, m_conditionEventId );

		return json;
	}

	@Override
	public int compareTo( final OldState other )
	{
		return m_start == other.m_start ? m_label.compareTo( other.m_label ) : m_start.compareTo( other.m_start );
	}
}