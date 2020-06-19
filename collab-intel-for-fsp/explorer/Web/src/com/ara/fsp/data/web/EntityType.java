//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class EntityType implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_LABEL = "label";

	private final int m_id;
	private final String m_label;

	public int getId()
	{
		return m_id;
	}

	public String getLabel()
	{
		return m_label;
	}

	public EntityType( final int id, final String label ) throws ArgumentException
	{
		m_id = id;
		m_label = label;

		return;
	}

	public EntityType( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_LABEL, m_label );

		return json;
	}
}