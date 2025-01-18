/*
 * gMark: A domain- and query language-independent query workload generator and query language utility library.
 * Copyright (C) 2021  Roan Hofland (roan@roanh.dev).  All rights reserved.
 * GitHub Repository: https://github.com/RoanH/gMark
 *
 * gMark is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * gMark is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dev.roanh.gmark.cli;

import dev.roanh.gmark.query.QueryGenerator.ProgressListener;

/**
 * Progress listener that report progress to
 * standard out at 10% intervals.
 * @author Roan
 */
public class ProgressReporter implements ProgressListener{
	/**
	 * The last completion count progress was reported.
	 */
	private int last = 0;
	
	/**
	 * Constructs a new progress reporter.
	 */
	public ProgressReporter(){
		System.out.print("Progress: ");
	}

	@Override
	public void update(int done, int total){
		if(done == total){
			System.out.println("100%");
		}else if(last + total / 10 == done){
			System.out.print((done / (total / 10)) * 10 + "%...");
			last = done;
		}
	}
}