package weka;
import java.io.File;
public class TestTest {
	public static void main(String[] args) {
		File file = new File("/home/njj/workspace/wekaclustering4/dataset");
		File[] lf = file.listFiles();
		for(int i=0; i<lf.length; i++){
			if(lf[i].getName().indexOf(".arff~")!=-1||lf[i].getName().indexOf(".arff~~")!=-1||lf[i].getName().indexOf(".arff~~~")!=-1)
				continue;
			else AppendToFile.appendMethodA("detasetName", lf[i].getName()+"\n");
//			System.out.println(lf[i].getName());
		}
		int c=fileTest.readarffgetFeaatureNum("/home/njj/workspace/wekaclustering4/a.arff");
		int k=kOfkmeans.getClustereK("/home/njj/workspace/wekaclustering4/a.arff");
		System.out.println(c);
		System.out.println(k);
	}	
}


