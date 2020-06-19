//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.questions;

import com.ara.fsp.ci.Question;
import com.ara.fsp.ci.QuestionType;

public class ConditionQuestion extends Question {
	private int featureQuestionId=0;
	private String conditionQuestion=null;
	private String option1=null;
	private String option2=null;
	private String option1Value=null;
	private String option2Value=null;
	private double option1Conf=0.0;
	private double option2Conf=0.0;
	private int option1EdgeId=-1;
	private int option2EdgeId=-1;


	public ConditionQuestion(String label) {
		super(label, QuestionType.CONDITION);
	}

	public ConditionQuestion(int id, String label, boolean active) {
		super(id, label, QuestionType.CONDITION, active);
	}

	public String getConditionQuestion() {
		return conditionQuestion;
	}

	public void setConditionQuestion(String conditionQuestion) {
		this.conditionQuestion = conditionQuestion;
	}

	public String getOption1() {
		return option1;
	}

	public void setOption1(String option1) {
		this.option1 = option1;
	}

	public String getOption2() {
		return option2;
	}

	public void setOption2(String option2) {
		this.option2 = option2;
	}

	@Override
	public String getQuestionHTML() {
		return null;
	}

	public String getOption1Value() {
		return option1Value;
	}

	public void setOption1Value(String option1Value) {
		this.option1Value = option1Value;
	}

	public String getOption2Value() {
		return option2Value;
	}

	public void setOption2Value(String option2Value) {
		this.option2Value = option2Value;
	}

	public double getOption1Conf() {
		return option1Conf;
	}

	public void setOption1Conf(double option1Conf) {
		this.option1Conf = option1Conf;
	}

	public double getOption2Conf() {
		return option2Conf;
	}

	public void setOption2Conf(double option2Conf) {
		this.option2Conf = option2Conf;
	}

	public int getOption1EdgeId() {
		return option1EdgeId;
	}

	public void setOption1EdgeId(int option1EdgeId) {
		this.option1EdgeId = option1EdgeId;
	}

	public int getOption2EdgeId() {
		return option2EdgeId;
	}

	public void setOption2EdgeId(int option2EdgeId) {
		this.option2EdgeId = option2EdgeId;
	}

	public int getFeatureQuestionId() {
		return featureQuestionId;
	}

	public void setFeatureQuestionId(int featureQuestionId) {
		this.featureQuestionId = featureQuestionId;
	}

}
