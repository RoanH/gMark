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
}
