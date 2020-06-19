//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.questions;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import com.ara.fsp.api.*;
import com.ara.fsp.ci.Question;
import com.ara.fsp.ci.QuestionMgr;
import com.ara.fsp.ci.QuestionType;
import com.ara.fsp.runtime.mysql.DataLayer;
import com.ara.fsp.runtime.mysql.FeatureId;

public class CrowdSim {
	private DataLayer dl=DataLayer.getInstance();
	private Random rand=new Random(1);
	private QuestionMgr qm=null;
	private SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");


	public CrowdSim() {
		dl.setJdbc("jdbc:mysql://localhost/fsp?user=root&password=dogstar");
	}

	/*
	public static void main(String[] args) {
		CrowdSim sim=new CrowdSim();
		try {
			while(true){
				Thread.sleep(10);
				sim.run();
			}
		} catch (Exception e) {
			System.err.println(e);
			e.printStackTrace();
		}
	}
	*/
	
	private int insertAnswer(int question, String site, String user, Date date, String value, double conf) throws FspException{
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		int id=-1;	
		
		try {
		      statement = dl.connect().prepareStatement(
		    		  "INSERT INTO cicrowdinput (questionid,site,user,date,value,confidence) values (?, ?, ?, '"+df.format(date)+"', ?, ?)",
		    		  Statement.RETURN_GENERATED_KEYS);
		      statement.setInt(1, question);
		      statement.setString(2, "CrowdSim");
		      statement.setString(3,user);
		      statement.setString(4,value);
		      statement.setDouble(5,conf);
		      statement.executeUpdate();
		      ResultSet results=statement.getGeneratedKeys();
		      if(results.next()){
		    	  id=results.getInt(1);
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to Insert CrowdSim Answer:"+e);
	    } finally {
	    	try{
		    	if (resultSet != null) {resultSet.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }	
		return id;
	}
	
	private void run() throws FspException{
		int u=rand.nextInt(1000);
		String user="Bot_"+u;
		int c=rand.nextInt(10);
		List<Question> list=qm.getQuestions();
		while(c>0){
			c--;
			int i=rand.nextInt(list.size());
			Question q= list.get(i);
			System.out.println("Reading:\n"+q.getQuestionHTML());
			System.out.println("Answering:\n");
			// TODO finish this
		}
	
	}
	
	private FspFeature getAnswers(int questionId) throws FspException{
		
		FspFeature f=null;
		//TODO GET FEATURE TYPE by tracing back to fmap
		
		Statement statement = null;
		ResultSet results = null;
		HashMap<String,FspFeature> users=new HashMap<String,FspFeature>();
		String query="SELECT value, user, conf, date FROM cicrowdinput "
				+"WHERE questionid="+questionId+" ORDER BY date DESC;";
		try {
		      statement = dl.connect().createStatement();
		      results=statement.executeQuery(query);
		      while(results.next()){
		    	  String value=results.getString("value");
		    	  String user=results.getString("user");
		    	  double conf=results.getDouble("conf");
		    	  if(!users.containsKey(user)){
		    		users.put(user, null); //TODO finish this
		    		//list.add( create new feature with data...))
		    	  }
		      }
	    } catch (Exception e) {
	    	throw new FspException(this.getClass().getName()+" Failed to load CI crowd question inputs:"+e);
	    } finally {
	    	try{
		    	if (results != null) {results.close();}
		    	if (statement != null) {statement.close();}
	    	} catch(Exception e){}
	    }
		return f;
	}
}
