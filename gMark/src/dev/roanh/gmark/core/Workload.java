package dev.roanh.gmark.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.QueryGenerator;
import dev.roanh.gmark.query.QuerySet;
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
	/**
	 * The graph schema this workload should be generated on.
	 */
	private Schema schema;
	
	/**
	 * Constructs a new workload by parsing data used by
	 * all workload types from the given configuration node.
	 * @param elem The configuration node to parse.
	 * @param schema The schema for this workload.
	 */
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
	
	/**
	 * Gets the type of this workload.
	 * @return The type of this workload.
	 */
	public abstract WorkloadType getType();
	
	/**
	 * Gets the maximum length of paths needed in the selectivity
	 * graph to generate queries in accordance with this workload.
	 * @return The maximum required selectivity graph path length.
	 */
	public abstract int getMaxSelectivityGraphLength();
	
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

	/**
	 * Gets a generator for generating conjuncts
	 * according to this workload configuration.
	 * @return A worklaod conjunct generator.
	 */
	public abstract ConjunctGenerator getConjunctGenerator();
	
	/**
	 * Generates a single queries according to the parameters
	 * specified in this workload configuration.
	 * @return The generated query.
	 * @throws GenerationException When an exception
	 *         occurs when generating the query.
	 */
	public Query generateSingleQuery() throws GenerationException{
		return QueryGenerator.generateQuery(this);
	}
	
	/**
	 * Generates workload according to the parameters
	 * specified in this workload configuration.
	 * @return The generated queries.
	 * @throws GenerationException When an exception
	 *         occurs when generating queries.
	 */
	public QuerySet generateQueries() throws GenerationException{
		return QueryGenerator.generateQueries(this);
	}
	
	/**
	 * Gets the graph schema for this workload.
	 * @return The graph schema for this workload.
	 */
	public Schema getGraphSchema(){
		return schema;
	}
	
	/**
	 * Gets the probability that a conjunct has a
	 * Kleene star above it for this workload.
	 * @return The Kleene start probability.
	 */
	public double getStarProbability(){
		return starProbability;
	}
	
	/**
	 * Sets the minimum arity allowed for
	 * queries generated according to this workload.
	 * @param arity The new minimum query arity.
	 */
	public void setMinArity(int arity){
		minArity = arity;
	}
	
	/**
	 * Sets the maximum arity allowed for
	 * queries generated according to this workload.
	 * @param arity The new maximum query arity.
	 */
	public void setMaxArity(int arity){
		maxArity = arity;
	}
	
	/**
	 * Sets the minimum number of conjuncts allowed for
	 * queries generated according to this workload.
	 * @param conj The new minimum number of conjuncts.
	 */
	public void setMinConjuncts(int conj){
		minConjuncts = conj;
	}
	
	/**
	 * Sets the maximum number of conjuncts allowed for
	 * queries generated according to this workload.
	 * @param conj The new maximum number of conjuncts.
	 */
	public void setMaxConjuncts(int conj){
		maxConjuncts = conj;
	}
	
	/**
	 * Adds the given selectivities as valid selectivities for
	 * queries generated for this workload.
	 * @param selectivities The selectivities to add.
	 */
	public void addSelectivities(Selectivity... selectivities){
		Arrays.stream(selectivities).forEach(this.selectivities::add);
	}
	
	/**
	 * Removes the given selectivities as valid selectivities for
	 * queries generated for this workload.
	 * @param selectivities The selectivities to remove.
	 */
	public void removeSelectivities(Selectivity... selectivities){
		Arrays.stream(selectivities).forEach(this.selectivities::remove);
	}
	
	/**
	 * Adds the given shapes as valid shapes for
	 * queries generated for this workload.
	 * @param shapes The shapes to add.
	 */
	public void addShapes(QueryShape... shapes){
		Arrays.stream(shapes).forEach(this.shapes::add);
	}
	
	/**
	 * Removes the given shapes as valid shapes for
	 * queries generated for this workload.
	 * @param shapes The shapes to remove.
	 */
	public void removeShapes(QueryShape... shapes){
		Arrays.stream(shapes).forEach(this.shapes::remove);
	}
	
	/**
	 * Sets the total number of queries that should
	 * be generated for this workload.
	 * @param size The total number of queries for this workload.
	 */
	public void setSize(int size){
		this.size = size;
	}
	
	/**
	 * Gets the probability that a conjunct has
	 * a Kleene star above it (as a fraction).
	 * @param factor The conjunct Kleene star probability.
	 */
	public void setStarProbability(double factor){
		starProbability = factor;
	}
	
	/**
	 * Gets the total number of queries that should
	 * be generated for this workload.
	 * @return The total number of queries for this workload.
	 */
	public int getSize(){
		return size;
	}
	
	/**
	 * Gets the minimum arity allowed for queries
	 * in this workload.
	 * @return The minimum arity.
	 */
	public int getMinArity(){
		return minArity;
	}
	
	/**
	 * Gets the maximum arity allowed for queries
	 * in this workload.
	 * @return The maximum arity.
	 */
	public int getMaxArity(){
		return maxArity;
	}
	
	/**
	 * Gets the minimum number of conjuncts allowed
	 * for queries in this workload.
	 * @return The minimum number of conjuncts.
	 */
	public int getMinConjuncts(){
		return minConjuncts;
	}
	
	/**
	 * Gets the maximum number of conjuncts allowed
	 * for queries in this workload.
	 * @return The maximum number of conjuncts.
	 */
	public int getMaxConjuncts(){
		return maxConjuncts;
	}
	
	/**
	 * Gets all the shapes allowed for queries
	 * in this workload.
	 * @return All allowed query shapes.
	 */
	public Set<QueryShape> getShapes(){
		return shapes;
	}
	
	/**
	 * Gets all the selectivities allowed for
	 * queries in this workload.
	 * @return All allowed query selectivities.
	 */
	public Set<Selectivity> getSelectivities(){
		return selectivities;
	}
	
	@Override
	public int getID(){
		return id;
	}
}
