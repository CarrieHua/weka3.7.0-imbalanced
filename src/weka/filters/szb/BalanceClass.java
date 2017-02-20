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
 *    Randomize.java
 *    Copyright (C) 1999 University of Waikato, Hamilton, New Zealand
 *
 */


package weka.filters.szb;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.Option;
import weka.core.OptionHandler;
import weka.core.RevisionUtils;
import weka.core.Utils;
import weka.core.Capabilities.Capability;
import weka.filters.Filter;
import weka.filters.UnsupervisedFilter;

import java.util.Enumeration;
import java.util.Random;
import java.util.Vector;

/** 
 <!-- globalinfo-start -->
 * Randomly shuffles the order of instances passed through it. The random number generator is reset with the seed value whenever a new set of instances is passed in.
 * <p/>
 <!-- globalinfo-end -->
 * 
 <!-- options-start -->
 * Valid options are: <p/>
 * 
 * <pre> -S &lt;num&gt;
 *  Specify the random number seed (default 42)</pre>
 * 
 <!-- options-end -->
 *
 * @author Len Trigg (trigg@cs.waikato.ac.nz)
 * @version $Revision: 5499 $
 */
public class BalanceClass 
  extends Filter 
  implements UnsupervisedFilter, OptionHandler {
  
  /** for serialization */
  static final long serialVersionUID = 8854479785121877582L;

  /** The random number seed */
  protected int m_Seed = 42;

  /** The current random number generator */
  protected Random m_Random;
  
  /**
   * Returns a string describing this classifier
   * @return a description of the classifier suitable for
   * displaying in the explorer/experimenter gui
   */
  public String globalInfo() {
    return "Randomly shuffles the order of instances passed through it. "
      + "The random number generator is reset with the seed value whenever "
      + "a new set of instances is passed in.";
  }

  /**
   * Returns an enumeration describing the available options.
   *
   * @return an enumeration of all the available options.
   */
  public Enumeration listOptions() {

    Vector newVector = new Vector(1);

    newVector.addElement(new Option(
              "\tSpecify the random number seed (default 42)",
              "S", 1, "-S <num>"));

    return newVector.elements();
  }


  /**
   * Parses a given list of options. <p/>
   * 
   <!-- options-start -->
   * Valid options are: <p/>
   * 
   * <pre> -S &lt;num&gt;
   *  Specify the random number seed (default 42)</pre>
   * 
   <!-- options-end -->
   *
   * @param options the list of options as an array of strings
   * @throws Exception if an option is not supported
   */
  public void setOptions(String[] options) throws Exception {
    
    String seedString = Utils.getOption('S', options);
    if (seedString.length() != 0) {
      setRandomSeed(Integer.parseInt(seedString));
    } else {
      setRandomSeed(42);
    }

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

    String [] options = new String [2];
    int current = 0;

    options[current++] = "-S"; options[current++] = "" + getRandomSeed();

    while (current < options.length) {
      options[current++] = "";
    }
    return options;
  }

  /**
   * Returns the tip text for this property
   * @return tip text for this property suitable for
   * displaying in the explorer/experimenter gui
   */
  public String randomSeedTipText() {
    return "Seed for the random number generator.";
  }

  /**
   * Get the random number generator seed value.
   *
   * @return random number generator seed value.
   */
  public int getRandomSeed() {
    
    return m_Seed;
  }
  
  /**
   * Set the random number generator seed value.
   *
   * @param newRandomSeed value to use as the random number generator seed.
   */
  public void setRandomSeed(int newRandomSeed) {
    
    m_Seed = newRandomSeed;
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
    result.enableAllClasses();
    result.enable(Capability.MISSING_CLASS_VALUES);
    result.enable(Capability.NO_CLASS);
    
    return result;
  }
  
  /**
   * Sets the format of the input instances.
   *
   * @param instanceInfo an Instances object containing the input instance
   * structure (any instances contained in the object are ignored - only the
   * structure is required).
   * @return true if the outputFormat may be collected immediately
   * @throws Exception if format cannot be processed
   */
  public boolean setInputFormat(Instances instanceInfo) throws Exception {
      super.setInputFormat(instanceInfo);
      setOutputFormat(instanceInfo);
      m_Random = new Random(m_Seed);
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
   * Signify that this batch of input to the filter is finished. If
   * the filter requires all instances prior to filtering, output()
   * may now be called to retrieve the filtered instances. Any
   * subsequent instances filtered should be filtered based on setting
   * obtained from the first batch (unless the setInputFormat has been
   * re-assigned or new options have been set). This 
   * implementation randomizes all the instances received in the batch.
   *
   * @return true if there are instances pending output
   * @throws IllegalStateException if no input format has been set. 
   */
  public boolean batchFinished() {

    if (getInputFormat() == null) {
      throw new IllegalStateException("No input instance format defined");
    }

    Instances toFilter = getInputFormat();
    toFilter.randomize(m_Random);
    toFilter.deleteWithMissingClass();
    Instances toFilter1=new Instances(toFilter);
    Instances toBalance1 = new Instances(toFilter1,0);/*第一个类标签的实例集*/
    Instances toBalance2 = new Instances(toFilter1,0);/*第二个类标签的实例集*/
    if (!isFirstBatchDone()) 
    {
           
            for (int i = 0; i < toFilter1.numInstances(); i++)
             {
          	  if (toFilter1.instance(i).classValue() ==0)
          	  {
          		  toBalance1.add(toFilter1.instance(i));    		  
          	  }
          	  else
          	  {
          		  toBalance2.add(toFilter1.instance(i));
          	  }
          	  
             }
    
            int j=toBalance1.numInstances();/*第一个类标签的实例个数*/
            int k=toBalance2.numInstances();/*第二个类标签的实例个数*/
                  

            int  Balance=k/j;
            FastVector nominal_values = new FastVector(Balance);
            nominal_values.addElement("defective");
            if(k%j<j/2)
           	   {
                 for (int i = 0; i <Balance; i++)
                 {
             	   nominal_values.addElement("cluster" + (i+1)); 
                 }
           	   }
               else
               {
               	 for (int i = 0; i <=Balance; i++)
                 {
               	   nominal_values.addElement("cluster" + (i+1)); 
                  }
               }
          	   toFilter1.setClassIndex(0);
               toFilter1.deleteAttributeAt(toFilter1.numAttributes()-1);
               toFilter1.insertAttributeAt(new Attribute("cluster", nominal_values), toFilter1.numAttributes());
               toFilter1.setClassIndex(toFilter1.numAttributes()-1);
               setOutputFormat(toFilter1);
               int num=0;//calculate the number of the Minority class
               // build new dataset
           	   if(k%j<j/2)//not add a class
           	  {
                 for (int i=0; i<toFilter1.numInstances(); i++) 
                 {
                   if(toFilter.instance(i).classValue()==0)
                   {
                	 toFilter1.instance(i).setClassValue(toFilter1.classAttribute().value(0));
                	 num++;
                    }
                   else
                   {
                 	  if((i-num)<j*Balance)
                 	  {
                         toFilter1.instance(i).setClassValue(toFilter1.classAttribute().value((i-num)/j+1));
                 	  }
                 	  else
                 	  {
                 		  toFilter1.instance(i).setClassValue(toFilter1.classAttribute().value((i-num)/j));
                 	  }
                    }
                  } 
              }
           //add a class   
              else
              {
           		 for (int i=0; i<toFilter1.numInstances(); i++) 
                  {
                    if(toFilter.instance(i).classValue()==0)
                    {
                   	 toFilter1.instance(i).setClassValue(toFilter1.classAttribute().value(0));
                   	 num++;
                     }
                    else
                 	{
                 	 toFilter1.instance(i).setClassValue(toFilter1.classAttribute().value((i-num)/j+1));
                 	}
                  }
           	  } 
         
      for (int i = 0; i < toFilter1.numInstances(); i++) 
       {
              push(toFilter1.instance(i));
           }

    flushInput();
    
    m_NewBatch = true;
    m_FirstBatchDone = true;
    }
    return (numPendingOutput() != 0);
  }
  
  /**
   * Returns the revision string.
   * 
   * @return		the revision
   */
  public String getRevision() {
    return RevisionUtils.extract("$Revision: 5499 $");
  }

  /**
   * Main method for testing this class.
   *
   * @param argv should contain arguments to the filter: use -h for help
   */
  public static void main(String [] argv) {
    runFilter(new BalanceClass(), argv);
  }
}
