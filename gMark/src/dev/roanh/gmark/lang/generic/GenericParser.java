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
package dev.roanh.gmark.lang.generic;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.roanh.gmark.core.graph.Predicate;

/**
 * Class with generic query language parsing utilities.
 * @author Roan
 */
public abstract class GenericParser{
	
	/**
	 * Hide constructor.
	 */
	protected GenericParser(){
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
	
	/**
	 * Attempts to parse an (inverse) predicate/label from the given query string.
	 * @param query The query string to parse.
	 * @param labels The set of predicates encountered so far. Used to reuse existing
	 *        labels and to register newly detected predicates.
	 * @param inverse The symbol used to denote inverse labels.
	 * @return The predicate that was parsed.
	 * @throws IllegalArgumentException When the given query string does not represent a valid predicate.
	 */
	protected static Predicate parsePredicate(String query, Map<String, Predicate> labels, char inverse) throws IllegalArgumentException{
		boolean inv = false;
		if(query.charAt(query.length() - 1) == inverse){
			inv = true;
			query = query.substring(0, query.length() - 1);
		}
		
		if(query.indexOf(inverse) == -1){
			Predicate label = labels.computeIfAbsent(query, k->new Predicate(labels.size(), k));
			return inv ? label.getInverse() : label;
		}
		
		throw new IllegalArgumentException("Invalid predicate syntax.");
	}
}