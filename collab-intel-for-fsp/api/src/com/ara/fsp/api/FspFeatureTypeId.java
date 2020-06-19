//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

public interface FspFeatureTypeId {
	public String getLabel();
	public boolean equals(FspFeatureTypeId other);
	public int hashCode();
}
