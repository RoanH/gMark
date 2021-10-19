package dev.roanh.gmark.util;

import java.util.Deque;
import java.util.Objects;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.util.Graph.GraphEdge;

public class EdgeGraphData{
	
	
	
	
	
	public static EdgeGraphData of(GraphEdge<SelectivityType, Predicate> edge){
		return new PredicateData(edge);
	}
	
	public static EdgeGraphData of(String name){
		return new EndpointData(name);
	}
	
	public static IntersectionData of(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first, Deque<EdgeGraphData> second){
		return new IntersectionData(source, target, first, second);
	}
	
	protected static class IntersectionData extends EdgeGraphData{
		private EdgeGraphData source;
		private EdgeGraphData target;
		private Deque<EdgeGraphData> first;
		private Deque<EdgeGraphData> second;
		
		private IntersectionData(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first, Deque<EdgeGraphData> second){
			this.source = source;
			this.target = target;
			this.first = first;
			this.second = second;
		}
		
		public EdgeGraphData getSource(){
			return source;
		}
		
		public EdgeGraphData getTarget(){
			return target;
		}
		
		@Override
		public String toString(){
			StringBuilder builder = new StringBuilder();
			for(EdgeGraphData data : first){
				builder.append(data.toString());
				builder.append('◦');
			}
			builder.deleteCharAt(builder.length() - 1);
			builder.append(" ∩ ");
			for(EdgeGraphData data : second){
				builder.append(data.toString());
				builder.append('◦');
			}
			builder.deleteCharAt(builder.length() - 1);
			return builder.toString();
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(source, target, first, second);
		}
		
		@Override
		public boolean equals(Object other){
			if(other instanceof IntersectionData){
				IntersectionData data = (IntersectionData)other;
				if(data.source.equals(source) && data.target.equals(target)){
					return (data.first.equals(first) && data.second.equals(second)) || (data.second.equals(first) && data.first.equals(second));
				}
			}
			return false;
		}
	}
	
	private static class EndpointData extends EdgeGraphData{
		private final String name;
		
		private EndpointData(String name){
			this.name = name;
		}
		
		@Override
		public String toString(){
			return name;
		}
		
		@Override
		public boolean equals(Object other){
			return other == this;
		}
	}
	
	public static class PredicateData extends EdgeGraphData{
		private GraphEdge<SelectivityType, Predicate> edge;
		
		private PredicateData(GraphEdge<SelectivityType, Predicate> edge){
			this.edge = edge;
		}
		
		@Override
		public String toString(){
			return edge.getData().getAlias();
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(edge);
		}
		
		@Override
		public boolean equals(Object other){
			return other instanceof PredicateData ? ((PredicateData)other).edge == edge : false;
		}
	}
}