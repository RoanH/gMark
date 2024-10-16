package nl.group9.quicksilver.core.data;

import java.util.Optional;

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
		return new PathQuery(Optional.empty(), query, Optional.of(target));
	}

	/**
	 * Constructs a new {@link PathQuery} with a bound source vertex and a free target vertex.
	 * @param source The source vertex all matched paths to start at.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(int source, QueryLanguageSyntax query){
		return new PathQuery(Optional.of(source), query, Optional.empty());
	}

	/**
	 * Constructs a new {@link PathQuery} with a bound source vertex and a bound target vertex.
	 * @param source The source vertex all matched paths to start at.
	 * @param query The path querying defining the constraints matching paths need to satisfy to be returned.
	 * @param target The target vertex all matched paths have to end at.
	 * @return The newly constructed path query.
	 */
	public static final PathQuery of(int source, QueryLanguageSyntax query, int target){
		return new PathQuery(Optional.of(source), query, Optional.of(target));
	}

	@Override
	public String toString(){
		return source.map(String::valueOf).orElse("*") + ", " + query + ", " + target.map(String::valueOf).orElse("*");
	}
}
