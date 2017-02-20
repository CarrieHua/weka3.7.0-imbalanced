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
 *    AttributeSelection.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */

package weka.filters.supervised.attribute;

import weka.AppendToFile;
import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeTransformer;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.UnsupervisedAttributeEvaluator;
import weka.attributeSelection.UnsupervisedSubsetEvaluator;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.SparseInstance;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;

import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Vector;
import java.io.IOException;
/** 
 <!-- globalinfo-start -->
 * A supervised attribute filter that can be used to select attributes. It is very flexible and allows various search and evaluation methods to be combined.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -S &lt;"Name of search class [search options]"&gt;
 *  Sets search method for subset evaluators.
 *  eg. -S "weka.attributeSelection.BestFirst -S 8"</pre>
 * 
 * <pre> -E &lt;"Name of attribute/subset evaluation class [evaluator options]"&gt;
 *  Sets attribute/subset evaluator.
 *  eg. -E "weka.attributeSelection.CfsSubsetEval -L"</pre>
 * 
 * <pre> 
 * Options specific to evaluator weka.attributeSelection.CfsSubsetEval:
 * </pre>
 * 
 * <pre> -M
 *  Treat missing values as a seperate value.</pre>
 * 
 * <pre> -L
 *  Don't include locally predictive attributes.</pre>
 * 
 * <pre> 
 * Options specific to search weka.attributeSelection.BestFirst:
 * </pre>
 * 
 * <pre> -P &lt;start set&gt;
 *  Specify a starting set of attributes.
 *  Eg. 1,3,5-7.</pre>
 * 
 * <pre> -D &lt;0 = backward | 1 = forward | 2 = bi-directional&gt;
 *  Direction of search. (default = 1).</pre>
 * 
 * <pre> -N &lt;num&gt;
 *  Number of non-improving nodes to
 *  consider before terminating search.</pre>
 * 
 * <pre> -S &lt;num&gt;
 *  Size of lookup cache for evaluated subsets.
 *  Expressed as a multiple of the number of
 *  attributes in the data set. (default = 1)</pre>
 * 
 <!-- options-end -->
 *
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 1.9 $
 */
public class ReliefFbest
  extends Filter
  implements SupervisedFilter, OptionHandler {
  
  /** for serialization */
  static final long serialVersionUID = -296211247688169716L;

  /** the attribute selection evaluation object */
  private weka.attributeSelection.AttributeSelection m_trainSelector;

  /** the attribute evaluator to use */
  private ASEvaluation m_ASEvaluator;

  /** the search method if any */
  private ASSearch m_ASSearch;

  /** holds a copy of the full set of valid  options passed to the filter */
  private String [] m_FilterOptions;

  /** holds the selected attributes  */
  private int [] m_SelectedAttributes;
  public double njjPercentage = 0.05;
//  public double NN;
//  public int NumAttr;
  /**
   * Returns a string describing this filter
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {

    return "A supervised attribute filter that can be used to select " 
      + "attributes. It is very flexible and allows various search " 
      + "and evaluation methods to be combined.";
  }

  /**
   * Constructor
   */
  public ReliefFbest () {
    
    resetOptions();
  }
  
//  public ReliefFbest (double nn) {
//	    
//	    resetOptions();
//	    NN=nn;
//	  }

  /**
   * Returns an enumeration describing the available options.
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    
    Vector newVector = new Vector(6);

    newVector.addElement(new Option(
	"\tSets search method for subset evaluators.\n"
	+ "\teg. -S \"weka.attributeSelection.BestFirst -S 8\"", 
	"S", 1,
	"-S <\"Name of search class [search options]\">"));

    newVector.addElement(new Option(
	"\tSets attribute/subset evaluator.\n"
	+ "\teg. -E \"weka.attributeSelection.CfsSubsetEval -L\"",
	"E", 1,
	"-E <\"Name of attribute/subset evaluation class [evaluator options]\">"));
    
    if ((m_ASEvaluator != null) && (m_ASEvaluator instanceof OptionHandler)) {
      Enumeration enu = ((OptionHandler)m_ASEvaluator).listOptions();
      
      newVector.addElement(new Option("", "", 0, "\nOptions specific to "
	   + "evaluator " + m_ASEvaluator.getClass().getName() + ":"));
      while (enu.hasMoreElements()) {
	newVector.addElement((Option)enu.nextElement());
      }
    }
  
    if ((m_ASSearch != null) && (m_ASSearch instanceof OptionHandler)) {
      Enumeration enu = ((OptionHandler)m_ASSearch).listOptions();
    
      newVector.addElement(new Option("", "", 0, "\nOptions specific to "
	      + "search " + m_ASSearch.getClass().getName() + ":"));
      while (enu.hasMoreElements()) {
	newVector.addElement((Option)enu.nextElement());
      }
    }
    return newVector.elements();
  }

  /**
   * Parses a given list of options. <p/>
   * 
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -S &lt;"Name of search class [search options]"&gt;
   *  Sets search method for subset evaluators.
   *  eg. -S "weka.attributeSelection.BestFirst -S 8"</pre>
   * 
   * <pre> -E &lt;"Name of attribute/subset evaluation class [evaluator options]"&gt;
   *  Sets attribute/subset evaluator.
   *  eg. -E "weka.attributeSelection.CfsSubsetEval -L"</pre>
   * 
   * <pre> 
   * Options specific to evaluator weka.attributeSelection.CfsSubsetEval:
   * </pre>
   * 
   * <pre> -M
   *  Treat missing values as a seperate value.</pre>
   * 
   * <pre> -L
   *  Don't include locally predictive attributes.</pre>
   * 
   * <pre> 
   * Options specific to search weka.attributeSelection.BestFirst:
   * </pre>
   * 
   * <pre> -P &lt;start set&gt;
   *  Specify a starting set of attributes.
   *  Eg. 1,3,5-7.</pre>
   * 
   * <pre> -D &lt;0 = backward | 1 = forward | 2 = bi-directional&gt;
   *  Direction of search. (default = 1).</pre>
   * 
   * <pre> -N &lt;num&gt;
   *  Number of non-improving nodes to
   *  consider before terminating search.</pre>
   * 
   * <pre> -S &lt;num&gt;
   *  Size of lookup cache for evaluated subsets.
   *  Expressed as a multiple of the number of
   *  attributes in the data set. (default = 1)</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    
    String optionString;
    resetOptions();

    if (Utils.getFlag('X',options)) {
	throw new Exception("Cross validation is not a valid option"
			    + " when using attribute selection as a Filter.");
    }

    optionString = Utils.getOption('E',options);
    if (optionString.length() != 0) {
      optionString = optionString.trim();
      // split a quoted evaluator name from its options (if any)
      int breakLoc = optionString.indexOf(' ');
      String evalClassName = optionString;
      String evalOptionsString = "";
      String [] evalOptions=null;
      if (breakLoc != -1) {
	evalClassName = optionString.substring(0, breakLoc);
	evalOptionsString = optionString.substring(breakLoc).trim();
	evalOptions = Utils.splitOptions(evalOptionsString);
      }
      setEvaluator(ASEvaluation.forName(evalClassName, evalOptions));
    }

    if (m_ASEvaluator instanceof AttributeEvaluator) {
      setSearch(new Ranker());
    }
    
 

    optionString = Utils.getOption('S',options);
//    +Njj;
    
    
    
    if (optionString.length() != 0) {
      optionString = optionString.trim();
      int breakLoc = optionString.indexOf(' ');
      String SearchClassName = optionString;
      String SearchOptionsString = "";
      String [] SearchOptions=null;
      if (breakLoc != -1) {
	SearchClassName = optionString.substring(0, breakLoc);
	SearchOptionsString = optionString.substring(breakLoc).trim();
	SearchOptions = Utils.splitOptions(SearchOptionsString);
      }
      setSearch(ASSearch.forName(SearchClassName, SearchOptions));
    }

    Utils.checkForRemainingOptions(options);
  }


  
  public void njjsetOptions(String[] options, int n) throws Exception {
	    
	    String optionString;
	    resetOptions();

	    if (Utils.getFlag('X',options)) {
		throw new Exception("Cross validation is not a valid option"
				    + " when using attribute selection as a Filter.");
	    }

	    optionString = Utils.getOption('E',options);
	    if (optionString.length() != 0) {
	      optionString = optionString.trim();
	      // split a quoted evaluator name from its options (if any)
	      int breakLoc = optionString.indexOf(' ');
	      String evalClassName = optionString;
	      String evalOptionsString = "";
	      String [] evalOptions=null;
	      if (breakLoc != -1) {
		evalClassName = optionString.substring(0, breakLoc);
		evalOptionsString = optionString.substring(breakLoc).trim();
		evalOptions = Utils.splitOptions(evalOptionsString);
	      }
	      setEvaluator(ASEvaluation.forName(evalClassName, evalOptions));
	    }

	    if (m_ASEvaluator instanceof AttributeEvaluator) {
	      setSearch(new Ranker());
	    }
	    
	   
//	    String Njj=String.valueOf(NN*(double)n);


	    optionString = Utils.getOption('S',options);
//	    +"-N" +Njj ;
//	    +Njj;
	    
	    
	    
	    if (optionString.length() != 0) {
	      optionString = optionString.trim();
	      int breakLoc = optionString.indexOf(' ');
	      String SearchClassName = optionString;
	      String SearchOptionsString = "";
	      String [] SearchOptions=null;
	      if (breakLoc != -1) {
		SearchClassName = optionString.substring(0, breakLoc);
		SearchOptionsString = optionString.substring(breakLoc).trim();
		SearchOptions = Utils.splitOptions(SearchOptionsString);
	      }
	      setSearch(ASSearch.forName(SearchClassName, SearchOptions));
	    }

	    Utils.checkForRemainingOptions(options);
	  }


  
  
  
  
  /**
   * Gets the current settings for the attribute selection (search, evaluator)
   * etc.
   *
   * @return an array of strings suitable for passing to setOptions()
   */
  public String [] getOptions() {
    String [] EvaluatorOptions = new String[0];
//    String [] SearchOptions1 = new String[0];
    String [] SearchOptions = new String[0];
    int current = 0;

    if (m_ASEvaluator instanceof OptionHandler) {
      EvaluatorOptions = ((OptionHandler)m_ASEvaluator).getOptions();
    }

    if (m_ASSearch instanceof OptionHandler) {
        
      SearchOptions = ((OptionHandler)m_ASSearch).getOptions();
//      SearchOptions = new String[SearchOptions1.length+2];
//      NumAttr = getInputFormat().numAttributes();
//      String Njj =String.valueOf((int)(NN*(double)NumAttr));
      
//      SearchOptions[5]="10";
//      SearchOptions[SearchOptions.length-1]=Njj;
//      SearchOptions[SearchOptions.length-2]="-N";
//      String [] SearchOptions =SearchOptions1 + "-N "+ Njj;
    }

    String [] setOptions = new String [10];
    setOptions[current++]="-E";
    setOptions[current++]= getEvaluator().getClass().getName()
      +" "+Utils.joinOptions(EvaluatorOptions);

    setOptions[current++]="-S";
    

    
    
    setOptions[current++]=getSearch().getClass().getName() 
      + " "+Utils.joinOptions(SearchOptions);

    while (current < setOptions.length) {
      setOptions[current++] = "";
    }
    
    return setOptions;
  }
  
  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String evaluatorTipText() {

    return "Determines how attributes/attribute subsets are evaluated.";
  }

  /**
   * set attribute/subset evaluator
   * 
   * @param evaluator the evaluator to use
   */
  public void setEvaluator(ASEvaluation evaluator) {
    m_ASEvaluator = evaluator;
  }
  
  /**
   * Returns the tip text for this property
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String searchTipText() {

    return "Determines the search method.";
  }

  /**
   * Set search class
   * 
   * @param search the search class to use
   */
  public void setSearch(ASSearch search) {
    m_ASSearch = search;
  }

  /**
   * Get the name of the attribute/subset evaluator
   *
   * @return the name of the attribute/subset evaluator as a string
   */
  public ASEvaluation getEvaluator() {
    
      return m_ASEvaluator;
  }

  /**
   * Get the name of the search method
   *
   * @return the name of the search method as a string
   */
  public ASSearch getSearch() {
    
      return m_ASSearch;
  }

  /** 
   * Returns the Capabilities of this filter.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
  public Capabilities getCapabilities() {
    Capabilities	result;
    
    if (m_ASEvaluator == null) {
      result = super.getCapabilities();
    }
    else {
      result = m_ASEvaluator.getCapabilities();
      // class index will be set if necessary, so we always allow the dataset
      // to have no class attribute set. see the following method:
      //   weka.attributeSelection.AttributeSelection.SelectAttributes(Instances)
      result.enable(Capability.NO_CLASS);
    }
    
    result.setMinimumNumberInstances(0);
    
    return result;
  }

  /**
   * Input an instance for filtering. Ordinarily the instance is processed
   * and made available for output immediately. Some filters require all
   * instances be read before producing output.
   *
   * @param instance the input instance
   * @return true if the filtered instance may now be
   * collected with output().
   * @throws IllegalStateException if no input format has been defined.
   * @throws Exception if the input instance was not of the correct format 
   * or if there was a problem with the filtering.
   */
//  public boolean GetNumAttr(Instance instance) throws Exception {
//	    
//	    if (getInputFormat() == null) {
//	      throw new IllegalStateException("No input instance format defined");
//	    }
//	    NumAttr=getInputFormat().numAttributes();
//	    String Njj=String.valueOf(NN*(double)NumAttr);
//	    return true;
//  }

  
  
  
  
  public boolean input(Instance instance) throws Exception {
    
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
   
    

    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }

    if (isOutputFormatDefined()) {
      convertInstance(instance);
      return true;
    }

    bufferInput(instance);
    return false;
  }

  /**
   * Signify that this batch of input to the filter is finished. If the filter
   * requires all instances prior to filtering, output() may now be called
   * to retrieve the filtered instances.
   *
   * @return true if there are instances pending output.
   * @throws IllegalStateException if no input structure has been defined.
   * @throws Exception if there is a problem during the attribute selection.
   */
  public boolean batchFinished() throws Exception {
    
    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    
    
   
    if (!isOutputFormatDefined()) {
    	
      m_trainSelector.setEvaluator(m_ASEvaluator);
      m_trainSelector.setSearch(m_ASSearch);
      long st = System.currentTimeMillis();
      m_trainSelector.SelectAttributes(getInputFormat());
      //      System.out.println(m_trainSelector.toResultsString());
    
      m_SelectedAttributes = m_trainSelector.selectedAttributes();
      long end =System.currentTimeMillis();
//      System.out.println("**cost time:"+(end-st));
      
      
      if (m_SelectedAttributes == null) {
	throw new Exception("No selected attributes\n");
      }
//      int clusterNum;
	
//      for (int i = 0; i < m_SelectedAttributes.length; i++){
//    	  System.out.print("\t"+m_SelectedAttributes[i]);
////    	  clusterNum=i;
//      }
//     System.out.println();
      setOutputFormat();
//      m_OutputFormat.setRelationName( clusterNum+","+ njjData.numAttributes()+ ","  +name  );
      
      // Convert pending input instances
     
      String name =getInputFormat().relationName();
      m_OutputFormat.setRelationName(m_SelectedAttributes.length+","+ getInputFormat().numAttributes()+ ",ReliefF,"  +name  );
      for (int i = 0; i < getInputFormat().numInstances(); i++) {
	convertInstance(getInputFormat().instance(i));
      }
  	AppendToFile.appendMethodA("D:/7/1258ReliefF/Time.txt", m_SelectedAttributes.length+","+getInputFormat().numAttributes()+",");
      flushInput();
    }
    
    m_NewBatch = true;
    return (numPendingOutput() != 0);
  }

  /**
   * Set the output format. Takes the currently defined attribute set 
   * m_InputFormat and calls setOutputFormat(Instances) appropriately.
   * 
   * @throws Exception if something goes wrong
   */
  protected void setOutputFormat() throws Exception {
    Instances informat;

    if (m_SelectedAttributes == null) {
      setOutputFormat(null);
      return;
    }

    FastVector attributes = new FastVector(m_SelectedAttributes.length);

    int i;
    if (m_ASEvaluator instanceof AttributeTransformer) {
      informat = ((AttributeTransformer)m_ASEvaluator).transformedData();
    } else {
      informat = getInputFormat();
    }

    for (i=0;i < m_SelectedAttributes.length;i++) {
      attributes.
	addElement(informat.attribute(m_SelectedAttributes[i]).copy());
    }

    Instances outputFormat = 
      new Instances(getInputFormat().relationName(), attributes, 0);


    if (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator) &&
	!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) {
      outputFormat.setClassIndex(m_SelectedAttributes.length - 1);
    }
    
    setOutputFormat(outputFormat);  
  }

  /**
   * Convert a single instance over. Selected attributes only are transfered.
   * The converted instance is added to the end of
   * the output queue.
   *
   * @param instance the instance to convert
   * @throws Exception if something goes wrong
   */
  protected void convertInstance(Instance instance) throws Exception {
    double[] newVals = new double[getOutputFormat().numAttributes()];
//    NumAttr =(int)( NN*(double)getOutputFormat().numAttributes());
    if (m_ASEvaluator instanceof AttributeTransformer) {
      Instance tempInstance = ((AttributeTransformer)m_ASEvaluator).
	convertInstance(instance);
      for (int i = 0; i < m_SelectedAttributes.length; i++) {
	int current = m_SelectedAttributes[i];
	newVals[i] = tempInstance.value(current);
      }
    } else {
      for (int i = 0; i < m_SelectedAttributes.length; i++) {
	int current = m_SelectedAttributes[i];
	newVals[i] = instance.value(current);
      }
    }
    if (instance instanceof SparseInstance) {
      push(new SparseInstance(instance.weight(), newVals));
    } else {
      push(new Instance(instance.weight(), newVals));
    }
  }

  /**
   * set options to their default values
   */
  protected void resetOptions() {

    m_trainSelector = new weka.attributeSelection.AttributeSelection();
    setEvaluator(new CfsSubsetEval());
    setSearch(new BestFirst());
    m_SelectedAttributes = null;
    m_FilterOptions = null;
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 1.9 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain arguments to the filter: use -h for help
   */
  
  public static String readLine(FileInputStream in) throws IOException
	 {
		 String tempstr="";
		 char ch;
		 ch=(char)in.read();
		 while(ch=='\n'||ch=='\r')
		 {
		    ch=(char)in.read();
		 }
	     while (ch!='\n'&&ch!='\r'&&in.available()>0)
		 {
			tempstr=tempstr+ch;
			ch=(char)in.read();
		 }
	    
		 return tempstr;
		 
	 }
  
  
  public static String DelComma(String name) {
	  String a ="_";
	  for(int i=0;i<=name.length();i++){
		  if(name.charAt(i)==','||name.charAt(i)=='-'||name.charAt(i)==' ')
			  a=a.concat("_");
//			  continue;
		  else if(name.charAt(i)=='.')
			  break;
		
		  else a=a.concat(String.valueOf(name.charAt(i)));
	  }
	  a=a.concat(".txt");
	  
	    return a;
	  }
  
  
  
  public static void main(String[] argv) {
		File file = new File("D:/dataset/");
		File[] lf = file.listFiles();
		String str = null;
		double[] pp = new double[4];
		pp[0] = 0.01;
		pp[1] = 0.02;
		pp[2] = 0.05;
		pp[3] = 0.08;
		try {
			FileInputStream in = new FileInputStream("D:/7/NJJbest/Num.txt");
			for (int i = 0; i < lf.length; i++) {
				System.out.println(lf[i].getName());
				str = readLine(in);

				for (int ii = 0; ii < 4; ii++) {
					int nn = (int) (Double.parseDouble(str) * pp[ii]);
					if(nn<1)
						continue;
					String N = String.valueOf(nn);
					AppendToFile.appendMethodA("D:/7/1258ReliefF/Time.txt",
							lf[i].getName() + ",");
					String[] arg = {
							"-E",
							"weka.attributeSelection.ReliefFAttributeEval -M -1 -D 1 -K 10",
							"-S",
							"weka.attributeSelection.Ranker -T -1 -N " + N,
							"-c", "last",

							"-i", "D:/dataset/" + lf[i].getName(), "-o",
							"D:/7/1258ReliefF/" + N + "," + lf[i].getName() };
					long st = System.currentTimeMillis();
					runFilter(new ReliefFbest(), arg);
					long end = System.currentTimeMillis();
					long time = end - st;
					AppendToFile.appendMethodA("D:/7/1258ReliefF/Time.txt",
							time + "\r\n");
				}
			}
		} catch (Exception e) {
		}
	}
}



//  public static void main(String[] argv) {
//		File file = new File("D:/dataset/");
//		File[] lf = file.listFiles();
//		String str = null;
//		for (int i = 0; i < lf.length; i++) {
//			String name = lf[i].getName();
//			AppendToFile.appendMethodA("D:/NJJbest/name.txt", name + "\r\n");
//		}
//	}
//}
  
  




// public static void main(String[] argv) {
//
//	// File file = new File("D:/NJJbest/result/M/");
//	// File file = new File("D:/NJJbest/dataset/ReliefF/");
//	File file = new File("D:/dataset/");
//	File[] lf = file.listFiles();
//	String str = null;
//	// try {
//	//			
//	for (int i = 0; i < lf.length; i++) {
//		String name = lf[i].getName();
//
//		String nametxt = DelComma(name);
//		AppendToFile
//				.appendMethodA(
//						"D:/NJJbest/Re_140_J48.txt",
//						"java -Xmx2048M -cp weka.jar weka.classifiers.trees.J48 -C 0.25 -M 2 -t dataset/"
//								+ name
//								+ " > result/J48/"
//								+ nametxt
//								+ "\r\n");
//
//		// String acc =null;
//		// int f;
//		// FileInputStream in = new
//		// FileInputStream("D:/NJJbest/result/M/"+name);
//		// for(int j=0;j<30000;){
//		// acc = readLine(in);
//		// f=acc.compareTo("=== Stratified cross-validation ===");
//		// if(f==0)
//		// break;
//		// // if(j==15000)
//		// // System.out.println(i);
//		// j++;
//		// }
//		// acc = readLine(in);
//		// // acc = readLine(in);
//		//				
//		// AppendToFile.appendMethodA("D:/NJJbest/Acc.txt", name+","+acc+
//		// ",\r\n");
//		//				
//
//		//			}					
//		//  }catch (Exception e) {
//		//		}
//		//	}
//		//}
//
//		System.out.println(lf[i].getName());
//		//				str = readLine(in);
//
//		for (int ii = 1; ii < 5; ii++) {
//			double n = 0.2 * ii;
//
//			int nn = (int) (Double.parseDouble(str) * n);
//			//					System.out.println("nn= " + nn);
//
//			String N = String.valueOf(nn);
//			//					System.out.println("N= " + N);
//
//			AppendToFile.appendMethodA("D:/7/ReliefF/Time.txt", lf[i]
//					.getName()
//					+ ",");
//
//			String[] arg = {
//					"-E",
//					"weka.attributeSelection.ReliefFAttributeEval -M -1 -D 1 -K 10",
//					"-S", "weka.attributeSelection.Ranker -T -1 -N " + N,
//					"-c", "last",
//
//					"-i", "D:/dataset/" + lf[i].getName(), "-o",
//					"D:/7/ReliefF/" + N + "," + lf[i].getName() };
//			long st = System.currentTimeMillis();
//			runFilter(new ReliefFbest(), arg);
//			long end = System.currentTimeMillis();
//			long time = end - st;
//			AppendToFile.appendMethodA("D:/7/ReliefF/Time.txt", time
//					+ "\r\n");
//		}
//	}
//	//		} catch (Exception e) {
//	//		}
//}
//}

