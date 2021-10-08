package dev.roanh.gmark.core;

import java.util.function.Function;

import org.w3c.dom.Element;

import dev.roanh.gmark.core.dist.GaussianDistribution;
import dev.roanh.gmark.core.dist.UniformDistribution;
import dev.roanh.gmark.core.dist.ZipfianDistribution;

public enum DistributionType{
	UNIFORM("uniform", UniformDistribution::new),
	GAUSSIAN("gaussian", GaussianDistribution::new),
	ZIPFIAN("zipfian", ZipfianDistribution::new);
	
	private String name;
	private Function<Element, Distribution> constructor;

	private DistributionType(String name, Function<Element, Distribution> ctor){
		this.name = name;
		this.constructor = ctor;
	}
	
	public String getName(){
		return name;
	}
	
	public Distribution newInstane(Element data){
		return constructor.apply(data);
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public static final DistributionType getByName(String name){
		for(DistributionType type : values()){
			if(type.name.equalsIgnoreCase(name)){
				return type;
			}
		}
		return null;
	}
}
