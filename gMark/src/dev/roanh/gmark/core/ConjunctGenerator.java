package dev.roanh.gmark.core;

import dev.roanh.gmark.conjunct.cpq.GeneratorCPQ;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public enum ConjunctGenerator{
	CPQ{
		@Override
		public Conjunct generateConjunct(SelectivityGraph gSel, SchemaGraph gs, SelectivityType start, SelectivityType end){
			return GeneratorCPQ.generateInnerCPQ(gSel, gs, start, end, 4, 5);//TODO args hardcoded
		}
	},
	RPQ{
		@Override
		public Conjunct generateConjunct(SelectivityGraph gSel, SchemaGraph gs, SelectivityType start, SelectivityType end){
			// TODO Auto-generated method stub
			return null;
		}
	};

	public abstract Conjunct generateConjunct(SelectivityGraph gSel, SchemaGraph gs, SelectivityType start, SelectivityType end);//TODO pass args
}
