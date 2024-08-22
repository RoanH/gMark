package nl.group9.quicksilver.core.spec;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;

public abstract interface Estimator{

	public abstract void prepare();//TODO pass graph?
	
	public abstract CardStat estimate(PathQuery query);
}
