package dev.roanh.gmark.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;
import java.util.stream.Collectors;

import dev.roanh.gmark.conjunct.cpq.CPQ;
import dev.roanh.gmark.conjunct.cpq.ConcatCPQ;
import dev.roanh.gmark.conjunct.cpq.EdgeCPQ;
import dev.roanh.gmark.conjunct.cpq.IntersectionCPQ;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.util.Graph.GraphEdge;

/**
 * Class to represent data stored in the edge graph.
 * These data object can represent path intersections,
 * identity intersections, normal edges and the edge
 * graph source and target node. These data objects
 * are closely linked to CPQ's.
 * @author Roan
 * @see EdgeGraph
 * @see CPQ
 */
public abstract class EdgeGraphData{

	/**
	 * Constructs a new edge graph data object for the given
	 * schema graph edge.
	 * @param edge The schema graph edge.
	 * @return The constructed edge graph predicate data object.
	 */
	public static PredicateData of(GraphEdge<SelectivityType, Predicate> edge){
		return new PredicateData(edge);
	}
	
	/**
	 * Constructs a new unique piece of edge graph data with
	 * a string as its descriptor. This is only used for the
	 * edge graph source and target node.
	 * @param name The name of this data object.
	 * @param type The selectivity type of this data object.
	 * @return The constructed edge graph endpoint data object.
	 */
	public static EndpointData of(String name, SelectivityType type){
		return new EndpointData(name, type);
	}
	
	/**
	 * Constructs a new piece of data for the intersection
	 * of the given two paths.
	 * @param source The shared source node (before paths).
	 * @param target The shared target node (after paths).
	 * @param first The first path.
	 * @param second The second path.
	 * @return The constructed edge graph identity data object.
	 */
	public static IntersectionData of(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first, Deque<EdgeGraphData> second){
		return new IntersectionData(source, target, first, second);
	}
	
	/**
	 * Constructs a new piece of data for the intersection
	 * of the given path with identity.
	 * @param source The shared source node (before path).
	 * @param target The shared target node (after path).
	 * @param first The path to intersect with identity.
	 * @return The constructed edge graph identity data object.
	 */
	public static IntersectionData of(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first){
		Deque<EdgeGraphData> second = new ArrayDeque<EdgeGraphData>();
		second.add(of(source.getTargetType()));
		return new IntersectionData(source, target, first, second);
	}
	
	/**
	 * Constructs a new piece of data for the intersection
	 * with identity of between two nodes of the given type.
	 * @param type The node type.
	 * @return The constructed edge graph identity data object.
	 */
	public static IdentityData of(Type type){
		return new IdentityData(type);
	}
	
	/**
	 * Gets the length of the shortest path
	 * through the CPQ represented by this
	 * edge graph data object.
	 * @return The length of the shortest
	 *         path through this object.
	 */
	public abstract int size();
	
	/**
	 * The type of the source node for
	 * the CPQ in this data object.
	 * @return The target type for this object.
	 */
	public abstract Type getSourceType();
	
	/**
	 * The type of the source node for
	 * the CPQ in this data object.
	 * @return The source type for this object.
	 */
	public abstract Type getTargetType();
	
	/**
	 * The selectivity of the source node for
	 * the CPQ in this data object.
	 * @return The source selectivity for this object.
	 */
	public abstract Selectivity getSourceSelectivity();

	/**
	 * The selectivity of the target node for
	 * the CPQ in this data object.
	 * @return The source selectivity for this object.
	 */
	public abstract Selectivity getTargetSelectivity();
	
	/**
	 * Converts this edge graph data object to an actual
	 * CPQ representing it.
	 * @return A CPQ for this edge graph data object.
	 * @see CPQ
	 */
	public abstract CPQ toCPQ();
	
	/**
	 * Data representing identity.
	 * @author Roan
	 */
	protected static class IdentityData extends EdgeGraphData{
		/**
		 * Type of the nodes intersected with identity.
		 */
		private Type type;
		
		/**
		 * Constructs new identity data for the given node type.
		 * @param type The node type.
		 */
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

		@Override
		public CPQ toCPQ(){
			return CPQ.IDENTITY;
		}
	}
	
	/**
	 * Metadata representing the intersection of two paths.
	 * @author Roan
	 */
	protected static class IntersectionData extends EdgeGraphData{
		/**
		 * The shared source of the paths in this intersection.
		 */
		private EdgeGraphData source;
		/**
		 * The shared target of the paths in this intersection.
		 */
		private EdgeGraphData target;
		/**
		 * The first path in this intersection.
		 */
		private Deque<EdgeGraphData> first;
		/**
		 * The second path in this intersection.
		 */
		private Deque<EdgeGraphData> second;
		
		/**
		 * Constructs new intersection data for the given source and target and
		 * two paths connecting them.
		 * @param source The shared source of the paths in the intersection.
		 * @param target The shared target of the paths in the intersection.
		 * @param first The first path in the intersection.
		 * @param second The second path in the intersection.
		 */
		private IntersectionData(EdgeGraphData source, EdgeGraphData target, Deque<EdgeGraphData> first, Deque<EdgeGraphData> second){
			this.source = source;
			this.target = target;
			this.first = first;
			this.second = second;
		}
		
		/**
		 * Gets the shared source of the paths in this intersection.
		 * @return The shared source of the paths in this intersection.
		 */
		public EdgeGraphData getSource(){
			return source;
		}
		
		/**
		 * Gets the shared target of the paths in this intersection.
		 * @return The shared target of the paths in this intersection.
		 */
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

		@Override
		public CPQ toCPQ(){
			return new IntersectionCPQ(
				first.size() == 1 ? first.getFirst().toCPQ() : new ConcatCPQ(first.stream().map(EdgeGraphData::toCPQ).collect(Collectors.toList())),
				second.size() == 1 ? second.getFirst().toCPQ() : new ConcatCPQ(second.stream().map(EdgeGraphData::toCPQ).collect(Collectors.toList()))
			);
		}
	}
	
	/**
	 * Represents metadata for the edge graph source
	 * and target nodes.
	 * @author Roan
	 */
	private static class EndpointData extends EdgeGraphData{
		/**
		 * The display name of this data object.
		 */
		private final String name;
		/**
		 * The selectivity type of this data object.
		 * This corresponds to the selectivity type
		 * of the source/target node from the schema graph.
		 */
		private final SelectivityType selType;
		
		/**
		 * Constructs new end point data with the given name
		 * and selectivity type.
		 * @param name The display name.
		 * @param type The selectivity type.
		 */
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

		@Override
		public CPQ toCPQ(){
			//always an empty start or end of a path
			return CPQ.IDENTITY;
		}
	}
	
	/**
	 * Represents data for a normal graph node
	 * constructed from a schema graph edge.
	 * @author Roan
	 * @see SchemaGraph
	 */
	private static class PredicateData extends EdgeGraphData{
		/**
		 * The schema graph edge represented by this data object.
		 */
		private GraphEdge<SelectivityType, Predicate> edge;
		
		/**
		 * Constructs a new predicate data object for the given
		 * schema graph edge.
		 * @param edge The schema graph edge.
		 */
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

		@Override
		public CPQ toCPQ(){
			return new EdgeCPQ(edge.getData());
		}
	}
}