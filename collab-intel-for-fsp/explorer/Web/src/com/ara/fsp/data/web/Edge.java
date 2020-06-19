//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

import com.ara.fsp.exceptions.ArgumentException;

public final class Edge implements FSPWebObject, Identifiable
{
	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_DESCRIPTION = "description";
	public static final String JSONKEY_P = "p";
	public static final String JSONKEY_PREV = "prev";
	public static final String JSONKEY_NEXT = "next";

	private final int m_id;
	private final String m_label;
	private final String m_description;
	private final double m_p;
	private final int m_prev;
	private final int m_next;

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

	public double getP()
	{
		return m_p;
	}

	public int getPrev()
	{
		return m_prev;
	}

	public int getNext()
	{
		return m_next;
	}

	public Edge( final int id, final String label, final String description, final double p, final int prev, final int next ) throws ArgumentException
	{
		m_id = id;
		m_label = label;
		m_description = description;
		m_p = p;
		m_prev = prev;
		m_next = next;

		return;
	}

	public Edge( final JSONObject sourceJSON ) throws ArgumentException
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_description = sourceJSON.getString( JSONKEY_DESCRIPTION );
		m_p = sourceJSON.getDouble( JSONKEY_P );
		m_prev = sourceJSON.getInt( JSONKEY_PREV );
		m_next = sourceJSON.getInt( JSONKEY_NEXT );

		return;
	}

	@Override
	public JSONObject toJSON() throws ArgumentException
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_DESCRIPTION, m_description );
		json.put( JSONKEY_P, m_p );
		json.put( JSONKEY_PREV, m_prev );
		json.put( JSONKEY_NEXT, m_next );

		return json;
	}
}