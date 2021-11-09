package dev.roanh.gmark.query.shape;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.core.Workload;
import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.Util;

public class ChainGenerator{
	private Random random = new Random();
	
	
	
	public void generate(Configuration config, Workload workload){
		int conjunctNum = Util.uniformRandom(random, workload.getMinConjuncts(), workload.getMaxConjuncts());
		List<Conjunct> conjuncts = new ArrayList<Conjunct>(conjunctNum);
		
		SelectivityGraph g = new SelectivityGraph(config.getSchema(), workload.getMaxLength());
		Selectivity selectivity = Util.selectRandom(random, workload.getSelectivities());
		//TODO store selected selectivity info for the query we're working on
		
		//TODO draw path from selectivity graph
		
		for(int i = 0; i < conjunctNum; i++){
			
			
			
			//TODO generate conjunct with cpq/rpq generator
		}
		
		
		//TODO
		
		
		//TODO return query
	}
}
