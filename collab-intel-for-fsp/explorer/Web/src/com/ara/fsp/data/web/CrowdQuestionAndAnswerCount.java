//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.data.web;

import org.json.JSONObject;

public class CrowdQuestionAndAnswerCount implements FSPWebObject
{
	public static final String JSONKEY_ANSWERS = "answers";

	private final CrowdQuestion m_question;
	private final int m_answerCount;

	public CrowdQuestionAndAnswerCount( final CrowdQuestion question, final int answerCount )
	{
		m_question = question;
		m_answerCount = answerCount;

		return;
	}

	@Override
	public JSONObject toJSON()
	{
		JSONObject json = m_question.toJSON();

		json.put( JSONKEY_ANSWERS, m_answerCount );

		return json;
	}
}
