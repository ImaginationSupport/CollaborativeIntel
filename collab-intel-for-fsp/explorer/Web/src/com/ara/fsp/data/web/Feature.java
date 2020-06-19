//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class Feature implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_ENTITYID = "entityid";
	public static final String JSONKEY_FEATUREMAPID = "featuremapid";
	public static final String JSONKEY_STATEID = "stateid";
	public static final String JSONKEY_VALUE = "value";
	public static final String JSONKEY_CONFIDENCE = "confidence";

	private final int m_id;
	private final int m_entityId;
	private final int m_featureMapId;
	private final int m_stateId;
	private final String m_value;
	private final double m_confidence;

	public int getId()
	{
		return m_id;
	}

	public int getEntityId()
	{
		return m_entityId;
	}

	public int getFeatureMapId()
	{
		return m_featureMapId;
	}

	public int getStateId()
	{
		return m_stateId;
	}

	public String getValue()
	{
		return m_value;
	}

	public double getConfidence()
	{
		return m_confidence;
	}

	public Feature( final int id, final int entityId, final int featureMapId, final int stateId, final String value, final double confidence )
		throws ArgumentException
	{
		m_id = id;
		m_entityId = entityId;
		m_featureMapId = featureMapId;
		m_stateId = stateId;
		m_value = value;
		m_confidence = confidence;

		return;
	}

	public Feature( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_entityId = sourceJSON.getInt( JSONKEY_ENTITYID );
		m_featureMapId = sourceJSON.getInt( JSONKEY_FEATUREMAPID );
		m_stateId = sourceJSON.getInt( JSONKEY_STATEID );
		m_value = sourceJSON.getString( JSONKEY_VALUE );
		m_confidence = sourceJSON.getDouble( JSONKEY_CONFIDENCE );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_ENTITYID, m_entityId );
		json.put( JSONKEY_FEATUREMAPID, m_featureMapId );
		json.put( JSONKEY_STATEID, m_stateId );
		json.put( JSONKEY_VALUE, m_value );
		json.put( JSONKEY_CONFIDENCE, m_confidence );

		return json;
	}
}