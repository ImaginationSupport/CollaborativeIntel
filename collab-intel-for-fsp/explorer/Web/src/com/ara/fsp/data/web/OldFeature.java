//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public class OldFeature implements FSPWebObject, Identifiable, Comparable< OldFeature >
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_ENTITY_NAME = "entityName";
	public static final String JSONKEY_KEY = "key";
	public static final String JSONKEY_VALUE = "value";
	public static final String JSONKEY_VALUE_TYPE = "valueType";

	private final int m_id;
	private final String m_entityName;
	private final String m_key;
	private final String m_value;
	private final int m_valueType;

	public int getId()
	{
		return m_id;
	}

	public String getEntityName()
	{
		return m_entityName;
	}

	public String getKey()
	{
		return m_key;
	}

	public String getValue()
	{
		return m_value;
	}

	public int getValueType()
	{
		return m_valueType;
	}

	public OldFeature( final int id, final String entityName, final String key, final String value, final int valueType ) throws ArgumentException
	{
		m_id = id;
		m_entityName = entityName;
		m_key = key;
		m_value = value;
		m_valueType = valueType;

		return;
	}

	public OldFeature( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_entityName = sourceJSON.getString( JSONKEY_ENTITY_NAME );
		m_key = sourceJSON.getString( JSONKEY_KEY );
		m_value = sourceJSON.getString( JSONKEY_VALUE );
		m_valueType = sourceJSON.getInt( JSONKEY_VALUE_TYPE );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_ENTITY_NAME, m_entityName );
		json.put( JSONKEY_KEY, m_key );
		json.put( JSONKEY_VALUE, m_value );
		json.put( JSONKEY_VALUE_TYPE, m_valueType );

		return json;
	}

	@Override
	public int compareTo( OldFeature other )
	{
		if( m_entityName == other.m_entityName )
			return m_key.compareTo( other.m_key );
		else
			return m_entityName.compareTo( other.m_entityName );
	}
}
