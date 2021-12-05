package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

public class QueryBody{
	private List<Conjunct> conjuncts;
	
	public QueryBody(List<Conjunct> conjuncts){
		this.conjuncts = conjuncts;
	}
	
	@Override
	public String toString(){
		StringJoiner joiner = new StringJoiner(",");
		for(Conjunct conj : conjuncts){
			joiner.add(conj.toString());
		}
		return joiner.toString();
	}
}
