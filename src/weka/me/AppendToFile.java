package weka.me;

import java.io.FileWriter; 
import java.io.IOException; 
import java.io.RandomAccessFile; 
import java.io.File;
/** 
* 将内容追加到文件尾部 
*/ 
public class AppendToFile {
	/** 
	* A方法追加文件：使用RandomAccessFile 
	* @param fileName 文件名 
	* @param content 追加的内容 
	*/ 
	public static void appendMethodA(String fileName,String content){ 
		try { 
			// 打开一个随机访问文件流，按读写方式 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 
			// 文件长度，字节数 
			
			long fileLength = randomFile.length(); 
			//将写文件指针移到文件尾。 
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
			// 打开一个随机访问文件流，按读写方式 
			RandomAccessFile randomFile = new RandomAccessFile(fileName, "rw"); 
			// 文件长度，字节数 
			long fileLength = randomFile.length(); 
			//将写文件指针移到文件尾。 
			randomFile.seek(fileLength); 
			randomFile.writeDouble(content); 
			randomFile.close(); 
			} catch (IOException e){ 
				e.printStackTrace(); 
			} 
	} 
	/** 
	* B方法追加文件：使用FileWriter 
	* @param fileName 
	* @param content 
	*/ 
	public static void appendMethodB(String fileName, String content){ 
		try { 
			//打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件 
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
//		//按方法A追加文件 
//		AppendToFile.appendMethodA(fileName, pi);
//		AppendToFile.appendMethodA(fileName, content);
//		AppendToFile.appendMethodA(fileName, "append end. \r\n"); 
		//显示文件内容 
//		fileTest.readFileByLines(fileName); 
//		//按方法B追加文件 
//		AppendToFile.appendMethodB(fileName, content); 
//		AppendToFile.appendMethodB(fileName, "append end. \r\n"); 
//		//显示文件内容 
//		fileTest.readFileByLines(fileName); 
	} 
}
