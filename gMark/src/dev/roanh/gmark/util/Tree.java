package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Tree<T>{
	private List<Tree<T>> children = new ArrayList<Tree<T>>();
	private Tree<T> parent = null;
	private T data;
	
	public Tree(T data){
		this.data = data;
	}
	
	public void forEach(Consumer<Tree<T>> nodeVisitor){
		nodeVisitor.accept(this);
		for(Tree<T> child : children){
			child.forEach(nodeVisitor);
		}
	}
	
	public T getData(){
		return data;
	}
	
	public void addChild(Tree<T> node) throws IllegalArgumentException{
		if(node.parent != null){
			throw new IllegalArgumentException("Node already has a parent.");
		}
		
		children.add(node);
		node.parent = this;
	}
	
	public List<Tree<T>> getChildren(){
		return children;
	}
	
	public int getDepth(){
		return parent == null ? 0 : (1 + parent.getDepth());
	}
}
