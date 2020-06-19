//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class Entity implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_TYPEID = "typeid";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";

	private final int m_id;
	private final int m_typeId;
	private final String m_label;
	private final String m_description;

	public int getId()
	{
		return m_id;
	}

	public int getTypeId()
	{
		return m_typeId;
	}

	public String getLabel()
	{
		return m_label;
	}

	public String getDescription()
	{
		return m_description;
	}

	public Entity( final int id, final int typeId, final String label, final String description ) throws ArgumentException
	{
		m_id = id;
		m_typeId = typeId;
		m_label = label;
		m_description = description;

		return;
	}

	public Entity( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_typeId = sourceJSON.getInt( JSONKEY_TYPEID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_TYPEID, m_typeId );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );

		return json;
	}
	
//	@Override
//	public String toString()
//	{
//		return "hello, world?";
//	}
}