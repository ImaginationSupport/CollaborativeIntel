//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.walkers;

import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Stack;

import com.ara.fsp.api.*;

public class FeatureTimeSeries implements FspStateSpaceBackwardWalker {

	public class FeatureInstance implements Comparable<FeatureInstance>{
		public Date time;
		public FspFeature feature;
		public FeatureInstance(Date time, FspFeature feature){
			this.time=time;
			this.feature=feature;
		}
		@Override
		public int compareTo(FeatureInstance other){
			return time.compareTo(other.time);
		}
	}
	private Stack<FeatureInstance> values=new Stack<FeatureInstance>();
	
	private FspEntity entity=null;
	private FspFeatureMap fmap=null;
	private FspState target=null;
	private FspRunTime rt= com.ara.fsp.runtime.RunTime.getInstance();
	
	public FeatureTimeSeries(FspState target, FspEntity entity, FspFeatureMap fmap) {
		this.entity=entity;
		this.fmap=fmap;
		this.target=target;
	}

	@Override
	public boolean onState(FspState state) {
		if(!state.equals(target)){
			try {
				FspStateMgr sm=rt.getStateMgr();
				FspFeature p=sm.getFeature(state.getId(),entity.getId(),fmap.getId());	
				values.push(new FeatureInstance(state.getDate(),p));
			} catch (FspException e1) {
				e1.printStackTrace();
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean onConditioningEvent(FspConditionOption option) {
		return true;
	}
	
	public List<FeatureInstance> getTimeSeriesFeatures(){
		Collections.sort(values); // shouldn't be needed but just in case
		return values;
	}
}
