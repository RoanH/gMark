package nl.group9.quicksilver.core.spec;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;

public abstract interface Evaluator<G extends Graph>{
	
	public abstract G createGraph(int vertexCount, int edgeCount, int labelCount);

	public abstract void prepare(G graph);
	
	public abstract G evaluate(PathQuery query);
	
	public abstract CardStat computeCardinality(G graph);
}
