//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

import java.util.Date;
import java.util.List;
import java.util.Set;

public interface FspState {
	public FspStateId getId();
	public Date getDate();
	public void setDate(Date date);
	public Date	getStart();
	public void setStart(Date date);
	public Date getEnd();
	public void setEnd(Date date);
	public double getP();
	public void setP(double p);
	
	public List<FspEntity> getEntities();
	public Set<FspEntityId> getEntityIds();
	public FspEntity getEntity(FspEntityId id) throws FspException;
	public boolean save();
}
