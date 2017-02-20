package weka.me.graph;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import weka.me.*;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.supervised.attribute.*;


public class njjGraph1 {

	public class edge{
		public int v1,v2;
		public double weight;
		public edge(int v1, int v2, double len){
			this.v1 = v1;
			this.v2 = v2;
			weight = len;
		}
	}
	public class adjEdge{
		public int v2;
		public double weight;
		public adjEdge(int v2, double len){
			this.v2 = v2;
			weight = len;
		}
	}
	public int numVertex=0;
	public int numEdge=0;
	private double[][] matrix;
	public edge[] edgeList;
	private int[] vertex;
	public Vector<Vector<adjEdge>> adjList;
    private boolean m_directed = false;
    
	public njjGraph1(){
		
	}
	public njjGraph1(double[][] matrix){
//		if (matrix.length == 0)
//		{
//			System.out.println("null matrix!!");
//			System.exit(0);
//		}
//		if (matrix.length != matrix[0].length){
//			System.out.println("not squared matrix!!");
//			System.exit(0);
//		}
//		this.numVertex = matrix.length;
//		this.numEdge= numVertex*(numVertex-1)/2;
//		this.matrix = matrix;	
//		edgeList = new edge[numEdge];
//		vertex = new int[numVertex];
//		for (int i = 0; i < numVertex; i++){
//			vertex[i]=i;
//		}
//		int edIndex = 0;
//		adjList =new Vector [matrix.length][matrix.length]; 
		for (int i =1; i < matrix.length; i++){
			for (int j =0; j <i;j++){
					if(matrix[i][j]>0){			
//					edge edge1= new edge(i,j,matrix[i][j]);
					setAdj(i,j,matrix[i][j]);
//					edgeList[edIndex]=new edge(i,j,matrix[i][j]);
//					edIndex++;
					}
				
			}
		}
	}

//	public void njjsetWeightedGraph(double matrix[][]){
//
//		edgeList = new edge[numEdge];
//		vertex = new int[numVertex];
//		for (int i = 0; i < numVertex; i++){
//			vertex[i]=i;
//		}
//		int edIndex = 0;
//		for (int i =1; i < numVertex; i++){
//			for (int j =0; j <i;j++){
//				if (matrix[i][j] > 0){
//					
//					edge edge1= new edge(i,j,matrix[i][j]);
//					edgeList[edIndex]=edge1;
//					edIndex++;
//				}
//			}
//		}
//	}
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
		for (int i = 0; i < numVertex; i++){
			for (int j = 0; j <numVertex; j++){
				System.out.print(matrix[i][j]+"\t");
			}
			System.out.println();
		}
		
	}
	public void displayEdgeList(){
		if (edgeList==null){
			System.out.println("no graph!");
			return;
		}
		for (int i = 0; i < numEdge; i++){
			edge e = edgeList[i];
			System.out.println(i+":\t"+e.v1+","+e.v2+": "+e.weight);
		}
	}
	
	public void displayAdjList(){
		if (adjList == null)
			return;
		for (int i = 0; i < adjList.size(); i++){
//			System.out.print(vertex[i]+":\t");
			System.out.print(i+":\t");
			Vector<adjEdge> v = adjList.elementAt(i);
			for (int j = 0; j < v.size(); j++){
				System.out.print(v.elementAt(j).v2+"("+v.elementAt(j).weight+")\t");
			}
			System.out.println();
		}
	}
	
	public  njjGraph1(int numV){


		numVertex = numV;
		numEdge=0;
//		matrix = new double[numV][numV];
//		edgeList = new edge[numV*(numV-1)/2];
//		vertex = new int[numV];
//		for (int i = 0; i < numV; i++){
//			vertex[i]=i;
//		}
		adjList = new Vector(numV);
		for (int i = 0; i < numV; i++){
			Vector ai = new Vector();
			adjList.add(ai);
		}		
	}
	
	
	
	public static void deleteEdge(njjGraph1 tree, edge e){
		Vector<adjEdge> v1adj=tree.adjList.elementAt(e.v1);
		int i;
		for( i=0;i<v1adj.size()-1;i++){
			if(tree.adjList.elementAt(e.v1).elementAt(i).v2==e.v2){
				for(int j=i;j<v1adj.size()-1;j++){
					tree.adjList.elementAt(e.v1).elementAt(j).v2=v1adj.elementAt(j+1).v2;
					tree.adjList.elementAt(e.v1).elementAt(j).weight=v1adj.elementAt(j+1).weight;
				}
			}	
		}
		tree.adjList.elementAt(e.v1).remove(i);
		
		Vector<adjEdge> v2adj=tree.adjList.elementAt(e.v2);
		for( i=0;i<v2adj.size()-1;i++){
			if(tree.adjList.elementAt(e.v2).elementAt(i).v2==e.v1){
				for(int j=i;j<v2adj.size()-1;j++){
					tree.adjList.elementAt(e.v2).elementAt(j).v2=v2adj.elementAt(j+1).v2;
					tree.adjList.elementAt(e.v2).elementAt(j).weight=v2adj.elementAt(j+1).weight;
				}
			}	
		}
		tree.adjList.elementAt(e.v2).remove(i);
	}
	
	public static int findMaxIndex(double[] key){
		double max =-1;
		int maxI = -1;
		for (int i = 0; i < key.length; i++){
			if (max < key[i])
			{
				max = key[i];
				maxI = i;
			}
		}
		return maxI;
	}
	
	public static int findMinIndex(double[] key){
		double min =2;
		int minI = -1;
		for (int i = 0; i < key.length; i++){
			if (min > key[i])
			{
				min = key[i];
				minI = i;
			}
		}
		return minI;
	}
	
	public static void display(int[] a){
		for (int x : a){
			System.out.print(x+"\t");
		}
		System.out.println();
	}
	
	public static void displayVclu(int[] a){
		for (int i=0;i<a.length;i++   ){
			System.out.print(i+":"+a[i]+"\t");
		}
		System.out.println();
	}
	
	public static void display(double[] a){
		for (double x : a){
			System.out.print(x+"\t");
		}
		System.out.println();
	}
	
	public void setEdge(int i, int j, double len){
//		if (matrix == null){
//			System.out.println("no graph");
//		    return;
//		}
		
		
		System.out.print("edge: (" +i+", "+j+", "+len+"):\t");
		edgeList[numEdge]=new edge(i,j,len);
		matrix[i][j] = len;
		matrix[j][i] = len;
		if (adjList != null)
		{
		adjList.elementAt(i).add(new adjEdge(j,len));
		adjList.elementAt(j).add(new adjEdge(i,len));
		
		}
		numEdge++;
	}
	
	public void setAdj(int i, int j, double len){
//		if (adjList != null)
//		{
		adjList.elementAt(i).add(new adjEdge(j,len));
		adjList.elementAt(j).add(new adjEdge(i,len));
		
//		}
//		numEdge++;
	}
	
	public void setAdj5(int i, int j, double len,int[] ranked,int yuzhi){
		if (adjList != null)
		{
		adjList.elementAt(i).add(new adjEdge(j,len));
		adjList.elementAt(j).add(new adjEdge(i,len));
		
		}
		numEdge++;
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
	
	public static boolean NotInQueue(int[] queue,int value){
		boolean flag=true;
		for(int i=0;i<queue.length;i++){
			if(queue[i]==value)
				flag=false;
		}
		return flag;
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
	
	public static njjGraph1 njjprimQ(Instances njjData, int sourceIndex){
		CfsSubsetEval njjCfs = new CfsSubsetEval();
		njjCfs.njjsetm_trInstances(njjData);
		njjGraph1 treeQ = new njjGraph1(njjData.numAttributes()-1);
		int[] black = new int[1];
		int[] white = new int[njjData.numAttributes()-2];
		int[] parent = new int[njjData.numAttributes()-2];
		double[] key = new double[njjData.numAttributes()-2];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < njjData.numAttributes()-2; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			
			key[i]= njjCfs.njjsymmUncertCorr(sourceIndex, index);
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < njjData.numAttributes()-1; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			treeQ.setEdge(parent[maxQ], v, key[maxQ]);
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				double vu=njjCfs.njjsymmUncertCorr(v, u);
				
				if (vu>0 && vu > key[j]){
					key[j] = vu;
					parent[j] = v;
				}
			}
		}		
		return treeQ;
	}
	
	public static njjGraph1 njjprim(njjGraph1 g, int sourceIndex){
		njjGraph1 tree = new njjGraph1(g.numVertex);
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
			int maxQ = findMaxIndex(key);
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
	
	public static njjGraph1 njjprimsu(double [][] g, int sourceIndex){
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 njjprimsu5(double [][] g, int sourceIndex, double[] a,double[] sum){
		int ac=0;
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
			a[ac]=key[maxQ];
			sum[0]+=key[maxQ];
			ac++;
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 PrimMaxTree(double [][] g, int sourceIndex){
//		int ac=0;
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
//			a[ac]=key[maxQ];
//			sum[0]+=key[maxQ];
//			ac++;
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 PrimMaxTree1(double [][] g, int sourceIndex){
//		int ac=0;
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
//			a[ac]=key[maxQ];
//			sum[0]+=key[maxQ];
//			ac++;
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 njjprimsuMIN(double [][] g, int sourceIndex, double[] a){
		int ac=0;
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int minQ = findMinIndex(key);
			if (minQ < 0)
				display(key);
			int v = white[minQ];
			tree.setAdj(parent[minQ], v, key[minQ]);
			a[ac]=key[minQ];
//			sum[0]+=key[maxQ];
			ac++;
			black = insert(black,white[minQ]);
			white = delete(white,minQ);
			key = delete(key,minQ);
			parent = delete(parent,minQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 njjprimsuFCBF(double [][] g, int sourceIndex){
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 njjprimsuFCBF2(double [][] g, int sourceIndex, double[] suic, int[] ranked){
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			
			if ((key[maxQ] >(1-suic[ranked[parent[maxQ]]]))&&(key[maxQ] >(1- suic[ranked[v]])))
				
			tree.setAdj(parent[maxQ], v, key[maxQ]);
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	
	public static njjGraph1 njjprimsu6(double [][] g, int sourceIndex,double[] bian){
//		int ac=0;
		int njjbian=0;
		njjGraph1 tree = new njjGraph1(g.length);
		int[] black = new int[1];
		int[] white = new int[g.length-1];
		int[] parent = new int[g.length-1];
		double[] key = new double[g.length-1];
		black[0] = sourceIndex;
		int index = 0;
		for (int i = 0; i < g.length-1; i++){
			if (index == sourceIndex)
				index++;
			white[i]= index;
			key[i]=g[sourceIndex][index];
			//if (key[i] == 0)
			//	key[i] = Double.MIN_VALUE;
			parent[i] = sourceIndex;
			index++;
		}
		for (int i = 1; i < g.length; i++){
			int maxQ = findMaxIndex(key);
			if (maxQ < 0)
				display(key);
			int v = white[maxQ];
			tree.setAdj(parent[maxQ], v, key[maxQ]);
//			a[ac]=key[maxQ];
//			sum[0]+=key[maxQ];
			bian[njjbian]= key[maxQ];
			njjbian++;
//			ac++;
			black = insert(black,white[maxQ]);
			white = delete(white,maxQ);
			key = delete(key,maxQ);
			parent = delete(parent,maxQ);
			for (int j = 0; j < white.length; j++){
				int u = white[j];
				if (g[v][u]>0 && g[v][u] > key[j]){
					key[j] = g[v][u];
					parent[j] = v;
				}
			}
		}		
		return tree;
	}
	public static void njjMPedgeMax(edge edgeList[],int e){
		edge temp;
		int njjlen=e;
		boolean tag=true;
		while(tag){
			tag=false;
			njjlen--;
			for (int j=1; j <=njjlen; j++ ){
				if(edgeList[j-1].weight < edgeList[j].weight){
					temp = edgeList[j-1];
					edgeList[j-1] =edgeList[j];
					edgeList[j] = temp;
					tag=true;
				}
			}						
		}
	}
	
	public static void njjMPedge(edge edgeList[],int e){//min
		edge temp;
		int njjlen=e;
		boolean tag=true;
		while(tag){
			tag=false;
			njjlen--;
			for (int j=1; j <=njjlen; j++ ){
				if(edgeList[j-1].weight > edgeList[j].weight){
					temp = edgeList[j-1];
					edgeList[j-1] =edgeList[j];
					edgeList[j] = temp;
					tag=true;
				}
			}						
		}
	}
	
	public static void njjMPadjNum(int[][] adjNum,int v){
		int[] temp;
		int njjlen=v;
		boolean tag=true;
		while(tag){
			tag=false;
			njjlen--;
			for (int j=1; j <=njjlen; j++ ){
				if(adjNum[j-1][1] < adjNum[j][1]){
					temp = adjNum[j-1];
					adjNum[j-1] =adjNum[j];
					adjNum[j] = temp;
					tag=true;
				}
			}						
		}
	}
	
	public static void njjMP(int[]a){
		int temp;
		int njjlen=a.length;
		boolean tag=true;
		while(tag){
			tag=false;
			njjlen--;
			for (int j=1; j <=njjlen; j++ ){
				if(a[j-1]< a[j]){
					temp = a[j-1];
					a[j-1] =a[j];
					a[j] = temp;
					tag=true;
				}
			}						
		}
	}
	
//	public static njjGraph1[] cluster(double[] queue, int index){
//		int len = queue.length;
//		double[] result = new double[len-1];
//		System.arraycopy(queue, 0, result, 0, index);
//		System.arraycopy(queue, index+1, result, index, len-index-1);
//		return result;
//	}
	
//	int v1=tree.edgeList[0].v1;
//	int v2=tree.edgeList[0].v2;
//	int[] V1=new int[1];		
//	V1[0]=v1;
//	
//	Vector<adjEdge> adjv1 = tree.adjList.elementAt(v1);
//	boolean[] visited=new boolean [tree.numVertex];
//	int VisitFunc;
//	public static void DFSTraverse(Vector<Vector> adG,int[] v,int v1,int numVertex,boolean[] visited,int[] Vclu,int clu){
////		for(int i=0;i<numVertex;i++)
////			visited[i]=false;
//		for(int i=0;i<numVertex;i++)
//			if(!visited[v])
//				DFS(adG,v,visited,Vclu,clu);		
//	}
	
	public static void VisitFunc(int v,int[] Vclu,int clu){
		Vclu[v]=clu;
	}
	
	public static void VisitFunc5(int v,int[] clu,int[] GG,int[] father,int clusterNum){
		
	}
	public static int FirstAdjVex(Vector<Vector<adjEdge>> adG,int v){
		Vector<adjEdge> Fst= adG.elementAt(v);
		int FstAdj=-1;
		if(Fst.size()!=0)		
			FstAdj=Fst.elementAt(0).v2;
		return FstAdj;
	}
	public static  int nextAdjVex(Vector<Vector<adjEdge>> adG,int v,int w){
		int nextAd=-1;
	
		Vector<adjEdge> Fst= adG.elementAt(v);
		
		for(int i=0;i<Fst.size()-1;i++){
			if(Fst.elementAt(i).v2==w){
					nextAd=Fst.elementAt(i+1).v2;
				break;
			}
		}
		return nextAd;
	}
	
	public static void DFS(Vector<Vector<adjEdge>> adG,int v,boolean[] visited,int[] Vclu,int clu){
		visited[v]=true;
		VisitFunc(v,Vclu,clu);
		int w;
		for(w=FirstAdjVex(adG,v);w>=0;w=nextAdjVex(adG,v,w))
			if(!visited[w])
				DFS(adG,w,visited,Vclu,clu);
	}
	
	public static void DFS5(Vector<Vector<adjEdge>> adG,int v,boolean[] visited,int[] clu,int[] GG,int[] father,int clusterNum){
		visited[v]=true;
		VisitFunc5(v,clu,GG,father,clusterNum);
		int w;
		for(w=FirstAdjVex(adG,v);w>=0;w=nextAdjVex(adG,v,w))
			if(!visited[w])
				DFS5(adG,w,visited,clu,GG,father,clusterNum);
	}
	public static void DFST(njjGraph1 tree,boolean[] visited,int[] Vclu ){
		int clu=-1;
		for(int v=0;v<tree.numVertex;v++){
			if(!visited[v]){
				clu++;
				DFS(tree.adjList,v,visited,Vclu,clu);
			}
		}
	}
	
//	public static void DeleteEdge(Vector<Vector> adG,int v1,int v2){
//		Vector<adjEdge> Fst= adG.elementAt(v1);
//		
//		for(int i=0;Fst.elementAt(i).v2>=0;i++){
//			if(Fst.elementAt(i).v2==v2){
////				Fst.
//				break;
//			}
//		}
//	}
	
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