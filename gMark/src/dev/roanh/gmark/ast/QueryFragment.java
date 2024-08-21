package dev.roanh.gmark.ast;

import dev.roanh.gmark.lang.QueryLanguageSyntax;

public abstract interface QueryFragment<T extends QueryLanguageSyntax<T>>{
	
	public abstract OperationType getOperationType();
	
	public abstract PathTree<T> toAbstractSyntaxTree();
}
