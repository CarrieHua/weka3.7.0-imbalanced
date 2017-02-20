/**
 * ���������ݴ������Ƚ�
 * Ĭ�ϴ���Ķ��಻���������������Ե�
 * ��һ�����ǩΪ������
 * �ڶ������ǩΪ������
 */
package szb.imbalanceDataHandling;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.CostMatrix;
import weka.classifiers.Evaluation;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.meta.MetaCost;
import weka.classifiers.meta.MultiClassClassifier;
import weka.classifiers.meta.MultiFilteredClassifier;
import weka.classifiers.rules.JRip;
import weka.classifiers.szb.EasyEnsemble;
import weka.classifiers.trees.J48;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SelectedTag;
import weka.core.Utils;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.szb.BalanceClass;
import weka.filters.szb.MyOverSample;
import weka.filters.szb.MySMOTE;
import weka.filters.szb.MyUnderSample;

/**
 * @author szb
 * 
 */
public class ImbalanceLearning {

	private Instances m_Data; // ���ݼ�
	private int m_Fold; // ������֤����
	private int m_Repeat; // ������֤����

	public ImbalanceLearning() {
		m_Data = null;
		m_Fold = 10;
		m_Repeat = 10;
	}

	/**
	 * ���ļ��ж�ȡ���ݼ�
	 * 
	 * @param filename
	 * @throws Exception
	 */
	public void generateData(String filename) throws Exception {
		DataSource ds = new DataSource(filename);
		m_Data = ds.getDataSet();
		m_Data.setClassIndex(m_Data.numAttributes() - 1);
	}

	/**
	 * ������֤����
	 * 
	 * @throws Exception
	 */
	public String crossValidate(Classifier c) throws Exception {

		double Accuracy = 0;
		double AUC = 0;
		double TPR = 0;
		double TNR = 0;
		double G_Means = 0;
		double Recall = 0;
		double Precision = 0;
		double F_Measure = 0;

		for (int i = 0; i < m_Repeat; i++) {
			Random rd = new Random(i);
			m_Data.randomize(rd);
			if (m_Data.classAttribute().isNominal()) {
				m_Data.stratify(m_Fold);
			}

			Evaluation eval = new Evaluation(m_Data);

			for (int j = 0; j < m_Fold; j++) {
				// ѵ��
				Instances train = m_Data.trainCV(m_Fold, j, rd);
				Classifier copyC = Classifier.makeCopy(c);
				eval.setPriors(train);
				copyC.buildClassifier(train);

				// ����
				Instances test = m_Data.testCV(m_Fold, j);
				eval.evaluateModel(copyC, test);
			}

			Accuracy += eval.pctCorrect();
			AUC += eval.areaUnderROC(0);
			TPR += eval.truePositiveRate(0);
			TNR += eval.trueNegativeRate(0);
			G_Means += Math.sqrt(eval.truePositiveRate(0)
					* eval.trueNegativeRate(0));
			Recall += eval.truePositiveRate(0);
			Precision += eval.precision(0);
			F_Measure += eval.fMeasure(0);
		}

		Accuracy = Accuracy / m_Repeat;
		AUC = AUC / m_Repeat;
		TPR = TPR / m_Repeat;
		TNR = TNR / m_Repeat;
		G_Means = G_Means / m_Repeat;
		Recall = Recall / m_Repeat;
		Precision = Precision / m_Repeat;
		F_Measure = F_Measure / m_Repeat;

		String result = Utils.doubleToString(Accuracy, 4) + ","
				+ Utils.doubleToString(AUC, 4) + ","
				+ Utils.doubleToString(TPR, 4) + ","
				+ Utils.doubleToString(TNR, 4) + ","
				+ Utils.doubleToString(G_Means, 4) + ","
				+ Utils.doubleToString(Recall, 4) + ","
				+ Utils.doubleToString(Precision, 4) + ","
				+ Utils.doubleToString(F_Measure, 4);
		return result;
	}
	
	/**
	 * ����ͨ������ʹ��MetaCost�㷨֮���ٽ�����֤
	 * @param c
	 * @return
	 * @throws Exception 
	 */
	public String metacostValidate(Classifier c) throws Exception{
		
		double Accuracy = 0;
		double AUC = 0;
		double TPR = 0;
		double TNR = 0;
		double G_Means = 0;
		double Recall = 0;
		double Precision = 0;
		double F_Measure = 0;
		
		for (int i = 0; i < m_Repeat; i++) {
			Random rd = new Random(i);
			m_Data.randomize(rd);
			if (m_Data.classAttribute().isNominal()) {
				m_Data.stratify(m_Fold);
			}
			Evaluation eval = new Evaluation(m_Data);

			for (int j = 0; j < m_Fold; j++) {
				// ѵ��
				Instances train = m_Data.trainCV(m_Fold, j, rd);
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

				// ����
				Instances test = m_Data.testCV(m_Fold, j);
				eval.evaluateModel(copyC, test);
			}
			Accuracy += eval.pctCorrect();
			AUC += eval.areaUnderROC(0);
			TPR += eval.truePositiveRate(0);
			TNR += eval.trueNegativeRate(0);
			G_Means += Math.sqrt(eval.truePositiveRate(0)
					* eval.trueNegativeRate(0));
			Recall += eval.truePositiveRate(0);
			Precision += eval.precision(0);
			F_Measure += eval.fMeasure(0);
		}

		Accuracy = Accuracy / m_Repeat;
		AUC = AUC / m_Repeat;
		TPR = TPR / m_Repeat;
		TNR = TNR / m_Repeat;
		G_Means = G_Means / m_Repeat;
		Recall = Recall / m_Repeat;
		Precision = Precision / m_Repeat;
		F_Measure = F_Measure / m_Repeat;

		String result = Utils.doubleToString(Accuracy, 4) + ","
				+ Utils.doubleToString(AUC, 4) + ","
				+ Utils.doubleToString(TPR, 4) + ","
				+ Utils.doubleToString(TNR, 4) + ","
				+ Utils.doubleToString(G_Means, 4) + ","
				+ Utils.doubleToString(Recall, 4) + ","
				+ Utils.doubleToString(Precision, 4) + ","
				+ Utils.doubleToString(F_Measure, 4);
		return result;
		
	}

	/**
	 * ������
	 * 
	 * @throws IOException
	 */
	public static void outputResults(String content, String filename)
			throws IOException {
		FileWriter writer = new FileWriter(filename, true);
		writer.write(content);
		writer.close();
	}

	/**
	 * ͳ�����ݼ��������� �� ����������������������ʵ�����������Ը�������������������ֵ
	 * 
	 * @throws Exception
	 */
	public static void countDataProperty() throws Exception {
		String datasets[] = { "CM1", "KC1" };
		String sourceDir = "F:/ImBalanceLearning/Data/";
		String destDir = "F:/ImBalanceLearning/Data/";

		String destName = destDir + "DatProverty.csv";
		String content = "Data" + "," + "Minority" + "," + "Majority" + ","
				+ "Sum" + "," + "Attributes" + "," + "Ratio" + "\n";
		outputResults(content, destName);
		for (int i = 0; i < datasets.length; i++) {
			String source = sourceDir + datasets[i] + ".arff";
			DataSource ds = new DataSource(source);
			Instances data = ds.getDataSet();
			data.setClassIndex(data.numAttributes() - 1);
			int sum = data.numInstances();
			int attr = data.numAttributes();
			int numMin = 0;
			int numMaj = 0;
			for (int j = 0; j < sum; j++) {
				Instance ins = data.instance(j);
				if (ins.classValue() == 0)
					numMin++;
				else if (ins.classValue() == 1)
					numMaj++;
			}
			int ratio = numMaj / numMin;
			content = datasets[i] + "," + numMin + "," + numMaj + "," + sum
					+ "," + attr + "," + ratio + "\n";
			outputResults(content, destName);
		}
	}

	/**
	 * ͳ�����ݼ��ж������������ı�ֵ
	 * 
	 * @param data
	 * @return
	 */
	public int checkRatio(Instances data) {
		int numMin = 0;
		int numMaj = 0;
		for (int i = 0; i < data.numInstances(); i++) {
			Instance ins = data.instance(i);
			if (ins.classValue() == 0)
				numMin++;
			else if (ins.classValue() == 1)
				numMaj++;
		}
		return numMaj / numMin;
	}

	/**
	 * ��������������
	 * 
	 * @throws Exception
	 */
	public static void evalBench() throws Exception {
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/Orig/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(model[i]);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}

	}
	
	/**
	 * ����UnderSampling
	 * under sampling�����ݼ��ж�������������ֵΪ1:1
	 * @throws Exception 
	 */
	public static void evalRUS() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/RUS/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				FilteredClassifier fc=new FilteredClassifier();
				fc.setClassifier(model[i]);
				MyUnderSample rus=new MyUnderSample();
				fc.setFilter(rus);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(fc);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}
	
	/**
	 * ����OverSampling
	 * over sampling�����ݼ��ж�������������ֵΪ1:1
	 * @throws Exception 
	 */
	public static void evalROS() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/ROS/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				FilteredClassifier fc=new FilteredClassifier();
				fc.setClassifier(model[i]);
				MyOverSample ros=new MyOverSample();
				fc.setFilter(ros);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(fc);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}
	
	/**
	 * ����SMOTE
	 * @throws Exception
	 */
	public static void evalSMOTE() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/SMOTE/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				FilteredClassifier fc=new FilteredClassifier();
				fc.setClassifier(model[i]);
				MySMOTE smote=new MySMOTE();
				fc.setFilter(smote);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(fc);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}
	
	/**
	 * ����MetaCost
	 * @throws Exception 
	 */
	public static void evalMetaCost() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/MetaCost/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.metacostValidate(model[i]);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
			
	}
	
	/**
	 * ����Bagging
	 * @throws Exception 
	 */
	public static void evalBagging() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/Bagging/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				Bagging bag=new Bagging();
				bag.setClassifier(model[i]);
				bag.setNumIterations(10);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(bag);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}
	
	/**
	 * ����Boosting
	 * @throws Exception 
	 */
	public static void evalBoosting() throws Exception{
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/Boosting/";

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				AdaBoostM1 boost=new AdaBoostM1();
				boost.setClassifier(model[i]);
				boost.setUseResampling(true);
				boost.setNumIterations(10);
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(boost);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}
	
	/**
	 * ����EM1vs1
	 * @throws Exception 
	 */
	public static void evalEM1vs1() throws Exception{
		
		// ����������
		J48 j48 = new J48();
		JRip rip = new JRip();

		// ����Դ
		String datasets[] = {"CM1","KC1"};
		String sourceDir = "F:/ImBalanceLearning/Data/";

		// ������Ŀ¼
		String destDir = "F:/ImBalanceLearning/Results/EM1vs1/";
		
		//���뷽��
		int Coding=3;

		Classifier[] model = new Classifier[10];
		String[] modelName = new String[10];

		model[0] = j48;
		modelName[0] = "j48";
		model[1]=rip;
		modelName[1]="rip";

		for (int i = 0; i < model.length; i++) {
			if (model[i] == null)
				break;
			String destName = destDir + modelName[i] + ".csv";
			String title = "Data" + "," + "Accuracy" + "," + "AUC" + ","+"TPR"
					+ "," + "TNR" + "," + "G_Means" + "," + "Recall" + ","
					+ "Precision" + "," + "F_Measure" + "," + "Time" + "\n";
			outputResults(title, destName);
			for (int j = 0; j < datasets.length; j++) {
				long timeStart = System.currentTimeMillis();

				String source = sourceDir + datasets[j] + ".arff";
				ImbalanceLearning idh = new ImbalanceLearning();
				idh.generateData(source);
				
				// ��Ԫ����������ѡ�񼰻�����������
				MultiClassClassifier mc = new MultiClassClassifier();
				mc.setClassifier(model[i]);
				mc.setUsePairwiseCoupling(true);
				SelectedTag sel = new SelectedTag(Coding, mc.TAGS_METHOD);
				mc.setMethod(sel);
				
				//��Ԫ���˷��������˷�����������������
				MultiFilteredClassifier mf=new MultiFilteredClassifier();
				mf.setClassifier(mc);
				BalanceClass bc=new BalanceClass();
				mf.setFilter(bc);
				
				String content = "";
				content = content + datasets[j] + ","
						+ idh.crossValidate(mf);

				long time = System.currentTimeMillis() - timeStart;
				content = content + ","
						+ Utils.doubleToString(time / 1000.0, 4) + "\n";
				outputResults(content, destName);
			}
		}
		
	}

	/**
	 * ������
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		//countDataProperty();
		//evalBench();
		//evalRUS();
		//evalROS();
		//evalSMOTE();
		//evalMetaCost();
		//evalBagging();
		//evalBoosting();
		evalEM1vs1();
		//evalEasyEnsemble();
	}

}
