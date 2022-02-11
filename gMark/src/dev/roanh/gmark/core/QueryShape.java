package dev.roanh.gmark.core;

import java.util.function.Function;

import dev.roanh.gmark.query.shape.ChainGenerator;
import dev.roanh.gmark.query.shape.CycleGenerator;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.query.shape.StarChainGenerator;
import dev.roanh.gmark.query.shape.StarGenerator;

/**
 * Enum of different shapes the conjuncts
 * of a query can be arranged in.
 * @author Roan
 */
public enum QueryShape{
	/**
	 * A chain query shape where conjuncts are
	 * ordered sequentially. <br>For example:
	 * <code>(?x0,?x1),(?x1,?x2),(?x2,?x3)</code>.
	 */
	CHAIN("chain", ChainGenerator::new),
	/**
	 * A star query shape where conjuncts all share
	 * a single variable. <br>For example:
	 * <code>(?x0,?x1),(?x0,?x2),(?x0,?x3)</code>.
	 */
	STAR("star", StarGenerator::new),
	/**
	 * A cycle query shape where two chains of conjuncts
	 * exist that share the same first variable and last variable.
	 * <br>For example: <code>(?x0,?x1),(?x1,?x2),(?x0,?x3),(?x3,?x2)</code>.
	 */
	CYCLE("cycle", CycleGenerator::new),
	/**
	 * A query shape where a chain effectively has a star on both ends. <br>For example: 
	 * <code>(?x0,?x1),(?x0,?x2),(?x0,?x3),(?x3,?x4),(?x4,?x5),(?x4,?x6)</code>
	 */
	STARCHAIN("starchain", StarChainGenerator::new);
	
	/**
	 * The name of this query shape (as used in configuration files).
	 */
	private final String name;
	/**
	 * A function to create a new instance of a query shape generator.
	 */
	private Function<Workload, ShapeGenerator> ctor;
	
	/**
	 * Constructs a new query shape with the given name and constructor.
	 * @param name The name of this query shape (as used for configuration).
	 * @param ctor A function to create a new generator for this query shape
	 *        when given a workload instance.
	 */
	private QueryShape(String name, Function<Workload, ShapeGenerator> ctor){
		this.name = name;
		this.ctor = ctor;
	}

	/**
	 * Gets the name of this query shape. This is name is consistent
	 * with the name used for writing configuration files.
	 * @return The name of this query shape.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Gets a new generator to generate query with this shape
	 * for the given workload instance.
	 * @param workload The workload instance to generate queries for.
	 * @return A query shape generator for the given workload.
	 */
	public ShapeGenerator getQueryGenerator(Workload workload){
		return ctor.apply(workload);
	}
	
	/**
	 * Gets a query shape by its configuration name.
	 * @param name The name to find the query shape for.
	 * @return The query shape identified by the given
	 *         name or <code>null</code> if not query
	 *         shape was found for the given name.
	 */
	public static final QueryShape getByName(String name){
		for(QueryShape shape : values()){
			if(shape.name.equalsIgnoreCase(name)){
				return shape;
			}
		}
		return null;
	}
}
