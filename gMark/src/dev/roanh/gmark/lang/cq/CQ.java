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
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import dev.roanh.gmark.ast.EdgeQueryAtom;
import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.IndentWriter;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

/**
 * Interface for conjunctive queries (CQs).
 * @author Roan
 * @see <a href="https://en.wikipedia.org/wiki/Conjunctive_query">Conjunctive Query</a>
 * @see VarCQ
 * @see AtomCQ
 */
public final class CQ implements QueryLanguageSyntax{
	/**
	 * Set of variables that appear in the CQ.
	 */
	private final Set<VarCQ> variables;
	/**
	 * List of formulae that appear in the CQ, these correspond to graph edges.
	 */
	private final List<AtomCQ> formulae;
	
	/**
	 * Constructs a new empty CQ.
	 */
	private CQ(){
		variables = new HashSet<VarCQ>();
		formulae = new ArrayList<AtomCQ>();
	}
	
	/**
	 * Constructs a new CQ with the given set of variables and formulae.
	 * @param variables The variables that appear in the CQ.
	 * @param formulae The formulae corresponding to graph edges.
	 */
	protected CQ(Set<VarCQ> variables, List<AtomCQ> formulae){
		this.variables = variables;
		this.formulae = formulae;
	}
	
	/**
	 * Adds a new free (projected) variable to this CQ.
	 * @param name The name of the variable.
	 * @return The newly added variable.
	 * @see VarCQ
	 */
	public VarCQ addFreeVariable(String name){
		VarCQ v = new VarCQ(name, true);
		variables.add(v);
		return v;
	}
	
	/**
	 * Adds a new bound (body only) variable to this CQ.
	 * @param name The name of the variable.
	 * @return The newly added variable.
	 * @see VarCQ
	 */
	public VarCQ addBoundVariable(String name){
		VarCQ v = new VarCQ(name, false);
		variables.add(v);
		return v;
	}
	
	/**
	 * Adds a new atom (edge) to this CQ.
	 * @param source The source node/variable of the edge represented by the atom.
	 * @param label The label on the edge matched by the atom.
	 * @param target The target node/variable of the edge represented by the atom.
	 */
	public void addAtom(VarCQ source, Predicate label, VarCQ target){
		formulae.add(new AtomCQ(source, label.isInverse() ? label.getInverse() : label, target));
	}

	/**
	 * Computes and returns the query graph for this CQ.
	 * @return The query graph for this CQ.
	 * @see QueryGraphCQ
	 */
	public QueryGraphCQ toQueryGraph(){
		return new QueryGraphCQ(variables, formulae);
	}
	
	/**
	 * Gets the set of free (projected) variables for this CQ.
	 * @return The set of free variables for this CQ.
	 * @see VarCQ
	 */
	public Set<VarCQ> getFreeVariables(){
		return variables.stream().filter(VarCQ::isFree).collect(Collectors.toSet());
	}
	
	/**
	 * Gets the set of bound (body only) variables for this CQ.
	 * @return The set of bound variables for this CQ.
	 * @see VarCQ
	 */
	public Set<VarCQ> getBoundVariables(){
		return variables.stream().filter(VarCQ::isBound).collect(Collectors.toSet());
	}
	
	@Override
	public QueryLanguage getQueryLanguage(){
		return QueryLanguage.CQ;
	}

	@Override
	public void writeSQL(IndentWriter writer){
		if(formulae.isEmpty()){
			throw new IllegalStateException("Cannot convert a CQ without formulae to SQL.");
		}
			
		//general setup
		Map<VarCQ, List<AtomCQ>> atomsForVariable = new HashMap<VarCQ, List<AtomCQ>>();
		Map<AtomCQ, Integer> atomId = new LinkedHashMap<AtomCQ, Integer>();
		int id = 0;
		for(AtomCQ atom : formulae){
			atomId.put(atom, id++);
			
			atomsForVariable.computeIfAbsent(
				atom.getSource(),
				k->new ArrayList<AtomCQ>()
			).add(atom);
			
			if(!atom.getSource().equals(atom.getTarget())){
				atomsForVariable.computeIfAbsent(
					atom.getTarget(),
					k->new ArrayList<AtomCQ>()
				).add(atom);
			}
		}
		
		//head
		writer.println("SELECT", 2);
		for(Entry<VarCQ, List<AtomCQ>> entry : atomsForVariable.entrySet().stream().sorted(Comparator.comparing(e->e.getKey().getName())).toList()){
			VarCQ v = entry.getKey();
			if(v.isFree()){
				AtomCQ atom = entry.getValue().get(0);
				writer.print("edge");
				writer.print(atomId.get(atom));
				writer.print(".");
				writer.print(atom.getSource().equals(v) ? "src" : "trg");
				writer.print(" AS ");
				writer.print(v.getName());
				writer.mark();
				writer.println(",");
			}
		}
		
		//tables
		writer.deleteFromMark(1);
		writer.decreaseIndent(2);
		writer.println("FROM", 2);
		for(Entry<AtomCQ, Integer> entry : atomId.entrySet()){
			writer.print("edge edge");
			writer.print(entry.getValue());
			writer.mark();
			writer.println(",");
		}
		
		//edge label bindings
		writer.deleteFromMark(1);
		writer.decreaseIndent(2);
		writer.println("WHERE", 2);
		for(Entry<AtomCQ, Integer> entry : atomId.entrySet()){
			writer.print("edge");
			writer.print(entry.getValue());
			writer.print(".label = ");
			writer.print(entry.getKey().getLabel().getID());
			writer.mark();
			writer.println();
			writer.println("AND");
		}
		
		//constraints
		for(Entry<VarCQ, List<AtomCQ>> entry : atomsForVariable.entrySet()){
			List<AtomCQ> atoms = entry.getValue();
			VarCQ v = entry.getKey();
			AtomCQ base = atoms.get(0);
			boolean src = base.getSource().equals(v);
			String baseRef = "edge" + atomId.get(base) + "." + (src ? "src" : "trg");

			for(int i = 0; i < atoms.size(); i++){
				AtomCQ other = atoms.get(i);
				int otherId = atomId.get(other);

				if(other.getSource().equals(v) && !(i == 0 && src)){
					writer.print(baseRef);
					writer.print(" = edge");
					writer.print(otherId);
					writer.print(".src");
					writer.mark();
					writer.println();
					writer.println("AND");
				}

				if(other.getTarget().equals(v) && !(i == 0 && !src)){
					writer.print(baseRef);
					writer.print(" = edge");
					writer.print(otherId);
					writer.print(".trg");
					writer.mark();
					writer.println();
					writer.println("AND");
				}
			}
		}
		
		writer.deleteAllFromMark();
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
		writer.println("<variables>", 2);
		variables.stream().sorted(Comparator.comparing(VarCQ::getName)).forEach(v->v.writeXML(writer));
		writer.println(2, "</variables>");
		writer.println("<formulae>", 2);
		formulae.forEach(atom->atom.writeXML(writer));
		writer.println(2, "</formulae>");
		writer.println(2, "</cq>");
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.JOIN;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		return QueryTree.ofNAry(
			formulae.stream().map(AtomCQ::toAbstractSyntaxTree).toList(),
			this
		);
	}
	
	@Override
	public String toString(){
		return toFormalSyntax();
	}
	
	/**
	 * Parses the given CQ in string from to a CQ instance. The input is assumed
	 * to use {@value QueryLanguageSyntax#CHAR_ASSIGN} to separate the head and
	 * body of the query.
	 * @param query The CQ to parse.
	 * @return The parsed CQ.
	 * @throws IllegalArgumentException When the given string is not a valid CQ.
	 * @see QueryLanguageSyntax
	 * @see ParserCQ#parse(String, char)
	 */
	public static CQ parse(String query) throws IllegalArgumentException{
		return ParserCQ.parse(query);
	}
	
	/**
	 * Parses the given CQ in string from to a CQ instance. The input is assumed
	 * to use {@value QueryLanguageSyntax#CHAR_ASSIGN} to separate the head and
	 * body of the query.
	 * @param query The CQ to parse.
	 * @param labels The variable set to use, new labels will <b>not</b> be created
	 *        if labels are found in the input that are not covered by the given list.
	 * @return The parsed CQ.
	 * @throws IllegalArgumentException When the given string is not a valid CQ or contains unknown labels.
	 * @see QueryLanguageSyntax
	 * @see ParserCQ#parse(String, List, char)
	 */
	public static CQ parse(String query, List<Predicate> labels) throws IllegalArgumentException{
		return ParserCQ.parse(query, labels);
	}
	
	/**
	 * Attempts to parse the given AST to a CQ.
	 * @param ast The AST to parse to a CQ.
	 * @return The CQ represented by the given AST.
	 * @throws IllegalArgumentException When the given AST does
	 *         not represent a valid CQ.
	 * @see QueryTree
	 */
	public static CQ parse(QueryTree ast) throws IllegalArgumentException{
		if(ast.getOperation() != OperationType.JOIN){
			throw new IllegalArgumentException("The given AST contains operations that are not part of the CQ query language.");
		}

		Set<VarCQ> variables = new HashSet<VarCQ>();
		List<AtomCQ> formulae = new ArrayList<AtomCQ>();
		for(int i = 0; i < ast.getArity(); i++){
			QueryTree op = ast.getOperand(i);
			if(op.getOperation() != OperationType.EDGE){
				throw new IllegalArgumentException("The given AST contains operations that are not part of the CQ query language.");
			}

			EdgeQueryAtom atom = op.getEdgeAtom();
			VarCQ source = new VarCQ(atom.getSource());
			VarCQ target = new VarCQ(atom.getTarget());
			variables.add(source);
			variables.add(target);
			formulae.add(new AtomCQ(source, atom.getLabel(), target));
		}
		
		return new CQ(variables, formulae);
	}
	
	/**
	 * Constructs a new CQ from the given graph.
	 * @param graph The graph to parse.
	 * @return The constructed CQ.
	 */
	public static CQ of(UniqueGraph<VarCQ, AtomCQ> graph){
		return new QueryGraphCQ(graph).toCQ();
	}
	
	/**
	 * Constructs a new completely empty CQ without and variables or formulae.
	 * @return The newly created CQ.
	 */
	public static CQ empty(){
		return new CQ();
	}
}
