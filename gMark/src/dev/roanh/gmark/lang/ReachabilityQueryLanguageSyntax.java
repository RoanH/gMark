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
package dev.roanh.gmark.lang;

/**
 * More restrictive class of query language specifications for
 * reachability queries. These query are used to discover if a
 * path that adheres to specific constraints exists between two
 * nodes. Therefore, by definition the query has two output variables,
 * the source and target, which respectively represent the source
 * and target vertex of a path that adheres to the query constraints.
 * @author Roan
 */
public abstract interface ReachabilityQueryLanguageSyntax extends QueryLanguageSyntax{
}
