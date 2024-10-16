package nl.group9.quicksilver.impl;

import nl.group9.quicksilver.core.spec.EvaluatorTest;

public class SimpleEvaluatorTest extends EvaluatorTest<SimpleGraph, SimpleGraph>{

	@Override
	public Provider getProvider(){
		return new Provider();
	}
}
