package dev.roanh.gmark.output;

import java.util.Locale;
import java.util.function.Function;

import dev.roanh.gmark.query.Query;

public enum ConcreteSyntax{
	SQL("sql", "sql", OutputSQL::toSQL);
	
	private final String id;
	private final String extension;
	private final Function<Query, String> convert;
	
	private ConcreteSyntax(String id, String extension, Function<Query, String> convert){
		this.id = id;
		this.extension = extension;
		this.convert = convert;
	}
	
	public String getName(){
		return id.toUpperCase(Locale.ROOT);
	}
	
	public String getID(){
		return id;
	}
	
	public String getExtension(){
		return extension;
	}
	
	public String convert(Query query){
		return convert.apply(query);
	}
	
	public static ConcreteSyntax fromName(String name){
		for(ConcreteSyntax syntax : values()){
			if(syntax.id.equalsIgnoreCase(name)){
				return syntax;
			}
		}
		return null;
	}
}
