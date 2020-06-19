//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

public enum QuestionType {
	FEATURE ("FEATURE"),
	CONDITION ("CONDITION"),
	PROBABILITY ("PROBABILITY"),
	VERIFICATION ("VERIFICATION"),
	DISCOVERY ("DISCOVERY");
	
	private final String name;
	
	private QuestionType(String name){
		this.name=name;
	}
	
	public String toString(){
		return name;
	}
	
}
