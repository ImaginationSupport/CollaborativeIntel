//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class FeatureType implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_JAR = "jar";
	public static final String JSONKEY_CLASSNAME = "className";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";

	private final int m_id;
	private final String m_jar;
	private final String m_className;
	private final String m_label;
	private final String m_description;

	public int getId()
	{
		return m_id;
	}

	public String getJar()
	{
		return m_jar;
	}

	public String getClassName()
	{
		return m_className;
	}

	public String getLabel()
	{
		return m_label;
	}

	public String getDescription()
	{
		return m_description;
	}

	public FeatureType( final int id, final String jar, final String className, final String label, final String description ) throws ArgumentException
	{
		m_id = id;
		m_jar = jar;
		m_className = className;
		m_label = label;
		m_description = description;

		return;
	}

	public FeatureType( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_jar = sourceJSON.getString( JSONKEY_JAR );
		m_className = sourceJSON.getString( JSONKEY_CLASSNAME );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_JAR, m_jar );
		json.put( JSONKEY_CLASSNAME, m_className );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );

		return json;
	}
}