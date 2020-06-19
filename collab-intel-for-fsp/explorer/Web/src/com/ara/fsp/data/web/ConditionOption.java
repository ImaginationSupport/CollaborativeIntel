//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class ConditionOption implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_CONDITIONID = "conditionid";
	public static final String JSONKEY_EDGEID = "edgeid";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_P = "p";

	private final int m_id;
	private final int m_conditionId;
	private final int m_edgeId;
	private final String m_label;
	private final double m_p;

	public int getId()
	{
		return m_id;
	}

	public int getConditionId()
	{
		return m_conditionId;
	}

	public int getEdgeId()
	{
		return m_edgeId;
	}

	public String getLabel()
	{
		return m_label;
	}

	public double getP()
	{
		return m_p;
	}

	public ConditionOption( final int id, final int conditionId, final int edgeId, final String label, final double p ) throws ArgumentException
	{
		m_id = id;
		m_conditionId = conditionId;
		m_edgeId = edgeId;
		m_label = label;
		m_p = p;

		return;
	}

	public ConditionOption( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_conditionId = sourceJSON.getInt( JSONKEY_CONDITIONID );
		m_edgeId = sourceJSON.getInt( JSONKEY_EDGEID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_p = sourceJSON.getDouble( JSONKEY_P );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_CONDITIONID, m_conditionId );
		json.put( JSONKEY_EDGEID, m_edgeId );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_P, m_p );

		return json;
	}
}