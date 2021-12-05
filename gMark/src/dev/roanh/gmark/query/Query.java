package dev.roanh.gmark.query;

import java.util.List;
import java.util.StringJoiner;

public class Query{
	//TODO in gmark a query also stores some stats about itself, but really those can be derrived
	private List<Variable> variables;//specifically the projected ones (LHS)
	private QueryBody body;//TODO in gmark a query can have multiple bodies but this feature appears unused? there is only ever one generated
	
	public Query(List<Conjunct> conjuncts, List<Variable> variables){
		this.body = new QueryBody(conjuncts);
		this.variables = variables;
	}
	
	public Query(QueryBody body, List<Variable> variables){
		this.body = body;
		this.variables = variables;
	}
	
	@Override
	public String toString(){
		StringJoiner lhs = new StringJoiner(",", "(", ")");
		for(Variable var : variables){
			lhs.add(var.toString());
		}
		return lhs.toString() + " ← " + body;
	}
}
