package dev.roanh.gmark.lang.cq;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.roanh.gmark.util.graph.generic.UniqueGraph;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphEdge;
import dev.roanh.gmark.util.graph.generic.UniqueGraph.GraphNode;

public class QueryGraphCQ{
	private final Collection<VarCQ> variables; 
	private final Collection<AtomCQ> edges;
	
	public QueryGraphCQ(Collection<VarCQ> variables, Collection<AtomCQ> edges){
		this.variables = variables;
		this.edges = edges;
	}
	
	public CQ toCQ(){
		return new CQ(variables, edges);
	}
	
	public int getVertexCount(){
		return variables.size();
	}
	
	public int getEdgeCount(){
		return edges.size();
	}
	
	
	
	
	
	
	
	
	public UniqueGraph<VarCQ, AtomCQ> toUniqueGraph(){
		UniqueGraph<VarCQ, AtomCQ> graph = new UniqueGraph<VarCQ, AtomCQ>();
		
		for(VarCQ v : variables){
			graph.addUniqueNode(v);
		}
		
		for(AtomCQ atom : edges){
			graph.addUniqueEdge(atom.getSource(), atom.getTarget(), atom);
		}
		
		return graph;
	}
	
	public List<QueryGraphCQ> splitOnFreeVariables(){
		UniqueGraph<VarCQ, AtomCQ> graph = toUniqueGraph();
		
		Set<AtomCQ> seen = new HashSet<AtomCQ>();
		Deque<GraphEdge<VarCQ, AtomCQ>> currentEdges = new ArrayDeque<GraphEdge<VarCQ, AtomCQ>>();
		Deque<GraphEdge<VarCQ, AtomCQ>> nextEdges = new ArrayDeque<GraphEdge<VarCQ, AtomCQ>>();
		currentEdges.add(graph.getEdges().get(0));
		
		Set<VarCQ> componentVars = new HashSet<VarCQ>();
		List<AtomCQ> componentAtoms = new ArrayList<AtomCQ>();
		List<QueryGraphCQ> components = new ArrayList<QueryGraphCQ>();
		
		while(!nextEdges.isEmpty() || !currentEdges.isEmpty()){
			if(!currentEdges.isEmpty()){
				GraphEdge<VarCQ, AtomCQ> edge = currentEdges.removeFirst();
				if(seen.add(edge.getData())){
					componentAtoms.add(edge.getData());
					
					GraphNode<VarCQ, AtomCQ> sourceNode = edge.getSourceNode();
					VarCQ source = sourceNode.getData();
					if(componentVars.add(source)){
						if(source.isFree()){
							nextEdges.addAll(sourceNode.getOutEdges());
							nextEdges.addAll(sourceNode.getInEdges());
						}else{
							currentEdges.addAll(sourceNode.getOutEdges());
							currentEdges.addAll(sourceNode.getInEdges());
						}
					}
					
					GraphNode<VarCQ, AtomCQ> targetNode = edge.getTargetNode();
					VarCQ target = targetNode.getData();
					if(componentVars.add(target)){
						if(target.isFree()){
							nextEdges.addAll(targetNode.getOutEdges());
							nextEdges.addAll(targetNode.getInEdges());
						}else{
							currentEdges.addAll(targetNode.getOutEdges());
							currentEdges.addAll(targetNode.getInEdges());
						}
					}
				}
			}else{
				if(!componentAtoms.isEmpty()){
					components.add(new QueryGraphCQ(componentVars, componentAtoms));
					componentVars = new HashSet<VarCQ>();
					componentAtoms = new ArrayList<AtomCQ>();
				}
				
				currentEdges.add(nextEdges.removeFirst());
			}
		}
		
		return components;
	}
	
	@Override
	public String toString(){
		return toCQ().toString();
	}
}
