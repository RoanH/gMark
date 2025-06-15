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
package dev.roanh.gmark.lang;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import dev.roanh.gmark.type.schema.Predicate;

public class QueryLanguageTest{

	@Test
	public void validateReachabilityAssumptions(){
		for(QueryLanguage language : QueryLanguage.values()){
			if(language == QueryLanguage.CPQ || language == QueryLanguage.RPQ){
				assertTrue(language.isReachabilityQueryLanguage());
			}else if(language == QueryLanguage.CQ){
				assertFalse(language.isReachabilityQueryLanguage());
			}else{
				fail("Unknown query language");
			}
		}
	}
	
	@Test
	public void validateLinking(){
		Map<QueryLanguage, String> queries = Map.of(
			QueryLanguage.CPQ, "id",
			QueryLanguage.RPQ, "a",
			QueryLanguage.CQ, "(f1) ← a(f1, f1)"
		);
		
		for(String name : Arrays.stream(QueryLanguage.values()).map(QueryLanguage::name).toList()){
			QueryLanguage lang = QueryLanguage.fromName(name).orElseThrow();
			assertTrue(queries.containsKey(lang));
			assertEquals(lang, lang.parse(queries.get(lang)).getQueryLanguage());
			assertEquals(lang, lang.parse(queries.get(lang), List.of(new Predicate(0, "a"))).getQueryLanguage());
		}
	}
}
