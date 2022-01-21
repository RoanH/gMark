package dev.roanh.gmark.core;

import java.util.function.Function;

import dev.roanh.gmark.query.shape.ChainGenerator;
import dev.roanh.gmark.query.shape.ShapeGenerator;
import dev.roanh.gmark.query.shape.StarGenerator;

public enum QueryShape{
	CHAIN("chain", ChainGenerator::new),
	STAR("star", StarGenerator::new),//TODO
	CYCLE("cycle", null),//TODO
	STARCHAIN("starchain", null);
	
	private final String name;
	private Function<Workload, ShapeGenerator> ctor;
	
	private QueryShape(String name, Function<Workload, ShapeGenerator> ctor){
		this.name = name;
		this.ctor = ctor;
	}
	
	public ShapeGenerator getQueryGenerator(Workload workload){
		return ctor.apply(workload);
	}
	
	public static QueryShape getByName(String name){
		for(QueryShape shape : values()){
			if(shape.name.equals(name)){
				return shape;
			}
		}
		return null;
	}
}
