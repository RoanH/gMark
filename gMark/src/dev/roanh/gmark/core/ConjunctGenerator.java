package dev.roanh.gmark.core;

import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.SchemaGraph;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public enum ConjunctGenerator{
	CPQ,
	RPQ;

	public /*abstract*/ Conjunct generateConjunct(SelectivityGraph gSel, SchemaGraph gs, SelectivityType start, SelectivityType end){
		//TODO pass args
		return null;//TODO
	}
}
