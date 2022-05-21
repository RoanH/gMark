package dev.roanh.gmark.conjunct.cpq;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

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
		
		@Override
		public String toString(){
			if(this == SOURCE){
				return "s";
			}else if(this == TARGET){
				return "t";
			}else{
				return String.valueOf(this.hashCode() % 100);//TODO empty
			}
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
	
	//TODO private
	public void merge(){
		while(!fid.isEmpty()){
			Pair elem = getIdPair();
			
			vertices.remove(elem.v);
			
			for(Edge edge : edges.stream().collect(Collectors.toList())){
				if(edge.v == elem.v){
					edges.add(new Edge(elem.u, edge.u, edge.label));
				}
				
				if(edge.u == elem.v){
					edges.add(new Edge(edge.v, elem.u, edge.label));
				}
			}
			edges.removeIf(e->e.v == elem.v || e.u == elem.v);
			
			source = source == elem.v ? source : elem.u;
			target = target == elem.v ? target : elem.u;
			
			for(Pair pair : fid.stream().collect(Collectors.toList())){
				if(pair.u == elem.v){
					fid.add(new Pair(elem.u, pair.u));
				}
				
				if(pair.v == elem.v){
					fid.add(new Pair(pair.v, elem.u));
				}
			}
			
			System.out.println(this);
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
		return "QueryGraphCPQ[V=" + vertices + ",E=" + edges + ",Fid=" + fid + "]";
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
		
		@Override
		public String toString(){
			return "(" + v + "," + u + "," + label.getAlias() + ")";
		}
	}
	
	private class Pair{
		private Vertex v;
		private Vertex u;
		
		private Pair(Vertex v, Vertex u){
			this.v = v;
			this.u = u;
		}
		
		@Override
		public String toString(){
			return "(" + v + "," + u + ")";
		}
	}
}
