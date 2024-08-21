package dev.roanh.gmark.ast;

import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.util.IndentWriter;

//fragment = atom for leaves
public class PathTree<T extends QueryLanguageSyntax<T>>{
	private final PathTree<T> left;
	private final PathTree<T> right;
	private final T fragment;
	
	private PathTree(PathTree<T> left, PathTree<T> right, T fragment){
		this.left = left;
		this.right = right;
		this.fragment = fragment;
	}
	
	public boolean isLeaf(){
		return left == null && right == null;
	}
	
	public boolean isUnary(){
		return getOperation().isUnary();
	}
	
	public boolean isBinary(){
		return getOperation().isBinary();
	}
	
	public OperationType getOperation(){
		return fragment.getOperationType();
	}
	
	public T getQueryFragment(){
		return fragment;
	}
	
	protected void writeAST(IndentWriter writer){
		if(isLeaf()){
			writer.println(fragment.toString());
		}else{
			writer.println(getOperation().toString(), 2);
			writer.print("- ");
			left.writeAST(writer);
			if(right != null){
				writer.print("- ");
				right.writeAST(writer);
			}
			
			writer.decreaseIndent(2);
		}
	}
	
	@Override
	public String toString(){
		IndentWriter writer = new IndentWriter();
		writeAST(writer);
		return writer.toString();
	}
	
	public static <T extends QueryLanguageSyntax<T>> PathTree<T> ofAtom(T fragment){
		return new PathTree<T>(null, null, fragment);
	}
	
	public static <T extends QueryLanguageSyntax<T>> PathTree<T> ofUnary(PathTree<T> left, T fragment){
		return new PathTree<T>(left, null, fragment);
	}

	public static <T extends QueryLanguageSyntax<T>> PathTree<T> ofBinary(PathTree<T> left, PathTree<T> right, T fragment){
		return new PathTree<T>(left, right, fragment);
	}
}
