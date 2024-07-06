package dev.roanh.gmark.lang.rpq;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.generic.GenericEdge;

public class EdgeRPQ extends GenericEdge implements RPQ{

	protected EdgeRPQ(Predicate symbol){
		super(symbol);
	}
}
