package dev.roanh.gmark.ast;

public abstract interface QueryFragment{
	
	public abstract OperationType getOperationType();
	
	public abstract QueryTree toAbstractSyntaxTree();
}
