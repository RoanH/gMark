/*
 * gMark: A domain- and query language-independent graph instance and query workload generator.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.StringJoiner;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Represents the body of a query, this is essentially
 * the query without the projected head variables.
 * @author Roan
 * @see Query
 */
public class QueryBody implements OutputXML{
	/**
	 * The conjuncts in this query body.
	 */
	private List<Conjunct> conjuncts;
	/**
	 * The selectivity of this query body.
	 */
	private Selectivity selectivity;
	/**
	 * The shape of this query body.
	 */
	private QueryShape shape;
	
	/**
	 * Constructs a new query body with the given conjuncts, selectivity and shape.
	 * @param conjuncts The conjuncts for this query body.
	 * @param selectivity The selectivity of this query body.
	 * @param shape The shape of this query body.
	 * @throws IllegalArgumentException When the list of conjuncts is empty.
	 */
	public QueryBody(List<Conjunct> conjuncts, Selectivity selectivity, QueryShape shape) throws IllegalArgumentException{
		this.conjuncts = conjuncts;
		if(conjuncts.isEmpty()){
			throw new IllegalArgumentException("Query body cannot have no conjuncts.");
		}
		this.selectivity = selectivity;
		this.shape = shape;
	}
	
	/**
	 * Gets the number of conjuncts in this query body.
	 * @return The number of conjuncts in this query body.
	 */
	public int getConjunctCount(){
		return conjuncts.size();
	}
	
	/**
	 * Gets the multiplicity of this query body. This is computed
	 * as the fraction of all conjuncts that has a Kleene star above it.
	 * @return The multiplicity of this query body.
	 */
	public double getMultiplicity(){
		long stars = conjuncts.stream().filter(Conjunct::hasStar).count();
		return ((double)stars) / conjuncts.size();
	}
	
	/**
	 * Gets the selectivity of this query body.
	 * @return The selectivity of this query body.
	 */
	public Selectivity getSelectivity(){
		return selectivity;
	}
	
	/**
	 * Gets the shape of this query body.
	 * @return The shape of this query body.
	 */
	public QueryShape getShape(){
		return shape;
	}
	
	@Override
	public String toString(){
		StringJoiner joiner = new StringJoiner(",");
		for(Conjunct conj : conjuncts){
			joiner.add(conj.toString());
		}
		return joiner.toString();
	}

	/**
	 * Converts this query body to SQL.
	 * @param lhs The projected head variables.
	 * @return The SQL form of this query body.
	 */
	protected String toSQL(List<Variable> lhs){
		StringBuilder buffer = new StringBuilder();
		int n = conjuncts.size();
		Map<Variable, List<Conjunct>> varMap = new HashMap<Variable, List<Conjunct>>();
		Map<Conjunct, Integer> idMap = new HashMap<Conjunct, Integer>();
		
		buffer.append("(WITH RECURSIVE ");
		int extra = 0;
		for(int i = 0; i < n; i++){
			Conjunct conj = conjuncts.get(i);
			varMap.computeIfAbsent(conj.getSource(), v->new ArrayList<Conjunct>()).add(conj);
			varMap.computeIfAbsent(conj.getTarget(), v->new ArrayList<Conjunct>()).add(conj);
			idMap.put(conj, i);
			
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
		
		if(lhs.isEmpty()){
			buffer.append(" SELECT \"true\" FROM edge WHERE EXISTS (SELECT *");
		}else{
			//just need one occurrence
			buffer.append(" SELECT DISTINCT ");
			for(int i = 0; i < lhs.size(); i++){
				Variable var = lhs.get(i);
				buffer.append(conjunctVarToSQL(var, varMap.get(var).get(0), idMap));
				if(i < lhs.size() - 1){
					buffer.append(", ");
				}
			}
		}
		
		buffer.append(" FROM ");
		for(int i = 0; i < n + extra; i++){
			buffer.append('c');
			buffer.append(i);
			if(i < n + extra - 1){
				buffer.append(", ");
			}
		}
		
		//a single conjunct shares no variables with other conjuncts or itself
		if(conjuncts.size() > 1){
			buffer.append(" WHERE ");
			Iterator<Entry<Variable, List<Conjunct>>> iter = varMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Variable, List<Conjunct>> data = iter.next();
				List<Conjunct> conjuncts = data.getValue();
				Variable var = data.getKey();
				
				//compare the first with all others
				for(int i = 1; i < conjuncts.size(); i++){
					buffer.append(conjunctVarToSQL(var, conjuncts.get(0), idMap));
					buffer.append(" = ");
					buffer.append(conjunctVarToSQL(var, conjuncts.get(i), idMap));
					buffer.append(" AND ");
				}
			}
			
			//remove trailing AND
			buffer.delete(buffer.length() - 5, buffer.length());
		}
		
		if(lhs.isEmpty()){
			buffer.append(")");
		}
		
		buffer.append(")");
		return buffer.toString();
	}
	
	/**
	 * Converts a conjunct variable to SQL.
	 * @param var The variable to convert.
	 * @param conj The conjunct this variable is a part of.
	 * @param idMap A map storing the ID of each conjunct.
	 * @return The SQL version of this conjunct variable.
	 */
	private static String conjunctVarToSQL(Variable var, Conjunct conj, Map<Conjunct, Integer> idMap){
		if(var.equals(conj.getSource())){
			return "c" + idMap.get(conj) + ".src";
		}else if(var.equals(conj.getTarget())){
			return "c" + idMap.get(conj) + ".trg";
		}else{
			return null;
		}
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<body>", 2);
		conjuncts.forEach(conj->conj.writeXML(writer));
		writer.println(2, "</body>");
	}
}
