//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.walkers;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Stack;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.QuestionMgr;

public class ContextBuilder implements FspStateSpaceBackwardWalker {

	private Stack<FspConditionOption> conditioningEvents=new Stack<FspConditionOption>();
	private FspFeature prevFeature=null;
	private FspState prevFeatureState=null;
	private FspEntityId entity=null;
	private FspFeatureMapId fmap=null;
	private FspStateId target=null;
	private static SimpleDateFormat df = new SimpleDateFormat("MMM yyyy");

	
	public ContextBuilder(FspStateId target, FspEntityId entity, FspFeatureMapId fmap) {
		this.entity=entity;
		this.fmap=fmap;
		this.target=target;
	}

	@Override
	public boolean onState(FspState state) {
		if((prevFeature==null)&&(!state.getId().equals(target))){
			try {
				prevFeature=QuestionMgr.getInstance().getRunTime().getStateMgr()
						.getFeature(state.getId(), entity, fmap);
				prevFeatureState=state;
			} catch (FspException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onConditioningEvent(FspConditionOption option) {
		conditioningEvents.push(option);
		return true;
	}
	
	public List<FspConditionOption> getConditioningEvents(){
		return conditioningEvents;
	}
	
	public FspFeature getPreviousFeature(){
		return prevFeature;
	}
	
	public FspState getPreviousFeatureState(){
		return prevFeatureState;
	}
	
	public String htmlContext(){
		if(conditioningEvents.isEmpty() && prevFeature==null) return "";
		
		String html="Please assume the follow statements are true when answering this question:<UL class=\'contextlist\'>";
		while(!conditioningEvents.isEmpty()){
			FspConditionOption o=conditioningEvents.pop();
			html+="<LI class='contextitem'>"+o.getLabel()+"</LI>";
		}
		if(prevFeature!=null)
			html+="<LI class='contextitem'>"+fmap.getLabel()+" was "+prevFeature.toDisplayString()+" in "+df.format(prevFeatureState.getDate())+"</LI>";
		html+="</UL><BR>";
		return html;
	}

}
