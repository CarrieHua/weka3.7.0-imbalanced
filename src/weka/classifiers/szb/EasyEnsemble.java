/**
 * 默认处理的数据集中第一个类为少数类
 * 第二个类为多数类
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
	 * 获得data中少数类的实例数
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
	 * 获得data中的少数类实例
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
	 * 获得data中的多数类实例
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
	 * EasyEnsemble方法
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
	 * 对未知实例分类
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
