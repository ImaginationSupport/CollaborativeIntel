//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

import java.util.ArrayList;

import com.ara.fsp.api.*;

public abstract class Question {
	private int id=-1;
	private String label="";
	private String context="";
	private String units="";
	private QuestionType type;
	private boolean active=true;
	
	public class ContextEvent{
		public FspConditionId condId;
		public FspConditionOption option;
		public ContextEvent(FspConditionId condId, FspConditionOption option){
			this.condId=condId;
			this.option=option;
		}
	}
	protected ArrayList<ContextFact> contextFacts=new ArrayList<ContextFact>();
	protected ArrayList<String> contextFactsText=new ArrayList<String>();
	
	public class ContextFact{
		public FspStateId stateId;
		public FspFeatureId featureId;
		public ContextFact(FspStateId stateId, FspFeatureId featureId){
			this.stateId=stateId;
			this.featureId=featureId;
		}
	}
	protected ArrayList<ContextEvent> contextEvents=new ArrayList<ContextEvent>();
	protected ArrayList<String> contextConditions=new ArrayList<String>();

	private String question="No question specified";

	public Question(String label, QuestionType type){
		this.label=label;
		this.type=type;
	}
	
	public Question(int id, String label, QuestionType type, boolean active){
		this.id=id;
		this.label=label;
		this.type=type;
		this.active=active;
	}
	
	public boolean isActive(){
		return active;
	}
	
	public QuestionType getType(){
		return type;
	}
	
	protected void setId(int id){
		this.id=id;
	}
	
	public int getId(){
		return id;
	}
	
	public String getLabel(){
		return label;
	}
	
	public void setLabel(String label){
		this.label=label;
	}
	
	
	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public String getUnits() {
		return units;
	}

	public void setUnits(String units) {
		this.units = units;
	}

	public void save(){
		
	}
			
	public abstract String getQuestionHTML();
		
	public String generateContextHTML(){
		String context="<div class='context'><B>For this question, please assume ";
		
		if(!contextConditions.isEmpty()){
			context+="the following events have occured:</B><BR><UL>\n";
			for (String line: contextConditions){
				context+="<LI class='contextfact'>"+line+"</LI>";
			}
			context+="</UL>";
		}
		if((!contextFacts.isEmpty()) && (!contextConditions.isEmpty())){
			context+="<BR><B>and that";
		}
		if(!contextFacts.isEmpty()){
			context+="the following information is correct:</B><BR><UL>\n";
			for (String line: contextConditions){
				context+="<LI class='contextfact'>"+line+"</LI>";
			}
			context+="</UL>";
		}
		context+="</div>";
		return context;
	}
	
	public void addContextConditioningEvent(FspConditionOption option) {
		ContextEvent cond=new ContextEvent(option.getCondition().getId(),option);
		contextEvents.add(cond);
		
	}
		
	public void addContextFact(FspStateId stateId, FspFeatureId featureId){
		ContextFact fact=new ContextFact(stateId,featureId);
		contextFacts.add(fact);
		
	}

	public String getContext() {
		return context;		
	}
	
	public void setContext(String context) {
		this.context=context;		
	}

	public void setActive(boolean active) {
		this.active=active;
	}
	
	//public abstract void setTargetFeature(FspStateId stateId, FspFeatureId featureId);
	
}
