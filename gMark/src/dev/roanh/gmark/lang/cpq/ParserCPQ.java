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
package dev.roanh.gmark.lang.cpq;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.core.graph.Predicate;

/**
 * Parser for CPQs (Conjunctive Path Queries).
 * @author Roan
 * @see CPQ
 */
public final class ParserCPQ{
	
	/**
	 * Prevent instantiation.
	 */
	private ParserCPQ(){
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
	public static CPQ parse(String query, Map<String, Predicate> labels, char join, char intersect, char inverse) throws IllegalArgumentException{
		List<String> parts = split(query, join);
		if(parts.size() > 1){
			return CPQ.concat(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
		}
		
		parts = split(query, intersect);
		if(parts.size() > 1){
			return CPQ.intersect(parts.stream().map(part->{
				return parse(part, labels, join, intersect, inverse);
			}).toList());
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
}
