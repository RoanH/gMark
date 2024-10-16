package dev.roanh.gmark.eval;

import java.util.List;

import dev.roanh.gmark.util.graph.BinaryGraph;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.SourceTargetPair;

/**
 * Result graph describing the result of a database operation
 * or query evaluation as a whole. This is an abstract graph
 * representing the existence of paths without describing these
 * paths exactly. In essence this graph can also be interpreted
 * as a set of source target pairs, each representing the existence
 * of one or more paths between the source and target vertex of the pair.
 * @author Roan
 * @see CardStat
 * @see SourceTargetPair
 */
public class ResultGraph{
	private final BinaryGraph graph;
	
	//TODO may not be spec but actual impl

	/**
	 * Computes cardinality statistics for the result contained in this graph.
	 * @return Cardinality statistics for this result graph.
	 * @see CardStat
	 */
	public abstract CardStat computeCardinality();

	/**
	 * Gets the source target pairs in this result graph. This is the
	 * actual database operation or query evaluation result output.
	 * @return The paths matched by the database operation or query.
	 * @see SourceTargetPair
	 */
	public abstract List<SourceTargetPair> getSourceTargetPairs();
}
