
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


public class NEXTa extends Filter implements SupervisedFilter, OptionHandler {

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
	public NEXTa() {

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
		if(x==1)
			return a[0];
		if(x==2)
			return a[0];
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
		if(x==1)
			return a[0];
		if(x==2)
			return a[0];
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

	    
	      for (int i = 0; i < clusterNum; i++) {
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
			
			Instances data1 = getInputFormat();  //input the original data set
			int A=data1.numAttributes();

			Instances data;			
			Filter m_Filter1 = new weka.filters.unsupervised.attribute.RemoveUseless();
			m_Filter1.setInputFormat(data1); 
			data = Filter.useFilter(data1, m_Filter1);  //removeUseless;
			int B=data.numAttributes();
			
			
//			Instances data;			
//			Filter m_Filter1 = new weka.filters.unsupervised.attribute.RemoveUseless();
//			m_Filter1.setInputFormat(data1); 
//			data = Filter.useFilter(data1, m_Filter1);  //removeUseless;
//			int B=data.numAttributes();
			
			
			
			
			
			int N =data.numAttributes()-1 ;  // numAttributes without class
			
			
			
			int S = 10;   //seeds
			int x = 8;	 // each seeds include x attributes
			
			
			
			if(N<x || N<S)
				return false;   // the given data set is too small to run this algorithm
			
			AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", data.numInstances()+","+N+",");
//			***********************************************************************************************************************************************************************
			
			//----separate the numerical attributes with the nominal attributes 
			boolean isNum=false;
			boolean isNom=false;
			
			int[] AtrNum = new int[N];
			int[] AtrNom = new int[N];
			
			int CountNum=0;
			int CountNom=0;
			
			for(int i =0;i<N;i++){
				isNum = data.attribute(i).isNumeric();
				if(isNum){
					AtrNum[CountNum]= i;
					CountNum++;
				}
				else{
					AtrNom[CountNom]= i;
					CountNom++;
				}
			}
			//-----end
			
			int yNum ;
			int yNom ;			
			int y=0 ;
			
			int [][]seedsNum;
			int[][] fenNum;			
			int yyNum = 0;
			
			int [][]seedsNom;
			int[][] fenNom;			
			int yyNom = 0;
			
			double maxPearson = 0;
			int maxID=0;
			double tempPearson;
			double[] avePearson = new double[x];
			
			double maxSU = 0;
			double tempSU;
			double[] aveSU = new double[x];
			
			boolean flag;
			int[]GG;
			int weiba=0;
			
//----Nominal data set			
			if((CountNum < x || CountNum < S) && (CountNom >= x && CountNom >= S)){
				
				AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", "Nom"+","+S+","+x+",");
				
				yNom = (CountNom-1)/x + 1;  // Number of Clusters
				seedsNom = new int[S][x+1];   //Clustering
				fenNom = new int[yNom][x+1];   //Clustered
				
				flag = false;
				
				//---pith the first S attributes as seeds
				for(int i=0;i<S;i++)
				{
					seedsNom[i][0]=AtrNom[i];  //the seed
					seedsNom[i][x]=1;    // count
				}
				
				
				//---clustering the rest attributes		
				for(int i=S;i<CountNom;i++){
					if(flag){                   // add new seed;
						seedsNom[maxID][0]= AtrNom[i];
						seedsNom[maxID][x]= 1;
						flag = false;
					}
					
					else{
						maxSU=0;
						for(int ii=0;ii<S;ii++){	
							tempSU = SU(AtrNom[i],seedsNom[ii][0],data);
							if(tempSU > maxSU){
								maxSU = tempSU;
								maxID = ii;
							}
						}
						seedsNom[maxID][seedsNom[maxID][x]]= AtrNom[i];
						seedsNom[maxID][x]++;
						if(seedsNom[maxID][x]==x){					
							flag = true;
							for(int iii = 0; iii < x; iii++){				
								fenNom[yyNom][iii]=seedsNom[maxID][iii];
							}
							seedsNom[maxID][x]=0;
							yyNom++;
						}
					}				
				}
				
			
				//deal the attributes in seeds which is not reach x
				if (yyNom != yNom) {
					int count = 0;
					for (int ii = 0; ii < S; ii++) {
						for (int i = 0; i < seedsNom[ii][x]; i++) {
							if (count != x) {
								fenNom[yyNom][count] = seedsNom[ii][i];
								count++;
							} else {
								yyNom++;
								count = 0;
								fenNom[yyNom][count] = seedsNom[ii][i];
								count++;
							}
						}
					}
					weiba=count;
				}
					
				
				//----- chose the represent feature for each cluster.
				for(int i= 0; i< (yNom-1); i++){
					fenNom[i][x]=CHOSEsu(fenNom[i],x,data);					
				}
				fenNom[yNom-1][x]= CHOSEsu(fenNom[yNom-1],weiba,data);
				
				GG = new int[yNom+1];
				GG[yNom] = N;
				for (int i = 0; i < yNom; i++) {
					GG[i] = fenNom[i][x];
				}
				y=yNom;
			}
				
		
			
//-------------------------------------------------------------------------------
			else if ((CountNom < x||CountNom < S) && (CountNum >=x&&CountNum >=S)) {
				AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", "Num"+","+S+","+x+",");
			
				yNum = (CountNum - 1) / x + 1;
				seedsNum = new int[S][x + 1];
				fenNum = new int[yNum][x + 1];

				flag = false;
				for (int i = 0; i < S; i++) {
					seedsNum[i][0] = AtrNum[i];
					seedsNum[i][x] = 1;
				}

				for (int i = S; i < CountNum; i++) {
					if (flag) { // add new seed;
						seedsNum[maxID][0] = AtrNum[i];
						seedsNum[maxID][x] = 1;
						flag = false;
					}

					else {
						maxPearson = 0;
						for (int ii = 0; ii < S; ii++) {
							tempPearson = Pearson(AtrNum[i], seedsNum[ii][0],
									data);
							if (tempPearson > maxPearson) {
								maxPearson = tempPearson;
								maxID = ii;
							}
						}
						seedsNum[maxID][seedsNum[maxID][x]] = AtrNum[i];
						seedsNum[maxID][x]++;
						if (seedsNum[maxID][x] == x) {
							flag = true;
							for (int iii = 0; iii < x; iii++) {
								fenNum[yyNum][iii] = seedsNum[maxID][iii];
							}
							seedsNum[maxID][x]=0;
							yyNum++;
						}
					}
				}

				weiba = 0;
				if (yyNum != yNum) {
					int count = 0;
					for (int ii = 0; ii < S; ii++) {
						for (int i = 0; i < seedsNum[ii][x]; i++) {
							if (count != x) {
								fenNum[yyNum][count] = seedsNum[ii][i];
								count++;
							} else {
								yyNum++;
								count = 0;
								fenNum[yyNum][count] = seedsNum[ii][i];
								count++;
							}
						}
					}
					weiba = count;
				}
				for (int i = 0; i < (yNum - 1); i++) {
					fenNum[i][x] = CHOSEpearson(fenNum[i], x, data);
				}
				fenNum[yNum - 1][x] = CHOSEpearson(fenNum[yNum - 1], weiba,
						data);

				GG = new int[yNum+1 ];
				GG[yNum] = N;
				for (int i = 0; i < yNum; i++) {
					GG[i] = fenNum[i][x];
				}
				y=yNum;
			}
//---------------------------------------------------------
			
			

			
//-------------------------------------------------------------------
			else if( (CountNum >=x&&CountNum >=S)&&(CountNom >=x&&CountNom >=S)) {
				AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", "Mix"+","+S+","+x+",");
				yNom = (CountNom-1)/x + 1;
				seedsNom = new int[S][x+1];
				fenNom = new int[yNom][x+1];
				
				flag = false;			
				for(int i=0;i<S;i++)
				{
					seedsNom[i][0]=AtrNom[i];
					seedsNom[i][x]=1;
				}
				
				for(int i=S;i<CountNom;i++){
					if(flag){                   // add new seed;
						seedsNom[maxID][0]= AtrNom[i];
						seedsNom[maxID][x]= 1;
						flag = false;
					}
					
					else{
						maxSU=0;
						for(int ii=0;ii<S;ii++){	
							tempSU = SU(AtrNom[i],seedsNom[ii][0],data);
							if(tempSU > maxSU){
								maxSU = tempSU;
								maxID = ii;
							}
						}
						seedsNom[maxID][seedsNom[maxID][x]]= AtrNom[i];
						seedsNom[maxID][x]++;
						if(seedsNom[maxID][x]==x){					
							flag = true;
							for(int iii = 0; iii < x; iii++){				
								fenNom[yyNom][iii]=seedsNom[maxID][iii];
							}
							seedsNom[maxID][x]=0;
							yyNom++;
						}
					}				
				}
				
			
				if (yyNom != yNom) {
					int count = 0;
					for (int ii = 0; ii < S; ii++) {
						for (int i = 0; i < seedsNom[ii][x]; i++) {
							if (count != x) {
								fenNom[yyNom][count] = seedsNom[ii][i];
								count++;
							} else {
								yyNom++;
								count = 0;
								fenNom[yyNom][count] = seedsNom[ii][i];
								count++;
							}
						}
					}
					weiba=count;
				}
					
				for(int i= 0; i< (yNom-1); i++){
					fenNom[i][x]=CHOSEsu(fenNom[i],x,data);					
				}
				fenNom[yNom-1][x]= CHOSEsu(fenNom[yNom-1],weiba,data);
				int[]GGnom = new int[yNom];		
				for(int i=0;i<yNom;i++){
					GGnom[i]=fenNom[i][x];
				}
				
				//----
				yNum = (CountNum - 1) / x + 1;
				seedsNum = new int[S][x + 1];
				fenNum = new int[yNum][x + 1];

				flag = false;
				for (int i = 0; i < S; i++) {
					seedsNum[i][0] = AtrNum[i];
					seedsNum[i][x] = 1;
				}

				for (int i = S; i < CountNum; i++) {
					if (flag) { // add new seed;
						seedsNum[maxID][0] = AtrNum[i];
						seedsNum[maxID][x] = 1;
						flag = false;
					}

					else {
						maxPearson = 0;
						for (int ii = 0; ii < S; ii++) {
							tempPearson = Pearson(AtrNum[i], seedsNum[ii][0],
									data);
							if (tempPearson > maxPearson) {
								maxPearson = tempPearson;
								maxID = ii;
							}
						}
						seedsNum[maxID][seedsNum[maxID][x]] = AtrNum[i];
						seedsNum[maxID][x]++;
						if (seedsNum[maxID][x] == x) {
							flag = true;
							for (int iii = 0; iii < x; iii++) {
								fenNum[yyNum][iii] = seedsNum[maxID][iii];
							}
							seedsNum[maxID][x]=0;
							yyNum++;
						}
					}
				}

				weiba = 0;
				if (yyNum != yNum) {
					int count = 0;
					for (int ii = 0; ii < S; ii++) {
						for (int i = 0; i < seedsNum[ii][x]; i++) {
							if (count != x) {
								fenNum[yyNum][count] = seedsNum[ii][i];
								count++;
							} else {
								yyNum++;
								count = 0;
								fenNum[yyNum][count] = seedsNum[ii][i];
								count++;
							}
						}
					}
					weiba = count;
				}
				for (int i = 0; i < (yNum - 1); i++) {
					fenNum[i][x] = CHOSEpearson(fenNum[i], x, data);
				}
				fenNum[yNum - 1][x] = CHOSEpearson(fenNum[yNum - 1], weiba,
						data);
				
				int[]GGnum = new int[yNum];		
				for(int i=0;i<yNum;i++){
					GGnum[i]=fenNum[i][x];
				}
				
				
				//---
				y = yNum+yNom;
				GG = new int[y+1];
				GG[y] = N;
				for(int i=0;i<yNum;i++){
					GG[i]=GGnum[i];
				}
				
				for(int i=0;i<yNom;i++){
					GG[i+yNum]=GGnom[i];
				}
				
				
			}
			
			
			else {
				AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", "FALSE!"+",");
				return false;
			}
			
			
			
			
			
			
			
			
			
			
			
			//============================================================
			setOutputFormatnjj(GG, y, data);
		
			m_OutputFormat.setRelationName(y+",NEXTa,"+ data.numAttributes()+ "," +data.relationName() );
			// Convert pending input instances
			for (int i = 0; i <data.numInstances(); i++) {
				convertInstancenjj(data.instance(i), GG, y);
			}
			AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", y+",");

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
	    

	    for (i=0;i < clusterNum;i++) {
	      attributes.
		addElement(informat.attribute(G[i]).copy());
	    }

	    Instances outputFormat = 
	      new Instances(getInputFormat().relationName()+String.valueOf(clusterNum), attributes, 0);


	    if (!(m_ASEvaluator instanceof UnsupervisedSubsetEvaluator) &&
		!(m_ASEvaluator instanceof UnsupervisedAttributeEvaluator)) {
	      outputFormat.setClassIndex(-1);
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
		AppendToFile.appendMethodB("D:/NEXTaResult/Time.txt", "name,\tI,\tF,\tcatg,\tS,\tx,\tnewF,\tFenTime\r\n");
		File[] lf = file.listFiles();
		for (int i = 0; i < lf.length; i++) {
			System.out.println(lf[i].getName());
			String[] arg = {
//					"-E", "weka.attributeSelection.CfsSubsetEval -L",
//					"-E", "weka.attributeSelection.SymmetricalUncertAttributeSetEval ",
//					"-S", "weka.attributeSelection.BestFirst -S 8",
					"-c", "last",

					"-i", "D:/dataset/" + lf[i].getName(), 
					"-o", "D:/datasetEM/" + lf[i].getName()
			};
			AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", lf[i].getName()+",");
			long st =System.currentTimeMillis();
			runFilter(new NEXTa(), arg);
			long end =System.currentTimeMillis();
			long time=end-st;
			AppendToFile.appendMethodA("D:/NEXTaResult/Time.txt", time + "\r\n");
		}
	}

	public int[] getM_SelectedAttributes() {
		return m_SelectedAttributes;
	}

	public void setM_SelectedAttributes(int[] selectedAttributes) {
		m_SelectedAttributes = selectedAttributes;
	}
}

