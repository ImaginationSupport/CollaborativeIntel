//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci.questions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

public class FakeCrowd {
	public static Random rand=null;
	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

	
	public FakeCrowd() {
		// TODO Auto-generated constructor stub
	}

	public static void main(String[] args) {
		FakeCrowd.rand=new Random();
		
		for (int i=0;i<50;i++){
			out(6,((rand.nextGaussian()*200)+400));
		}
	
		for (int i=0;i<50;i++){
			out(7,((rand.nextGaussian()*150)+550));
		}
		
		for (int i=0;i<50;i++){
			out(8,((rand.nextGaussian()*250)+850));
		}

		
	}
	private static void out (int question,double value){
		int year = 2000;
		int dayOfYear =rand.nextInt(100)+1;
		Calendar calendar = Calendar.getInstance();
		calendar.set(Calendar.YEAR, year);
		calendar.set(Calendar.DAY_OF_YEAR, dayOfYear);
		Date d = calendar.getTime();
		int user=rand.nextInt(1000);
		double conf=rand.nextDouble();
		System.out.println("INSERT INTO cicrowdinput (questionid,site,user,date,value,confidence) VALUES ("+question+",'ARAgen',"+user+",'"+df.format(d)+"','"+(int)Math.floor(value)+"',"+conf+");");
		
	}

}
