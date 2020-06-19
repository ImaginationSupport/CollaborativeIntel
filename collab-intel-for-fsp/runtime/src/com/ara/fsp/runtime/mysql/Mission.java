//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.util.Date;

import com.ara.fsp.api.FspException;
import com.ara.fsp.api.FspMission;
import com.ara.fsp.api.FspMissionId;
import com.ara.fsp.api.FspStateId;

public class Mission implements FspMission{
	private FspMissionId id;
	private String label;
	private String desc;
	private Date horizon;
	private FspStateId root;

	public Mission(FspMissionId id) {
		this.id=id;
		this.label=id.getLabel();
	}

	@Override
	public FspMissionId getId() throws FspException {
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
	public Date getHorizon() {
		return horizon;
	}

	@Override
	public void setHorizon(Date date) {
		this.horizon=date;
	}

	@Override
	public FspStateId getRoot() throws FspException {
		return root;
	}

	@Override
	public void setRoot(FspStateId id) throws FspException {
		this.root=id;
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}

}
