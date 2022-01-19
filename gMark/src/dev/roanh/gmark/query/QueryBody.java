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
		for(int i = 0; i < n; i++){
			buffer.append('c');
			buffer.append(i);
			buffer.append("(src, trg) AS ");
			buffer.append(conjuncts.get(i).toPartialSQL());
			if(i < n - 1){
				buffer.append(", ");
			}
		}
		
		
		
		
		//return "(SELECT edge.src, edge.src FROM edge UNION SELECT edge.trg, edge.trg FROM edge UNION " + cpq.toSQL() + ")";

//		file << ", c" << c << "(src, trg) AS (";
//        file << "SELECT src, trg FROM c" << c - 1 << " UNION SELECT head.src, tail.trg FROM c" << c -1 << " as head, c" << c << " as tail WHERE head.trg = tail.src) ";
//        c++;
		
		//TODO sel distinct
		
		
		buffer.append("FROM ");
		for(int i = 0; i < n; i++){
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
