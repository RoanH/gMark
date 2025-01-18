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
package dev.roanh.gmark.eval;

import java.util.Optional;

import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.lang.QueryLanguage;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;

/**
 * Complete definition of a database reachability path query.
 * @author Roan
 * @param source The source vertex of the path query. If present all paths matched
 *        by the query have to start at this vertex, meaning all returned paths
 *        will have this vertex as their source vertex. If no source vertex is
 *        specified returned paths can start at any vertex in the database graph.
 * @param query The path query defining the constraints paths between the matched
 *        source and target vertices have to adhere to. Will be either a {@link CPQ}
 *        or an {@link RPQ} and the query can safely be cast to either of these depending
 *        on the return value of {@link QueryLanguageSyntax#getQueryLanguage()}.
 * @param target The target vertex of the path query. If present all paths matched
 *        by the query have to end at this vertex, meaning all returned paths
 *        will have this vertex as their target vertex. If no target vertex is
 *        specified returned paths can end at any vertex in the database graph.
 * @see QueryLanguage
 * @see SourceTargetPair
 */
public record PathQuery(Optional<Integer> source, QueryLanguageSyntax query, Optional<Integer> target){
	
	/**
	 * Constructs a new {@link PathQuery} with a free source vertex and a free target vertex.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(QueryLanguageSyntax query){
		return new PathQuery(Optional.empty(), query, Optional.empty());
	}

	/**
	 * Constructs a new {@link PathQuery} with a free source vertex and a bound target vertex.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @param target The target vertex all matched paths have to end at.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(QueryLanguageSyntax query, int target){
		return new PathQuery(Optional.empty(), query, ofVertex(target));
	}

	/**
	 * Constructs a new {@link PathQuery} with a bound source vertex and a free target vertex.
	 * @param source The source vertex all matched paths to start at.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(int source, QueryLanguageSyntax query){
		return new PathQuery(ofVertex(source), query, Optional.empty());
	}

	/**
	 * Constructs a new {@link PathQuery} with a bound source vertex and a bound target vertex.
	 * @param source The source vertex all matched paths to start at.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @param target The target vertex all matched paths have to end at.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(int source, QueryLanguageSyntax query, int target){
		return new PathQuery(ofVertex(source), query, ofVertex(target));
	}

	@Override
	public String toString(){
		return source.map(String::valueOf).orElse("*") + ", " + query + ", " + target.map(String::valueOf).orElse("*");
	}
	
	/**
	 * Constructs a new optional for the given vertex.
	 * @param vertex An optional for the given vertex.
	 * @return An optional for the given vertex or an empty
	 *         optional of the vertex was -1.
	 */
	private static final Optional<Integer> ofVertex(int vertex){
		return vertex == -1 ? Optional.empty() : Optional.of(vertex);
	}
}
