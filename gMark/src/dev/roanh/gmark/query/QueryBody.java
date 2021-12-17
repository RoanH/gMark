package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

public class QueryBody{
	private List<Conjunct> conjuncts;
	
	public QueryBody(List<Conjunct> conjuncts){
		this.conjuncts = conjuncts;
	}
	
	public int getConjunctCount(){
		return conjuncts.size();
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
