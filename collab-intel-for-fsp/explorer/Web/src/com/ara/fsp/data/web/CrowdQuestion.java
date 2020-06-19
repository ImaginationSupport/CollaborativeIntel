//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

public final class CrowdQuestion implements FSPWebObject, Identifiable
{
	public enum QuestionType
	{
		Unknown( 0 ), Feature( 1 );

		private static final String STRING_VERSION_FEATURE = "FEATURE";

		private final int m_value;

		public int getValue()
		{
			return m_value;
		}

		private QuestionType( final int value )
		{
			m_value = value;
			return;
		};

		public static QuestionType fromInt( final int value )
		{
			switch( value )
			{
			case 1:
				return Feature;

			default:
				return Unknown;
			}
		}

		public static QuestionType fromString( final String value )
		{
			switch( value )
			{
			case STRING_VERSION_FEATURE:
				return Feature;

			default:
				return Unknown;
			}
		}
	}

	public static final String JSONKEY_ID = "id";
	public static final String JSONKEY_QUESTIONTYPE = "questiontype";
	public static final String JSONKEY_LABEL = "label";
	public static final String JSONKEY_CONTEXT = "context";
	public static final String JSONKEY_QUESTION = "question";
	public static final String JSONKEY_ACTIVE = "active";

	private final int m_id;
	private final QuestionType m_questionType;
	private final String m_label;
	private final String m_context;
	private final String m_question;
	private final boolean m_active;

	public int getId()
	{
		return m_id;
	}

	public QuestionType getQuestionType()
	{
		return m_questionType;
	}

	public String getLabel()
	{
		return m_label;
	}

	public String getContext()
	{
		return m_context;
	}

	public String getQuestion()
	{
		return m_question;
	}

	public boolean isActive()
	{
		return m_active;
	}

	public CrowdQuestion( final int id, final QuestionType questionType, final String label, final String context, final String question, final boolean active )
	{
		m_id = id;
		m_questionType = questionType;
		m_label = label;
		m_context = context;
		m_question = question;
		m_active = active;

		return;
	}

	public CrowdQuestion( final JSONObject sourceJSON )
	{
		m_id = sourceJSON.getInt( JSONKEY_ID );
		m_questionType = QuestionType.fromInt( sourceJSON.getInt( JSONKEY_QUESTIONTYPE ) );
		m_label = sourceJSON.getString( JSONKEY_LABEL );
		m_context = sourceJSON.getString( JSONKEY_CONTEXT );
		m_question = sourceJSON.getString( JSONKEY_QUESTION );
		m_active = sourceJSON.getBoolean( JSONKEY_ACTIVE );

		return;
	}

	@Override
	public JSONObject toJSON()
	{
		JSONObject json = new JSONObject();

		json.put( JSONKEY_ID, m_id );
		json.put( JSONKEY_QUESTIONTYPE, m_questionType.getValue() );
		json.put( JSONKEY_LABEL, m_label );
		json.put( JSONKEY_CONTEXT, m_context );
		json.put( JSONKEY_QUESTION, m_question );
		json.put( JSONKEY_ACTIVE, m_active );

		return json;
	}
}
