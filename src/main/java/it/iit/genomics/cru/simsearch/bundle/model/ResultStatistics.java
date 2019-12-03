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
package it.iit.genomics.cru.simsearch.bundle.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;

/**
 * @author Arnaud Ceol
 */
public class ResultStatistics {

	private static Logger logger = Logger.getLogger(ResultStatistics.class.getName());	
	
	public final static int MAX_DISPLAY = 100;

	// Set to true if the data has already been collapsed.
	private boolean isCollapsed = false;
	
	private HashMap<String, Integer> trackCombinationCount = new HashMap<>();

	private HashMap<String, Integer> patialMatchCoverage = new HashMap<>();

	private HashMap<String, Double> pvalues = new HashMap<>();

	private HashMultimap<Integer, String> scoreTracks = HashMultimap.create();

	private HashMap<String, Integer> trackCoverages;

	private Collection<String> partialMatchDatasetIds;
	
	public ResultStatistics(HashMap<String, Integer> trackCoverages, Collection<String> partialMatchDatasetIds) {
		this.trackCoverages = trackCoverages;
		this.partialMatchDatasetIds = partialMatchDatasetIds;
	}

	private HashMap<String, Integer> count = new HashMap<>(); 
	
	/**
	 * Get all subset of the pattern, for instance for pattern a,b,c: 
	 * returns a, b, c, ab, ac, bc, abc.
	 * @param trackNames Labels of all datasets in this mathcing
	 * @param length Length of the region. Used to calculate p values.
	 */
	public void addResult(List<String> trackNames, int length) {

		/* Count each single dataset */
		for (String datasetId : trackNames) {
			if (false == count.containsKey(datasetId)) {
				count.put(datasetId, 1);
			} else {
				count.put(datasetId, count.get(datasetId) + 1);
			}
		}
		
		/* count combinations */
		BooleanCounter counter = new BooleanCounter(trackNames.size());

		// init
		// counter.next();

		while (counter.hasNext()) {

			counter.next();

			String key = "";

			for (int i = 0; i < trackNames.size(); i++) {
				if (true == counter.getValueAt(i)) {
					if (false == key.equals("")) {
						key += "#";
					}
					key += trackNames.get(i);
				}
			}

			scoreTracks.put(key.split("#").length, key);

			if (false == trackCombinationCount.containsKey(key)) {
				trackCombinationCount.put(key, 1);
			} else {
				trackCombinationCount.put(key, trackCombinationCount.get(key) + 1);
			}
			
		}

	}
	
	public int getNumberOfResults(String datasetId) {
		return count.containsKey(datasetId) ? count.get(datasetId) :  0;
	}
	

	/**
	 * Remove maps which are part of another one.
	 */
	public void collapse() {
		
		if (isCollapsed) {
			return;
		}
		logger.info("Filter out non informative combinations: " + scoreTracks.values().size());
		
		ArrayList<Integer> sizes = new ArrayList<>();
		sizes.addAll(scoreTracks.keySet());
		Collections.sort(sizes);

		ArrayList<String> removeKeys = new ArrayList<>();

		for (int i = 0; i < sizes.size() - 1 && i < 10; i++) {
			for (int j = i + 1; j < sizes.size(); j++) {
				for (String keySmall : scoreTracks.get(sizes.get(i))) {
					for (String keyBig : scoreTracks.get(sizes.get(j))) {
						int numSmall = trackCombinationCount.get(keySmall);
						int numBig = trackCombinationCount.get(keyBig);

						if (numSmall <= numBig && isSubset(keySmall, keyBig)) {
							removeKeys.add(keySmall);
							break;
						}
					}
				}
			}
		}

		for (String key : removeKeys) {
			trackCombinationCount.remove(key);
		}

		this.isCollapsed = true;
	}

//	public void calculatePValues() {
//		// dive everything by bins of 500, to avoid using long
//
//		int binSize = 150;
//
//		int sampleSize = trackCoverages.get(referenceTrack) / binSize;
//
//		// 3000000000L / 500 = 6000000
//		int populationSize = 0; //250000 * 23;
//
//		for (int length: this.trackCoverages.values()  ) {
//			populationSize += length / binSize;
//		}
//		
//		
//		
//		for (String partialMatchDatasetId : partialMatchDatasetIds) {
//
//		
//			int numberOfSuccesses = Math.max(1, trackCombinationLength.get(partialMatchDatasetId) / binSize);
//
//			logger.info("search pvalue for " + key);
//
//			double probabilityOfSuccess = 1;
//
//				if (trackCoverages.containsKey(partialMatchDatasetId)) {
//					logger
//							.info(" numberOfSuccessesInPopulation = " + probabilityOfSuccess
//									+ " * ((double)Math.max(1, " + trackCoverages.get(partialMatchDatasetId) + ") / " + binSize + ") / "
//									+ populationSize);
//					probabilityOfSuccess = probabilityOfSuccess
//							* ((double) Math.max(1, trackCoverages.get(partialMatchDatasetId)) / binSize) / populationSize;
//				}
//		
//
//			double expectedPositives = probabilityOfSuccess * populationSize;
//
//			logger.info("Expected positives:  " + expectedPositives);
//
//			HypergeometricDistribution hd = new HypergeometricDistribution(populationSize, (int) expectedPositives,
//					sampleSize);
//
//			double pvalue = hd.upperCumulativeProbability(numberOfSuccesses);
//
//			logger
//					.info("pval: " + expectedPositives + ", " + sampleSize + ", " + numberOfSuccesses + " = " + pvalue);
//			pvalues.put(key, pvalue);
//		}
//	}

	public double getPvalue(String key) {
		if (false == pvalues.containsKey(key)) {
			return -1;
		}
		return pvalues.get(key);
	}

	private boolean isSubset(String smallKey, String bigKey) {
		List<String> smallKeyList = Arrays.stream(smallKey.split("#")).collect(Collectors.toList());

		List<String> bigKeyList = Arrays.stream(bigKey.split("#")).collect(Collectors.toList());

		return bigKeyList.containsAll(smallKeyList);
	}

	public Map<String, Integer> getStatistics() {
		return Collections.unmodifiableMap(trackCombinationCount);
	}

	public Collection<String> getKeysOrderBySize() {
		ArrayList<String> keys = new ArrayList<>();

		ArrayList<Integer> sizes = new ArrayList<>();
		sizes.addAll(scoreTracks.keySet());
		Collections.sort(sizes);
		Collections.reverse(sizes);

		for (int i = 0; i < sizes.size(); i++) {
			for (String key : scoreTracks.get(sizes.get(i))) {
				if (trackCombinationCount.containsKey(key)) {
					keys.add(key);
				}
			}
		}

		return keys;

	}

	public Collection<String> getKeysOrderByCount() {

		ArrayList<String> keys = new ArrayList<>();

		ArrayListMultimap<Integer, String> countToTracks = ArrayListMultimap.create();

		for (String key : trackCombinationCount.keySet()) {
			countToTracks.put(trackCombinationCount.get(key), key);
		}

		ArrayList<Integer> counts = new ArrayList<>();
		counts.addAll(countToTracks.keySet());
		Collections.sort(counts);
		Collections.reverse(counts);

		for (Integer i : counts) {
			for (String key : countToTracks.get(i)) {
				keys.add(key);
			}
		}

		return keys.subList(0, Math.min(keys.size(), MAX_DISPLAY));

	}

}
