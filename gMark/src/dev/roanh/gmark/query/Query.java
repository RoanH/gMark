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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.StringJoiner;
import java.util.Map.Entry;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.output.OutputFormal;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.output.OutputXML;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Represents a generated query with a number of
 * projected variables and one or more query bodies.
 * @author Roan
 */
public class Query implements OutputSQL, OutputFormal, OutputXML{
	/**
	 * The projected variables for this query (head, left hand side).
	 */
	private List<Variable> variables;
	/**
	 * The body for this query.
	 */
	private QueryBody body;
	
	/**
	 * Constructs a new query with a single body made up of the given
	 * list of conjuncts and with the given project variables, selectivity and shape.
	 * @param conjuncts The conjuncts for the query body.
	 * @param variables The projected query variables.
	 * @param selectivity The query body selectivity.
	 * @param shape The query body shape.
	 */
	public Query(List<Conjunct> conjuncts, List<Variable> variables, Selectivity selectivity, QueryShape shape){
		this(new QueryBody(conjuncts, selectivity, shape), variables);
	}
	
	/**
	 * Constructs a new query with a single body and with the
	 * given project variables, selectivity and shape.
	 * @param body The query body.
	 * @param variables The projected query variables.
	 */
	public Query(QueryBody body, List<Variable> variables){
		this.body = body;
		this.variables = variables;
	}
	
	/**
	 * Gets the shape of this query.
	 * @return The query shape.
	 */
	public QueryShape getShape(){
		return body.getShape();
	}
	
	/**
	 * Gets the selectivity of this query.
	 * @return The selectivity of this query.
	 */
	public Selectivity getSelectivity(){
		return body.getSelectivity();
	}
	
	/**
	 * Gets the number of conjuncts in the query body with the most conjuncts.
	 * @return The maximum query body conjunct count.
	 */
	public int getConjunctCount(){
		return body.getConjunctCount();
	}
	
	/**
	 * Checks if this query is binary. A query is binary when
	 * its arity is equal to 0.
	 * @return True if this query is binary.
	 */
	public boolean isBinary(){
		return getArity() == 0;
	}
	
	/**
	 * Gets the arity (number of projected variables) for this query.
	 * @return The arity for this query.
	 */
	public int getArity(){
		return variables.size();
	}
	
	/**
	 * Gets the query body for this query.
	 * @return The query body.
	 */
	public QueryBody getBody(){
		return body;
	}
	
	@Override
	public String toFormalSyntax(){
		StringJoiner lhs = new StringJoiner(",", "(", ")");
		for(Variable v : variables){
			lhs.add(v.toString());
		}
		
		StringBuilder buffer = new StringBuilder();
		buffer.append(lhs.toString());
		buffer.append(" ← ");
		buffer.append(body.toString());
		return buffer.toString();
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}
	
	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<query>", 2);		
		
		writer.println("<head>", 2);
		variables.forEach(v->{
			writer.print("<var>");
			writer.print(v.toString());
			writer.println("</var>");
		});
		writer.println(2, "</head>");
		
		writer.println("<bodies>", 2);
		body.writeXML(writer);
		writer.println(2, "</bodies>");
		
		writer.println(2, "</query>");
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		body.writeSQL(writer);
		
		//compile which variables are in which conjuncts
		int n = body.getConjunctCount();
		Map<Variable, List<Conjunct>> varMap = new LinkedHashMap<Variable, List<Conjunct>>();
		Map<Conjunct, Integer> idMap = new HashMap<Conjunct, Integer>();
		for(int i = 0; i < n; i++){
			Conjunct conj = body.getConjuncts().get(i);
			varMap.computeIfAbsent(conj.getSource(), v->new ArrayList<Conjunct>()).add(conj);
			varMap.computeIfAbsent(conj.getTarget(), v->new ArrayList<Conjunct>()).add(conj);
			idMap.put(conj, i);
		}
		
		//write the select clause selecting representatives of the projected query head variables
		writer.println();
		if(variables.isEmpty()){
			writer.println("SELECT \"true\"");
			writer.println("FROM edge");
			writer.println("WHERE EXISTS (", 2);
			writer.println("SELECT *");
		}else{
			//just need one occurrence
			writer.println("SELECT DISTINCT", 2);
			for(int i = 0; i < variables.size(); i++){
				Variable v = variables.get(i);
				writer.print(conjunctVarToSQL(v, varMap.get(v).get(0), idMap));
				if(i < variables.size() - 1){
					writer.println(",");
				}
			}
			
			writer.println();
			writer.decreaseIndent(2);
		}
		
		//the from clause just list all regular and star clauses in the body
		writer.println("FROM");
		writer.increaseIndent(2);
		for(int i = 0; i < n; i++){
			writer.print("c");
			writer.print(i);
			
			if(body.getConjuncts().get(i).hasStar()){
				writer.println(",");
				writer.print("c");
				writer.print(i);
				writer.print("tc");
			}
			
			if(i < n - 1){
				writer.println(",");
			}else{
				writer.println();
			}
		}
		
		//a single conjunct shares no variables with other conjuncts or itself (at least right now)
		if(body.getConjunctCount() > 1){
			writer.println(2, "WHERE");
			writer.increaseIndent(2);
			
			boolean first = true;
			Iterator<Entry<Variable, List<Conjunct>>> iter = varMap.entrySet().iterator();
			while(iter.hasNext()){
				Entry<Variable, List<Conjunct>> data = iter.next();
				List<Conjunct> conjuncts = data.getValue();
				Variable v = data.getKey();
				
				//compare the first with all others
				for(int i = 1; i < conjuncts.size(); i++){
					if(!first){
						writer.println();
						writer.println("AND");
					}
					
					writer.print(conjunctVarToSQL(v, conjuncts.get(0), idMap));
					writer.print(" = ");
					writer.print(conjunctVarToSQL(v, conjuncts.get(i), idMap));
					first = false;
				}
			}
		}
		
		if(variables.isEmpty()){
			writer.println();
			writer.decreaseIndent(4);
			writer.print(")");
		}
	}
	
	/**
	 * Converts a conjunct variable to SQL.
	 * @param v The variable to convert.
	 * @param conj The conjunct this variable is a part of.
	 * @param idMap A map storing the ID of each conjunct.
	 * @return The SQL version of this conjunct variable.
	 */
	private static String conjunctVarToSQL(Variable v, Conjunct conj, Map<Conjunct, Integer> idMap){
		if(v.equals(conj.getSource())){
			return "c" + idMap.get(conj) + ".src";
		}else if(v.equals(conj.getTarget())){
			return "c" + idMap.get(conj) + ".trg";
		}else{
			return null;
		}
	}
}
