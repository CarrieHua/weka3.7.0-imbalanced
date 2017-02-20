package weka.me;


import weka.AppendToFile;
import weka.core.Instances;
import weka.core.Utils;
import weka.me.graph.*;

public class test {
	
	public static int[] removeFrom(int[] ranked,int[] del){
		int[] G =new int[ranked.length+1];
		int count=0;
		int j=0;
		for(int i =0;i<ranked.length;i++){
			if(ranked[i] > del[j]){
				G[count]= ranked[i];
				count++;
			}
			else if(ranked[i]== del[j]){
				j++;
			}
			else{
				j++;
				i--;
			}
//			else break;			
		}
		G[G.length-1]=count;
		return G;
	}
	

	


	static int Count1 = 0;
	private static double[][] SUij={{0,3,0,0,0},{3,0,0,5,0},{0,0,0,4,0},{0,5,4,0,6},{0,0,0,6,0}};
	private static double[]suic ={4,2,5,3,1,0.1,0.2,0.3,0.4,0.5};
	private static boolean[] visited;

	
	private static int cluNum=1;

	private static double [][] Single;
	private static double minsu=0;
	private static double sumsu=0;
	private static int del[] ;
	private static int[][]clu ;
	
		
	
	public static void fuzhi( int[] del){
		for(int i = 0 ;i <del.length;i++)
			del[i]=-1;
	}
	
	
	
public static void BFS3(int i, njjGraph1 tree,  int[] ranked, double[] suic) {
		visited[i]=true;
		double suij;
		
		for (int j = 0; j < tree.adjList.elementAt(i).size(); j++) { // 3
			int jj = tree.adjList.elementAt(i).elementAt(j).v2;	
			suij= tree.adjList.elementAt(i).elementAt(j).weight;
			double a =SUij[i][jj];
			if (!visited[jj]) { // 4
				visited[jj] = true;
				if (suij > suic[ranked[i]] && suij> suic[ranked[jj]]) {	
					if(suic[ranked[i]]>suic[ranked[jj]]){
						del[Count1]=ranked[jj];
						System.out.print(ranked[jj]+"\t");}
					else{
						del[Count1]= ranked[i];
						System.out.print(ranked[i]+"\t");}
					
					Count1++;
				}
				BFS3(jj, tree, ranked, suic);
				
			}
		}
	}

public static  void BFS5(int i, njjGraph1 tree,  int[] ranked) {
	visited[i]=true;
	double suij;
	clu[i][0]=cluNum;
	
	for (int j = 0; j < tree.adjList.elementAt(i).size(); j++) { // 3
		int jj = tree.adjList.elementAt(i).elementAt(j).v2;	
		suij= tree.adjList.elementAt(i).elementAt(j).weight;
		if (!visited[jj]) { // 4
			visited[jj] = true;
			if (suij >= suic[ranked[jj]] && suic[ranked[i]]>= suic[ranked[jj]]) {	
				
					del[Count1]=ranked[jj];
					Count1++;
					clu[jj][0]=cluNum;
					clu[jj][1]=1;
			}
				else if(suij >= suic[ranked[i]] && suic[ranked[jj]]>= suic[ranked[i]]){
					del[Count1]= ranked[i];
				Count1++;
				clu[jj][0]=cluNum;
				clu[i][1]=1;
			}
				else
				{
					cluNum++;
					clu[jj][0]=cluNum;
				}
			BFS5(jj, tree, ranked);				
		}
	}
}


public static   void single(int[] ranked,int yuzhi){
	int[][] cluster=new int[cluNum+1][yuzhi];
	for(int i=0;i<cluNum+1;i++)
		for(int j=0;j<yuzhi;j++)
			cluster[i][j]=-1;
	
	int[][] daibiao=new int[cluNum+1][yuzhi];
	for(int i=0;i<cluNum+1;i++)
		for(int j=0;j<yuzhi;j++)
			daibiao[i][j]=-1;
	
	int [] eachCluNum= new int[yuzhi];
	int [] eachCluR= new int[yuzhi];
	
	
	
	for(int i=0;i<yuzhi;i++){
		cluster[clu[i][0]][eachCluNum[clu[i][0]]]=i;
		eachCluNum[clu[i][0]]++;
		
		if(clu[i][1]==0){
			daibiao[clu[i][0]][eachCluR[clu[i][0]]]=i;
		eachCluR[clu[i][0]]++;
		}
	}
	
	int sig =0;
	for(int i=1;i<yuzhi;i++){
		if(eachCluNum[i]==1){
			sig++;
			System.out.print(cluster[i][0]+": "+suic[ranked[cluster[i][0]]]+"\t");
		}
	}
	
	System.out.print("\n");
	
	System.out.print(suic[ranked[yuzhi]]);
	
	System.out.print("\n");
	
	double sum=0;
	int sf=0;
	
	for(int i=1;i<yuzhi;i++){
		if(eachCluNum[i]> 1){
		for(int j=0;j<eachCluR[i];j++){
			sum+=suic[ranked[daibiao[i][j]]];
			sf++;
			}
		}
	}
	
	double Rave= sum/(double)sf;
	
	System.out.print(Rave);
	
	System.out.print("\n");
	System.out.print(sf+sig);
	
	
	
	
	AppendToFile.appendMethodA("D:/FASTdef6c/su.txt", ","+sig+",");
	
}

public static void njjMP(int a[]) {
	int temp;
	int njjlen = a.length;
	boolean tag = true;
	while (tag) {
		tag = false;
		njjlen--;
		for (int j = 1; j <= njjlen; j++) {
			if (a[j - 1] < a[j]) {
				temp = a[j - 1];
				a[j - 1] = a[j];
				a[j] = temp;
				tag = true;
			}
		}
	}
}


	
public static void main(String[] argv) {
	
	
	
	
	
	njjGraph1 njjg = new njjGraph1();	
	njjGraph1 tree1 = njjg.PrimMaxTree(SUij, 0);
	tree1.displayAdjList();
	
	int njjFeatureNum=10;
	int yuzhi=5;
		
	double[] suicc = {1,3,0,2,4,4.9,4.8,4.7,4.6,4.5};
	int[] ranked = Utils.sort(suicc);  
		
		
		visited = new boolean[yuzhi];
		del = new int[yuzhi+1];
		fuzhi(del);
		Count1=0;
		System.out.print("\n\n");
		
		
		clu = new int[yuzhi][2];
		BFS5(0,tree1,ranked);
		single(ranked,yuzhi);
		
		System.out.print("\n\n");
		
		int[] yuzhiAtr =new int[yuzhi];
		for(int i=0;i<yuzhi;i++){
			yuzhiAtr[i]=ranked[i];
		}
		
		
//		int[] rankedyuzhiAtr = Utils.sort(yuzhiAtr);
		
		njjMP(del);  //from Max. to Min
		for(int i=0;i<del.length; i++)
		System.out.print(del[i]+"\t");
		System.out.print("\n\n");
		
		njjMP(yuzhiAtr);
		
		
		
		int[] GGG = removeFrom(yuzhiAtr,del);
		
		int sf = GGG[GGG.length-1];
		GGG[sf] = njjFeatureNum;
		
	
	}

	

}
