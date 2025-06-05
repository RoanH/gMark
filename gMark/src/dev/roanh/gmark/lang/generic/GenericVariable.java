package dev.roanh.gmark.lang.generic;

import dev.roanh.gmark.ast.Variable;

public final class GenericVariable implements Variable{
	public static final Variable SRC = new GenericVariable("src");
	public static final Variable TRG = new GenericVariable("trg");
	private final String name;
	
	private GenericVariable(String name){
		this.name = name;
	}
	
	@Override
	public String getName(){
		return name;
	}

	@Override
	public boolean isFree(){
		return true;
	}
}
