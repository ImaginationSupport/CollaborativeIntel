//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;


public interface FspFeatureType {
	public FspFeatureTypeId getId();
	public String getLabel();
	public void setLabel(String label);
	public String getDesc();
	public void setDesc(String desc);
	public boolean equals(FspFeatureType other);
	public boolean save();
	
}
