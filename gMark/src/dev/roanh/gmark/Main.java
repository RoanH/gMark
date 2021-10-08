package dev.roanh.gmark;

import java.nio.file.Paths;

import dev.roanh.gmark.core.graph.Configuration;

public class Main{

	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
		
		
		
		System.out.println("Predicates:");
		config.getPredicates().forEach(System.out::println);
		
		System.out.println("\nTypes:");
		config.getTypes().forEach(System.out::println);
	}
}
