package dev.roanh.gmark.lang.cpq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.SimpleGraph;
import dev.roanh.gmark.util.graph.generic.SimpleGraph.SimpleVertex;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;
import dev.roanh.gmark.util.graph.specific.SpanningTreeDFS;

public class DecompositionCPQ{

//	public static void decompose(CQ query){
//		decompose(query.toQueryGraph().toUniqueGraph());
//	}
	
	
	
	
	public static <E> List<UniqueGraph<Vertex, E>> decompose(UniqueGraph<Vertex, E> graph){
		//a CPQ can have at most two projected variables
		Set<Vertex> projected = getProjectedVertices(graph).map(GraphNode::getData).collect(Collectors.toSet());
		System.out.println("initial projected: " + projected.size());
		if(projected.size() <= 2){
			return decomposeBoundedComponent(graph);
		}else{
			//if we have more we split into components and decompose those
			return decomposeComponentSplits(graph, projected);
		}
	}
	
	private static <E> List<UniqueGraph<Vertex, E>> decomposeComponentSplits(UniqueGraph<Vertex, E> graph, Set<Vertex> splitVertices){
		List<UniqueGraph<Vertex, E>> cpqs = new ArrayList<UniqueGraph<Vertex, E>>();
		System.out.println("split on: " + splitVertices);
		List<UniqueGraph<Vertex, E>> splits = Util.splitOnNodes(graph, splitVertices);
		for(UniqueGraph<Vertex, E> split : splits){
			System.out.println("split: " + toString(split));
			//TODO split result is currently incorrect
		}
		
		for(UniqueGraph<Vertex, E> component : splits){
			cpqs.addAll(decomposeComponent(component));
		}
		
		System.out.println("--- end splits ---");
		
		return cpqs;
	}
	
	//here we guarantee that free variables are on the edges, i.e., all free variables combined enclose a single connected component
	private static <E> List<UniqueGraph<Vertex, E>> decomposeComponent(UniqueGraph<Vertex, E> graph){
		Set<GraphNode<Vertex, E>> projected = getProjectedVertices(graph).collect(Collectors.toSet());
		System.out.println("component projected: " + projected.size());
		if(projected.size() <= 2){
			return decomposeBoundedComponent(graph);
		}
		
		//TODO probably overloaded is a hack
//		Set<Vertex> overloaded = markOverloadedVertices(graph);
//		if(!overloaded.isEmpty()){
//			overloaded.addAll(projected);
//			return decomposeComponentSplits(graph, overloaded);
//		}
		
		SimpleGraph<GraphNode<Vertex, E>, Void> simple = graph.toSimpleGraph();
		SpanningTreeDFS<GraphNode<Vertex, E>, Void> dfs = new SpanningTreeDFS<GraphNode<Vertex, E>, Void>(simple);
		for(GraphNode<Vertex, E> free : projected){
//			dfs.addForcedLeaf(simple.getVertex(free));
			//TODO the above doesn't appear to work entirely yet
		}
		
		List<GraphNode<Vertex, E>> merges = dfs.getArticulationPoints().stream().map(SimpleVertex::getData).toList();
		System.out.println("ap: " + merges);
		
		
		
		

			
		//TODO ?
		return null;
	}
	
	//if there exists a vertex directly reachable from more than 2 free vertices, it has to be free too
	private static <E> Set<Vertex> markOverloadedVertices(UniqueGraph<Vertex, E> graph){
		Set<Vertex> found = new HashSet<Vertex>();
		for(GraphNode<Vertex, E> node : graph.getNodes()){
			if(!node.getData().isProjected()){
				int reachable = 0;
				
				for(GraphEdge<Vertex, E> edge : node.getOutEdges()){
					if(edge.getTarget().isProjected()){
						reachable++;
					}
				}
				
				for(GraphEdge<Vertex, E> edge : node.getInEdges()){
					if(edge.getSource().isProjected()){
						reachable++;
					}
				}
				
				if(reachable > 2){
					node.getData().projected = true;
					found.add(node.getData());
				}
			}
		}
		
		return found;
	}
	
	//graph with #free variables <= 2, these either are or aren't CPQs, maybe use this to convert CPQ query graphs back to CPQs
	private static <E> List<UniqueGraph<Vertex, E>> decomposeBoundedComponent(UniqueGraph<Vertex, E> graph){
		System.out.println("append: " + toString(graph));
		return List.of(graph);//TODO
	}
	
	private static <E> String toString(UniqueGraph<Vertex, E> graph){
		return graph.getNodes().stream().map(n->n.getData().label).collect(Collectors.joining(", "));
	}

	//articulation points always need to be projected
//	Util.computeArticulationPoints(graph).forEach(v->v.getData().projected = true);
	
	private static <E> Stream<GraphNode<Vertex, E>> getProjectedVertices(UniqueGraph<Vertex, E> graph){
		return graph.getNodes().stream().filter(v->v.getData().isProjected());
	}
	
	public static class Vertex{
//		private ? original;
		private String label;//TODO remove/replace
		private boolean projected;
		
		public Vertex(String label, boolean projected){
			this.label = label;
			this.projected = projected;
		}
		
		public boolean isProjected(){
			return projected;
		}
		
		@Override
		public String toString(){
			return label;
		}
	}
}
