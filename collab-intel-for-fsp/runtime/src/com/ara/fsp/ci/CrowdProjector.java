//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.ModalityDetector.Modality;
import com.ara.fsp.ci.questions.ConditionQuestion;
import com.ara.fsp.ci.questions.FeatureQuestion;
import com.ara.fsp.ci.walkers.ContextBuilder;
import com.ara.fsp.runtime.mysql.ConditionOption;
import com.ara.fsp.runtime.mysql.EdgeId;
import com.ara.fsp.runtime.mysql.StateId;
import com.ara.fsp.runtime.mysql.StateMgr;

public class CrowdProjector implements FspProjector {
	private FspRunTime rt=null;
	private static SimpleDateFormat  df = new SimpleDateFormat("MMM yyyy");
	private static final String labelTemplate="explaining the #FEATUREMAP# for #ENTITY#";

	public CrowdProjector() {
	}

	@Override
	public String getLabel() {
		return "Crowdsource";
	}

	@Override
	public void init(FspRunTime rt) {
		this.rt=rt;
	}


	@Override
	public List<FspProjectorParameter> getProjectorParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setProjectorParameter(FspProjectorParameter parameter,
			FspFeatureMap feature) throws FspException {
		// TODO Auto-generated method stub

	}

	@Override
	public FspFeatureMap getProjectorParameter(FspProjectorParameter paramater) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void project(FspState state, FspEntityId eId, FspFeatureMapId fmapId) throws FspException {		
		FspFeatureId fid=null;
		FspFeature f=null;
		FspFeatureMap fmap=rt.getFeatureMgr().getFeatureMap(fmapId);;
		if(rt.getStateMgr().hasFeature(state.getId(), eId, fmapId)){
			f=rt.getStateMgr().getFeature(state.getId(), eId, fmapId);
			fid=f.getId();
		} else { // create feature
			f=rt.getFeatureMgr().loadFeature(fmap.getType().getId());
			f.setConfidence(-1.0);
			fid=rt.getStateMgr().addFeature(state.getId(), eId, fmapId, f);
			f.setId(fid);
		}
		
		QuestionMgr qm=QuestionMgr.getInstance();
		Question q=qm.getQuestion(fid);
		if(q==null) {
			q=buildFeatureQuestion(state,eId,fmap,f);
		} else {
			checkQuestion(q);
		}
	}

	private void checkQuestion(Question q) {
		// TODO Auto-generated method stub
		
	}

	private Question buildFeatureQuestion(FspState state, FspEntityId entityId, FspFeatureMap fmap, FspFeature feature ) throws FspException{
		QuestionMgr qm=QuestionMgr.getInstance();
		String label=feature.getLabelTemplate();
		if (entityId.getLabel().equalsIgnoreCase("Global")){
			label=label.replace("for #ENTITY#", "");
			label=label.replace("#ENTITY#", "All");
		} else {
			label=label.replace("#ENTITY#",entityId.getLabel());
		}
		label=label.replace("#DATE#",df.format(state.getDate()));
		label=label.replace("#UNITS#","(in "+fmap.getUnits()+")");
		label=label.replace("#FEATUREMAP#",fmap.getLabel());
		label=label.substring(0, 1).toUpperCase() + label.substring(1);
		Question q=new FeatureQuestion(label);
		q.setUnits(fmap.getUnits());
		q.setId(qm.addQuestion(q));
		qm.addTargetFeature(q, feature.getId());
		
		ContextBuilder cb=new ContextBuilder(state.getId(),entityId, fmap.getId());
		try {
			rt.getStateMgr().walkStateSpace(state, cb);
		} catch (FspException e) {
			System.err.println("CrowdProjector failed finding context for question.");
			e.printStackTrace();
		}
		for(FspConditionOption o: cb.getConditioningEvents()){
			if(o!=null) qm.addContextCondition(q,o);
		}
		if(cb.getPreviousFeature()!=null){
			qm.addContextFeature(q,cb.getPreviousFeature().getId());
		}
		String contextText=cb.htmlContext();
		if(!contextText.isEmpty())
			q.setContext(contextText+"<P>");
		
		String questionText=feature.getQuestionTemplate();
		if (entityId.getLabel().equalsIgnoreCase("Global")){
			questionText=questionText.replace("for #ENTITY#", "");
			questionText=questionText.replace("#ENTITY#", "All");
		} else {
			questionText=questionText.replace("#ENTITY#",entityId.getLabel());
		}
		questionText=questionText.replace("#DATE#",df.format(state.getDate()));
		questionText=questionText.replace("#UNITS#","(in "+fmap.getUnits()+")");
		questionText=questionText.replace("#FEATUREMAP#",fmap.getLabel());
		
		questionText =questionText.substring(0, 1).toUpperCase() + questionText.substring(1);
		System.out.println("\nGiving context: "+q.getContext());
		System.out.println("Adding question: "+questionText);
		q.setQuestion(questionText);
		qm.updateQuestion(q);
		return q;
	}
	
	private static Question buildConditionQuestion(FspRunTime rt, FeatureQuestion q, FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmapId, FspFeature feature, FspFeature f2, FspEdge edge1, FspEdge edge2) throws FspException{
		QuestionMgr qm=QuestionMgr.getInstance();
		FspState state=rt.getStateMgr().getState(stateId);
		FspFeatureMap fmap=rt.getFeatureMgr().getFeatureMap(fmapId);
		String label="explaining differences in "+feature.getLabelTemplate();
		if (entityId.getLabel().equalsIgnoreCase("Global")){
			label=label.replace("for #ENTITY#", "");
			label=label.replace("#ENTITY#", "All");
		} else {
			label=label.replace("#ENTITY#",entityId.getLabel());
		}
		label=label.replace("#DATE#",df.format(state.getDate()));
		label=label.replace("#UNITS#","(in "+fmap.getUnits()+")");
		label=label.replace("#FEATUREMAP#",fmap.getLabel());
		label=label.substring(0, 1).toUpperCase() + label.substring(1);
		ConditionQuestion cq=new ConditionQuestion(label);
		cq.setUnits(fmap.getUnits());
		cq.setConditionQuestion("What event might explain each of these distinct values for "+fmap.getUnits()+"?");
		cq.setFeatureQuestionId(q.getId());
		cq.setOption1(feature.toString()+" "+fmap.getUnits());
		cq.setOption2(f2.toString()+" "+fmap.getUnits());
		cq.setOption1Value(feature.toString());
		cq.setOption2Value(f2.toString());
		cq.setOption1EdgeId(((EdgeId)edge1.getId()).value);
		cq.setOption2EdgeId(((EdgeId)edge2.getId()).value);
		
		String questionText="predictions for #FEATUREMAP# for #ENTITY# on #DATE# varied significantly.\n Please indicate an event that would explain why each of the distinct values of #UNITS# might occur." ;
		if (entityId.getLabel().equalsIgnoreCase("Global")){
			questionText=questionText.replace("for #ENTITY#", "");
			questionText=questionText.replace("#ENTITY#", "All");
		} else {
			questionText=questionText.replace("#ENTITY#",entityId.getLabel());
		}
		questionText=questionText.replace("#DATE#",df.format(state.getDate()));
		questionText=questionText.replace("#UNITS#","(in "+fmap.getUnits()+")");
		questionText=questionText.replace("#FEATUREMAP#",fmap.getLabel());
		questionText =questionText.substring(0, 1).toUpperCase() + questionText.substring(1);
		cq.setContext(q.getContext());
		System.out.println("\nGiving context: "+cq.getContext());
		cq.setQuestion(questionText);
		System.out.println("Adding question: "+questionText);
		
		cq.setId(qm.addQuestion(cq));
		//qm.addConditioningQuestionText(cq);
		
		return q;
	}
	

	public static boolean checkCrowd(FspRunTime rt,QuestionMgr qm){
		try{
			System.out.println("Checking status of crowd-sourced questions...");
			List<Question> activeQuestions=qm.getQuestions();
			for (Question q: activeQuestions){
				switch(q.getType()){
					case FEATURE:{
						List<CrowdInput> crowdInput=qm.getCrowdInput((FeatureQuestion) q);
						if(crowdInput.size()<=20) continue;
						System.out.println("Conditioning Question["+q.getId()+"] "+q.getLabel()+" has sufficient ["+crowdInput.size()+"] crowd samples.");
						List<FspFeature> featuresTargetted=qm.getTargetFeatures(q);
						List<FspFeature> values=new ArrayList<FspFeature>();
						for (FspFeature f: featuresTargetted){
							for (CrowdInput ci: crowdInput){
								FspFeature n=f.random();
								n.fromString(ci.getValue());
								n.setConfidence(ci.getConfidence());
								values.add(n);
							}
							
							ModalityDetector md=new ModalityDetector(values);
							List<Modality> ms=md.getModes(.2);
							if(ms.size()==0){
								FspFeature a=f.aggregate(values);
								a.setId(f.getId());
								qm.updateFeature(a);
							} 
							if(ms.size()==1){
								f.fromString(ms.get(0).mean+"");
								qm.updateFeature(f);
							}
							if(ms.size()>1){
								FspStateId stateId1;
								FspEntityId eid=rt.getStateMgr().getEntityIdForFeature(f.getId());
								FspFeatureMapId fmapid=rt.getStateMgr().getFeatureMapIdForFeature(f.getId());
								stateId1 = rt.getStateMgr().getStateIdForFeature(f.getId());
								FspEdgeId edgeId1= rt.getStateMgr().getPrevEdge(stateId1);
								
								if (edgeId1==null) {
									System.out.println("WARNING: Crowd split on feature for the root node, there is nothing we can do about conditioning before this.");
									// report mean and bail out
									FspFeature a=f.aggregate(values);
									a.setId(f.getId());
									qm.updateFeature(a);
									q.setActive(false);
									qm.updateQuestion(q);
									break;
								}
								
								FspEdge edge1=rt.getStateMgr().getEdge(edgeId1);
								
								if(edge1.getCondition()!=null || qm.isOpenConditionQuestion( ((EdgeId)edgeId1).value)){
									// TODO: this prevents multiple conditioning events between the same nodes - need method for combining them eventually.
									System.out.println("WARNING: Ignoring modalities for potential secondary conditioning events - no logic for combining conditionings at this time.");
									// report mean and bail out
									FspFeature a=f.aggregate(values);
									a.setId(f.getId());
									qm.updateFeature(a);
									q.setActive(false);
									qm.updateQuestion(q);
									break;
								}
								
								
								f.fromString(ms.get(0).mean+"");
								double c=ms.get(0).width/ms.get(0).mean;
								if (c>.99) c=.99;
								f.setConfidence(1.0-c);
								rt.getStateMgr().updateFeature(f.getId(), f);
								FspFeature f2=null;
								FspEdge edge2=null;
								for(int i=1;i<2;i++){	//ignoring other modalities was ms.size()
									edge2= rt.getStateMgr().cloneState(stateId1);
									FspStateId stateId2=edge2.getNext();
									f2=rt.getStateMgr().getFeature(stateId2,eid,fmapid);
									f2.fromString(ms.get(i).mean+"");
									double c2=ms.get(i).width/ms.get(i).mean;
									if (c2>.99) c2=.99;
									f2.setConfidence(1.0-c2);
									rt.getStateMgr().updateFeature(f2.getId(), f2);
								} //TODO: only gets the first and last b/c assumes bimodal
								buildConditionQuestion(rt,(FeatureQuestion)q,stateId1,eid,fmapid,f,f2,edge1,edge2);
							}
							q.setActive(false);
							qm.updateQuestion(q);
						}
						break;
					}
					case CONDITION:{
						List<CrowdExplains> crowdExplains=qm.getCrowdExplains((ConditionQuestion) q);
						if(crowdExplains.size()==0) break;
						if(crowdExplains.get(0).getVotes()<3) continue;
						ConditionQuestion cq=qm.getConditioningQuestionText(q);
						FspCondition c=rt.getStateMgr().addCondition(FspConditionType.EVENT, crowdExplains.get(0).getEventLabel(), "Elicited from crowd.");
						
						FspEdgeId edgeId1=new EdgeId(cq.getOption1EdgeId(),"");
						FspEdge edge1=rt.getStateMgr().getEdge(edgeId1);
						((StateMgr)rt.getStateMgr()).setStateLabel(edge1.getNext(), crowdExplains.get(0).getOption1(),
								"Crowd elicited conditioning due to modality around "+cq.getOption1()+" in responses to '"+cq.getQuestion()+"'");
						FspEdgeId edgeId2=new EdgeId(cq.getOption2EdgeId(),"");
						FspEdge edge2=rt.getStateMgr().getEdge(edgeId2);
						((StateMgr)rt.getStateMgr()).setStateLabel(edge2.getNext(), crowdExplains.get(0).getOption2(),
								"Crowd elicited conditioning due to modality around "+cq.getOption2()+" in responses to '"+cq.getQuestion()+"'");

						FspConditionOption c0=new ConditionOption(c, 0);
						c0.setLabel(crowdExplains.get(0).getOption1());
						FspConditionOption c1=new ConditionOption(c, 1);
						c1.setLabel(crowdExplains.get(0).getOption2());;	
						rt.getStateMgr().addConditionOption(c0, new EdgeId(cq.getOption1EdgeId(),""));
						rt.getStateMgr().addConditionOption(c1, new EdgeId(cq.getOption2EdgeId(),""));

						System.out.println("Crowd nominated the explaination for id '"+cq.getId()+"'");
						
						q.setActive(false);
						qm.updateQuestion(q);
						break;
					}
					default:
						break;
				}
			}
			return activeQuestions.isEmpty();
		} catch (Exception e){
			e.printStackTrace();
		}
		return false;
	}


}
