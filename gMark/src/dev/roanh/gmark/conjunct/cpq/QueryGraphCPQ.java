package dev.roanh.gmark.conjunct.cpq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Graph;

public class QueryGraphCPQ{
	private Set<Vertex> vertices = new HashSet<Vertex>();
	private Set<Edge> edges = new HashSet<Edge>();
	private Vertex source;
	private Vertex target;
	private Set<Pair> fid = new HashSet<Pair>();
	
	//effectively identity
	protected QueryGraphCPQ(Vertex source, Vertex target){
		vertices.add(this.source = source);
		vertices.add(this.target = target);
		fid.add(new Pair(source, target));
	}
	
	protected QueryGraphCPQ(EdgeCPQ cpq, Vertex source, Vertex target){
		vertices.add(this.source = source);
		vertices.add(this.target = target);
		edges.add(new Edge(source, target, cpq.getLabel()));
	}
	
	public static class Vertex{
		public static final Vertex SOURCE = new Vertex();
		public static final Vertex TARGET = new Vertex();
		
		@Override
		public String toString(){
			if(this == SOURCE){
				return "src";
			}else if(this == TARGET){
				return "trg";
			}else{
				return String.valueOf((char)('a' + (this.hashCode() % 26)));
			}
		}
	}
	
	public String getVertexString(Vertex vertex){
		if(vertex == source){
			return vertex == target ? "src,trg" : "src";
		}else{
			return vertex == target ? "trg" : "";
		}
	}
	
	protected QueryGraphCPQ union(QueryGraphCPQ other){
		vertices.addAll(other.vertices);
		edges.addAll(other.edges);
		fid.addAll(other.fid);
		return this;
	}
	
	protected void setTarget(Vertex target){
		this.target = target;
	}
	
	public Graph<Vertex, Predicate> toGraph(){
		merge();
		Graph<Vertex, Predicate> graph = new Graph<Vertex, Predicate>();
		vertices.forEach(graph::addUniqueNode);
		for(Edge edge : edges){
			graph.addUniqueEdge(edge.src, edge.trg, edge.label);
		}
		return graph;
	}
	
	private void merge(){
		while(!fid.isEmpty()){
			Pair elem = getIdPair();
			
			vertices.remove(elem.first);
			
			for(Edge edge : edges.stream().collect(Collectors.toList())){
				if(edge.src == elem.first){
					edges.add(new Edge(elem.second, edge.trg, edge.label));
				}
				
				if(edge.trg == elem.first){
					edges.add(new Edge(edge.src, elem.second, edge.label));
				}
			}
			edges.removeIf(e->e.src == elem.first || e.trg == elem.first);
			
			source = source == elem.first ? source : elem.second;
			target = target == elem.first ? target : elem.second;
			
			for(Pair pair : fid.stream().collect(Collectors.toList())){
				if(pair.second == elem.first){
					fid.add(new Pair(elem.second, pair.second));
				}
				
				if(pair.first == elem.first){
					fid.add(new Pair(pair.first, elem.second));
				}
			}
		}
	}
	
	private Pair getIdPair(){
		Iterator<Pair> iter = fid.iterator();
		Pair elem = iter.next();
		iter.remove();
		return elem;
	}
	
	@Override
	public String toString(){
		return "QueryGraphCPQ[V=" + vertices + ",E=" + edges + ",src=" + source + ",trg=" + target + ",Fid=" + fid + "]";
	}
	
	private static class Edge{
		private Vertex src;
		private Vertex trg;
		private Predicate label;
		
		private Edge(Vertex v, Vertex u, Predicate label){
			this.src = v;
			this.trg = u;
			this.label = label;
		}
		
		@Override
		public String toString(){
			return "(" + src + "," + trg + "," + label.getAlias() + ")";
		}
	}
	
	private static class Pair{
		private Vertex first;
		private Vertex second;
		
		private Pair(Vertex v, Vertex u){
			this.first = v;
			this.second = u;
		}
		
		@Override
		public String toString(){
			return "(" + first + "," + second + ")";
		}
	}
}
