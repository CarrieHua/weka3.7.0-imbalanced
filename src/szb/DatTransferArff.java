package szb;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;

public class DatTransferArff {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		 String docNames[] = { "ecoli-0_vs_1", "ecoli1", "ecoli2", "ecoli3",
//		 "glass-0-1-2-3_vs_4-5-6", "glass0", "glass1", "glass6",
//		 "haberman", "iris0", "new-thyroid1", "newthyroid2",
//		 "page-blocks0", "pima", "segment0", "vehicle0", "vehicle1",
//		 "vehicle2", "vehicle3", "wisconsin", "yeast1", "yeast3" };
		
//		 String docNames[]={
//		 "abalone19", "abalone9-18",
//		 "ecoli-0-1-3-7_vs_2-6", "ecoli4", "glass-0-1-6_vs_2",
//		 "glass-0-1-6_vs_5", "glass2", "glass4", "glass5",
//		 "page-blocks-1-3_vs_4", "shuttle-c0-vs-c4", "shuttle-c2-vs-c4",
//		 "vowel0", "yeast-0-5-6-7-9_vs_4", "yeast-1-2-8-9_vs_7",
//		 "yeast-1-4-5-8_vs_7", "yeast-1_vs_7", "yeast-2_vs_4",
//		 "yeast-2_vs_8", "yeast4", "yeast5", "yeast6"
//		 }
		String docNames[] = { "cleveland-0_vs_4", "ecoli-0-1-4-6_vs_5",
				"ecoli-0-1-4-7_vs_2-3-5-6", "ecoli-0-1-4-7_vs_5-6",
				"ecoli-0-1_vs_2-3-5", "ecoli-0-1_vs_5", "ecoli-0-2-3-4_vs_5",
				"ecoli-0-2-6-7_vs_3-5", "ecoli-0-3-4-6_vs_5",
				"ecoli-0-3-4-7_vs_5-6", "ecoli-0-3-4_vs_5", "ecoli-0-4-6_vs_5",
				"ecoli-0-6-7_vs_3-5", "ecoli-0-6-7_vs_5", "glass-0-1-4-6_vs_2",
				"glass-0-1-5_vs_2", "glass-0-4_vs_5", "glass-0-6_vs_5",
				"led7digit-0-2-4-5-6-7-8-9_vs_1", "yeast-0-2-5-6_vs_3-7-8-9",
				"yeast-0-2-5-7-9_vs_3-6-8", "yeast-0-3-5-9_vs_7-8" };
		String srcDoc = "F:\\DataSets\\KeelDataSets\\KeelData\\imb_IRhigherThan9p2\\DatData\\";
		String desDoc = "F:\\DataSets\\KeelDataSets\\KeelData\\imb_IRhigherThan9p2\\ArffData\\";

		for (int i = 0; i < docNames.length; i++) {
			String srcFile = srcDoc + docNames[i] + ".dat";
			String desFile = desDoc + docNames[i] + ".arff";
			try {
				BufferedReader reader = new BufferedReader(new FileReader(
						srcFile));
				BufferedWriter writer = new BufferedWriter(new FileWriter(
						desFile));
				String line = reader.readLine();
				while (line != null) {
					if (!line.startsWith("@input")
							&& !line.startsWith("@output")) {
						line = line + "\n";
						writer.write(line);
					}
					line = reader.readLine();
				}
				reader.close();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
