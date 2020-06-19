//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

public class CrowdInput {
	private String value;
	private double confidence;
	
	public String getValue() {
		return value;
	}

	public double getConfidence() {
		return confidence;
	}

	public CrowdInput(String value, double confidence) {
		this.value=value;
		this.confidence=confidence;
	}
}
