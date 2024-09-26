package nl.group9.quicksilver.core.spec;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;

//G input graph, R output result graph
public abstract interface Evaluator<G extends Graph, R extends Graph>{

	public abstract void prepare(G graph);
	
	public abstract R evaluate(PathQuery query);
	
	public abstract CardStat computeCardinality(R graph);
}
