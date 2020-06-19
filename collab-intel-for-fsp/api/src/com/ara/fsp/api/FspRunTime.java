//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

public interface FspRunTime {
	public void init();
	public void setStateMgr(FspStateMgr stateMgr);
	public FspStateMgr getStateMgr();
	public void setFeatureMgr(FspFeatureMgr featureMgr);
	public FspFeatureMgr getFeatureMgr();
	public void addState(FspStateId stateId);
	public void run();
}
