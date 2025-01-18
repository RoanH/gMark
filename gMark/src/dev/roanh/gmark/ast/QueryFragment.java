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
package dev.roanh.gmark.ast;

/**
 * Interface for objects that represent a fragment of an entire query.
 * These fragments have a main database operation as their main responsibility
 * (root operation) and additionally can be converted together with their
 * sub queries (children) to an AST. 
 * @author Roan
 * @see QueryTree
 */
public abstract interface QueryFragment{
	
	/**
	 * The top level operation represented by this query fragment.
	 * @return The top level operation for this query fragment.
	 */
	public abstract OperationType getOperationType();
	
	/**
	 * Converts this query fragment to an equivalent abstract syntax tree.
	 * @return The constructed Abstract Syntax Tree (AST).
	 */
	public abstract QueryTree toAbstractSyntaxTree();
}
