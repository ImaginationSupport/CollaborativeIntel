//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.util.*;

import com.ara.fsp.api.*;

public class Entity implements FspEntity{

	private FspEntityId id;
	private FspEntityTypeId typeId;
	private String label = "Unnamed Entity";
	private String desc = "None";
	private List<FspFeatureMap> featureMaps = new ArrayList<FspFeatureMap>();
	
	public Entity(FspEntityId id, FspEntityTypeId typeId, String label, String desc){
		this.id=id;
		this.typeId=typeId;
		this.label=label;
		this.desc=desc;
	}
	
	public FspEntityId getId() {
		return this.id;
	}

	public FspEntityTypeId getTypeId() {
		return this.typeId;
	}

	public String getLabel() {
		return this.label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
	
	protected void setFeatureMaps(List<FspFeatureMap> featureMaps){
		this.featureMaps=featureMaps;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public List<FspFeatureMap> getFeatureMaps() {
		return featureMaps;
	}

}