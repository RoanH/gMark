package nl.group9.quicksilver;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.rpq.RPQ;

import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.impl.SimpleEvaluator;
import nl.group9.quicksilver.impl.SimpleGraph;

public class Main{

	public static void main(String[] args){
		SimpleEvaluator evaluator = new SimpleEvaluator();
		
		SimpleGraph graph = evaluator.createGraph(14, 27, 2);
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
		evaluator.prepare(graph);
		
		Predicate a = new Predicate(0, "a");
		Predicate b = new Predicate(1, "b");
		RPQ query = RPQ.labels(a, b);
		System.out.println("Query: " + query);
		
		SimpleGraph result = evaluator.evaluate(PathQuery.of(query.toAbstractSyntaxTree()));
		result.getSourceTargetPairs().forEach(System.out::println);
		System.out.println(evaluator.computeCardinality(result));
	}
}
