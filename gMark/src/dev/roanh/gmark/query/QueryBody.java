package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.output.SQL;

public class QueryBody implements SQL{
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

	@Override
	public String toSQL(){
		StringBuilder buffer = new StringBuilder();
		int n = conjuncts.size();
		
		buffer.append("WITH RECURSIVE ");
		for(int i = 0; i < n; i++){
			buffer.append("c");
			buffer.append(i);
			buffer.append("(src, trg) AS ");
			buffer.append(conjuncts.get(i).toSQL());
			
			
			
		}
		
		
		
		
		
		
		
		// TODO Auto-generated method stub
		return null;
	}
}
