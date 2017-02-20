package weka.filters.szb;
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
 *    UnderSample.java
 *    Copyright (C) 2002 University of Waikato, Hamilton, New Zealand
 *
 */
import weka.core.Capabilities;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.SupervisedFilter;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/** 
 * Under sample the data with the given sample size percent for the given class (default for two class data)
 * if  m_InvertSelection is true, undersample from the first class
 * if m_InvertSection is false, undersample from the second class
 * 
 <!-- options-end -->
 *
 * @author Len Trigg (len@reeltwo.com)
 * @author FracPete (fracpete at waikato dot ac dot nz)
 * @version $Revision: 5492 $ 
 */
public class MyUnderSampleForEachClass
  extends Filter 
  implements SupervisedFilter, OptionHandler {
  
  /** for serialization. */
  static final long serialVersionUID = 7079064953548300681L;
  
  /** The subsample size, percent of original set, default 50%. */
  protected double m_SampleSizePercent = 25;
  
  /** The random number generator seed. */
  protected int m_RandomSeed = 1;
  
  /** The degree of bias towards uniform (nominal) class distribution. */
  protected double m_BiasToUniformClass = 0;

  /** Whether to perform sampling with replacement or without. */
  protected boolean m_NoReplacement = false;

  /** Whether to invert the selection (only if instances are drawn WITHOUT 
   * replacement).
   * @see #m_NoReplacement */
  protected boolean m_InvertSelection = false;

  /**
   * Returns a string describing this filter.
   *
   * @return a description of the filter suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return 
        "Produces a random subsample of a dataset using either sampling "
      + "with replacement or without replacement.\n"
      + "The original dataset must "
      + "fit entirely in memory. The number of instances in the generated "
      + "dataset may be specified. The dataset must have a nominal class "
      + "attribute. If not, use the unsupervised version. The filter can be "
      + "made to maintain the class distribution in the subsample, or to bias "
      + "the class distribution toward a uniform distribution. When used in batch "
      + "mode (i.e. in the FilteredClassifier), subsequent batches are NOT UnderSampled.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {
    Vector result = new Vector();

    result.addElement(new Option(
	"\tSpecify the random number seed (default 1)",
	"S", 1, "-S <num>"));

    result.addElement(new Option(
	"\tThe size of the output dataset, as a percentage of\n"
	+"\tthe input dataset (default 100)",
	"Z", 1, "-Z <num>"));

    result.addElement(new Option(
	"\tBias factor towards uniform class distribution.\n"
	+"\t0 = distribution in input data -- 1 = uniform distribution.\n"
	+"\t(default 0)",
	"B", 1, "-B <num>"));

    result.addElement(new Option(
	"\tDisables replacement of instances\n"
	+"\t(default: with replacement)",
	"no-replacement", 0, "-no-replacement"));

    result.addElement(new Option(
	"\tInverts the selection - only available with '-no-replacement'.",
	"V", 0, "-V"));

    return result.elements();
  }


  /**
   * Parses a given list of options. <p/>
   * 
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -S &lt;num&gt;
   *  Specify the random number seed (default 1)</pre>
   * 
   * <pre> -Z &lt;num&gt;
   *  The size of the output dataset, as a percentage of
   *  the input dataset (default 100)</pre>
   * 
   * <pre> -B &lt;num&gt;
   *  Bias factor towards uniform class distribution.
   *  0 = distribution in input data -- 1 = uniform distribution.
   *  (default 0)</pre>
   * 
   * <pre> -no-replacement
   *  Disables replacement of instances
   *  (default: with replacement)</pre>
   * 
   * <pre> -V
   *  Inverts the selection - only available with '-no-replacement'.</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    String	tmpStr;
    
    tmpStr = Utils.getOption('S', options);
    if (tmpStr.length() != 0)
      setRandomSeed(Integer.parseInt(tmpStr));
    else
      setRandomSeed(1);

    tmpStr = Utils.getOption('B', options);
    if (tmpStr.length() != 0)
      setBiasToUniformClass(Double.parseDouble(tmpStr));
    else
      setBiasToUniformClass(0);

    tmpStr = Utils.getOption('Z', options);
    if (tmpStr.length() != 0)
      setSampleSizePercent(Double.parseDouble(tmpStr));
    else
      setSampleSizePercent(50);

    setNoReplacement(Utils.getFlag("no-replacement", options));

    if (getNoReplacement())
      setInvertSelection(Utils.getFlag('V', options));

    if (getInputFormat() != null) {
      setInputFormat(getInputFormat());
    }
  }

  /**
   * Gets the current settings of the filter.
   *
   * @return an array of strings suitable for passing to setOptions
   */
  public String [] getOptions() {
    Vector<String>	result;

    result = new Vector<String>();

    result.add("-B");
    result.add("" + getBiasToUniformClass());

    result.add("-S");
    result.add("" + getRandomSeed());

    result.add("-Z");
    result.add("" + getSampleSizePercent());

    if (getNoReplacement()) {
      result.add("-no-replacement");
      if (getInvertSelection())
	result.add("-V");
    }
    
    return result.toArray(new String[result.size()]);
  }
    
  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String biasToUniformClassTipText() {
    return "Whether to use bias towards a uniform class. A value of 0 leaves the class "
      + "distribution as-is, a value of 1 ensures the class distribution is "
      + "uniform in the output data.";
  }
    
  /**
   * Gets the bias towards a uniform class. A value of 0 leaves the class
   * distribution as-is, a value of 1 ensures the class distributions are
   * uniform in the output data.
   *
   * @return the current bias
   */
  public double getBiasToUniformClass() {
    return m_BiasToUniformClass;
  }
  
  /**
   * Sets the bias towards a uniform class. A value of 0 leaves the class
   * distribution as-is, a value of 1 ensures the class distributions are
   * uniform in the output data.
   *
   * @param newBiasToUniformClass the new bias value, between 0 and 1.
   */
  public void setBiasToUniformClass(double newBiasToUniformClass) {
    m_BiasToUniformClass = newBiasToUniformClass;
  }
    
  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String randomSeedTipText() {
    return "Sets the random number seed for subsampling.";
  }
  
  /**
   * Gets the random number seed.
   *
   * @return the random number seed.
   */
  public int getRandomSeed() {
    return m_RandomSeed;
  }
  
  /**
   * Sets the random number seed.
   *
   * @param newSeed the new random number seed.
   */
  public void setRandomSeed(int newSeed) {
    m_RandomSeed = newSeed;
  }
    
  /**
   * Returns the tip text for this property.
   *
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String sampleSizePercentTipText() {
    return "The subsample size as a percentage of the original set.";
  }
  
  /**
   * Gets the subsample size as a percentage of the original set.
   *
   * @return the subsample size
   */
  public double getSampleSizePercent() {
    return m_SampleSizePercent;
  }
  
  /**
   * Sets the size of the subsample, as a percentage of the original set.
   *
   * @param newSampleSizePercent the subsample set size, between 0 and 100.
   */
  public void setSampleSizePercent(double newSampleSizePercent) {
    m_SampleSizePercent = newSampleSizePercent;
  }
  
  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String noReplacementTipText() {
    return "Disables the replacement of instances.";
  }

  /**
   * Gets whether instances are drawn with or without replacement.
   * 
   * @return true if the replacement is disabled
   */
  public boolean getNoReplacement() {
    return m_NoReplacement;
  }
  
  /**
   * Sets whether instances are drawn with or with out replacement.
   * 
   * @param value if true then the replacement of instances is disabled
   */
  public void setNoReplacement(boolean value) {
    m_NoReplacement = value;
  }
  
  /**
   * Returns the tip text for this property.
   * 
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String invertSelectionTipText() {
    return "Inverts the selection (only if instances are drawn WITHOUT replacement).";
  }

  /**
   * Gets whether selection is inverted (only if instances are drawn WIHTOUT 
   * replacement).
   * 
   * @return true if the replacement is disabled
   * @see #m_NoReplacement
   */
  public boolean getInvertSelection() {
    return m_InvertSelection;
  }
  
  /**
   * Sets whether the selection is inverted (only if instances are drawn WIHTOUT 
   * replacement).
   * 
   * @param value if true then selection is inverted
   */
  public void setInvertSelection(boolean value) {
    m_InvertSelection = value;
  }

  /** 
   * Returns the Capabilities of this filter.
   *
   * @return            the capabilities of this object
   * @see               Capabilities
   */
  public Capabilities getCapabilities() {
    Capabilities result = super.getCapabilities();
    result.disableAll();

    // attributes
    result.enableAllAttributes();
    result.enable(Capability.MISSING_VALUES);
    
    // class
    result.enable(Capability.NOMINAL_CLASS);
    result.enable(Capability.NUMERIC_CLASS);
    
    return result;
  }
  
  /**
   * Sets the format of the input instances.
   *
   * @param instanceInfo an Instances object containing the input 
   * instance structure (any instances contained in the object are 
   * ignored - only the structure is required).
   * @return true if the outputFormat may be collected immediately
   * @throws Exception if the input format can't be set 
   * successfully
   */
  public boolean setInputFormat(Instances instanceInfo) 
       throws Exception {

    super.setInputFormat(instanceInfo);
    setOutputFormat(instanceInfo);
    return true;
  }

  /**
   * Input an instance for filtering. Filter requires all
   * training instances be read before producing output.
   *
   * @param instance the input instance
   * @return true if the filtered instance may now be
   * collected with output().
   * @throws IllegalStateException if no input structure has been defined
   */
  public boolean input(Instance instance) {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }
    if (m_NewBatch) {
      resetQueue();
      m_NewBatch = false;
    }
    if (isFirstBatchDone()) {
      push(instance);
      return true;
    } else {
      bufferInput(instance);
      return false;
    }
  }

  /**
   * Signify that this batch of input to the filter is finished. 
   * If the filter requires all instances prior to filtering,
   * output() may now be called to retrieve the filtered instances.
   *
   * @return true if there are instances pending output
   * @throws IllegalStateException if no input structure has been defined
   */
  public boolean batchFinished() {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }

    if (!isFirstBatchDone()) {
      // Do the subsample, and clear the input instances.
      createSubsample();
    }
    flushInput();

    m_NewBatch = true;
    m_FirstBatchDone = true;
    return (numPendingOutput() != 0);
  }

  /**
   * Creates a subsample of the current set of input instances. The output
   * instances are pushed onto the output queue for collection.
   */
  public void MinorSubsample()
  {
	  int origSize=getInputFormat().numInstances();
	  int minor=0;
	  Instances sample=new Instances(getInputFormat(),0);
	  for(int i=0;i<origSize;i++)
	  {
		  Instance ins=getInputFormat().instance(i);
		  if(getInputFormat().classAttribute().isNominal())
		  {
			  if(ins.classValue()==0)
			  {
				  minor++;
				  sample.add((Instance)ins.copy());
			  }
			  else
			  {
				  push((Instance)ins.copy());
			  }
		  }
	  }
	  int sampleSize=(int)(minor*m_SampleSizePercent/100);
	  Random random = new Random(m_RandomSeed);
	  for (int j = 0; j < sampleSize; j++)
	  {
		int i = random.nextInt(minor);
		push((Instance)sample.instance(i).copy());
		sample.delete(i);
		minor--;
	  }
  }
  public void MajorSubsample()
  {
	  int origSize=getInputFormat().numInstances();
	  int major=0,minor=0;
	  Instances sample=new Instances(getInputFormat(),0);
	  for(int i=0; i<origSize;i++)
	  {
		  Instance ins=getInputFormat().instance(i);
		  if(getInputFormat().classAttribute().isNominal())
		  {
			  if(ins.classValue()==0)
			  {
				  minor++;
				  push((Instance)ins.copy());
			  }
			  else
			  {
				  sample.add((Instance)ins.copy());
			  }
		  }
	  }
	  major=origSize-minor;
	  int sampleSize=(int)(major * m_SampleSizePercent/100);
  	  Random random = new Random(m_RandomSeed);
	  for (int j = 0; j < sampleSize; j++)
	  {
		int i = random.nextInt(major);
		push((Instance)sample.instance(i).copy());
		sample.delete(i);
		major--;
	  }
  }
  protected void createSubsample() {
	  if(m_InvertSelection)
	  {
		  MinorSubsample();
	  }
	  else
	  {
		  MajorSubsample();
	  }
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 5492 $");
  }
  
  /**
   * Main method for testing this class.
   *
   * @param argv should contain arguments to the filter: 
   * use -h for help
   */
  public static void main(String [] argv) {
    runFilter(new MyUnderSampleForEachClass(), argv);
  }
}



