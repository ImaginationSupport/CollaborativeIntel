//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import java.util.List;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public class EntityWithFeatures implements FSPWebObject
{
	public static final String JSONKEY_ENTITY = "entity";
	public static final String JSONKEY_FEATURES = "features";

	private final Entity m_entity;
	private final List< Feature > m_features;

	public Entity getEntity()
	{
		return m_entity;
	}

	public List< Feature > getFeatures()
	{
		return m_features;
	}

	public EntityWithFeatures( final Entity entity, final List< Feature > features )
	{
		m_entity = entity;
		m_features = features;

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ENTITY, m_entity );
		json.put( JSONKEY_FEATURES, m_features );

		return json;
	}
}
