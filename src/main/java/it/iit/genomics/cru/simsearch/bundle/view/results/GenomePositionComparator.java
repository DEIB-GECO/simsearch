/* 
 * Copyright 2017 Fondazione Istituto Italiano di Tecnologia.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package it.iit.genomics.cru.simsearch.bundle.view.results;

import java.util.Comparator;

/**
 * @author Arnaud Ceol
 */
public class GenomePositionComparator implements Comparator<String> {
	/**
	 * Custom compare to sort numbers as numbers. Strings as strings, with
	 * numbers ordered before strings.
	 * 
	 * @param o1
	 * @param o2
	 * @return
	 */
	@Override
	public int compare(String oo1, String oo2) {

		String chr1 = oo1.split(":")[0];
		Integer from1 = Integer.parseInt(oo1.split(":")[1].split("-")[0]);
		Integer to1 = Integer.parseInt(oo1.split("-")[1]);

		String chr2 = oo2.split(":")[0];
		Integer from2 = Integer.parseInt(oo2.split(":")[1].split("-")[0]);
		Integer to2 = Integer.parseInt(oo2.split("-")[1]);

		if (false == chr1.equals(chr2)) {
			return chr1.compareTo(chr2);
		}

		if (false == from1.equals(from2)) {
			return from1.compareTo(from2);
		}

		return to1.compareTo(to2);
	}
}
