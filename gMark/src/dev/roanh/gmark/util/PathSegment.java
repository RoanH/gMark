package dev.roanh.gmark.util;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Type;

//gmark: tripple
public class PathSegment{
	private Type start;
	private SelectivityClass selectivity;
	private Type end;
	private boolean star;
	
	public PathSegment(Type start, SelectivityClass selecitvity, Type end, boolean star){
		this.start = start;
		this.selectivity = selecitvity;
		this.end = end;
		this.star = star;
	}
}
