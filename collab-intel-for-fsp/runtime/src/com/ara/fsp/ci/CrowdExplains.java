//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

public class CrowdExplains {
	private String eventLabel;
	private String option1;
	private String option2;
	int votes;
	
	public CrowdExplains(String eventLabel, String option1, String option2, int votes) {
		this.eventLabel=eventLabel;
		this.option1=option1;
		this.option2=option2;
		this.votes=votes;
	}

	public String getEventLabel() {
		return eventLabel;
	}

	public String getOption1() {
		return option1;
	}

	public String getOption2() {
		return option2;
	}

	public int getVotes() {
		return votes;
	}

}
