package dev.roanh.gmark.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.util.IDable;

/**
 * Represents a workload with all the important arguments required
 * for query generation. Conjuncts specific settings are managed
 * by subclasses of this class.
 * @author Roan
 * @see WorkloadType
 */
public abstract class Workload implements IDable{
	/**
	 * The unique ID of this workload.
	 */
	private final int id;
	/**
	 * Number of queries in this workload.
	 */
	private int size;
	/**
	 * The minimum number of conjuncts in queries
	 * generated in this workload.
	 */
	private int minConjuncts;
	/**
	 * The maximum number of conjuncts in queries
	 * generated in this workload.
	 */
	private int maxConjuncts;
	/**
	 * The minimum arity of queries generated
	 * in this workload (0 is allowed).
	 */
	private int minArity;
	/**
	 * Them maximum arity of queries generated
	 * in this workload.
	 */
	private int maxArity;
	/**
	 * Probability that a conjunct has a Kleene star above it.
	 */
	private double starProbability;
	/**
	 * Set of valid queries shapes for queries
	 * generated in this workload.
	 */
	private Set<QueryShape> shapes = new HashSet<QueryShape>();
	/**
	 * Set of valid query selectivities for queries
	 * generated in this workload.
	 */
	private Set<Selectivity> selectivities = new HashSet<Selectivity>();
	private Schema schema;
	
	protected Workload(Element elem, Schema schema){
		this.schema = schema;
		id = Integer.parseInt(elem.getAttribute("id"));
		size = Integer.parseInt(elem.getAttribute("size"));
		
		//conjuncts
		Element conj = ConfigParser.getElement(ConfigParser.getElement(elem, "size"), "conjuncts");
		minConjuncts = Integer.parseInt(conj.getAttribute("min"));
		maxConjuncts = Integer.parseInt(conj.getAttribute("max"));
		
		//star
		starProbability = Double.parseDouble(ConfigParser.getElement(elem, "multiplicity").getAttribute("star"));
		
		//arity
		Element arity = ConfigParser.getElement(elem, "arity");
		minArity = Integer.parseInt(arity.getAttribute("min"));
		maxArity = Integer.parseInt(arity.getAttribute("max"));
		
		//selectivities
		ConfigParser.forEach(ConfigParser.getElement(elem, "selectivity").getAttributes(), (key, value)->{
			if(value.equals("1")){
				selectivities.add(Selectivity.getByName(key));
			}
		});
		
		//shapes
		ConfigParser.forEach(ConfigParser.getElement(elem, "type").getAttributes(), (key, value)->{
			if(value.equals("1")){
				shapes.add(QueryShape.getByName(key));
			}
		});
	}
	
	public abstract WorkloadType getType();
	
	public abstract int getMaxSelectivityGraphLength();//TODO reconsider
	
	/**
	 * Validates this workload by checking that all of the arguments
	 * are valid and usable for query generation. Subclasses should
	 * override this method to validate their own arguments.
	 * @throws IllegalStateException When it is determined that the
	 *         workload is not valid.
	 */
	public void validate() throws IllegalStateException{
		if(minArity < 0){
			throw new IllegalStateException("Minimum arity cannot be negative.");
		}else if(minArity > maxArity){
			throw new IllegalStateException("Minimum arity cannot be greater than the maximum arity.");
		}else if(minConjuncts < 1){
			throw new IllegalStateException("Minimum number of conjuncts cannot be less than 1.");
		}else if(minConjuncts > maxConjuncts){
			throw new IllegalStateException("Minimum number of conjuncts cannot be greater than the maximum number of conjuncts.");
		}else if(shapes.isEmpty()){
			throw new IllegalStateException("Workload cannot have no allowed query shapes.");
		}else if(selectivities.isEmpty()){
			throw new IllegalStateException("Workload cannot have no allowed selectivities.");
		}else if(0.0D > starProbability || 1.0D < starProbability){
			throw new IllegalStateException("Star probability has to be in range 0.0~1.0.");
		}
	}

	public ConjunctGenerator getConjunctGenerator(){
		return getType().getConjunctGenerator(this);
	}
	
	public Query generateSingleQuery() throws GenerationException{
		return QueryGenerator.generateQuery(this);
	}
	
	public List<Query> generateQueries() throws GenerationException{
		return QueryGenerator.generateQueries(this);
	}
	
	public Schema getGraphSchema(){
		return schema;
	}
	
	public double getStarProbability(){
		return starProbability;
	}
	
	public void setMinArity(int arity){
		minArity = arity;
	}
	
	public void setMaxArity(int arity){
		maxArity = arity;
	}
	
	public void setMinConjuncts(int conj){
		minConjuncts = conj;
	}
	
	public void setMaxConjuncts(int conj){
		maxConjuncts = conj;
	}
	
	public void addSelectivities(Selectivity... selectivities){
		Arrays.stream(selectivities).forEach(this.selectivities::add);
	}
	
	public void removeSelectivities(Selectivity... selectivities){
		Arrays.stream(selectivities).forEach(this.selectivities::remove);
	}
	
	public void addShapes(QueryShape... shapes){
		Arrays.stream(shapes).forEach(this.shapes::add);
	}
	
	public void removeShapes(QueryShape... shapes){
		Arrays.stream(shapes).forEach(this.shapes::remove);
	}
	
	public void setSize(int size){
		this.size = size;
	}
	
	public void setStarProbability(double factor){
		starProbability = factor;
	}
	
	public int getSize(){
		return size;
	}
	
	public int getMinArity(){
		return minArity;
	}
	
	public int getMaxArity(){
		return maxArity;
	}
	
	public int getMinConjuncts(){
		return minConjuncts;
	}
	
	public int getMaxConjuncts(){
		return maxConjuncts;
	}
	
	public Set<QueryShape> getShapes(){
		return shapes;
	}
	
	public Set<Selectivity> getSelectivities(){
		return selectivities;
	}
	
	@Override
	public int getID(){
		return id;
	}
}
