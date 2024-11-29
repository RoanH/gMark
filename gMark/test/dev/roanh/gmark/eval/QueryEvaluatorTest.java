package dev.roanh.gmark.eval;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.data.CardStat;
import dev.roanh.gmark.data.SourceTargetPair;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;
import dev.roanh.gmark.util.Util;
import dev.roanh.gmark.util.graph.IntGraph;

@TestInstance(Lifecycle.PER_CLASS)
public class QueryEvaluatorTest{
	private static final Predicate l0 = new Predicate(0, "0");//a
	private static final Predicate l1 = new Predicate(1, "1");//b
	private static final Predicate l2 = new Predicate(2, "2");
	private static final Predicate l3 = new Predicate(3, "3");
	private static final Predicate l4 = new Predicate(4, "4");
	private static final Predicate l5 = new Predicate(5, "5");
	private static final Predicate l6 = new Predicate(6, "6");
	private static final Predicate l8 = new Predicate(8, "8");
	private static final Predicate l9 = new Predicate(9, "9");
	private static final Predicate l11 = new Predicate(11, "11");
	private static final Predicate l12 = new Predicate(12, "12");
	private static final Predicate l13 = new Predicate(13, "13");
	private static final Predicate l14 = new Predicate(14, "14");
	private static final Predicate l16 = new Predicate(16, "16");
	private static final Predicate l17 = new Predicate(17, "17");
	private static final Predicate l18 = new Predicate(18, "18");
	private static final Predicate l20 = new Predicate(20, "20");
	private static final Predicate l24 = new Predicate(24, "24");
	private static final Predicate l28 = new Predicate(28, "28");
	private static final Predicate l30 = new Predicate(30, "30");
	private DatabaseGraph example;
	private DatabaseGraph syn1;
	private DatabaseGraph real1;
	private DatabaseGraph real2;
	private DatabaseGraph real3;
	private DatabaseGraph real4;
	private DatabaseGraph real5;

	@BeforeAll
	public void loadData() throws IOException{
		example = getGraph();
		syn1 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/syn/1/graph.edge")));
		real1 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/real/1/graph.edge")));
		real2 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/real/2/graph.edge")));
		real3 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/real/3/graph.edge")));
		real4 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/real/4/graph.edge")));
		real5 = new DatabaseGraph(Util.readGraph(ClassLoader.getSystemResourceAsStream("workload/real/5/graph.edge")));
	}
	
	@Test
	public void query0(){
		ResultGraph result = evaluate(
			1,
			CPQ.intersect(
				CPQ.label(l1),
				CPQ.labels(
					l0,
					l1
				)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 2)
		));
		
		assertEquals(new CardStat(1, 1, 1), result.computeCardinality());
	}
	
	@Test
	public void query1(){
		ResultGraph result = evaluate(
			1,
			RPQ.kleene(
				RPQ.label(l0)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(1, 6),
			new SourceTargetPair(1, 8),
			new SourceTargetPair(1, 9),
			new SourceTargetPair(1, 10),
			new SourceTargetPair(1, 11),
			new SourceTargetPair(1, 12)
		));
		
		assertEquals(new CardStat(1, 8, 8), result.computeCardinality());
	}
	
	@Test
	public void query2(){
		ResultGraph result = evaluate(
			CPQ.id()
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(2, 2),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 12),
			new SourceTargetPair(13, 13)
		));
		
		assertEquals(new CardStat(14, 14, 14), result.computeCardinality());
	}
	
	@Test
	public void query3(){
		ResultGraph result = evaluate(
			RPQ.labels(
				l1.getInverse(),
				l0,
				l1
			)
		);
		
		System.out.println(result.getSourceTargetPairs());
		assertPaths(result, List.of(
			new SourceTargetPair(2, 2),
			new SourceTargetPair(2, 13),
			new SourceTargetPair(13, 2),
			new SourceTargetPair(13, 13)
		));
		
		assertEquals(new CardStat(2, 4, 2), result.computeCardinality());
	}
	
	@Test
	public void query4(){
		ResultGraph result = evaluate(
			RPQ.disjunct(
				RPQ.label(l0),
				RPQ.label(l1)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 2),
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 2),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(3, 6),
			new SourceTargetPair(3, 9),
			new SourceTargetPair(4, 2),
			new SourceTargetPair(4, 7),
			new SourceTargetPair(5, 2),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(6, 2),
			new SourceTargetPair(6, 8),
			new SourceTargetPair(6, 10),
			new SourceTargetPair(7, 2),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 10),
			new SourceTargetPair(8, 12),
			new SourceTargetPair(8, 13),
			new SourceTargetPair(9, 8),
			new SourceTargetPair(9, 13),
			new SourceTargetPair(10, 2),
			new SourceTargetPair(10, 11),
			new SourceTargetPair(11, 0),
			new SourceTargetPair(11, 2),
			new SourceTargetPair(12, 11),
			new SourceTargetPair(12, 13)
		));
		
		assertEquals(new CardStat(12, 27, 13), result.computeCardinality());
	}
	
	@Test
	public void query5(){
		ResultGraph result = evaluate(
			CPQ.intersect(
				CPQ.id(),
				CPQ.labels(
					l0,
					l0.getInverse()
				)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(1, 1),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 12)
		));
		
		assertEquals(new CardStat(11, 11, 11), result.computeCardinality());
	}
	
	@Test
	public void query6(){
		ResultGraph result = evaluate(
			CPQ.intersect(
				CPQ.labels(
					l0,
					l0,
					l0
				),
				CPQ.id()
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(7, 7)
		));
		
		assertEquals(new CardStat(3, 3, 3), result.computeCardinality());
	}
	
	@Test
	public void query7(){
		ResultGraph result = evaluate(
			CPQ.concat(
				CPQ.intersect(
					CPQ.id(),
					CPQ.labels(
						l0,
						l0,
						l0
					)
				),
				CPQ.label(l0)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(4, 7),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 10)
		));
		
		assertEquals(new CardStat(3, 4, 4), result.computeCardinality());
	}
	
	@Test
	public void query8(){
		ResultGraph result = evaluate(
			RPQ.label(l0),
			10
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(6, 10),
			new SourceTargetPair(7, 10)
		));
		
		assertEquals(new CardStat(2, 2, 1), result.computeCardinality());
	}
	
	@Test
	public void query9(){
		ResultGraph result = evaluate(
			3,
			RPQ.labels(
				l0,
				l0
			),
			8
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(3, 8)
		));
		
		assertEquals(new CardStat(1, 1, 1), result.computeCardinality());
	}
	
	@Test
	public void query10(){
		ResultGraph result = evaluate(
			3,
			RPQ.labels(
				l0,
				l1
			),
			8
		);
		
		assertPaths(result, List.of());
		
		assertEquals(new CardStat(0, 0, 0), result.computeCardinality());
	}
	
	@Test
	public void query11(){
		ResultGraph result = evaluate(
			RPQ.labels(
				l1,
				l1.getInverse()
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 3),
			new SourceTargetPair(0, 4),
			new SourceTargetPair(0, 5),
			new SourceTargetPair(0, 6),
			new SourceTargetPair(0, 7),
			new SourceTargetPair(0, 10),
			new SourceTargetPair(0, 11),
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(1, 4),
			new SourceTargetPair(1, 5),
			new SourceTargetPair(1, 6),
			new SourceTargetPair(1, 7),
			new SourceTargetPair(1, 10),
			new SourceTargetPair(1, 11),
			new SourceTargetPair(3, 0),
			new SourceTargetPair(3, 1),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(3, 4),
			new SourceTargetPair(3, 5),
			new SourceTargetPair(3, 6),
			new SourceTargetPair(3, 7),
			new SourceTargetPair(3, 10),
			new SourceTargetPair(3, 11),
			new SourceTargetPair(4, 0),
			new SourceTargetPair(4, 1),
			new SourceTargetPair(4, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(4, 5),
			new SourceTargetPair(4, 6),
			new SourceTargetPair(4, 7),
			new SourceTargetPair(4, 10),
			new SourceTargetPair(4, 11),
			new SourceTargetPair(5, 0),
			new SourceTargetPair(5, 1),
			new SourceTargetPair(5, 3),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(5, 6),
			new SourceTargetPair(5, 7),
			new SourceTargetPair(5, 10),
			new SourceTargetPair(5, 11),
			new SourceTargetPair(6, 0),
			new SourceTargetPair(6, 1),
			new SourceTargetPair(6, 3),
			new SourceTargetPair(6, 4),
			new SourceTargetPair(6, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(6, 7),
			new SourceTargetPair(6, 10),
			new SourceTargetPair(6, 11),
			new SourceTargetPair(7, 0),
			new SourceTargetPair(7, 1),
			new SourceTargetPair(7, 3),
			new SourceTargetPair(7, 4),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(7, 10),
			new SourceTargetPair(7, 11),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(8, 9),
			new SourceTargetPair(8, 12),
			new SourceTargetPair(9, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(9, 12),
			new SourceTargetPair(10, 0),
			new SourceTargetPair(10, 1),
			new SourceTargetPair(10, 3),
			new SourceTargetPair(10, 4),
			new SourceTargetPair(10, 5),
			new SourceTargetPair(10, 6),
			new SourceTargetPair(10, 7),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(10, 11),
			new SourceTargetPair(11, 0),
			new SourceTargetPair(11, 1),
			new SourceTargetPair(11, 3),
			new SourceTargetPair(11, 4),
			new SourceTargetPair(11, 5),
			new SourceTargetPair(11, 6),
			new SourceTargetPair(11, 7),
			new SourceTargetPair(11, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 8),
			new SourceTargetPair(12, 9),
			new SourceTargetPair(12, 12)
		));
		
		assertEquals(new CardStat(12, 90, 12), result.computeCardinality());
	}
	
	@Test
	public void query12(){
		ResultGraph result = evaluate(
			RPQ.kleene(
				RPQ.disjunct(
					RPQ.label(l1),
					RPQ.label(l1.getInverse())
				)
			)
		);
		
		assertPaths(result, List.of(
			new SourceTargetPair(0, 0),
			new SourceTargetPair(0, 1),
			new SourceTargetPair(0, 2),
			new SourceTargetPair(0, 3),
			new SourceTargetPair(0, 4),
			new SourceTargetPair(0, 5),
			new SourceTargetPair(0, 6),
			new SourceTargetPair(0, 7),
			new SourceTargetPair(0, 10),
			new SourceTargetPair(0, 11),
			new SourceTargetPair(1, 0),
			new SourceTargetPair(1, 1),
			new SourceTargetPair(1, 2),
			new SourceTargetPair(1, 3),
			new SourceTargetPair(1, 4),
			new SourceTargetPair(1, 5),
			new SourceTargetPair(1, 6),
			new SourceTargetPair(1, 7),
			new SourceTargetPair(1, 10),
			new SourceTargetPair(1, 11),
			new SourceTargetPair(2, 0),
			new SourceTargetPair(2, 1),
			new SourceTargetPair(2, 2),
			new SourceTargetPair(2, 3),
			new SourceTargetPair(2, 4),
			new SourceTargetPair(2, 5),
			new SourceTargetPair(2, 6),
			new SourceTargetPair(2, 7),
			new SourceTargetPair(2, 10),
			new SourceTargetPair(2, 11),
			new SourceTargetPair(3, 0),
			new SourceTargetPair(3, 1),
			new SourceTargetPair(3, 2),
			new SourceTargetPair(3, 3),
			new SourceTargetPair(3, 4),
			new SourceTargetPair(3, 5),
			new SourceTargetPair(3, 6),
			new SourceTargetPair(3, 7),
			new SourceTargetPair(3, 10),
			new SourceTargetPair(3, 11),
			new SourceTargetPair(4, 0),
			new SourceTargetPair(4, 1),
			new SourceTargetPair(4, 2),
			new SourceTargetPair(4, 3),
			new SourceTargetPair(4, 4),
			new SourceTargetPair(4, 5),
			new SourceTargetPair(4, 6),
			new SourceTargetPair(4, 7),
			new SourceTargetPair(4, 10),
			new SourceTargetPair(4, 11),
			new SourceTargetPair(5, 0),
			new SourceTargetPair(5, 1),
			new SourceTargetPair(5, 2),
			new SourceTargetPair(5, 3),
			new SourceTargetPair(5, 4),
			new SourceTargetPair(5, 5),
			new SourceTargetPair(5, 6),
			new SourceTargetPair(5, 7),
			new SourceTargetPair(5, 10),
			new SourceTargetPair(5, 11),
			new SourceTargetPair(6, 0),
			new SourceTargetPair(6, 1),
			new SourceTargetPair(6, 2),
			new SourceTargetPair(6, 3),
			new SourceTargetPair(6, 4),
			new SourceTargetPair(6, 5),
			new SourceTargetPair(6, 6),
			new SourceTargetPair(6, 7),
			new SourceTargetPair(6, 10),
			new SourceTargetPair(6, 11),
			new SourceTargetPair(7, 0),
			new SourceTargetPair(7, 1),
			new SourceTargetPair(7, 2),
			new SourceTargetPair(7, 3),
			new SourceTargetPair(7, 4),
			new SourceTargetPair(7, 5),
			new SourceTargetPair(7, 6),
			new SourceTargetPair(7, 7),
			new SourceTargetPair(7, 10),
			new SourceTargetPair(7, 11),
			new SourceTargetPair(8, 8),
			new SourceTargetPair(8, 9),
			new SourceTargetPair(8, 12),
			new SourceTargetPair(8, 13),
			new SourceTargetPair(9, 8),
			new SourceTargetPair(9, 9),
			new SourceTargetPair(9, 12),
			new SourceTargetPair(9, 13),
			new SourceTargetPair(10, 0),
			new SourceTargetPair(10, 1),
			new SourceTargetPair(10, 2),
			new SourceTargetPair(10, 3),
			new SourceTargetPair(10, 4),
			new SourceTargetPair(10, 5),
			new SourceTargetPair(10, 6),
			new SourceTargetPair(10, 7),
			new SourceTargetPair(10, 10),
			new SourceTargetPair(10, 11),
			new SourceTargetPair(11, 0),
			new SourceTargetPair(11, 1),
			new SourceTargetPair(11, 2),
			new SourceTargetPair(11, 3),
			new SourceTargetPair(11, 4),
			new SourceTargetPair(11, 5),
			new SourceTargetPair(11, 6),
			new SourceTargetPair(11, 7),
			new SourceTargetPair(11, 10),
			new SourceTargetPair(11, 11),
			new SourceTargetPair(12, 8),
			new SourceTargetPair(12, 9),
			new SourceTargetPair(12, 12),
			new SourceTargetPair(12, 13),
			new SourceTargetPair(13, 8),
			new SourceTargetPair(13, 9),
			new SourceTargetPair(13, 12),
			new SourceTargetPair(13, 13)
		));
		 
		assertEquals(new CardStat(14, 116, 14), result.computeCardinality());
	}
	
	@Test
	public void syn1rpq0(){
		assertEquals(new CardStat(2291, 118241, 861), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq1(){
		assertEquals(new CardStat(1, 170, 170), evaluate(syn1, 3, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq2(){
		assertEquals(new CardStat(1, 454, 454), evaluate(syn1, 12, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq3(){
		assertEquals(new CardStat(1, 157, 157), evaluate(syn1, 48, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq4(){
		assertEquals(new CardStat(275, 275, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 4));
	}
	
	@Test
	public void syn1rpq5(){
		assertEquals(new CardStat(73, 73, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 13));
	}
	
	@Test
	public void syn1rpq6(){
		assertEquals(new CardStat(185, 185, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 47));
	}
	
	@Test
	public void syn1rpq7(){
		assertEquals(new CardStat(2368, 25841, 270), evaluate(syn1, RPQ.labels(l0, l1, l2, l3)));
	}
	
	@Test
	public void syn1rpq8(){
		assertEquals(new CardStat(3841, 7067, 4000), evaluate(syn1, RPQ.label(l0)));
	}
	
	@Test
	public void syn1rpq9(){
		assertEquals(new CardStat(3841, 78917, 3841), evaluate(syn1, RPQ.labels(l0, l0.getInverse())));
	}
	
	@Test
	public void syn1rpq10(){
		assertEquals(new CardStat(3841, 367675, 4000), evaluate(syn1, RPQ.labels(l0, l0.getInverse(), l0)));
	}
	
	@Test
	public void syn1rpq11(){
		assertEquals(new CardStat(1, 57, 57), evaluate(syn1, 8, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq12(){
		assertEquals(new CardStat(1, 26, 26), evaluate(syn1, 14, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq13(){
		assertEquals(new CardStat(1, 262, 262), evaluate(syn1, 31, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq14(){
		assertEquals(new CardStat(1, 397, 397), evaluate(syn1, 90, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq15(){
		assertEquals(new CardStat(1, 362, 362), evaluate(syn1, 95, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq16(){
		assertEquals(new CardStat(1, 94, 94), evaluate(syn1, 132, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq17(){
		assertEquals(new CardStat(1, 220, 220), evaluate(syn1, 119, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1rpq18(){
		assertEquals(new CardStat(605, 605, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 49));
	}
	
	@Test
	public void syn1rpq19(){
		assertEquals(new CardStat(76, 76, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 96));
	}
	
	@Test
	public void syn1rpq20(){
		assertEquals(new CardStat(479, 479, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 133));
	}
	
	@Test
	public void syn1rpq21(){
		assertEquals(new CardStat(6234, 37006, 4491), evaluate(syn1, kleene(l0, l1)));
	}
	
	@Test
	public void syn1rpq22(){
		assertEquals(new CardStat(3841, 7067, 4000), evaluate(syn1, RPQ.kleene(RPQ.label(l0))));
	}
	
	@Test
	public void syn1rpq23(){
		assertEquals(new CardStat(2393, 10460, 491), evaluate(syn1, RPQ.kleene(RPQ.label(l1))));
	}
	
	@Test
	public void syn1rpq24(){
		assertEquals(new CardStat(944, 4065, 968), evaluate(syn1, RPQ.kleene(RPQ.label(l2))));
	}
	
	@Test
	public void syn1rpq25(){
		assertEquals(new CardStat(807, 1907, 1676), evaluate(syn1, RPQ.kleene(RPQ.label(l3))));
	}
	
	@Test
	public void syn1rpq26(){
		assertEquals(new CardStat(7178, 185374, 5459), evaluate(syn1, kleene(l0, l1, l2)));
	}

	@Test
	public void syn1cpq0(){
		assertEquals(new CardStat(4, 4, 4), evaluate(syn1, CPQ.intersect(CPQ.id(), CPQ.labels(l1.getInverse(), l3.getInverse(), l2.getInverse()))));
	}

	@Test
	public void syn1cpq1(){
		assertEquals(new CardStat(2393, 4374, 2699), evaluate(syn1, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.labels(l1, l1.getInverse())), CPQ.label(l0.getInverse()))));
	}

	@Test
	public void syn1cpq2(){
		assertEquals(new CardStat(2393, 10460, 491), evaluate(syn1, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.labels(l1, l1.getInverse())), CPQ.label(l1))));
	}

	@Test
	public void syn1cpq3(){
		assertEquals(new CardStat(1268, 7904, 886), evaluate(syn1, CPQ.labels(l3.getInverse(), l2.getInverse(), l2)));
	}

	@Test
	public void syn1cpq4(){
		assertEquals(new CardStat(280, 5728, 280), evaluate(syn1, CPQ.intersect(CPQ.labels(l2, l2.getInverse()), CPQ.labels(l1.getInverse(), l1))));
	}

	@Test
	public void syn1cpq5(){
		assertEquals(new CardStat(4, 4, 4), evaluate(syn1, CPQ.intersect(CPQ.id(), CPQ.labels(l1, l2, l3))));
	}

	@Test
	public void syn1cpq6(){
		assertEquals(new CardStat(2393, 2393, 2393), evaluate(syn1, CPQ.intersect(CPQ.id(), CPQ.labels(l1, l1.getInverse()))));
	}
	
	@Test
	public void syn1cpq7(){
		assertEquals(new CardStat(1, 157, 157), evaluate(syn1, 48, CPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1cpq8(){
		assertEquals(new CardStat(275, 275, 1), evaluate(syn1, CPQ.labels(l0, l1, l2, l2, l3), 4));
	}
	
	@Test
	public void real1rpq0(){
		assertEquals(new CardStat(823, 823, 282), evaluate(real1, RPQ.label(l1)));
	}
	
	@Test
	public void real1rpq1(){
		assertEquals(new CardStat(823, 823, 282), evaluate(real1, RPQ.kleene(RPQ.label(l1))));
	}
	
	@Test
	public void real1rpq2(){
		assertEquals(new CardStat(780, 1396, 143), evaluate(real1, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1rpq3(){
		assertEquals(new CardStat(3112, 4964, 1150), evaluate(real1, RPQ.label(l8)));
	}
	
	@Test
	public void real1rpq4(){
		assertEquals(new CardStat(3112, 7442, 1150), evaluate(real1, RPQ.kleene(RPQ.label(l8))));
	}
	
	@Test
	public void real1rpq5(){
		assertEquals(new CardStat(780, 1778, 151), evaluate(real1, RPQ.concat(RPQ.kleene(RPQ.label(l1)), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1rpq6(){
		assertEquals(new CardStat(535, 3240, 17), evaluate(real1, RPQ.labels(l1, l8, l9)));
	}
	
	@Test
	public void real1rpq7(){
		assertEquals(new CardStat(595, 3455, 17), evaluate(real1, RPQ.concat(RPQ.label(l1), RPQ.kleene(RPQ.label(l8)), RPQ.kleene(RPQ.label(l9)))));
	}
	
	@Test
	public void real1rpq8(){
		assertEquals(new CardStat(340, 340, 1), evaluate(real1, RPQ.labels(l1, l8, l9, l8)));
	}
	
	@Test
	public void real1rpq9(){
		assertEquals(new CardStat(340, 4420, 13), evaluate(real1, RPQ.labels(l1, l8, l9, l8, l17)));
	}
	
	@Test
	public void real1rpq10(){
		assertEquals(new CardStat(743, 9659, 13), evaluate(real1, RPQ.labels(l8, l9, l8, l17)));
	}
	
	@Test
	public void real1rpq11(){
		assertEquals(new CardStat(4, 52, 13), evaluate(real1, RPQ.labels(l9, l8, l17)));
	}
	
	@Test
	public void real1rpq12(){
		assertEquals(new CardStat(4, 52, 13), evaluate(real1, RPQ.concat(RPQ.label(l9), RPQ.kleene(RPQ.label(l8)), RPQ.label(l17))));
	}
	
	@Test
	public void real1rpq13(){
		assertEquals(new CardStat(4, 4, 1), evaluate(real1, RPQ.labels(l9, l8)));
	}
	
	@Test
	public void real1rpq14(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real1, 200, RPQ.label(l1)));
	}
	
	@Test
	public void real1rpq15(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real1, 242, RPQ.label(l1)));
	}
	
	@Test
	public void real1rpq16(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 200, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1rpq17(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 200, RPQ.concat(RPQ.label(l1), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1rpq18(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 227, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1rpq19(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 227, RPQ.concat(RPQ.kleene(RPQ.label(l1)), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1rpq20(){
		assertEquals(new CardStat(1, 3, 3), evaluate(real1, 227, kleene(l1, l8)));
	}
	
	@Test
	public void real1rpq21(){
		assertEquals(new CardStat(3935, 10010, 1328), evaluate(real1, kleene(l1, l8)));
	}
	
	@Test
	public void real1rpq22(){
		assertEquals(new CardStat(2114, 5065, 1340), evaluate(real1, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real1rpq23(){
		assertEquals(new CardStat(2164, 5167, 1389), evaluate(real1, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real1rpq24(){
		assertEquals(new CardStat(4122, 9020, 1391), evaluate(real1, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real1cpq0(){
		assertEquals(new CardStat(5, 5, 5), evaluate(real1, CPQ.intersect(CPQ.labels(l8, l12.getInverse()), CPQ.label(l30.getInverse()))));
	}
	
	@Test
	public void real1cpq1(){
		assertEquals(new CardStat(646, 46395, 259), evaluate(real1, CPQ.labels(l1, l8, l6.getInverse())));
	}
	
	@Test
	public void real1cpq2(){
		assertEquals(new CardStat(19, 19, 19), evaluate(real1, CPQ.intersect(CPQ.id(), CPQ.labels(l6, l12.getInverse()))));
	}
	
	@Test
	public void real1cpq3(){
		assertEquals(new CardStat(14, 14, 14), evaluate(real1, CPQ.intersect(CPQ.id(), CPQ.labels(l24.getInverse(), l24))));
	}
	
	@Test
	public void real1cpq4(){
		assertEquals(new CardStat(4, 8, 5), evaluate(real1, CPQ.concat(CPQ.intersect(l16, l9.getInverse()), CPQ.label(l9))));
	}
	
	@Test
	public void real1cpq5(){
		assertEquals(new CardStat(26, 161, 18), evaluate(real1, CPQ.labels(l17.getInverse(), l12.getInverse(), l12)));
	}
	
	@Test
	public void real1cpq6(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real1, CPQ.concat(CPQ.label(l13.getInverse()), CPQ.intersect(CPQ.id(), CPQ.labels(l28, l28.getInverse())))));
	}
	
	@Test
	public void real1cpq7(){
		assertEquals(new CardStat(7, 7, 7), evaluate(real1, CPQ.intersect(CPQ.id(), CPQ.labels(l8.getInverse(), l14.getInverse(), l6))));
	}
	
	@Test
	public void real1cpq8(){
		assertEquals(new CardStat(6, 6, 6), evaluate(real1, CPQ.intersect(CPQ.id(), CPQ.concat(CPQ.intersect(l18, l20), CPQ.label(l18.getInverse())))));
	}
	
	@Test
	public void real1cpq9(){
		assertEquals(new CardStat(26, 26, 26), evaluate(real1, CPQ.intersect(CPQ.id(), CPQ.concat(CPQ.intersect(l17, l17.getInverse()), CPQ.label(l17)))));
	}
	
	@Test
	public void real1cpq10(){
		assertEquals(new CardStat(4, 4, 4), evaluate(real1, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.labels(l30, l11.getInverse())), CPQ.label(l30))));
	}
	
	@Test
	public void real2rpq0(){
		assertEquals(new CardStat(3005, 17258, 1480), evaluate(real2, RPQ.label(l0)));
	}
	
	@Test
	public void real2rpq1(){
		assertEquals(new CardStat(3016, 21260, 2963), evaluate(real2, RPQ.label(l1)));
	}
	
	@Test
	public void real2rpq2(){
		assertEquals(new CardStat(2723, 216799, 2510), evaluate(real2, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2rpq3(){
		assertEquals(new CardStat(2886, 183286, 2547), evaluate(real2, RPQ.labels(l0, l1.getInverse())));
	}
	
	@Test
	public void real2rpq4(){
		assertEquals(new CardStat(2657, 835604, 2441), evaluate(real2, RPQ.labels(l0, l1, l2)));
	}
	
	@Test
	public void real2rpq5(){
		assertEquals(new CardStat(2214, 395003, 1693), evaluate(real2, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real2rpq6(){
		assertEquals(new CardStat(946, 31587, 1697), evaluate(real2, RPQ.labels(l2, l3)));
	}
	
	@Test
	public void real2rpq7(){
		assertEquals(new CardStat(2580, 1335113, 1684), evaluate(real2, RPQ.labels(l0, l1, l2, l3)));
	}
	
	@Test
	public void real2rpq8(){
		assertEquals(new CardStat(2639, 756006, 1528), evaluate(real2, RPQ.labels(l0, l1, l2.getInverse(), l3)));
	}
	
	@Test
	public void real2rpq9(){
		assertEquals(new CardStat(1, 33, 33), evaluate(real2, 8, RPQ.label(l0)));
	}
	
	@Test
	public void real2rpq10(){
		assertEquals(new CardStat(1, 12, 12), evaluate(real2, 12, RPQ.label(l0)));
	}
	
	@Test
	public void real2rpq11(){
		assertEquals(new CardStat(1, 439, 439), evaluate(real2, 8, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2rpq12(){
		assertEquals(new CardStat(1, 182, 182), evaluate(real2, 16, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2rpq13(){
		assertEquals(new CardStat(1, 203, 203), evaluate(real2, 32, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2rpq14(){
		assertEquals(new CardStat(1, 697, 697), evaluate(real2, 32, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real2rpq15(){
		assertEquals(new CardStat(1, 5, 5), evaluate(real2, 45, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real2cpq0(){
		assertEquals(new CardStat(2023, 98657, 1348), evaluate(real2, CPQ.concat(CPQ.label(l2.getInverse()), CPQ.intersect(l2.getInverse(), l2), CPQ.label(l2.getInverse()))));
	}
	
	@Test
	public void real2cpq1(){
		assertEquals(new CardStat(238, 1192, 345), evaluate(real2, CPQ.concat(CPQ.label(l3), CPQ.intersect(CPQ.id(), CPQ.labels(l0, l0)))));
	}
	
	@Test
	public void real2cpq2(){
		assertEquals(new CardStat(206, 28088, 1587), evaluate(real2, CPQ.concat(CPQ.intersect(l2, l1.getInverse()), CPQ.labels(l3, l3))));
	}
	
	@Test
	public void real2cpq3(){
		assertEquals(new CardStat(443, 24254, 548), evaluate(real2, CPQ.concat(CPQ.label(l0.getInverse()), CPQ.intersect(l0, l3.getInverse()), CPQ.label(l1))));
	}
	
	@Test
	public void real2cpq4(){
		assertEquals(new CardStat(60, 106, 54), evaluate(real2, CPQ.intersect(CPQ.concat(CPQ.label(l2), CPQ.intersect(l3, l1.getInverse())), CPQ.label(l1.getInverse()))));
	}
	
	@Test
	public void real2cpq5(){
		assertEquals(new CardStat(1101, 19187, 244), evaluate(real2, CPQ.concat(CPQ.label(l1), CPQ.intersect(l1.getInverse(), l3), CPQ.label(l3.getInverse()))));
	}
	
	@Test
	public void real2cpq6(){
		assertEquals(new CardStat(513, 2599, 393), evaluate(real2, CPQ.intersect(CPQ.labels(l2, l1.getInverse()), CPQ.labels(l2.getInverse(), l0))));
	}
	
	@Test
	public void real2cpq7(){
		assertEquals(new CardStat(269, 2151, 179), evaluate(real2, CPQ.concat(CPQ.label(l3), CPQ.intersect(CPQ.label(l1), CPQ.labels(l3.getInverse(), l2.getInverse())))));
	}
	
	@Test
	public void real2cpq8(){
		assertEquals(new CardStat(145, 5854, 1289), evaluate(real2, CPQ.concat(CPQ.intersect(CPQ.labels(l2.getInverse(), l3.getInverse()), CPQ.label(l2.getInverse())), CPQ.label(l1))));
	}
	
	@Test
	public void real2cpq9(){
		assertEquals(new CardStat(707, 707, 707), evaluate(real2, CPQ.intersect(CPQ.id(), CPQ.concat(CPQ.label(l2.getInverse()), CPQ.intersect(l2, l2.getInverse())))));
	}
	
	@Test
	public void real2cpq10(){
		assertEquals(new CardStat(741, 2294, 403), evaluate(real2, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.labels(l0.getInverse(), l0)), CPQ.label(l3.getInverse()))));
	}
	
	@Test
	public void real2cpq11(){
		assertEquals(new CardStat(372, 1631, 412), evaluate(real2, CPQ.concat(CPQ.label(l3), CPQ.intersect(CPQ.id(), CPQ.labels(l3, l3.getInverse())))));
	}
	
	@Test
	public void real3rpq0(){
		assertEquals(new CardStat(5482, 81754, 251), evaluate(real3, RPQ.concat(RPQ.label(l0), RPQ.kleene(RPQ.label(l3)), RPQ.kleene(RPQ.label(l2)))));
	}
	
	@Test
	public void real3rpq1(){
		assertEquals(new CardStat(6032, 60754, 34), evaluate(real3, RPQ.concat(RPQ.label(l0), RPQ.kleene(RPQ.label(l3)), RPQ.label(l2.getInverse()))));
	}
	
	@Test
	public void real3rpq2(){
		assertEquals(new CardStat(5482, 81702, 243), evaluate(real3, RPQ.labels(l0, l3, l2)));
	}
	
	@Test
	public void real3rpq3(){
		assertEquals(new CardStat(3, 12, 4), evaluate(real3, RPQ.labels(l0, l3, l2, l1, l4, l2)));
	}
	
	@Test
	public void real3rpq4(){
		assertEquals(new CardStat(1596, 6384, 4), evaluate(real3, RPQ.labels(l3, l2, l1, l4, l2)));
	}
	
	@Test
	public void real3rpq5(){
		assertEquals(new CardStat(45351, 118371, 18098), evaluate(real3, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real3rpq6(){
		assertEquals(new CardStat(58167, 841888, 36508), evaluate(real3, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real3rpq7(){
		assertEquals(new CardStat(58635, 940640, 36594), evaluate(real3, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real3cpq0(){
		assertEquals(new CardStat(19, 66, 44), evaluate(real3, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.label(l3)), CPQ.label(l0))));
	}
	
	@Test
	public void real3cpq1(){
		assertEquals(new CardStat(40, 58, 17), evaluate(real3, CPQ.concat(CPQ.label(l1.getInverse()), CPQ.intersect(CPQ.id(), CPQ.label(l3)))));
	}
	
	@Test
	public void real3cpq2(){
		assertEquals(new CardStat(21, 21, 21), evaluate(real3, CPQ.intersect(CPQ.labels(l3.getInverse(), l3), CPQ.intersect(CPQ.id(), CPQ.label(l2)))));
	}
	
	@Test
	public void real3cpq3(){
		assertEquals(new CardStat(3, 4, 4), evaluate(real3, CPQ.concat(CPQ.intersect(CPQ.labels(l1, l3.getInverse()), CPQ.label(l1)), CPQ.label(l3))));
	}
	
	@Test
	public void real3cpq4(){
		assertEquals(new CardStat(107, 365, 365), evaluate(real3, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.label(l2), CPQ.label(l2.getInverse())), CPQ.label(l2))));
	}
	
	@Test
	public void real3cpq5(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real3, CPQ.intersect(CPQ.concat(CPQ.intersect(l2.getInverse(), l3), CPQ.label(l2.getInverse())), CPQ.label(l3.getInverse()))));
	}
	
	@Test
	public void real3cpq6(){
		assertEquals(new CardStat(32, 139, 65), evaluate(real3, CPQ.concat(CPQ.concat(CPQ.label(l2), CPQ.intersect(l2, l3.getInverse()), CPQ.label(l3)))));
	}
	
	@Test
	public void real4rpq0(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real4, RPQ.labels(l0, l2, l3, l4, l5, l1)));
	}
	
	@Test
	public void real4rpq1(){
		assertEquals(new CardStat(19591, 49678, 8090), evaluate(real4, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real4rpq2(){
		assertEquals(new CardStat(20133, 52278, 8518), evaluate(real4, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real4rpq3(){
		assertEquals(new CardStat(59043, 99433, 30672), evaluate(real4, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real4cpq0(){
		assertEquals(new CardStat(56, 137, 137), evaluate(real4, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.label(l4)), CPQ.label(l4))));
	}
	
	@Test
	public void real4cpq1(){
		assertEquals(new CardStat(32, 32, 32), evaluate(real4, CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.label(l5)), CPQ.intersect(CPQ.id(), CPQ.label(l5.getInverse())))));
	}
	
	@Test
	public void real4cpq2(){
		assertEquals(new CardStat(51, 52, 32), evaluate(real4, CPQ.concat(CPQ.label(l5.getInverse()), CPQ.intersect(CPQ.id(), CPQ.label(l5.getInverse())))));
	}
	
	@Test
	public void real4cpq3(){
		assertEquals(new CardStat(2, 2, 2), evaluate(real4, CPQ.intersect(CPQ.id(), CPQ.concat(CPQ.intersect(CPQ.id(), CPQ.label(l3.getInverse())), CPQ.label(l3)))));
	}
	
	@Test
	public void real4cpq4(){
		assertEquals(new CardStat(78, 89, 16), evaluate(real4, CPQ.concat(CPQ.label(l4), CPQ.intersect(l4.getInverse(), l5))));
	}
	
	@Test
	public void real5rpq0(){
		assertEquals(new CardStat(23, 23, 1), evaluate(real5, RPQ.labels(l4, l3, l0, l2, l4, l1)));
	}
	
	@Test
	public void real5rpq1(){
		assertEquals(new CardStat(117599, 187448, 27431), evaluate(real5, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real5rpq2(){
		assertEquals(new CardStat(132218, 240562, 32690), evaluate(real5, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real5rpq3(){
		assertEquals(new CardStat(141187, 494784, 37545), evaluate(real5, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real5cpq0(){
		assertEquals(new CardStat(14, 14, 14), evaluate(real5, CPQ.intersect(CPQ.id(), CPQ.label(l3), CPQ.label(l3.getInverse()))));
	}
	
	@Test
	public void real5cpq1(){
		assertEquals(new CardStat(14, 14, 14), evaluate(real5, CPQ.intersect(CPQ.id(), CPQ.labels(l3, l3), CPQ.label(l3))));
	}
	
	@Test
	public void real5cpq2(){
		assertEquals(new CardStat(10, 14, 13), evaluate(real5, CPQ.intersect(CPQ.labels(l4.getInverse(), l3, l3.getInverse()), CPQ.label(l4))));
	}
	
	@Test
	public void real5cpq3(){
		assertEquals(new CardStat(17, 66, 20), evaluate(real5, CPQ.concat(CPQ.labels(l3.getInverse(), l4), CPQ.intersect(l4, l4.getInverse()))));
	}
	
	@Test
	public void real5cpq4(){
		assertEquals(new CardStat(166, 810, 6), evaluate(real5, CPQ.labels(l2, l4, l3.getInverse())));
	}
	
	private void assertPaths(ResultGraph result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().sorted().toList());
	}
	
	private CardStat evaluate(DatabaseGraph graph, QueryLanguageSyntax query){
		return evaluate(graph, PathQuery.of(query)).computeCardinality();
	}
	
	private CardStat evaluate(DatabaseGraph graph, int source, QueryLanguageSyntax query){
		return evaluate(graph, PathQuery.of(source, query)).computeCardinality();
	}
	
	private CardStat evaluate(DatabaseGraph graph, QueryLanguageSyntax query, int target){
		return evaluate(graph, PathQuery.of(query, target)).computeCardinality();
	}
	
	private ResultGraph evaluate(QueryLanguageSyntax query){
		return evaluate(PathQuery.of(query));
	}

	private ResultGraph evaluate(QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(query, target));
	}

	private ResultGraph evaluate(int source, QueryLanguageSyntax query){
		return evaluate(PathQuery.of(source, query));
	}

	private ResultGraph evaluate(int source, QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(source, query, target));
	}
	
	private ResultGraph evaluate(PathQuery query){
		return evaluate(example, query);
	}
	
	private ResultGraph evaluate(DatabaseGraph graph, PathQuery query){
		return new QueryEvaluator(graph).evaluate(query);
	}
	
	//see: https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsubsection.5.2.1.1
	private DatabaseGraph getGraph(){
		IntGraph graph = new IntGraph(14, 2);
		graph.addEdge(0, 2, 1);
		graph.addEdge(1, 0, 0);
		graph.addEdge(1, 2, 1);
		graph.addEdge(1, 3, 0);
		graph.addEdge(3, 2, 1);
		graph.addEdge(3, 6, 0);
		graph.addEdge(3, 9, 0);
		graph.addEdge(4, 2, 1);
		graph.addEdge(4, 7, 0);
		graph.addEdge(5, 4, 0);
		graph.addEdge(5, 2, 1);
		graph.addEdge(6, 8, 0);
		graph.addEdge(6, 10, 0);
		graph.addEdge(6, 2, 1);
		graph.addEdge(7, 5, 0);
		graph.addEdge(7, 2, 1);
		graph.addEdge(7, 10, 0);
		graph.addEdge(8, 12, 0);
		graph.addEdge(8, 13, 1);
		graph.addEdge(9, 8, 0);
		graph.addEdge(9, 13, 1);
		graph.addEdge(10, 2, 1);
		graph.addEdge(10, 11, 0);
		graph.addEdge(11, 0, 0);
		graph.addEdge(11, 2, 1);
		graph.addEdge(12, 11, 0);
		graph.addEdge(12, 13, 1);
		return new DatabaseGraph(graph);
	}
	
	private static RPQ kleene(Predicate... labels){
		return RPQ.kleene(RPQ.disjunct(Arrays.stream(labels).map(RPQ::label).toList()));
	}
}
