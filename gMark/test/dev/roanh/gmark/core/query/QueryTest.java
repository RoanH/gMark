package dev.roanh.gmark.core.query;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.conjunct.cpq.CPQ;
import dev.roanh.gmark.conjunct.cpq.ConjunctCPQ;
import dev.roanh.gmark.core.QueryShape;
import dev.roanh.gmark.core.Selectivity;
import dev.roanh.gmark.query.Query;
import dev.roanh.gmark.query.Variable;

public class QueryTest{
	private static CPQ cpq0 = CPQ.parse("a◦a◦a");
	private static CPQ cpq1 = CPQ.parse("b");
	private static Variable v0 = new Variable(0);
	private static Variable v1 = new Variable(1);

	@Test
	public void string0(){
		assertEquals(
			"(?x0,?x1) ← (?x0,(a◦a◦a)*,?x1),(?x0,b,?x1)",
			new Query(
				List.of(
					new ConjunctCPQ(cpq0, v0, v1, true),
					new ConjunctCPQ(cpq1, v0, v1, false)
				),
				List.of(v0, v1),
				Selectivity.LINEAR,
				QueryShape.CYCLE
			).toString()
		);
	}
}
