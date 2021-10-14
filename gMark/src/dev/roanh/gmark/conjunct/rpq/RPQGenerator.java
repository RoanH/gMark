package dev.roanh.gmark.conjunct.rpq;

import java.util.Random;

import dev.roanh.gmark.core.graph.Configuration;
import dev.roanh.gmark.util.Util;

public class RPQGenerator{
	private Random random = new Random();
	private RPQWorkload workloadConfig;
	private Configuration config;
	
	
	@Deprecated
	public RPQQuery generateQuery(){
		switch(Util.selectRandom(random, workloadConfig.getShapes())){
		case CHAIN:
			break;
		case CYCLE:
			//TODO
			break;
		case STAR:
			//TODO
			break;
		case STARCHAIN:
			//TODO
			break;
		default:
			//TODO
			break;
		}
		return null;
	}
	
	public RPQQuery generateChainQuery(){
		int conjunctCount = Util.uniformRandom(random, workloadConfig.getMinimumConjuncts(), workloadConfig.getMaximumConjuncts());
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		
		//TODO
		return null;
	}
}
