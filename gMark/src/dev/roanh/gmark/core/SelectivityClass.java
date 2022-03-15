package dev.roanh.gmark.core;

/**
 * Enum of all the selectivity classes. At its core this describes
 * the effect of an edge between two nodes of a given type. There
 * are two primary factors that influence the selectivity class:
 * <ol>
 * <li>The presence of the source and target node in the graph. That
 * is whether the nodes are present in a fixed quantity (constant) or
 * in a quantity that grows with the size of the graph (growing).</li>
 * <li>The incoming and outgoing distribution of the edge</li>
 * </ol>
 * @author Roan
 */
public enum SelectivityClass{
	/**
	 * <code>(1,=,1)</code> a class between constant
	 * nodes, also the only class associated with a
	 * query of constant selectivity.
	 */
	ONE_ONE("(1,=,1)", Selectivity.CONSTANT){
		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
				return other;
			case N_ONE:
				return ONE_ONE;
			case CROSS:
			case EQUALS:
			case GREATER:
			case LESS:
			case LESS_GREATER:
				return ONE_N;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(N,&gt;,1)</code> a class between a growing
	 * and a constant node, associated with a query
	 * of linear selectivity.
	 */
	N_ONE("(N,>,1)", Selectivity.LINEAR){
		@Override
		public SelectivityClass negate(){
			return ONE_N;
		}

		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
				return CROSS;
			case ONE_ONE:
			case N_ONE:
			case CROSS:
			case EQUALS:
			case GREATER:
			case LESS:
			case LESS_GREATER:
				return N_ONE;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(1,&lt;,N)</code> a class between a growing
	 * and a constant node, associated with a query
	 * of linear selectivity.
	 */
	ONE_N("(1,<,N)", Selectivity.LINEAR){
		@Override
		public SelectivityClass negate(){
			return N_ONE;
		}

		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
				return other;
			case N_ONE:
				return ONE_ONE;
			case CROSS:
			case EQUALS:
			case GREATER:
			case LESS:
			case LESS_GREATER:
				return ONE_N;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(N,=,N)</code> a class between growing
	 * nodes, associated with a query of linear selectivity.
	 */
	EQUALS("(N,=,N)", Selectivity.LINEAR){
		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			return other;
		}
	},
	/**
	 * <code>(N,&gt;,N)</code> a class between growing
	 * nodes, where the edge in distribution is zipfian.
	 * Associated with a query of linear selectivity.
	 */
	GREATER("(N,>,N)", Selectivity.LINEAR){
		@Override
		public SelectivityClass negate(){
			return LESS;
		}

		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
			case N_ONE:
			case CROSS:
			case GREATER:
				return other;
			case EQUALS:
				return GREATER;
			case LESS:
			case LESS_GREATER:
				return CROSS;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(N,&lt;,N)</code> a class between growing
	 * nodes, where the edge out distribution is zipfian.
	 * Associated with a query of linear selectivity.
	 */
	LESS("(N,<,N)", Selectivity.LINEAR){
		@Override
		public SelectivityClass negate(){
			return GREATER;
		}

		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
			case N_ONE:
			case LESS:
			case CROSS:
				return other;
			case EQUALS:
				return LESS;
			case GREATER:
			case LESS_GREATER:
				return LESS_GREATER;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(N,◇,N)</code> a class between growing
	 * nodes where the edge in and out distributions
	 * are both zipfian. Associated with a query of
	 * linear selectivity.
	 */
	LESS_GREATER("(N,◇,N)", Selectivity.LINEAR){
		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
			case N_ONE:
			case CROSS:
				return other;
			case EQUALS:
			case GREATER:
				return LESS_GREATER;
			case LESS:
			case LESS_GREATER:
				return CROSS;
			default:
				return null;
			}
		}
	},
	/**
	 * <code>(N,⨉,N)</code> a class between growing
	 * nodes, this includes for example the transitive
	 * closure. This class is always derived from the
	 * combination of the other selectivity classes.
	 * This is also the only class associated with a
	 * query of quadratic selectivity.
	 */
	CROSS("(N,⨉,N)", Selectivity.QUADRATIC){
		@Override
		public SelectivityClass conjunction(SelectivityClass other){
			switch(other){
			case ONE_N:
			case ONE_ONE:
			case N_ONE:
				return other;
			case CROSS:
			case EQUALS:
			case GREATER:
			case LESS:
			case LESS_GREATER:
				return CROSS;
			default:
				return null;
			}
		}
	};
	
	/**
	 * The display name of this selectivity class.
	 */
	private final String name;
	/**
	 * The selectivity associated with this selectivity class.
	 */
	private final Selectivity selectivity;
	
	/**
	 * Constructs a new selectivity class with the given
	 * display name and selectivity.
	 * @param name The display name of the selectivity class.
	 * @param selectivity The selectivity of the selectivity class.
	 */
	private SelectivityClass(String name, Selectivity selectivity){
		this.name = name;
		this.selectivity = selectivity;
	}
	
	/**
	 * Gets the estimated selectivity value of a query
	 * that belongs to this selectivity class.
	 * @return The selectivity of this selectivity class.
	 */
	public Selectivity getSelectivity(){
		return selectivity;
	}
	
	/**
	 * Gets the negation of this selectivity class. Meaning
	 * the effect of following an edge with this selectivity
	 * class in the reverse direction. For example the selectivity
	 * class <code>(N,&lt;,N)</code> becomes <code>(N,&gt;,N)</code>.
	 * @return The negation of this selectivity class.
	 */
	public SelectivityClass negate(){
		return this;
	}
	
	//TODO public abstract SelectivityClass disjunction(SelectivityClass other);
	
	/**
	 * Computes the selectivity class of the conjunction of this selectivity
	 * class and the given other selectivity class. This should be seen as
	 * first following an edge with this selectivity class and then following
	 * and edge with the given other selectivity. The conjunction of both
	 * selectivity classes is then the final selectivity class of the path.
	 * @param other The other selectivity class to compute the conjunction with.
	 * @return The selectivity class of the conjunction of this selectivity class
	 *         with the given selectivity class.
	 */
	public abstract SelectivityClass conjunction(SelectivityClass other);
	
	@Override
	public String toString(){
		return name;
	}
}
