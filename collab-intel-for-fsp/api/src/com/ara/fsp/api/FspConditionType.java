//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.api;

public enum FspConditionType {
	EVENT ("EVENT"),
	CHOICE ("CHOICE"),
	STOCHASTIC ("STOCHASTIC"),
	INTREPRETATION ("INTREPRETATION"),
	DIRECT ("DIRECT");
	
	private final String name;
	
	private FspConditionType(String name){
		this.name=name;
	}
	
	public String toString(){
		return name;
	}
}
