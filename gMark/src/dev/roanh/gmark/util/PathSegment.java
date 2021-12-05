package dev.roanh.gmark.util;

import dev.roanh.gmark.core.SelectivityClass;
import dev.roanh.gmark.core.graph.Schema;

//gmark: tripple
public class PathSegment{
	private SelectivityType start;
	private SelectivityType end;
	private boolean star;
	
	public PathSegment(SelectivityType start, SelectivityType end, boolean star){
		this.start = start;
		this.end = end;
		this.star = star;
	}
	
	public SelectivityType getSource(){
		return start;
	}
	
	public SelectivityType getTarget(){
		return end;
	}
	
	public boolean hasStar(){
		return star;
	}
	
	public static final PathSegment of(Schema schema, int start, SelectivityClass sel, int end, boolean star){
		return new PathSegment(
			SelectivityType.of(schema.getType(start), SelectivityClass.EQUALS),//TODO this is always equals per generate_random_path_aux2 should look into this
			SelectivityType.of(schema.getType(end), sel),
			star
		);
	}
}
