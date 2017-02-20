package weka.me.graph;

import java.util.LinkedList;
import java.util.Random;
import java.util.Vector;

import weka.me.*;
import weka.attributeSelection.CfsSubsetEval;
import weka.core.Instances;
import weka.filters.supervised.attribute.*;


public class njjGraph2 {

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
    
	public njjGraph2(){
		
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
	
	public void display(){
		displayMatrix();
		displayEdgeList();
		displayAdjList();
	}
	
	public  njjGraph2(int numV){


		numVertex = numV;
		numEdge=0;
		adjList = new Vector(numV);
		for (int i = 0; i < numV; i++){
			Vector ai = new Vector();
			adjList.add(ai);
		}		
	}
	
	

	
	public void setAdj(int i, int j, double len){
		adjList.elementAt(i).add(new adjEdge(j,len));
		adjList.elementAt(j).add(new adjEdge(i,len));		
	}
	
	
	public static void main(String[] args) {

		njjGraph2 G =new njjGraph2(5);
		System.out.println("---------------------------");
		G.setAdj(0, 1, 5);
		G.setAdj(0, 2, 3);
		G.setAdj(3, 1, 4);
		G.setAdj(4, 1, 6);
		G.displayAdjList();
		System.out.println("---------------------------");
	
	}
}