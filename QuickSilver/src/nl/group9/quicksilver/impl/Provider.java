package nl.group9.quicksilver.impl;

import nl.group9.quicksilver.core.spec.DatabaseGraph;
import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.core.spec.EvaluatorProvider;

/**
 * Simple bridging class to provided to concrete evaluator and
 * database graph implementation that should be used by QuickSilver.
 * @author Roan
 * @see DatabaseGraph
 * @see Evaluator
 */
public class Provider implements EvaluatorProvider<SimpleGraph, SimpleGraph>{

	@Override
	public Evaluator<SimpleGraph, SimpleGraph> createEvaluator(){
		return new SimpleEvaluator();
	}

	@Override
	public SimpleGraph createGraph(int vertexCount, int edgeCount, int labelCount){
		return new SimpleGraph(vertexCount, labelCount);
	}
}
