package weka.attributeSelection;

import java.io.File;

import weka.core.Instances;
import weka.filters.supervised.attribute.njj;

public class njjEva
extends ASEvaluation
 {
	public void buildEvaluator(Instances data){
		
	}
	public static void main (String[] args) {
		File file = new File("/home/njj/workspace/wekaclustering4/dataset1");
		File[] lf = file.listFiles();
//		Instances njjData = getInputFormat();
//		for(int i=0; i<10; i++){
			for(int i=0; i<lf.length; i++){
		
			if(lf[i].getName().indexOf(".arff~")!=-1||lf[i].getName().indexOf(".arff~~")!=-1||lf[i].getName().indexOf(".arff~~~")!=-1)
				continue;
			else{ 
//				AppendToFile.appendMethodA("detasetName", lf[i].getName()+"\n");
//				for(int c=11;c<13;c++){
//					for(int k=2;k<=9;k++){
						String[] arg ={
								"-i","dataset1/"+lf[i].getName(), 
								"-c","last",
								"-s","weka.attributeSelection.BestFirst -D 1 -N 5",

								

//								"-p", String.valueOf(c),//"-k",String.valueOf(k),

								
								
								//
								"-o", "newdataset1/"+"new_"+lf[i].getName()};
//								"-i","letter.arff", "-o", "result/letter/letter_"+c+"_"+k+".arff"};
						runEvaluator(new CfsSubsetEval(), arg);
//						runEvaluator(new njj(), args);
//						runFilter(new njj(), arg);
//						}
//					}
				}
			}
		}
//	    runEvaluator(new CfsSubsetEval(), args);
//	  }
}
