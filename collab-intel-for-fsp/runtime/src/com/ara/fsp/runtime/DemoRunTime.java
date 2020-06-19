//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.CrowdExplains;
import com.ara.fsp.ci.CrowdInput;
import com.ara.fsp.ci.CrowdProjector;
import com.ara.fsp.ci.Question;
import com.ara.fsp.ci.QuestionMgr;
import com.ara.fsp.ci.QuestionType;
import com.ara.fsp.ci.questions.ConditionQuestion;
import com.ara.fsp.ci.questions.FeatureQuestion;
import com.ara.fsp.ci.walkers.ExtendLeaves;
import com.ara.fsp.ci.walkers.FindBlanks;
import com.ara.fsp.runtime.mysql.*;


public class DemoRunTime {

	public static void main(String[] args) {

		String jdbc="jdbc:mysql://localhost/fsp?user=root&password=dogstar";
		if (args.length==1){
			jdbc=args[0];
		} else {
			System.err.println("Usage: java DemoRunTime {jdbc connection string here}" );
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
			
			System.out.println("Step 1: Missioning the System");
			
			System.out.println("\tFeature Space (types and maps)");
			List<FspFeatureMapId> maps=fm.getFeatureMaps();
			
			if(maps.isEmpty()){
				loadFeatures(fm);
				maps=fm.getFeatureMaps();
			}
			
			for (FspFeatureMapId mapId: maps){
				FspFeatureMap map = fm.getFeatureMap(mapId);
				System.out.println("\t\tFound Feature of type "+map.getType().getId().getLabel()+" mapped to '"+map.getId().getLabel()+"'("+map.getUnits()+")");
				FspFeatureType x= fm.getFeatureType(map.getType().getId());
			}
		
	
			System.out.println("\tEntities (types and instances)");
			
			List<FspEntityId> entities=fm.getEntities();
			
			if(entities.isEmpty()){
				loadEntities(fm);
				entities=fm.getEntities();
			}
			
			for (FspEntityId eId: entities){
				FspEntity e=fm.getEntity(eId);
				System.out.println("\t\tFound entity of type "+e.getTypeId().getLabel()+" labeled as '"+e.getLabel()+"'");
				List<FspFeatureMapId> slotsList=((FeatureMgr)fm).getFeatureSlots(e.getTypeId());
				for (FspFeatureMapId mapId: slotsList){
					FspFeatureMap map=fm.getFeatureMap(mapId);
					System.out.println("\t\t\twith feature slot for '"+mapId.getLabel()+"' of type "+map.getType().getLabel());
				}
			}
			
			System.out.println("\tCreate Initial State for Mission");
			
			
			List<FspMissionId> missions=sm.getMissions();
			
			if(missions.isEmpty()){
				loadMissions(sm);
				missions=sm.getMissions();
			}
			
			FspState here=null;
			Date horizon=null;
			for (FspMissionId mId: missions){
				FspMission m=sm.getMission(mId);
				FspStateId root=m.getRoot();
				System.out.println("Found Mission: "+mId.getLabel()+" for "+m.getLabel()+" root="+root.getLabel()+" with horizon="+m.getHorizon());
				FindBlanks fb=new FindBlanks(rt);
				here=sm.getState(root); //TODO: hack to just keep last mission's root state for building from
				horizon=m.getHorizon();
				sm.walkStateSpace(here, fb);
				rt.run();
			}
					
			System.out.println("\nStep 2 (main loop): extend mission, iterate check CI, identify bifurcated crowd response, and create conditioning event");

			
			while(true){				
				if (CrowdProjector.checkCrowd(rt,qm)){
					System.out.println("\n\nExtending state space out 1 more year");
					ExtendLeaves el=new ExtendLeaves(rt,qm,horizon);
					rt.getStateMgr().walkStateSpace(here,el);	
					FindBlanks fb=new FindBlanks(rt);
					rt.getStateMgr().walkStateSpace(here,fb);
				} else {
					try {Thread.sleep(1000);} catch (InterruptedException e) { e.printStackTrace(); }
				}
				rt.run();
				try {Thread.sleep(10000);} catch (InterruptedException e) { e.printStackTrace(); }
			}
		
		} catch (FspException e) {
			e.printStackTrace();
		}
		
	}
		

	private static void loadFeatures(FspFeatureMgr fm){
		System.out.println("No Features are currently loaded, loading...");
		try {
			FspFeatureTypeId id= fm.addFeatureType("fsp.jar", "com.ara.fsp.features.BooleanValue");
			FspFeatureMapId mapId=fm.addFeatureMap("free ice cream on campus", id, "1 or 0");
			FspFeatureMap map=fm.getFeatureMap(mapId);
			System.out.println("Added feature '"+map.getLabel()+"' of java type "+map.getType().getLabel());
		} catch (FspException e){
			System.err.println("Failed to create Boolean feature: "+e);
		}
		
		try {
			FspFeatureTypeId id= fm.addFeatureType("fsp.jar", "com.ara.fsp.features.CountableQuantity");
			FspFeatureMapId mapId=fm.addFeatureMap("analysts moved to RTP", id, "number of people");
			FspFeatureMap map=fm.getFeatureMap(mapId);
			System.out.println("Added feature '"+map.getLabel()+"' of java type "+map.getType().getLabel());
		} catch (FspException e){
			System.err.println("Failed to create Countable feature: "+e);
		}
		
		try {
			FspFeatureTypeId id= fm.addFeatureType("fsp.jar", "com.ara.fsp.features.ProbabilisticVariable");
			FspFeatureMapId mapId=fm.addFeatureMap("NCSU football team has winning season", id,"0.0 - 1.0");
			FspFeatureMap map=fm.getFeatureMap(mapId);
			System.out.println("Added feature '"+map.getLabel()+"' of java type "+map.getType().getLabel());
		} catch (FspException e){
			System.err.println("Failed to create Probablistic feature: "+e);
		}
	}

	private static void loadMissions(FspStateMgr sm) {
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

		System.out.println("No Missions are currently loaded, loading...");

		try {
			String label="Energy from 2010";
			FspStateId stateId=sm.addState(df.parse("2010-02-11 13:00:00"), df.parse("2010-12-01 22:00:00"));
			FspMissionId missionId=sm.addMission(label, "This is an energy scenario starting in 1980 and ending in 2016.", stateId,df.parse("2016-02-11 13:00:00"));
			FspMission m= sm.getMission(missionId);
			((StateMgr)sm).setStateLabel(stateId, label, "Initial state for mission, values are randomly assigned using artifical crowd.");
			System.out.println("Added mission "+m.getLabel()+".");
		} catch (FspException | ParseException e){
			System.err.println("Failed to create Mission: "+e);
		}
	}
	
	private static void loadEntities(FspFeatureMgr fm){
		System.out.println("No Entites are currently loaded, loading...");
		try {
			FspEntityTypeId gid= fm.addEntityType("Global");
			FspFeatureTypeId fid4= fm.getFeatureTypeId("com.ara.fsp.features.CountableQuantity");
			FspFeatureMapId mapId4=fm.addFeatureMap("price of oil", fid4, "US $ per barrel");
			fm.addFeatureSlot(gid, mapId4);

			FspEntityTypeId id= fm.addEntityType("Oil Company");
			
			FspFeatureTypeId fid1= fm.getFeatureTypeId("com.ara.fsp.features.CountableQuantity");
			FspFeatureMapId mapId1=fm.addFeatureMap("barrels produced daily", fid1, "millions of barrels of oil");
			fm.addFeatureSlot(id, mapId1);
			
			FspFeatureTypeId fid2= fm.getFeatureTypeId("com.ara.fsp.features.BooleanValue");
			FspFeatureMapId mapId2=fm.addFeatureMap("actively exploring new fields", fid2, "1 or 0");
			fm.addFeatureSlot(id, mapId2);
			
			FspFeatureTypeId fid3= fm.getFeatureTypeId("com.ara.fsp.features.ProbabilisticVariable");
			FspFeatureMapId mapId3=fm.addFeatureMap("will find new oil well", fid3, "0.0 - 1.0");
			fm.addFeatureSlot(id, mapId3);
			
			System.out.println("Added entitytype '"+id.getLabel()+"'");
			
			fm.addEntity(id,"Saudi Aramco","the Saudi Aramco oil company of Saudi Arabia"); //12.5 M barrels/day Saudi
			fm.addEntity(id,"National Iranian Oil","Iraian nationalized oil company"); //6.4 M barrels/day Iran
			fm.addEntity(id,"ExxonMobil","the ExxonMobil oil company of the US"); //5.3 M b/day US
			/*
			fm.addEntity(id,"PetroChina","the PetroChina oil company"); //4.4 M b/day China
			fm.addEntity(id,"BP","the British Petroleum oil company"); //4.1 M b/day UK
			fm.addEntity(id,"Shell","the Royal Dutch Shell oil company"); //3.9 M b/day UK
			fm.addEntity(id,"Pemex","the Mexican Pemex oil company"); //3.6 M b/day Mexico
			fm.addEntity(id,"Chevron","the Chevron oil company of the US"); //3.5 M b/day US
			fm.addEntity(id,"KPC","the Kuwait Petroleum Corp oil company"); //3.2 M b/day Kuwait
			fm.addEntity(id,"ADHO","the Abu Dhabi National Oil company of the UAE"); //2.9 M b/day UAE
			fm.addEntity(id,"Total","the Total Oil company of the France"); //2.7 M b/day France
			fm.addEntity(id,"Petrobras","the Petrobras oil company"); //2.6 M b/day ?
			fm.addEntity(id,"Rosneft","the Rosneft oil company of Russia"); //2.6 M b/day ?
			fm.addEntity(id,"IOM","the Iraqi Oril Ministry"); //2.3 M b/day Iraq
			fm.addEntity(id,"Lukoil","the Lukoil oil company of Russia"); //2.2 M b/day Russia
			fm.addEntity(id,"Eni","the Eni oil company of Italy"); //2.2 M b/day Italy
			fm.addEntity(id,"Statoil","the Statoil oil company of Norway"); //2.1 M b/day Norway
			*/
			
			
		} catch (FspException e){
			System.err.println("Failed to create Oil Company: "+e);
		}
		
		try {
			FspEntityTypeId id= fm.addEntityType("Natural Gas Company");
			
			FspFeatureTypeId fid1= fm.getFeatureTypeId("com.ara.fsp.features.CountableQuantity");
			FspFeatureMapId mapId1=fm.addFeatureMap("amount of natural gas produced daily", fid1, "millions of BTUs per day");
			fm.addFeatureSlot(id, mapId1);
			
			//FspFeatureTypeId fid2= fm.getFeatureTypeId("com.ara.fsp.features.BooleanValue");
			//FspFeatureMapId mapId2=fm.addFeatureMap("incorporating methane in gas mixture", fid2, "1 or 0");
			//fm.addFeatureSlot(id, mapId2);
			
			FspFeatureTypeId fid3= fm.getFeatureTypeId("com.ara.fsp.features.ProbabilisticVariable");
			FspFeatureMapId mapId3=fm.addFeatureMap("will be using fracking", fid3, "0.0 - 1.0");
			fm.addFeatureSlot(id, mapId3);
			
			System.out.println("Added entitytype '"+id.getLabel()+"'");
			
			fm.addEntity(id,"Gazprom","the Gazprom natural gas company"); //9.7 M barrels/day China
			fm.addEntity(id,"Sonatrach","the Gazprom oil company of Algeria"); //2.7 M barrels/day 
			//fm.addEntity(id,"Qatar Petroleum","the Qatar Petroleum oil company"); //2.3 M b/day Qatar
			
			
		} catch (FspException e){
			System.err.println("Failed to create Oil Company: "+e);
		}
		
		try {
			FspEntityTypeId id= fm.addEntityType("Alternative Energy Company");
			
			FspFeatureTypeId fid1= fm.getFeatureTypeId("com.ara.fsp.features.CountableQuantity");
			FspFeatureMapId mapId1=fm.addFeatureMap("customer base", fid1, "number of homes");
			fm.addFeatureSlot(id, mapId1);
			
			//FspFeatureTypeId fid2= fm.getFeatureTypeId("com.ara.fsp.features.BooleanValue");
			//FspFeatureMapId mapId2=fm.addFeatureMap("researching new technologies", fid2, "1 or 0");
			//fm.addFeatureSlot(id, mapId2);
			
			//FspFeatureTypeId fid3= fm.getFeatureTypeId("com.ara.fsp.features.ProbabilisticVariable");
			//FspFeatureMapId mapId3=fm.addFeatureMap("market adoption for domestic use", fid3, "0.0 - 1.0");
			//fm.addFeatureSlot(id, mapId3);
			
			System.out.println("Added entitytype '"+id.getLabel()+"'");
			fm.addEntity(id,"AltaRock Energy","the AltaRock Engery geothermal energy company");
			fm.addEntity(id,"BioFuelBox","The BioFuelBox sewage to diesal company");
			//fm.addEntity(id,"BrightSource","the BrightSource Energy solar energy company");
			//fm.addEntity(id,"Clean Current","the Clear Current Power Systems underwater turbines company");
			
		} catch (FspException e){
			System.err.println("Failed to create Alternative Energy Company: "+e);
		}
		
	}
	
}
