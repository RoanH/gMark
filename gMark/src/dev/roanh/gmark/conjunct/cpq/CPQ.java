package dev.roanh.gmark.conjunct.cpq;

public abstract interface CPQ{
	public static final CPQ IDENTITY = new CPQ(){
		@Override
		public String toString(){
			return "id";
		}

		@Override
		public String toSQL(){
			throw new IllegalStateException("Indentity to SQL not supported (and never generated).");
		}
	};
	
	//has to be wrapped in () at the outer level and select a src-trg pair table
	public abstract String toSQL();//TODO separate interface
	
	public default boolean isIntersection(){
		return this instanceof IntersectionCPQ;
	}
	
	public default boolean isConcatenation(){
		return this instanceof ConcatCPQ;
	}
	
	public default boolean isLabel(){
		return this instanceof EdgeCPQ;
	}
	
	public default IntersectionCPQ asIntersection(){
		return isIntersection() ? (IntersectionCPQ)this : null;
	}
	
	public default ConcatCPQ asConcatenation(){
		return isConcatenation() ? (ConcatCPQ)this : null;
	}
	
	public default EdgeCPQ asLabel(){
		return isLabel() ? (EdgeCPQ)this : null;
	}
}
