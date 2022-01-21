package dev.roanh.gmark.core;

import dev.roanh.gmark.exception.GenerationException;
import dev.roanh.gmark.query.Conjunct;
import dev.roanh.gmark.util.SelectivityGraph;
import dev.roanh.gmark.util.SelectivityType;

public abstract interface ConjunctGenerator{

	public abstract Conjunct generateConjunct(SelectivityGraph gSel, SelectivityType start, SelectivityType end) throws GenerationException;
}
