//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.questions.ConditionQuestion;
import com.ara.fsp.ci.questions.FeatureQuestion;
import com.ara.fsp.runtime.mysql.Condition;
import com.ara.fsp.runtime.mysql.ConditionId;
import com.ara.fsp.runtime.mysql.ConditionOption;
import com.ara.fsp.runtime.mysql.ConditionOptionId;
import com.ara.fsp.runtime.mysql.DataLayer;
import com.ara.fsp.runtime.mysql.EdgeId;
import com.ara.fsp.runtime.mysql.FeatureId;
import com.ara.fsp.runtime.mysql.State;
import com.ara.fsp.runtime.mysql.StateId;

public class QuestionMgr {
	
	private DataLayer dl=null;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	private FspRunTime rt=null;
	private static QuestionMgr self=null;
	
	public static QuestionMgr getInstance(){
		return self;
	}
	
	public FspRunTime getRunTime(){
		return rt;
	}
	
	public QuestionMgr(DataLayer dataLayer,FspRunTime rt){
		this.dl=dataLayer;
		this.rt=rt;
		QuestionMgr.self=this;
	}

	public List<Question> getQuestions() throws FspException{
		Statement statement = null;
		ResultSet results = null;
		ArrayList<Question> questions=new ArrayList<Question>();
		String query="SELECT id, label, questiontype, context, question, active FROM ciquestions WHERE active=1;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  boolean active=results.getBoolean("active");
		    	  String test=results.getString("questiontype");
		    	  QuestionType type=QuestionType.valueOf(test);
		    	  String label=results.getString("label");
		    	  Question q=getQuestion(id,label,type,active);
		    	  q.setQuestion(results.getString("question"));
		    	  q.setContext(results.getString("context"));
		    	  loadContext(q);
		    	  questions.add(q);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return questions;
	}
		
	protected void loadContext(Question question) throws FspException{
		if(question.getId()<1) return;
		
		// Feature Context
		{
			Statement statement = null;
			ResultSet results = null;		
			String query="SELECT f.id as featureid, f.stateid as stateid "
					+"FROM cicontextfeatures as cf, features as f "
					+"WHERE cf.questionid="+question.getId()+" "
					+"AND cf.featureid=f.id;";
			try {
			      statement = dl.connect().createStatement();
			      results=statement.executeQuery(query);
			      while(results.next()){
			    	  int fid=results.getInt("featureid");
			    	  int sid=results.getInt("stateid");
			    	  question.addContextFact(new StateId(sid),new FeatureId(fid));
			      }
		    } catch (Exception e) {
		    	throw new FspException(this.getClass().getName()+" Failed to load CI question:"+e);
		    } finally {
		    	try{
			    	if (results != null) {results.close();}
			    	if (statement != null) {statement.close();}
		    	} catch(Exception e){}
		    }
		}
		
		// Condition Context
		{
			Statement statement = null;
			ResultSet results = null;
			String query="SELECT id, conditionoptionid "
					+"FROM cicontextconditions "
					+"WHERE questionid="+question.getId()+";";
			try {
			      statement = dl.connect().createStatement();
			      results=statement.executeQuery(query);
			      while(results.next()){
			    	  int oid=results.getInt("conditionoptionid");
			    	  FspConditionOption co=getOption(oid);
			    	  question.addContextConditioningEvent(co);
			      }
		    } catch (Exception e) {
		    	throw new FspException(this.getClass().getName()+" Failed to load CI question:"+e);
		    } finally {
		    	try{
			    	if (results != null) {results.close();}
			    	if (statement != null) {statement.close();}
		    	} catch(Exception e){}
		    }
		}
	}
	
	public FspConditionOption getOption(int optionId) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		Question q=null;
		String query="SELECT co.id , co.conditionid, co.edgeid, co.label, co.p, c.type, c.label as clabel, c.description  "
				+"FROM conditions as c, conditionoptions as co "
				+"WHERE co.id="+optionId+" "
				+"AND co.conditionid=c.id LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  int conditionId=results.getInt("conditionid");
		    	  int edgeId=results.getInt("edgeid");
		    	  String label=results.getString("label");
		    	  double p=results.getDouble("p");
		    	  FspConditionType type=FspConditionType.valueOf(results.getString("type"));	    	  
		    	  String clabel=results.getString("clabel");
		    	  String description=results.getString("description");

		    	  FspCondition c=new Condition(new ConditionId(conditionId,clabel), type, clabel, description);
		    	  FspConditionOption co=new ConditionOption(c, id);
		    	  co.setLabel(label);
		    	  co.setP(p);
		    	  return co;
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI condition option "+optionId+":"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return null;
	}
	
	public List<FspFeature> getTargetFeatures(Question q) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		ArrayList<FspFeature> targets=new ArrayList<FspFeature>();
		String query="SELECT f.featureid FROM citargetfeatures as f, ciquestions as q WHERE "
				+"f.questionid=q.id AND q.id="+q.getId()+";";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("featureid");
		    	  FspFeature f=rt.getStateMgr().getFeature(new FeatureId(id));
		    	  if (f!=null) targets.add(f);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI question targets:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return targets;
	}
	
	public int addQuestion(Question question) throws FspException {
		PreparedStatement statement = null;
		ResultSet results = null;
		int id=-1;				
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO ciquestions (questiontype,label,question,units,active,context) values (?, ?, ?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, question.getType().toString());
		      statement.setString(2, question.getLabel());
		      statement.setString(3, question.getQuestion());
		      statement.setString(4, question.getUnits());
		      statement.setBoolean(5, question.isActive());
		      statement.setString(6, question.getContext());
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }

	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
	    question.setId(id);
	    if(question.getType()==QuestionType.CONDITION && id!=-1)
	    	addConditioningQuestionText((ConditionQuestion)question);
		return id;
	}
	
	public int addConditioningQuestionText(ConditionQuestion question) throws FspException{
		PreparedStatement statement = null;
		ResultSet results = null;
		int id=-1;				
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO ciconditions (questionid,question,option1,option2,option1value,option2value,option1conf,option2conf,option1edgeid,option2edgeid,featurequestionid) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, question.getId());
		      statement.setString(2, question.getConditionQuestion());
		      statement.setString(3, question.getOption1());
		      statement.setString(4, question.getOption2());
		      statement.setString(5, question.getOption1Value());
		      statement.setString(6, question.getOption2Value());
		      statement.setDouble(7, question.getOption1Conf());
		      statement.setDouble(8, question.getOption2Conf());
		      statement.setInt(9, question.getOption1EdgeId());
		      statement.setInt(10, question.getOption2EdgeId());
		      statement.setInt(11, question.getFeatureQuestionId());
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Conditioning Question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return id;
		
	}
	
	public ConditionQuestion getConditioningQuestionText(Question question) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		ConditionQuestion q=new ConditionQuestion("");
		String query="SELECT id,questionid,question,option1,option2,option1value,option2value,option1conf,option2conf,option1edgeid,option2edgeid,featurequestionid "
				+"FROM ciconditions "
				+"WHERE questionid="+question.getId()+" LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  q.setId(results.getInt("id"));
		    	  q.setConditionQuestion(results.getString("question"));
		    	  q.setId(results.getInt("questionid"));
		    	  q.setOption1(results.getString("option1"));
		    	  q.setOption2(results.getString("option2"));
		    	  q.setOption1Value(results.getString("option1value"));
		    	  q.setOption2Value(results.getString("option2value"));
		    	  q.setOption1Conf(results.getDouble("option1conf"));
		    	  q.setOption2Conf(results.getDouble("option2conf"));
		    	  q.setOption1EdgeId(results.getInt("option1edgeid"));
		    	  q.setOption2EdgeId(results.getInt("option2edgeid"));
		    	  q.setFeatureQuestionId(results.getInt("featurequestionid"));
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI conditioning text by question "+question.getLabel()+":"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return q;
	}
	
	public int updateQuestion(Question question) throws FspException {
		if (question.getId()==-1){
			return addQuestion(question);
		}
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "UPDATE ciquestions SET question=?, context=? ,active=? WHERE id=?;",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, question.getQuestion());
		      statement.setString(2, question.getContext());
		      statement.setBoolean(3, question.isActive());
		      statement.setInt(4, question.getId());
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Update Question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return question.getId();
	}
	
	public void updateAll(){
		try {
			List<Question> questions=getQuestions();
			for(Question q: getQuestions()){
				update(q);
			}
		} catch (FspException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void update(Question question){
		switch(question.getType()){
		case FEATURE:{
			
			break;
		}
		case CONDITION:{
			
			break;
		}
		default:{
			System.err.println("CI Error GetQuestion for Unsupported Type in QuestionMgr.");
		}
	}
	}
	
	private Question getQuestion(int id, String label, QuestionType type, boolean active){
		Question q=null;
		switch(type){
			case FEATURE:{
				q=new FeatureQuestion(id,label,active);
				break;
			}
			case CONDITION:{
				q=new ConditionQuestion(id,label,active);
				break;
			}
			default:{
				System.err.println("CI Error GetQuestion for Unsupported Type in QuestionMgr.");
			}
		}
		return q;
	}
	
	public Question getQuestion(FspFeatureId featureId) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		Question q=null;
		String query="SELECT cq.id, cq.questiontype, cq.label, cq.context, cq.question, cq.active "
				+"FROM ciquestions as cq, citargetfeatures as ctf "
				+"WHERE ctf.featureid="+((FeatureId)featureId).value+" LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  boolean active=results.getBoolean("active");
		    	  QuestionType type=QuestionType.valueOf(results.getString("questiontype"));
		    	  String label=results.getString("label");
		    	  q=getQuestion(id,label,type,active);
		    	  q.setQuestion(results.getString("question"));
		    	  q.setContext(results.getString("context"));
		    	  loadContext(q);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI question by feature "+featureId.getLabel()+":"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return q;
	}
	
	public int addContextCondition(Question question, FspConditionOption option) throws FspException {
		PreparedStatement statement = null;
		ResultSet results = null;
		int id=-1;				
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO cicontextconditions (questionid,conditionoptionid) values (?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, question.getId());
		      statement.setInt(2, ((ConditionOption)option).getId());
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return id;
	}
	
	
	public int addTargetFeature(Question question, FspFeatureId featureId) throws FspException {
		PreparedStatement statement = null;
		ResultSet results = null;
		int id=-1;				
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO citargetfeatures (questionid,featureid,updated) values (?, ?, NOW())",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, question.getId());
		      statement.setInt(2, ((FeatureId)featureId).value);
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Question Target:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return id;
	}
	
	
	public int addContextFeature(Question question, FspFeatureId fId) throws FspException {
		PreparedStatement statement = null;
		ResultSet results = null;
		int id=-1;				
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO cicontextfeatures (questionid,featureid) values (?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, question.getId());
		      statement.setInt(2, ((FeatureId)fId).value);
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert Question:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return id;
	}
	
	public List<CrowdInput> getCrowdInput(FeatureQuestion q) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		ArrayList<CrowdInput> input=new ArrayList<CrowdInput>();
		String query="SELECT value, confidence FROM cicrowdinput WHERE questionid="+q.getId()+";";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  String value=results.getString("value");
		    	  int confidence=results.getInt("confidence");
		    	  CrowdInput ci=new CrowdInput(value,confidence);
		    	  input.add(ci);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load Crowd Input:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return input;
	}
	
	public List<CrowdExplains> getCrowdExplains(ConditionQuestion q) throws FspException{
		Statement statement = null;
		ResultSet results = null;
		ArrayList<CrowdExplains> input=new ArrayList<CrowdExplains>();
		String query="SELECT eventlabel, option1, option2, votes FROM cicrowdexplains WHERE questionid="+q.getId()+" ORDER BY votes DESC;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  String eventlabel=results.getString("eventlabel");
		    	  String option1=results.getString("option1");
		    	  String option2=results.getString("option2");
		    	  int votes=results.getInt("votes");
		    	  CrowdExplains ce=new CrowdExplains(eventlabel,option1,option2,votes);
		    	  input.add(ce);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load Crowd Explaination:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return input;
	}

	public void updateFeature(FspFeature a) throws FspException {
		PreparedStatement statement = null;
		ResultSet results = null;
		try {
		      statement = dl.connect().prepareStatement(
		    		  "UPDATE features SET value=?, confidence=? WHERE id=?;",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setString(1, a.toString());
		      statement.setDouble(2, a.getConfidence());
		      statement.setInt(3, ((FeatureId)a.getId()).value);
		      statement.executeUpdate();
		      results=statement.getGeneratedKeys();
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Update Feature:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return;
	}

	public boolean isOpenConditionQuestion(int value) throws FspException {
		Statement statement = null;
		ResultSet results = null;
		boolean q=false;
		String query="SELECT id FROM ciconditions WHERE (option1edgeid="+value+" or option2edgeid="+value+") LIMIT 1;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  int id=results.getInt("id");
		    	  q=true;
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to check if there was an active conditioning question for edge id "+value+":"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return q;
	}
	
}
