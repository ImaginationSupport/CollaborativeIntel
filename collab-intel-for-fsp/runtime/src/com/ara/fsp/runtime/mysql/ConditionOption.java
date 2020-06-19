//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

public class ConditionOption implements FspConditionOption {
	private FspCondition condition=null;
	private int id;
	private String label="Not Set";
	private String desc="Not Set";
	private double p=-1.0;

	public ConditionOption(FspCondition condition,int id) {
		this.condition=condition;
		this.id=id;
	}
	
	public int getId(){
		return id;
	}

	@Override
	public FspCondition getCondition() {
		return condition;
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
	public double getP() {
		return p;
	}
	
	@Override
	public void setP(double p) {
		this.p=p;
	}

}
