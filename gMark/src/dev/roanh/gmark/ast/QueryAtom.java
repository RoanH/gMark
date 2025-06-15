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
 * Representation of the smallest atomic unit in a query.
 * By choice these atoms always have two result outputs,
 * the source and target variables, which can be identical.
 * The variables returned here bind to the same vertex for
 * all directly children of a parent node in the AST.
 * @author Roan
 */
public abstract interface QueryAtom extends QueryFragment{

	/**
	 * The source variable for this atom.
	 * @return The source variable.
	 */
	public abstract QueryVariable getSource();
	
	/**
	 * The target variable for this atom.
	 * @return The target variable.
	 */
	public abstract QueryVariable getTarget();

	@Override
	public default QueryTree toAbstractSyntaxTree(){
		return QueryTree.ofAtom(this);
	}
}
