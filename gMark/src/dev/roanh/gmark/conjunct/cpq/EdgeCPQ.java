package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.core.graph.Predicate;

public class EdgeCPQ implements CPQ{
	private Predicate symbol;
	
	public EdgeCPQ(Predicate symbol){
		this.symbol = symbol;
	}
	
	@Override
	public String toString(){
		return symbol.getAlias();
	}

	@Override
	public String toSQL(){
		return symbol.toSQL();
	}
}
