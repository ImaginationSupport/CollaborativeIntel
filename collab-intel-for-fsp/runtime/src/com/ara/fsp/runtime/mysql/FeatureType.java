//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import com.ara.fsp.api.*;

public class FeatureType implements FspFeatureType {

	protected FspFeatureTypeId id;
	protected String label;
	protected String desc;
	protected String jarName;
	protected String className;
	
	protected FeatureType(FspFeatureTypeId id, String label, String desc, String jarName, String className){
		this.id=id;
		this.jarName=jarName;
		this.className=className;
		this.label=label;
		this.desc=desc;
	}
	
	@Override
	public FspFeatureTypeId getId() {
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
	public boolean equals(FspFeatureType other) {
		if(other instanceof FeatureType){
			if (this.id==((FeatureType) other).id) return true;
			if (this.jarName.equals(((FeatureType) other).jarName) && this.className.equals(((FeatureType) other).className)) return true;
		}
		
		return false;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

}
