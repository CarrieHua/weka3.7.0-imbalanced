package weka.me;

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
			// ��һ����������ļ���������д��ʽ 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 
			// �ļ����ȣ��ֽ��� 
			
			long fileLength = randomFile.length(); 
			//��д�ļ�ָ���Ƶ��ļ�β�� 
//			if (fileLength > 0)
			randomFile.seek(fileLength); 
			randomFile.writeBytes(content); 
			randomFile.close(); 
			} catch (IOException e){ 
				e.printStackTrace(); 
			} 
	}
	public static void appendMethodA(String fileName,double content){ 
		try { 
			// ��һ����������ļ���������д��ʽ 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 
			// �ļ����ȣ��ֽ��� 
			long fileLength = randomFile.length(); 
			//��д�ļ�ָ���Ƶ��ļ�β�� 
			randomFile.seek(fileLength); 
			randomFile.writeDouble(content); 
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
			//��һ��д�ļ��������캯���еĵڶ�������true��ʾ��׷����ʽд�ļ� 
			FileWriter writer = new FileWriter(fileName, true); 
			writer.write(content); 
			writer.close(); 
			} catch (IOException e) { 
				e.printStackTrace(); 
			} 
	} 
	public static void main(String[] args) { 
		int a = 4;
		a = (++a)+(++a);
		System.out.println(a);
//		String fileName = "newTemp.txt"; 
//		File file = new File(fileName);
//		if (file.exists()){
//			file.delete();
//		}
//		String content = "new append!"; 
//		double pi = 3.14159;
//		//������A׷���ļ� 
//		AppendToFile.appendMethodA(fileName, pi);
//		AppendToFile.appendMethodA(fileName, content);
//		AppendToFile.appendMethodA(fileName, "append end. \r\n"); 
		//��ʾ�ļ����� 
//		fileTest.readFileByLines(fileName); 
//		//������B׷���ļ� 
//		AppendToFile.appendMethodB(fileName, content); 
//		AppendToFile.appendMethodB(fileName, "append end. \r\n"); 
//		//��ʾ�ļ����� 
//		fileTest.readFileByLines(fileName); 
	} 
}
