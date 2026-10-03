package dev.roanh.gmark.index.bisim;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import dev.roanh.cpqindex.Index;
import dev.roanh.cpqindex.LabelSequence;
import dev.roanh.cpqindex.LabelledPath;
import dev.roanh.cpqindex.Pair;

public class BisimBlockFull extends BisimBlock{
	/**
	 * A list of all label sequences that map to this block. This is
	 * the same set of label sequences as computed in the original
	 * paper on language aware indexing. This list may also be set
	 * to null if its computation is not explicitly requested by
	 * setting {@link Index#computeLabels} to true.
	 */
	private List<LabelSequence> labels;//opt
	/**
	 * Blocks from previous layers that were combined to form this layer.
	 */
	private List<BlockPair> combinations;//opt
	/**
	 * The block from the previous layer that the paths in this block were stored at.
	 * @see #paths
	 */
	private BisimBlockFull ancestor;//opt
	
	/**
	 * Constructs a new index block for the given diameter and with the given paths.
	 * @param k The diameter this block is for, corresponds to the index layer.
	 * @param slice The paths to store at this block.
	 */
	protected BisimBlockFull(int k, boolean computeLabels, List<LabelledPath> slice){//TODO compute boolean input is weird
		LabelledPath range = slice.get(0);
		super(range.getSegmentId(), k, slice.stream().map(LabelledPath::getPair).collect(Collectors.toList()));
		slice.forEach(s->s.setBlock(this));
		combinations = range.getSegments().stream().map(BlockPair::new).collect(Collectors.toList());
		
		if(computeLabels || combinations.isEmpty()){
			//we need labels to compute cores for k = 1 and in rare cases higher k where a k = 1 block did not get any higher k paths added
			labels = new ArrayList<LabelSequence>();
			labels.addAll(range.getLabels());
		}else{
			labels = null;
		}
		
		//we inherit all labels from the previous layer block the paths in this block are a subset of
		if(range.hasAncestor()){
			ancestor = range.getAncestor().getBlock();
			if(computeLabels){
				labels.addAll(ancestor.labels);
			}
		}else{
			ancestor = null;
		}
	}
	
//	/**
//	 * Reads a previously saved block from the given input stream.
//	 * @param in The stream to read from.
//	 * @param full True if extra information has to be read.
//	 * @param blockMap A map of already read blocks indexed by ID.
//	 * @throws IOException When an IOException occurs.
//	 */
//	private BisimBlock(DataInputStream in, boolean full, RangeList<Block> blockMap) throws IOException{
//		id = in.readInt();
//
//		int len = in.readInt();
//		paths = new ArrayList<Pair>(len);
//		for(int i = 0; i < len; i++){
//			paths.add(new Pair(in));
//		}
//
//		if(full){
//			k = in.readInt();
//
//			len = in.readInt();
//			labels = new ArrayList<LabelSequence>(len);
//			for(int i = 0; i < len; i++){
//				labels.add(new LabelSequence(in, predicates));
//			}
//
//			int anc = in.readInt();
//			ancestor = anc == -1 ? null : blockMap.get(anc);
//
//			len = in.readInt();
//			combinations = new ArrayList<BlockPair>(len);
//			for(int i = 0; i < len; i++){
//				combinations.add(new BlockPair(in, blockMap));
//			}
//		}else{
//			k = -1;
//			ancestor = null;
//			labels = null;
//			combinations = null;
//		}
//	}
	
//	/**
//	 * Writes this block to the given stream.
//	 * @param out The stream to write to.
//	 * @param full True to write extended information required
//	 *        to later compute cores.
//	 * @throws IOException When an IOException occurs.
//	 */
//	private final void write(DataOutputStream out, boolean full) throws IOException{
//		out.writeInt(id);
//
//		out.writeInt(paths.size());
//		for(Pair pair : paths){
//			pair.write(out);
//		}
//
//		if(full){
//			out.writeInt(k);
//
//			out.writeInt(labels == null ? 0 : labels.size());
//			if(labels != null){
//				for(LabelSequence seq : labels){
//					seq.write(out);
//				}
//			}
//
//			out.writeInt(ancestor == null ? -1 : ancestor.getId());
//
//			out.writeInt(combinations == null ? 0 : combinations.size());
//			if(combinations != null){
//				for(BlockPair pair : combinations){
//					pair.write(out);
//				}
//			}
//		}
//	}

	/**
	 * Gets the ID of this block. This is equal to
	 * the ID of the segments this block was built from.
	 * @return The ID of this block.
	 */
	public final int getId(){
		return id;
	}
	
	/**
	 * Gets the paths stored at this block.
	 * @return The paths for this block.
	 */
	public final List<Pair> getPaths(){
		return paths;
	}

	/**
	 * Gets the number of paths stored at this block.
	 * @return The number of paths for this block.
	 */
	public final int getPathCount(){
		return paths.size();
	}
	
	/**
	 * Gets the label sequences that map to this block.
	 * @return The label sequences that map to this block.
	 *         This value may be null unless label computation
	 *         was explicitly requested via {@link Index#computeLabels}.
	 */
	public final List<LabelSequence> getLabels(){
		return labels;
	}
	
	/**
	 * Checks if this block represents a loop, this means that
	 * all paths in this block have the same source and target vertex.
	 * @return True if this block represents a loop.
	 */
	public final boolean isLoop(){
		return paths.get(0).isLoop();
	}
	
	@Override
	public String toString(){
		StringBuilder builder = new StringBuilder();
		builder.append("Block[id=");
		builder.append(id);
		builder.append(",paths=");
		builder.append(paths);
		builder.append(",labels={");
		if(labels != null){
			for(LabelSequence seq : labels){
				builder.append(seq.toString());
				builder.append(",");
			}
			builder.delete(builder.length() - 1, builder.length());
		}
		builder.append("}]");
		return builder.toString();
	}
}
