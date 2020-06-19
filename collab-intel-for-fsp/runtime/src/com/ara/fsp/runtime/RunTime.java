//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime;


import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.QuestionMgr;
import com.ara.fsp.runtime.mysql.*;

public class RunTime implements FspRunTime{
	private FspStateMgr stateMgr=null;
	private FspFeatureMgr featureMgr=null;
			
	private static FspRunTime self=null;
	
	private Queue<FspStateId> open=new LinkedList<FspStateId>();
	
	/*
	public static void main(String[] args) {
		RunTime runTime=new RunTime();
		runTime.init();
	}
	*/
	
	public RunTime() {
	}

	@Override
	public void init() {
		if(self==null){
			self=this;
		} else {
			System.err.println("Error: Multiple calls to Run Time Init!");
		}
		DataLayer dl=DataLayer.getInstance();
		dl.setJdbc("jdbc:mysql://localhost/fsp?user=root&password=dogstar");

		setFeatureMgr(new FeatureMgr(dl,this));
		setStateMgr(new StateMgr(dl,this,featureMgr));
	}

	@Override
	public FspStateMgr getStateMgr() {
		return stateMgr;
	}
	
	@Override
	public void setStateMgr(FspStateMgr stateMgr) {
		this.stateMgr=stateMgr;
	}

	@Override
	public FspFeatureMgr getFeatureMgr() {
		return featureMgr;
	}
	
	@Override
	public void setFeatureMgr(FspFeatureMgr featureMgr) {
		this.featureMgr=featureMgr;
	}
	
	@Override
	public void run() {
		while(!open.isEmpty()){
			FspStateId sId=open.poll();
			processState(sId);
		}
	}
	
	public void addState(FspStateId sId){
		open.add(sId);
	}
	
	public void processState(FspStateId sId){
		try{
			FspState s=stateMgr.getState(sId);
			List<FspEntityId> entities=featureMgr.getEntities();
			for(FspEntityId eId: entities){
				FspEntity e=featureMgr.getEntity(eId);
				FspEntityTypeId etId=e.getTypeId();
				List<FspFeatureMapId> slots=featureMgr.getFeatureSlots(etId);
				for(FspFeatureMapId fId: slots){
					if(!stateMgr.hasFeature(sId, eId, fId)){
						FspProjector p=stateMgr.getAssignedProjector(fId);
						p.init(this);
						p.project(s,eId,fId);
					}
				}
			}
		} catch (FspException e){
			System.err.println("Error Processing State"+sId+": "+e);
		}
	}

	public static FspRunTime getInstance() {
		return self;
	}

}
