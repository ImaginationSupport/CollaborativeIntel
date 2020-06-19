//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.CrowdProjector;

public class StateMgr implements FspStateMgr {

	private DataLayer dl=null;
	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	FspRunTime rt=null;
	FspFeatureMgr featureMgr=null;

	
	public StateMgr(DataLayer dataLayer,FspRunTime rt, FspFeatureMgr featureMgr){
		this.dl=dataLayer;
		this.rt=rt;
		rt.setStateMgr(this);
		this.featureMgr=featureMgr;
	}
	
	@Override
	public void init() throws FspException {
	}

	@Override
	public boolean hasState(FspStateId stateId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		if (!(stateId instanceof StateId)) throw new FspException ("Not a StateId");
		StateId sid=(StateId) stateId;
		String query="SELECT EXISTS(SELECT 1 from states where where id='"+sid.value+"' LIMIT 1);";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  if (results.getInt(1)==1) return true;
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find State:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return false;
	}

	@Override
	public FspState getState(FspStateId stateId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;

		if (!(stateId instanceof StateId))
			throw new FspException((this.getClass().getName()+" StateId not correct format."));
		State s=null;
		String query="SELECT id, p, start, end from states where id='"+((StateId)stateId).value+"' LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  s=new State(new StateId(results.getInt("id")));
		    	  s.setP(results.getDouble("p"));
		    	  s.setStart(results.getDate("start"));
		    	  s.setEnd(results.getDate("end"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find State with id "+((StateId)stateId).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return s;
	}


	@Override
	public List<FspEdgeId> getNextEdges(FspStateId stateId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;

		if (!(stateId instanceof StateId))
			throw new FspException((this.getClass().getName()+" StateId not correct format."));
		
		List<FspEdgeId> edges =new ArrayList<FspEdgeId>();
		String query="SELECT id, label FROM edges where prev='"+((StateId)stateId).value+"';";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		      	  FspEdgeId edge=new EdgeId(id,label);
		      	  edges.add(edge);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Edges with previous StateId "+((StateId)stateId).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return edges;
	}

	@Override
	public FspEdgeId getPrevEdge(FspStateId stateId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;

		if (!(stateId instanceof StateId))
			throw new FspException((this.getClass().getName()+" StateId not correct format."));
		
		FspEdgeId edge=null;
		String query="SELECT id, label FROM edges where next='"+((StateId)stateId).value+"' LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		      	  edge=new EdgeId(id,label);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Edge with next StateId "+((StateId)stateId).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return edge;
	}

	@Override
	public FspEdge getEdge(FspEdgeId edgeId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;

		if (!(edgeId instanceof EdgeId))
			throw new FspException((this.getClass().getName()+" EdgeId not correct format."));
		Edge edge=null;
		String query="SELECT id, label, description, p, prev, next FROM edges where id='"+((EdgeId)edgeId).value+"' LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  FspStateId prev=new StateId(results.getInt("prev"));
		    	  FspStateId next=new StateId(results.getInt("next"));
		    	  edge=new Edge(prev,next);
		    	  edge.setP(results.getDouble("p"));
		    	  edge.setLabel(results.getString("label"));
		    	  edge.setDesc(results.getString("description"));
		    	  edge.setId(new EdgeId(results.getInt("id"),edge.getLabel()));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Edge with id "+((EdgeId)edgeId).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return edge;
	}

	@Override
	public FspEdge addState(FspState prev, Date start, Date end, String edgeLabel, String edgeDesc) throws FspException {
		if (!(prev instanceof State))
			throw new FspException((this.getClass().getName()+" StateId not correct format."));
		
		FspStateId stateId=addState(start,end);
		FspEdgeId edgeId=addEdge(prev.getId(),stateId,edgeLabel,edgeDesc);
		FspEdge edge=getEdge(edgeId);
		return edge;
	}

	@Override
	public FspStateId addState(Date start, Date end) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		State s=null;
		int id=-1;
		String query="INSERT INTO states (`start`,`end`,`p`) VALUES ('"+df.format(start)+"','"+df.format(end)+"','-1.0');";
		try {
		      statement = dl.connect().createStatement();
		      statement.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
	    	  ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to create State:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		FspStateId sId=new StateId(id);
		rt.addState(sId);
		return sId;
	}
	
	public void setStateLabel(FspStateId stateId, String label, String description) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "UPDATE states SET label=?, description=? WHERE id=?",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, label);
		      statement.setString(2, description);
		      statement.setInt(3,((StateId)stateId).value);
		      statement.executeUpdate();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to set labels on State:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
	}

	public FspEdgeId addEdge(FspStateId prev, FspStateId next, String label, String description) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		
		if (!(prev instanceof StateId) || !(next instanceof StateId))
			throw new FspException("Incorrect StateId types passed into add edge.");
		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO edges (label, description, prev, next) values (?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, label);
		      statement.setString(2, description);
		      statement.setInt(3, ((StateId)prev).value);
		      statement.setInt(4, ((StateId)next).value);
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Edge:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return new EdgeId(id,label);
	}
	
	@Override
	public FspProjectorTypeId addProjectorType(String jarFile, String className, String label, String desc) throws FspException{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO projectortypes (jar, class, label, description) values (?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, jarFile);
		      statement.setString(2, className);
		      statement.setString(3, label);
		      statement.setString(4, desc);
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Projector Type:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		if (id==-1) throw new FspException(this.getClass().getName()+" Failed to Insert Projector Type.");

		return new ProjectorTypeId(id,label);
	}

	@Override
	public List<FspProjectorTypeId> getProjectorTypes() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label from projectortypes;";
		List<FspProjectorTypeId> list=new ArrayList<FspProjectorTypeId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  ProjectorTypeId p=new ProjectorTypeId(id,label);
		    	  list.add(p);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Projector Type ids:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

	@Override
	public FspProjectorType getProjectorType(FspProjectorTypeId id) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		FspProjectorType pt=null;
		String query="SELECT id, jar, class, label, description from projectortypes where id=? limit 1;";
		try {
			  statement = dl.connect().prepareStatement(query);
		      statement.setInt(1, ((ProjectorTypeId)id).value);
		      ResultSet results=statement.executeQuery();
		      if(results.next()){
		    	  int typeId=results.getInt("id");
		    	  String jarName=results.getString("jar");
		    	  String className=results.getString("class");
		    	  String label=results.getString("label");
		    	  String desc=results.getString("description");
		    	  pt=new ProjectorType(new ProjectorTypeId(typeId,label), label, desc, jarName, className);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find ProjectorType by Id "+id.getLabel()+": "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return pt;
	}
	
	@Override
	public FspProjector getAssignedProjector(FspFeatureMapId featureMapId) throws FspException {
		// TODO: Need to look up the assigned projector for a specific feature
		CrowdProjector cp=new CrowdProjector();
		cp.init(rt);
		return cp;
	}

	@Override
	public FspMissionId addMission(String label, String desc, FspStateId initState, Date horizon) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO missions (label, description, root, horizon) values (?, ?, ?, '"+df.format(horizon)+"')",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, label);
		      statement.setString(2, desc);
		      statement.setInt(3, ((StateId)initState).value);
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Mission:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		if (id==-1) throw new FspException(this.getClass().getName()+" Failed to Insert Mission.");

		return new MissionId(id,label);
	}

	@Override
	public FspMission getMission(FspMissionId id) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;

		if (!(id instanceof MissionId))
			throw new FspException((this.getClass().getName()+" MissionId not correct format."));
		Mission m=null;
		String query="SELECT id, label, description, root, horizon FROM missions where id='"+((MissionId)id).value+"' LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  m=new Mission(new MissionId(results.getInt("id"),results.getString("label")));
		    	  m.setLabel(results.getString("label"));
		    	  m.setDesc(results.getString("description"));
		    	  m.setRoot(new StateId(results.getInt("root")));
		    	  m.setHorizon(results.getDate("horizon"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Mission with id "+((MissionId)id).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return m;
	}
	
	@Override
	public List<FspMissionId> getMissions() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		ArrayList<FspMissionId> missions=new ArrayList<FspMissionId>();
		String query="SELECT id, label from missions;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id= results.getInt("id");
		    	  String label=results.getString("label");
		    	  MissionId m=new MissionId(id,label);
		    	  missions.add(m);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load Missions:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return missions;
	}
	
	public List<FspMission> getAllMissions() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		ArrayList<FspMission> missions=new ArrayList<FspMission>();
		String query="SELECT id, label, desc, root, horizon from missions;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  Mission m=new Mission(new MissionId(results.getInt("id"),results.getString("label")));
		    	  m.setDesc(results.getString("desc"));
		    	  m.setRoot(new StateId(results.getInt("root")));
		    	  m.setHorizon(results.getDate(""));
		    	  missions.add(m);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load Missions:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return missions;
	}

	@Override
	public FspMission createMission(String label, String desc, Date start, Date horizon) {
		FspMission m=null;
		try {
			FspStateId stateId=addState(start, start);
			FspMissionId missionId=addMission(label, desc, stateId, horizon);
			m= getMission(missionId);
		} catch (FspException e){
			e.printStackTrace();
		}
		return m;
	}
	
	
	public void walkStateSpace(FspMission mission, FspStateSpaceForwardWalker walker) throws FspException {
		FspStateId rootId=mission.getRoot();
		FspState root=getState(rootId);
		walkStateSpace(root,walker);
	}
	
	@Override
	public void walkStateSpace(FspState state, FspStateSpaceForwardWalker walker) throws FspException {
		if(walker.onState(state)){
			List<FspEdgeId> edgeIds=getNextEdges(state.getId());
			for(FspEdgeId edgeId: edgeIds){
				if (edgeId==null) continue;
				FspEdge edge=getEdge(edgeId);
				if(edge==null) return;
				FspConditionOption option=edge.getConditionOption();
				if(walker.onConditioningEvent(option)){
					FspState next= getState(edge.getNext());
					walkStateSpace(next,walker);
				}
			}
		}
	}

	@Override
	public void walkStateSpace(FspState state, FspStateSpaceBackwardWalker walker) throws FspException {
		if(walker.onState(state)){
			FspEdgeId edgeId=getPrevEdge(state.getId());
			if (edgeId==null) return;
			FspEdge edge=getEdge(edgeId);
			if(edge==null) return;
			for (FspConditionOption option: rt.getStateMgr().getConditions(edgeId)){
				if(option!=null && !walker.onConditioningEvent(option)) return;
			}
			//FspConditionOption option=edge.getConditionOption();

			FspState prev= getState(edge.getPrevious());
			walkStateSpace(prev,walker);
			
		}
	}
	
	
	@Override
	public FspFeature getFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmapId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		FspFeature feature=null;
		String query="SELECT f.id as id, f.value as value, f.confidence as confidence, "
				+" t.id as typeid FROM features as f, featuretypes as t, featuremaps as m "
				+" WHERE f.stateId="+((StateId)stateId).value
					+" AND f.entityId="+((EntityId)entityId).value
					+" AND f.featureMapId="+((FeatureMapId)fmapId).value+" "
					+" AND m.id=f.featureMapId AND t.id=m.featuretypeid LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id= results.getInt("id");
		    	  String value=results.getString("value");
		    	  double c=results.getDouble("confidence");
		    	  int typeId= results.getInt("typeid");
		    	  feature=featureMgr.loadFeature(new FeatureTypeId(typeId,""));
		    	  feature.fromString(value);
		    	  feature.setConfidence(c);
		    	  feature.setId(new FeatureId(id));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to get feature "+"HERE"+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return feature;
	}
	
	@Override
	public FspFeature getFeature(FspFeatureId featureId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		FspFeature feature=null;
		String query="SELECT f.id, f.value, f.confidence, t.id as typeid "
				+" FROM features as f, featuretypes as t, featuremaps as m "
				+" WHERE f.id="+((FeatureId)featureId).value
				+" AND t.id=m.featuretypeid AND m.id=f.featuremapid LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id= results.getInt("id");
		    	  String value=results.getString("value");
		    	  double c=results.getDouble("confidence");
		    	  int typeId= results.getInt("typeid");
		    	  feature=featureMgr.loadFeature(new FeatureTypeId(typeId,""));
		    	  feature.fromString(value);
		    	  feature.setConfidence(c);
		    	  feature.setId(new FeatureId(id));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to get feature "+"HERE"+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return feature;
	}
	
	@Override
	public boolean hasFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmapId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		boolean rt=false;
		String query="SELECT f.id as id, f.value as value, f.confidence as confidence, "
				+" t.id as typeid FROM features as f, featuretypes as t, featuremaps as m "
				+" WHERE f.stateId="+((StateId)stateId).value
					+" AND f.entityId="+((EntityId)entityId).value
					+" AND f.featureMapId="+((FeatureMapId)fmapId).value+" "
					+" AND m.id=f.featureMapId AND t.id=m.featuretypeid LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      rt=results.next();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to get feature "+"HERE"+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return rt;
	}

	@Override
	public FspFeatureId addFeature(FspStateId stateId, FspEntityId entityId, FspFeatureMapId fmap, FspFeature feature) throws FspException{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO features (stateId, entityId, featureMapId, value, confidence) values (?, ?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, ((StateId)stateId).value);
		      statement.setInt(2, ((EntityId)entityId).value);
		      statement.setInt(3, ((FeatureMapId)fmap).value);
		      statement.setString(4, feature.toString());
		      statement.setDouble(5, feature.getConfidence());
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Feature:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		if (id==-1) throw new FspException(this.getClass().getName()+" Failed to Insert Feature.");

		return new FeatureId(id);
	}

	@Override
	public void updateFeature(FspFeatureId featureId, FspFeature feature) throws FspException{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try {
		      statement = dl.connect().prepareStatement("UPDATE features SET value=?, confidence=? WHERE id=?");
		      statement.setString(1, feature.toString());
		      statement.setDouble(2, feature.getConfidence());
		      statement.setInt(3, ((FeatureId)featureId).value);
		      statement.executeUpdate();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Update Feature:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return;
	}

	@Override
	public List<FspConditionOption> getConditions(FspEdgeId edge) throws FspException{
		Statement statement = null;
		ResultSet resultSet = null;
		List<FspConditionOption> options =new ArrayList<FspConditionOption>();
		String query="SELECT co.id, co.label, co.p, c.id as cid, c.type, c.label as clabel, c.description "
				+ "FROM conditionoptions as co, conditions as c WHERE co.conditionid=c.id AND edgeId="+((EdgeId)edge).value+";";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  double p=results.getDouble("p");
		    	  int cid=results.getInt("cid");
		    	  String type=results.getString("type");
		    	  String clabel=results.getString("clabel");
		    	  String cdesc=results.getString("description");		    	  
		    	  FspCondition cond=new Condition(new ConditionId(cid,clabel),FspConditionType.valueOf(type),clabel,cdesc);
		      	  FspConditionOption option=new ConditionOption(cond, id);
		      	  	option.setLabel(label);
		      	  	option.setP(p);
		      	  options.add(option);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Conditons with edge: "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return options;
	}

	@Override
	public FspConditionOption addConditionOption(FspConditionOption option, FspEdgeId edgeId) throws FspException {
		FspCondition c=option.getCondition();
		if (c.getId()==null){
			c=addCondition(c.getType(),c.getLabel(),c.getDesc());
		}
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO conditionoptions (conditionid, edgeid, label, p) values (?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1,((ConditionId)c.getId()).value);
		      statement.setInt(2,((EdgeId)edgeId).value);

		      statement.setString(3, option.getLabel());
		      statement.setDouble(4, option.getP());
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Mission:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		if (id==-1) throw new FspException(this.getClass().getName()+" Failed to Insert Mission.");
		
		FspConditionOption r= new ConditionOption(c, id);
		r.setLabel(option.getLabel());
		r.setP(option.getP());
		return r;
	}
	
	@Override
	public FspCondition addCondition(FspConditionType type, String label, String desc) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO conditions (type, label, description) values (?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, type.toString());
		      statement.setString(2, label);
		      statement.setString(3, desc);
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Condition:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }		
		return new Condition(new ConditionId(id,label), type, label, desc);
	}

	@Override
	public FspStateId getStateIdForFeature(FspFeatureId id) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		if (!(id instanceof FeatureId))
			throw new FspException((this.getClass().getName()+" Feature Id not correct format."));
		StateId s=null;
		String query="SELECT stateid FROM features WHERE id='"+((FeatureId)id).value+"' LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  s=new StateId(results.getInt("stateid"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find StateId for feature with id "+((FeatureId)id).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return s;
	}
	
	@Override
	public FspEntityId getEntityIdForFeature(FspFeatureId id) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		if (!(id instanceof FeatureId))
			throw new FspException((this.getClass().getName()+" Feature Id not correct format."));
		EntityId s=null;
		String query="SELECT e.id, e.label FROM features as f, entities as e WHERE f.id='"+((FeatureId)id).value+"' and f.entityid=e.id LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  s=new EntityId(results.getInt("id"),results.getString("label"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find StateId for feature with id "+((FeatureId)id).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return s;
	}

	@Override
	public FspEdge cloneState(FspStateId stateId) throws FspException {
		FspEdgeId edgeId=getPrevEdge(stateId);		
		if (edgeId==null) return null;
		FspEdge edge=getEdge(edgeId);
		if(edge==null) return null;
		FspState prev=getState(edge.getPrevious());
		FspState state=getState(stateId);
		FspEdge edgeNew=addState(prev, state.getStart(), state.getEnd(), "(unknown)", "(unknown)");
		PreparedStatement statement = null;
		PreparedStatement statement2 = null;
		ResultSet resultSet = null;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO features (entityid, featuremapid, stateid, value, confidence) SELECT entityid,featuremapid,?,value,confidence FROM features WHERE stateid=? AND confidence<>-1.0",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1,((StateId)edgeNew.getNext()).value);
		      statement.setInt(2,((StateId)stateId).value);
		      statement.executeUpdate();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Clone State:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
		    	if (statement2 != null) {statement2.close();}
	    	} catch(Exception e){}
	    }
		return edgeNew;
	}

	@Override
	public FspFeatureMapId getFeatureMapIdForFeature(FspFeatureId id) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		if (!(id instanceof FeatureId))
			throw new FspException((this.getClass().getName()+" Feature Id not correct format."));
		FeatureMapId s=null;
		String query="SELECT m.id, m.label FROM features as f, featuremaps as m WHERE f.id='"+((FeatureId)id).value+"' and f.featuremapid=m.id LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  s=new FeatureMapId(results.getInt("id"),results.getString("label"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find FeatureMapId for feature with id "+((FeatureId)id).value+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return s;
	}

}
