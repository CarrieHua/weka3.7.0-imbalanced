/**
 * 列出指定目录下面指定类型的所有文件名字
 */
package szb.fileHandling;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;

/**
 * @author szb
 * 
 */
public class ListFileName {

	/**
	 * 统计filePath下扩展名为fileExt的文件名字 输出到destFile中
	 * 
	 * @param filePath
	 * @param fileExt
	 * @param destFile
	 */
	public void list(String filePath, String fileExt, String destFile) {
		File dir = new File(filePath);
		if (!dir.isDirectory()) {
			System.err.print("指定的文件路径必须为目录！！！");
		} else {
			String fileName[] = dir.list();
			String content = "";
			for (int i = 0; i < fileName.length; i++) {
				if (fileName[i].endsWith(fileExt)) {
					int endIndex = fileName[i].length() - fileExt.length();
					content = content + fileName[i].substring(0, endIndex)
							+ "\n";
				}
			}
			output(content, destFile);
		}
	}

	/**
	 * 将text写入filename中
	 * 
	 * @param text
	 * @param filename
	 */
	public void output(String text, String filename) {

		try {
			FileWriter writer = new FileWriter(filename, true);
			writer.write(text);
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {

		String filePath = "F:/DataSets/KeelDataSets/KeelData/KeelArffData";
		String fileExt = ".arff";
		String destFile = "F:/DataSets/KeelDataSets/KeelData/KeelArffData/FileName.csv";
		ListFileName test = new ListFileName();
		test.list(filePath, fileExt, destFile);
	}
}
