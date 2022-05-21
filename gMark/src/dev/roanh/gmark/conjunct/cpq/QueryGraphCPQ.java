package dev.roanh.gmark.conjunct.cpq;

import java.util.HashSet;
import java.util.Set;

import dev.roanh.gmark.core.graph.Predicate;

public class QueryGraphCPQ{
	private Set<Vertex> vertices = new HashSet<Vertex>();
	private Set<Edge> edges = new HashSet<Edge>();
	private Vertex source;
	private Vertex target;
	private Set<Pair> fid = new HashSet<Pair>();
	
	//effectively identity
	protected QueryGraphCPQ(Vertex source, Vertex target){
		vertices.add(source);
		vertices.add(target);
		fid.add(new Pair(source, target));
	}
	
	protected QueryGraphCPQ(EdgeCPQ cpq, Vertex source, Vertex target){
		vertices.add(source);
		vertices.add(target);
		edges.add(new Edge(source, target, cpq.getLabel()));
	}
	
	public static class Vertex{
		public static final Vertex SOURCE = new Vertex();
		public static final Vertex TARGET = new Vertex();
		
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
	
	private class Edge{
		private Vertex v;
		private Vertex u;
		private Predicate label;
		
		private Edge(Vertex v, Vertex u, Predicate label){
			this.v = v;
			this.u = u;
			this.label = label;
		}
	}
	
	private class Pair{
		private Vertex v;
		private Vertex u;
		
		private Pair(Vertex v, Vertex u){
			this.v = v;
			this.u = u;
		}
	}
}
