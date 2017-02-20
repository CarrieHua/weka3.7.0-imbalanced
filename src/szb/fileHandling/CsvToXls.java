/**
 * 
 */
package szb.fileHandling;

import java.io.File;

import jxl.Workbook;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

import com.csvreader.CsvReader;

/**
 * @author szb
 *
 */
public class CsvToXls {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String doc[] = { "Orig", "UnderSample", "OverSample", "SMOTE",
				"MetaCost", "Bagging", "Boosting", "EM1vs1","RUSBoost"};
		String srcFileName[] = { "nb", "j48", "ripper", "rf", "smo","bn","ibk" };
		String destFileName[] = { "AUC_Results.xls", "TPR_Results.xls",
				"TNR_Results.xls", "GMeans_Results.xls", "Recall_Results.xls",
				"Presicion_Results.xls", "FMeasure_Results.xls",
				"Accuracy_Results.xls", "Time_Results.xls" };
		String destSheetName[] = { "Naive Bayes", "J48", "Ripper",
				"Random Forest", "SMO","BayesNet","IBk"};
		String srcPath = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelDataResults\\";
		String destPath = "F:\\DataSets\\KeelDataSets\\KeelData\\KeelDataResults\\SumForResults\\";

		String column[] = {"data","ecoli-0_vs_1", "ecoli1", "ecoli2", "ecoli3",
				 "glass-0-1-2-3_vs_4-5-6", "glass0", "glass1", "glass6",
				 "haberman", "iris0", "new-thyroid1", "newthyroid2",
				 "page-blocks0", "pima", "segment0", "vehicle0", "vehicle1",
				 "vehicle2", "vehicle3", "wisconsin", "yeast1", "yeast3",
				 "abalone19", "abalone9-18",
				 "ecoli-0-1-3-7_vs_2-6", "ecoli4", "glass-0-1-6_vs_2",
				 "glass-0-1-6_vs_5", "glass2", "glass4", "glass5",
				 "page-blocks-1-3_vs_4", "shuttle-c0-vs-c4", "shuttle-c2-vs-c4",
				 "vowel0", "yeast-0-5-6-7-9_vs_4", "yeast-1-2-8-9_vs_7",
				 "yeast-1-4-5-8_vs_7", "yeast-1_vs_7", "yeast-2_vs_4",
				 "yeast-2_vs_8", "yeast4", "yeast5", "yeast6",
				 "cleveland-0_vs_4", "ecoli-0-1-4-6_vs_5",
					"ecoli-0-1-4-7_vs_2-3-5-6", "ecoli-0-1-4-7_vs_5-6",
					"ecoli-0-1_vs_2-3-5", "ecoli-0-1_vs_5", "ecoli-0-2-3-4_vs_5",
					"ecoli-0-2-6-7_vs_3-5", "ecoli-0-3-4-6_vs_5",
					"ecoli-0-3-4-7_vs_5-6", "ecoli-0-3-4_vs_5", "ecoli-0-4-6_vs_5",
					"ecoli-0-6-7_vs_3-5", "ecoli-0-6-7_vs_5", "glass-0-1-4-6_vs_2",
					"glass-0-1-5_vs_2", "glass-0-4_vs_5", "glass-0-6_vs_5",
					"led7digit-0-2-4-5-6-7-8-9_vs_1", "yeast-0-2-5-6_vs_3-7-8-9",
					"yeast-0-2-5-7-9_vs_3-6-8", "yeast-0-3-5-9_vs_7-8"
					};

		try {
			//构造excel表
			WritableWorkbook AUC = Workbook.createWorkbook(new File(destPath
					+ destFileName[0]));
			WritableWorkbook TPR = Workbook.createWorkbook(new File(destPath
					+ destFileName[1]));
			WritableWorkbook TNR = Workbook.createWorkbook(new File(destPath
					+ destFileName[2]));
			WritableWorkbook GMeans = Workbook.createWorkbook(new File(destPath
					+ destFileName[3]));
			WritableWorkbook Recall = Workbook.createWorkbook(new File(destPath
					+ destFileName[4]));
			WritableWorkbook Precision = Workbook.createWorkbook(new File(
					destPath + destFileName[5]));
			WritableWorkbook FMeasure = Workbook.createWorkbook(new File(
					destPath + destFileName[6]));
			WritableWorkbook Accuracy = Workbook.createWorkbook(new File(
					destPath + destFileName[7]));
			WritableWorkbook Time = Workbook.createWorkbook(new File(destPath
					+ destFileName[8]));

			for (int k = 0; k < destSheetName.length; k++) {
				//为excel表添加工作簿
				WritableSheet sheet1 = AUC.createSheet(destSheetName[k], k);
				WritableSheet sheet2 = TPR.createSheet(destSheetName[k], k);
				WritableSheet sheet3 = TNR.createSheet(destSheetName[k], k);
				WritableSheet sheet4 = GMeans.createSheet(destSheetName[k], k);
				WritableSheet sheet5 = Recall.createSheet(destSheetName[k], k);
				WritableSheet sheet6 = Precision.createSheet(destSheetName[k],
						k);
				WritableSheet sheet7 = FMeasure
						.createSheet(destSheetName[k], k);
				WritableSheet sheet8 = Accuracy
						.createSheet(destSheetName[k], k);
				WritableSheet sheet9 = Time.createSheet(destSheetName[k], k);
				//为工作簿添加第一列内容，默认工作簿的第一列为0列
				for (int i = 0; i < column.length; i++) {
					Label label1 = new Label(0, i, column[i]);
					sheet1.addCell(new Label(0, i, column[i]));
					sheet2.addCell(new Label(0, i, column[i]));
					sheet3.addCell(new Label(0, i, column[i]));
					sheet4.addCell(new Label(0, i, column[i]));
					sheet5.addCell(new Label(0, i, column[i]));
					sheet6.addCell(new Label(0, i, column[i]));
					sheet7.addCell(new Label(0, i, column[i]));
					sheet8.addCell(new Label(0, i, column[i]));
					sheet9.addCell(new Label(0, i, column[i]));
				}
				//为工作簿添加第一行内容，默认工作簿的第一行为0行
				for (int j = 0; j < doc.length; j++) {
					sheet1.addCell(new Label(j + 1, 0, doc[j]));
					sheet2.addCell(new Label(j + 1, 0, doc[j]));
					sheet3.addCell(new Label(j + 1, 0, doc[j]));
					sheet4.addCell(new Label(j + 1, 0, doc[j]));
					sheet5.addCell(new Label(j + 1, 0, doc[j]));
					sheet6.addCell(new Label(j + 1, 0, doc[j]));
					sheet7.addCell(new Label(j + 1, 0, doc[j]));
					sheet8.addCell(new Label(j + 1, 0, doc[j]));
					sheet9.addCell(new Label(j + 1, 0, doc[j]));
				}
                
				for (int i = 0; i < doc.length; i++) {
					String srcFile = srcPath + doc[i] + "//" + srcFileName[k]
							+ ".csv";
					CsvReader reader = new CsvReader(srcFile);
					int row = 0;
					while (reader.readRecord()) {
						//为工作簿的row+1行i+1列添加内容
						sheet1.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(1))));
						sheet2.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(2))));
						sheet3.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(3))));
						sheet4.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(4))));
						sheet5.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(5))));
						sheet6.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(6))));
						sheet7.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(7))));
						sheet8.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(8))));
						sheet9.addCell(new Number(i + 1, row+1, Double
								.parseDouble(reader.get(9))));
						row = row + 1;
					}
				}
			}

			AUC.write();
			AUC.close();
			TPR.write();
			TPR.close();
			TNR.write();
			TNR.close();
			GMeans.write();
			GMeans.close();
			Recall.write();
			Recall.close();
			Precision.write();
			Precision.close();
			FMeasure.write();
			FMeasure.close();
			Accuracy.write();
			Accuracy.close();
			Time.write();
			Time.close();

		} catch (Exception e1) {
			e1.printStackTrace();
		}

	}

}
