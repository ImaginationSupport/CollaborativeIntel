//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.runtime.mysql;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import com.ara.fsp.api.*;

public class FeatureMgr implements FspFeatureMgr {
	private DataLayer dl=null;
	FspRunTime rt=null;

	//private List<FeatureMapping> list= new ArrayList<FeatureMapping>();
	
	public FeatureMgr(DataLayer dataLayer,FspRunTime rt){
		this.dl=dataLayer;
		this.rt=rt;
		rt.setFeatureMgr(this);
	}
	
	@Override
	public void init() throws FspException {
	}

	@Override
	public boolean hasFeatureType(String featureName) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT EXISTS(SELECT 1 FROM featuretypes WHERE label='"+featureName+"' OR classname='"+featureName+"' LIMIT 1);";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  if (results.getInt(1)==1) return true;
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+"  Failed to check Feature Type:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return false;
	}

	@Override
	public FspFeatureTypeId getFeatureTypeId(String featureName) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label FROM featuretypes WHERE label='"+featureName+"' OR classname='"+featureName+"' LIMIT 1;";
		int id=-1;
		String label=null;
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      if(results.next()){
		    	  id=results.getInt("id");
		    	  label=results.getString("label");
		      } else {
		    	  throw new FspException(this.getClass().getName()+" No Feature Type for "+featureName+" is loaded.");
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Feature Type Id for "+featureName+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return new FeatureTypeId(id,label);
	}


	@Override
	public List<FspFeatureId> getFeatureIds(String typeName) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		ArrayList<FspFeatureId> ids=new ArrayList<FspFeatureId>();
		String query="SELECT features.id as id from features, featuretypes where featuretypes.id=features.featuretypeid;";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  ids.add(new FeatureId(results.getInt("id")));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find ids:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return ids;
	}
	
	@Override
	public List<FspFeatureId> getFeatureIdsByType(String typeName) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		ArrayList<FspFeatureId> ids=new ArrayList<FspFeatureId>();
		String query="SELECT features.id as id from features, featuretypes where featuretypes.id=features.featuretypeid and featuretypes.classname='"+typeName+"';";
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  ids.add(new FeatureId(results.getInt("id")));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find ids with type "+typeName+":"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return ids;
	}

	@Override
	public FspFeatureTypeId addFeatureType(String jarFile, String className) throws FspException {		
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;	
		String label="Unknown";
		String desc="Not Set.";
		FspFeature ft=null;
		try {
			ft=loadFeatureType(jarFile, className);
			label=ft.getLabel();
			desc=ft.getDesc();
		} catch (FspException e){
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Feature Type:"+e);
		}
		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO featuretypes (jar, classname, label, description) values (?, ?, ?, ?)",
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
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Feature Type:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		if (id==-1) throw new FspException(this.getClass().getName()+" Failed to Insert Feature Type.");

		return new FeatureTypeId(id,label);
	}

	@Override
	public FspFeature loadFeature(FspFeatureTypeId featureTypeId) throws FspException{
		FeatureType ft=(FeatureType)getFeatureType(featureTypeId);
		FspFeature f=loadFeatureType(ft.jarName,ft.className);
		return f;
	}
	
	private FspFeature loadFeatureType(String jarName, String className) throws FspException{
		Class<?> c;
		FspFeature o=null;
		try {
			c = Class.forName(className);
			Constructor<?> ctor;
			ctor = c.getConstructor();
			o = (FspFeature) ctor.newInstance();
		} catch (ClassNotFoundException e2) {
			throw new FspException(this.getClass().getName()+" could not find Java class "+className+".");
		} catch (NoSuchMethodException | SecurityException e1) {
			throw new FspException(this.getClass().getName()+" FeatureType implementation is incorrect for "+className+".");
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException e) {
			throw new FspException(this.getClass().getName()+" unable to load Java class "+className+".");
		}
		return o;
	}

	
	@Override
	public List<FspFeatureTypeId> getFeatureTypes() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label from featuretypes;";
		List<FspFeatureTypeId> list=new ArrayList<FspFeatureTypeId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  FeatureTypeId fm=new FeatureTypeId(id,label);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find ids:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

	@Override
	public FspFeatureType getFeatureType(FspFeatureTypeId id) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		FeatureType ft=null;
		String query="SELECT id, jar, classname, label, description FROM featuretypes WHERE featuretypes.id=? LIMIT 1;";
		try {
			  statement = dl.connect().prepareStatement(query);
		      statement.setInt(1, ((FeatureTypeId)id).value);
		      ResultSet results=statement.executeQuery();
		      if(results.next()){
		    	  int typeId=results.getInt("id");
		    	  String jarName=results.getString("jar");
		    	  String className=results.getString("classname");
		    	  String label=results.getString("label");
		    	  String desc=results.getString("description");
		    	  ft=new FeatureType(new FeatureTypeId(typeId,label), label, desc, jarName, className);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find FeatureMap by Id "+id.getLabel()+": "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return ft;
	}
	
	@Override
	public FspFeatureMapId addFeatureMap(String label, FspFeatureTypeId typeId, String units) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO featuremaps (label, featuretypeid, units) values (?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, label);
		      statement.setInt(2, ((FeatureTypeId)typeId).value);
		      statement.setString(3, units);
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
		return new FeatureMapId(id,label);
	}
	
	@Override
	public List<FspFeatureMapId> getFeatureMaps() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label from featuremaps;";
		List<FspFeatureMapId> list=new ArrayList<FspFeatureMapId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  FeatureMapId fm=new FeatureMapId(id,label);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find ids:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}
	
	@Override
	public FspFeatureMap getFeatureMap(FspFeatureMapId id) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		FeatureMap fm=null;
		String query="SELECT fm.id as id, fm.label as label, fm.units as units, ft.id as typeid, ft.label as typelabel FROM featuremaps as fm, featuretypes as ft WHERE ft.id=fm.featuretypeid AND fm.id=? LIMIT 1;";
		try {
			  statement = dl.connect().prepareStatement(query);
		      statement.setInt(1, ((FeatureMapId)id).value);
		      ResultSet results=statement.executeQuery();
		      if(results.next()){
		    	  int mapId=results.getInt("id");
		    	  String label=results.getString("label");
		    	  int typeId=results.getInt("typeid");
		    	  String typeLabel=results.getString("typelabel");
		    	  String units=results.getString("units");
		    	  fm=new FeatureMap(new FeatureMapId(mapId,label), label, getFeatureType(new FeatureTypeId(typeId,typeLabel)),units);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find FeatureMap by Id "+id.getLabel()+": "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return fm;
	}

	@Override
	public List<FspFeatureMap> getFeatureMaps(FspEntityTypeId eTypeId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT fm.id as id, fm.label as label, fm.units as units, fm.featuretypeid as typeid, ft.label as typelabel FROM featuremaps as fm, featuretypes as ft, featureslots as fs WHERE fm.featuretypeid=ft.id AND fs.featuremapid=fm.id AND fs.entitytypeid="+((EntityTypeId)eTypeId).value+";";
		List<FspFeatureMap> list=new ArrayList<FspFeatureMap>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  int typeId=results.getInt("typeid");
		    	  String typeLabel=results.getString("typelabel");
		    	  String units=results.getString("units");
		    	  FeatureMap fm=new FeatureMap(new FeatureMapId(id,label), label, getFeatureType(new FeatureTypeId(typeId,typeLabel)),units);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Feature Maps for EntityType '"+eTypeId.getLabel()+"':"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}
	
	public List<FspFeatureMap> getALLFeatureMaps() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT fm.id as id, fm.label as label, fm.units as units, fm.typeid as typeid, ft.label as typelabel FROM featuremaps as fm, featuretypes as ft WHERE fm.typeid=ft.id;";
		List<FspFeatureMap> list=new ArrayList<FspFeatureMap>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  int typeId=results.getInt("typeid");
		    	  String typeLabel=results.getString("typelabel");
		    	  String units=results.getString("units");
		    	  FeatureMap fm=new FeatureMap(new FeatureMapId(id,label), label, getFeatureType(new FeatureTypeId(typeId,typeLabel)),units);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find All Feature Maps:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

	@Override
	public FspEntityTypeId addEntityType(String label) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO entitytypes (label) values (?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, label);
		      statement.executeUpdate();
			  ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Entity Type:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return new EntityTypeId(id,label);
	}

	@Override
	public List<FspEntityTypeId> getEntityTypes() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label from entitytypes;";
		List<FspEntityTypeId> list=new ArrayList<FspEntityTypeId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  EntityTypeId fm=new EntityTypeId(id,label);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Entity Types:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

//	@Override
//	public FspEntityType getEntityType(FspEntityTypeId id) throws FspException {
//		// TODO Auto-generated method stub
//		return null;
//	}

	@Override
	public void addFeatureSlot(FspEntityTypeId typeId, FspFeatureMapId mapId) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO featureslots (entitytypeid, featuremapid) values (?,?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, ((EntityTypeId)typeId).value);
		      statement.setInt(2, ((FeatureMapId)mapId).value);
		      statement.executeUpdate();
			  ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
		      if(id<0) throw new FspException(this.getClass().getName()+" had no generated key returned on insert.");
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Feature Slot: "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return;
	}

	@Override
	public List<FspFeatureMapId> getFeatureSlots(FspEntityTypeId typeId) throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT f.id, f.label FROM featureslots as s, featuremaps f WHERE s.entitytypeid="+((EntityTypeId)typeId).value+" AND s.featuremapid=f.id;";
		List<FspFeatureMapId> list=new ArrayList<FspFeatureMapId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  FeatureMapId fm=new FeatureMapId(id,label);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to find feature slots for entity:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

	@Override
	public FspEntityId addEntity(FspEntityTypeId typeId, String label, String desc) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO entities (typeid, label, description) values (?,?,?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, ((EntityTypeId)typeId).value);
		      statement.setString(2, label);
		      statement.setString(3, desc);
		      statement.executeUpdate();
			  ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Entity:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return new EntityId(id,label);
	}

	@Override
	public List<FspEntityId> getEntities() throws FspException {
		Statement statement = null;
		ResultSet resultSet = null;
		String query="SELECT id, label FROM entities;";
		List<FspEntityId> list=new ArrayList<FspEntityId>();
		try {
		      statement = dl.connect().createStatement();
		      ResultSet results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  String label=results.getString("label");
		    	  EntityId fm=new EntityId(id,label);
		    	  list.add(fm);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Entities:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return list;
	}

	@Override
	public FspEntity getEntity(FspEntityId id) throws FspException {
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Entity en=null;
		String query="SELECT e.id as id, e.typeid as typeid, et.label as typelabel, e.label as label, e.description as description FROM entities as e, entitytypes as et WHERE e.id=? AND e.typeid=et.id LIMIT 1;";
		try {
			  statement = dl.connect().prepareStatement(query);
		      statement.setInt(1, ((EntityId)id).value);
		      ResultSet results=statement.executeQuery();
		      if(results.next()){
		    	  int eid=results.getInt("id");
		    	  int typeId=results.getInt("typeid");
		    	  String tLabel=results.getString("typelabel");
		    	  String label=results.getString("label");
		    	  String desc=results.getString("description");
		    	  EntityTypeId etId= new EntityTypeId(typeId,tLabel);
		    	  en=new Entity(new EntityId(eid,label),etId, label, desc);
		    	  en.setFeatureMaps(getFeatureMaps(etId));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Find Entity by Id "+id.getLabel()+": "+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return en;
	}
	


}
