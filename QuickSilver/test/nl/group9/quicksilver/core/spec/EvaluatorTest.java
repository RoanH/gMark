package nl.group9.quicksilver.core.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.data.SourceTargetPair;

public abstract class EvaluatorTest<G extends Graph, R extends Graph>{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");
	
	public abstract EvaluatorProvider<G, R> getProvider();

	@Test
	public void query0(){
		R result = evaluate(
			1,
			CPQ.intersect(
				CPQ.label(b),
				CPQ.labels(
					a,
					b
				)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 2)
		));
		
		assertEquals(new CardStat(1, 1, 1), result.computeCardinality());
	}
	
	@Test
	public void query1(){
		R result = evaluate(
			1,
			RPQ.kleene(
				RPQ.label(a)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(1, 6),
			new SourceTargetPair(1, 8),
			new SourceTargetPair(1, 9),
			new SourceTargetPair(1, 10),
			new SourceTargetPair(1, 11),
			new SourceTargetPair(1, 12)
		));
		
		assertEquals(new CardStat(1, 8, 8), result.computeCardinality());
	}
	
	@Test
	public void query2(){
		R result = evaluate(
			CPQ.id()
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(2, 2),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 12),
			new SourceTargetPair(13, 13)
		));
		
		assertEquals(new CardStat(14, 14, 14), result.computeCardinality());
	}
	
	@Test
	public void query3(){
		R result = evaluate(
			RPQ.labels(
				b.getInverse(),
				a,
				b
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(2, 2),
			new SourceTargetPair(2, 13),
			new SourceTargetPair(13, 2),
			new SourceTargetPair(13, 13)
		));
		
		assertEquals(new CardStat(2, 4, 2), result.computeCardinality());
	}
	
	@Test
	public void query4(){
		R result = evaluate(
			RPQ.disjunct(
				RPQ.label(a),
				RPQ.label(b)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 2),
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 2),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(3, 6),
			new SourceTargetPair(3, 9),
			new SourceTargetPair(4, 2),
			new SourceTargetPair(4, 7),
			new SourceTargetPair(5, 2),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(6, 2),
			new SourceTargetPair(6, 8),
			new SourceTargetPair(6, 10),
			new SourceTargetPair(7, 2),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 10),
			new SourceTargetPair(8, 12),
			new SourceTargetPair(8, 13),
			new SourceTargetPair(9, 8),
			new SourceTargetPair(9, 13),
			new SourceTargetPair(10, 2),
			new SourceTargetPair(10, 11),
			new SourceTargetPair(11, 0),
			new SourceTargetPair(11, 2),
			new SourceTargetPair(12, 11),
			new SourceTargetPair(12, 13)
		));
		
		assertEquals(new CardStat(12, 27, 13), result.computeCardinality());
	}
	
	@Test
	public void query5(){
		R result = evaluate(
			CPQ.intersect(
				CPQ.id(),
				CPQ.labels(
					a,
					a.getInverse()
				)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 1),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 12)
		));
		
		assertEquals(new CardStat(11, 11, 11), result.computeCardinality());
	}
	
	@Test
	public void query6(){
		R result = evaluate(
			CPQ.intersect(
				CPQ.labels(
					a,
					a,
					a
				),
				CPQ.id()
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(7, 7)
		));
		
		assertEquals(new CardStat(3, 3, 3), result.computeCardinality());
	}
	
	@Test
	public void query7(){
		R result = evaluate(
			CPQ.concat(
				CPQ.intersect(
					CPQ.id(),
					CPQ.labels(
						a,
						a,
						a
					)
				),
				CPQ.label(a)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 7),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 10)
		));
		
		assertEquals(new CardStat(3, 4, 4), result.computeCardinality());
	}
	
	@Test
	public void query8(){
		R result = evaluate(
			RPQ.label(a),
			10
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(6, 10),
			new SourceTargetPair(7, 10)
		));
		
		assertEquals(new CardStat(2, 2, 1), result.computeCardinality());
	}
	
	@Test
	public void query9(){
		R result = evaluate(
			3,
			RPQ.labels(
				a,
				a
			),
			8
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(3, 8)
		));
		
		assertEquals(new CardStat(1, 1, 1), result.computeCardinality());
	}
	
	@Test
	public void query10(){
		R result = evaluate(
			3,
			RPQ.labels(
				a,
				b
			),
			8
		);
		
		assertPaths(result, List.of());
		
		assertEquals(new CardStat(0, 0, 0), result.computeCardinality());
	}
	
	private void assertPaths(R result, List<SourceTargetPair> expected){
		//we allow duplicates in the output, but by definition there should not be any
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().distinct().sorted().toList());
	}
	
	private R evaluate(QueryLanguageSyntax query){
		return evaluate(PathQuery.of(query));
	}

	private R evaluate(QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(query, target));
	}

	private R evaluate(int source, QueryLanguageSyntax query){
		return evaluate(PathQuery.of(source, query));
	}

	private R evaluate(int source, QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(source, query, target));
	}
	
	private R evaluate(PathQuery query){
		Evaluator<G, R> evaluator = getProvider().createEvaluator();
		evaluator.prepare(getGraph());
		return evaluator.evaluate(query);
	}
	
	//see: https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsubsection.5.2.1.1
	private G getGraph(){
		G graph = getProvider().createGraph(14, 27, 2);
		graph.addEdge(0, 2, 1);
		graph.addEdge(1, 0, 0);
		graph.addEdge(1, 2, 1);
		graph.addEdge(1, 3, 0);
		graph.addEdge(3, 2, 1);
		graph.addEdge(3, 6, 0);
		graph.addEdge(3, 9, 0);
		graph.addEdge(4, 2, 1);
		graph.addEdge(4, 7, 0);
		graph.addEdge(5, 4, 0);
		graph.addEdge(5, 2, 1);
		graph.addEdge(6, 8, 0);
		graph.addEdge(6, 10, 0);
		graph.addEdge(6, 2, 1);
		graph.addEdge(7, 5, 0);
		graph.addEdge(7, 2, 1);
		graph.addEdge(7, 10, 0);
		graph.addEdge(8, 12, 0);
		graph.addEdge(8, 13, 1);
		graph.addEdge(9, 8, 0);
		graph.addEdge(9, 13, 1);
		graph.addEdge(10, 2, 1);
		graph.addEdge(10, 11, 0);
		graph.addEdge(11, 0, 0);
		graph.addEdge(11, 2, 1);
		graph.addEdge(12, 11, 0);
		graph.addEdge(12, 13, 1);
		return graph;
	}
}
