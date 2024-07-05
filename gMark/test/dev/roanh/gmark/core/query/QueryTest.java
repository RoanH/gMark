package dev.roanh.gmark.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;
import dev.roanh.gmark.query.conjunct.cpq.ConjunctCPQ;

public class QueryTest{
	private static final Variable v0 = new Variable(0);
	private static final Variable v1 = new Variable(1);

	@Test
	public void string0(){
		assertEquals(
			"(?x0,?x1) ← (?x0,(a◦a◦a)*,?x1),(?x0,b,?x1)",
			new Query(
				List.of(
					new ConjunctCPQ(CPQ.parse("a◦a◦a"), v0, v1, true),
					new ConjunctCPQ(CPQ.parse("b"), v0, v1, false)
				),
				List.of(v0, v1),
				Selectivity.LINEAR,
				QueryShape.CYCLE
			).toString()
		);
	}
}
