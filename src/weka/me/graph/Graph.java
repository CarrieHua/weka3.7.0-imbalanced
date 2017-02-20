package weka.me.graph;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

//import weka.Graph;

public class Graph {

	private class edge{
		public String v1,v2;
		public double weight;
		public edge(String v1, String v2, double len){
			this.v1 = v1;
			this.v2 = v2;
			weight = len;
		}
	}
	private class adjEdge{
		public String v2;
		public double weight;
		public adjEdge(String v2, double len){
			this.v2 = v2;
			weight = len;
		}
	}
	private int numVertex=0;
	private int numEdge=0;
	private double[][] matrix;
	private Vector<edge> edgeList;
	private Vector<String> vertex;
	private Vector<Vector> adjList;
    private boolean m_directed = false;
	public Graph(){
		
	}
	public Graph(double[][] matrix){
		if (matrix.length == 0)
		{
			System.out.println("null matrix!!");
			System.exit(0);
		}
		if (matrix.length != matrix[0].length){
			System.out.println("not squared matrix!!");
			System.exit(0);
		}
		this.numVertex = matrix.length-1;
		this.matrix = matrix;
		//njj
//		int edIndex=0;
//		for (int i = 1; i < numVertex; i++){
//			for (int j = 0; j < i;j++){
//				if (matrix[i][j] > 0){
//					double len =matrix[i][j];
//					edgeList.set(edIndex, new edge(vertex.elementAt(i),vertex.elementAt(j),len));
//					edIndex++;
//				}
//				
//			}
//		}
		//njj/
		
	}
	public Graph(int num){
//		System.out.println("success!");
		int numV=num+1;
		numVertex = numV;
		matrix = new double[numV][numV];
		edgeList = new Vector<edge>();
		vertex = new Vector<String>();
		for (int i = 0; i < numV; i++){
			vertex.add(String.valueOf(i));
		}
		adjList = new Vector(numV);
		for (int i = 0; i < numV; i++){
			Vector ai = new Vector();
			adjList.add(ai);
		}		
	}
	
//	public void njjsetWeightedGraph(double matrix[][]){
////		directed = false;
//		numVertex = matrix.length-1;
////		matrix = new double[numV][numV];
//		edgeList = new Vector<edge>();
//		vertex = new Vector<String>();
//		for (int i = 0; i < numVertex; i++){
//			vertex.add(String.valueOf(i));
//		}
//		int edIndex = 0;
//		for (int i =1; i < numVertex; i++){
//			for (int j =0; j <i;j++){
//				if (matrix[i][j] > 0){
//					
//					edge edge1= new edge(String.valueOf(i),String.valueOf(j),matrix[i][j]);
//					edgeList.set(edIndex, new edge(String.valueOf(i),String.valueOf(j),matrix[i][j]));
//					edIndex++;
//				}
//			}
//		}
//	}
	
	public void setUnweightedGraph(int numV,double threshold){
//		directed = false;
		numVertex = numV;
		matrix = new double[numV][numV];
		edgeList = new Vector<edge>();
		vertex = new Vector<String>();
		for (int i = 0; i < numV; i++){
			vertex.add(String.valueOf(i));
		}
		for (int i = 0; i < numV; i++){
			for (int j = i+1; j < numV;j++){
				if (Math.random() > threshold){
					matrix[i][j]=1;
					matrix[j][i]=1;
					edge e_ij = new edge(vertex.elementAt(i),vertex.elementAt(j),1);
					edgeList.add(e_ij);
					numEdge++;
				}
			}
		}
	}
	public void setWeightedGraph(int maxLen){
		//based on the unweighted Graph
		Random r = new Random(47);
		if (numVertex == 0)
			return;
		int edIndex = 0;
		for (int i = 0; i < numVertex; i++){
			for (int j = i+1; j < numVertex;j++){
				if (matrix[i][j] > 0){
					double len =matrix[i][j] = matrix[j][i] = r.nextInt(maxLen)+1;
					edgeList.set(edIndex, new edge(vertex.elementAt(i),vertex.elementAt(j),len));
					edIndex++;
				}
				
			}
		}
		
	}
	public void display(){
		displayMatrix();
		displayEdgeList();
		displayAdjList();
	}
	public void displayMatrix(){
		if (matrix == null){
			System.out.println("no graph!");
			return;
		}
		for (int i = 0; i < matrix.length; i++){
			for (int j = 0; j < matrix[i].length; j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
		
	}
	public void setEdge(int i, int j, double len){
		
//		if(i<1||j<1){
//			System.out.println("error: i and j should >=1");
//		}
//		else if(i>numVertex||j>numVertex){
//			System.out.println("error£ºiand j must < vNum");
//			
//		}
//		else if(i==j){
//			System.out.println("error£ºi==j ");
//		}
//		else 
//			System.out.println("success!");
//		numEdge++;
//		edgeList.add(new edge(String.valueOf(i),String.valueOf(j),len));
		matrix[i][j] = len;
		matrix[j][i] = len;
		if (adjList != null)
		{
		adjList.elementAt(i).add(new adjEdge(String.valueOf(j),len));
		adjList.elementAt(j).add(new adjEdge(String.valueOf(i),len));	
		}
	}
	public void setDirectEdge(int i, int j, double len){
		if (matrix == null){
			System.out.println("no graph");
		    return;
		}
		numEdge++;
		m_directed = true;
		edgeList.add(new edge(String.valueOf(i),String.valueOf(j),len));
		matrix[i][j] = len;
		adjList.elementAt(i).add(new adjEdge(String.valueOf(j),len));				
	}
	public void displayEdgeList(){
		if (edgeList==null){
			System.out.println("no graph!");
			return;
		}
		for (int i = 0; i < edgeList.size(); i++){
			edge e = edgeList.elementAt(i);
			System.out.println(e.v1+","+e.v2+", "+e.weight);
		}
	}
	public void displayAdjList(){
		if (adjList == null)
			return;
		for (int i = 1; i < adjList.size(); i++){
			System.out.print((i)+":\t");
			Vector<adjEdge> v = adjList.elementAt(i);
			for (int j = 0; j < v.size(); j++){
				if(v.elementAt(j).weight==100)
					continue;
				System.out.print(v.elementAt(j).v2+"("+v.elementAt(j).weight+")\t");
			}
			System.out.println();
		}
	} 
	public void generateAdjList(){
		if (matrix == null){
			return;
		}
		adjList = new Vector();
		for (int i = 0; i < numVertex; i++){
			Vector<adjEdge> v1 = new Vector<adjEdge>();
			for (int j = 0; j < numVertex; j++){
				if (matrix[i][j] > 0){
					v1.add(new adjEdge(vertex.elementAt(j),matrix[i][j]));
				}
			}
			adjList.add(v1);
		}
	}
	public static void breadth_first(Graph g,int source){
		int[] color = new int[g.numVertex];// 0-white,1-grey,2-black
		double[] dist = new double[g.numVertex];
		Vector<TreeNode> tree = new Vector<TreeNode>();
		for (int i = 0; i < dist.length; i++){
			dist[i] = Double.MAX_VALUE;
		}
		color[source] = 1;
		dist[source] = 0;
		int[] father = new int[g.numVertex];
		for (int i = 0; i < father.length; i++){
			father[i] = -1;
		}
//		TreeNode root = new TreeNode(g.vertex.elementAt(source));
		LinkedList<Integer> queue = new LinkedList<Integer>();
		queue.add(new Integer(source));
		while (queue.size() > 0){
			int u = (Integer)(queue.poll());
			for (int i = 0; i < g.adjList.elementAt(u).size(); i++){
				String vstr = ((Graph.adjEdge)g.adjList.elementAt(u).elementAt(i)).v2;
				int v = g.getIndex(vstr);
				if (color[v]==0){
					color[v] = 1;
					queue.add(v);
					dist[v] = dist[u]+1;
					father[v]=u;
				}
			}
			color[u] = 2; //black
			System.out.print(u+"\t");
		}
		
	}
	public static void printPath(int[] father, int s,int v){
		if (v==s)
			System.out.println(v);
		else if(father[v] == -1){
			System.out.print("no path from s to v\n");
			
		}
		else{
			printPath(father, s, father[v]);
			System.out.print(v);
		}
		
	}
	private int getIndex(String ver){
		for (int i= 0; i < numVertex; i++){
			if (vertex.elementAt(i).equals(ver))
				return i;
		}
		return -1;
	}
	public static int time = 0;
	public static void depth_first(Graph g){
		int[] color = new int[g.numVertex];
		int[] father = new int[g.numVertex];
		for (int i = 0; i < father.length; i++){
			father[i] = -1;
		}
		double[][] timesamp = new double[g.numVertex][2];
		time = 0;
		for (int i = 0; i < g.numVertex; i++){
			if(color[i] == 0){
				dfs_visit(i,g,color,father,timesamp);
			}
		}
	}
	private static void dfs_visit(int u, Graph g, int[] color, int[] father,double[][] timesamp){
		color[u] = 1;
		time = time+1;
		timesamp[u][0] = time;
		for (int i = 0; i < g.adjList.elementAt(u).size(); i++){
			String str = ((Graph.adjEdge)g.adjList.elementAt(u).elementAt(i)).v2;
			int s = Integer.parseInt(str);
			if (color[s]==0){
				father[s] = u;
				dfs_visit(s,g,color,father,timesamp);
			}
		}
		color[u]= 2;
		time++;
		timesamp[u][1]= time;	
	}
	/**
	 * @param args
	 */
	public static void test(){
//		int num = 10;
		
//		Graph g = new Graph();
//		for (int i = 0; i < 9; i++){
//			System.out.print((char)('a'+i)+"\t");
//		}
//		System.out.print("\n");
//		g.setEdge(0, 1, 4);
//		g.setEdge(1, 2, 8);
//		g.setEdge(3, 2, 7);
//		g.setEdge(4, 3, 9);
//		g.setEdge(5, 4, 10);
//		g.setEdge(5, 3, 14);
//		g.setEdge(2, 5, 4);
//		g.setEdge(5, 6, 2);
//		g.setEdge(6, 7, 1);
//		g.setEdge(6, 8, 6);
//		g.setEdge(7, 8, 7);
//		g.setEdge(8, 2, 2);
//		g.setEdge(1, 7, 11);
//		g.setEdge(0, 7, 8);
//		g.display();
//		double suu[][]=new double[4][4];
//		suu[1][0]=1;
//		suu[2][0]=3;
//		suu[2][1]=2;
//		suu[3][0]=2;
//		suu[3][1]=2;
//		suu[3][2]=2;
//		
//		Graph njjg = new Graph(suu);
//		njjg.njjsetWeightedGraph(suu);
//		njjg.display();
		
		double suu[][]=new double[5][5];
		suu[0][1]=1;
		suu[0][2]=4;
		suu[0][3]=2;
		suu[1][0]=1;
		suu[2][0]=4;
		suu[2][1]=6;
		suu[3][0]=2;
		suu[3][1]=3;
		suu[3][2]=5;
		suu[1][2]=6;
		suu[1][3]=3;
		suu[2][3]=5;
		
		
		Graph njjg = new Graph(suu);
//		njjg.njjsetWeightedGraph(suu);
		njjg.display();
		long st = System.currentTimeMillis();
		Graph tree = prim2(njjg,0);
		long end = System.currentTimeMillis();
		
		System.out.println("time costs: \t"+(end-st));
		tree.display();
		
//		g.setUnweightedGraph(num, -1);
//		g.setWeightedGraph(num);
//		g.display();
//		long st = System.currentTimeMillis();
//		Graph tree = prim2(g,0);
//		long end = System.currentTimeMillis();
//	System.out.println("time costs: \t"+(end-st));
//	tree.display();
		
	}
	
	public static Graph prim2(Graph g, int sourceIndex){
		Graph tree = new Graph(g.numVertex);
		int[] black = new int[1];
		int[] white = new int[g.numVertex-1];
		int[] parent = new int[g.numVertex-1];
		double[] key = new double[g.numVertex-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.numVertex-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g.matrix[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.numVertex; i++){
			int maxQ = findMinIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setEdge(parent[maxQ], v, key[maxQ]);
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g.matrix[v][u]>0 && g.matrix[v][u] > key[j]){
					key[j] = g.matrix[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	public static int findMinIndex(double[] key){
		double min = Double.MAX_VALUE;
		int minI = -1;
		for (int i = 0; i < key.length; i++){
			if (key[i] > 0 && min > key[i] )
			{
				min = key[i];
				minI = i;
			}
		}
		return minI;
	}
	public static int[] insert(int[] queue, int index, int value){
		int[] result = new int[queue.length+1];
		System.arraycopy(queue, 0, result, 0, index);
		result[index] = value;
		System.arraycopy(queue, index, result, index+1, queue.length-index);
		return result;
	}
	public static int[] insert(int[] queue, int value){
		int len = queue.length;
		int[] result = new int[len+1];
		System.arraycopy(queue, 0, result, 0, len);
		result[len] = value;
		return result;
	}
	public static int[] delete(int[] queue, int index){
		int len = queue.length;
		int[] result = new int[len-1];
		System.arraycopy(queue, 0, result, 0, index);
		System.arraycopy(queue, index+1, result, index, len-index-1);
		return result;
	}
	public static double[] delete(double[] queue, int index){
		int len = queue.length;
		double[] result = new double[len-1];
		System.arraycopy(queue, 0, result, 0, index);
		System.arraycopy(queue, index+1, result, index, len-index-1);
		return result;
	}
	public static Graph prim(Graph g, int sourceIndex){
		Graph tree = new Graph(g.numVertex);
		Vector<Integer> black = new Vector<Integer>();
		Vector<Integer> white = new Vector<Integer>();
		for (int i = 0; i < g.numVertex; i++){
			white.add(new Integer(i));
		}
		Integer source = white.elementAt(sourceIndex);
		black.add(source);
		white.remove(source);
		for (int i = 1; i < g.numVertex; i++){
			double minLen = Double.MAX_VALUE;
			int[] minEdge = new int[2];
			int minV = -1;
			for (int j = 0; j < black.size(); j++){
				int st = black.elementAt(j);
				for (int k = 0; k < white.size(); k++){
					int end = white.elementAt(k);
					if (g.matrix[st][end] < minLen && g.matrix[st][end]>0){
						minLen = g.matrix[st][end];
						minEdge[0] = st;
						minEdge[1] = end;
						minV = k;
					}
				}
			}
			tree.setEdge(minEdge[0], minEdge[1], minLen);			
			black.add(white.elementAt(minV));
			white.removeElementAt(minV);
		}
//		tree.display();
		return tree;
		
	}
	public static void display(int[] a){
		for (int x : a){
			System.out.print(x+"\t");
		}
		System.out.println();
	}
	public static void display(double[] a){
		for (double x : a){
			System.out.print(x+"\t");
		}
		System.out.println();
	}
	public static void main(String[] args) {
		Graph G =new Graph(5);
		System.out.println("---------------------------");
		G.setEdge(1,2,0.5);
		G.setEdge(1,3,3);
		G.setEdge(1,4,1.3);
		G.setEdge(4,2,9.2);
		G.setEdge(3,2,0.7);
		G.setEdge(5,2,8.1);
		G.setEdge(5,3,4.5);
		G.setEdge(5,4,0.2);
		G.displayAdjList();
		System.out.println("---------------------------");
		Graph tree = G.prim2(G, 1);
		tree.displayAdjList();	
	}
}
