package nl.group9.quicksilver.impl;

import nl.group9.quicksilver.core.spec.Evaluator;
import nl.group9.quicksilver.core.spec.EvaluatorTest;

public class SimpleEvaluatorTest extends EvaluatorTest<SimpleGraph>{

	@Override
	public Evaluator<SimpleGraph> getEvaluator(){
		return new SimpleEvaluator();
	}
}
