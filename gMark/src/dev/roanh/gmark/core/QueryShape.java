package dev.roanh.gmark.core;

public enum QueryShape{
	CHAIN("chain"),
	STAR("star"),
	CYCLE("cycle"),
	STARCHAIN("starchain");
	
	private final String name;
	
	private QueryShape(String name){
		this.name = name;
	}
	
	public static QueryShape getByName(String name){
		for(QueryShape shape : values()){
			if(shape.name.equals(name)){
				return shape;
			}
		}
		return null;
	}
}
