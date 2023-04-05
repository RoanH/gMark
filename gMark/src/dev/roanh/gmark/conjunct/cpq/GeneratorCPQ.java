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
package dev.roanh.gmark.conjunct.cpq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.EdgeGraph;
import dev.roanh.gmark.util.EdgeGraphData;
import dev.roanh.gmark.util.UniqueGraph.GraphNode;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;
import dev.roanh.gmark.util.Util;

/**
 * Generator for CPQs (Conjunctive Path Queries).
 * @author Roan
 */
public class GeneratorCPQ implements ConjunctGenerator{
	/**
	 * The schema graph to use to generate CPQs.
	 */
	private SchemaGraph gs;
	/**
	 * The workload specifying what CPQs to generate.
	 */
	private WorkloadCPQ workload;
	
	/**
	 * Constructs a new CPQ generator using the given workload.
	 * @param wl The workload specification.
	 * @see WorkloadCPQ
	 */
	public GeneratorCPQ(WorkloadCPQ wl){
		gs = new SchemaGraph(wl.getGraphSchema());
		workload = wl;
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. A set of labels of the given size
	 * is automatically generated together with corresponding inverse
	 * labels for each label. Three relatively meaningless but technically
	 * valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * <li>The query consisting of only identity and nothing else.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param labels The number of distinct labels to use (upper limit).
	 * @return The randomly generated CPQ.
	 * @throws IllegalArgumentException When the list of labels is empty.
	 * @see CPQ
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 */
	public static CPQ generatePlainCPQ(int ruleApplications, int labels) throws IllegalArgumentException{
		return generatePlainCPQ(ruleApplications, Util.generateLabels(labels));
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. Labels and inverse labels are
	 * drawn from the given set of labels. Three relatively meaningless
	 * but technically valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * <li>The query consisting of only identity and nothing else.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated CPQ.
	 * @throws IllegalArgumentException When the list of labels is empty.
	 * @see CPQ
	 * @see <a href="https://cpqkeys.roanh.dev/notes/cpq_definition">CPQ Definition</a>
	 */
	public static CPQ generatePlainCPQ(int ruleApplications, List<Predicate> labels) throws IllegalArgumentException{
		if(labels.isEmpty()){
			throw new IllegalArgumentException("List of labels cannot be empty.");
		}
		
		return generatePlainCPQ(ruleApplications, false, labels);
	}
	
	/**
	 * Generates a random CPQ created by applying the intersection
	 * (conjunction) and concatenation steps from the CPQ grammar
	 * the given number of times. Labels and inverse labels are
	 * drawn from the given set of labels. Two relatively meaningless
	 * but technically valid patterns are intentionally never generated:
	 * <ol>
	 * <li>Concatenation with identity.</li>
	 * <li>Intersection of identity with identity.</li>
	 * </ol>
	 * @param ruleApplications The number of times the intersection and
	 *        concatenation steps are allowed to be applied.
	 * @param allowId Whether the result of this call is allowed to be
	 *        the identity CPQ.
	 * @param labels The set of labels to draw from.
	 * @return The randomly generated CPQ.
	 */
	private static CPQ generatePlainCPQ(int ruleApplications, boolean allowId, List<Predicate> labels){
		if(ruleApplications == 0){
			if(allowId && Util.getRandom().nextBoolean()){
				return CPQ.IDENTITY;
			}else{
				//edge label
				Predicate label = Util.selectRandom(labels);
				return CPQ.label(Util.getRandom().nextBoolean() ? label : label.getInverse());
			}
		}else{
			if(Util.getRandom().nextBoolean()){
				//concatenation
				int split = Util.uniformRandom(0, ruleApplications - 1);
				return CPQ.concat(generatePlainCPQ(split, false, labels), generatePlainCPQ(ruleApplications - split - 1, false, labels));
			}else{
				//intersect
				int split = Util.uniformRandom(0, ruleApplications - 1);
				return CPQ.intersect(generatePlainCPQ(split, true, labels), generatePlainCPQ(ruleApplications - split - 1, false, labels));
			}
		}
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. The input is assumed
	 * to use brackets where possible and to use the '{@code id}', '{@value CPQ#CHAR_JOIN}',
	 * '{@value CPQ#CHAR_CAP}' and '{@value Predicate#CHAR_INVERSE}' symbols to denote
	 * operations. Example input: {@code (0◦(((1◦0) ∩ (1◦1))◦1⁻))}.
	 * @param query The CPQ to parse.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see #parse(String, char, char, char)
	 */
	public static CPQ parse(String query) throws IllegalArgumentException{
		return parse(query, CPQ.CHAR_JOIN, CPQ.CHAR_CAP, Predicate.CHAR_INVERSE);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. Unlike
	 * {@link #parse(String)} this subroutine allows custom symbols
	 * to be used to input the CPQ.
	 * @param query The CPQ to parse.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 * @see #parse(String)
	 */
	public static CPQ parse(String query, char join, char intersect, char inverse) throws IllegalArgumentException{
		return parse(query, new HashMap<String, Predicate>(), join, intersect, inverse);
	}
	
	/**
	 * Parses the given CPQ in string form to a CPQ instance. Unlike
	 * {@link #parse(String)} this subroutine allows custom symbols
	 * to be used to input the CPQ.
	 * @param query The CPQ to parse.
	 * @param labels A map with predicates found so far.
	 * @param join The symbol to use for the join/concatenation operation.
	 * @param intersect The symbol to use for the intersection/conjunction operation.
	 * @param inverse The symbol to use for the inverse edge label operation.
	 * @return The parsed CPQ.
	 * @throws IllegalArgumentException When the given string is not a valid CPQ.
	 */
	private static CPQ parse(String query, Map<String, Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		List<String> parts = split(query, join);
		if(parts.size() > 1){
			return CPQ.concat(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).collect(Collectors.toList()));
		}
		
		parts = split(query, intersect);
		if(parts.size() > 1){
			return CPQ.intersect(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).collect(Collectors.toList()));
		}
		
		if(query.equals("id")){
			return CPQ.IDENTITY;
		}
		
		if(query.startsWith("(") && query.endsWith(")")){
			return parse(query.substring(1, query.length() - 1), labels, join, intersect, inverse);
		}
		
		if(query.indexOf('(') == -1 && query.indexOf(')') == -1 && query.indexOf(join) == -1 && query.indexOf(intersect) == -1){
			boolean inv = false;
			if(query.charAt(query.length() - 1) == inverse){
				inv = true;
				query = query.substring(0, query.length() - 1);
			}
			
			if(query.indexOf(inverse) == -1){
				Predicate label = labels.computeIfAbsent(query, k->new Predicate(labels.size(), k));
				return CPQ.label(inv ? label.getInverse() : label);
			}
		}

		throw new IllegalArgumentException("Invalid CPQ.");
	}
	
	/**
	 * Splits the given string into parts on the given character.
	 * The given character will not be returned in any parts and
	 * the found parts will be trimmed of leading and trailing
	 * whitespace. This method will ignore any regions of the
	 * input string that are enclosed in (nested) round brackets.
	 * @param str The string to split.
	 * @param symbol The character to split on.
	 * @return The input string split on the given character.
	 * @throws IllegalArgumentException When brackets are present
	 *         in the given string, but not balanced properly.
	 */
	protected static List<String> split(String str, char symbol) throws IllegalArgumentException{
		List<String> parts = new ArrayList<String>();
		
		int start = 0;
		for(int i = 0; i < str.length(); i++){
			if(str.charAt(i) == '('){
				i++;
				int open = 1;
				while(true){
					if(str.charAt(i) == '('){
						open++;
					}else if(str.charAt(i) == ')'){
						open--;
						if(open == 0){
							break;
						}
					}
					
					i++;
					if(i >= str.length()){
						throw new IllegalArgumentException("Unbalanced brackets.");
					}
				}
			}else if(str.charAt(i) == symbol){
				parts.add(str.substring(start, i).trim());
				start = i + 1;
			}
		}
		
		parts.add(str.substring(start, str.length()).trim());

		return parts;
	}

	@Override
	public Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end, Variable source, Variable target, boolean star) throws GenerationException{
		EdgeGraph graph = new EdgeGraph(gs, workload.getMaxDiameter(), start, end, workload.getMaxRecursion());
		List<GraphNode<EdgeGraphData, Void>> path = graph.drawPath();
		
		assert !path.isEmpty() : "path is not allowed not be empty!";
		
		return new ConjunctCPQ(path.size() == 1 ? path.get(0).getData().toCPQ() : new ConcatCPQ(
			path.stream().map(GraphNode::getData).map(EdgeGraphData::toCPQ).collect(Collectors.toList())
		), source, target, star);
	}
}
