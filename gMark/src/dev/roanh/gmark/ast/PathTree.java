package dev.roanh.gmark.ast;

import dev.roanh.gmark.lang.QueryLanguageSyntax;

//fragment = atom for leaves
public class PathTree<T extends QueryLanguageSyntax<T>>{
	private final PathTree<T> left;
	private final PathTree<T> right;
//	private final OperationType operation;
	private final T fragment;
	
	private PathTree(PathTree<T> left, PathTree<T> right, /*OperationType operation, */T fragment){
		this.left = left;
		this.right = right;
//		this.operation = operation;
		this.fragment = fragment;
	}
	
	public boolean isLeaf(){//TODO maybe delegate to OP type
		return left == null && right == null;
	}
	
//	public boolean isUnary(){//TODO maybe delegate to OP type
//		return left != null && right == null;
//	}
//	
//	public boolean isBinary(){//TODO maybe delegate to OP type
//		return left != null && right != null;
//	}
	
	public OperationType getOperation(){
		return fragment.getOperationType();
	}
	
	public T getQueryFragment(){
		return fragment;
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
