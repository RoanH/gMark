package nl.group9.quicksilver.core.data;

public record SourceTargetPair(int source, int target) implements Comparable<SourceTargetPair>{

	@Override
	public String toString(){
		return "(" + source + ", " + target + ")";
	}
	
	@Override
	public int compareTo(SourceTargetPair other){
		int cmp = Integer.compare(source, other.source());
		return cmp == 0 ? Integer.compare(target, other.target()) : cmp;
	}
}
