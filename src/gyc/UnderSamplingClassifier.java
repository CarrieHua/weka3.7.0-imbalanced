package gyc;

/*
 *    把FilteredClassifier改成
 *    filter为UnderSampling的特定版本
 *    
 *    SMOTE的参数要根据training data的class distribution设置
 *    设置参数的过程要放进build_classifier里面
 */

import weka.classifiers.SingleClassifierEnhancer;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;
import weka.filters.supervised.instance.SpreadSubsample;


public class UnderSamplingClassifier
  extends SingleClassifierEnhancer 
  implements Drawable {

  /** for serialization */
  static final long serialVersionUID = -4523450618538717400L;
  
  /** The instance structure of the filtered instances */
  protected Instances m_FilteredInstances;

  /**
   * Returns a string describing this classifier
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return   "Class for running an arbitrary classifier on data that has been passed "
      + "through an arbitrary filter. Like the classifier, the structure of the filter "
      + "is based exclusively on the training data and test instances will be processed "
      + "by the filter without changing their structure.";
  }

  /**
   * String describing default classifier.
   * 
   * @return the default classifier classname
   */
  protected String defaultClassifierString() {
    
    return "weka.classifiers.trees.J48";
  }

  /**
   * Default constructor.
   */
  public UnderSamplingClassifier() {

    m_Classifier = new weka.classifiers.trees.J48();

  }

  /**
   * Returns the type of graph this classifier
   * represents.
   *  
   * @return the graph type of this classifier
   */   
  public int graphType() {
    
    if (m_Classifier instanceof Drawable)
      return ((Drawable)m_Classifier).graphType();
    else 
      return Drawable.NOT_DRAWABLE;
  }

  /**
   * Returns graph describing the classifier (if possible).
   *
   * @return the graph of the classifier in dotty format
   * @throws Exception if the classifier cannot be graphed
   */
  public String graph() throws Exception {
    
    if (m_Classifier instanceof Drawable)
      return ((Drawable)m_Classifier).graph();
    else throw new Exception("Classifier: " + getClassifierSpec()
			     + " cannot be graphed");
  }
  

  /**
   * Build the classifier on the filtered data.
   *
   * @param data the training data
   * @throws Exception if the classifier could not be built successfully
   */
  public void buildClassifier(Instances data) throws Exception {

    if (m_Classifier == null) {
      throw new Exception("No base classifiers have been set!");
    }

    // remove instances with missing class
    data = new Instances(data);
    data.deleteWithMissingClass();

  //统计类别实例个数，默认第一个类为少数类，第二个类为多数类
    int classNum[]=data.attributeStats(data.classIndex()).nominalCounts;
    int minC, nMin=classNum[0];
    int majC, nMaj=classNum[1];
    if (nMin<nMaj){
  	  minC=0;
  	  majC=1;
    }else {
  	  minC=1;
  	  majC=0;
  	  nMin=classNum[1];
  	  nMaj=classNum[0];
    }
    
    /*
    String fname = m_Filter.getClass().getName();
    fname = fname.substring(fname.lastIndexOf('.') + 1);
    util.Timer t = util.Timer.getTimer("FilteredClassifier::" + fname);
    t.start();
    */
    if (nMin>0){
    	SpreadSubsample filter = new SpreadSubsample();
	    filter.setInputFormat(data);  // filter capabilities are checked here
	    filter.setDistributionSpread(1.0);
	    //两个类之间最大比为1:1
	    data = Filter.useFilter(data, filter);
	    //t.stop();
    }
    
    //测试输出一下
    classNum=data.attributeStats(data.classIndex()).nominalCounts;
    //System.out.println(classNum[0]+","+classNum[1]);
    
    // can classifier handle the data?
    getClassifier().getCapabilities().testWithFail(data);

    m_FilteredInstances = data.stringFreeStructure();
    m_Classifier.buildClassifier(data);
  }

  /**
   * Classifies a given instance after filtering.
   *
   * @param instance the instance to be classified
   * @return the class distribution for the given instance
   * @throws Exception if instance could not be classified
   * successfully
   */
  public double [] distributionForInstance(Instance instance)
    throws Exception {

    /*
     * 去掉了filter的代码
     */
    return m_Classifier.distributionForInstance(instance);
  }

  /**
   * Output a representation of this classifier
   * 
   * @return a representation of this classifier
   */
  public String toString() {

    if (m_FilteredInstances == null) {
      return "FilteredClassifier: No model built yet.";
    }

    String result = "FilteredClassifier using "
      + getClassifierSpec()
      + " on data filtered through "
      + "SMOTE"
      + "\n\nFiltered Header\n"
      + m_FilteredInstances.toString()
      + "\n\nClassifier Model\n"
      + m_Classifier.toString();
    return result;
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: gyc-SMOTE-beta $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain the following arguments:
   * -t training file [-T test file] [-c class index]
   */
  public static void main(String [] argv) {
    runClassifier(new UnderSamplingClassifier(), argv);
  }
}
