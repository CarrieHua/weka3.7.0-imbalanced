package weka.me.graph;

import java.util.Vector;

public class TreeNode {

	public String field;
	public TreeNode father;
	public Vector<TreeNode> children;
	public TreeNode(String name){
		field = name;
	}
	public static void display(TreeNode root){
		if (root == null || root.field == null){
			return;
		}
		System.out.println(root.field);
		if (root.children == null)
			return;
		for (int i = 0; i < root.children.size(); i++){
			display(root.children.elementAt(i));
		}
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
