package dev.roanh.gmark.conjunct.cpq;

import dev.roanh.gmark.output.SQL;

public abstract interface CPQ extends SQL{
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
}
