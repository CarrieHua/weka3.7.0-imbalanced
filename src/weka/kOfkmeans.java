package weka;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class kOfkmeans {
	public static int getClustereK(String fileName){ 
		int k=0;
		File file = new File(fileName); 
		BufferedReader reader = null; 
		try { 
			reader = new BufferedReader(new FileReader(file)); 
			String tempString = null;
			while ((tempString = reader.readLine()) != null){
				if(tempString.indexOf("@attribute ")!=-1){
				int c=0;
				for(int i=0;i<tempString.length();i++){
					if(tempString.charAt(i)==',')
						c++;
				}
				if(c>k)
					k=c;}
				
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
	return k+1;
	}			
}
