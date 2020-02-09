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
package it.iit.genomics.cru.simsearch.bundle.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.ServerUtils;
import com.google.common.collect.HashMultimap;

import org.apache.commons.math3.distribution.HypergeometricDistribution;

import it.unibo.disi.simsearch.core.model.Region;
import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * Take annotations from a track, and count how many time each annotation is
 * associated to the topK results.
 * 
 * @author Arnaud Ceol
 *
 */
public class AnnotationsFromTrack {

	private static Logger logger = Logger.getLogger(AnnotationsFromTrack.class.getName());

	/**
	 * Number of time an annotation is present
	 */
	private HashMap<String, Integer> annotationsCount;

	/**
	 * Number of time an annotation associated to a result.
	 */
	private HashMap<String, Integer> annotationsMappedCount;

	/**
	 * key: annotation type values: regions Id (position)
	 */
	private HashMultimap<String, String> matchingMappedByAnnotationType = HashMultimap.create();

	private HashMap<String, Double> pValues = new HashMap<>();

	public Collection<String> getAnnotations() {
		return annotationsMappedCount.keySet();
	}

	public Collection<String> getAnnotationsByPvalueIncreasing() {
		ArrayList<Double> probabilities = new ArrayList<>();
		probabilities.addAll(pValues.values());
		Collections.sort(probabilities);

		ArrayList<String> sortedAnnotations = new ArrayList<>();

		for (Double probability : probabilities) {
			for (String annotation : getAnnotations()) {
				if (pValues.get(annotation).equals(probability) && false == sortedAnnotations.contains(annotation)) {
					sortedAnnotations.add(annotation);
				}
			}
		}

		return sortedAnnotations;
	}

	public Collection<String> getAnnotationsByNumberOfOverlap() {
		ArrayList<Integer> counts = new ArrayList<>();
		counts.addAll(annotationsMappedCount.values());
		Collections.sort(counts);
		Collections.reverse(counts);

		ArrayList<String> sortedAnnotations = new ArrayList<>();

		for (Integer count : counts) {
			for (String annotation : getAnnotations()) {
				if (annotationsMappedCount.get(annotation).equals(count)
						&& false == sortedAnnotations.contains(annotation)) {
					sortedAnnotations.add(annotation);
				}
			}
		}

		return sortedAnnotations;
	}

	public int getNumberOfMatchings(String annotationType) {
		HashSet<String> matching = new HashSet<>();
		matching.addAll(matchingMappedByAnnotationType.get(annotationType));
		return matching.size();
	}

	public int getNumberOfAnnotations(String annotation) {
		return annotationsCount.get(annotation);
	}

	public int getNumberOfAnnotationsMapped(String annotation) {
		return annotationsMappedCount.get(annotation);
	}

	public double getProbability(String annotation) {
		return this.pValues.get(annotation);
	}

	public int getNumberOfAnnotations() {
		int total = 0;
		for (int number : annotationsCount.values()) {
			total += number;
		}
		return total;
	}

	public int getNumberOfAnnotationsMapped() {
		int total = 0;
		for (int number : annotationsMappedCount.values()) {
			total += number;
		}
		return total;
	}

	public void process(String trackId, Collection<TopkResult> results) {

		// Chromosome n

		// for each annotation

		// for each result
		// for each region
		// if overlapp
		// --> add annotation
		// next annotation
		// next result.

		annotationsCount = new HashMap<>();
		annotationsMappedCount = new HashMap<>();

		// Order topkresults
		HashMap<String, ArrayList<Region>> orderedRegions = new HashMap<>();

		logger.info("Order regions");

		try {

			for (TopkResult result : results) {
				for (String datasetId : result.getPositiveMatchDatasetIds()) {
					if (result.getDataset(datasetId) == null) {
						continue;
					}

					if (result.getDataset(datasetId).getRegions() == null) {
						continue;
					}

					for (Region region : result.getDataset(datasetId).getRegions()) {

						if (region == null) {
							logger.info("null region");
							continue;
						}

						if (region.getChromosome() == null) {
							logger.info("null chromosome");
							continue;
						}

						ArrayList<Region> regions = orderedRegions.get(region.getChromosome());

						if (regions == null) {
							regions = new ArrayList<Region>();
							orderedRegions.put(region.getChromosome(), regions);
						}

						int i = 0;
						for (; i < regions.size(); i++) {
							if (regions.get(i).getLeft() > region.getLeft()) {
								break;
							}
						}
						regions.add(i, region);
					}
				}
			}

		} catch (Exception e) {
			logger.severe(e.getMessage());
			e.printStackTrace();
		}

		logger.info("Regions ordered. Coumt annotations.");

		Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
				.ifPresent(selectedGenomeVersion -> {
					selectedGenomeVersion.getAvailableDataContainers().stream().flatMap(dc -> dc.getDataSets().stream())
							.filter(dataSet -> dataSet.isVisible())
							.filter(dataSet -> dataSet.getDataSetName().equals(trackId))
							.map(dataSet -> ServerUtils.determineLoader(SymLoader.getExtension(dataSet.getURI()),
									dataSet.getURI(), Optional.empty(), DataSet.detemineFriendlyName(dataSet.getURI()),
									selectedGenomeVersion))
							.forEach(symLoader -> {
								selectedGenomeVersion.getSeqList().stream().forEach(bioSeq -> {

									String chromosome = bioSeq.getId();
									ArrayList<Region> regions = orderedRegions.get(chromosome);

									if (regions != null) {

										try {

											List<? extends SeqSymmetry> symmetries = symLoader.getChromosome(bioSeq);
											if (chromosome.startsWith("chr") && false == chromosome.contains("_")
													&& symmetries != null && symmetries.size() > 0) {

												Iterator<? extends SeqSymmetry> annotationSymIterator = symmetries
														.iterator();

												while (annotationSymIterator.hasNext()) {
													String annotation = null;
													SeqSymmetry seqSym = annotationSymIterator.next();
													// span = seqSym.getSpan(0);

													if (null != ((SymWithProps) seqSym).getProperty("name")) {
														annotation = ((SymWithProps) seqSym).getProperty("name")
																.toString();
													} else {
														// find the first text annotation
														for (Object propertyValue : ((SymWithProps) seqSym)
																.getProperties().values()) {
															String s = propertyValue.toString();
															if (s.matches("[A-Za-z]")) {
																annotation = s;
																break;
															}
														}
													}

													if (null != annotation) {
														increase(annotationsCount, annotation);
													}

												}
											}
										} catch (Exception e) {
											logger.severe("pre-load annotations: " + bioSeq.getId());
										}
									}
								});
							});
				});

		for (String annotation : annotationsCount.keySet()) {
			logger.severe("Annotation: " + annotation + " : " + annotationsCount.get(annotation));
		}

		Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
				.ifPresent(selectedGenomeVersion -> {
					selectedGenomeVersion.getAvailableDataContainers().stream().flatMap(dc -> dc.getDataSets().stream())
							.filter(dataSet -> dataSet.isVisible())
							.filter(dataSet -> dataSet.getDataSetName().equals(trackId))
							.map(dataSet -> ServerUtils.determineLoader(SymLoader.getExtension(dataSet.getURI()),
									dataSet.getURI(), Optional.empty(), DataSet.detemineFriendlyName(dataSet.getURI()),
									selectedGenomeVersion))
							.forEach(symLoader -> {
								selectedGenomeVersion.getSeqList().stream().forEach(bioSeq -> {

									String chromosome = bioSeq.getId();

									ArrayList<Region> regions = orderedRegions.get(chromosome);

									if (regions != null) {

										Iterator<Region> regionIt = regions.iterator();

										Region currentResult = regionIt.next();
										SeqSpan span = null;

										String annotation = null;

										try {

											List<? extends SeqSymmetry> symmetries = symLoader.getChromosome(bioSeq);
											if (chromosome.startsWith("chr") && false == chromosome.contains("_")
													&& symmetries != null && symmetries.size() > 0) {

												Iterator<? extends SeqSymmetry> annotationSymIterator = symmetries
														.iterator();

												boolean nextResult = false;
												boolean nextAnnotation = annotationSymIterator.hasNext();

												while (nextResult || nextAnnotation) {

													if (nextAnnotation) {

														SeqSymmetry seqSym = annotationSymIterator.next();

														if (seqSym.getSpanCount() == 0) {
															continue;
														}

														if (false == SymWithProps.class.isInstance(seqSym)) {
															continue;
														}

														span = seqSym.getSpan(0);

														if (null != ((SymWithProps) seqSym).getProperty("name")) {
															annotation = ((SymWithProps) seqSym).getProperty("name")
																	.toString();
														} else {
															// find the first text annotation
															for (Object propertyValue : ((SymWithProps) seqSym)
																	.getProperties().values()) {
																String s = propertyValue.toString();
																if (s.matches("[A-Za-z]")) {
																	annotation = s;
																	break;
																}
															}
														}

														// if (null != annotation) {
														// increase(annotationsCount, annotation);
														// }
														if (span.getMax() < currentResult.getLeft()) {
															continue;
														}

														nextAnnotation = false;
													}

													if (nextResult) {
														if (false == regionIt.hasNext()) {

															break;
														}
														currentResult = regionIt.next();

														if (currentResult.getRight() < span.getMin()) {
															continue;
														}

														nextResult = false;

													}

													if (overlap(span, currentResult)) {
														increase(annotationsMappedCount, annotation);

														/**
														 * Warning: in the original model, tostring returns null. We
														 * should create a id
														 */
														String resultLabel = currentResult.getChromosome() + ":"
																+ currentResult.getLeft() + "-"
																+ currentResult.getRight();
														matchingMappedByAnnotationType.put(annotation, resultLabel);
														// next annotation
														nextAnnotation = true;
														nextResult = false;
													} else if (currentResult.getRight() < span.getMin()) {
														nextResult = true;
														nextAnnotation = false;
													} else {
														nextResult = false;
														nextAnnotation = annotationSymIterator.hasNext();
														// nextResult =
														// true;
													}
												}

											}

										} catch (Exception ex) {
											logger.severe(ex.getMessage() + ". Feature2 " + symLoader.getFeatureName()
													+ ", " + bioSeq.getId());
											ex.printStackTrace();
										}
									}

								});
							});
				});

		/**
		 * Get pvalues
		 */

		int populationSize = this.getNumberOfAnnotations();
		int sampleSize = this.getNumberOfAnnotationsMapped();

		for (String annotation : this.getAnnotations()) {
			int numberOfSuccesses = this.getNumberOfAnnotations(annotation);

			HypergeometricDistribution hd = new HypergeometricDistribution(populationSize, numberOfSuccesses,
					sampleSize);

			double probability = hd.upperCumulativeProbability(this.getNumberOfAnnotationsMapped(annotation));
			this.pValues.put(annotation, probability);
			logger.severe("pvalue: " + numberOfSuccesses + " | " + sampleSize + " | " + populationSize + " | "
					+ this.getNumberOfAnnotationsMapped(annotation) + " = " + probability);
		}

		logger.info("Num annotations: " + this.getAnnotations().size() + " / " + annotationsCount.keySet().size());

	}

	private boolean overlap(SeqSpan span, Region region) {

		if (span.getMin() < region.getLeft() && span.getMax() > region.getLeft()) {
			return true;
		}

		if (span.getMin() > region.getLeft() && span.getMin() < region.getRight()) {
			return true;
		}
		return false;
	}

	private void increase(HashMap<String, Integer> annotationsCount1, String annotation) {

		if (annotationsCount1.containsKey(annotation)) {
			annotationsCount1.put(annotation, annotationsCount1.get(annotation) + 1);
		} else {
			annotationsCount1.put(annotation, 1);
		}
	}

}
