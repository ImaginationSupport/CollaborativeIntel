//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

public class MissionId implements FspMissionId {
	protected int value=-1;
	protected String label="UNKNOWN";

	@Override
	public String getLabel() {
		return label+" ["+value+"]";
	}
	
	@Override
	public boolean equals(FspMissionId other) {
		if (other instanceof MissionId)
			if(((MissionId)other).value==this.value) return true;
		return false;
	}
	
	protected MissionId(int value, String label){
		this.value=value;
		this.label=label;
	}

	@Override
	public int hashCode() {
		return value;
	}

	
	
}
