package nl.group9.quicksilver.core.spec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;

import dev.roanh.gmark.core.graph.Predicate;
import dev.roanh.gmark.lang.QueryLanguageSyntax;
import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.rpq.RPQ;

import nl.group9.quicksilver.core.GraphUtil;
import nl.group9.quicksilver.core.data.CardStat;
import nl.group9.quicksilver.core.data.PathQuery;
import nl.group9.quicksilver.core.data.SourceTargetPair;

@TestInstance(Lifecycle.PER_CLASS)
public abstract class EvaluatorTest<G extends DatabaseGraph, R extends ResultGraph>{
	private static final Predicate l0 = new Predicate(0, "0");//a
	private static final Predicate l1 = new Predicate(1, "1");//b
	private static final Predicate l2 = new Predicate(2, "2");
	private static final Predicate l3 = new Predicate(3, "3");
	private static final Predicate l4 = new Predicate(4, "4");
	private static final Predicate l5 = new Predicate(5, "5");
	private static final Predicate l8 = new Predicate(8, "8");
	private static final Predicate l9 = new Predicate(9, "9");
	private static final Predicate l17 = new Predicate(17, "17");
	private G example;
	private G syn1;
	private G real1;
	private G real2;
	private G real3;
	private G real4;
	private G real5;
	
	public abstract EvaluatorProvider<G, R> getProvider();

	@BeforeAll
	public void loadData() throws IOException{
		example = getGraph();
		syn1 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "syn", "1", "graph.edge"));
		real1 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "real", "1", "graph.edge"));
		real2 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "real", "2", "graph.edge"));
		real3 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "real", "3", "graph.edge"));
		real4 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "real", "4", "graph.edge"));
		real5 = GraphUtil.readGraph(getProvider(), Paths.get("workload", "real", "5", "graph.edge"));
	}
	
	@Test
	public void query0(){
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
			RPQ.labels(
				l1.getInverse(),
				l0,
				l1
			)
		);
		
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
		R result = evaluate(
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
	public void syn1q0(){
		assertEquals(new CardStat(2291, 118241, 861), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q1(){
		assertEquals(new CardStat(1, 170, 170), evaluate(syn1, 3, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q2(){
		assertEquals(new CardStat(1, 454, 454), evaluate(syn1, 12, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q3(){
		assertEquals(new CardStat(1, 157, 157), evaluate(syn1, 48, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q4(){
		assertEquals(new CardStat(275, 275, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 4));
	}
	
	@Test
	public void syn1q5(){
		assertEquals(new CardStat(73, 73, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 13));
	}
	
	@Test
	public void syn1q6(){
		assertEquals(new CardStat(185, 185, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 47));
	}
	
	@Test
	public void syn1q7(){
		assertEquals(new CardStat(2368, 25841, 270), evaluate(syn1, RPQ.labels(l0, l1, l2, l3)));
	}
	
	@Test
	public void syn1q8(){
		assertEquals(new CardStat(3841, 7067, 4000), evaluate(syn1, RPQ.label(l0)));
	}
	
	@Test
	public void syn1q9(){
		assertEquals(new CardStat(3841, 78917, 3841), evaluate(syn1, RPQ.labels(l0, l0.getInverse())));
	}
	
	@Test
	public void syn1q10(){
		assertEquals(new CardStat(3841, 367675, 4000), evaluate(syn1, RPQ.labels(l0, l0.getInverse(), l0)));
	}
	
	@Test
	public void syn1q11(){
		assertEquals(new CardStat(1, 57, 57), evaluate(syn1, 8, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q12(){
		assertEquals(new CardStat(1, 26, 26), evaluate(syn1, 14, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q13(){
		assertEquals(new CardStat(1, 262, 262), evaluate(syn1, 31, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q14(){
		assertEquals(new CardStat(1, 397, 397), evaluate(syn1, 90, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q15(){
		assertEquals(new CardStat(1, 362, 362), evaluate(syn1, 95, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q16(){
		assertEquals(new CardStat(1, 94, 94), evaluate(syn1, 132, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q17(){
		assertEquals(new CardStat(1, 220, 220), evaluate(syn1, 119, RPQ.labels(l0, l1, l2, l2, l3)));
	}
	
	@Test
	public void syn1q18(){
		assertEquals(new CardStat(605, 605, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 49));
	}
	
	@Test
	public void syn1q19(){
		assertEquals(new CardStat(76, 76, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 96));
	}
	
	@Test
	public void syn1q20(){
		assertEquals(new CardStat(479, 479, 1), evaluate(syn1, RPQ.labels(l0, l1, l2, l2, l3), 133));
	}
	
	@Test
	public void syn1q21(){
		assertEquals(new CardStat(6234, 37006, 4491), evaluate(syn1, kleene(l0, l1)));
	}
	
	@Test
	public void syn1q22(){
		assertEquals(new CardStat(3841, 7067, 4000), evaluate(syn1, RPQ.kleene(RPQ.label(l0))));
	}
	
	@Test
	public void syn1q23(){
		assertEquals(new CardStat(2393, 10460, 491), evaluate(syn1, RPQ.kleene(RPQ.label(l1))));
	}
	
	@Test
	public void syn1q24(){
		assertEquals(new CardStat(944, 4065, 968), evaluate(syn1, RPQ.kleene(RPQ.label(l2))));
	}
	
	@Test
	public void syn1q25(){
		assertEquals(new CardStat(807, 1907, 1676), evaluate(syn1, RPQ.kleene(RPQ.label(l3))));
	}
	
	@Test
	public void syn1q26(){
		assertEquals(new CardStat(7178, 185374, 5459), evaluate(syn1, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real1q0(){
		assertEquals(new CardStat(823, 823, 282), evaluate(real1, RPQ.label(l1)));
	}
	
	@Test
	public void real1q1(){
		assertEquals(new CardStat(823, 823, 282), evaluate(real1, RPQ.kleene(RPQ.label(l1))));
	}
	
	@Test
	public void real1q2(){
		assertEquals(new CardStat(780, 1396, 143), evaluate(real1, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1q3(){
		assertEquals(new CardStat(3112, 4964, 1150), evaluate(real1, RPQ.label(l8)));
	}
	
	@Test
	public void real1q4(){
		assertEquals(new CardStat(3112, 7442, 1150), evaluate(real1, RPQ.kleene(RPQ.label(l8))));
	}
	
	@Test
	public void real1q5(){
		assertEquals(new CardStat(780, 1778, 151), evaluate(real1, RPQ.concat(RPQ.kleene(RPQ.label(l1)), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1q6(){
		assertEquals(new CardStat(535, 3240, 17), evaluate(real1, RPQ.labels(l1, l8, l9)));
	}
	
	@Test
	public void real1q7(){
		assertEquals(new CardStat(595, 3455, 17), evaluate(real1, RPQ.concat(RPQ.label(l1), RPQ.kleene(RPQ.label(l8)), RPQ.kleene(RPQ.label(l9)))));
	}
	
	@Test
	public void real1q8(){
		assertEquals(new CardStat(340, 340, 1), evaluate(real1, RPQ.labels(l1, l8, l9, l8)));
	}
	
	@Test
	public void real1q9(){
		assertEquals(new CardStat(340, 4420, 13), evaluate(real1, RPQ.labels(l1, l8, l9, l8, l17)));
	}
	
	@Test
	public void real1q10(){
		assertEquals(new CardStat(743, 9659, 13), evaluate(real1, RPQ.labels(l8, l9, l8, l17)));
	}
	
	@Test
	public void real1q11(){
		assertEquals(new CardStat(4, 52, 13), evaluate(real1, RPQ.labels(l9, l8, l17)));
	}
	
	@Test
	public void real1q12(){
		assertEquals(new CardStat(4, 52, 13), evaluate(real1, RPQ.concat(RPQ.label(l9), RPQ.kleene(RPQ.label(l8)), RPQ.label(l17))));
	}
	
	@Test
	public void real1q13(){
		assertEquals(new CardStat(4, 4, 1), evaluate(real1, RPQ.labels(l9, l8)));
	}
	
	@Test
	public void real1q14(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real1, 200, RPQ.label(l1)));
	}
	
	@Test
	public void real1q15(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real1, 242, RPQ.label(l1)));
	}
	
	@Test
	public void real1q16(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 200, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1q17(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 200, RPQ.concat(RPQ.label(l1), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1q18(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 227, RPQ.labels(l1, l8)));
	}
	
	@Test
	public void real1q19(){
		assertEquals(new CardStat(1, 2, 2), evaluate(real1, 227, RPQ.concat(RPQ.kleene(RPQ.label(l1)), RPQ.kleene(RPQ.label(l8)))));
	}
	
	@Test
	public void real1q20(){
		assertEquals(new CardStat(1, 3, 3), evaluate(real1, 227, kleene(l1, l8)));
	}
	
	@Test
	public void real1q21(){
		assertEquals(new CardStat(3935, 10010, 1328), evaluate(real1, kleene(l1, l8)));
	}
	
	@Test
	public void real1q22(){
		assertEquals(new CardStat(2114, 5065, 1340), evaluate(real1, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real1q23(){
		assertEquals(new CardStat(2164, 5167, 1389), evaluate(real1, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real1q24(){
		assertEquals(new CardStat(4122, 9020, 1391), evaluate(real1, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real2q0(){
		assertEquals(new CardStat(3005, 17258, 1480), evaluate(real2, RPQ.label(l0)));
	}
	
	@Test
	public void real2q1(){
		assertEquals(new CardStat(3016, 21260, 2963), evaluate(real2, RPQ.label(l1)));
	}
	
	@Test
	public void real2q2(){
		assertEquals(new CardStat(2723, 216799, 2510), evaluate(real2, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2q3(){
		assertEquals(new CardStat(2886, 183286, 2547), evaluate(real2, RPQ.labels(l0, l1.getInverse())));
	}
	
	@Test
	public void real2q4(){
		assertEquals(new CardStat(2657, 835604, 2441), evaluate(real2, RPQ.labels(l0, l1, l2)));
	}
	
	@Test
	public void real2q5(){
		assertEquals(new CardStat(2214, 395003, 1693), evaluate(real2, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real2q6(){
		assertEquals(new CardStat(946, 31587, 1697), evaluate(real2, RPQ.labels(l2, l3)));
	}
	
	@Test
	public void real2q7(){
		assertEquals(new CardStat(2580, 1335113, 1684), evaluate(real2, RPQ.labels(l0, l1, l2, l3)));
	}
	
	@Test
	public void real2q8(){
		assertEquals(new CardStat(2639, 756006, 1528), evaluate(real2, RPQ.labels(l0, l1, l2.getInverse(), l3)));
	}
	
	@Test
	public void real2q9(){
		assertEquals(new CardStat(1, 33, 33), evaluate(real2, 8, RPQ.label(l0)));
	}
	
	@Test
	public void real2q10(){
		assertEquals(new CardStat(1, 12, 12), evaluate(real2, 12, RPQ.label(l0)));
	}
	
	@Test
	public void real2q11(){
		assertEquals(new CardStat(1, 439, 439), evaluate(real2, 8, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2q12(){
		assertEquals(new CardStat(1, 182, 182), evaluate(real2, 16, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2q13(){
		assertEquals(new CardStat(1, 203, 203), evaluate(real2, 32, RPQ.labels(l0, l1)));
	}
	
	@Test
	public void real2q14(){
		assertEquals(new CardStat(1, 697, 697), evaluate(real2, 32, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real2q15(){
		assertEquals(new CardStat(1, 5, 5), evaluate(real2, 45, RPQ.labels(l1, l2, l3)));
	}
	
	@Test
	public void real3q0(){
		assertEquals(new CardStat(5482, 81754, 251), evaluate(real3, RPQ.concat(RPQ.label(l0), RPQ.kleene(RPQ.label(l3)), RPQ.kleene(RPQ.label(l2)))));
	}
	
	@Test
	public void real3q1(){
		assertEquals(new CardStat(6032, 60754, 34), evaluate(real3, RPQ.concat(RPQ.label(l0), RPQ.kleene(RPQ.label(l3)), RPQ.label(l2.getInverse()))));
	}
	
	@Test
	public void real3q2(){
		assertEquals(new CardStat(5482, 81702, 243), evaluate(real3, RPQ.labels(l0, l3, l2)));
	}
	
	@Test
	public void real3q3(){
		assertEquals(new CardStat(3, 12, 4), evaluate(real3, RPQ.labels(l0, l3, l2, l1, l4, l2)));
	}
	
	@Test
	public void real3q4(){
		assertEquals(new CardStat(1596, 6384, 4), evaluate(real3, RPQ.labels(l3, l2, l1, l4, l2)));
	}
	
	@Test
	public void real3q5(){
		assertEquals(new CardStat(45351, 118371, 18098), evaluate(real3, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real3q6(){
		assertEquals(new CardStat(58167, 841888, 36508), evaluate(real3, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real3q7(){
		assertEquals(new CardStat(58635, 940640, 36594), evaluate(real3, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real4q0(){
		assertEquals(new CardStat(1, 1, 1), evaluate(real4, RPQ.labels(l0, l2, l3, l4, l5, l1)));
	}
	
	@Test
	public void real4q1(){
		assertEquals(new CardStat(19591, 49678, 8090), evaluate(real4, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real4q2(){
		assertEquals(new CardStat(20133, 52278, 8518), evaluate(real4, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real4q3(){
		assertEquals(new CardStat(59043, 99433, 30672), evaluate(real4, kleene(l0, l1, l2, l3, l4)));
	}
	
	@Test
	public void real5q0(){
		assertEquals(new CardStat(23, 23, 1), evaluate(real5, RPQ.labels(l4, l3, l0, l2, l4, l1)));
	}
	
	@Test
	public void real5q1(){
		assertEquals(new CardStat(117599, 187448, 27431), evaluate(real5, kleene(l0, l1, l2)));
	}
	
	@Test
	public void real5q2(){
		assertEquals(new CardStat(132218, 240562, 32690), evaluate(real5, kleene(l0, l1, l2, l3)));
	}
	
	@Test
	public void real5q3(){
		assertEquals(new CardStat(141187, 494784, 37545), evaluate(real5, kleene(l0, l1, l2, l3, l4)));
	}
	
	private void assertPaths(R result, List<SourceTargetPair> expected){
		assertIterableEquals(expected, result.getSourceTargetPairs().stream().sorted().toList());
	}
	
	private CardStat evaluate(G graph, QueryLanguageSyntax query){
		return evaluate(graph, PathQuery.of(query)).computeCardinality();
	}
	
	private CardStat evaluate(G graph, int source, QueryLanguageSyntax query){
		return evaluate(graph, PathQuery.of(source, query)).computeCardinality();
	}
	
	private CardStat evaluate(G graph, QueryLanguageSyntax query, int target){
		return evaluate(graph, PathQuery.of(query, target)).computeCardinality();
	}
	
	private R evaluate(QueryLanguageSyntax query){
		return evaluate(PathQuery.of(query));
	}

	private R evaluate(QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(query, target));
	}

	private R evaluate(int source, QueryLanguageSyntax query){
		return evaluate(PathQuery.of(source, query));
	}

	private R evaluate(int source, QueryLanguageSyntax query, int target){
		return evaluate(PathQuery.of(source, query, target));
	}
	
	private R evaluate(PathQuery query){
		return evaluate(example, query);
	}
	
	private R evaluate(G graph, PathQuery query){
		Evaluator<G, R> evaluator = getProvider().createEvaluator();
		evaluator.prepare(graph);
		return evaluator.evaluate(query);
	}
	
	//see: https://research.roanh.dev/Indexing%20Conjunctive%20Path%20Queries%20for%20Accelerated%20Query%20Evaluation.pdf#subsubsection.5.2.1.1
	private G getGraph(){
		G graph = getProvider().createGraph(14, 27, 2);
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
		return graph;
	}
	
	private static RPQ kleene(Predicate... labels){
		return RPQ.kleene(RPQ.disjunct(Arrays.stream(labels).map(RPQ::label).toList()));
	}
}
