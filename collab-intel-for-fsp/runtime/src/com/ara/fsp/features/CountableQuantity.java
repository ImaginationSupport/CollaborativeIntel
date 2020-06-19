//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.features;

import java.util.List;
import java.util.Random;

import com.ara.fsp.api.FspFeature;
import com.ara.fsp.api.FspFeatureId;

public class CountableQuantity implements FspFeature {
	private int value=0;
	private double conf=0.0;
	private FspFeatureId id=null;


	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}

	@Override
	public String getLabel() {
		return "Countable Quantity";
	}

	@Override
	public String getDesc() {
		return "A countable quantity, which can be aggregated by averaging.";
	}

	@Override
	public String toDisplayString() {
		return ""+value;
	}

	@Override
	public String toString() {
		return ""+value;
	}
	
	@Override
	public void fromString(String data) {
		double temp=Double.parseDouble(data);
		value=(int)Math.round(temp);
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
		return "the #FEATUREMAP# for #ENTITY#";
	}
	
	@Override
	public String getQuestionTemplate() {
		return "what will the #FEATUREMAP# #UNITS# for #ENTITY# be on #DATE#?";
	}
	
	@Override
	public String getStatementTemplate() {
		return "the #FEATUREMAP# for #ENTITY# will be #VALUE# on #DATE#.";

	}

	@Override
	public FspFeature random() {
		CountableQuantity f=new CountableQuantity();
		f.setValue(new Random().nextInt(1000));
		f.setConfidence(-1.0);
		return f;
	}

	@Override
	public FspFeature aggregate(List<FspFeature> values) {
		int c=0;
		int t=0;
		for (FspFeature f: values){
			if(f instanceof CountableQuantity){
				int w=(int)Math.floor(100.0*f.getConfidence());
				c+=w;
				t+=((CountableQuantity)f).getValue()*w;	
			}
		}
		CountableQuantity agg=new CountableQuantity();
		int avg=Math.round(t/values.size());
		agg.setValue(avg);
		agg.setConfidence(Math.round(c/100)/values.size()); //TODO work out math for confidence
		return agg;
	}

	@Override
	public double getNumeric() {
		return (double)value;
	}
	
}
