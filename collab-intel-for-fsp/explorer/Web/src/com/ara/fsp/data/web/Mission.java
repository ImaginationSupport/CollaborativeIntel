//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.util.Calendar;

import org.json.JSONObject;

import com.ara.fsp.WebCommon;
import com.ara.fsp.exceptions.ArgumentException;

public final class Mission implements FSPWebObject
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";
	public static final String JSONKEY_ROOT_ID = "rootId";
	public static final String JSONKEY_HORIZON = "horizon";

	private final int m_id;
	private final String m_label;
	private final String m_description;
	private final int m_rootId;
	private final Calendar m_horizon;

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

	public int getRootId()
	{
		return m_rootId;
	}

	public Calendar getHorizon()
	{
		return m_horizon;
	}

	public Mission( final int id, final String label, final String description, final int rootId, final Calendar horizon )
	{
		m_id = id;
		m_label = label;
		m_description = description;
		m_rootId = rootId;
		m_horizon = horizon;

		return;
	}

	public Mission( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );
		m_rootId = sourceJSON.getInt( JSONKEY_ROOT_ID );
		m_horizon = WebCommon.parseDateTime( sourceJSON.getString( JSONKEY_HORIZON ) );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );
		json.put( JSONKEY_ROOT_ID, m_rootId );
		json.put( JSONKEY_HORIZON, WebCommon.formatDateTime( m_horizon ) );
		
		return json;
	}
}
