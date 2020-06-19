//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.List;

public interface FspEntity {
	public FspEntityId getId();
	public FspEntityTypeId getTypeId();
	public String getLabel();
	public void setLabel(String label);
	public String getDesc();
	public void setDesc(String desc);
	public List<FspFeatureMap> getFeatureMaps();
	public boolean save();
	
}
