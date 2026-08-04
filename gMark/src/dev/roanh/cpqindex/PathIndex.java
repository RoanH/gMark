/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
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
 * along with gMark.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

/**
 * CPQx-style path index backed by a k-path-bisimulation partition.
 * <p>
 * This index retains the label sequences associated with each partition block
 * and evaluates CPQs without computing canonical cores.
 * @author Roan
 * @see Index
 */
public final class PathIndex implements CPQIndex{
	/**
	 * The coreless k-path-bisimulation partition used by this index.
	 */
	private final Index partition;
	/**
	 * Evaluator for the retained path and label data.
	 */
	private final PathIndexEvaluator evaluator;

	/**
	 * Constructs a path index for the given graph and diameter.
	 * @param graph The graph to index.
	 * @param k The maximum indexed path length.
	 * @throws IllegalArgumentException When {@code k} is less than one.
	 * @throws InterruptedException When construction is interrupted.
	 */
	public PathIndex(UniqueGraph<Integer, Predicate> graph, int k) throws IllegalArgumentException, InterruptedException{
		this(graph, k, ProgressListener.NONE);
	}

	/**
	 * Constructs a path index for the given graph and diameter.
	 * @param graph The graph to index.
	 * @param k The maximum indexed path length.
	 * @param listener The listener for construction progress updates.
	 * @throws IllegalArgumentException When {@code k} is less than one.
	 * @throws InterruptedException When construction is interrupted.
	 */
	public PathIndex(UniqueGraph<Integer, Predicate> graph, int k, ProgressListener listener) throws IllegalArgumentException, InterruptedException{
		if(k < 1){
			throw new IllegalArgumentException("Invalid value of k for bisimulation, has to be 1 or greater.");
		}
		partition = new Index(graph, k, false, true, 1, Integer.MAX_VALUE, listener);
		evaluator = new PathIndexEvaluator(partition.getBlocks(), k);
	}

	/**
	 * Reads a fully saved path index from the given stream.
	 * @param source The stream containing the index.
	 * @throws IOException When the index cannot be read or does not contain the
	 *         label data required for path-index evaluation.
	 */
	public PathIndex(InputStream source) throws IOException{
		partition = new Index(source);
		if(partition.getBlocks().stream().anyMatch(block->block.getLabels() == null)){
			throw new IOException("The saved index does not contain label data required for path-index evaluation.");
		}
		evaluator = new PathIndexEvaluator(partition.getBlocks(), partition.getK());
	}

	/**
	 * Writes this path index to the given stream. Path indexes are always saved
	 * in the full format because their label data is required for evaluation.
	 * @param target The stream to write to.
	 * @throws IOException When the index cannot be written.
	 */
	public void write(OutputStream target) throws IOException{
		partition.write(target, true);
	}

	@Override
	public List<Pair> query(CPQ cpq) throws IllegalArgumentException{
		validate(cpq);
		return evaluator.query(cpq);
	}

	@Override
	public long computeResultCardinality(CPQ cpq) throws IllegalArgumentException{
		validate(cpq);
		return evaluator.computeResultCardinality(cpq);
	}

	@Override
	public int getK(){
		return partition.getK();
	}

	/**
	 * Validates that the given query can be evaluated by this index. Composite
	 * queries may have a diameter larger than k because their indexed path
	 * fragments are joined during evaluation.
	 * @param cpq The query to validate.
	 * @throws IllegalArgumentException When the query diameter is zero.
	 */
	private void validate(CPQ cpq) throws IllegalArgumentException{
		if(cpq.getDiameter() == 0){
			throw new IllegalArgumentException("Query diameter cannot be 0.");
		}
	}
}
