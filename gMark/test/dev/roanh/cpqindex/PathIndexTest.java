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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.eval.PathQuery;
import dev.roanh.gmark.eval.ReachabilityQueryEvaluator;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.type.schema.Predicate;
import dev.roanh.gmark.util.graph.generic.IntGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;

/**
 * Tests for CPQ evaluation using a coreless path index.
 * @author Roan
 */
public class PathIndexTest{
	private static final Predicate A = new Predicate(0, "a");
	private static final Predicate B = new Predicate(1, "b");
	private static final Predicate C = new Predicate(2, "c");
	private static final List<Predicate> LABELS = List.of(A, B, C);

	/**
	 * Tests all CPQ operations and relevant combinations against gMark's direct evaluator.
	 */
	@Test
	public void evaluateOperations() throws IllegalArgumentException, InterruptedException{
		GraphData data = fixedGraph();
		PathIndex index = new PathIndex(data.indexGraph(), 3);
		ReachabilityQueryEvaluator direct = new ReachabilityQueryEvaluator(data.evalGraph());

		List<CPQ> queries = List.of(
			CPQ.label(A),
			CPQ.label(A.getInverse()),
			CPQ.labels(A, B),
			CPQ.labels(A, B, A),
			CPQ.labels(A.getInverse(), C),
			CPQ.intersect(CPQ.label(A), CPQ.label(C)),
			CPQ.intersect(CPQ.labels(A, B), CPQ.label(C)),
			CPQ.concat(CPQ.intersect(CPQ.label(A), CPQ.label(C)), CPQ.label(B)),
			CPQ.concat(CPQ.label(A), CPQ.intersect(CPQ.label(B), CPQ.label(C))),
			CPQ.intersect(CPQ.label(C), CPQ.id()),
			CPQ.intersect(CPQ.labels(A, B), CPQ.id()),
			CPQ.concat(CPQ.id(), CPQ.label(A)),
			CPQ.concat(CPQ.label(A), CPQ.id()),
			CPQ.intersect(CPQ.id(), CPQ.intersect(CPQ.id(), CPQ.label(C)))
		);

		for(CPQ query : queries){
			assertMatchesDirect(index, direct, query);
		}
	}

	/**
	 * Tests that the public contract delegates to path-index evaluation.
	 */
	@Test
	public void commonInterface() throws IllegalArgumentException, InterruptedException{
		GraphData data = fixedGraph();
		CPQIndex index = new PathIndex(data.indexGraph(), 3);

		assertEquals(3, index.getK());
		assertMatchesDirect(index, new ReachabilityQueryEvaluator(data.evalGraph()), CPQ.labels(A, B));
	}

	/**
	 * Tests queries beyond the indexed path length and validation of identity.
	 */
	@Test
	public void indexedPathLength() throws IllegalArgumentException, InterruptedException{
		GraphData data = fixedGraph();
		PathIndex index = new PathIndex(data.indexGraph(), 2);
		ReachabilityQueryEvaluator direct = new ReachabilityQueryEvaluator(data.evalGraph());

		assertThrows(IllegalArgumentException.class, ()->index.query(CPQ.id()));
		assertMatchesDirect(index, direct, CPQ.labels(A, B, A));
		assertMatchesDirect(index, direct, CPQ.concat(CPQ.labels(A, B), CPQ.labels(A, B)));
		assertMatchesDirect(index, direct, CPQ.intersect(
			CPQ.concat(CPQ.labels(A, B), CPQ.labels(A.getInverse(), B.getInverse())),
			CPQ.id()
		));
		assertThrows(IllegalArgumentException.class, ()->new PathIndex(fixedGraph().indexGraph(), 0));
	}

	/**
	 * Tests that the full path-index representation survives a write/read cycle.
	 */
	@Test
	public void writeRead() throws IllegalArgumentException, InterruptedException, IOException{
		GraphData data = fixedGraph();
		PathIndex index = new PathIndex(data.indexGraph(), 3);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out);

		PathIndex read = new PathIndex(new ByteArrayInputStream(out.toByteArray()));
		ReachabilityQueryEvaluator direct = new ReachabilityQueryEvaluator(data.evalGraph());
		assertEquals(index.getK(), read.getK());
		assertMatchesDirect(read, direct, CPQ.label(A.getInverse()));
		assertMatchesDirect(read, direct, CPQ.labels(A, B, A));
		assertMatchesDirect(read, direct, CPQ.intersect(CPQ.labels(A, B), CPQ.label(C)));
	}

	/**
	 * Tests that a compact index without retained labels cannot be used as a path index.
	 */
	@Test
	public void rejectIndexWithoutLabels() throws IllegalArgumentException, InterruptedException, IOException{
		Index index = new Index(fixedGraph().indexGraph(), 2, false, true, 1);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		index.write(out, false);

		assertThrows(IOException.class, ()->new PathIndex(new ByteArrayInputStream(out.toByteArray())));
	}

	/**
	 * Differentially tests deterministic random graphs and CPQs against direct evaluation.
	 */
	@Test
	public void randomDifferential() throws IllegalArgumentException, InterruptedException{
		Random random = new Random(0xC0FFEE);
		for(int graphId = 0; graphId < 5; graphId++){
			GraphData data = randomGraph(random, 7, 30);
			PathIndex index = new PathIndex(data.indexGraph(), 2);
			ReachabilityQueryEvaluator direct = new ReachabilityQueryEvaluator(data.evalGraph());

			for(int tested = 0; tested < 100; tested++){
				CPQ query = randomQuery(random, 3);
				assertMatchesDirect(index, direct, query);
			}
		}
	}

	/**
	 * Compares indexed evaluation with direct evaluation.
	 * @param index The index to test.
	 * @param direct The direct evaluator used as the oracle.
	 * @param query The query to evaluate.
	 */
	private static void assertMatchesDirect(CPQIndex index, ReachabilityQueryEvaluator direct, CPQ query){
		List<Pair> expected = direct.evaluate(PathQuery.of(query)).getSourceTargetPairs().stream()
			.map(pair->new Pair(pair.source(), pair.target()))
			.sorted()
			.toList();
		List<Pair> actual = index.query(query);

		assertEquals(expected, actual, query.toString());
		assertEquals(expected.size(), index.computeResultCardinality(query), query.toString());
	}

	/**
	 * Constructs the fixed graph used by operation tests.
	 * @return The graph in index and direct-evaluator form.
	 */
	private static GraphData fixedGraph(){
		GraphData data = emptyGraph(4);
		addEdge(data, 0, 1, A);
		addEdge(data, 0, 2, A);
		addEdge(data, 3, 0, A);
		addEdge(data, 1, 3, B);
		addEdge(data, 2, 3, B);
		addEdge(data, 3, 2, B);
		addEdge(data, 0, 1, C);
		addEdge(data, 0, 3, C);
		addEdge(data, 1, 1, C);
		addEdge(data, 1, 3, C);
		addEdge(data, 3, 3, C);
		return data;
	}

	/**
	 * Constructs a deterministic random graph.
	 * @param random The random source.
	 * @param vertices The number of vertices.
	 * @param edges The number of edge insertion attempts.
	 * @return The generated graph.
	 */
	private static GraphData randomGraph(Random random, int vertices, int edges){
		GraphData data = emptyGraph(vertices);
		for(int i = 0; i < edges; i++){
			addEdge(data, random.nextInt(vertices), random.nextInt(vertices), LABELS.get(random.nextInt(LABELS.size())));
		}
		return data;
	}

	/**
	 * Constructs an empty graph in both required representations.
	 * @param vertices The number of vertices.
	 * @return The empty graph data.
	 */
	private static GraphData emptyGraph(int vertices){
		UniqueGraph<Integer, Predicate> indexGraph = new UniqueGraph<Integer, Predicate>();
		for(int i = 0; i < vertices; i++){
			indexGraph.addUniqueNode(i);
		}
		return new GraphData(indexGraph, new IntGraph(vertices, LABELS.size()));
	}

	/**
	 * Adds an edge to both graph representations.
	 * @param data The graph data to update.
	 * @param source The source vertex.
	 * @param target The target vertex.
	 * @param label The edge label.
	 */
	private static void addEdge(GraphData data, int source, int target, Predicate label){
		data.indexGraph().addUniqueEdge(source, target, label);
		data.evalGraph().addEdge(source, target, label.getID());
	}

	/**
	 * Generates a random CPQ using all supported CPQ operations.
	 * @param random The random source.
	 * @param depth The remaining recursion depth.
	 * @return The generated CPQ.
	 */
	private static CPQ randomQuery(Random random, int depth){
		if(depth == 0){
			Predicate label = LABELS.get(random.nextInt(LABELS.size()));
			return CPQ.label(random.nextBoolean() ? label : label.getInverse());
		}

		return switch(random.nextInt(4)){
		case 0 -> randomQuery(random, 0);
		case 1 -> CPQ.concat(randomQuery(random, depth - 1), randomQuery(random, depth - 1));
		case 2 -> CPQ.intersect(randomQuery(random, depth - 1), randomQuery(random, depth - 1));
		default -> CPQ.intersect(randomQuery(random, depth - 1), CPQ.id());
		};
	}

	/**
	 * The two graph representations needed for differential testing.
	 * @param indexGraph The graph used for index construction.
	 * @param evalGraph The graph used for direct evaluation.
	 */
	private static record GraphData(UniqueGraph<Integer, Predicate> indexGraph, IntGraph evalGraph){
	}
}
