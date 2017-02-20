/**
 * �г�ָ��Ŀ¼����ָ�����͵������ļ�����
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
	 * ͳ��filePath����չ��ΪfileExt���ļ����� �����destFile��
	 * 
	 * @param filePath
	 * @param fileExt
	 * @param destFile
	 */
	public void list(String filePath, String fileExt, String destFile) {
		File dir = new File(filePath);
		if (!dir.isDirectory()) {
			System.err.print("ָ�����ļ�·������ΪĿ¼������");
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
	 * ��textд��filename��
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
