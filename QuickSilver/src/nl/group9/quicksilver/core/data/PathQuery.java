package nl.group9.quicksilver.core.data;

import java.util.Optional;

import dev.roanh.gmark.lang.QueryLanguageSyntax;

public record PathQuery(Optional<Integer> source, QueryLanguageSyntax query, Optional<Integer> target){
	
	public static final PathQuery of(QueryLanguageSyntax query){
		return new PathQuery(Optional.empty(), query, Optional.empty());
	}

	public static final PathQuery of(QueryLanguageSyntax query, int target){
		return new PathQuery(Optional.empty(), query, Optional.of(target));
	}

	public static final PathQuery of(int source, QueryLanguageSyntax query){
		return new PathQuery(Optional.of(source), query, Optional.empty());
	}

	public static final PathQuery of(int source, QueryLanguageSyntax query, int target){
		return new PathQuery(Optional.of(source), query, Optional.of(target));
	}

	@Override
	public String toString(){
		return source.map(String::valueOf).orElse("*") + ", " + query + ", " + target.map(String::valueOf).orElse("*");
	}
}
