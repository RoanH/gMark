package dev.roanh.gmark.lang.cq;

public class VarCQ{
	private final String name;
	private final boolean free;
	
	public VarCQ(String name, boolean free){
		this.name = name;
		this.free = free;
	}
	
	public String getName(){
		return name;
	}
	
	public boolean isFree(){
		return free;
	}
}
