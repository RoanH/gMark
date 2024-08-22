package nl.group9.quicksilver.impl.data;

public record SourceTargetPair(int source, int target){

	@Override
	public String toString(){
		return "(" + source + ", " + target + ")";
	}
}
