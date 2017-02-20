package szb;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import com.csvreader.CsvReader;
import jxl.*;
import jxl.write.*;
import jxl.write.Number;

import weka.attributeSelection.ASSearch;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.GreedyStepwise;
import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.lazy.IBk;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.AttributeSelectedClassifier;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.MetaCost;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.meta.MultiFilteredClassifier;
import weka.classifiers.meta.MyMultiClassClassifier1;
import weka.classifiers.rules.JRip;
import weka.classifiers.szb.RUSBoostM1;
import weka.classifiers.szb.SMOTEBoostM1;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.szb.BalanceClass;
import weka.filters.szb.MultiCluster;
import weka.filters.szb.MySMOTE;
import weka.filters.szb.MyOverSample;
import weka.filters.szb.MyUnderSample;

public class MxNResults {

	private Instances m_Data; // 数据集
	private int m_folds; // 交叉验证的折数
	private int m_repeat; // 交叉验证的次数

	public MxNResults() {
		m_Data = null;
		m_folds = 10;
		m_repeat = 10;
	}

	/**
	 * 从文件中读取数据集，并将数据集存入到m_Data中
	 */
	public void generateData(String filename) {
		try {
			DataSource ds = new DataSource(filename);
			m_Data = ds.getDataSet();
			m_Data.setClassIndex(m_Data.numAttributes() - 1);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * M*N 折分层交叉验证
	 */
	public String crossValidate(Classifier c) throws Exception {

		double auc = 0;
		String result = "";

		for (int i = 0; i < m_repeat; i++) {
			Random rd = new Random(i);
			m_Data.randomize(rd);
			if (m_Data.classAttribute().isNominal()) {
				m_Data.stratify(m_folds);
			}
			Evaluation eval = new Evaluation(m_Data);

			for (int j = 0; j < m_folds; j++) {
				// 训练
				Instances train = m_Data.trainCV(m_folds, j, rd);
				Classifier copyC = Classifier.makeCopy(c);
				eval.setPriors(train);
				copyC.buildClassifier(train);
				// 测试
				Instances test = m_Data.testCV(m_folds, j);
				eval.evaluateModel(copyC, test);
				
				auc = eval.areaUnderROC(0);
				result +=Utils.doubleToString(auc, 4) + ",";
			}
		}
		return result;
	}

	/**
	 * 对普通分类器使用MetaCost算法之后再交叉验证
	 */
	public String metacostValidate(Classifier c) throws Exception {

		double auc = 0;
		String result = "";

		for (int i = 0; i < m_repeat; i++) {
			Random rd = new Random(i);
			m_Data.randomize(rd);
			if (m_Data.classAttribute().isNominal()) {
				m_Data.stratify(m_folds);
			}
			Evaluation eval = new Evaluation(m_Data);

			for (int j = 0; j < m_folds; j++) {
				// 训练
				Instances train = m_Data.trainCV(m_folds, j, rd);
				Classifier copyC = Classifier.makeCopy(c);
				eval.setPriors(train);

				CostMatrix matrix = new CostMatrix(2);
				double ratio = checkRatio(train);
				matrix.setElement(0, 0, 0);
				matrix.setElement(0, 1, 1 / (ratio + 1));
				matrix.setElement(1, 0, ratio / (ratio + 1));
				matrix.setElement(1, 1, 0);

				MetaCost meta = new MetaCost();
				meta.setClassifier(copyC);
				meta.setCostMatrix(matrix);
				meta.buildClassifier(train);

				// 测试
				Instances test = m_Data.testCV(m_folds, j);
				eval.evaluateModel(copyC, test);
				
				auc = eval.areaUnderROC(0);
				result +=Utils.doubleToString(auc, 4) + ",";
			}
		}
		return result;
	}

	/**
	 * 将实验结果content输出到文件filename中
	 */
	public static void outputResults(String filename, String content) {
		try {
			FileWriter writer = new FileWriter(filename, true);
			writer.write(content);
			writer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 统计数据集中多数类实例和少数类实例的比值
	 */
	public double checkRatio(Instances inst) {
		int major = 0, minor = 0;
		for (int i = 0; i < inst.numInstances(); i++) {
			Instance ins = inst.instance(i);
			if (inst.classAttribute().isNominal()) {
				if (ins.classValue() == 0)
					minor++;
				else
					major++;
			} else if (inst.classAttribute().isNumeric()) {
				if (ins.classValue() > 0)
					minor++;
				else
					major++;
			}
		}

		return (double) major / minor;
	}

	/**
	 *统计数据集中多数类，少数类，实例总数和属性个数
	 */
	public static void countDataProperty() {

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 * 
		 * String sourceDir = "F:\\DataSets\\UCIData\\MultiClass\\BinaryData\\";
		 * String destDir = "F:\\DataSets\\UCIData\\MultiClass\\BinaryData\\";
		 */
		String datasets[] = { "ecoli-0_vs_1", "ecoli1", "ecoli2", "ecoli3",
				 "glass-0-1-2-3_vs_4-5-6", "glass0", "glass1", "glass6",
				 "haberman", "iris0", "new-thyroid1", "newthyroid2",
				 "page-blocks0", "pima", "segment0", "vehicle0", "vehicle1",
				 "vehicle2", "vehicle3", "wisconsin", "yeast1", "yeast3",
				 "abalone19", "abalone9-18",
				 "ecoli-0-1-3-7_vs_2-6", "ecoli4", "glass-0-1-6_vs_2",
				 "glass-0-1-6_vs_5", "glass2", "glass4", "glass5",
				 "page-blocks-1-3_vs_4", "shuttle-c0-vs-c4", "shuttle-c2-vs-c4",
				 "vowel0", "yeast-0-5-6-7-9_vs_4", "yeast-1-2-8-9_vs_7",
				 "yeast-1-4-5-8_vs_7", "yeast-1_vs_7", "yeast-2_vs_4",
				 "yeast-2_vs_8", "yeast4", "yeast5", "yeast6",
				 "cleveland-0_vs_4", "ecoli-0-1-4-6_vs_5",
					"ecoli-0-1-4-7_vs_2-3-5-6", "ecoli-0-1-4-7_vs_5-6",
					"ecoli-0-1_vs_2-3-5", "ecoli-0-1_vs_5", "ecoli-0-2-3-4_vs_5",
					"ecoli-0-2-6-7_vs_3-5", "ecoli-0-3-4-6_vs_5",
					"ecoli-0-3-4-7_vs_5-6", "ecoli-0-3-4_vs_5", "ecoli-0-4-6_vs_5",
					"ecoli-0-6-7_vs_3-5", "ecoli-0-6-7_vs_5", "glass-0-1-4-6_vs_2",
					"glass-0-1-5_vs_2", "glass-0-4_vs_5", "glass-0-6_vs_5",
					"led7digit-0-2-4-5-6-7-8-9_vs_1", "yeast-0-2-5-6_vs_3-7-8-9",
					"yeast-0-2-5-7-9_vs_3-6-8", "yeast-0-3-5-9_vs_7-8"  };
		String sourceDir = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelArffData\\";
		String destDir = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelArffData\\";

		String destName = destDir + "DataProperty.csv";
		String content = "Data" + "," + "Minority" + "," + "Majority" + ","
				+ "Sum" + "," + "Attributes" +","+"Ratio"+ "\n";
		outputResults(destName, content);
		for (int i = 0; i < datasets.length; i++) {
			String source = sourceDir + datasets[i] + ".arff";
			try {
				DataSource ds = new DataSource(source);
				Instances data = ds.getDataSet();
				data.setClassIndex(data.numAttributes() - 1);
				Instances data1 = new Instances(data, 0);
				Instances data2 = new Instances(data, 0);
				for (int j = 0; j < data.numInstances(); j++) {
					Instance ins = data.instance(j);
					if (ins.classValue() == 0) {
						data1.add(ins);
					} else {
						data2.add(ins);
					}
				}

				double min = data1.numInstances();
				double max = data2.numInstances();
				double sum = data.numInstances();
				double attr = data.numAttributes();
				double ratio=max/min;
				content = datasets[i] + "," + min + "," + max + "," + sum + ","
						+ attr+","+ratio + "\n";
				outputResults(destName, content);

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * 评估基本分类器的性能
	 */
	public static void evalBench() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		// String datasets[] = { "breast-cancer", "breast-w", "credit-g",
		// "haberman", "hepatitis", "ionosphere", "ozone1hr", "ozone8hr",
		// "phoneme", "pima", "sick", "sick-euthyroid", "tic-tac-toe",
		// "titanic", "abalone9-18", "abalone19", "carG", "ecoliCP-IM",
		// "ecoliIM", "ecoliIMU", "ecoliOM", "glassBWFP", "glassBWNFP",
		// "glassContainers", "glassNW", "glassTableware", "glassVWFP",
		// "new-thyroid", "optdigits0", "satimage2", "satimage5",
		// "segment1", "spliceEI", "spliceIE", "vehicleVAN", "vowelZ",
		// "yeastCYT-POX", "yeastEXC", "yeastME1", "yeastME2"
		// };

		// String datasets[]={
		// "adult","breast-cancer","credit-g","haberman","hepatitis",
		// "ozone1hr","ozone8hr","sick","sick-euthyroid",
		// "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		// "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		// "ecoli4","ecoli6","glass1","glass5","lymph3",
		// "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		// "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		// "satimage1","satimage4","segment1","segment4","segment7",
		// "soybean1","soybean4","soybean17","splice1","splice2",
		// "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		// "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6"
		// };

		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};

		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/Orig/";
		//String sourceDir="F:\\DataSets\\KeelDataSets\\KeelData\\KeelArffData\\";
		//String destDir="F:\\DataSets\\KeelDataSets\\KeelData\\KeelDataResults\\100AucResults\\Original\\";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];

		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < 7; i++) {
			if (models[i] == null)
				break;
			String destName = destDir + modelNames[i] + ".csv";
			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					content += datasets[j] + "," + mxn.crossValidate(models[i])+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destName, content);
			}
		}
	}

	/**
	 *评估random oversample 分类结果
	 */
	public static void evalOverSample() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		// String datasets[]={
		// "adult","breast-cancer","credit-g","haberman","hepatitis",
		// "ozone1hr","ozone8hr","sick","sick-euthyroid",
		// "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		// "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		// "ecoli4","ecoli6","glass1","glass5","lymph3",
		// "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		// "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		// "satimage1","satimage4","segment1","segment4","segment7",
		// "soybean1","soybean4","soybean17","splice1","splice2",
		// "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		// "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6"
		// };
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };

		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/OverSample/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					FilteredClassifier fc = new FilteredClassifier();
					fc.setClassifier(models[i]);
					MyOverSample os = new MyOverSample();
					fc.setFilter(os);
					content += datasets[j] + "," + mxn.crossValidate(fc)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}
	}

	/**
	 * 评估random undersample分类结果
	 */
	public static void evalUnderSample() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/UnderSample/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					FilteredClassifier fc = new FilteredClassifier();
					fc.setClassifier(models[i]);
					MyUnderSample us = new MyUnderSample();
					fc.setFilter(us);
					content += datasets[j] + "," + mxn.crossValidate(fc)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}
	}

	/**
	 * 评估SMOTE分类结果
	 */
	public static void evalSMOTE() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		// String
		// datasets[]={"CM1","JM1","KC1","KC2","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/SMOTE/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					FilteredClassifier fc = new FilteredClassifier();
					fc.setClassifier(models[i]);
					MySMOTE smote = new MySMOTE();
					fc.setFilter(smote);
					content += datasets[j] + "," + mxn.crossValidate(fc)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}

	}

	/**
	 * 评估MetaCost分类结果
	 */
	public static void evalMetaCost() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		// String
		// datasets[]={"CM1","JM1","KC1","KC2","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/MetaCost/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";
			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					content += datasets[j] + ","
							+ mxn.metacostValidate(models[i])+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}
	}

	/**
	 * 评估Bagging分类结果
	 */
	public static void evalBagging() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		// String
		// datasets[]={"CM1","JM1","KC1","KC2","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/Bagging/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					Bagging bag = new Bagging();
					bag.setClassifier(models[i]);
					bag.setNumIterations(10);
					content += datasets[j] + "," + mxn.crossValidate(bag)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}
	}

	/**
	 * 评估Boosting分类结果
	 */
	public static void evalBoosting() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		// String
		// datasets[]={"CM1","JM1","KC1","KC2","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"};
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/Boosting/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					AdaBoostM1 boost = new AdaBoostM1();
					boost.setClassifier(models[i]);
					boost.setNumIterations(10);
					boost.setUseResampling(true);
					content += datasets[j] + "," + mxn.crossValidate(boost)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}

	}

	/**
	 * 评估不同编码方案的性能 将二类随机划分为多类问题，每个类中有相同数量的实例
	 */
	public static void evalMultiClassWithRandom() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		/*
		 * String datasets[]={
		 * "adult","breast-cancer","credit-g","haberman","hepatitis",
		 * "ozone1hr","ozone8hr","sick","sick-euthyroid",
		 * "arrhythmia2","arrhythmia4","arrhythmia8","arrhythmia15","car2",
		 * "car3","dermatology2","dermatology4","dermatology6","ecoli2",
		 * "ecoli4","ecoli6","glass1","glass5","lymph3",
		 * "lymph4","nursery3","nursery5","optdigits1","optdigits4",
		 * "optdigits9","page-blocks2","page-blocks4","pendigits1","pendigits4",
		 * "satimage1","satimage4","segment1","segment4","segment7",
		 * "soybean1","soybean4","soybean17","splice1","splice2",
		 * "vehicle1","vehicle4","vowel2","vowel4","vowel8",
		 * "yeast1","yeast3","yeast6","yeast9","zoo2","zoo6" };
		 */
		// String datasets[]={"ant1.3","ant1.4","ant1.5","ant1.6","ant1.7",
		// "camel1.0","camel1.2","camel1.4","camel1.6",
		// "ivy1.4","ivy2.0","jedit3.2.1","jedit4.0","jedit4.1",
		// "jedit4.2","jedit4.3","lucene2.0",
		// "poi2.0","synapse1.0",
		// "synapse1.1","synapse1.2","tomcat6.0",
		// "velocity1.6.1","xalan2.4","xalan2.6",
		// "xerces1.0","xerces1.2","xerces1.3"
		// };
		// String
		// datasets[]={"ivy1.1","lucene2.2","lucene2.4","poi1.5","poi2.5.1","poi3.0",
		// "velocity1.4","velocity1.5","xalan2.5","xalan2.7","xerces1.4.4"
		// };
		// String
		// datasets[]={"CM1","JM1","KC1","KC2","KC3","KC4","MC1","MC2","MW1","PC1","PC2","PC3","PC4","PC5"};
		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/EM1vs1/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];

		int Coding = 3; /* 编码方案 */

		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";
			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					// 多元分类器编码选择及基分类器设置
					MultiClassClassifier mc = new MultiClassClassifier();
					mc.setClassifier(models[i]);
					mc.setUsePairwiseCoupling(true);
					SelectedTag sel = new SelectedTag(Coding, mc.TAGS_METHOD);
					mc.setMethod(sel);
					// 多元过滤分类器过滤方法及基分类器设置
					MultiFilteredClassifier mf = new MultiFilteredClassifier();
					mf.setClassifier(mc);
					BalanceClass bc = new BalanceClass();
					mf.setFilter(bc);
					content += datasets[j] + "," + mxn.crossValidate(mf)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}

		}
	}

	/**
	 * 对不平衡数据集中的多数类使用聚类算法，聚成多个类 然后再用编码方案进行多类学习
	 */
	public static void evalMultiClassWithCluster() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);
		/*
		 * String datasets[] = { "breast-cancer", "breast-w", "credit-g",
		 * "haberman", "hepatitis", "ionosphere", "ozone1hr", "ozone8hr",
		 * "phoneme", "pima", "sick", "sick-euthyroid", "tic-tac-toe",
		 * "titanic", "abalone9-18", "abalone19", "carG", "ecoliCP-IM",
		 * "ecoliIM", "ecoliIMU", "ecoliOM", "glassBWFP", "glassBWNFP",
		 * "glassContainers", "glassNW", "glassTableware", "glassVWFP",
		 * "new-thyroid", "optdigits0", "satimage2", "satimage5", "segment1",
		 * "spliceEI", "spliceIE", "vehicleVAN", "vowelZ", "yeastCYT-POX",
		 * "yeastEXC", "yeastME1", "yeastME2" };
		 */
		String datasets[] = { "titanic" };
		String sourceDir = "D://data//";
		String destDir = "D://data//";
		/*
		 * String sourceDir = "data/"; String destDir = "KMeans/";
		 */
		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];

		int Coding = 3; /* 编码方案 */

		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "rip";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 2; i < 3; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();
				MxNFoldsold mxn = new MxNFoldsold();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					MultiClassClassifier mc = new MultiClassClassifier();
					mc.setClassifier(models[i]);
					mc.setUsePairwiseCoupling(true);
					SelectedTag sel = new SelectedTag(Coding, mc.TAGS_METHOD);
					mc.setMethod(sel);

					MultiFilteredClassifier mf = new MultiFilteredClassifier();
					mf.setClassifier(mc);
					MultiCluster cluster = new MultiCluster();
					mf.setFilter(cluster);

					content += datasets[j] + "," + mxn.crossValidate(mf);

				} catch (Exception e) {
					e.printStackTrace();
				}
				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(destname, content);
			}

		}

	}

	public static void evalRUSBoost() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);


		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/RUSBoost/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					RUSBoostM1 rusboost = new RUSBoostM1();
					rusboost.setClassifier(models[i]);
					rusboost.setNumIterations(10);
					content += datasets[j] + "," + mxn.crossValidate(rusboost)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}
	}

	public static void evalSMOTEBoost() {
		NaiveBayes nb = new NaiveBayes();
		J48 j48 = new J48();
		JRip rip = new JRip();
		RandomForest rf = new RandomForest();
		SMO smo = new SMO();
		BayesNet bn = new BayesNet();
		IBk ibk = new IBk();
		ibk.setKNN(5);

		String datasets[] = { "glass1","pima","glass0","yeast1",
				"haberman","vehicle1","vehicle3","ecoli2",
				"glass6","yeast3","ecoli3","ecoli-0-3-4_vs_5",
				"ecoli-0-6-7_vs_3-5","ecoli-0-2-3-4_vs_5","glass-0-1-5_vs_2","yeast-0-3-5-9_vs_7-8",
				"yeast-0-2-5-6_vs_3-7-8-9","ecoli-0-4-6_vs_5","ecoli-0-1_vs_2-3-5","ecoli-0-2-6-7_vs_3-5",
				"ecoli-0-3-4-6_vs_5","yeast-0-5-6-7-9_vs_4","ecoli-0-6-7_vs_5","glass-0-1-6_vs_2",
				"ecoli-0-1-4-7_vs_2-3-5-6","ecoli-0-1_vs_5", "glass-0-1-4-6_vs_2","glass2",
				 "ecoli-0-1-4-7_vs_5-6","cleveland-0_vs_4","ecoli-0-1-4-6_vs_5","yeast-1_vs_7",
				 "glass4","ecoli4","abalone9-18", "yeast-1-4-5-8_vs_7",
				 "yeast-2_vs_8","yeast4","yeast-1-2-8-9_vs_7","yeast5",
				 "ecoli-0-1-3-7_vs_2-6", "yeast6"};
		String sourceDir = "KeelArffData/";
		String destDir = "KeelDataResults/100AucResults/SMOTEBoost/";

		Classifier[] models = new Classifier[10];
		String[] modelNames = new String[10];
		models[0] = nb;
		models[1] = j48;
		models[2] = rip;
		models[3] = rf;
		models[4] = smo;
		models[5] = bn;
		models[6] = ibk;
		modelNames[0] = "nb";
		modelNames[1] = "j48";
		modelNames[2] = "ripper";
		modelNames[3] = "rf";
		modelNames[4] = "smo";
		modelNames[5] = "bn";
		modelNames[6] = "ibk";

		for (int i = 0; i < models.length; i++) {
			if (models[i] == null)
				break;
			String destname = destDir + modelNames[i] + ".csv";

			for (int j = 0; j < datasets.length; j++) {
				MxNResults mxn = new MxNResults();
				String source = sourceDir + datasets[j] + ".arff";
				mxn.generateData(source);
				String content = "";
				try {
					SMOTEBoostM1 smoteboost=new SMOTEBoostM1();
					smoteboost.setClassifier(models[i]);
					smoteboost.setNumIterations(10);
					content += datasets[j] + "," + mxn.crossValidate(smoteboost)+"\n";
				} catch (Exception e) {
					e.printStackTrace();
				}
				outputResults(destname, content);
			}
		}

	}

	/**
	 * 汇总实验结果
	 */
	public static void sumResults() {

		String doc[] = { "Orig", "UnderSample", "OverSample", "SMOTE",
				"MetaCost", "Bagging", "Boosting", "EM1vs1","RUSBoost"};
		String srcFileName[] = { "nb", "j48", "ripper", "rf", "smo","bn","ibk" };
		String destFileName[] = { "AUC_Results.xls", "TPR_Results.xls",
				"TNR_Results.xls", "GMeans_Results.xls", "Recall_Results.xls",
				"Presicion_Results.xls", "FMeasure_Results.xls",
				"Accuracy_Results.xls", "Time_Results.xls" };
		String destSheetName[] = { "Naive Bayes", "J48", "Ripper",
				"Random Forest", "SMO","BayesNet","IBk"};
		String srcPath = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelDataResults\\";
		String destPath = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelDataResults\\SumForResults\\";

		String column[] = {"data","ecoli-0_vs_1", "ecoli1", "ecoli2", "ecoli3",
				 "glass-0-1-2-3_vs_4-5-6", "glass0", "glass1", "glass6",
				 "haberman", "iris0", "new-thyroid1", "newthyroid2",
				 "page-blocks0", "pima", "segment0", "vehicle0", "vehicle1",
				 "vehicle2", "vehicle3", "wisconsin", "yeast1", "yeast3",
				 "abalone19", "abalone9-18",
				 "ecoli-0-1-3-7_vs_2-6", "ecoli4", "glass-0-1-6_vs_2",
				 "glass-0-1-6_vs_5", "glass2", "glass4", "glass5",
				 "page-blocks-1-3_vs_4", "shuttle-c0-vs-c4", "shuttle-c2-vs-c4",
				 "vowel0", "yeast-0-5-6-7-9_vs_4", "yeast-1-2-8-9_vs_7",
				 "yeast-1-4-5-8_vs_7", "yeast-1_vs_7", "yeast-2_vs_4",
				 "yeast-2_vs_8", "yeast4", "yeast5", "yeast6",
				 "cleveland-0_vs_4", "ecoli-0-1-4-6_vs_5",
					"ecoli-0-1-4-7_vs_2-3-5-6", "ecoli-0-1-4-7_vs_5-6",
					"ecoli-0-1_vs_2-3-5", "ecoli-0-1_vs_5", "ecoli-0-2-3-4_vs_5",
					"ecoli-0-2-6-7_vs_3-5", "ecoli-0-3-4-6_vs_5",
					"ecoli-0-3-4-7_vs_5-6", "ecoli-0-3-4_vs_5", "ecoli-0-4-6_vs_5",
					"ecoli-0-6-7_vs_3-5", "ecoli-0-6-7_vs_5", "glass-0-1-4-6_vs_2",
					"glass-0-1-5_vs_2", "glass-0-4_vs_5", "glass-0-6_vs_5",
					"led7digit-0-2-4-5-6-7-8-9_vs_1", "yeast-0-2-5-6_vs_3-7-8-9",
					"yeast-0-2-5-7-9_vs_3-6-8", "yeast-0-3-5-9_vs_7-8"
					};

		try {
			WritableWorkbook AUC = Workbook.createWorkbook(new File(destPath
					+ destFileName[0]));
			WritableWorkbook TPR = Workbook.createWorkbook(new File(destPath
					+ destFileName[1]));
			WritableWorkbook TNR = Workbook.createWorkbook(new File(destPath
					+ destFileName[2]));
			WritableWorkbook GMeans = Workbook.createWorkbook(new File(destPath
					+ destFileName[3]));
			WritableWorkbook Recall = Workbook.createWorkbook(new File(destPath
					+ destFileName[4]));
			WritableWorkbook Precision = Workbook.createWorkbook(new File(
					destPath + destFileName[5]));
			WritableWorkbook FMeasure = Workbook.createWorkbook(new File(
					destPath + destFileName[6]));
			WritableWorkbook Accuracy = Workbook.createWorkbook(new File(
					destPath + destFileName[7]));
			WritableWorkbook Time = Workbook.createWorkbook(new File(destPath
					+ destFileName[8]));

			for (int k = 0; k < destSheetName.length; k++) {
				WritableSheet sheet1 = AUC.createSheet(destSheetName[k], k);
				WritableSheet sheet2 = TPR.createSheet(destSheetName[k], k);
				WritableSheet sheet3 = TNR.createSheet(destSheetName[k], k);
				WritableSheet sheet4 = GMeans.createSheet(destSheetName[k], k);
				WritableSheet sheet5 = Recall.createSheet(destSheetName[k], k);
				WritableSheet sheet6 = Precision.createSheet(destSheetName[k],
						k);
				WritableSheet sheet7 = FMeasure
						.createSheet(destSheetName[k], k);
				WritableSheet sheet8 = Accuracy
						.createSheet(destSheetName[k], k);
				WritableSheet sheet9 = Time.createSheet(destSheetName[k], k);
				for (int i = 0; i < column.length; i++) {
					Label label1 = new Label(0, i, column[i]);
					sheet1.addCell(new Label(0, i, column[i]));
					sheet2.addCell(new Label(0, i, column[i]));
					sheet3.addCell(new Label(0, i, column[i]));
					sheet4.addCell(new Label(0, i, column[i]));
					sheet5.addCell(new Label(0, i, column[i]));
					sheet6.addCell(new Label(0, i, column[i]));
					sheet7.addCell(new Label(0, i, column[i]));
					sheet8.addCell(new Label(0, i, column[i]));
					sheet9.addCell(new Label(0, i, column[i]));
				}
				for (int j = 0; j < doc.length; j++) {
					sheet1.addCell(new Label(j + 1, 0, doc[j]));
					sheet2.addCell(new Label(j + 1, 0, doc[j]));
					sheet3.addCell(new Label(j + 1, 0, doc[j]));
					sheet4.addCell(new Label(j + 1, 0, doc[j]));
					sheet5.addCell(new Label(j + 1, 0, doc[j]));
					sheet6.addCell(new Label(j + 1, 0, doc[j]));
					sheet7.addCell(new Label(j + 1, 0, doc[j]));
					sheet8.addCell(new Label(j + 1, 0, doc[j]));
					sheet9.addCell(new Label(j + 1, 0, doc[j]));
				}

				for (int i = 0; i < doc.length; i++) {
					String srcFile = srcPath + doc[i] + "//" + srcFileName[k]
							+ ".csv";
					CsvReader reader = new CsvReader(srcFile);
					int row = 1;
					while (reader.readRecord()) {
						sheet1.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(1))));
						sheet2.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(2))));
						sheet3.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(3))));
						sheet4.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(4))));
						sheet5.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(5))));
						sheet6.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(6))));
						sheet7.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(7))));
						sheet8.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(8))));
						sheet9.addCell(new Number(i + 1, row, Double
								.parseDouble(reader.get(9))));
						row = row + 1;
					}
				}
			}

			AUC.write();
			AUC.close();
			TPR.write();
			TPR.close();
			TNR.write();
			TNR.close();
			GMeans.write();
			GMeans.close();
			Recall.write();
			Recall.close();
			Precision.write();
			Precision.close();
			FMeasure.write();
			FMeasure.close();
			Accuracy.write();
			Accuracy.close();
			Time.write();
			Time.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	public static void testCSV() {
		String filename = "F:\\Test\\Results\\Orig\\nb.csv";
		try {
			CsvReader reader = new CsvReader(filename);
			while (reader.readRecord()) {
				int column = reader.getColumnCount();
				for (int i = 0; i < column; i++) {
					System.out.print(reader.get(i) + ",");
				}
				System.out.println();
			}
			long currentrecord = reader.getCurrentRecord();
			System.out.println(currentrecord);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void testValidation(){
		String filename="F:\\DataSets\\NASA\\Original\\CM1.arff";
		DataSource ds;
		try {
			ds = new DataSource(filename);
			Instances data=ds.getDataSet();
			data.setClassIndex(data.numAttributes()-1);
			
			J48 j48 = new J48();
			double tpr=0;
			double auc=0;

			for (int i = 0; i < 10; i++) {
				Random rd = new Random(i);
				data.randomize(rd);
				if (data.classAttribute().isNominal()) {
					data.stratify(10);
				}
				Evaluation eval = new Evaluation(data);

				for (int j = 0; j < 10; j++) {
					// 训练
					Instances train = data.trainCV(10, j, rd);
					Classifier copyC = Classifier.makeCopy(j48);
					eval.setPriors(train);
					copyC.buildClassifier(train);
					// 测试
					Instances test = data.testCV(10, j);
					eval.evaluateModel(copyC, test);
					
					tpr=eval.truePositiveRate(0);
					auc=eval.areaUnderROC(0);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}

	/**
	 * 主函数
	 */
	public static void main(String[] args) {

        // countDataProperty();
		// evalBench();
		// evalOverSample();
		// evalUnderSample();
		// evalSMOTE();
		// evalMetaCost();
		// evalBagging();
		// evalBoosting();
		// evalMultiClassWithRandom();
		// evalMultiClassWithCluster();
		// evalRUSBoost();
		// evalSMOTEBoost();
		 sumResults();
		// testCSV();
	    // testValidation();
	}
}

