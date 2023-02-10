package dev.roanh.gmark.util;

import java.util.List;
import java.util.function.Consumer;

public class Tree<T>{
	private List<Tree<T>> children;
	private Tree<T> parent;
	private T data;
	
	public void forEach(Consumer<Tree<T>> nodeVisitor){
		nodeVisitor.accept(this);
		for(Tree<T> child : children){
			child.forEach(nodeVisitor);
		}
	}
	
	public T getData(){
		return data;
	}
}
