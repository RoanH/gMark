package nl.group9.quicksilver.core.spec;

import java.util.List;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.SourceTargetPair;

public abstract interface Graph{//TODO we should probably separate the graph from the minimum graph required by the core logic

	public abstract int getNoVertices();
	
	public abstract int getNoEdges();
	
	public abstract int getNoDistinctEdges();
	
	public abstract int getNoLabels();
	
	public abstract void addEdge(int from, int to, int label);
	
	public abstract boolean hasEdge(int source, int target, int label);
	
	public abstract List<SourceTargetPair> getSourceTargetPairs();

	public abstract CardStat computeCardinality();
}
