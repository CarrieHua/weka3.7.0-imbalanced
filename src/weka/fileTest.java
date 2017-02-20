package weka;

import java.io.BufferedReader; 
import java.io.File; 
import java.io.FileInputStream; 
import java.io.FileReader; 
import java.io.IOException; 
import java.io.InputStream; 
import java.io.InputStreamReader; 
import java.io.RandomAccessFile; 
import java.io.Reader; 
public class fileTest {

	/** 
	*/ 
public static void readFileByBytes(String fileName){ 
	File file = new File(fileName); 
	InputStream in = null; 
	try { 
		System.out.println(""); 
	    // һ�ζ�һ���ֽ� 
		in = new FileInputStream(file); 
		int tempbyte; 
		while((tempbyte=in.read()) != -1){ 
			System.out.write(tempbyte); 
		} 
		in.close(); 
	} catch (IOException e) { 
		e.printStackTrace(); 
		return; 
	} 
	try { 
		System.out.println(""); 
		
		byte[] tempbytes = new byte[100]; 
		int byteread = 0; 
		in = new FileInputStream(fileName); 
		showAvailableBytes(in); 
		
		while ((byteread = in.read(tempbytes)) != -1){ 
			System.out.write(tempbytes, 0, byteread); 
		} 
	} catch (Exception e1) { 
		e1.printStackTrace();
	  } finally { 
		  if (in != null){ 
			  try { 
				  in.close(); 
			  } catch (IOException e1) { 
				  
			  } 
		  } 
	     } 
} 
	/** 
	* 
	*/ 
public static void readFileByChars(String fileName){ 
	File file = new File(fileName); 
	Reader reader = null; 
	try { 
		System.out.println(""); 
	    // 
		reader = new InputStreamReader(new FileInputStream(file)); 
		int tempchar; 
		while ((tempchar = reader.read()) != -1){ 
			
			if (((char)tempchar) != 'r'){ 
				System.out.print((char)tempchar); 
			} 
		}
		reader.close(); 
	} catch (Exception e) { 
		e.printStackTrace(); 
	} 
	try { 
		System.out.println(""); 
		
		char[] tempchars = new char[30]; 
		int charread = 0; 
		reader = new InputStreamReader(new FileInputStream(fileName)); 
		
		while ((charread = reader.read(tempchars))!=-1){ 
			
			if ((charread == tempchars.length)&&(tempchars[tempchars.length-1] != 'r')){ 
				System.out.print(tempchars); 
			}else {
				for (int i=0; i<charread; i++){ 
					if(tempchars[i] == 'r'){
						continue; 
					}else{ 
						System.out.print(tempchars[i]); 
					} 
				} 
			} 
		} 
	} catch (Exception e1) { 
		e1.printStackTrace(); 
	}finally { 
		if (reader != null){ 
			try { 
				reader.close(); 
			} catch (IOException e1) { 
				
			} 
		} 
	} 
} 
	
public static void readFileByLines(String fileName){ 
	File file = new File(fileName); 
	BufferedReader reader = null; 
	try { 

		reader = new BufferedReader(new FileReader(file)); 
		String tempString = null; 
		int line = 1; 
		
		while ((tempString = reader.readLine()) != null){
		
			line++; 
		} 
		reader.close(); 
	} catch (IOException e) { 
		e.printStackTrace(); 
	} finally { 
		if (reader != null){ 
			try { 
				reader.close(); 
			} catch (IOException e1) {}
		} 
	} 
} 


public static int readarffgetFeaatureNum(String fileName){ 
	File file = new File(fileName); 
	BufferedReader reader = null; 
	int c=0;
	try { 
		reader = new BufferedReader(new FileReader(file)); 
		String tempString = null; 
		while ((tempString = reader.readLine()) != null){
			if(tempString.indexOf("@attribute ")!=-1){
				c++;
			}		
		} 
		reader.close(); 
	} catch (IOException e) { 
		e.printStackTrace(); 
	} finally { 
		if (reader != null){ 
			try { 
				reader.close(); 
			} catch (IOException e1) {}
		} 
	} 
	return c;
} 

public static int[] readFileByLinestoInt(String fileName,int dianNo){ 
	File file = new File(fileName); 
	BufferedReader reader = null; 
	int[] b=new int[dianNo];
	
	int i=0;
	try { 
	 
		reader = new BufferedReader(new FileReader(file)); 
		String tempString = null; 
		int line = 1; 
	
		while ((tempString = reader.readLine()) != null){
			b[i]=Integer.parseInt(tempString);
			i++;
		 
			line++; 
		}

		reader.close(); 
	} catch (IOException e) { 
		e.printStackTrace(); 
	} finally { 
		if (reader != null){ 
			try { 
				reader.close(); 
			} catch (IOException e1) {}
		} 
	}
	return b;
}
	
public static void readFileByRandomAccess(String fileName){ 
	RandomAccessFile randomFile = null; 
	try { 
		System.out.println(""); 
		
		randomFile = new RandomAccessFile(fileName, "r"); 
	
		long fileLength = randomFile.length(); 
	
		int beginIndex = (fileLength > 4) ? 4 : 0; 
	 
		randomFile.seek(beginIndex); 
		byte[] bytes = new byte[10]; 
		int byteread = 0; 
	
		while ((byteread = randomFile.read(bytes)) != -1){ 
			System.out.write(bytes, 0, byteread); 
		} 
	} catch (IOException e){ 
		e.printStackTrace(); 
	} finally { 
		if (randomFile != null){ 
			try { 
				randomFile.close(); 
			} catch (IOException e1) { } 
		} 
	} 
} 
public static void readdataFile(String fileName){
	RandomAccessFile randomFile = null;
	int total = 0;
	try{
		System.out.println("now begin to read the file");
		randomFile = new RandomAccessFile(fileName, "r");
		long fileLength = randomFile.length();
		int pos = 0;
		while (pos < fileLength){
			double data = randomFile.readDouble();
			System.out.println(data);
			pos += 8;
			total++;
		}
	}catch (IOException e){
		e.printStackTrace();
	}finally {
		if (randomFile != null){
			try{
				randomFile.close();
			}catch(IOException e){}
			
		}
	}
	System.out.println("total number is: " + total);
}
public static void readAndCalculate(String relationName, double benchmark, int[][] matrix){
	RandomAccessFile preFile = null;
	RandomAccessFile actFile = null;
	int total = 0;
	String preName = relationName + "_pre.txt";
	String actName = relationName + "_act.txt";
	try{
//		System.out.println("now begin to read the file");
		preFile = new RandomAccessFile(preName, "r");
        actFile = new RandomAccessFile(actName, "r");
        
		long fileLength = preFile.length();
		int pos = 0;
		while (pos < fileLength){
			double predata = preFile.readDouble();
			double actdata = actFile.readDouble();
//			System.out.println(predata + "\t" + actdata );
		      
		      if (actdata > 0){
		    	  if (predata > benchmark )
		    		  matrix[0][0]++;
		    	  else
		    		  matrix[0][1]++;
		      }
		      else{
		    	  if (predata > benchmark )
		    		  matrix[1][0]++;
		    	  else
		    		  matrix[1][1]++;
		      }
			pos += 8;
			total++;
		}
	}catch (IOException e){
		e.printStackTrace();
	}finally {
		if (preFile != null){
			try{
				preFile.close();
			}catch(IOException e){}
			
		}
		if (actFile != null){
			try{
				actFile.close();
			}catch(IOException e){}
			
		}
	}
//	System.out.println("total number is: " + total);
}
	
private static void showAvailableBytes(InputStream in){ 
	try { 
	System.out.println("" + in.available()); 
	} catch (IOException e) { 
	e.printStackTrace(); 
	} 
} 
    
public static void main(String[] args) { 
	
	String[] fileNames ={"PC1","MW1","KC3","CM1","PC2","KC4","PC3","PC4"}; 
	double benchmark = 0.1;//,"PC2-weka.filters.unsupervised.attribute.Remove-R9"
  for(int j = 0; j < 10; j++){
	  System.out.println("--------------benchmark = "+benchmark);
	for (int i = 0; i < fileNames.length; i++){
		int[][] resultmatrix = new int[2][2];
		String str = "MLP/" + fileNames[i] + "_0.4";
		readAndCalculate(str,benchmark,resultmatrix);
		double TPRate = (double)resultmatrix[0][0] / (resultmatrix[0][0] + resultmatrix[0][1]);
		double FPRate = (double)resultmatrix[1][0] / (resultmatrix[1][1] + resultmatrix[1][0]);
		double precision = (double)resultmatrix[0][0] / (resultmatrix[0][0]+resultmatrix[1][0]);
		double accuracy = (double)(resultmatrix[0][0]+resultmatrix[1][1])/(resultmatrix[0][0]+resultmatrix[0][1]+resultmatrix[1][0]+resultmatrix[1][1]);
	    double balance = 1 - Math.sqrt((FPRate * FPRate + (1 - TPRate)* (1-TPRate)) / 2) ;
//		System.out.println("-------"+str+"-------"+benchmark);
//    	System.out.println("TP rate\tFP rate\t Precision \t accuracy: ");
//    	System.out.println(TPRate );
//    	System.out.println("\t" + FPRate);
//    	System.out.println("\t\t" + precision);
//    	System.out.println("\t\t\t" + accuracy);
    	System.out.println(balance);
//    	System.out.println();
	}
    benchmark += 0.1;
  }
} 
	
    
}
