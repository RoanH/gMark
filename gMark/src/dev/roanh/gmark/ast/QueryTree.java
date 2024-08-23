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
package dev.roanh.gmark.ast;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.generic.GenericEdge;
import dev.roanh.gmark.util.IndentWriter;

//fragment = atom for leaves
public class QueryTree{
	private final QueryTree left;
	private final QueryTree right;
	private final QueryFragment fragment;
	
	private <T extends QueryFragment> QueryTree(QueryTree left, QueryTree right, T fragment){
		this.left = left;
		this.right = right;
		this.fragment = fragment;
	}
	
	public boolean isLeaf(){
		return left == null && right == null;
	}
	
	public boolean isUnary(){
		return getOperation().isUnary();
	}
	
	public boolean isBinary(){
		return getOperation().isBinary();
	}
	
	public OperationType getOperation(){
		return fragment.getOperationType();
	}
	
	//leaf only
	public Predicate getPredicate() throws IllegalStateException{
		if(getOperation() != OperationType.EDGE){
			throw new IllegalStateException("Query fragment is not an AST edge leaf.");
		}
		
		return ((GenericEdge)fragment).getLabel();
	}
	
	public QueryTree getLeft(){
		return left;
	}
	
	public QueryTree getRight(){
		return right;
	}
	
	@Deprecated
	public QueryFragment getQueryFragment(){
		return fragment;
	}
	
	private void writeAST(IndentWriter writer){//TODO level 0 indent broken
		if(isLeaf()){
			writer.println(fragment.toString());
		}else{
			writer.println(getOperation().toString(), 2);
			writer.print("- ");
			left.writeAST(writer);
			if(right != null){
				writer.print("- ");
				right.writeAST(writer);
			}
			
			writer.decreaseIndent(2);
		}
	}
	
	public static void main(String[] args){
		System.out.println(CPQ.generateRandomCPQ(10, 3).toAbstractSyntaxTree());
	}
	
	@Override
	public String toString(){
		IndentWriter writer = new IndentWriter();
		writer.decreaseIndent(2);//TODO hacky
		writeAST(writer);
		return writer.toString();
	}
	
	public static <T extends QueryFragment> QueryTree ofAtom(T fragment){
		return new QueryTree(null, null, fragment);
	}
	
	public static <T extends QueryFragment> QueryTree ofUnary(QueryTree left, T fragment){
		return new QueryTree(left, null, fragment);
	}

	public static <T extends QueryFragment> QueryTree ofBinary(QueryTree left, QueryTree right, T fragment){
		return new QueryTree(left, right, fragment);
	}
}
