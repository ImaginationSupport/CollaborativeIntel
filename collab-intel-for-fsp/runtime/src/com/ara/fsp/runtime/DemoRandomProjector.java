//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime;

import java.util.List;
import com.ara.fsp.api.*;
import com.ara.fsp.ci.Question;
import com.ara.fsp.ci.QuestionMgr;
import com.ara.fsp.runtime.mysql.*;


public class DemoRandomProjector {

	public static void main(String[] args) {

		String jdbc="jdbc:mysql://localhost/fsp?user=root&password=dogstar";
		if (args.length==1){
			jdbc=args[0];
		}
		
		// Setup database connection for this framework
		DataLayer dl=DataLayer.getInstance();
		dl.setJdbc(jdbc);
		FspRunTime rt=new RunTime();

		try {
			FspFeatureMgr fm=new FeatureMgr(dl,rt);
			FspStateMgr sm=new StateMgr(dl,rt,fm);
			QuestionMgr qm=new QuestionMgr(dl, rt);
			fm.init();
			sm.init();
			sm.init();
			
			System.out.println("Random Projector - Fills in randomly generated values for all crowdsource targetted features");
			try{
				List<Question> activeQuestions=qm.getQuestions();
				for (Question q: activeQuestions){
					List<FspFeature> featuresTargetted=qm.getTargetFeatures(q);
					for (FspFeature f: featuresTargetted){
						FspFeature r=f.random();
						String rs=r.toString();
						f.fromString(rs);
						f.setConfidence(0.1);
						rt.getStateMgr().updateFeature(f.getId(), f);
						q.setActive(false);
						qm.updateQuestion(q);
						System.out.println("\tFilled in question ["+q.getId()+"] about '"+q.getLabel()+"' with "+f.toString()+".");
					}
				}
			} catch (Exception e){
				e.printStackTrace();
			}
		
		} catch (FspException e) {
			e.printStackTrace();
		}
	}
		
}