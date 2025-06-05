package dev.roanh.gmark.ast;

public abstract interface Variable{
	
	public abstract String getName();
	
	public abstract boolean isFree();

	public default boolean isBound(){
		return !isFree();
	}
}
