//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.features;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Random;

import com.ara.fsp.api.FspFeature;
import com.ara.fsp.api.FspFeatureId;

public class ProbabilisticVariable implements FspFeature  {
	public static DecimalFormat df=new DecimalFormat("0.000");
	private double value=0;
	private double conf=0.0;
	private FspFeatureId id=null;

	public ProbabilisticVariable(){
	}
	
	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
		fix();
	}

	@Override
	public String getLabel() {
		return "Probalistic Variable";
	}
	
	@Override
	public String getDesc() {
		return "A probablistic variable that can be set as a double precision number between 0 and 1.";
	}

	private void fix() {
		if (value>1.0) value=1.0;
		if (value<0.0) value=0.0;
	}

	@Override
	public String toDisplayString() {
		return df.format(value);
	}

	@Override
	public String toString() {
		return ""+value;
	}
	
	@Override
	public void fromString(String data) {
		value=Double.parseDouble(data);
		fix();
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
		return "will #ENTITY# #FEATUREMAP#";
	}
	
	@Override
	public String getQuestionTemplate() {
		return "what is the probability that #ENTITY# #FEATUREMAP# on #DATE#?";
	}
	
	@Override
	public String getStatementTemplate() {
		return "there is a #VALUE# probability that #ENTITY# #FEATUREMAP# on #DATE#";
	}
	
	@Override
	public FspFeature random() {
		ProbabilisticVariable f=new ProbabilisticVariable();
		f.setValue(new Random().nextDouble());
		f.setConfidence(-1.0);
		return f;
	}

	@Override
	public FspFeature aggregate(List<FspFeature> values) {
		int c=0;
		double t=0;
		for (FspFeature f: values){
			if(f instanceof ProbabilisticVariable){
				double w=Math.floor(100.0*f.getConfidence());
				c+=w;
				t+=((ProbabilisticVariable)f).getValue()*w;	
			}
		}
		ProbabilisticVariable agg=new ProbabilisticVariable();
		double avg=(t/c);
		agg.setValue(avg);
		agg.setConfidence(Math.round(c/100)/values.size()); //TODO work out math for confidence
		return agg;
	}

	@Override
	public double getNumeric() {
		return value;
	}
}
