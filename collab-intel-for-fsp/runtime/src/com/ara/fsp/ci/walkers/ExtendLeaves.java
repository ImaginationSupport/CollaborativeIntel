//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.walkers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.ara.fsp.api.FspCondition;
import com.ara.fsp.api.FspConditionOption;
import com.ara.fsp.api.FspConditionType;
import com.ara.fsp.api.FspEdge;
import com.ara.fsp.api.FspException;
import com.ara.fsp.api.FspMission;
import com.ara.fsp.api.FspMissionId;
import com.ara.fsp.api.FspRunTime;
import com.ara.fsp.api.FspState;
import com.ara.fsp.api.FspStateId;
import com.ara.fsp.api.FspStateSpaceForwardWalker;
import com.ara.fsp.ci.QuestionMgr;
import com.ara.fsp.runtime.mysql.ConditionOption;
import com.ara.fsp.runtime.mysql.StateId;
import com.ara.fsp.runtime.mysql.StateMgr;

public class ExtendLeaves implements FspStateSpaceForwardWalker{
	public Random rand=new Random();
	private FspRunTime rt=null;
	private QuestionMgr qm=null;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private SimpleDateFormat dfyear= new SimpleDateFormat("yyyy");
	private SimpleDateFormat dfmonth= new SimpleDateFormat("mm");
	private SimpleDateFormat dfday= new SimpleDateFormat("dd");
	private SimpleDateFormat dftime= new SimpleDateFormat("hh:mm:ss");
	private SimpleDateFormat dfrest= new SimpleDateFormat("-MM-dd hh:mm:ss");
	private SimpleDateFormat dfshort= new SimpleDateFormat("MMM yyyy");
	private Date horizon=null;

	public ExtendLeaves(FspRunTime rt, QuestionMgr qm, Date horizon) {
		this.rt=rt;
		this.qm=qm;
		this.horizon=horizon;
	}

	@Override
	public boolean onState(FspState state) {
		try {
			if(rt.getStateMgr().getNextEdges(state.getId()).isEmpty()){
				if(state.getEnd().before(horizon))
					extendHere(state);
				return false;
			}
		} catch (FspException e) {
			e.printStackTrace();
		}
		return true;
	}

	@Override
	public boolean onConditioningEvent(FspConditionOption option) {
		return true;
	}
	
	private static boolean cond=false;
	private void extendHere(FspState state){
		try {
			// Stupid stupid Date objects...
			Date nextStart=df.parse(""+((Integer.parseInt(dfyear.format(state.getStart()))+1)+dfrest.format(state.getStart())));
			Date nextEnd=df.parse(""+((Integer.parseInt(dfyear.format(state.getEnd()))+1)+dfrest.format(state.getEnd())));			
			if((cond==false) || ((cond==true) && (rand.nextInt(100)>50))){
				FspEdge edge=rt.getStateMgr().addState(state,nextStart, nextEnd, "timepasses", "Like the sands through an hour glass, so passes the days of our lives.");
				((StateMgr)rt.getStateMgr()).setStateLabel(edge.getNext(), dfshort.format(nextStart), "Generated state for 1 year after previous state."); 
				System.out.println("Added an edge without conditioning after state "+((StateId)state.getId()).value +" ("+state.getStart()+" - "+state.getEnd()+")");
				cond=true; // start random after first step
			} else { // add a conditioning event here
				FspEdge edgeA=rt.getStateMgr().addState(state,nextStart, nextEnd, "timepassesA", "hmm A");
				FspEdge edgeB=rt.getStateMgr().addState(state,nextStart, nextEnd, "timepassesB", "hmm B");
				
				String event="";
				String op0="";
				String op1="";
				
				switch(rand.nextInt(3)){
					case(0):{
						event="OPEC changes the production limits";
						op0="OPEC maintains production limits";
						op1="OPEC lowers production limits";
						break;
					}
					case(1):{
						event="federal laws subsidize US based natural gas companies";
						op0="federal laws subsidize US based natural gas companies";
						op1="federal laws force US based natural gas companies to compete unaided with forgein energy";
						break;
					}
					case(2):{
						event="An oil spill that taints public option";
						op0="No oil spill taints public option";
						op1="A large poorly handled oil spill taints public opinion";
						break;
					}
					case(3):{
						event="OPEC changes the production limits";
						op0="OPEC maintains production limits";
						op1="OPEC raises production limits";
						break;
					}
					default:{
						event="flying cars are finally released";
						op0="flying cards are released and they do not use gasoline";
						op1="flying cars are released and they use gasoline";
						break;
					}
				}

				FspCondition c=rt.getStateMgr().addCondition(FspConditionType.EVENT, event, "this is a general description of "+event);
				FspConditionOption c0=new ConditionOption(c, 0);
				c0.setLabel(op0);
				FspConditionOption c1=new ConditionOption(c, 1);
				c1.setLabel(op1);	
				rt.getStateMgr().addConditionOption(c0, edgeA.getId());
				rt.getStateMgr().addConditionOption(c1, edgeB.getId());
				((StateMgr)rt.getStateMgr()).setStateLabel(edgeA.getNext(), "State if "+op0, "Generated state for 1 year after previous state conditioned on "+event+"."); 
				((StateMgr)rt.getStateMgr()).setStateLabel(edgeB.getNext(), "State if "+op1, "Generated state for 1 year after previous state conditioned on "+event+"."); 

				System.out.println("Added Conditioning event after state "+((StateId)state.getId()).value +" ("+state.getStart()+" - "+state.getEnd()+"): for '"+event+"'");
			}
		} catch (FspException e) {
			e.printStackTrace();
		} catch (ParseException e){
			e.printStackTrace();
		}
		
	}
	



}
