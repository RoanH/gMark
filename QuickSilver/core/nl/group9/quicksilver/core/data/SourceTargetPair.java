package nl.group9.quicksilver.core.data;

/**
 * Combination of a source and target vertex implicitly representing
 * the existence of some path between these two vertices.
 * @author Roan
 * @param source The source vertex of the path.
 * @param target The target vertex of the path.
 */
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
