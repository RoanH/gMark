package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.util.Graph.GraphEdge;

public abstract class EdgeGraphData{
	
	
	
	
	public static PredicateData of(GraphEdge<SelectivityType, Predicate> edge){
		return new PredicateData(edge);
	}
	
	public static EndpointData of(String name, SelectivityType type){
		return new EndpointData(name, type);
	}
	
	public static IntersectionData of(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first, Deque<EdgeGraphData> second){
		return new IntersectionData(source, target, first, second);
	}
	
	public static IntersectionData of(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first){
		Deque<EdgeGraphData> second = new ArrayDeque<EdgeGraphData>();
		second.add(new IdentityData(source.getTargetType()));
		return new IntersectionData(source, target, first, second);
	}
	
	public static IdentityData of(Type type){
		return new IdentityData(type);
	}
	
	public abstract int size();
	
	public abstract Type getSourceType();
	
	public abstract Type getTargetType();
	
	public abstract Selectivity getSourceSelectivity();

	public abstract Selectivity getTargetSelectivity();
	
	protected static class IdentityData extends EdgeGraphData{
		private Type type;
		
		private IdentityData(Type type){
			this.type = type;
		}

		@Override
		public int size(){
			return 0;
		}

		@Override
		public Type getSourceType(){
			return type;
		}

		@Override
		public Type getTargetType(){
			return type;
		}
		
		@Override
		public String toString(){
			return "id";
		}

		@Override
		public Selectivity getSourceSelectivity(){
			//unused, but this makes most sense
			return type.isScalable() ? Selectivity.LINEAR : Selectivity.CONSTANT;
		}

		@Override
		public Selectivity getTargetSelectivity(){
			//unused, but this makes most sense
			return type.isScalable() ? Selectivity.LINEAR : Selectivity.CONSTANT;
		}
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
			builder.append('(');
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
			builder.append(')');
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

		@Override
		public int size(){
			return Math.max(first.stream().mapToInt(EdgeGraphData::size).sum(), second.stream().mapToInt(EdgeGraphData::size).sum());
		}

		@Override
		public Type getSourceType(){
			return source.getTargetType();
		}

		@Override
		public Type getTargetType(){
			return target.getSourceType();
		}

		@Override
		public Selectivity getSourceSelectivity(){
			return source.getTargetSelectivity();
		}

		@Override
		public Selectivity getTargetSelectivity(){
			return target.getSourceSelectivity();
		}
	}
	
	private static class EndpointData extends EdgeGraphData{
		private final String name;
		private final SelectivityType selType;
		
		private EndpointData(String name, SelectivityType type){
			this.name = name;
			selType = type;
		}
		
		@Override
		public String toString(){
			return name;
		}
		
		@Override
		public boolean equals(Object other){
			return other == this;
		}

		@Override
		public int size(){
			return 0;
		}

		@Override
		public Type getSourceType(){
			return selType.getType();
		}

		@Override
		public Type getTargetType(){
			return selType.getType();
		}

		@Override
		public Selectivity getSourceSelectivity(){
			return selType.getSelectivity().getSelectivity();
		}

		@Override
		public Selectivity getTargetSelectivity(){
			return selType.getSelectivity().getSelectivity();
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

		@Override
		public int size(){
			return 1;
		}

		@Override
		public Type getSourceType(){
			return edge.getSource().getType();
		}

		@Override
		public Type getTargetType(){
			return edge.getTarget().getType();
		}

		@Override
		public Selectivity getSourceSelectivity(){
			return edge.getSource().getSelectivity().getSelectivity();
		}

		@Override
		public Selectivity getTargetSelectivity(){
			return edge.getTarget().getSelectivity().getSelectivity();
		}
	}
}