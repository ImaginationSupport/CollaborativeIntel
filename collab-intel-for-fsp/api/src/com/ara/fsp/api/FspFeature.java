//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.List;

public interface FspFeature {
	public FspFeatureId getId();
	public void setId(FspFeatureId id);
	
	public String getLabel();
	public String getDesc();
	
	public String toDisplayString();
	public String toString();
	public void fromString (String data);
	
	public double getConfidence();
	public void setConfidence(double conf);
	
	public String getLabelTemplate();
	public String getQuestionTemplate();
	public String getStatementTemplate();
	
	public FspFeature random();
	public FspFeature aggregate(List<FspFeature> values);
	public double getNumeric();
}
