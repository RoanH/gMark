package dev.roanh.gmark.conjunct.cpq;

public class IntersectionCPQ implements CPQ{
	private CPQ first;
	private CPQ second;
	
	public IntersectionCPQ(CPQ first, CPQ second){
		this.first = first;
		this.second = second;
	}
	
	@Override
	public String toString(){
		return "(" + first + " ∩ " + second + ")";
	}
}
