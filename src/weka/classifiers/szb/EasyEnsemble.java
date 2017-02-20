/**
 * Ĭ�ϴ�������ݼ��е�һ����Ϊ������
 * �ڶ�����Ϊ������
 */
package weka.classifiers.szb;

import weka.classifiers.Classifier;
import weka.classifiers.meta.AdaBoostM1;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Utils;
import weka.filters.Filter;
import weka.filters.szb.MyUnderSample;

/**
 * @author szb
 * 
 */
public class EasyEnsemble extends Classifier {

	protected int m_SampleNum;
	protected Classifier m_Boost[];

	public EasyEnsemble(Classifier baseClassifier) throws Exception {
		m_SampleNum = 4;
		AdaBoostM1 boost = new AdaBoostM1();
		boost.setNumIterations(10);
		boost.setClassifier(baseClassifier);
		m_Boost = Classifier.makeCopies(boost, m_SampleNum);
	}

	/**
	 * ���data���������ʵ����
	 * 
	 * @param data
	 * @return
	 */
	protected int getNumMin(Instances data) {
		int numMin = 0;
		for (int i = 0; i < data.numInstances(); i++) {
			Instance ins = data.instance(i);
			if (ins.classValue() == 0)
				numMin++;
		}
		return numMin;
	}

	/**
	 * ���data�е�������ʵ��
	 * 
	 * @param data
	 * @return
	 */
	protected Instances getMinClass(Instances data) {
		Instances minData = new Instances(data, 0);
		for (int i = 0; i < data.numInstances(); i++) {
			Instance ins = data.instance(i);
			if (ins.classValue() == 0)
				minData.add(ins);
		}
		return minData;
	}

	/**
	 * ���data�еĶ�����ʵ��
	 * 
	 * @param data
	 * @return
	 */
	protected Instances getMajClass(Instances data) {
		Instances majData = new Instances(data, 0);
		for (int i = 0; i < data.numInstances(); i++) {
			Instance ins = data.instance(i);
			if (ins.classValue() == 1)
				majData.add(ins);
		}
		return majData;
	}

	/**
	 * EasyEnsemble����
	 * 
	 * @throws Exception
	 */
	public void buildClassifier(Instances data) throws Exception {

		Instances dataCopy = new Instances(data);
		dataCopy.deleteWithMissingClass();

		MyUnderSample rus = new MyUnderSample();
		rus.setInputFormat(dataCopy);
		for (int sampleNum = 0; sampleNum < m_SampleNum; sampleNum++) {
			Instances training = Filter.useFilter(dataCopy, rus);
			m_Boost[sampleNum].buildClassifier(training);
		}
	}

	/**
	 * ��δ֪ʵ������
	 */
	public double [] distributionForInstance(Instance ins) throws Exception {

        double sum[] = new double [2];
		for (int sampleNum = 0; sampleNum < m_SampleNum; sampleNum++) {
			double results[] = m_Boost[sampleNum].distributionForInstance(ins);
			sum[0] = sum[0] + results[0];
			sum[1] = sum[1] +results[1];
		}
		
		Utils.normalize(sum);
		
		return sum;
	}
}
