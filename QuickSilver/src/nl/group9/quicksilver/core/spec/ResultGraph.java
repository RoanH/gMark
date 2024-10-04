package nl.group9.quicksilver.core.spec;

import java.util.List;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.SourceTargetPair;

public abstract interface ResultGraph{

	public abstract CardStat computeCardinality();

	public abstract List<SourceTargetPair> getSourceTargetPairs();
}
