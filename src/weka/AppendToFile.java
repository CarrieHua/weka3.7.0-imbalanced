package weka;

import java.io.FileWriter; 
import java.io.IOException; 
import java.io.RandomAccessFile; 
import java.io.File;
/** 
* ������׷�ӵ��ļ�β�� 
*/ 
public class AppendToFile {
	/** 
	* A����׷���ļ���ʹ��RandomAccessFile 
	* @param fileName �ļ��� 
	* @param content ׷�ӵ����� 
	*/ 
	public static void appendMethodA(String fileName,String content){ 
		try { 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 			
			long fileLength = randomFile.length(); 
			randomFile.seek(fileLength); 
			randomFile.writeBytes(content); 
			randomFile.close(); 
			} catch (IOException e){ 
				e.printStackTrace(); 
			} 
	}
	public static void appendMethodA(String fileName,double content){ 
		try { 
 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 		
			long fileLength = randomFile.length(); 
			randomFile.seek(fileLength); 
			randomFile.writeDouble(content); 
			randomFile.close(); 
			} catch (IOException e){ 
				e.printStackTrace(); 
			} 
	} 
	
	public static void appendMethodC(String fileName,long content){ 
		try { 

			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 		
			long fileLength = randomFile.length(); 		
			randomFile.seek(fileLength); 
			randomFile.writeLong(content); 
			randomFile.close(); 
			} catch (IOException e){ 
				e.printStackTrace(); 
			} 
	}
	/** 
	* B����׷���ļ���ʹ��FileWriter 
	* @param fileName 
	* @param content 
	*/ 
	public static void appendMethodB(String fileName, String content){ 
		try { 
			//
			FileWriter writer = new FileWriter(fileName, false); 
			writer.write(content); 
			writer.close(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
	} 
	
	
	public static void main(String[] args) { 
		String fileName = "newTemp.txt"; 
		File file = new File(fileName);
		if (file.exists()){
			file.delete();
		}
		String content = "new append!"; 
		double pi = 3.14159;
		//������A׷���ļ� 
		AppendToFile.appendMethodA(fileName, pi);
		AppendToFile.appendMethodA(fileName, content);
		AppendToFile.appendMethodA(fileName, "append end. \r\n"); 
		//��ʾ�ļ����� 
		fileTest.readFileByLines(fileName); 
		//������B׷���ļ� 
		AppendToFile.appendMethodB(fileName, content); 
		AppendToFile.appendMethodB(fileName, "append end. \r\n"); 
		//��ʾ�ļ����� 
		fileTest.readFileByLines(fileName); 
	} 
}
