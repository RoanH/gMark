package dev.roanh.gmark;

import java.nio.file.Paths;

import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.util.SchemaGraph;

public class Main{

	//General assumption that type and symbol IDs are consecutive in the sense that
	//for a total of 4 types these types have ID 0, 1, 2 and 3
	public static void main(String[] args){
		Configuration config = ConfigParser.parse(Paths.get("C:\\Users\\RoanH\\Downloads\\tmp\\gmark\\use-cases\\test.xml"));
		
		
		
		System.out.println("Predicates:");
		config.getPredicates().forEach(System.out::println);
		
		System.out.println("\nTypes:");
		config.getTypes().forEach(System.out::println);
		
		System.out.println("\nEdges:");
		config.getSchema().getEdges().forEach(System.out::println);
		
		System.out.println("\nGs nodes:");
		SchemaGraph gs = new SchemaGraph(config.getSchema());
		gs.printNodes();
		
		System.out.println("\nGs edges:");
		gs.printEdges();
		
		System.out.println("\nGs Graph edges");
		gs.getEdges().stream().forEach(e->{
			System.out.println(e.getSource() + " via " + e.getData().getAlias() + " via " + e.getTarget());
		});
		
		System.out.println("\nGs Graph nodes");
		gs.getNodes().stream().forEach(n->{
			System.out.println(n);
		});
	}
}
