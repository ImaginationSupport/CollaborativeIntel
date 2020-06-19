//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class FeatureSlot implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_ENTITYTYPEID = "entitytypeid";
	public static final String JSONKEY_FEATUREMAPID = "featuremapid";

	private final int m_id;
	private final int m_entityTypeId;
	private final int m_featureMapId;

	public int getId()
	{
		return m_id;
	}

	public int getEntityTypeId()
	{
		return m_entityTypeId;
	}

	public int getFeatureMapId()
	{
		return m_featureMapId;
	}

	public FeatureSlot( final int id, final int entityTypeId, final int featureMapId ) throws ArgumentException
	{
		m_id = id;
		m_entityTypeId = entityTypeId;
		m_featureMapId = featureMapId;

		return;
	}

	public FeatureSlot( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_entityTypeId = sourceJSON.getInt( JSONKEY_ENTITYTYPEID );
		m_featureMapId = sourceJSON.getInt( JSONKEY_FEATUREMAPID );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_ENTITYTYPEID, m_entityTypeId );
		json.put( JSONKEY_FEATUREMAPID, m_featureMapId );

		return json;
	}
}