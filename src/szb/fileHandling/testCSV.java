/**
 * 
 */
package szb.fileHandling;

import com.csvreader.CsvReader;

/**
 * @author szb
 *
 */
public class testCSV {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String filename = "F:\\Test\\Results\\Orig\\nb.csv";
		try {
			CsvReader reader = new CsvReader(filename);
			while (reader.readRecord()) {
				int column = reader.getColumnCount();
				for (int i = 0; i < column; i++) {
					System.out.print(reader.get(i) + ",");
				}
				System.out.println();
			}
			long currentrecord = reader.getCurrentRecord();
			System.out.println(currentrecord);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
