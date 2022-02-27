package dev.roanh.gmark.output;

import java.util.function.Function;

import dev.roanh.gmark.query.Query;

public enum ConcreteSyntax{
	SQL("sql", "sql", OutputSQL::toSQL);
	
	private final String name;
	private final String extension;
	private final Function<Query, String> convert;
	
	private ConcreteSyntax(String name, String extension, Function<Query, String> convert){
		this.name = name;
		this.extension = extension;
		this.convert = convert;
	}
	
	public String getName(){
		return name;
	}
	
	public String getExtension(){
		return extension;
	}
	
	public String convert(Query query){
		return convert.apply(query);
	}
	
	public static ConcreteSyntax fromName(String name){
		for(ConcreteSyntax syntax : values()){
			if(syntax.name.equalsIgnoreCase(name)){
				return syntax;
			}
		}
		return null;
	}
}
