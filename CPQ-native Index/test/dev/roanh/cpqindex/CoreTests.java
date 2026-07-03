/*
 * CPQ-native Index: A graph database index with native support for CPQs.
 * Copyright (C) 2023  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/CPQ-native-index
 *
 * CPQ-native Index is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * CPQ-native Index is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.cpqindex;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import dev.roanh.gmark.lang.cpq.CPQ;
import dev.roanh.gmark.lang.cpq.QueryGraphCPQ;
import dev.roanh.gmark.util.Util;

/**
 * Simple performance evaluation test for CPQ core computation.
 * @author Roan
 */
public class CoreTests{

	public static void main(String[] args) throws InterruptedException, ExecutionException{
		Util.setRandomSeed(1234L);
		
		//warmup runs
		for(int i = 0; i < 100; i++){
			CPQ.generateRandomCPQ(4, 2).computeCore();
		}
		
		ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
		
		//actual runs
		for(int rules = 4; rules <= 1024; rules *= 2){
			List<Integer> sizeE = new ArrayList<Integer>(1024);
			List<Integer> sizeV = new ArrayList<Integer>(1024);
			List<Callable<Long>> tasks = new ArrayList<Callable<Long>>();
			
			for(int i = 0; i < 1024; i++){
				QueryGraphCPQ q = CPQ.generateRandomCPQ(rules, 4).toQueryGraph();
				sizeE.add(q.getEdgeCount());
				sizeV.add(q.getVertexCount());
				tasks.add(()->{
					long start = System.nanoTime();
					q.computeCore();
					long end = System.nanoTime();
					return end - start;
				});
			}
			
			List<Long> times = new ArrayList<Long>(1024);
			for(Future<Long> f : executor.invokeAll(tasks)){
				times.add(f.get());
			}
			
			System.out.print(rules + " DataE: ");
			printStats(sizeE);
			System.out.print("DataV: ");
			printStats(sizeV);
			System.out.print("Time: ");
			printStats(times);
		}
	}
	
	private static void printStats(List<? extends Number> data){
		DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
		data.forEach(num->stats.accept(num.doubleValue()));
		double diff = data.stream().mapToDouble(d->Math.pow(d.doubleValue() - stats.getAverage(), 2.0D)).sum();
		System.out.println("min= " + stats.getMin() + " max= " + stats.getMax() + " avg= " + stats.getAverage() + " stddev= " + Math.sqrt(diff / stats.getCount()));
	}
}
