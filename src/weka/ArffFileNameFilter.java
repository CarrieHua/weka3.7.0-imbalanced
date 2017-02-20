package weka;

import java.io.*;
public class ArffFileNameFilter implements FilenameFilter{

	private String suffix;

	public ArffFileNameFilter(String suffix) {
	   this.suffix = suffix;
	}

	public boolean accept(File dir, String name) {
	   if(name.endsWith(suffix)) {
	    return true;
	   }
	   return false;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String dirName = "D:\\eclipse3.3\\work\\wekaclastering3\\voteNew1";
		String[] names = new java.io.File(dirName).list(new ArffFileNameFilter(".arff"));
		System.out.println(names.length);
		for (int i = 0; i < names.length; i++){
			System.out.println(names[i]);
		}



	}

}
