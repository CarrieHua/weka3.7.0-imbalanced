package weka.me;

import java.io.File;
import java.util.Random;

import weka.AppendToFile;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.RBFNetwork;
import weka.classifiers.lazy.IB1;
import weka.classifiers.meta.LogitBoost;
import weka.classifiers.rules.JRip;
import weka.classifiers.rules.OneR;
import weka.classifiers.rules.PART;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.supervised.attribute.njj7;
import weka.filters.*;

public class exp1 {
//	public void MGFwork(String dir,String dataset){
//		String distDirName = "MGFwork\\";
//		File distDir = new File(distDirName);
//		
//		if (!distDir.exists())
//			distDir.mkdir();
//		else{
//			System.out.println("directory exsits!!");
//		}
//		if (!distDir.isDirectory()){
//			System.out.println("wrong directory!!");
//			return;
//		}
//		String evaDist = distDirName+dataset+"_eva.csv";
//		String preDist = distDirName+dataset+"_pred.csv";
//	    File dist = new File(evaDist);
//	    if (dist.exists())
//	    	dist.delete();
//	    dist = new File(preDist);
//	    if (dist.exists())
//	    	dist.delete();
//		CVSEAttrIterSelect mgf = new CVSEAttrIterSelect();
//		mgf.m_distFile = evaDist;
////		mgf.setDebug(true);
//		mgf.setClassifier(new NaiveBayes());
////		mgf.
//		for (int i = 1; i < 11; i++){
//			String sourceTrain = dir+dataset+"_train"+i+".arff";
//			String sourceTest = dir +dataset+"_test"+i+".arff";
//			System.out.println(sourceTrain+","+sourceTest);
//			try {
//				DataSource tr_source = new DataSource(sourceTrain);
//				Instances trainData  = tr_source.getDataSet();
//				trainData.setClassIndex(trainData.numAttributes()-1);
//				
//				Evaluation eva = new Evaluation(trainData);
//				eva.setPriors(trainData);
//				
//				Classifier copiedClassifier = Classifier.makeCopy(mgf);
//			    copiedClassifier.buildClassifier(trainData);
//			    
//			    DataSource te_source = new DataSource(sourceTest);
//				Instances testData  = te_source.getDataSet();
//				testData.setClassIndex(testData.numAttributes()-1);
////				System.out.println(copiedClassifier.toString());
//				
//				eva.evaluateModel(copiedClassifier, testData);
//				String content ="";
//				content = Utils.doubleToString(eva.truePositiveRate(0), 6, 4)+","
//                       + Utils.doubleToString(eva.falsePositiveRate(0), 6, 4)+","
//                       + Utils.doubleToString(eva.balance(0), 6, 4)+","
//                       + Utils.doubleToString(eva.areaUnderROC(0), 6, 4)+"\n";
//			    AppendToFile.appendMethodA(preDist, content);
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			
//		}
//		
//		
//	}
//	
//	public void OurFramework(String dir, String dataset, int ith){
//		// the i-th simulation of the dataset 
//		String distDirName = "OurFramework\\";
//		File distDir = new File(distDirName);
//		
//		if (!distDir.exists())
//			distDir.mkdir();
//		else{
//			System.out.println("directory exsits!!");
//		}
//		if (!distDir.isDirectory()){
//			System.out.println("wrong directory!!");
//			return;
//		}
//		String evaDist = distDirName+dataset+"_"+ith+"_eva.csv";
//		String preDist = distDirName+dataset+"_pred.csv";
////	    File dist = new File(evaDist);
////	    if (dist.exists())
////	    	dist.delete();
////	    dist = new File(preDist);
////	    if (dist.exists())
////	    	dist.delete();
//		CVInfoAttrSelect our = new CVInfoAttrSelect();
//		our.setClassifier(new NaiveBayes());
//		String sourceTrain = dir+dataset+"_train"+ith+".arff";
//		String sourceTest = dir +dataset+"_test"+ith+".arff";
//		//evaluation
//		System.out.println(sourceTrain);
//		evaluation (our,sourceTrain,evaDist,10,10);
//
//		//prediction		
//		try {
//			DataSource tr_source = new DataSource(sourceTrain);
//			Instances trainData  = tr_source.getDataSet();
//			trainData.setClassIndex(trainData.numAttributes()-1);
//			
//			Evaluation eva = new Evaluation(trainData);
//			eva.setPriors(trainData);
//			
//			Classifier copiedClassifier = Classifier.makeCopy(our);
//		    copiedClassifier.buildClassifier(trainData);
//		    
//		    DataSource te_source = new DataSource(sourceTest);
//			Instances testData  = te_source.getDataSet();
//			testData.setClassIndex(testData.numAttributes()-1);
//			
//			eva.evaluateModel(copiedClassifier, testData);
//			String content ="";
//			content = Utils.doubleToString(eva.truePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.falsePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.balance(0), 6, 4)+","
//                   + Utils.doubleToString(eva.areaUnderROC(0), 6, 4)+"\n";
//		    AppendToFile.appendMethodA(preDist, content);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	    
//	}
//	
//	public void MGFPrediction(String dir, String dataset){
//		String distDirName = "MGFwork\\";
//		File distDir = new File(distDirName);
//		
//		if (!distDir.exists())
//			distDir.mkdir();
//		else{
//			System.out.println("directory exsits!!");
//		}
//		if (!distDir.isDirectory()){
//			System.out.println("wrong directory!!");
//			return;
//		}
//		String evaDist = distDirName+dataset+"_eva.csv";
//		String preDist = distDirName+dataset+"_pred.csv";
//	    File dist = new File(evaDist);
//	    if (dist.exists())
//	    	dist.delete();
//	    dist = new File(preDist);
//	    if (dist.exists())
//	    	dist.delete();
//		CVSEAttrIterSelect mgf = new CVSEAttrIterSelect();
//		mgf.m_distFile = evaDist;
////		mgf.setDebug(true);
//		mgf.setClassifier(new NaiveBayes());
//		try {
//			String sampleTrain = dir + dataset + "_train1.arff";
//			DataSource sample_source = new DataSource(sampleTrain);
//			Instances sample  = sample_source.getDataSet();
//			sample.setClassIndex(sample.numAttributes()-1);
//			Evaluation eva = new Evaluation(sample);
//			
//			for (int i = 1; i < 11; i++){
//				String sourceTrain = dir+dataset+"_train"+i+".arff";
//				String sourceTest = dir +dataset+"_test"+i+".arff";
//				System.out.println(sourceTrain+","+sourceTest);
//				
//				DataSource tr_source = new DataSource(sourceTrain);
//				Instances trainData  = tr_source.getDataSet();
//				trainData.setClassIndex(trainData.numAttributes()-1);
//				eva.setPriors(trainData);
//				
//				Classifier copiedClassifier = Classifier.makeCopy(mgf);
//			    copiedClassifier.buildClassifier(trainData);
//			    
//			    DataSource te_source = new DataSource(sourceTest);
//				Instances testData  = te_source.getDataSet();
//				testData.setClassIndex(testData.numAttributes()-1);
//				eva.evaluateModel(copiedClassifier, testData);
//			}
//			String content ="";
//			content = Utils.doubleToString(eva.truePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.falsePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.balance(0), 6, 4)+","
//                   + Utils.doubleToString(eva.areaUnderROC(0), 6, 4)+"\n";
//		    AppendToFile.appendMethodA(preDist, content);
//		    
//		}catch (Exception e){
//			e.printStackTrace();
//		}
//		
//		
//	}
//	public void OurPrediction(String dir, String dataset){
//		String distDirName = "OurFramework\\";
//		File distDir = new File(distDirName);
//		
//		if (!distDir.exists())
//			distDir.mkdir();
//		else{
//			System.out.println("directory exsits!!");
//		}
//		if (!distDir.isDirectory()){
//			System.out.println("wrong directory!!");
//			return;
//		}
//		String preDist = distDirName+dataset+"_pred.csv";
//		CVInfoAttrSelect our = new CVInfoAttrSelect();
//		our.setClassifier(new NaiveBayes());
//		
//       try {
//			String sampleTrain = dir + dataset + "_train1.arff";
//			DataSource sample_source = new DataSource(sampleTrain);
//			Instances sample  = sample_source.getDataSet();
//			sample.setClassIndex(sample.numAttributes()-1);
//			Evaluation eva = new Evaluation(sample);
//			
//			for (int ith = 1; ith < 11; ith++){
//				String sourceTrain = dir+dataset+"_train"+ith+".arff";
//				String sourceTest = dir +dataset+"_test"+ith+".arff";
//				System.out.println(sourceTrain);
//		
//				//prediction		
//				DataSource tr_source = new DataSource(sourceTrain);
//				Instances trainData  = tr_source.getDataSet();
//				trainData.setClassIndex(trainData.numAttributes()-1);
//				eva.setPriors(trainData);
//				
//				Classifier copiedClassifier = Classifier.makeCopy(our);
//				copiedClassifier.buildClassifier(trainData);
//		    
//				DataSource te_source = new DataSource(sourceTest);
//				Instances testData  = te_source.getDataSet();
//				testData.setClassIndex(testData.numAttributes()-1);
//			
//				eva.evaluateModel(copiedClassifier, testData);
//			}
//			String content ="";
//			content = Utils.doubleToString(eva.truePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.falsePositiveRate(0), 6, 4)+","
//                   + Utils.doubleToString(eva.balance(0), 6, 4)+","
//                   + Utils.doubleToString(eva.areaUnderROC(0), 6, 4)+"\n";
//		    AppendToFile.appendMethodA(preDist, content);
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
//	
	
	public void evaluation(Classifier c, String dataset, String only,
			String distFile, String acc, String rest, int numRep, int numFolds) {
		// 10_fold cross-validate

		Instances origdata = null;
		int m_seed = 0;

		try {
			// read from file
			DataSource source = new DataSource(dataset);
			origdata = source.getDataSet();

			// set the last attribute as the class label in default
			origdata.setClassIndex(origdata.numAttributes() - 1);
			String content = "";
			String content1 = "";
			String content2 = "";
			String on = "";
			double accave=0;
			for (int rep = 0; rep < numRep; rep++) {

				Instances data = new Instances(origdata);
				data.randomize(new Random(rep));

			
				if (data.classAttribute().isNominal()) {
					data.stratify(numFolds);
				}
				// Do the folds
				for (int i = 0; i < numFolds; i++) {
					System.out.println(rep + "," + i);

					Evaluation eva1 = new Evaluation(data);
					Instances train = data.trainCV(numFolds, i, new Random(i));
					eva1.setPriors(train);
					Classifier copiedClassifier = Classifier.makeCopy(c);
					copiedClassifier.buildClassifier(train);
					Instances test = data.testCV(numFolds, i);

					eva1.evaluateModel(copiedClassifier, test);
					
					accave+=eva1.pctCorrect();
			

					content += rep + "," + i + "," + data.numClasses() + "\r\n";

					for (int njj = 0; njj < data.numClasses(); njj++) {

						content += Utils.doubleToString(eva1
								.truePositiveRate(njj), 5, 4)
								+ ",";
						content += Utils.doubleToString(eva1
								.falsePositiveRate(njj), 5, 4)
								+ ",";
						content += Utils.doubleToString(eva1.precision(njj), 5,
								4)
								+ ",";
						content += Utils.doubleToString(eva1.recall(njj), 5, 4)
								+ ",";
						content += Utils.doubleToString(eva1.fMeasure(njj), 5,
								4)
								+ ",";
						content += Utils.doubleToString(eva1.areaUnderROC(njj),
								5, 4)
								+",";
						content += (data.classAttribute().value(njj)+"\r\n");

					}
					content += "\r\n";

					content1 += rep + "," + i + ","
							+ Utils.doubleToString(eva1.pctCorrect(), 5, 4)
							+ "\r\n";

					content2 += rep + "," + i + ",";
					content2 += Utils.doubleToString(eva1.kappa(), 5, 4) + ",";
					content2 += Utils.doubleToString(eva1.meanAbsoluteError(),
							5, 4)
							+ ",";
					content2 += Utils.doubleToString(eva1
							.rootMeanSquaredError(), 5, 4)
							+ ",";
					content2 += Utils.doubleToString(eva1
							.relativeAbsoluteError(), 5, 4)
							+ ",";
					content2 += Utils.doubleToString(eva1
							.rootRelativeSquaredError(), 5, 4)
							+ "\r\n";

				}

			}
			AppendToFile.appendMethodA(distFile, content);
			AppendToFile.appendMethodA(acc, content1);
			AppendToFile.appendMethodA(rest, content2);
			
			accave/= (numRep*numFolds);
			on += dataset + "," + origdata.numAttributes() + ","
					+ origdata.numClasses() + "," + origdata.numInstances() + ","
					+ Utils.doubleToString(accave, 5, 4) + "\r\n";
			AppendToFile.appendMethodA(only, on);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args){
//		File file = new File("dataset/");
		exp1 exp=new exp1();
		
		Classifier c;
		String dataset, only, TPR, acc, rest;
		int numRep,numFolds;		
			NaiveBayes nb = new NaiveBayes();
			J48 j48 = new J48();
			IB1 ib1= new IB1();
			OneR oner=new OneR();	
			JRip jrip = new JRip();
			PART part=new PART();
			RBFNetwork rbf=new RBFNetwork();
			BayesNet bn=new BayesNet();
			LogitBoost boost =new LogitBoost();
			
			
			
			Classifier cs[]=new Classifier[10];
			cs[0]=nb;
			cs[1]=j48;
			cs[2]=ib1;
			cs[3]=oner;	
			cs[4]=jrip;
			cs[5]=part;
			cs[6]=rbf;
			cs[7]=bn;
			cs[8]=boost;
			
			
			c=cs[Integer.parseInt(args[0])];
			dataset = "dataset/"+args[1]+"/"+args[2];
			only = "result/ACC/"+args[1]+args[0]+"/"+args[0]+".txt";
			TPR = "result/ACC/"+args[1]+args[0]+"/"+"TRP"+args[0]+"_"+args[1]+"_"+args[2]+".txt";
			acc = "result/ACC/"+args[1]+args[0]+"/"+"acc"+args[0]+"_"+args[1]+"_"+args[2]+".txt";
			rest = "result/ACC/"+args[1]+args[0]+"/"+"rest"+args[0]+"_"+args[1]+"_"+args[2]+".txt";
			numRep = 5;
			numFolds = 10;			
			exp.evaluation(c,dataset,only,TPR,acc,rest,numRep,numFolds);	
	}
}
