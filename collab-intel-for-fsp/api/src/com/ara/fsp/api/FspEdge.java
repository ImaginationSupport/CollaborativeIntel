//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;


public interface FspEdge {
	public FspEdgeId getId();
	public FspCondition getCondition();
	public FspConditionOption getConditionOption();
	public String getLabel();
	public void setLabel(String label);
	public String getDesc();
	public void setDesc(String desc);
	public FspStateId getPrevious();
	public FspStateId getNext();
	public double getP();
	public void setP(double p);
	public boolean save();
}