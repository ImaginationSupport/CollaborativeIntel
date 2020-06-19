//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.questions;

import com.ara.fsp.ci.Question;
import com.ara.fsp.ci.QuestionType;

public class FeatureQuestion extends Question {

	public FeatureQuestion(String label) {
		super(label, QuestionType.FEATURE);
	}

	public FeatureQuestion(int id, String label, boolean active) {
		super(id, label, QuestionType.FEATURE, active);
	}

	@Override
	public String getQuestionHTML() {
		// TODO Auto-generated method stub
		return null;
	}

}
