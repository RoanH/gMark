package dev.roanh.gmark.core;

/**
 * Enum with all possible selectivity values. The
 * selectivity value shows how the number of results
 * returned by a query grows when the size of the graph grows.
 * @author Roan
 */
public enum Selectivity{
	/**
	 * This means a query returns about the same number
	 * of results regardless of how large the graph becomes.
	 * Typically this is seen for queries that select nodes
	 * that are present in a fixed quantity in the graph.
	 */
	CONSTANT("constant"),
	/**
	 * The most common selectivity value. This indicates
	 * queries that select more results when the graph grows
	 * in size and the number of results select grows in
	 * a linear fashion with the graph size.
	 */
	LINEAR("linear"),
	/**
	 * This selectivity value indicates that the number of
	 * results selected by a query grows much faster than
	 * the size of the graph itself. This selectivity value
	 * is typically seen for queries that perform a Cartesian
	 * product.
	 */
	QUADRATIC("quadratic");
	
	private final String name;
	
	private Selectivity(String name){
		this.name = name;
	}
	
	@Override
	public String toString(){
		return name;
	}
	
	public static Selectivity getByName(String name){
		for(Selectivity sel : values()){
			if(sel.name.equals(name)){
				return sel;
			}
		}
		return null;
	}
}
