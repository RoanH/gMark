package dev.roanh.gmark.lang.cq;

import java.util.Objects;

import dev.roanh.gmark.ast.Variable;

public class VarCQ implements Variable{
	private final String name;
	/**
	 * True if this is a free variable, free variables
	 * are projected in the output of a query.
	 */
	private final boolean free;
	
	public VarCQ(Variable variable){
		this(variable.getName(), variable.isFree());
	}
	
	public VarCQ(String name, boolean free){
		this.name = name;
		this.free = free;
	}
	
	@Override
	public String getName(){
		return name;
	}
	
	@Override
	public boolean isFree(){
		return free;
	}
	
	@Override
	public String toString(){
		return getName();
	}
	
	@Override
	public boolean equals(Object obj){
		return obj instanceof VarCQ v && Objects.equals(v.name, name) && v.free == free;
	}
	
	@Override
	public int hashCode(){
		return Objects.hash(name, free);
	}
}
