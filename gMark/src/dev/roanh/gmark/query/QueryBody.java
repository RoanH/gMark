package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.output.SQL;

public class QueryBody implements SQL{
	private List<Conjunct> conjuncts;
	
	public QueryBody(List<Conjunct> conjuncts) throws IllegalArgumentException{
		this.conjuncts = conjuncts;
		if(conjuncts.isEmpty()){
			throw new IllegalArgumentException("Query body cannot have no conjuncts.");
		}
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
		int extra = 0;
		for(int i = 0; i < n; i++){
			Conjunct conj = conjuncts.get(i);
			
			buffer.append('c');
			buffer.append(i);
			buffer.append("(src, trg) AS ");
			if(conj.hasStar()){
				buffer.append("(SELECT edge.src, edge.src FROM edge UNION SELECT edge.trg, edge.trg FROM edge UNION ");
			}
			buffer.append(conj.toPartialSQL());
			if(conj.hasStar()){
				buffer.append("), c");
				buffer.append(n + extra);
				buffer.append("(src, trg) AS (SELECT src, trg FROM c");
				buffer.append(i);
				buffer.append("UNION SELECT head.src, tail.trg FROM c");
				buffer.append(i);
				buffer.append(" AS head, c");
				buffer.append(n + extra);
				buffer.append(" AS tail WHERE head.trg = tail.src)");
				extra++;
			}
			
			if(i < n - 1){
				buffer.append(", ");
			}
		}
		

		//TODO sel distinct
		
		
		buffer.append("FROM ");
		for(int i = 0; i < n + extra; i++){
			buffer.append('c');
			buffer.append(i);
			if(i < n - 1){
				buffer.append(", ");
			}
		}
		
		//buffer.append("WHERE")
		
		
		
		// TODO Auto-generated method stub
		return null;
	}
}
