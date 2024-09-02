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

public abstract class EvaluatorTest<G extends Graph>{
	private static final Predicate a = new Predicate(0, "a");
	private static final Predicate b = new Predicate(1, "b");
	
	public abstract Evaluator<G> getEvaluator();

	@Test
	public void query0(){
		EvalResult result = evaluate(1, CPQ.intersect(
			CPQ.labels(a, b),
			CPQ.label(b)
		));
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 2),
			new SourceTargetPair(1, 2)
		));
		
		assertEquals(new CardStat(1, 1, 1), result.cardStat());
	}
	
	@Test
	public void query1(){
		EvalResult result = evaluate(1, RPQ.kleene(RPQ.label(a)));
		
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
		
		assertEquals(new CardStat(1, 8, 8), result.cardStat());
	}
	
	private void assertPaths(EvalResult result, List<SourceTargetPair> expected){
		List<SourceTargetPair> actual = result.resultGraph().getSourceTargetPairs();
		actual.sort(null);
		assertIterableEquals(expected, actual);
	}
	
	private EvalResult evaluate(QueryLanguageSyntax query){
		return evaluate(PathQuery.of(query.toAbstractSyntaxTree()));
	}

	private EvalResult evaluate(QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(query.toAbstractSyntaxTree(), target));
	}

	private EvalResult evaluate(int source, QueryLanguageSyntax query){
		return evaluate(PathQuery.of(source, query.toAbstractSyntaxTree()));
	}

	private EvalResult evaluate(int source, QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(source, query.toAbstractSyntaxTree(), target));
	}
	
	private EvalResult evaluate(PathQuery query){
		Evaluator<G> evaluator = getEvaluator();
		evaluator.prepare(getGraph(evaluator));
		G result = evaluator.evaluate(query);
		return new EvalResult(result, evaluator.computeCardinality(result));
	}
	
	//see: https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsubsection.5.2.1.1
	private G getGraph(Evaluator<G> evaluator){
		G graph = evaluator.createGraph(14, 27, 2);
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
	
	private static final record EvalResult(Graph resultGraph, CardStat cardStat){
	}
}
