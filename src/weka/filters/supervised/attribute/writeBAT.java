package weka.filters.supervised.attribute;

import java.io.File;

import weka.attributeSelection.ASEvaluation;
import weka.AppendToFile;

public class writeBAT {

	public static void main(String[] argv) {
//		File file = new File("D:/exp/0609/dataset/");
		File file = new File("D:/exp/0609/高性能/njjFCBF2/");
//		File file = new File("D:/exp/0609/高性能/FOCUS/");
//		AppendToFile.appendMethodB("result/FileName.txt", "title njj\r\n\r\n");
		
		

		
		File[] lf = file.listFiles();
		String txtname ="D:/exp/new5/njjFCBF2nb";
		
		AppendToFile.appendMethodB(txtname, "#!/bin/sh"+"\r\n"+"#PBS -N njjFCBF2nb"+"\r\n"+"#PBS -l cput=48:00:00"+"\r\n"+"#PBS -l walltime=48:00:00"+"\r\n");
		for (int i = 0; i < lf.length; i++) {
//			String txtname ="D:/exp/new3/njjsqre-"+String.valueOf(i);
//			String txtname ="D:/exp/njj-fcbf/NJJNB";
//			String txtname1 ="D:/exp/ReliefF/"+lf[i].getName();
//			AppendToFile.appendMethodB(txtname, "#!/bin/sh"+"\r\n"+"#PBS -N njjsqre-"+String.valueOf(i)+"\r\n"+"#PBS -l cput=2:00:00"+"\r\n"+"#PBS -l walltime=2:00:00"+"\r\n");
//			AppendToFile.appendMethodA(txtname1, "java -jar ReliefF.jar "+lf[i].getName()+"\r\n");
			//			AppendToFile.appendMethodB(txtname, "#!/bin/sh"+"\r\n"+"#PBS -N PC54"+"\r\n"+"#PBS -l cput=2:00:00"+"\r\n"+"#PBS -l walltime=2:00:00"+"\r\n");
			
			AppendToFile.appendMethodA(txtname, "java -jar C.jar "+"0 "+"njjFCBF2 "+lf[i].getName()+"\r\n");
//			AppendToFile.appendMethodA(txtname, "java -jar njjsqrt.jar "+lf[i].getName()+"\r\n");
		}
	}
}
