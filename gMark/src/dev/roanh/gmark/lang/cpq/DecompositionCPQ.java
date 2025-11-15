package dev.roanh.gmark.lang.cpq;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

public class DecompositionCPQ{

//	public static void decompose(CQ query){
//		decompose(query.toQueryGraph().toUniqueGraph());
//	}
	
	
	
	
	public static <E> List<UniqueGraph<Vertex, E>> decompose(UniqueGraph<Vertex, E> graph){
		//a CPQ can have at most two projected variables
		Set<Vertex> projected = getProjectedVertices(graph);
		if(projected.size() <= 2){
			return decomposeBoundedComponent(graph);
		}else{
			//if we have more we split into components and decompose those
			return decomposeComponentSplits(graph, projected);
		}
	}
	
	private static <E> List<UniqueGraph<Vertex, E>> decomposeComponentSplits(UniqueGraph<Vertex, E> graph, Set<Vertex> splitVertices){
		List<UniqueGraph<Vertex, E>> cpqs = new ArrayList<UniqueGraph<Vertex, E>>();
		for(UniqueGraph<Vertex, E> component : Util.splitOnNodes(graph, splitVertices)){
			cpqs.addAll(decomposeComponent(component));
		}
		
		return cpqs;
	}
	
	//here we guarantee that free variables are on the edges, i.e., all free variables combined enclose a single connected component
	private static <E> List<UniqueGraph<Vertex, E>> decomposeComponent(UniqueGraph<Vertex, E> graph){
		Set<Vertex> projected = getProjectedVertices(graph);
		if(projected.size() <= 2){
			return decomposeBoundedComponent(graph);
		}
		
		Set<Vertex> overloaded = markOverloadedVertices(graph);
		if(!overloaded.isEmpty()){
			overloaded.addAll(projected);
			return decomposeComponentSplits(graph, overloaded);
		}
		
		
		
		
		
		

			
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
	private static <E> List<UniqueGraph<Vertex, E>> decomposeBoundedComponent(UniqueGraph<Vertex, ?> graph){
		return null;//TODO
	}
	

	//articulation points always need to be projected
//	Util.computeArticulationPoints(graph).forEach(v->v.getData().projected = true);
	
	private static Set<Vertex> getProjectedVertices(UniqueGraph<Vertex, ?> graph){
		return graph.getNodes().stream().map(GraphNode::getData).filter(Vertex::isProjected).collect(Collectors.toSet());
	}
	
	public static class Vertex{
//		private ? original;
		private boolean projected;
		
		public boolean isProjected(){
			return projected;
		}
	}
}
