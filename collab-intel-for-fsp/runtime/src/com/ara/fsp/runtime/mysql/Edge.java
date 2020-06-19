//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

public class Edge implements FspEdge {

	protected FspEdgeId id;
	private String label;
	private String desc;
	private double p = 0.0;
	private ConditionOption conditionOption=null;
	
	protected FspStateId next=null;
	protected FspStateId prev=null;
	
	protected Edge(FspStateId prev, FspStateId next){
		this.prev=prev;
		this.next=next;
	}

	@Override
	public FspEdgeId getId() {
		return id;
	}
	
	protected void setId(FspEdgeId id) {
		this.id=id;
	}

	@Override
	public FspCondition getCondition() {
		if (conditionOption==null) return null;
		else return conditionOption.getCondition();
	}

	@Override
	public FspConditionOption getConditionOption() {
		return conditionOption;
	}

	@Override
	public String getLabel() {
		return label;
	}

	@Override
	public void setLabel(String label) {
		this.label=label;
	}

	@Override
	public String getDesc() {
		return desc;
	}

	@Override
	public void setDesc(String desc) {
		this.desc=desc;
	}
	
	@Override
	public FspStateId getPrevious() {
		return prev;
	}

	@Override
	public FspStateId getNext() {
		return next;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public double getP() {
		return p;
	}

	@Override
	public void setP(double p) {
		this.p=p;
	}



}