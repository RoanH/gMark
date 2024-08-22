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

import java.util.List;
import java.util.StringJoiner;

import dev.roanh.gmark.ast.OperationType;
import dev.roanh.gmark.ast.QueryTree;
import dev.roanh.gmark.ast.QueryFragment;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.output.OutputSQL;
import dev.roanh.gmark.util.IndentWriter;

/**
 * Generic definition of a number of query language fragments.
 * (also known as the join operation).
 * @author Roan
 * @param <T> The concrete query language fragment syntax.
 */
public abstract class GenericConcatenation<T extends QueryLanguageSyntax> implements OutputSQL, QueryFragment{
	/**
	 * The sub queries to concatenate in order from first to last.
	 */
	protected final List<T> elements;

	/**
	 * Constructs a new concatenation with the given list of fragments.
	 * @param elements The query language fragments to concatenate.
	 * @throws IllegalArgumentException When the given list of fragments is empty.
	 */
	protected GenericConcatenation(List<T> elements) throws IllegalArgumentException{
		this.elements = elements;
		if(elements.isEmpty()){
			throw new IllegalArgumentException("List of queries to concatenate cannot be empty.");
		}
	}
	
	@Override
	public String toString(){
		StringJoiner builder = new StringJoiner(String.valueOf(QueryLanguageSyntax.CHAR_JOIN), "(", ")");
		
		for(T item : elements){
			builder.add(item.toString());
		}
		
		return builder.toString();
	}
	
	@Override
	public void writeSQL(IndentWriter writer){
		if(elements.size() == 1){
			elements.get(0).writeSQL(writer);
			return;
		}
		
		int n = elements.size();
		writer.print("SELECT s0.src AS src, s");
		writer.print(n - 1);
		writer.println(".trg AS trg");
		
		writer.println("FROM");
		writer.increaseIndent(2);
		
		for(int i = 0; i < n; i++){
			writer.println("(", 2);
			elements.get(i).writeSQL(writer);
			writer.println();
			writer.decreaseIndent(2);
			writer.print(") AS s");
			writer.print(i);
			if(i < n - 1){
				writer.print(",");
			}
			
			writer.println();
		}
		
		writer.decreaseIndent(2);
		writer.print("WHERE ");
		for(int i = 0; i < n - 1; i++){
			writer.print("s");
			writer.print(i);
			writer.print(".trg = s");
			writer.print(i + 1);
			writer.print(".src");
			if(i < n - 2){
				writer.print(" AND ");
			}
		}
	}

	@Override
	public OperationType getOperationType(){
		return OperationType.CONCATENATION;
	}

	@Override
	public QueryTree toAbstractSyntaxTree(){
		QueryTree right = elements.get(elements.size() - 1).toAbstractSyntaxTree();
		if(elements.size() == 1){
			return right;
		}
		
		for(int i = elements.size() - 2; i >= 0; i--){
			right = QueryTree.ofBinary(elements.get(i).toAbstractSyntaxTree(), right, this);
		}
		
		return right;
	}
}
