package nl.group9.quicksilver.impl;

import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.spec.Estimator;

public class SimpleEstimator implements Estimator{

	@Override
	public void prepare(){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public CardStat estimate(PathQuery query){
		// TODO Auto-generated method stub
		return new CardStat(0, 0, 0);
	}
}
