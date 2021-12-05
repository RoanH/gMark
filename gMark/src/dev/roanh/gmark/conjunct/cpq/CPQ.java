package dev.roanh.gmark.conjunct.cpq;

public abstract interface CPQ{
	public static final CPQ IDENTITY = new CPQ(){
		@Override
		public String toString(){
			return "id";
		}
	};
}
