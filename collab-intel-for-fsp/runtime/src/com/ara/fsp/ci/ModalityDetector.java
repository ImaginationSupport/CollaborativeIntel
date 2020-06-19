//	Copyright 2015 - Applied Research Associates, Inc. (All Rights Reserved)
//	WARNING: this is a proof-of-concept demonstrator and not tested or warranted for production use
//	For additional information please contact Chris Argenta - cargenta@ara.com

package com.ara.fsp.ci;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import com.ara.fsp.api.FspFeature;

public class ModalityDetector {
	private static DecimalFormat df = new DecimalFormat( "###0.00" );
	
	//KEEP THIS
	public class Modality {
		public Modality (double mean, int amplitude, double width){
			this.mean=mean;
			this.amplitude=amplitude;
			this.width=width;
		}
		double mean;
		int amplitude;
		double width;
		Score score;
	}
	
	public class Count{
		public Count(double bin, int count){
			this.bin=bin;
			this.count=count;
		}
		public double bin;
		public int count;
	}
	
	public class Score{
		public int over=0;
		public int under=0;
		public int right=0;
		public double fit=0.0;
	}
	
	public class HistoCompare implements Comparator<Count>{
		@Override
		public int compare(Count c1, Count c2) {
			return c1.count - c2.count;
		}

	}

	private int bins=100;
	private int[] counts=new int[bins];
	private int samples=0;
	private Double min=null;
	private Double max=null;
	private Double mean=null;
	private Double prec=null;
	private int modes=0;
	private List<Count> cand=null;
	private boolean bimodal=false;
	
	public static void main(String[] args) {
		Random rand=new Random();
		List<Double> test=new ArrayList<Double>(300);
		
		/*
		for (int i=0;i<1000;i++){
			test.add((rand.nextGaussian()*100)+500);
		}
		
		
		for (int i=0;i<1000;i++){
			test.add((rand.nextGaussian()*50)+1000);
		}
		
		for (int i=0;i<1000;i++){
			test.add((rand.nextGaussian()*25)+100);
		}
		
		*/
		for (int i=0;i<1000;i++){
			test.add((rand.nextDouble()*500)+500);
		}
		
		for (int i=0;i<args.length;i++){
			test.add(Double.parseDouble(args[i]));
		}
		
		ModalityDetector md=new ModalityDetector(test,true);
		
		System.out.println("Bin\tCount");
		for(Count c:md.getHistogram()){
			System.out.println(df.format(c.bin)+"\t"+c.count);
		}
		
		List<Modality> ms=md.getModes(.2);
		
		
	}
	

	public ModalityDetector(List<Double> values, boolean test) {
		mean=0.0;
		for (Double v: values){
			if(min==null || min>v) min=v;
			if(max==null || max<v) max=v;
			mean+=v;
		}
		samples=values.size();
		mean=mean/samples;
		if (max==null || min==null) throw new RuntimeException("No input values given");
		prec=(max-min)/bins;
		for (Double v: values){
			counts[value2pos(v)]++;
		}
	}
	

	public ModalityDetector(List<FspFeature> values) {
		mean=0.0;
		for (FspFeature f: values){
			double v=f.getNumeric();
			if(min==null || min>v) min=v;
			if(max==null || max<v) max=v;
			mean+=v;
		}
		samples=values.size();
		mean=mean/samples;
		if (max==null || min==null) throw new RuntimeException("No input values given");
		prec=(max-min)/bins;
		for (FspFeature f: values){
			counts[value2pos(f.getNumeric())]++;
		}
	}
	
	private Modality findBest(int start, int end){
		int bestMean=-1;
		int bestWidth=-1;
		Score bestScore=new Score();
		Modality bestM=null;
		int top=-1;
		int c=0;
		for (int x=0;x<bins;x++){
			if(counts[x]>top) top=counts[x];
			c+=counts[x];
		}
		if (prec==0.0) return new Modality(mean,top,1.0);
		
		//System.out.println("Finding Best for "+pos2value(start)+"->"+pos2value(end)+" with n="+c+" and mean="+getMean(start,end));

		//System.out.println("\tmean\twidth\tright\tover\tunder\tscore");
		for (int i=start; i<end; i++){
			for (int w=10; w<100; w+=10){
				List<Modality> ms =new ArrayList<Modality>();
				Modality m=new Modality(pos2value(i),top, ((double)w)*prec);
				ms.add(m);
				int[] pat=generate(ms);
				Score s=score(counts,pat);
				s.fit=((double)s.right/((double)top*(w/2.0)));
				//System.out.println("\t"+i+"\t"+w+"\t"+s.right+"\t"+s.over+"\t"+s.under+"\t"+df.format(s.fit));
				if (s.fit>bestScore.fit){
					bestScore=s;
					bestMean=i;
					bestWidth=w;
					bestM=m;
				}
			}
		}
		if (bestM==null) return new Modality(mean,top,1.0);
		//System.out.println("\n\nBest Score:"+bestScore.fit+"\tMean:"+bestMean+"\tWidth="+bestWidth);

		bestM.score=bestScore;
		return bestM;
	}
	

	private int value2pos(double value){
		int pos=(int)Math.floor((value-min)/prec);
		if(pos==bins)pos=pos-1;
		if(pos<0) pos=0;
		if(pos>=bins) pos=bins-1;
		return pos;
	}
	
	private double pos2value (int pos){
		return min+(pos*prec);
	}
	
	public List<Count> getHistogram(){
		List<Count> hist=new ArrayList<Count>(bins);
		for (int i=0;i<bins;i++){
			hist.add(new Count((i*prec)+min, counts[i]));
		}
		return hist;
	}
	
	public List<Modality> get2Modes(){
		cand=getHistogram();
		Collections.sort(cand,new HistoCompare());
		List<Modality> out= new ArrayList<Modality>();		
		Modality m1=findBest(0,bins);

		int mc1=clearMode(m1);
		Modality m2=findBest(0,bins);
		int mc2=clearMode(m2);
		System.out.println("Modality 1 is mean="+m1.mean+", width="+m1.width+", amplitude="+m1.amplitude+" (fit="+df.format(m1.score.fit)+") and included "+mc1+" samples ("+df.format((double)mc1*100/samples)+"%)");
		System.out.println("Modality 2 is mean="+m2.mean+", width="+m2.width+", amplitude="+m2.amplitude+" (fit="+df.format(m2.score.fit)+") and included "+mc2+" samples ("+df.format((double)mc2*100/samples)+"%)");
		double diff=((double)(mc1*m1.score.fit)/samples)-((double)(mc2*m2.score.fit)/samples);
		if (Math.abs(diff)<.25){
			System.out.println("Bimodal ("+diff+")");
			out.add(m1);
			out.add(m2);
			bimodal=true;
		} else {
			if(mc1>mc2){
				System.out.println("UniModal around 1 Accepted ("+diff+")");
				out.add(m1);
				bimodal=false;
			} else {
				System.out.println("UniModal around 2 Accepted ("+diff+")");
				out.add(m2);
				bimodal=false;
			}
			
			
		}
		return out;
	}
	
	public List<Modality> getModes(double threshold){
		cand=getHistogram();
		Collections.sort(cand,new HistoCompare());
		List<Modality> out= new ArrayList<Modality>();	
		boolean finding=true;
		int left=samples;
		while (((double)left/samples)>threshold){
			Modality m=findBest(0,bins);
			if (m==null) break;
			int mc=countMode(m);
			left-=mc;
			double mean=getModalityMean(m);
			double ratio=(double)mc/samples;
			double s=0.0;
			if(m.score!=null) s=m.score.fit;
			System.out.print("Modality "+(out.size()+1)+" is mean="+df.format(m.mean)+" ("+df.format(mean)+"), width="+df.format(m.width)+", amplitude="+m.amplitude+" (fit="+df.format(s)+") and included "+mc+" samples ("+df.format(ratio*100)+"%)");
			if (ratio>=threshold){
				System.out.println("\tAccepted");
				out.add(m);
				clearMode(m);
			} else{
				System.out.println("\tRejected");
				clearMode(m);
				if (mc==0) break;
			}
				
		}
		return out;
	}
	
	private int clearMode(Modality m){
		int c=0;
		double halfWidth=(m.width/2);
		int mean=value2pos(m.mean);
		int start=value2pos(m.mean-halfWidth);
		int end=value2pos(m.mean+halfWidth);
		
		for(int i=start;i<=end;i++){
			c+=counts[i];
			counts[i]=0;
		}
		return c;
	}
	
	private int countMode(Modality m){
		int c=0;
		double halfWidth=(m.width/2);
		int mean=value2pos(m.mean);
		int start=value2pos(m.mean-halfWidth);
		int end=value2pos(m.mean+halfWidth);
		
		for(int i=start;i<=end;i++){
			c+=counts[i];
		}
		return c;
	}
	
	private double getModalityMean(Modality m){
		double halfWidth=(m.width/2);
		int mean=value2pos(m.mean);
		int start=value2pos(m.mean-halfWidth);
		int end=value2pos(m.mean+halfWidth);
		return getMean(start,end);
	}
		
	private double getMean(int start, int end){
		int count=0;
		double total=0.0;
		for(int i=start;i<end;i++){
			total+=counts[i]*pos2value(i);
			count+=counts[i];
		}
		return total/count;
	}
	
	private Score score(int[] orig, int[] estimate){
		Score s=new Score();
		for (int i=0;i<orig.length;i++){
			if(orig[i]<estimate[i]){
				s.over+=estimate[i]-orig[i];
				s.right+=orig[i];
			}
			if(orig[i]>=estimate[i]){
				s.under+=orig[i]-estimate[i];
				s.right+=estimate[i];
			}
		}
		return s;
	}
	
	private int[] generate(List<Modality> est){
		//TODO change kernel from triangles to gaussians or similar
		int[] out=new int[bins];
		for(Modality m:est){
			double halfWidth=(m.width/2);
			int mean=value2pos(m.mean);
			int start=value2pos(m.mean-halfWidth);
			int end=value2pos(m.mean+halfWidth);
			for (int i=start; i<end; i++){
				int c=0;;
				if(i<mean){
					c=(int)Math.floor(((i-start)/(mean-(halfWidth/prec)))*m.amplitude);
				}
				if(i>mean){
					c=(int)Math.floor((1-((i-mean)/(mean+(halfWidth/prec))))*m.amplitude);
				}
				if(i==mean){
					c=m.amplitude;
				}
				if(c>out[i]) out[i]=c;
			}
		}
		return out;
	}


}
