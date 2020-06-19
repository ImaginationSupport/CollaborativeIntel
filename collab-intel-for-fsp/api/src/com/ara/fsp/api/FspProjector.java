//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.List;


public interface FspProjector {
	public String getLabel();
	
	public void init(FspRunTime runtime);
	public void project(FspState state, FspEntityId eId, FspFeatureMapId fId) throws FspException;
	
	public List<FspProjectorParameter> getProjectorParameters();
	public void setProjectorParameter(FspProjectorParameter parameter, FspFeatureMap feature) throws FspException;
	public FspFeatureMap getProjectorParameter(FspProjectorParameter paramater);
}
