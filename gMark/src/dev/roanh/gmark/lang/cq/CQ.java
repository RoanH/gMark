/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
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
package dev.roanh.gmark.lang.cq;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Interface for conjunctive queries (CQs).
 * @author Roan
 * @see <a href="https://en.wikipedia.org/wiki/Conjunctive_query">Conjunctive Query</a>
 */
public class CQ implements QueryLanguageSyntax{
	private final Collection<VarCQ> variables; 
	private final Collection<AtomCQ> formulae;
	
	private CQ(){
		variables = new ArrayList<VarCQ>(); 
		formulae = new ArrayList<AtomCQ>();
	}
	
	protected CQ(Collection<VarCQ> variables, Collection<AtomCQ> formulae){
		this.variables = variables;
		this.formulae = formulae;
	}
	
	public VarCQ addFreeVariable(String name){//projected
		VarCQ v = new VarCQ(name, true);
		variables.add(v);
		return v;
	}
	
	public VarCQ addBoundVariable(String name){//inner
		VarCQ v = new VarCQ(name, false);
		variables.add(v);
		return v;
	}
	
	public void addAtom(VarCQ source, Predicate label, VarCQ target){
		formulae.add(new AtomCQ(source, label.isInverse() ? label.getInverse() : label, target));
	}

	public QueryGraphCQ toQueryGraph(){
		return new QueryGraphCQ(variables, formulae);
	}
	
	@Override
	public QueryLanguage getQueryLanguage(){
		return QueryLanguage.CQ;
	}

	@Override
	public void writeSQL(IndentWriter writer){
		
		
		
		// TODO Auto-generated method stub
		
		/*
		 * SELECT a,b,c
		 * FROM edge
		 * WHERE
		 * a = b AND b = c AND ...
		 * 
		 * 
		 * 
		 * 
		 */
		
	}

	@Override
	public String toFormalSyntax(){
		//free variables
		StringJoiner head = new StringJoiner(", ", "(", ") " + QueryLanguageSyntax.CHAR_ASSIGN + " ");
		variables.stream().filter(VarCQ::isFree).map(VarCQ::getName).sorted().forEach(head::add);

		//body
		StringJoiner body = new StringJoiner(", ", head.toString(), "");
		formulae.stream().map(AtomCQ::toString).sorted().forEach(body::add);
		
		return body.toString();
	}

	@Override
	public void writeXML(IndentWriter writer){
		writer.println("<cq>", 2);
		
		//vars
		
		//atoms
		
		writer.println(2, "</cq>");
	}

	@Override
	public OperationType getOperationType(){
		// TODO Auto-generated method stub
		return OperationType.JOIN;//join? 'and'
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}
	
	public static CQ parse(String query) throws IllegalArgumentException{
		return ParserCQ.parse(query);
	}
	
	public static CQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return ParserCQ.parse(query, labels);
	}
	
	public static CQ parse(QueryTree ast) throws IllegalArgumentException{
		return null;//TODO
	}
	
	public static final CQ empty(){
		return new CQ();
	}
}
