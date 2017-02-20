/**
 * 
 */
package szb;

import java.util.Random;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;

/**
 * @author szb
 *
 */
public class testValidate {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
					// ÑµÁ·
					Instances train = data.trainCV(10, j, rd);
					Classifier copyC = Classifier.makeCopy(j48);
					eval.setPriors(train);
					copyC.buildClassifier(train);
					// ²âÊÔ
					Instances test = data.testCV(10, j);
					eval.evaluateModel(copyC, test);
					
					tpr=eval.truePositiveRate(0);
					auc=eval.areaUnderROC(0);
				}
				auc=eval.areaUnderROC(0);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
