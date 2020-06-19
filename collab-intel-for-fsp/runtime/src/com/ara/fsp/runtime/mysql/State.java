//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.ara.fsp.api.FspEntity;
import com.ara.fsp.api.FspEntityId;
import com.ara.fsp.api.FspException;
import com.ara.fsp.api.FspState;
import com.ara.fsp.api.FspStateId;

public class State implements FspState {
	private FspStateId id;
	private Date start=null;
	private Date end=null;
	private double p=-1.0;
	private HashMap<FspEntityId, FspEntity> entities=new HashMap <FspEntityId, FspEntity>();
	private static final java.text.SimpleDateFormat df = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	public State(FspStateId id){
		this.id=id;
	}
	
	@Override
	public FspStateId getId() {
		return id;
	}

	@Override
	public Date getDate() {
		return new Date((start.getTime()+end.getTime())/2);
	}
	
	public String getDateString(){
		return df.format(getDate());
	}
	
	@Override
	public void setDate(Date date) {
		start=date;
		end=date;
	}

	@Override
	public Date getStart() {
		return start;
	}

	@Override
	public void setStart(Date date) {
		this.start=date;
	}

	@Override
	public Date getEnd() {
		return end;
	}

	@Override
	public void setEnd(Date date) {
		this.end=date;
	}

	@Override
	public double getP() {
		return p;
	}

	@Override
	public void setP(double p) {
		this.p=p;
	}

	@Override
	public List<FspEntity> getEntities() {
		return new ArrayList<FspEntity>(entities.values());
	}

	@Override
	public Set<FspEntityId> getEntityIds() {
		return entities.keySet();
	}

	@Override
	public FspEntity getEntity(FspEntityId e) throws FspException {
		if(!entities.containsKey(e))
			throw new FspException("Invalid FspEntityId "+e.getLabel()+" for State "+id);
		return entities.get(e);
	}

	@Override
	public boolean save() {
		// TODO Auto-generated method stub
		return false;
	}


}
