
/*
 * njj 2011-3-16
 * feature selection for clustering(have not use the class label)
 * only can deal with numerical data sets.
 */

package weka.filters.supervised.attribute;

import weka.Correlation.CorrelationCoefficient;
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
import weka.Correlation.*;
import weka.MyMath.*;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;

import java.util.Enumeration;
import java.util.Vector;


public class Fen2 extends Filter implements SupervisedFilter, OptionHandler {

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
	

	public String globalInfo() {

		return "A supervised attribute filter that can be used to select "
				+ "attributes. It is very flexible and allows various search "
				+ "and evaluation methods to be combined.";
	}

	/**
	 * Constructor
	 */
	public Fen2() {

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


		String[] setOptions = new String[10];

		while (current < setOptions.length) {
			setOptions[current++] = "";
		}

		return setOptions;
	}

	public String evaluatorTipText() {

		return "Determines how attributes/attribute subsets are evaluated.";
	}

	/**
	 * set attribute/subset evaluator
	 * 
	 * @param evaluator
	 *            the evaluator to use
	 */

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

	/**
	 * Get the name of the attribute/subset evaluator
	 * 
	 * @return the name of the attribute/subset evaluator as a string
	 */

	/**
	 * Get the name of the search method
	 * 
	 * @return the name of the search method as a string
	 */

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
	
	

	


	
	public double SU(int a, int b, Instances njjData){
		CfsSubsetEval njjCfs = new CfsSubsetEval();
		njjCfs.njjsetm_trInstances(njjData);
		double su = njjCfs.njjsymmUncertCorr(a,b);
		return 	su;
	}
	
	public double Pearson(int a, int b, Instances njjData){
		
		double[] aa=njjData.attributeToDoubleArray(a);
		double[] bb=njjData.attributeToDoubleArray(b);
		CorrelationCoefficient njj = new CorrelationCoefficient();		
		double result = njj.PearsonProductMomentCoefficient(aa,bb);
		if (result<0)
			result = -result;
        return result;
	}
	
	public int CHOSEsu(int[] a,int x, Instances njjData){
		int b=0;
		double[] ave= new double [x];
		double sum =0;
		for(int i= 0;i< x;i++){
			sum=0;
			for(int ii=0;ii< x;ii++){
				if(i!=ii){
					sum=sum+SU(a[i],a[ii],njjData);
				}
				else
					continue;
			}
			ave[i]=sum/(double)(x-1);
		}
		
		double max=0;
		int maxID=0;
		
		for(int i=0;i<x;i++){
			if(ave[i]> max){
				max = ave[i];
				maxID=i;
			}
		}			
		return a[maxID];
	}
	
	public int CHOSEpearson(int[] a,int x, Instances njjData){
		int b=0;
		double[] ave= new double [x];
		double sum =0;
		for(int i= 0;i< x;i++){
			sum=0;
			for(int ii=0;ii< x;ii++){
				if(i!=ii){
					sum=sum+Pearson(a[i],a[ii],njjData);
				}
				else
					continue;
			}
			ave[i]=sum/(double)(x-1);
		}
		
		double max=0;
		int maxID=0;
		
		for(int i=0;i<x;i++){
			if(ave[i]> max){
				max = ave[i];
				maxID=i;
			}
		}			
		return a[maxID];
	}
	

	

	
	

	// write the inputFile for
	// hMETIS--------------------------------------------
	

	// do shmetis to apate the graph--------------------------------------
	

	

	// all neighbors
	

	

	
	protected void convertInstancenjj(Instance instance,int[] G,int clusterNum) throws Exception {
	    double[] newVals = new double[getOutputFormat().numAttributes()];

	    
	      for (int i = 0; i < clusterNum+1; i++) {
		int current = G[i];
		newVals[i] = instance.value(current);
	      }
	    
	    if (instance instanceof SparseInstance) {
	      push(new SparseInstance(instance.weight(), newVals));
	    } else {
	      push(new Instance(instance.weight(), newVals));
	    }
	  }
	
	
	

	public boolean batchFinished() throws Exception {

		if (getInputFormat() == null) {
			throw new IllegalStateException("No input instance format defined");
		}

		if (!isOutputFormatDefined()) {
			
			Instances data = getInputFormat();  //input the train dataset
			int N =data.numAttributes()-1 ;			
				
			//2011-3-15____________________________________________
			int S = 5;
			int x = 5;
			int y = (N-1)/x + 1;
			
			int [][]seeds = new int[S][x+1];
			int[][] fen = new int[y][x+1];
			
			int yy = 0;
			
			double maxPearson = 0;
			int maxID=0;
			double tempPearson;
			double[] avePearson = new double[x];
			
			boolean flag = false;
			
			for(int i=0;i<S;i++)
			{
				seeds[i][0]=i;
				seeds[i][x]=1;
			}
			
			for(int i=S;i<N;i++){
				if(flag){                   // add new seed;
					seeds[maxID][0]= i;
					seeds[maxID][x]= 1;
					flag = false;
				}
				
				else{
					maxPearson=0;
					for(int ii=0;ii<S;ii++){	
//						int a = seeds[ii][0];
						
						
						tempPearson = Pearson(i,seeds[ii][0],data);
						if(tempPearson > maxPearson){
							maxPearson = tempPearson;
							maxID = ii;
						}
					}
					seeds[maxID][seeds[maxID][x]]= i;
					seeds[maxID][x]++;
					if(seeds[maxID][x]==x){					
						flag = true;
						for(int iii = 0; iii < x; iii++){				
							fen[yy][iii]=seeds[maxID][iii];
						}
						seeds[maxID][x]=0;
						yy++;
					}
				}				
			}
			
			int weiba=0;
			if (yy != y) {
				int count = 0;
				for (int ii = 0; ii < S; ii++) {
					for (int i = 0; i < seeds[ii][x]; i++) {
						if (count != x) {
							fen[yy][count] = seeds[ii][i];
							count++;
						} else {
							yy++;
							count = 0;
							fen[yy][count] = seeds[ii][i];
							count++;
						}
					}
				}
				weiba=count;
			}
			
			
				
				//===========================
				
			for(int i= 0; i< (y-1); i++){
				fen[i][x]=CHOSEpearson(fen[i],x,data);					
			}
			fen[y-1][x]= CHOSEpearson(fen[y-1],weiba,data);
			
			
			
			//2011-3-15------------------------------------------------
			
			int[]GG = new int[y+1];
			GG[y] = N;
			for(int i=0;i<y;i++){
				GG[i]=fen[i][x];
			}
			setOutputFormatnjj(GG, y, data);
		
			m_OutputFormat.setRelationName(y+",Fen2,"+ data.numAttributes()+ "," +data.relationName() );
			// Convert pending input instances
			for (int i = 0; i <data.numInstances(); i++) {
				convertInstancenjj(data.instance(i), GG, y);
			}
			AppendToFile.appendMethodA("D:/fen2Result/Time.txt", ","+y+",");

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
	
	protected void setOutputFormatnjj(int[] G,int clusterNum, Instances njjData) throws Exception {
	    Instances informat;

	
	    FastVector attributes = new FastVector(clusterNum+1);

	    int i;

	      informat = njjData;
	    

	    for (i=0;i < clusterNum+1;i++) {
	      attributes.
		addElement(informat.attribute(G[i]).copy());
	    }

	    Instances outputFormat = 
	      new Instances(getInputFormat().relationName()+String.valueOf(clusterNum), attributes, 0);


	    if (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator) &&
		!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) {
	      outputFormat.setClassIndex(clusterNum);
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
		AppendToFile.appendMethodB("D:/fen2Result/Time.txt", " ");
		File[] lf = file.listFiles();
		for (int i = 0; i < lf.length; i++) {
			System.out.println(lf[i].getName());
			String[] arg = {
//					"-E", "weka.attributeSelection.CfsSubsetEval -L",
//					"-E", "weka.attributeSelection.SymmetricalUncertAttributeSetEval ",
//					"-S", "weka.attributeSelection.BestFirst -S 8",
					"-c", "last",

					"-i", "D:/dataset/" + lf[i].getName(), 
					"-o", "D:/fen2Result/" + lf[i].getName()
			};
			AppendToFile.appendMethodA("D:/fen2Result/Time.txt", lf[i].getName());
			long st =System.currentTimeMillis();
			runFilter(new Fen2(), arg);
			long end =System.currentTimeMillis();
			long time=end-st;
			AppendToFile.appendMethodA("D:/fen2Result/Time.txt", time + "\r\n");
		}
	}

	public int[] getM_SelectedAttributes() {
		return m_SelectedAttributes;
	}

	public void setM_SelectedAttributes(int[] selectedAttributes) {
		m_SelectedAttributes = selectedAttributes;
	}
}

