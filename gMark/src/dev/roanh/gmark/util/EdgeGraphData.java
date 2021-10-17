package dev.roanh.gmark.util;

import java.util.Objects;

import dev.roanh.gmark.core.graph.Predicate;

public class EdgeGraphData{
	
	
	
	
	
	public static EdgeGraphData of(Predicate p){
		return new PredicateData(p);
	}
	
	public static EdgeGraphData ofEndpoint(){
		return new EndpointData();
	}
	
	private static class EndpointData extends EdgeGraphData{
		
		@Override
		public boolean equals(Object other){
			return other == this;
		}
	}
	
	public static class PredicateData extends EdgeGraphData{
		private Predicate predicate;
		
		private PredicateData(Predicate p){
			predicate = p;
		}
		
		@Override
		public int hashCode(){
			return Objects.hash(predicate);
		}
		
		@Override
		public boolean equals(Object other){
			return other instanceof PredicateData ? ((PredicateData)other).predicate == predicate : false;
		}
	}
}