package dev.roanh.gmark.index.cpqnative;

import java.util.List;
import java.util.Set;

import dev.roanh.cpqindex.CanonForm.CoreHash;
import dev.roanh.cpqindex.Index;
import dev.roanh.gmark.index.bisim.BisimBlock;
import dev.roanh.gmark.lang.cpq.CPQ;

public class CPQNativeBlock{
	private final BisimBlock base;
	/**
	 * Explicit core information for cores stored in this block.
	 * This list is never restored for an index that was saved and
	 * read back and is also cleared after core computation unless
	 * saving labels is enabled.
	 * @see Index#computeLabels
	 */
	private List<CPQ> cores;
	/**
	 * Hashes for the cores in this index block. Explicit forms are
	 * optionally stored in {@link #cores}.
	 */
	private Set<CoreHash> canonCores;
	
	
	

}
