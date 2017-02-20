/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    You should have received a copy of the GNU General Public License
 *    along with this program; if not, write to the Free Software
 *    Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */

/*
 *    njj.java
 *    Copyright (C) 2000 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.classifiers.meta;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeSelection;
import weka.classifiers.SingleClassifierEnhancer;
import weka.core.AdditionalMeasureProducer;
import weka.core.Capabilities;
import weka.core.Drawable;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.WeightedInstancesHandler;
import weka.core.Capabilities.Capability;
////////////////////////////////////////njj
import weka.attributeSelection.CfsSubsetEval;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.*;
//////////////////////////////////////////njj/
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/**
 <!-- globalinfo-start -->
 * Dimensionality of training and test data is reduced by attribute selection before being passed on to a classifier.
 * <p/>
 <!-- globalinfo-end -->
 *
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -E &lt;attribute evaluator specification&gt;
 *  Full class name of attribute evaluator, followed
 *  by its options.
 *  eg: "weka.attributeSelection.CfsSubsetEval -L"
 *  (default weka.attributeSelection.CfsSubsetEval)</pre>
 * 
 * <pre> -S &lt;search method specification&gt;
 *  Full class name of search method, followed
 *  by its options.
 *  eg: "weka.attributeSelection.BestFirst -D 1"
 *  (default weka.attributeSelection.BestFirst)</pre>
 * 
 * <pre> -D
 *  If set, classifier is run in debug mode and
 *  may output additional info to the console</pre>
 * 
 * <pre> -W
 *  Full name of base classifier.
 *  (default: weka.classifiers.trees.J48)</pre>
 * 
 * <pre> 
 * Options specific to classifier weka.classifiers.trees.J48:
 * </pre>
 * 
 * <pre> -U
 *  Use unpruned tree.</pre>
 * 
 * <pre> -C &lt;pruning confidence&gt;
 *  Set confidence threshold for pruning.
 *  (default 0.25)</pre>
 * 
 * <pre> -M &lt;minimum number of instances&gt;
 *  Set minimum number of instances per leaf.
 *  (default 2)</pre>
 * 
 * <pre> -R
 *  Use reduced error pruning.</pre>
 * 
 * <pre> -N &lt;number of folds&gt;
 *  Set number of folds for reduced error
 *  pruning. One fold is used as pruning set.
 *  (default 3)</pre>
 * 
 * <pre> -B
 *  Use binary splits only.</pre>
 * 
 * <pre> -S
 *  Don't perform subtree raising.</pre>
 * 
 * <pre> -L
 *  Do not clean up after the tree has been built.</pre>
 * 
 * <pre> -A
 *  Laplace smoothing for predicted probabilities.</pre>
 * 
 * <pre> -Q &lt;seed&gt;
 *  Seed for random data shuffling (default 1).</pre>
 * 
 <!-- options-end -->
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 1.26 $
 */
public class njj 
  extends SingleClassifierEnhancer
  implements OptionHandler, Drawable, AdditionalMeasureProducer,
             WeightedInstancesHandler {

  /** for serialization */
  static final long serialVersionUID = -5951805453487947577L;
  
  /** The attribute selection object */
  protected AttributeSelection m_AttributeSelection = null;
  ///////////////////////njj
  protected Remove m_delTransform = new Remove();
//////////////////////////////////////njj

  /** The attribute evaluator to use */
  protected ASEvaluation m_Evaluator = 
    new weka.attributeSelection.CfsSubsetEval();

  /** The search method to use */
  protected ASSearch m_Search = new weka.attributeSelection.BestFirst();

  /** The header of the dimensionally reduced data */
  protected Instances m_ReducedHeader;

  /** The number of class vals in the training data (1 if class is numeric) */
  protected int m_numClasses;

  /** The number of attributes selected by the attribute selection phase */
  protected double m_numAttributesSelected;

  /** The time taken to select attributes in milliseconds */
  protected double m_selectionTime;

  /** The time taken to select attributes AND build the classifier */
  protected double m_totalTime;

  public double njjPercentage = 0.05;
  
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
  public njj() {
    m_Classifier = new weka.classifiers.trees.J48();
  }

  /**
   * Returns a string describing this search method
   * @return a description of the search method suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Dimensionality of training and test data is reduced by "
      +"attribute selection before being passed on to a classifier.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
     Vector newVector = new Vector(3);
    
    newVector.addElement(new Option(
	      "\tFull class name of attribute evaluator, followed\n"
	      + "\tby its options.\n"
	      + "\teg: \"weka.attributeSelection.CfsSubsetEval -L\"\n"
	      + "\t(default weka.attributeSelection.CfsSubsetEval)",
	      "E", 1, "-E <attribute evaluator specification>"));

    newVector.addElement(new Option(
	      "\tFull class name of search method, followed\n"
	      + "\tby its options.\n"
	      + "\teg: \"weka.attributeSelection.BestFirst -D 1\"\n"
	      + "\t(default weka.attributeSelection.BestFirst)",
	      "S", 1, "-S <search method specification>"));
    
    Enumeration enu = super.listOptions();
    while (enu.hasMoreElements()) {
      newVector.addElement(enu.nextElement());
    }
    return newVector.elements();
  }

  /**
   * Parses a given list of options. <p/>
   *
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -E &lt;attribute evaluator specification&gt;
   *  Full class name of attribute evaluator, followed
   *  by its options.
   *  eg: "weka.attributeSelection.CfsSubsetEval -L"
   *  (default weka.attributeSelection.CfsSubsetEval)</pre>
   * 
   * <pre> -S &lt;search method specification&gt;
   *  Full class name of search method, followed
   *  by its options.
   *  eg: "weka.attributeSelection.BestFirst -D 1"
   *  (default weka.attributeSelection.BestFirst)</pre>
   * 
   * <pre> -D
   *  If set, classifier is run in debug mode and
   *  may output additional info to the console</pre>
   * 
   * <pre> -W
   *  Full name of base classifier.
   *  (default: weka.classifiers.trees.J48)</pre>
   * 
   * <pre> 
   * Options specific to classifier weka.classifiers.trees.J48:
   * </pre>
   * 
   * <pre> -U
   *  Use unpruned tree.</pre>
   * 
   * <pre> -C &lt;pruning confidence&gt;
   *  Set confidence threshold for pruning.
   *  (default 0.25)</pre>
   * 
   * <pre> -M &lt;minimum number of instances&gt;
   *  Set minimum number of instances per leaf.
   *  (default 2)</pre>
   * 
   * <pre> -R
   *  Use reduced error pruning.</pre>
   * 
   * <pre> -N &lt;number of folds&gt;
   *  Set number of folds for reduced error
   *  pruning. One fold is used as pruning set.
   *  (default 3)</pre>
   * 
   * <pre> -B
   *  Use binary splits only.</pre>
   * 
   * <pre> -S
   *  Don't perform subtree raising.</pre>
   * 
   * <pre> -L
   *  Do not clean up after the tree has been built.</pre>
   * 
   * <pre> -A
   *  Laplace smoothing for predicted probabilities.</pre>
   * 
   * <pre> -Q &lt;seed&gt;
   *  Seed for random data shuffling (default 1).</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {

    // same for attribute evaluator
    String evaluatorString = Utils.getOption('E', options);
    if (evaluatorString.length() == 0)
      evaluatorString = weka.attributeSelection.CfsSubsetEval.class.getName();
    String [] evaluatorSpec = Utils.splitOptions(evaluatorString);
    if (evaluatorSpec.length == 0) {
      throw new Exception("Invalid attribute evaluator specification string");
    }
    String evaluatorName = evaluatorSpec[0];
    evaluatorSpec[0] = "";
    setEvaluator(ASEvaluation.forName(evaluatorName, evaluatorSpec));

    // same for search method
    String searchString = Utils.getOption('S', options);
    if (searchString.length() == 0)
      searchString = weka.attributeSelection.BestFirst.class.getName();
    String [] searchSpec = Utils.splitOptions(searchString);
    if (searchSpec.length == 0) {
      throw new Exception("Invalid search specification string");
    }
    String searchName = searchSpec[0];
    searchSpec[0] = "";
    setSearch(ASSearch.forName(searchName, searchSpec));
   
    String perString = Utils.getOption('P', options);
    if (perString.length() == 0)
    	setnjjPercentage(0.05);
    else
    	setnjjPercentage(Double.valueOf(perString).doubleValue());
    
    super.setOptions(options);
  }

  /**
   * Gets the current settings of the Classifier.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {

    String [] superOptions = super.getOptions();
    String [] options = new String [superOptions.length + 6];

    int current = 0;

    // same attribute evaluator
    options[current++] = "-E";
    options[current++] = "" +getEvaluatorSpec();
    
    // same for search
    options[current++] = "-S";
    options[current++] = "" + getSearchSpec();

    options[current++] = "-P";
    options[current++] = "" + getnjjPercentage();

    
    
    System.arraycopy(superOptions, 0, options, current, 
		     superOptions.length);
    
    
    return options;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String evaluatorTipText() {
    return "Set the attribute evaluator to use. This evaluator is used "
      +"during the attribute selection phase before the classifier is "
      +"invoked.";
  }

  /**
   * Sets the attribute evaluator
   *
   * @param evaluator the evaluator with all options set.
   */
  public void setEvaluator(ASEvaluation evaluator) {
    m_Evaluator = evaluator;
  }

  /**
   * Gets the attribute evaluator used
   *
   * @return the attribute evaluator
   */
  public ASEvaluation getEvaluator() {
    return m_Evaluator;
  }

  public void setnjjPercentage(double p){
	  njjPercentage=p;
  }
  public double getnjjPercentage(){
	  return njjPercentage;
  }
  /**
   * Gets the evaluator specification string, which contains the class name of
   * the attribute evaluator and any options to it
   *
   * @return the evaluator string.
   */
  protected String getEvaluatorSpec() {
    
    ASEvaluation e = getEvaluator();
    if (e instanceof OptionHandler) {
      return e.getClass().getName() + " "
	+ Utils.joinOptions(((OptionHandler)e).getOptions());
    }
    return e.getClass().getName();
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String searchTipText() {
    return "Set the search method. This search method is used "
      +"during the attribute selection phase before the classifier is "
      +"invoked.";
  }
  
  /**
   * Sets the search method
   *
   * @param search the search method with all options set.
   */
  public void setSearch(ASSearch search) {
    m_Search = search;
  }

  /**
   * Gets the search method used
   *
   * @return the search method
   */
  public ASSearch getSearch() {
    return m_Search;
  }

  /**
   * Gets the search specification string, which contains the class name of
   * the search method and any options to it
   *
   * @return the search string.
   */
  protected String getSearchSpec() {
    
    ASSearch s = getSearch();
    if (s instanceof OptionHandler) {
      return s.getClass().getName() + " "
	+ Utils.joinOptions(((OptionHandler)s).getOptions());
    }
    return s.getClass().getName();
  }

  /**
   * Returns default capabilities of the classifier.
   *
   * @return      the capabilities of this classifier
   */
  public Capabilities getCapabilities() {
    Capabilities	result;
    
    if (getEvaluator() == null)
      result = super.getCapabilities();
    else
      result = getEvaluator().getCapabilities();
    
    // set dependencies
    for (Capability cap: Capability.values())
      result.enableDependency(cap);
    
    return result;
  }

  /**
   * Build the classifier on the dimensionally reduced data.
   *
   * @param data the training data
   * @throws Exception if the classifier could not be built successfully
   */
  public void buildClassifier(Instances data) throws Exception {
    if (m_Classifier == null) {
      throw new Exception("No base classifier has been set!");
    }

    if (m_Evaluator == null) {
      throw new Exception("No attribute evaluator has been set!");
    }

    if (m_Search == null) {
      throw new Exception("No search method has been set!");
    }
   
    // can classifier handle the data?
    getCapabilities().testWithFail(data);

    // remove instances with missing class
    Instances newData = new Instances(data);
    newData.deleteWithMissingClass();
    
    if (newData.numInstances() == 0) {
      m_Classifier.buildClassifier(newData);
      return;
    }
    if (newData.classAttribute().isNominal()) {
      m_numClasses = newData.classAttribute().numValues();
    } else {
      m_numClasses = 1;
    }

    Instances resampledData = null;
    // check to see if training data has all equal weights
    double weight = newData.instance(0).weight();
    boolean ok = false;
    for (int i = 1; i < newData.numInstances(); i++) {
      if (newData.instance(i).weight() != weight) {
        ok = true;
        break;
      }
    }
    
    if (ok) {
      if (!(m_Evaluator instanceof WeightedInstancesHandler) || 
          !(m_Classifier instanceof WeightedInstancesHandler)) {
        Random r = new Random(1);
        for (int i = 0; i < 10; i++) {
          r.nextDouble();
        }
        resampledData = newData.resampleWithWeights(r);
      }
    } else {
      // all equal weights in the training data so just use as is
      resampledData = newData;
    }
   ////////////////////////////////////njj
    
    Instances njjData = new Instances(data);
    int njjFeatureNum;
    njjFeatureNum=njjData.numAttributes();
    //njjFeatureNum属性个数
    
    int i,j,ii,jj,njjlen;
    njjlen=(njjFeatureNum-1)*(njjFeatureNum-2)/2;
    //njjlen所有属性对的个数
    
    double su[][][]=new double[njjFeatureNum][njjFeatureNum][2];
    //属性i和j的su
    //su[i][j][0]存储距离，su[i][j][1]存储属于那个簇
    
    double suu[][]=new double[njjlen][3];
    //suu[][0]表示su的距离，suu[][1]表示i，suu[][2]表示j
    
    CfsSubsetEval njjCfs = new CfsSubsetEval();
    njjCfs.njjsetm_trInstances(njjData);
    //njjsetm_trInstances把数据集传入CFS中计算su
    
    //输出训练集
   /* for( i = 0; i < njjData.numInstances(); i++){
        Instance test = njjData.instance(i);	
            for (int kk = 0; kk < test.numAttributes(); kk++){
			     System.out.print("\t"+test.value(kk));
		    }
        System.out.println();
    }*/
    //

    
    ii=0;//指示suu的个数
    for(i=1;i<njjFeatureNum-1;i++){//计算相异度矩阵
    	for(j=0;j<i;j++){
    		su[i][j][0]=njjCfs.symmUncertCorr (i,j);
    		su[i][j][1]=0;//未被分簇，初始化为0
    		suu[ii][0]=su[i][j][0];
    		suu[ii][1]=i;
    		suu[ii][2]=j;
    		
    //		System.out.print(suu[ii][1]);
    //		System.out.print(suu[ii][2]);
    //		System.out.print(": ");
    	/*	System.out.print((int)suu[ii][1]);
        	
        	System.out.print((int)suu[ii][2]);
        	System.out.print(": ");
    		System.out.print(suu[ii][0]);
    		System.out.print("  ");*/
    		++ii;
    	}
 //   	System.out.println();
    }
//    System.out.println();
    
    //排序suu[0]最大，suu[njjlen-1]最小
    ////double suuu[]={20.0,53.0,1.0,5.0,46.0,45.0,10.0};
    //// njjlen=7;
    double temp;
    for(ii=0;ii<njjlen-1;ii++){
        for (jj=ii+1; jj < njjlen; jj++ ){
                if(suu[ii][0] < suu[jj][0]){
                    temp = suu[ii][0];
                    suu[ii][0] =suu[jj][0];
                    suu[jj][0] = temp;
                    
                    temp = suu[ii][1];
                    suu[ii][1] =suu[jj][1];
                    suu[jj][1] = temp;
                    
                    temp = suu[ii][2];
                    suu[ii][2] =suu[jj][2];
                    suu[jj][2] = temp;
                   
                    }
        }
    }
    
    for(ii=njjlen-1;ii>=0;ii--){
    	System.out.print((int)suu[ii][1]);
    	System.out.print(" ");
    	System.out.print((int)suu[ii][2]);
    	System.out.print(": ");
    
    	System.out.println(suu[ii][0]);
    }
    System.out.println();
    
    /////阈值**********************************************************
    //****************************************************************
//    double njjPercentage=0.1;
    double njjThreshold=suu[(int)(njjlen*njjPercentage)][0];
    
    ////DBSCAN聚类
    int x[]=new int[njjFeatureNum];
    for(int njj=0;njj<njjFeatureNum;njj++)
    	x[njj]=njjFeatureNum;
    
    int G[][]=new int[njjFeatureNum][njjFeatureNum];
    for(int njjj=0;njjj<njjFeatureNum;njjj++)
    	for(int jjj=0;jjj<njjFeatureNum;jjj++){
    		G[njjj][jjj]=njjFeatureNum;
    	}
    
    int featureFlag[]=new int[njjFeatureNum];//标记属性是否已经被分簇
    for(int nj=0;nj<njjFeatureNum;nj++)
    	featureFlag[nj]=0;
    
    int y1,g,xthis,xx,yy;
    //g表示簇数，xthis指示x[]中当前x
    
    g=1;
 
    for(int n1=0;n1<njjlen;n1++){//顺序扫描suu数组，分完所有属性
    	//int xx,yy;
    	xx=(int)suu[n1][1];
    	yy=(int)suu[n1][2];
    	if((su[xx][yy][1]==0)&&featureFlag[xx]==0&&featureFlag[yy]==0&&su[xx][yy][0]>njjThreshold){
    		//如果未被分簇
    		x[0]=xx;//i
    	    x[1]=yy;//j
    	    xthis=2;
    	    
    	    su[x[0]][x[1]][1]=g;
    	    featureFlag[x[0]]=g;
    	    featureFlag[x[1]]=g;   	    
    	    
    	    for(int n=0;n<xthis;n++){//对x[]中的每个属性进行画圆计算
    	    	for(y1=0;y1<=x[n]-1;y1++){
    	    		if(su[x[n]][y1][0]>njjThreshold&&su[x[n]][y1][1]==0&&featureFlag[y1]==0){//被划入圆内
    	    			su[x[n]][y1][1]=g;
    	    			featureFlag[y1]=g;//新属性标记入簇
    	    			x[xthis]=y1;//新属性加入到x[]中
    	    			xthis++;  	    			
    	    		}
    	    	}
    	    	for(y1=x[n]+1;y1<njjFeatureNum-1;y1++){
    	    		if(su[y1][x[n]][0]>njjThreshold&&su[y1][x[n]][1]==0&&featureFlag[y1]==0){ 	    			
    	    			su[y1][x[n]][1]=g;
    	    			featureFlag[y1]=g;
    	    			x[xthis]=y1;
    	    			xthis++;   	    			
    	    		}
    	    	}
    	    }//for
    	    g++;//划完一个簇
    	}//if
    	
    }
    //把featureFlag扫描整理到G[][]
    int c,k;
    for(k=1;k<g;k++){
    	c=0;
    	for(int njj=0;njj<njjFeatureNum-1;njj++){
    		if(featureFlag[njj]==k){
    			G[k][c]=njj;
    			c++;
    		}
    	}
    }
    for(int njj=0;njj<njjFeatureNum-1;njj++){//单个属性为一个簇的情况
    	if(featureFlag[njj]==0){
    		G[k][0]=njj;
    		k++;
    	}
    }
    int clusterNum=k-1;
    
    System.out.println("**************聚类结果*********");
    System.out.print("njjThreshold:");
    System.out.println(njjThreshold);
    System.out.print("G:");
    System.out.println(clusterNum);
 //   System.out.println();
 /*   System.out.println("featureFlag:");
    for(int t1=0;t1<njjFeatureNum-1;t1++){
    	System.out.print(t1);
    	System.out.print(":");
    	System.out.println(featureFlag[t1]);
    }
    System.out.println();*/
/*    for(int test=njjlen-1;test>=0;test--){
    	System.out.print((int)suu[test][1]);
    	System.out.print(" ");
    	System.out.println((int)suu[test][2]);
    }
    for(int t=1;t<g;t++){
    	System.out.print(t);
    	System.out.print(": ");
    	for(int tt=1;tt<njjFeatureNum;tt++)
    		for(int tty=0;tty<tt;tty++){
    			if(su[tt][tty][1]==t){
    				System.out.print(tt);
    				System.out.print(",");
    				System.out.print(tty);
    				System.out.print(",");
    			}
    		}
    	
    }*/
    
    for(int njj=1;njj<k;njj++){
    	c=0;
    	System.out.print(njj);
    	System.out.print(": ");
    	while(G[njj][c]!=njjFeatureNum){
    		System.out.print(G[njj][c]);
    		c++;
    		System.out.print(",");	
    	}
        System.out.println();
    }
    System.out.println("___________________________________________________________");
    
    
    //初选每个簇的第一个属性代表这个簇
    int att[]=new int[clusterNum+1];
    for(int i1=0;i1<clusterNum;i1++){
    	att[i1]=G[i1+1][0];
    }
    att[clusterNum]=njjFeatureNum-1;
   
    
    
 ////////////////////////////////////njj/   选属性 分类
     m_delTransform.setInvertSelection(true);
	 m_delTransform.setAttributeIndicesArray(att);
	 m_delTransform.setInputFormat(njjData);	
	 Instances njjnewData = Filter.useFilter(njjData, m_delTransform);
	 
	//输出训练集
	    for( i = 0; i < njjnewData.numInstances(); i++){
	        Instance test = njjnewData.instance(i);	
	            for (int kk = 0; kk < test.numAttributes(); kk++){
				     System.out.print("\t"+test.value(kk));
			    }
	        System.out.println();
	    }
	 m_Classifier.buildClassifier(njjnewData);
	 
    
    /////////////////////////////////////njj/
    
//    m_AttributeSelection = new AttributeSelection();
//    m_AttributeSelection.setEvaluator(m_Evaluator);
//    m_AttributeSelection.setSearch(m_Search);
//    long start = System.currentTimeMillis();
//    m_AttributeSelection.
//      SelectAttributes((m_Evaluator instanceof WeightedInstancesHandler) 
//                       ? newData
//                       : resampledData);
//    long end = System.currentTimeMillis();
//    
//    
//    
//    if (m_Classifier instanceof WeightedInstancesHandler) {
//      newData = m_AttributeSelection.reduceDimensionality(newData);
//      m_Classifier.buildClassifier(newData);
//    } else {
//      resampledData = m_AttributeSelection.reduceDimensionality(resampledData);
//      m_Classifier.buildClassifier(resampledData);
//    }
//    
//    
//    
//
//    long end2 = System.currentTimeMillis();
//    m_numAttributesSelected = m_AttributeSelection.numberAttributesSelected();
//    m_ReducedHeader = 
//      new Instances((m_Classifier instanceof WeightedInstancesHandler) ?
//                    newData
//                    : resampledData, 0);
//    m_selectionTime = (double)(end - start);
//    m_totalTime = (double)(end2 - start);
  }

  /**
   * Classifies a given instance after attribute selection
   *
   * @param instance the instance to be classified
   * @return the class distribution
   * @throws Exception if instance could not be classified
   * successfully
   */
  public double [] distributionForInstance(Instance instance)
    throws Exception {

//    Instance newInstance;
//    if (m_AttributeSelection == null) {
//      //      throw new Exception("njj: No model built yet!");
//      newInstance = instance;
//    } else {
//      newInstance = m_AttributeSelection.reduceDimensionality(instance);
//    }
    if (!m_delTransform.input(instance)) {
	      throw new Exception("Filter didn't make the test instance"
				  + " immediately available!");
	    }
	    m_delTransform.batchFinished();
	    Instance newInstance = m_delTransform.output();	

    return m_Classifier.distributionForInstance(newInstance);
  }

  /**
   *  Returns the type of graph this classifier
   *  represents.
   *  
   *  @return the type of graph
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
   * Output a representation of this classifier
   * 
   * @return a representation of this classifier
   */
  public String toString() {
    if (m_AttributeSelection == null) {
      return "njj: No attribute selection possible.\n\n"
	+m_Classifier.toString();
    }

    StringBuffer result = new StringBuffer();
    result.append("njj:\n\n");
    result.append(m_AttributeSelection.toResultsString());
    result.append("\n\nHeader of reduced data:\n"+m_ReducedHeader.toString());
    result.append("\n\nClassifier Model\n"+m_Classifier.toString());

    return result.toString();
  }

  /**
   * Additional measure --- number of attributes selected
   * @return the number of attributes selected
   */
  public double measureNumAttributesSelected() {
    return m_numAttributesSelected;
  }

  /**
   * Additional measure --- time taken (milliseconds) to select the attributes
   * @return the time taken to select attributes
   */
  public double measureSelectionTime() {
    return m_selectionTime;
  }

  /**
   * Additional measure --- time taken (milliseconds) to select attributes
   * and build the classifier
   * @return the total time (select attributes + build classifier)
   */
  public double measureTime() {
    return m_totalTime;
  }

  /**
   * Returns an enumeration of the additional measure names
   * @return an enumeration of the measure names
   */
  public Enumeration enumerateMeasures() {
    Vector newVector = new Vector(3);
    newVector.addElement("measureNumAttributesSelected");
    newVector.addElement("measureSelectionTime");
    newVector.addElement("measureTime");
    if (m_Classifier instanceof AdditionalMeasureProducer) {
      Enumeration en = ((AdditionalMeasureProducer)m_Classifier).
	enumerateMeasures();
      while (en.hasMoreElements()) {
	String mname = (String)en.nextElement();
	newVector.addElement(mname);
      }
    }
    return newVector.elements();
  }
  
  /**
   * Returns the value of the named measure
   * @param additionalMeasureName the name of the measure to query for its value
   * @return the value of the named measure
   * @throws IllegalArgumentException if the named measure is not supported
   */
  public double getMeasure(String additionalMeasureName) {
    if (additionalMeasureName.compareToIgnoreCase("measureNumAttributesSelected") == 0) {
      return measureNumAttributesSelected();
    } else if (additionalMeasureName.compareToIgnoreCase("measureSelectionTime") == 0) {
      return measureSelectionTime();
    } else if (additionalMeasureName.compareToIgnoreCase("measureTime") == 0) {
      return measureTime();
    } else if (m_Classifier instanceof AdditionalMeasureProducer) {
      return ((AdditionalMeasureProducer)m_Classifier).
	getMeasure(additionalMeasureName);
    } else {
      throw new IllegalArgumentException(additionalMeasureName 
			  + " not supported (njj)");
    }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.26 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain the following arguments:
   * -t training file [-T test file] [-c class index]
   */
  public static void main(String [] argv) {
    runClassifier(new njj(), argv);
  }
}
