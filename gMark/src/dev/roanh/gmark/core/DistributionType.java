package dev.roanh.gmark.core;

import java.util.function.Function;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.dist.GaussianDistribution;
import dev.roanh.gmark.core.dist.UniformDistribution;
import dev.roanh.gmark.core.dist.ZipfianDistribution;

/**
 * Enum of all probability distributions supported by gMark.
 * @author Roan
 */
public enum DistributionType{
	/**
	 * Indicates that the distribution is not known.
	 */
	UNDEFINED("undefined", e->Distribution.UNDEFINED),
	/**
	 * Represents a uniform distribution where
	 * number are uniformly distributed between
	 * some given minimum and maximum number.
	 */
	UNIFORM("uniform", UniformDistribution::new),
	/**
	 * Represents a gaussian (normal) distribution
	 * where numbers are normally distributed with
	 * some given mean and standard deviation.
	 */
	GAUSSIAN("gaussian", GaussianDistribution::new),
	/**
	 * Represents a zipfian distribution
	 * with some given alpha value.
	 */
	ZIPFIAN("zipfian", ZipfianDistribution::new);
	
	/**
	 * The name of this distribution (as used in config files).
	 */
	private String name;
	/**
	 * Function to construct a distribution from a configuration element.
	 */
	private Function<Element, Distribution> constructor;

	/**
	 * Constructs a new distribution type with the given name and constructor.
	 * @param name The configuration name of this distribution.
	 * @param ctor A function to construct this distribution from
	 *        a configuration file element.
	 */
	private DistributionType(String name, Function<Element, Distribution> ctor){
		this.name = name;
		this.constructor = ctor;
	}
	
	/**
	 * Gets the name of this distribution (as used in configuration files).
	 * @return The name of this distribution.
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Constructs a new instance of this distribution from the given
	 * configuration file element.
	 * @param data The configuration file element to parse.
	 * @return The newly constructed distribution.
	 */
	public Distribution newInstance(Element data){
		return constructor.apply(data);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	/**
	 * Resolves a distribution type by its configuration file name.
	 * @param name The configuration type name to search for.
	 * @return The distribution with the given configuration name.
	 */
	public static final DistributionType getByName(String name){
		for(DistributionType type : values()){
			if(type.name.equalsIgnoreCase(name)){
				return type;
			}
		}
		return null;
	}
}
