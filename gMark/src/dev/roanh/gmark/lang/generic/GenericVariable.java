package dev.roanh.gmark.lang.generic;

import dev.roanh.gmark.ast.QueryVariable;

public final class GenericVariable implements QueryVariable{
	public static final QueryVariable SRC = new GenericVariable("src");
	public static final QueryVariable TRG = new GenericVariable("trg");
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
