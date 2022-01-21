package dev.roanh.gmark.query.shape;

import java.util.ArrayList;
import java.util.List;

import dev.roanh.gmark.core.ConjunctGenerator;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.Util;

/**
 * Base class for generators that generate a query made up
 * of a number conjuncts that are connected in a certain shape.
 * @author Roan
 */
public abstract class ShapeGenerator{
	/**
	 * The generator to use to populate the conjuncts.
	 */
	protected final ConjunctGenerator conjGen;
	/**
	 * The workload specification.
	 */
	protected final Workload workload;
	/**
	 * The selectivity graph for the graph schema.
	 */
	protected final SelectivityGraph gSel;
	
	/**
	 * Constructs a new shape generator for the given workload.
	 * @param workload The workload instance.
	 */
	protected ShapeGenerator(Workload workload){
		this.workload = workload;
		conjGen = workload.getConjunctGenerator();
		gSel = new SelectivityGraph(workload.getGraphSchema(), workload.getMaxSelectivityGraphLength());
	}
	
	/**
	 * Generates a new query with a specific shape following
	 * the provided workload specification.
	 * @return The generated query.
	 * @throws GenerationException When generating the query
	 *         failed for some reason.
	 */
	public abstract Query generateQuery() throws GenerationException;
	
	/**
	 * Generates a random number of conjuncts for the query
	 * within the limits set by the workload specification.
	 * @return The randomly selected number of conjuncts to generate.
	 */
	protected int randomConjunctNumber(){
		return Util.uniformRandom(workload.getMinConjuncts(), workload.getMaxConjuncts());
	}
	
	/**
	 * Generates a random selectivity for the query
	 * from the selectivities allowed by the workload specification.
	 * @return The randomly selected selectivity.
	 */
	protected Selectivity randomSelectivity(){
		return Util.selectRandom(workload.getSelectivities());
	}
	
	/**
	 * Generates am ordered list of variables
	 * numbered from 0 to n (exclusive).
	 * @param n The total number of variables to create.
	 * @return An ordered list of numbered variables.
	 */
	protected List<Variable> createVariables(int n){
		List<Variable> variables = new ArrayList<Variable>(n);
		for(int i = 0; i < n; i++){
			variables.add(new Variable(i));
		}
		return variables;
	}
	
	/**
	 * Generates a random arity for the query within
	 * the limits set by the workload specification.
	 * @param maxVariables The maximum number of variables
	 *        the generated arity can never be more than this.
	 * @return The randomly generated arity.
	 */
	protected int randomArity(int maxVariables){
		return Util.uniformRandom(workload.getMinArity(), Math.max(workload.getMaxArity(), maxVariables));
	}
}
