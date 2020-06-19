//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

// TODO: This class may not be necessary since the EntityTypeId has everything now

public class EntityType implements FspEntityType {

	protected FspEntityTypeId id;
	protected String label;
	protected String desc;
		
	@Override
	public FspEntityTypeId getId() {
		return id;
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
	public boolean equals(FspEntityType other) {
		if(other instanceof EntityType){
			if (this.id==((EntityType) other).id) return true;
		}
		
		return false;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}


}
