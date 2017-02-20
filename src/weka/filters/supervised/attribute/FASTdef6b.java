

package weka.filters.supervised.attribute;

import weka.attributeSelection.ASEvaluation;
import weka.attributeSelection.ASSearch;
import weka.attributeSelection.AttributeEvaluator;
import weka.attributeSelection.AttributeTransformer;
import weka.attributeSelection.BestFirst;
import weka.attributeSelection.CfsSubsetEval;
import weka.attributeSelection.Ranker;
import weka.attributeSelection.UnsupervisedAttributeEvaluator;
import weka.attributeSelection.UnsupervisedSubsetEvaluator;
import weka.core.*;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;
import weka.me.graph.Graph;
import weka.me.graph.njjGraph1;
import weka.clusterers.*; 
import weka.AppendToFile;
import weka.fileTest;
import weka.kOfkmeans;
import weka.me.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import java.util.Enumeration;
import java.util.Vector;

/**
 * <!-- globalinfo-start --> A supervised attribute filter that can be used to
 * select attributes. It is very flexible and allows various search and
 * evaluation methods to be combined.
 * <p/>
 * <!-- globalinfo-end -->
 * 
 * <!-- options-start --> Valid options are:
 * <p/>
 * 
 * <pre>
 * -S &lt;"Name of search class [search options]"&gt;
 *  Sets search method for subset evaluators.
 *  eg. -S "weka.attributeSelection.BestFirst -S 8"
 * </pre>
 * 
 * <pre>
 * -E &lt;"Name of attribute/subset evaluation class [evaluator options]"&gt;
 *  Sets attribute/subset evaluator.
 *  eg. -E "weka.attributeSelection.CfsSubsetEval -L"
 * </pre>
 * 
 * <pre>
 * Options specific to evaluator weka.attributeSelection.CfsSubsetEval:
 * </pre>
 * 
 * <pre>
 * -M
 *  Treat missing values as a seperate value.
 * </pre>
 * 
 * <pre>
 * -L
 *  Don't include locally predictive attributes.
 * </pre>
 * 
 * <pre>
 * Options specific to search weka.attributeSelection.BestFirst:
 * </pre>
 * 
 * <pre>
 * -P &lt;start set&gt;
 *  Specify a starting set of attributes.
 *  Eg. 1,3,5-7.
 * </pre>
 * 
 * <pre>
 * -D &lt;0 = backward | 1 = forward | 2 = bi-directional&gt;
 *  Direction of search. (default = 1).
 * </pre>
 * 
 * <pre>
 * -N &lt;num&gt;
 *  Number of non-improving nodes to
 *  consider before terminating search.
 * </pre>
 * 
 * <pre>
 * -S &lt;num&gt;
 *  Size of lookup cache for evaluated subsets.
 *  Expressed as a multiple of the number of
 *  attributes in the data set. (default = 1)
 * </pre>
 * 
 * <!-- options-end -->
 * 
 * @author Mark Hall (mhall@cs.waikato.ac.nz)
 * @version $Revision: 1.9 $
 */
public class FASTdef6b extends Filter implements SupervisedFilter, OptionHandler {

	/** for serialization */
	static final long serialVersionUID = -296211247688169716L;

	/** the attribute selection evaluation object */
	private weka.attributeSelection.AttributeSelection m_trainSelector;

	/** the attribute evaluator to use */
	private ASEvaluation m_ASEvaluator=new CfsSubsetEval();

	/** the search method if any */
	private ASSearch m_ASSearch=new BestFirst();

	/** holds a copy of the full set of valid options passed to the filter */
	private String[] m_FilterOptions;

	/** holds the selected attributes */
	private int[] m_SelectedAttributes;
	
	private int Count1;
	
	private int[]clu ;
	private int del[] ;
	private int[]father ;
	private double[][] suij;
	private double[]suic;
	private boolean[] visited;
	private Instances njjData;
	// ============================================================================
//	public double m_Th;
////	public double Thij;
////
////	//	
//	 public void setmTh (int c){
//		 m_Th=(double)c/(double)100;
//
//	 }
//	 public double getmTh(){
//		 return m_Th;
//	 }
//	 
//	 public void setThij (int c){
//		 Thij=(double)c/(double)100;
//
//	 }
//	public double getThij(){
//		 return Thij;
//		 }
	// public void setThk (int c){
	// Thk=c;
	// }

	// private int kMeans = 5;
	// public void setkMeans(int k){
	// kMeans=k;
	// }
	// public int getkMeans(){
	// return kMeans;
	// }
	/**
	 * Returns a string describing this filter
	 * 
	 * @return a description of the filter suitable for displaying in the
	 *         explorer/experimenter gui
	 */

	public String globalInfo() {

		return "A supervised attribute filter that can be used to select "
				+ "attributes. It is very flexible and allows various search "
				+ "and evaluation methods to be combined.";
	}

	/**
	 * Constructor
	 */
	public FASTdef6b() {

		resetOptions();
	}

	/**
	 * Returns an enumeration describing the available options.
	 * 
	 * @return an enumeration of all the available options.
	 */
	public Enumeration listOptions() {

		Vector newVector = new Vector(6);

		newVector
				.addElement(new Option(
						"\tSets search method for subset evaluators.\n"
								+ "\teg. -S \"weka.attributeSelection.BestFirst -S 8\"",
						"S", 1,
						"-S <\"Name of search class [search options]\">"));

		newVector
				.addElement(new Option(
						"\tSets attribute/subset evaluator.\n"
								+ "\teg. -E \"weka.attributeSelection.CfsSubsetEval -L\"",
						"E", 1,
						"-E <\"Name of attribute/subset evaluation class [evaluator options]\">"));

		if ((m_ASEvaluator != null) && (m_ASEvaluator instanceof OptionHandler)) {
			Enumeration enu = ((OptionHandler) m_ASEvaluator).listOptions();

			newVector.addElement(new Option("", "", 0, "\nOptions specific to "
					+ "evaluator " + m_ASEvaluator.getClass().getName() + ":"));
			while (enu.hasMoreElements()) {
				newVector.addElement((Option) enu.nextElement());
			}
		}

		if ((m_ASSearch != null) && (m_ASSearch instanceof OptionHandler)) {
			Enumeration enu = ((OptionHandler) m_ASSearch).listOptions();

			newVector.addElement(new Option("", "", 0, "\nOptions specific to "
					+ "search " + m_ASSearch.getClass().getName() + ":"));
			while (enu.hasMoreElements()) {
				newVector.addElement((Option) enu.nextElement());
			}
		}
		return newVector.elements();
	}

	/**
	 * Parses a given list of options.
	 * <p/>
	 * 
	 * <!-- options-start --> Valid options are:
	 * <p/>
	 * 
	 * <pre>
	 * -S &lt;"Name of search class [search options]"&gt;
	 *  Sets search method for subset evaluators.
	 *  eg. -S "weka.attributeSelection.BestFirst -S 8"
	 * </pre>
	 * 
	 * <pre>
	 * -E &lt;"Name of attribute/subset evaluation class [evaluator options]"&gt;
	 *  Sets attribute/subset evaluator.
	 *  eg. -E "weka.attributeSelection.CfsSubsetEval -L"
	 * </pre>
	 * 
	 * <pre>
	 * Options specific to evaluator weka.attributeSelection.CfsSubsetEval:
	 * </pre>
	 * 
	 * <pre>
	 * -M
	 *  Treat missing values as a seperate value.
	 * </pre>
	 * 
	 * <pre>
	 * -L
	 *  Don't include locally predictive attributes.
	 * </pre>
	 * 
	 * <pre>
	 * Options specific to search weka.attributeSelection.BestFirst:
	 * </pre>
	 * 
	 * <pre>
	 * -P &lt;start set&gt;
	 *  Specify a starting set of attributes.
	 *  Eg. 1,3,5-7.
	 * </pre>
	 * 
	 * <pre>
	 * -D &lt;0 = backward | 1 = forward | 2 = bi-directional&gt;
	 *  Direction of search. (default = 1).
	 * </pre>
	 * 
	 * <pre>
	 * -N &lt;num&gt;
	 *  Number of non-improving nodes to
	 *  consider before terminating search.
	 * </pre>
	 * 
	 * <pre>
	 * -S &lt;num&gt;
	 *  Size of lookup cache for evaluated subsets.
	 *  Expressed as a multiple of the number of
	 *  attributes in the data set. (default = 1)
	 * </pre>
	 * 
	 * <!-- options-end -->
	 * 
	 * @param options
	 *            the list of options as an array of strings
	 * @throws Exception
	 *             if an option is not supported
	 */
	public void setOptions(String[] options) throws Exception {

		String optionString;
		resetOptions();

		if (Utils.getFlag('X', options)) {
			throw new Exception("Cross validation is not a valid option"
					+ " when using attribute selection as a Filter.");
		}

//		optionString = Utils.getOption('E', options);
//		if (optionString.length() != 0) {
//			optionString = optionString.trim();
//			// split a quoted evaluator name from its options (if any)
//			int breakLoc = optionString.indexOf(' ');
//			String evalClassName = optionString;
//			String evalOptionsString = "";
//			String[] evalOptions = null;
//			if (breakLoc != -1) {
//				evalClassName = optionString.substring(0, breakLoc);
//				evalOptionsString = optionString.substring(breakLoc).trim();
//				evalOptions = Utils.splitOptions(evalOptionsString);
//			}
//			setEvaluator(ASEvaluation.forName(evalClassName, evalOptions));
//		}
//
//		if (m_ASEvaluator instanceof AttributeEvaluator) {
//			setSearch(new Ranker());
//		}
//
//		optionString = Utils.getOption('S', options);
//		if (optionString.length() != 0) {
//			optionString = optionString.trim();
//			int breakLoc = optionString.indexOf(' ');
//			String SearchClassName = optionString;
//			String SearchOptionsString = "";
//			String[] SearchOptions = null;
//			if (breakLoc != -1) {
//				SearchClassName = optionString.substring(0, breakLoc);
//				SearchOptionsString = optionString.substring(breakLoc).trim();
//				SearchOptions = Utils.splitOptions(SearchOptionsString);
//			}
//			setSearch(ASSearch.forName(SearchClassName, SearchOptions));
//		}

		// =========================================================================
//		 String perString2 = Utils.getOption('k', options);
//		 if (perString2.length() == 0)
//		 setmTh(0);
//		 else
//		 setmTh(Integer.valueOf(perString2).intValue());
//		 
//		 String perString1 = Utils.getOption('p', options);
//		 if (perString1.length() == 0)
//		 setThij(0);
//		 else
//		 setThij(Integer.valueOf(perString1).intValue());

		// perString = Utils.getOption('k', options);
		// if (perString.length() == 0)
		// setkMeans(5);
		// else
		// setkMeans(Integer.valueOf(perString).intValue());

		Utils.checkForRemainingOptions(options);
	}

	/**
	 * Gets the current settings for the attribute selection (search, evaluator)
	 * etc.
	 * 
	 * @return an array of strings suitable for passing to setOptions()
	 */
	public String[] getOptions() {
		String[] EvaluatorOptions = new String[0];
		String[] SearchOptions = new String[0];
		int current = 0;

//		if (m_ASEvaluator instanceof OptionHandler) {
//			EvaluatorOptions = ((OptionHandler) m_ASEvaluator).getOptions();
//		}
//
//		if (m_ASSearch instanceof OptionHandler) {
//			SearchOptions = ((OptionHandler) m_ASSearch).getOptions();
//		}

		String[] setOptions = new String[10];
//		setOptions[current++] = "-E";
//		setOptions[current++] = getEvaluator().getClass().getName() + " "
//				+ Utils.joinOptions(EvaluatorOptions);
//
//		setOptions[current++] = "-S";
//		setOptions[current++] = getSearch().getClass().getName() + " "
//				+ Utils.joinOptions(SearchOptions);
//		
//		 setOptions[current++] = "-k";
//		 setOptions[current++] = "" + getmTh();
//		 setOptions[current++] = "-p";
//		 setOptions[current++] = "" + getThij();

		while (current < setOptions.length) {
			setOptions[current++] = "";
		}

		return setOptions;
	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String evaluatorTipText() {

		return "Determines how attributes/attribute subsets are evaluated.";
	}

	/**
	 * set attribute/subset evaluator
	 * 
	 * @param evaluator
	 *            the evaluator to use
	 */
//	public void setEvaluator(ASEvaluation evaluator) {
//		m_ASEvaluator = evaluator;
//	}

	/**
	 * Returns the tip text for this property
	 * 
	 * @return tip text for this property suitable for displaying in the
	 *         explorer/experimenter gui
	 */
	public String searchTipText() {

		return "Determines the search method.";
	}

	/**
	 * Set search class
	 * 
	 * @param search
	 *            the search class to use
	 */
//	public void setSearch(ASSearch search) {
//		m_ASSearch = search;
//	}

	// public void setTh(double search) {
	// m_Th = search;
	// }
	//	
	// public void getTh(double search) {
	// return m_Th;
	// }

	/**
	 * Get the name of the attribute/subset evaluator
	 * 
	 * @return the name of the attribute/subset evaluator as a string
	 */
//	public ASEvaluation getEvaluator() {
//
//		return m_ASEvaluator;
//	}

	/**
	 * Get the name of the search method
	 * 
	 * @return the name of the search method as a string
	 */
//	public ASSearch getSearch() {
//
//		return m_ASSearch;
//	}

	/**
	 * Returns the Capabilities of this filter.
	 * 
	 * @return the capabilities of this object
	 * @see Capabilities
	 */
	public Capabilities getCapabilities() {
		Capabilities result;

		if (m_ASEvaluator == null) {
			result = super.getCapabilities();
		} else {
			result = m_ASEvaluator.getCapabilities();
			// class index will be set if necessary, so we always allow the
			// dataset
			// to have no class attribute set. see the following method:
			// weka.attributeSelection.AttributeSelection.SelectAttributes(Instances)
			result.enable(Capability.NO_CLASS);
		}

		result.setMinimumNumberInstances(0);

		return result;
	}

	/**
	 * Input an instance for filtering. Ordinarily the instance is processed and
	 * made available for output immediately. Some filters require all instances
	 * be read before producing output.
	 * 
	 * @param instance
	 *            the input instance
	 * @return true if the filtered instance may now be collected with output().
	 * @throws IllegalStateException
	 *             if no input format has been defined.
	 * @throws Exception
	 *             if the input instance was not of the correct format or if
	 *             there was a problem with the filtering.
	 */
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
	 * requires all instances prior to filtering, output() may now be called to
	 * retrieve the filtered instances.
	 * 
	 * @return true if there are instances pending output.
	 * @throws IllegalStateException
	 *             if no input structure has been defined.
	 * @throws Exception
	 *             if there is a problem during the attribute selection.
	 */

	public static void njjMP(int a[]) {
		int temp;
		int njjlen = a.length;
		boolean tag = true;
		while (tag) {
			tag = false;
			njjlen--;
			for (int j = 1; j <= njjlen; j++) {
				if (a[j - 1] < a[j]) {
					temp = a[j - 1];
					a[j - 1] = a[j];
					a[j] = temp;
					tag = true;
				}
			}
		}
	}
	
	public static void njjMP(double a[]) {
		double temp;
		int njjlen = a.length;
		boolean tag = true;
		while (tag) {
			tag = false;
			njjlen--;
			for (int j = 1; j <= njjlen; j++) {
				if (a[j - 1] < a[j]) {
					temp = a[j - 1];
					a[j - 1] = a[j];
					a[j] = temp;
					tag = true;
				}
			}
		}
	}
	
	public static void njjMPmin(double a[]) {
		double temp;
		int njjlen = a.length;
		boolean tag = true;
		while (tag) {
			tag = false;
			njjlen--;
			for (int j = 1; j <njjlen; j++) {
				if (a[j - 1] > a[j]) {
					temp = a[j - 1];
					a[j - 1] = a[j];
					a[j] = temp;
					tag = true;
				}
			}
		}
	}

	public static void njjMPiwithC(double[][] a) {
		double[] temp;
		int njjlen = a.length;
		boolean tag = true;
		while (tag) {
			tag = false;
			njjlen--;
			for (int j = 1; j <= njjlen; j++) {
				if (a[j - 1][1] < a[j][1]) {
					temp = a[j - 1];
					a[j - 1] = a[j];
					a[j] = temp;
					tag = true;
				}
			}
		}
	}

	public int njjMin(double a[]) {// a[0] is the min
		int min = 0;
		int k = 0;
		int njjlen = a.length;
		int x = 0;
		while (k < njjlen - 1) {
			if (a[x] < a[x + 1])
				min = x;
			else
				min = x + 1;
			k++;
		}
		return min;
	}

	public void njjputOutDataset(Instances njjData) {
		System.out
				.println("__________________dataset____________________________");
		for (int i = 0; i < 4; i++) {
			// for( int i = 0; i < njjData.numInstances(); i++){
			Instance test = njjData.instance(i);
			for (int kk = 0; kk < test.numAttributes(); kk++) {
				System.out.print("\t" + test.value(kk));
			}
			System.out.println();
		}
		System.out
				.println("__________________dataset____________________________");
	}

	// public void njjcorrelation(Instances njjData,double su[][][],double
	// suu[][]){
	// CfsSubsetEval njjCfs = new CfsSubsetEval();
	// njjCfs.njjsetm_trInstances(njjData);
	//		
	//
	// int ii=0;
	// double tt;
	// for(int i=1;i<njjData.numAttributes()-1;i++){
	// for(int j=0;j<i;j++){
	// su[i][j][0]=njjCfs.symmUncertCorr (i,j);
	//				
	// tt=su[i][j][0];
	//			 
	// su[i][j][1]=0;
	// suu[ii][0]=su[i][j][0];
	// suu[ii][1]=i;
	// suu[ii][2]=j;
	// ++ii;
	// }
	// }
	// }


	public void njjcorrelationQ(Instances njjData, double su[][],int[]ranked,int yuzhi) {
		CfsSubsetEval njjCfs = new CfsSubsetEval();
		njjCfs.njjsetm_trInstances(njjData);
		int ran;
		for (int i = 0; i < yuzhi; i++) {
			for(ran=i+1;ran<yuzhi;ran++){
				su[i][ran] =njjCfs.njjsymmUncertCorr(ranked[i], ranked[ran]);
				su[ran][i]=su[i][ran];
			}
		}
	}
	
	public double[] SUIC(Instances njjData,double[] suic){
		double[] suicc = new double[suic.length];
		CfsSubsetEval njjCfs = new CfsSubsetEval();
		njjCfs.njjsetm_trInstances(njjData);
		int Cno=njjData.numAttributes()-1;
		for (int i = 0; i < Cno; i++) {
			suic[i] = njjCfs.njjsymmUncertCorr(i, Cno);
			suicc[i] = 1-suic[i];
			}
		return suicc;
		
	}
	

	public void njjsort(double suu[][], Instances njjData) {
		int njjFeatureNum = njjData.numAttributes();
		int njjlen = (njjFeatureNum - 1) * (njjFeatureNum - 2) / 2;
		double temp;
		for (int ii = 0; ii < njjlen - 1; ii++) {
			for (int jj = ii + 1; jj < njjlen; jj++) {
				if (suu[ii][0] < suu[jj][0]) {
					temp = suu[ii][0];
					suu[ii][0] = suu[jj][0];
					suu[jj][0] = temp;

					temp = suu[ii][1];
					suu[ii][1] = suu[jj][1];
					suu[jj][1] = temp;

					temp = suu[ii][2];
					suu[ii][2] = suu[jj][2];
					suu[jj][2] = temp;

				}
			}
		}
	}
	
	
	
	public static int[] removeFrom(int[] ranked,int[] del){
		int[] G =new int[ranked.length+1];
		int count=0;
		int j=0;
		
		for(int i =0;i<ranked.length;i++){
			if(ranked[i] > del[j]){
				G[count]= ranked[i];
				count++;
			}
			else if(ranked[i]== del[j]){
				j++;
			}
			else{
				j++;
				i--;
			}				
		}	
		G[G.length-1]=count;
		return G;
	}

	
	public Instances njjInitnewdata(Instances njjData, int G[][], int Count1) {
		int njjFeatureNum = njjData.numAttributes();
		Instances njjnewData = new Instances(njjData);
		int delNum = 0;
		for (int q = 1; q < Count1 + 1; q++) {
			if (G[q][1] != -1) {
				int r = 0;
				while (G[q][r] != -1) {
					delNum++;
					r++;
				}
			}
		}

		int del[] = new int[delNum];
		int t = 0;
		for (int q = 1; q < Count1 + 1; q++) {
			if (G[q][1] != -1) {
				int r = 0;
				while (G[q][r] != -1) {
					del[t] = G[q][r];
					r++;
					t++;
				}
			}
		}
		// System.out.println("delNum="+delNum);

		// for (int tt = 0; tt < del.length; tt++){
		// System.out.print("\t"+del[tt]);
		// }

		// System.out.println();
		njjMP(del);
		// for (int tt = 0; tt < del.length; tt++){
		// System.out.print("\t"+del[tt]);
		// }
		// System.out.println("\n----------------------------");

		for (int i = 0; i < del.length; i++) {
			// System.out.println(del.length);
			njjnewData.deleteAttributeAt(del[i]);
		}
		return njjnewData;
	}

	public Instances njjFeatureSx(int G[][], int q, Instances njjData) {
		int njjFeatureNum = njjData.numAttributes();
		int i = 0;
		while (G[q][i] != -1) {
			i++;
		}
		int att[] = new int[i];
		i = 0;
		while (G[q][i] != -1) {
			att[i] = G[q][i];
			i++;
		}

		// for (int tt = 0; tt < att.length; tt++){
		// System.out.print("\t"+att[tt]);
		// }
		// System.out.println();

		m_SelectedAttributes = att;
		int att1[] = new int[njjFeatureNum];
		for (i = 0; i < njjFeatureNum; i++) {
			att1[i] = -1;
		}
		int ka = 0;
		int njj;
		for (i = njjFeatureNum - 1; i >= 0; i--) {
			for (njj = 0; njj < njjFeatureNum; njj++) {
				if (G[q][njj] == i)
					break;
			}
			if (njj == njjFeatureNum) {
				att1[ka] = i;
				ka++;
			}
		}

		// for (i = 0; i < att1.length; i++){
		// if(att1[i]==-1)
		// break;
		// System.out.print("\t"+att1[i]);
		// }
		// System.out.println("\n----------------------------");
		Instances njjDataB1 = new Instances(njjData);

		for (i = 0; i < njjFeatureNum; i++) {
			if (att1[i] == -1)
				break;
			njjDataB1.njj_deleteAttributeAt(att1[i]);
		}
		return njjDataB1;
	}

	

	public void njjInsertNewdata(Instances njjnewData, int k, int B1[], int q) {
		FastVector B_value = new FastVector();
		for (int i = 0; i < k; i++) {
			B_value.addElement(String.valueOf(i));
		}
		String B_name = "B" + q;
		Attribute BB1 = new Attribute(B_name, B_value);

		njjnewData.insertAttributeAt(BB1, 0);
		for (int i = 0; i < njjnewData.numInstances(); i++) {
			njjnewData.instance(i).setValue(0, B1[i]);
		}
	}

	public int njjGetknewNeighbor(double[][] tem, double[][][] su, int knew,
			int njjFeatureNum) {
		// for each feature do:-------------------------------------
		int cc = 0;
		for (int m = 0; m < njjFeatureNum - 1; m++) {
			// a[][]:one feature's njjFeatureNum-1 neighbors;
			double a[][] = new double[njjFeatureNum - 1][2];
			for (int k = 0; k < m; k++) {
				a[k][0] = su[m][k][0];
				a[k][1] = k;
			}
			for (int k = m + 1; k < njjFeatureNum - 1; k++) {
				a[k][0] = su[k][m][0];
				a[k][1] = k;
			}
			a[m][0] = 0;
			// mp-----------------------------------------------
			double temp1, temp2;
			boolean tag = true;
			int No = njjFeatureNum - 1;
			while (tag) {
				tag = false;
				No--;
				for (int jj = 1; jj <= No; jj++) {
					if (a[jj - 1][0] < a[jj][0]) {
						temp1 = a[jj - 1][0];
						a[jj - 1][0] = a[jj][0];
						a[jj][0] = temp1;
						temp2 = a[jj - 1][1];
						a[jj - 1][1] = a[jj][1];
						a[jj][1] = temp2;
						tag = true;
					}
				}
			}

			// get this feature's knew nearest neighbors;---------
			for (int jj = 0; jj < knew; jj++) {
				tem[cc][0] = a[jj][0];
				tem[cc][1] = m;
				tem[cc][2] = a[jj][1];
				cc++;
			}
		}
		return cc;

	}

	// write the inputFile for
	// hMETIS--------------------------------------------
	public void njjWriteInputFile(String FileName, int bianNo,
			int njjFeatureNum, double[][] tem, int cc) {
		AppendToFile.appendMethodB(FileName, String.valueOf(bianNo) + "\t"
				+ String.valueOf(njjFeatureNum - 1) + "\t" + String.valueOf(1));
		for (int jj = 0; jj < cc; jj++) {
			if (tem[jj][0] != -1) {
				int dis = (int) ((1 - tem[jj][0]) * 10000 + 1);
				AppendToFile.appendMethodA(FileName, "\r\n"
						+ String.valueOf(dis) + "\t");
				if ((int) tem[jj][1] != 0) {
					AppendToFile.appendMethodA(FileName, String
							.valueOf((int) tem[jj][1])
							+ "\t" + String.valueOf((int) tem[jj][2]));
				} else {
					AppendToFile.appendMethodA(FileName, String
							.valueOf((int) tem[jj][2])
							+ "\t" + String.valueOf((int) tem[jj][1]));
				}
			}
		}
	}

	// do shmetis to apate the graph--------------------------------------
	

	public void njjWrite1(String FileName, int njjFeatureNum, double[][][] su) {
		double tem[][] = new double[njjFeatureNum * njjFeatureNum][3];
		// knew nearest neighbor
		int knew = 15;
		int cc;
		cc = njjGetknewNeighbor(tem, su, knew, njjFeatureNum);

		// get rid of duplication edge,such like (1,2)and(2,1)----------
		int bianNo = cc;
		for (int jj = 0; jj < cc - 1; jj++) {
			for (int ii = jj + 1; ii < cc; ii++) {
				if (tem[jj][1] == tem[ii][2] && tem[jj][2] == tem[ii][1]) {
					tem[jj][0] = -1;
					bianNo--;
				}
			}
		}
		njjWriteInputFile(FileName, bianNo, njjFeatureNum, tem, cc);
	}

	// all neighbors
	public void njjWrite2(String FileName, int njjFeatureNum, double[][] su) {
		int dis;
		int x, y;
		int bianNo = (njjFeatureNum) * (njjFeatureNum - 1) / 2;
		AppendToFile.appendMethodB(FileName, String.valueOf(bianNo) + "\t"
				+ String.valueOf(njjFeatureNum) + "\t" + String.valueOf(1));

		try {
			RandomAccessFile randomFile = new RandomAccessFile(FileName, "rw");
			long fileLength = randomFile.length();

			randomFile.seek(fileLength);
			for (x = 1; x < njjFeatureNum; x++)
				for (y = 0; y < x; y++) {
					dis = (int) (su[x][y] * 10000 + 1);
					randomFile.writeBytes("\r\n" + String.valueOf(dis) + "\t"
							+ String.valueOf(x) + "\t" + String.valueOf(y));
				}
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * for(x=1;x<njjFeatureNum-1;x++) for(y=0;y<x;y++){
	 * dis=(int)((1-su[x][y][0])*10000+1); AppendToFile.appendMethodA(FileName,
	 * "\r\n"
	 * +String.valueOf(dis)+"\t"+String.valueOf(x)+"\t"+String.valueOf(y)); }
	 */

	public void njjWrite3(String FileName, int njjInstanceNumBX, double[][][] su) {
		int dis;
		int x, y;
		int bianNo = (njjInstanceNumBX) * (njjInstanceNumBX - 1) / 2;
		// AppendToFile.appendMethodB(FileName,
		// String.valueOf(bianNo)+"\t"+String.valueOf(njjInstanceNumBX)+"\t"+String.valueOf(1));
		try {
			//
			FileWriter writer = new FileWriter(FileName, false);
			writer.write(String.valueOf(bianNo) + "\t"
					+ String.valueOf(njjInstanceNumBX) + "\t"
					+ String.valueOf(1));
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			RandomAccessFile randomFile = new RandomAccessFile(FileName, "rw");
			long fileLength = randomFile.length();
			randomFile.seek(fileLength);

			for (x = 1; x < njjInstanceNumBX; x++)
				for (y = 0; y < x; y++) {
					dis = (int) ((su[x][y][0]) * 10000 + 1);
					// AppendToFile.appendMethodA(FileName,
					// "\r\n"+String.valueOf(dis)+"\t"+String.valueOf(x)+"\t"+String.valueOf(y));
					randomFile.writeBytes("\r\n" + String.valueOf(dis) + "\t"
							+ String.valueOf(x) + "\t" + String.valueOf(y));
				}
			randomFile.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public void njjIncountV(int[] countV,int V,int num){
	// for(int i=0;i<)
	// }
	protected void setOutputFormatnjj(int[] G,int Count1) throws Exception {
	    Instances informat;

	
	    FastVector attributes = new FastVector(Count1+1);

	    int i;

	      informat = njjData;
	    

	    for (i=0;i < Count1+1;i++) {
	      attributes.
		addElement(informat.attribute(G[i]).copy());
	    }

	    Instances outputFormat = 
	      new Instances(getInputFormat().relationName()+String.valueOf(Count1), attributes, 0);


	    if (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator) &&
		!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) {
	      outputFormat.setClassIndex(Count1);
	    }
	    
	    setOutputFormat(outputFormat);  
	  }
	
	protected void convertInstancenjj(Instance instance,int[] G,int Count1) throws Exception {
	    double[] newVals = new double[getOutputFormat().numAttributes()];

	    
	      for (int i = 0; i < Count1+1; i++) {
		int current = G[i];
		newVals[i] = instance.value(current);
	      }
	    
	    if (instance instanceof SparseInstance) {
	      push(new SparseInstance(instance.weight(), newVals));
	    } else {
	      push(new Instance(instance.weight(), newVals));
	    }
	  }
	
	
	
	public  void BFS4(int i, njjGraph1 tree,  int[] ranked, double[] suic) {
		visited[i]=true;
		double suij;
		
		for (int j = 0; j < tree.adjList.elementAt(i).size(); j++) { // 3
			int jj = tree.adjList.elementAt(i).elementAt(j).v2;	
			suij= tree.adjList.elementAt(i).elementAt(j).weight;
			if (!visited[jj]) { // 4
				visited[jj] = true;
				if (suij > suic[ranked[jj]] && suic[ranked[i]]>= suic[ranked[jj]]) {	
					
						del[Count1]=ranked[jj];
						Count1++;
				}
					else if(suij > suic[ranked[i]] && suic[ranked[jj]]>= suic[ranked[i]]){
						del[Count1]= ranked[i];
					Count1++;
				}
				BFS4(jj, tree, ranked, suic);
				
			}
		}
	}
	

	public boolean batchFinished() throws Exception {

		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}

		if (!isOutputFormatDefined()) {
			
			Instances data = getInputFormat(); //input the train dataset
			int feature =data.numAttributes();
			Instances njjData1;
			
			Filter m_Filter = new weka.filters.supervised.attribute.Discretize();
			m_Filter.setInputFormat(data); 	
			njjData1 = Filter.useFilter(data, m_Filter);  //discretize			
			
			Filter m_Filter1 = new weka.filters.unsupervised.attribute.RemoveUseless();
			m_Filter1.setInputFormat(njjData1); 
			njjData = Filter.useFilter(njjData1, m_Filter1);  //removeUseless;
			
			int njjFeatureNum = njjData.numAttributes() - 1;		
		
			
			int yuzhi;
			double yuzhi1,yuzhi2;
			yuzhi1=Math.log(njjFeatureNum);
			yuzhi2=Math.log(2);
			yuzhi=(int)((double)njjFeatureNum*yuzhi2/yuzhi1);  //filter yuzhi=N/logN base 2
	
			suic=new double[njjFeatureNum];
			
			double[] suicc = new double[njjFeatureNum];
			suicc = SUIC(njjData,suic);                  
			int[] ranked = Utils.sort(suicc);      //rank the SU with c, SU from high to low
			
//	Utils.sort(A); from the Min.A to Max.A, and return the index of A;		
			
			
			double yuzhi1SU = suic[ranked[yuzhi]];
												
			suij = new double[yuzhi][yuzhi];	
			njjcorrelationQ(njjData,suij,ranked,yuzhi);	   //Complete graph
			
			njjGraph1 njjg = new njjGraph1();
//			double[] a=new double[yuzhi];
//			double []suijave=new double[1];
			njjGraph1 tree = njjg.PrimMaxTree(suij, 0);	
//			
			
			
			
			visited=new boolean [yuzhi];

			del = new int[yuzhi+1];
			for(int i=0;i<yuzhi+1;i++)
				del[i]= -1;
					
			Count1=0;
	
			BFS4(0,tree,ranked,suic);
			
			
			
//			int[] rankeddel = Utils.sort(del); 
			int[] yuzhiAtr =new int[yuzhi];
			for(int i=0;i<yuzhi;i++){
				yuzhiAtr[i]=ranked[i];
			}
			
			njjMP(del);
//			for(int i=0;i<del.length;i++){
//				System.out.print(del[i]+"\t");
//			}
//			System.out.print("\n");
			
			njjMP(yuzhiAtr);
//			for(int i=0;i<yuzhiAtr.length;i++){
//				System.out.print(yuzhiAtr[i]+"\t");
//			}
			
			
			int[] retained = new int [yuzhi+1];
			retained = removeFrom(yuzhiAtr,del);
			
			int sf = retained[retained.length-1];
			retained[sf] = njjFeatureNum;
			
			
			
			
			
			
			


			String name = data.relationName();			
			setOutputFormatnjj(retained, sf);
		
			m_OutputFormat.setRelationName("FASTdef6b,"+ data.relationName()+","+yuzhi1SU+","  );
			for (int i = 0; i <njjData.numInstances(); i++) {
				convertInstancenjj(njjData.instance(i), retained, sf);
			}
			AppendToFile.appendMethodA("D:/FASTdef6b/Time.txt", ","+data.relationName()+","+data.numAttributes()+","+njjData.numAttributes()+","+yuzhi+","+sf+","+data.numInstances()+","+data.numClasses()+","+yuzhi1SU+",");

			flushInput();
		}
		m_NewBatch = true;
		return (numPendingOutput() != 0);

		// ************************
	}

	/**
	 * Set the output format. Takes the currently defined attribute set
	 * m_InputFormat and calls setOutputFormat(Instances) appropriately.
	 * 
	 * @throws Exception
	 *             if something goes wrong
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
			informat = ((AttributeTransformer) m_ASEvaluator).transformedData();
		} else {
			informat = getInputFormat();
		}

		for (i = 0; i < m_SelectedAttributes.length; i++) {
			attributes.addElement(informat.attribute(m_SelectedAttributes[i])
					.copy());
		}

		Instances outputFormat = new Instances(getInputFormat().relationName(),
				attributes, 0);

		if (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator)
				&& !(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) {
			outputFormat.setClassIndex(m_SelectedAttributes.length - 1);
		}

		setOutputFormat(outputFormat);
	}

	/**
	 * Convert a single instance over. Selected attributes only are transfered.
	 * The converted instance is added to the end of the output queue.
	 * 
	 * @param instance
	 *            the instance to convert
	 * @throws Exception
	 *             if something goes wrong
	 */
	// Instances njjDataB1=null;

	protected void convertInstance(Instance instance) throws Exception {
		double[] newVals = new double[getOutputFormat().numAttributes()];
		for (int i = 0; i < m_SelectedAttributes.length; i++) {
			int current = m_SelectedAttributes[i];
			newVals[i] = instance.value(current);
		}

		if (instance instanceof SparseInstance) {
			push(new SparseInstance(instance.weight(), newVals));
		} else {
			push(new Instance(instance.weight(), newVals));
			// njjDataB1.add(new Instance(instance.weight(), newVals));
		}
	}

	/**
	 * set options to their default values
	 */
	protected void resetOptions() {

		m_trainSelector = new weka.attributeSelection.AttributeSelection();
//		setEvaluator(new CfsSubsetEval());
//		setSearch(new BestFirst());
		m_SelectedAttributes = null;
		m_FilterOptions = null;
	}

	/**
	 * Returns the revision string.
	 * 
	 * @return the revision
	 */
	public String getRevision() {
		return RevisionUtils.extract("$Revision: 1.9 $");
	}

	/**
	 * Main method for testing this class.
	 * 
	 * @param argv
	 *            should contain arguments to the filter: use -h for help
	 */
	public static void main(String[] argv) {
		File file = new File("D:/dataset/");
		AppendToFile.appendMethodB("D:/FASTdef6b/Time.txt", " ");
		File[] lf = file.listFiles();
		for (int i = 0; i < lf.length; i++) {
			System.out.println(lf[i].getName());
			String[] arg = {
//					"-E", "weka.attributeSelection.CfsSubsetEval -L",
//					"-E", "weka.attributeSelection.SymmetricalUncertAttributeSetEval ",
//					"-S", "weka.attributeSelection.BestFirst -S 8",
					"-c", "last",

					"-i", "D:/dataset/" + lf[i].getName(), 
					"-o", "D:/FASTdef6b/" + lf[i].getName()
			};
			AppendToFile.appendMethodA("D:/FASTdef6b/Time.txt", lf[i].getName());
			long st =System.currentTimeMillis();
			runFilter(new FASTdef6b(), arg);
			long end =System.currentTimeMillis();
			long time=end-st;
			AppendToFile.appendMethodA("D:/FASTdef6b/Time.txt", time + "\r\n");
		}
	}

	public int[] getM_SelectedAttributes() {
		return m_SelectedAttributes;
	}

	public void setM_SelectedAttributes(int[] selectedAttributes) {
		m_SelectedAttributes = selectedAttributes;
	}
}
