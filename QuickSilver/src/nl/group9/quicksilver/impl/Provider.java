package nl.group9.quicksilver.impl;

import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.core.spec.EvaluatorProvider;

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
