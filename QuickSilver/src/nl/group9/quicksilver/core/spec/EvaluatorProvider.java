package nl.group9.quicksilver.core.spec;

public abstract interface EvaluatorProvider<G extends Graph, R extends Graph>{

	public abstract Evaluator<G, R> createEvaluator();
	
	public abstract G createGraph(int vertexCount, int edgeCount, int labelCount);
}
