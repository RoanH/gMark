package dev.roanh.gmark.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Edge;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.core.graph.Type;
import dev.roanh.gmark.exception.GenerationException;

public class SelectivityGraph extends Graph<SelectivityType, SelectivityClass>{
	//TODO, move to constructor if not used anywhere else
	//gmark: graph.neighbors.size()
	private final RangeList<Map<SelectivityClass, GraphNode<SelectivityType, SelectivityClass>>> index;
	private final Schema schema;
	
	
	public SelectivityGraph(Schema schema, int maxLength){
		this.schema = schema;
		index = new RangeList<Map<SelectivityClass, GraphNode<SelectivityType, SelectivityClass>>>(schema.getTypeCount(), Util.selectivityMapSupplier());
		
		//compute distance between types (matrix)
		//compute graph from matrix
		
		DistanceMatrix matrix = computeDistanceMatrix(schema);
	
		//TODO in the original codebase this creates links (t1,s1) --s2-> (t2,s1*s2) which would imply this graph is labelled?
		//--> looks like these labels are used when drawing paths
		for(Type t1 : schema.getTypes()){
			for(Type t2 : schema.getTypes()){
				for(SelectivityClass sel1 : SelectivityClass.values()){
					for(SelectivityClass sel2 : SelectivityClass.values()){
						//if there exists a path of valid length from t1 to t2 with result selectivity class sel2
						if(matrix.get(t1, t2).getOrDefault(sel2, Integer.MAX_VALUE) <= maxLength){
							//add graph edge (t1, sel1) -> (t2, sel1 * sel2)
							resolve(t1, sel1).addUniqueEdgeTo(resolve(t2, sel1.conjunction(sel2)), sel2);//TODO see todo higher up about labelling
						}
					}
				}
			}
		}
	
	}
	
	private GraphNode<SelectivityType, SelectivityClass> resolve(Type type, SelectivityClass sel){
		return index.get(type).computeIfAbsent(sel, k->addUniqueNode(new SelectivityType(type, sel)));
	}
	
	public List<PathSegment> generateRandomPath(Selectivity selectivity, int length, double star) throws GenerationException{
		return generateRandomPath(selectivity, null, length, star);
	}
	
	//sel graph, matrix_of_paths, first_node, len, star, path (return value)
	//generate_random_path(g, pathmat, -1, nb_conjs, wconf.multiplicity, path);
	//TODO currentNode and currentSel form a valid SelecitivityType see if we can rewrite the logic to use that
	//TODO consider a dedicated return type
	//multiplicity - double / star fraction of stars 0~1
	public List<PathSegment> generateRandomPath(Selectivity selectivity, SelectivityType start, int length, double star) throws GenerationException{
		DistanceMatrix matrix = computeNumberOfPaths(selectivity, length);
		
		int currentNode = 0;
		SelectivityClass currentSel = SelectivityClass.EQUALS;
		
		if(start != null){
			currentNode = start.getTypeID();
		}else{
			int m = index.size();
			int paths = 0;
			for(int i = 0; i < m; i++){
				paths += matrix.get(length, i).getOrDefault(SelectivityClass.EQUALS, 0);
			}

			if(paths == 0){
				throw new GenerationException("Failed to generate a random path (no paths)");
			}
			
			int rnd = Util.uniformRandom(1, paths);
			int acc = 0;
			for(int i = 0; i < m; i++){
				acc += matrix.get(length, i).getOrDefault(SelectivityClass.EQUALS, 0);
				if(acc >= rnd){
					currentNode = i;
					break;
				}
			}
		}
		
		if(matrix.get(length, currentNode).getOrDefault(currentSel, 0) == 0){
			throw new GenerationException("Failed to generate a random path");
		}
		
		List<PathSegment> path = new ArrayList<PathSegment>(length);
		for(int i = length; i > 0; i--){
			
			//self loop test
			boolean test = false;
			for(GraphEdge<SelectivityType, SelectivityClass> edge : index.get(currentNode).get(SelectivityClass.EQUALS).getOutEdges()){
				SelectivityType target = edge.getTarget();
				if(target.getSelectivity() == SelectivityClass.EQUALS && target.getType().getID() == currentNode){
					test = true;
					break;
				}
			}
			
			if(test && Util.getRandom().nextDouble() <= star){
				path.add(PathSegment.of(schema, currentNode, SelectivityClass.EQUALS, currentNode, true));
			}else{
				int previousNode = currentNode;
				SelectivityClass sel = null;
				
				int rnd = Util.uniformRandom(1, matrix.get(i, currentNode).get(currentSel));
				int acc = 0;
				for(GraphEdge<SelectivityType, SelectivityClass> edge : index.get(currentNode).get(currentSel).getOutEdges()){
					SelectivityType target = edge.getTarget();
					acc += matrix.get(i - 1, target.getType()).getOrDefault(target.getSelectivity(), 0);
					if(acc >= rnd){
						currentNode = target.getTypeID();
						currentSel = target.getSelectivity();
						sel = edge.getData();
						break;
					}
				}
				path.add(PathSegment.of(schema, previousNode, sel, currentNode, false));
			}
		}
		
		return path;
	}
	
	public DistanceMatrix computeNumberOfPaths(Selectivity selectivity, int length){
		//TODO number of columns should be the type count, should find a better way to pass that maybe
		int types = index.size();
		DistanceMatrix matrix = new DistanceMatrix(length + 1, types);
		
		for(int j = 0; j < types; j++){
			for(SelectivityClass sel : SelectivityClass.values()){
				if(sel.getSelectivity() == selectivity){
					matrix.get(0, j).put(sel, 1);
				}
			}
		}

		for(int i = 1; i <= length; i++){
			for(int j = 0; j < types; j++){
				for(SelectivityClass sel : SelectivityClass.values()){
					GraphNode<SelectivityType, SelectivityClass> node = index.get(j).get(sel);
					if(node != null && node.getOutCount() > 0){
						for(GraphEdge<SelectivityType, SelectivityClass> edge : node.getOutEdges()){
							SelectivityType target = edge.getTarget();
							matrix.get(i, j).merge(
								sel,
								matrix.get(i - 1, target.getType()).getOrDefault(target.getSelectivity(), 0),
								Integer::sum
							);
						}
					}
				}
			}
		}
		
		return matrix;
	}
	
	/**
	 * Computes the shortest path between any two nodes in
	 * the schema graph. This is done using a modified version
	 * of the Floyd-Warshall algorithm (all-pairs shortest path).
	 * @param schema The schema to compute the distance matrix for.
	 * @return The distance matrix for the given graph schema.
	 */
	public static DistanceMatrix computeDistanceMatrix(Schema schema){
		int size = schema.getTypeCount();
		int logSize = (int)Math.log(size) + 4;//TODO why does this use a natural log?
		
		DistanceMatrix matrix = new DistanceMatrix(size);
		DistanceMatrix tmp = new DistanceMatrix(size);
		
		//add all length 1 paths
		for(Edge edge : schema.getEdges()){
			SelectivityClass sel = edge.getSelectivty();
			matrix.get(edge.getSourceType(), edge.getTargetType()).put(sel, 1);
			matrix.get(edge.getTargetType(), edge.getSourceType()).put(sel.negate(), 1);
		}
		
		for(int step = 0; step < logSize; step++){
			//make a copy of the matrix
			tmp.overwrite(matrix);
			
			//for each source type
			for(int i = 0; i < size; i++){
				//for each target type
				for(int j = 0; j < size; j++){
					//for pairs of connected edges
					for(int k = 0; k < size; k++){
						for(Entry<SelectivityClass, Integer> first : tmp.get(i, k).entrySet()){
							for(Entry<SelectivityClass, Integer> second : tmp.get(k, j).entrySet()){
								//update length to be the minimum of an existing length and the current path
								matrix.get(i, j).merge(
									first.getKey().conjunction(second.getKey()),//result of following these two edges after each other
									first.getValue() + second.getValue(),
									Math::min
								);
							}
						}
					}
				}
			}
		}
		
		return matrix;
	}
	
	public static final class DistanceMatrix extends RangeMatrix<Map<SelectivityClass, Integer>>{
		
		private DistanceMatrix(int size){
			this(size, size);
		}
		
		private DistanceMatrix(int rows, int cols){
			super(rows, cols, Util.selectivityMapSupplier());
		}
		
		private void overwrite(DistanceMatrix data){
			for(int i = 0; i < getRowCount(); i++){
				for(int j = 0; j < getColumnCount(); j++){
					Map<SelectivityClass, Integer> cell = get(i, j);
					Map<SelectivityClass, Integer> other = data.get(i, j);
					for(SelectivityClass sel : SelectivityClass.values()){
						Integer value = other.get(sel);
						if(value == null){
							cell.remove(sel);
						}else{
							cell.put(sel, value);
						}
					}
				}
			}
		}
	}
}
