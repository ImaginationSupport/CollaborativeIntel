//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class FeatureMap implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_FEATURETYPEID = "featuretypeid";
	public static final String JSONKEY_UNITS = "units";

	private final int m_id;
	private final String m_label;
	private final int m_featureTypeId;
	private final String m_units;

	public int getId()
	{
		return m_id;
	}

	public String getLabel()
	{
		return m_label;
	}

	public int getFeatureTypeId()
	{
		return m_featureTypeId;
	}

	public String getUnits()
	{
		return m_units;
	}

	public FeatureMap( final int id, final String label, final int featureTypeId, final String units ) throws ArgumentException
	{
		m_id = id;
		m_label = label;
		m_featureTypeId = featureTypeId;
		m_units = units;

		return;
	}

	public FeatureMap( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_featureTypeId = sourceJSON.getInt( JSONKEY_FEATURETYPEID );
		m_units = sourceJSON.getString( JSONKEY_UNITS );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_FEATURETYPEID, m_featureTypeId );
		json.put( JSONKEY_UNITS, m_units );

		return json;
	}
}