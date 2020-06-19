//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.List;

public interface FspCondition {
	public FspConditionId getId();
	public FspConditionType getType();
	public String getLabel();
	public void setLabel(String label);
	public String getDesc();
	public void setDesc(String desc);
	public List<FspConditionOption> getOptions();
	public void addOption(FspConditionOption option) throws FspException;
	public boolean hasOption(FspConditionOption option);
	public boolean save();
}
