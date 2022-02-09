package dev.roanh.gmark.core;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.w3c.dom.Element;

import dev.roanh.gmark.ConfigParser;
import dev.roanh.gmark.core.graph.Schema;
import dev.roanh.gmark.util.IDable;

//TODO need to redo gmark formats a bit, probably just add another param after workload size with type="cpq", rpq, etc keep this class for shared info

//TODO do we want validation for the settings?
public abstract class Workload implements IDable{
	private int id;
	/**
	 * Number of queries in this workload.
	 */
	private int size;
	private int minConjuncts;
	private int maxConjuncts;
	private int minArity;
	private int maxArity;
	/**
	 * Probability that a conjunct has a Kleene star above it.
	 */
	private double starProbability;
	private Set<QueryShape> shapes = new HashSet<QueryShape>();//TODO assert this is not empty, general validation of everything really
	private Set<Selectivity> selectivities = new HashSet<Selectivity>();//TODO assert this is not empty -- note, should check on generation (possibly a validate method) since this can change now
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
	
	public ConjunctGenerator getConjunctGenerator(){
		return getType().getConjunctGenerator(this);
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
