package dev.roanh.gmark.query;

import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.output.SQL;

public class Query implements SQL{
	private List<Variable> variables;//specifically the projected ones (LHS)
	/**
	 * All the bodies for this query. Note that currently
	 * only queries with a single body are ever generated.
	 */
	private List<QueryBody> bodies;
	
	public Query(List<Conjunct> conjuncts, List<Variable> variables, Selectivity selectivity, QueryShape shape){
		this(new QueryBody(conjuncts, selectivity, shape), variables);
	}
	
	public Query(QueryBody body, List<Variable> variables){
		this.bodies = Collections.singletonList(body);
		this.variables = variables;
	}
	
	public boolean hasShape(QueryShape shape){
		return bodies.stream().anyMatch(body->body.getShape().equals(shape));
	}
	
	public List<QueryShape> getShapes(){
		return bodies.stream().map(QueryBody::getShape).collect(Collectors.toList());
	}
	
	public boolean hasSelectivity(Selectivity selectivity){
		return bodies.stream().anyMatch(body->body.getSelectivity().equals(selectivity));
	}
	
	public List<Selectivity> getSelectivities(){
		return bodies.stream().map(QueryBody::getSelectivity).collect(Collectors.toList());
	}
	
	public int getMinConjuncts(){
		return bodies.stream().mapToInt(QueryBody::getConjunctCount).min().orElse(0);
	}
	
	public int getMaxConjuncts(){
		return bodies.stream().mapToInt(QueryBody::getConjunctCount).max().orElse(0);
	}
	
	public boolean isBinary(){
		return getArity() == 0;
	}
	
	public int getArity(){
		return variables.size();
	}
	
	public List<QueryBody> getBodies(){
		return bodies;
	}
	
	/**
	 * Gets the number of bodies that make up this query.
	 * @return The number of bodies in this query.
	 */
	public int getBodyCount(){
		return bodies.size();
	}
	
	@Override
	public String toString(){
		StringJoiner lhs = new StringJoiner(",", "(", ")");
		for(Variable var : variables){
			lhs.add(var.toString());
		}
		
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < bodies.size(); i++){
			buffer.append(lhs.toString());
			buffer.append(" ← ");
			buffer.append(bodies.get(i).toString());
			if(i < bodies.size() - 1){
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

	@Override
	public String toSQL(){
		StringBuffer buffer = new StringBuffer();
		for(int i = 0; i < bodies.size(); i++){
			buffer.append(bodies.get(i).toSQL(variables));
			if(i < bodies.size() - 1){
				buffer.append(" UNION ");
			}
		}
		buffer.append(";");
		return buffer.toString();
	}
}
