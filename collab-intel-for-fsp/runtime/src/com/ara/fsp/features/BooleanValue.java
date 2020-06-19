//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.features;

import java.util.List;
import java.util.Random;

import com.ara.fsp.api.*;

public class BooleanValue implements FspFeature{
	private boolean value=false;
	private double conf=0.0;
	private FspFeatureId id=null;
	
	protected boolean getValue() {
		return value;
	}

	protected void setValue(boolean value) {
		this.value = value;
	}

	@Override
	public String getLabel() {
		return "Boolean Value";
	}

	@Override
	public String getDesc() {
		return "A boolean value that can be set to TRUE or FALSE, which can be aggrevated using a logical OR operator.";
	}
	
	@Override
	public String toDisplayString() {
		if(value) {
			return "True";
		} else {
			return "False";
		}
	}

	@Override
	public String toString() {
		if(value) return "1";
		return "0";
	}

	@Override
	public void fromString(String data) {
		if ((data.equalsIgnoreCase("TRUE")) || (data.equals("1"))) value = true;
		else value=false;
	}	

	@Override
	public double getConfidence() {
		return conf;
	}
	
	@Override
	public void setConfidence(double conf) {
		this.conf=conf;
	}

	@Override
	public FspFeatureId getId() {
		return id;
	}

	@Override
	public void setId(FspFeatureId id) {
		this.id=id;
	}

	@Override
	public String getLabelTemplate() {
		return "#ENTITY# #FEATUREMAP#";
	}
	
	@Override
	public String getQuestionTemplate() {
		return "will #ENTITY# be #FEATUREMAP# in #DATE#?";
	}
	
	@Override
	public String getStatementTemplate() {
		if(value) return "#ENTITY# will be #FEATUREMAP# in #DATE#";
		return  "#ENTITY# will not be #FEATUREMAP# in #DATE#";
	}
	
	@Override
	public FspFeature random() {
		BooleanValue f=new BooleanValue();
		f.setValue(new Random().nextBoolean());
		f.setConfidence(-1.0);
		return f;
	}

	@Override
	public FspFeature aggregate(List<FspFeature> values) {
		int c=0;
		double t=0;
		for (FspFeature f: values){
			if(f instanceof BooleanValue){
				c++;
				if(((BooleanValue)f).getValue()){
					t+=f.getConfidence();
				}else{
					t-=f.getConfidence();
				}		
			}
		}
		BooleanValue agg=new BooleanValue();
		double r=t/c;
		if (t>=0){
			agg.setValue(true);
			agg.setConfidence(r);
		}else{
			agg.setValue(false);
			agg.setConfidence(-r);
		}
		return agg;
	}

	@Override
	public double getNumeric() {
		if (value) return 1.0;
		return 0.0;
	}
	
}
