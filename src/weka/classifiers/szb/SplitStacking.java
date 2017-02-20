/**
 *  将多数类划分为多个子类，每个子类中含有与少数类相同的实例数量，然后将每个子类与
 * 少数类实例结合起来构成新的训练集，用这些训练集进行学习，构造多个分类器，最后
 * 将这些分类器的分类结果通过stacking技术整合起来
 */

package weka.classifiers.szb;

import weka.classifiers.Classifier;
import weka.classifiers.RandomizableMultipleClassifiersCombiner;
import weka.classifiers.rules.ZeroR;
import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.TechnicalInformation;
import weka.core.TechnicalInformationHandler;
import weka.core.Utils;
import weka.core.TechnicalInformation.Field;
import weka.core.TechnicalInformation.Type;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

public class SplitStacking 
  extends RandomizableMultipleClassifiersCombiner
  implements TechnicalInformationHandler {

  /** for serialization */
  static final long serialVersionUID = 5134738557155845452L;
  
  /** The meta classifier */
  protected Classifier m_MetaClassifier = new ZeroR();
  
  /** The base classifier */
  protected Classifier Base_Classifier = new ZeroR();
  
  protected Instances m_MetaData = null;
 
  /** Format for meta data */
  protected Instances m_MetaFormat = null;

  /** Format for base data */
  protected Instances m_BaseFormat = null;

  /** Set the number of folds for the cross-validation */
  protected int m_NumFolds = 10;
  
  protected Random m_Random;
    
  /**
   * Returns a string describing classifier
   * @return a description suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "Combines several classifiers using the stacking method. "
      + "Can do classification or regression.\n\n"
      + "For more information, see\n\n"
      + getTechnicalInformation().toString();
  }

  /**
   * Returns an instance of a TechnicalInformation object, containing 
   * detailed information about the technical background of this class,
   * e.g., paper reference or book this class is based on.
   * 
   * @return the technical information about this class
   */
  public TechnicalInformation getTechnicalInformation() {
    TechnicalInformation 	result;
    
    result = new TechnicalInformation(Type.ARTICLE);
    result.setValue(Field.AUTHOR, "David H. Wolpert");
    result.setValue(Field.YEAR, "1992");
    result.setValue(Field.TITLE, "Stacked generalization");
    result.setValue(Field.JOURNAL, "Neural Networks");
    result.setValue(Field.VOLUME, "5");
    result.setValue(Field.PAGES, "241-259");
    result.setValue(Field.PUBLISHER, "Pergamon Press");
    
    return result;
  }
  
  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    
    Vector newVector = new Vector(2);
    newVector.addElement(new Option(
	      metaOption(),
	      "M", 0, "-M <scheme specification>"));
    newVector.addElement(new Option(
	      "\tSets the number of cross-validation folds.",
	      "X", 1, "-X <number of folds>"));

    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }

  /**
   * String describing option for setting meta classifier
   * 
   * @return the string describing the option
   */
  protected String metaOption() {

    return "\tFull name of meta classifier, followed by options.\n" +
      "\t(default: \"weka.classifiers.rules.Zero\")";
  }

  /**
   * Parses a given list of options. <p/>
   *
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -M &lt;scheme specification&gt;
   *  Full name of meta classifier, followed by options.
   *  (default: "weka.classifiers.rules.Zero")</pre>
   * 
   * <pre> -X &lt;number of folds&gt;
   *  Sets the number of cross-validation folds.</pre>
   * 
   * <pre> -S &lt;num&gt;
   *  Random number seed.
   *  (default 1)</pre>
   * 
   * <pre> -B &lt;classifier specification&gt;
   *  Full class name of classifier to include, followed
   *  by scheme options. May be specified multiple times.
   *  (default: "weka.classifiers.rules.ZeroR")</pre>
   * 
   * <pre> -D
   *  If set, classifier is run in debug mode and
   *  may output additional info to the console</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {

    String numFoldsString = Utils.getOption('X', options);
    if (numFoldsString.length() != 0) {
      setNumFolds(Integer.parseInt(numFoldsString));
    } else {
      setNumFolds(10);
    }
    processMetaOptions(options);
    super.setOptions(options);
  }

  /**
   * Process options setting meta classifier.
   * 
   * @param options the options to parse
   * @throws Exception if the parsing fails
   */
  protected void processMetaOptions(String[] options) throws Exception {

    String classifierString = Utils.getOption('M', options);
    String [] classifierSpec = Utils.splitOptions(classifierString);
    String classifierName;
    if (classifierSpec.length == 0) {
      classifierName = "weka.classifiers.rules.ZeroR";
    } else {
      classifierName = classifierSpec[0];
      classifierSpec[0] = "";
    }
    setMetaClassifier(Classifier.forName(classifierName, classifierSpec));
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] superOptions = super.getOptions();
    String [] options = new String [superOptions.length + 4];

    int current = 0;
    options[current++] = "-X"; options[current++] = "" + getNumFolds();
    options[current++] = "-M";
    options[current++] = getMetaClassifier().getClass().getName() + " "
      + Utils.joinOptions(((OptionHandler)getMetaClassifier()).getOptions());

    System.arraycopy(superOptions, 0, options, current, 
		     superOptions.length);
    return options;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String numFoldsTipText() {
    return "The number of folds used for cross-validation.";
  }

  /** 
   * Gets the number of folds for the cross-validation.
   *
   * @return the number of folds for the cross-validation
   */
  public int getNumFolds() {

    return m_NumFolds;
  }

  /**
   * Sets the number of folds for the cross-validation.
   *
   * @param numFolds the number of folds for the cross-validation
   * @throws Exception if parameter illegal
   */
  public void setNumFolds(int numFolds) throws Exception {
    
    if (numFolds < 0) {
      throw new IllegalArgumentException("Stacking: Number of cross-validation " +
					 "folds must be positive.");
    }
    m_NumFolds = numFolds;
  }
  
  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String metaClassifierTipText() {
    return "The meta classifiers to be used.";
  }

  /**
   * Adds meta classifier
   *
   * @param classifier the classifier with all options set.
   */
  public void setMetaClassifier(Classifier classifier) {

    m_MetaClassifier = classifier;
  }
  
  /**
   * Gets the meta classifier.
   *
   * @return the meta classifier
   */
  public Classifier getMetaClassifier() {
    
    return m_MetaClassifier;
  }
  
  /**
   * Set the base learner.
   *
   * @param newClassifier the classifier to use.
   */
  public void setBaseClassifier(Classifier newClassifier) {

    Base_Classifier = newClassifier;
  }

  /**
   * Get the classifier used as the base learner.
   *
   * @return the classifier used as the classifier
   */
  public Classifier getBaseClassifier() {

    return Base_Classifier;
  }

  /**
   * Returns combined capabilities of the base classifiers, i.e., the
   * capabilities all of them have in common.
   *
   * @return      the capabilities of the base classifiers
   */
  public Capabilities getCapabilities() {
    Capabilities      result;
    
    result = super.getCapabilities();
    result.setMinimumNumberInstances(getNumFolds());

    return result;
  }

  /**
   * Buildclassifier selects a classifier from the set of classifiers
   * by minimising error on the training data.
   *
   * @param data the training data to be used for generating the
   * boosted classifier.
   * @throws Exception if the classifier could not be built successfully
   */
  public void buildClassifier(Instances data) throws Exception {

    if (m_MetaClassifier == null) {
      throw new IllegalArgumentException("No meta classifier has been set");
    }

    // can classifier handle the data?
    getCapabilities().testWithFail(data);  

    // remove instances with missing class
    Instances newData = new Instances(data);
    m_BaseFormat = new Instances(data, 0);
    newData.deleteWithMissingClass();
    //newData=ceilData(newData);
    
  //统计类别实例个数，默认第一个类为少数类，第二个类为多数类
    int classNum[]=newData.attributeStats(newData.classIndex()).nominalCounts;
    int minNum=classNum[0];
    int majNum=classNum[1]; 
    m_Classifiers=Classifier.makeCopies(Base_Classifier,majNum/minNum);
    m_Random = new Random(m_Seed); 
    m_MetaData = metaFormat(newData);
    m_MetaFormat = new Instances(m_MetaData, 0);
    
    newData.randomize(m_Random);
    if(newData.classAttribute().isNominal()){
    	newData.stratify(m_NumFolds);
    }
    for(int i=0;i<m_NumFolds;i++){
    	Instances train = newData.trainCV(m_NumFolds, i, m_Random);
    	Instances test = newData.testCV(m_NumFolds, i);
    	train.sort(train.classIndex());
        
    	for(int j=0;j<m_Classifiers.length;j++){
    		Instances sampleData=createSampleData(train,j);
    		m_Classifiers[j].buildClassifier(sampleData);
    	}  
    	
    	for(int k=0;k<test.numInstances();k++){
    		m_MetaData.add(metaInstance(test.instance(k)));
    	}    	
    }
 
    m_MetaClassifier.buildClassifier(m_MetaData);
    
    // Rebuilt all the base classifiers on the full training data
    newData.sort(newData.classIndex());
    for (int i = 0; i < m_Classifiers.length; i++) {
    	Instances sampleData=createSampleData(newData,i);
    	getClassifier(i).buildClassifier(sampleData);
    }
  }
  
  /**
   * 对原始数据进行采样
   * 选择所有少数类实例以及和少数类实例个数相同的多数类实例
   * @param data
   * @param i
   * @return
   */
  public Instances createSampleData(Instances data, int i){
	  Instances sample=new Instances(data,0);
	  int classNum[]=data.attributeStats(data.classIndex()).nominalCounts;
	  //少数类实例
	  for(int j=0;j<classNum[0];j++){
		  sample.add(data.instance(j));
	  }
	  //多数类实例
	  int num=classNum[1] / m_Classifiers.length;
	  //int offset=classNum[1] % m_Classifiers.length;
	  int start =i+classNum[0];
	  for(int j=0;j<=num && start<data.numInstances();j++){
		  sample.add(data.instance(start));  
		  start=start+m_Classifiers.length;
	  }
	  
	  m_Random = new Random(m_Seed); 
	  sample.randomize(m_Random);
	  return sample;
  }

  /**
   * 处理data后data中的少数类实例是m_NumFolds的整数倍，而多数类实例则是少数类
   * 实例的整数倍
   * @param data
   * @return
   */
  public Instances ceilData(Instances data){
	  Instances tempData=new Instances(data);
	  
	  //data中的多数类和少数类数据
	  Instances majData=new Instances(data,0);
	  Instances minData=new Instances(data,0);
	  for(int i=0;i<data.numInstances();i++){
		  if(data.instance(i).classValue()==1)
			  majData.add(data.instance(i));
		  else if(data.instance(i).classValue()==0)
		      minData.add(data.instance(i));
	  }
	  
	  //统计类别实例个数，默认第一个类为少数类，第二个类为多数类
	  int classNum[]=data.attributeStats(data.classIndex()).nominalCounts;
	  double minNum=classNum[0];
	  double majNum=classNum[1];
	  
	  double ceil1=Math.ceil(minNum/m_NumFolds);
	  double newMinNum=ceil1*m_NumFolds;
	  double ceil2=Math.ceil(majNum/newMinNum);
	  double newMajNum=ceil2*newMinNum;
	  
	  double sampleMinNum=newMinNum-minNum;
	  double sampleMajNum=newMajNum-majNum;
	  m_Random = new Random(m_Seed); 
	  
	  for(int i=0;i<sampleMinNum;i++){
		  int j=m_Random.nextInt(minData.numInstances());
		  tempData.add(minData.instance(j));
	  }
	  for(int i=0;i<sampleMajNum;i++){
		  int j=m_Random.nextInt(majData.numInstances());
		  tempData.add(majData.instance(j));
	  }
	  
	  tempData.randomize(m_Random);
	  return tempData;
  }
  
  /**
   * Generates the meta data
   * 
   * @param newData the data to work on
   * @param random the random number generator to use for cross-validation
   * @throws Exception if generation fails
   */
  protected void generateMetaLevel(Instances newData, int k, Random random) 
    throws Exception {
    for (int j = 0; j < m_NumFolds; j++) {
      Instances train = newData.trainCV(m_NumFolds, j, random);

      // Build base classifiers
	  getClassifier(k).buildClassifier(train);

      // Classify test instances and add to meta data
      Instances test = newData.testCV(m_NumFolds, j);
      for (int i = 0; i < test.numInstances(); i++) {
	   m_MetaData.add(metaInstance(test.instance(i)));
      }
    }
  }

  /**
   * Returns class probabilities.
   *
   * @param instance the instance to be classified
   * @return the distribution
   * @throws Exception if instance could not be classified
   * successfully
   */
  public double[] distributionForInstance(Instance instance) throws Exception {

    return m_MetaClassifier.distributionForInstance(metaInstance(instance));
  }

  /**
   * Output a representation of this classifier
   * 
   * @return a string representation of the classifier
   */
  public String toString() {

    if (m_Classifiers.length == 0) {
      return "Stacking: No base schemes entered.";
    }
    if (m_MetaClassifier == null) {
      return "Stacking: No meta scheme selected.";
    }
    if (m_MetaFormat == null) {
      return "Stacking: No model built yet.";
    }
    String result = "Stacking\n\nBase classifiers\n\n";
    for (int i = 0; i < m_Classifiers.length; i++) {
      result += getClassifier(i).toString() +"\n\n";
    }
   
    result += "\n\nMeta classifier\n\n";
    result += m_MetaClassifier.toString();

    return result;
  }

  /**
   * Makes the format for the level-1 data.
   *
   * @param instances the level-0 format
   * @return the format for the meta data
   * @throws Exception if the format generation fails
   */
  protected Instances metaFormat(Instances instances) throws Exception {

    FastVector attributes = new FastVector();
    Instances metaFormat;

    for (int k = 0; k < m_Classifiers.length; k++) {
      Classifier classifier = (Classifier) getClassifier(k);
      String name = classifier.getClass().getName();
      if (m_BaseFormat.classAttribute().isNumeric()) {
	attributes.addElement(new Attribute(name));
      } else {
	for (int j = 0; j < m_BaseFormat.classAttribute().numValues(); j++) {
	  attributes.addElement(new Attribute(name + ":" + 
					      m_BaseFormat
					      .classAttribute().value(j)));
	}
      }
    }
    attributes.addElement(m_BaseFormat.classAttribute().copy());
    metaFormat = new Instances("Meta format", attributes, 0);
    metaFormat.setClassIndex(metaFormat.numAttributes() - 1);
    return metaFormat;
  }

  /**
   * Makes a level-1 instance from the given instance.
   * 
   * @param instance the instance to be transformed
   * @return the level-1 instance
   * @throws Exception if the instance generation fails
   */
  protected Instance metaInstance(Instance instance) throws Exception {

    double[] values = new double[m_MetaFormat.numAttributes()];
    Instance metaInstance;
    int i = 0;
    for (int k = 0; k < m_Classifiers.length; k++) {
      Classifier classifier = getClassifier(k);
      if (m_BaseFormat.classAttribute().isNumeric()) {
	values[i++] = classifier.classifyInstance(instance);
      } else {
	double[] dist = classifier.distributionForInstance(instance);
	for (int j = 0; j < dist.length; j++) {
	  values[i++] = dist[j];
	}
      }
    }
    values[i] = instance.classValue();
    metaInstance = new Instance(1, values);
    metaInstance.setDataset(m_MetaFormat);
    return metaInstance;
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.32 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain the following arguments:
   * -t training file [-T test file] [-c class index]
   */
  public static void main(String [] argv) {
    runClassifier(new SplitStacking(), argv);
  }
}
