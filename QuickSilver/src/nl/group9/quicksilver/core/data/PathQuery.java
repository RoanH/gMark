package nl.group9.quicksilver.core.data;

import java.util.Optional;

import dev.roanh.gmark.ast.QueryTree;

public record PathQuery(Optional<Integer> source, QueryTree ast, Optional<Integer> target){
	//s,t,ast
//TODO s/t probably -1 if unbound
	
	public static final PathQuery of(QueryTree ast){
		return new PathQuery(Optional.empty(), ast, Optional.empty());
	}

	public static final PathQuery of(QueryTree ast, int target){
		return new PathQuery(Optional.empty(), ast, Optional.of(target));
	}

	public static final PathQuery of(int source, QueryTree ast){
		return new PathQuery(Optional.of(source), ast, Optional.empty());
	}

	public static final PathQuery of(int source, QueryTree ast, int target){
		return new PathQuery(Optional.of(source), ast, Optional.of(target));
	}
}
