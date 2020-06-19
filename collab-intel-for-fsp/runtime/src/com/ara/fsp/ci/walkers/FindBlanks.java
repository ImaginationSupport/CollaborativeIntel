//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.walkers;

import com.ara.fsp.api.FspConditionOption;
import com.ara.fsp.api.FspRunTime;
import com.ara.fsp.api.FspState;
import com.ara.fsp.api.FspStateSpaceForwardWalker;

public class FindBlanks implements FspStateSpaceForwardWalker{

	private FspRunTime rt=null;
	
	
	public FindBlanks(FspRunTime rt) {
		this.rt=rt;
	}

	@Override
	public boolean onState(FspState state){
		try{
			rt.addState(state.getId());
			
			
			
			
			return true;
		}catch (Exception e){
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean onConditioningEvent(FspConditionOption option) {
		return true;
	}

}
