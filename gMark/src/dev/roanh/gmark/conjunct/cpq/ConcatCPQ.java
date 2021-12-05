package dev.roanh.gmark.conjunct.cpq;

import java.util.List;
import java.util.StringJoiner;

public class ConcatCPQ implements CPQ{
	private List<CPQ> cpq;
	
	public ConcatCPQ(List<CPQ> cpq){
		this.cpq = cpq;
	}
	
	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner("◦", "(", ")");
		for(CPQ item : cpq){
			builder.add(item.toString());
		}
		return builder.toString();
	}
}
