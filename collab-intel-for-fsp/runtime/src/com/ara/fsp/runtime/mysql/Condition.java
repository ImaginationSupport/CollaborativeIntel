//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.util.ArrayList;
import java.util.List;

import com.ara.fsp.api.*;

public class Condition implements FspCondition {
	private FspConditionId id;
	private FspConditionType type;
	private String label;
	private String desc;
	private ArrayList<FspConditionOption> options=new ArrayList<FspConditionOption>();

	public Condition(FspConditionId id, FspConditionType type, String label, String desc) {
		this.id=id;
		this.type=type;
		this.label=label;
		this.desc=desc;
	}
	
	public Condition(FspConditionType type, String label, String desc) {
		this.type=type;
		this.label=label;
		this.desc=desc;
	}

	@Override
	public FspConditionId getId() {
		return id;
	}

	@Override
	public FspConditionType getType() {
		return type;
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
	public List<FspConditionOption> getOptions() {
		return options;
	}

	@Override
	public void addOption(FspConditionOption option) throws FspException {
		if(!hasOption(option)){
			options.add(option);
		} else {
			throw new FspException("Adding duplicate option "+option.getLabel()+" to condition "+getLabel());
		}
	}

	@Override
	public boolean hasOption(FspConditionOption option) {
		options.contains(option);
		return false;
	}


	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

}
