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
package it.iit.genomics.cru.simsearch.bundle.worker;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Logger;
import java.util.regex.Matcher;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.SwingWorker;
import javax.swing.plaf.basic.BasicButtonUI;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.ServerUtils;

import it.iit.genomics.cru.simsearch.bundle.model.SimSearchParameters;
import it.iit.genomics.cru.simsearch.bundle.utils.EditDistance;
import it.iit.genomics.cru.simsearch.bundle.utils.LoopsManager;
import it.iit.genomics.cru.simsearch.bundle.view.MainPanel;
import it.iit.genomics.cru.simsearch.bundle.view.pattern.TargetDatasetsSelectionPanel;
import it.iit.genomics.cru.simsearch.bundle.view.results.ResultTab;
import it.unibo.disi.simsearch.core.business.TopKSimilaritySearch;
import it.unibo.disi.simsearch.core.model.Dataset;
import it.unibo.disi.simsearch.core.model.InputData;
import it.unibo.disi.simsearch.core.model.Pattern;
import it.unibo.disi.simsearch.core.model.Region;
import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * @author Arnaud Ceol
 */
public class ExecuteQueryWorker extends SwingWorker<ArrayList<String>, String> {
	
	private static Logger logger = Logger.getLogger(ExecuteQueryWorker.class.getName());	
	
	private Pattern pattern;
	private HashMap<String, String> datasetToTrack;
	private SimSearchParameters parameters;
	private String resultsDirectory;
	// private HashMap<String, String> inputDatasetsSources;
	private ArrayList<String> noStrandDatasets;

	public ExecuteQueryWorker(Pattern pattern, HashMap<String, String> datasetToTrack,
			ArrayList<String> noStrandDatasets, SimSearchParameters parameters, String resultsDirectory) {
		this.pattern = pattern;
		this.datasetToTrack = datasetToTrack;
		this.parameters = parameters;
		this.resultsDirectory = resultsDirectory;
		this.noStrandDatasets = noStrandDatasets;
	}

	private JPanel tempPanel;

	private JLabel workingIcon;

	private InputData inputData;

	/**
	 * Keep trace of all labels already used to ensure they are not used twice.
	 */
	private static HashMap<String, Integer> labels = new HashMap<>();

	@Override
	public ArrayList<String> doInBackground() throws Exception {

		logger.info("Start worker");

		inputData = new InputData();

		// Add chromosomes:
		Set<String> chromosomeNames = new TreeSet<String>();
		for (BioSeq seq : GenometryModel.getInstance().getSelectedGenomeVersion().getSeqList()) {
			chromosomeNames.add(seq.getId());
		}
		inputData.setChromosomeNames(chromosomeNames);

		String label = parameters.getLabel();

		if ("".equals(parameters.getLabel().trim())) {
			label = "SimSearch";
		}

		if (false == labels.containsKey(label)) {
			labels.put(label, 1);
		} else {
			int index = labels.get(label) + 1;
			labels.put(label, index);
			label = label + "-" + index;
		}

		logger.info("Label: " + label);

		ImageIcon loading = new ImageIcon(getClass().getResource("/ajax-loader.gif"));

		workingIcon = new JLabel("working... ", loading, JLabel.CENTER);

		tempPanel = new JPanel();
		tempPanel.setLayout(new BoxLayout(tempPanel, BoxLayout.PAGE_AXIS));
		tempPanel.setBackground(Color.WHITE);

		tempPanel.add(new JLabel("Start..."));

		int pos = MainPanel.getTabbedPan().getTabCount();

		ResultTab resultTab = new ResultTab(label, pattern, datasetCoverage);
		JScrollPane spTemp = new JScrollPane(tempPanel);
		resultTab.add(spTemp, BorderLayout.CENTER);
		addClosableTab(MainPanel.getTabbedPan(), resultTab, label);

		publish("Get input data");

		getDataInput();

		publish("Start pattern search");

		logger.info("New TopK");
		TopKSimilaritySearch topKSimilaritySearch = new TopKSimilaritySearch(pattern, parameters, resultsDirectory);

		logger.info("Perform search");

		logger.info(pattern.toString());

		List<TopkResult> results = topKSimilaritySearch.performSearch(inputData);

		logger.info("Free input data");

		inputData = null;

		logger.info("Save output");

		publish("Prepare result tab");
		logger.info("Prepare result tab");

		resultTab.remove(spTemp);

		resultTab.done(results, resultsDirectory, parameters);

		MainPanel.getTabbedPan().setSelectedIndex(pos);
		publish("Pattern search done.");
		return new ArrayList<>();
	}

	/**
	 * Adds a component to a JTabbedPane with a little “close tab" button on the
	 * right side of the tab.
	 *
	 * @param tabbedPane
	 *            the JTabbedPane
	 * @param c
	 *            any JComponent
	 * @param title
	 *            the title for the tab
	 */
	public static void addClosableTab(final JTabbedPane tabbedPane, final JComponent c, final String title) {
		// Add the tab to the pane without any label
		tabbedPane.addTab(title, c);

		int pos = tabbedPane.indexOfComponent(c);

		// Create a FlowLayout that will space things 5px apart
		FlowLayout f = new FlowLayout(FlowLayout.CENTER, 5, 0);

		// Make a small JPanel with the layout and make it non-opaque
		JPanel pnlTab = new JPanel(f);
		pnlTab.setOpaque(false);

		// Add a JLabel with title and the left-side tab icon
		JLabel lblTitle = new JLabel(title);

		// Create a JButton for the close tab button
		JButton btnClose = new JButton("x");
		// btnClose.setOpaque(false);
		int size = 17;
		btnClose.setPreferredSize(new Dimension(size, size));
		btnClose.setToolTipText("close this tab");
		// Make the button looks the same for all Laf's
		btnClose.setUI(new BasicButtonUI());
		// Make it transparent
		btnClose.setContentAreaFilled(false);
		// No need to be focusable
		btnClose.setFocusable(false);
		btnClose.setBorder(BorderFactory.createEtchedBorder());
		btnClose.setBorderPainted(false);
		// Making nice rollover effect
		// we use the same listener for all buttons
		btnClose.setRolloverEnabled(true);
		// Close the proper tab by clicking the button

		// Configure icon and rollover icon for button
		btnClose.setRolloverEnabled(true);

		// Set border null so the button doesn’t make the tab too big
		btnClose.setBorder(null);
		// Make sure the button can’t get focus, otherwise it looks funny
		btnClose.setFocusable(false);

		// Put the panel together
		pnlTab.add(lblTitle);
		pnlTab.add(btnClose);

		// Add a thin border to keep the image below the top edge of the tab
		// when the tab is selected
		pnlTab.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
		// Now assign the component for the tab
		tabbedPane.setTabComponentAt(pos, pnlTab);

		// Add the listener that removes the tab
		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (JOptionPane.showConfirmDialog(new JFrame(),
						"Are you sure you want to remove this tab? All results will be lost!", "Remove tab",
						JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
					tabbedPane.remove(c);
				}
			}
		};
		btnClose.addActionListener(listener);

		// Optionally bring the new tab to the front
		tabbedPane.setSelectedComponent(c);

		tabbedPane.setSelectedIndex(pos);

	}

	private void getDataInput() {
		logger.info("Get input data: " + pattern.getAllDatasetIds().size());

		LoopsManager loops = new LoopsManager();

		// Look first for composite datasets
		for (String patternDatasetId : pattern.getLoopsDatasetIds()) {

			String trackId = datasetToTrack.get(patternDatasetId); // .split("\\.")[0];
			Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
					.ifPresent(selectedGenomeVersion -> {
						selectedGenomeVersion.getAvailableDataContainers().stream()
								.flatMap(dc -> dc.getDataSets().stream()).filter(dataSet -> dataSet.isVisible())
								.filter(dataSet -> dataSet.getDataSetName().equals(trackId))
								.map(dataSet -> ServerUtils.determineLoader(SymLoader.getExtension(dataSet.getURI()),
										dataSet.getURI(), Optional.empty(),
										DataSet.detemineFriendlyName(dataSet.getURI()), selectedGenomeVersion))

								.forEach(symLoader -> {
									selectedGenomeVersion.getSeqList().stream().forEach(bioSeq -> {

										try {

											int distanceAllowed = pattern.getLoopDistance(patternDatasetId);

											// logger.info("Distance allowed for loops :  " + distanceAllowed);

											String chrPattern = "chr[0-9]+";

											// Create a Pattern object
											java.util.regex.Pattern r = java.util.regex.Pattern.compile(chrPattern);
											java.util.regex.Matcher matcher = r.matcher(bioSeq.getId());
											if (matcher.matches() && symLoader.getChromosome(bioSeq) != null
													&& symLoader.getChromosome(bioSeq).size() > 0) {
														logger.info("Get Loop data: " + bioSeq.getId());
												symLoader.getChromosome(bioSeq).stream()
														.filter(seqSym -> seqSym.getSpanCount() > 0).forEach(seqSym -> {
															SeqSpan span = seqSym.getSpan(0);

															int x1 = 0;
															int y1 = 0;
															int x2 = 0;
															int y2 = 0;

															for (int i = 0; i < seqSym.getChildCount(); i++) {
																SeqSymmetry child = seqSym.getChild(i);
																SeqSpan childSpan = child.getSpan(0);

																if (i == 0) {
																	x1 = Math.max(0,
																			childSpan.getMin() - distanceAllowed);
																	y1 = childSpan.getMax() + distanceAllowed;
																}

																if (i == 1) {
																	x2 = Math.max(0,
																			childSpan.getMin() - distanceAllowed);
																	y2 = childSpan.getMax() + distanceAllowed;
																}
															}

															if (seqSym.getChildCount() >= 2) {
																loops.addLoop(span.getBioSeq().getId(), x1, y1, x2, y2);
																// logger.info("loop: " + span.getBioSeq().getId() + " : " + x1 + "-" + y1 + " -> " + x2 + "-" + y2);
															}

														});

											}

										} catch (Exception ex) {
											logger.severe("BioSeq: " + bioSeq.getId());
											logger.severe("Exception while getting loop data");ex.printStackTrace();
											logger.severe(ex.getMessage());
										}
									});
								});
					});
		}

		for (String patternDatasetId : pattern.getAllDatasetIds()) {

			List<Region> regions;

			String trackId = datasetToTrack.get(patternDatasetId); // .split("\\.")[0];

			publish("Get regions for dataset: " + patternDatasetId + " in track " + trackId);
			if (TargetDatasetsSelectionPanel.TSS_IGB_TRACK.equals(trackId)) {
				String strandPattern = pattern.getRegionsSpecificDataset(patternDatasetId).get(0).getStrand();
				regions = this.getTSS(strandPattern);
			} else if (TargetDatasetsSelectionPanel.MOTIF_TRACK.equals(trackId)) {
				// find pattern
				// for each chromosome:
				regions = new ArrayList<>();

				java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(patternDatasetId,
						java.util.regex.Pattern.CASE_INSENSITIVE);

				logger.info("pattern: " + pattern.toString());

				Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
						.ifPresent(selectedGenomeVersion -> {
							selectedGenomeVersion.getSeqList().stream().forEach(bioSeq -> {
								if (false == bioSeq.getId().contains("_") && false == "genome".equals(bioSeq.getId())) {

									logger
											.info("Search motif " + patternDatasetId + " in " + bioSeq.getId());
									try {
										Collection<Region> regionsChr = searchForRegexInResidues(true, pattern, bioSeq);
										logger.info("num regions: " + regionsChr.size());
										if (false == regionsChr.isEmpty()) {
											regions.addAll(regionsChr);
										}
									} catch (Exception e) {
										e.printStackTrace();
									}
								}
							});
						});

			} else {
				regions = new ArrayList<>();

				Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
						.ifPresent(selectedGenomeVersion -> {
							selectedGenomeVersion.getAvailableDataContainers().stream()
									.flatMap(dc -> dc.getDataSets().stream()).filter(dataSet -> dataSet.isVisible())
									.filter(dataSet -> dataSet.getDataSetName().equals(trackId))
									.map(dataSet -> ServerUtils.determineLoader(
											SymLoader.getExtension(dataSet.getURI()), dataSet.getURI(),
											Optional.empty(), DataSet.detemineFriendlyName(dataSet.getURI()),
											selectedGenomeVersion))
									.forEach(symLoader -> {
										selectedGenomeVersion.getSeqList().stream().filter(bioSeq -> {
											try {
												return symLoader.getChromosome(bioSeq) != null;
											} catch (Exception e) {
												return false;
											}
										}).forEach(bioSeq -> {
											try {
												symLoader.getChromosome(bioSeq).stream()
														.filter(seqSym -> seqSym.getSpanCount() > 0).forEach(seqSym -> {
															SeqSpan span = seqSym.getSpan(0);

															boolean correctStrand = true;

															if (false == pattern
																	.getRegionsSpecificDataset(patternDatasetId)
																	.isEmpty()) {

																String strandPattern = pattern
																		.getRegionsSpecificDataset(patternDatasetId)
																		.get(0).getStrand();
																try {

																	String strandRegion = span.isForward() ? "+" : "-";
																	if (false == strandPattern.equals(".")
																			&& false == strandPattern.equals("*")
																			&& false == strandPattern
																					.equals(strandRegion)) {
																		correctStrand = false;
																	}

																} catch (Exception e) {
																	logger
																			.info("No working: get strand for "
																					+ patternDatasetId);
																	e.printStackTrace();
																}
															}

															if (correctStrand) {

																/*
																 * if the region is too long, we split it
																 */
																ArrayList<Region> splitRegions = new ArrayList<>();


																// If max peak length is defined and the region is larger, split it.
																if (parameters.getMaxPeakLength() <= 0 || span.getLength() <= parameters.getMaxPeakLength()) {

																	Region regionS = new Region(
																			span.getBioSeq().getId(), span.getMin(),
																			span.getMax(),
																			noStrandDatasets.contains(patternDatasetId)
																					? "."
																					: span.isForward() ? "+" : "-",
																			seqSym.getID());

																	splitRegions.add(regionS);
																	
																} else {
																	int min = span.getMin();
																	int length = parameters.getMaxPeakLength();
																	int numRegions = (int) Math
																			.ceil(new Double(span.getLength())
																					/ parameters.getMaxPeakLength());

																	if (numRegions == 0) {
																		logger.info(
																				"Problem: region divided in 0!!!");
																	}

																	for (int i = 0; i < numRegions; i++) {
																		Region region = new Region(
																				span.getBioSeq().getId(), min,
																				Math.min(min + length, span.getMax()),
																				noStrandDatasets
																						.contains(patternDatasetId)
																								? "."
																								: span.isForward() ? "+"
																										: "-",
																				seqSym.getID() + "-" + i);
																		min += length;
																		splitRegions.add(region);
																	}
																}

																/*
																 * Attributes: only for positive matches
																 */
																if (parameters.isRegionSimilarityRelevant()
																		&& pattern.getPositiveMatchDatasetIds()
																				.contains(patternDatasetId)
																		&& SymWithProps.class.isInstance(seqSym)) {
																	SymWithProps symWithProps = ((SymWithProps) seqSym);

																	// Get
																	// attributes
																	// for this
																	// dataset

																	for (String targetAttribute : pattern
																			.getTargetAttributes(patternDatasetId)) {
																		Object value = symWithProps.getProperties()
																				.get(targetAttribute);
																		if (null == value) {
																			logger
																					.info("No value for attribute: "
																							+ targetAttribute);
																		} else if (String.class.isInstance(value)) {
																			// double?
																			try {
																				for (Region region : splitRegions) {
																					region.addAttribute(targetAttribute,
																							Double.parseDouble(
																									(String) value));
																				}

																			} catch (NumberFormatException nbe) {
																				// Not
																				// a
																				// double,
																				// ignore
																			}
																		} else if (Number.class.isInstance(value)) {
																			for (Region region : splitRegions) {
																				region.addAttribute(targetAttribute,
																						((Number) value).doubleValue());
																			}
																		}
																		// }
																	}
																}

																regions.addAll(splitRegions);

																// Transfer from composites
																ArrayList<Region> trasferedRegions = new ArrayList<>();
																for (Region region : splitRegions) {

																	addRegionLength(patternDatasetId,
																			region.getLength());

																	for (Region tr : loops.transferedRegions(region)) {
																		trasferedRegions.add(tr);
																	}
																}

																regions.addAll(trasferedRegions);

															}
														});
											} catch (Exception ex) {
												System.out.println("2. " + patternDatasetId + " -> " + bioSeq.getId());
												logger.severe(ex.getMessage() + ". Feature2 "
														+ symLoader.getFeatureName() + ", " + bioSeq.getId());
												ex.printStackTrace();
											}
										});
									});
						});

			}

			publish(regions.size() + " regions.");

			if (regions.isEmpty()) {
				logger.info("Get data: no regions for " + patternDatasetId);
				continue;
			}
			Dataset inputDataset = new Dataset(trackId, regions, null);

			if (inputDataset.getRegions().size() <= 0) {
				JOptionPane.showMessageDialog(tempPanel, "The selected dataset " + trackId
						+ " is not correct because it has no regions. Please change the selection.");
				continue;
			}

			if (pattern.getNegativeMatchDatasetIds().contains(patternDatasetId)) {
				inputData.addNegativeMatchDataset(trackId, patternDatasetId, inputDataset);
			} else if (pattern.getValidAreaDatasetIds().contains(patternDatasetId)) {
				inputData.addValidAreasDataset(trackId, patternDatasetId, inputDataset);
			} else {
				if (pattern.getPartialMatchDatasetIds().contains(patternDatasetId)) {
					inputData.addPartialMatchDataset(trackId, patternDatasetId, inputDataset);
				} else if (pattern.getPerfectMatchDatasetIds().contains(patternDatasetId)) {
					inputData.addPerfectMatchDataset(trackId, patternDatasetId, inputDataset);
				} else {
					// Off ??
					logger.info("Skip dataset (off?) " + trackId);
				}

				if (parameters.isRegionSimilarityRelevant()) {
					publish("... check attributes ");

					try {
						int attributesNumber = pattern.getRegionsSpecificDataset(patternDatasetId).get(0)
								.getAttributesNumber();
						List<String> patternAttributeNames = pattern.getRegionsSpecificDataset(patternDatasetId).get(0)
								.getAttributeNames();
						List<String> attributeNames = inputDataset.getRegions().get(0).getAttributeNames();

						HashMap<String, String> queryToTargetAttributesNamesCorrespondances = new HashMap<String, String>();
						for (int attributeIndex = 0; attributeIndex < attributesNumber; attributeIndex++) {
							String patternAttributeName = patternAttributeNames.get(attributeIndex);

							int minDistance = Integer.MAX_VALUE;
							String minDistanceAttributeName = null;
							for (String attributeName : attributeNames) {
								int editDistance = EditDistance.computeDistance(patternAttributeName, attributeName);
								if (editDistance < minDistance) {
									minDistanceAttributeName = attributeName;
									minDistance = editDistance;
								}
							}

							queryToTargetAttributesNamesCorrespondances.put(patternAttributeName,
									minDistanceAttributeName);
						}

						inputData.setDatasetQueryToTargetAttributesNamesCorrespondances(trackId,
								queryToTargetAttributesNamesCorrespondances);
					} catch (Exception ex) {
						logger.info(ex.getMessage());
						ex.printStackTrace();
					}
				}
			}
		}

	}

	/**
	 * Coverage of the genome by each positive dataset. This is an over estimation
	 * as we don't consider that two regions may overlap.
	 */
	private HashMap<String, Integer> datasetCoverage = new HashMap<>();// pattern.getPositiveMatchDatasetIds());

	private void addRegionLength(String datasetId, int length) {
		if (false == datasetCoverage.containsKey(datasetId)) {
			datasetCoverage.put(datasetId, length);
		} else {
			datasetCoverage.put(datasetId, datasetCoverage.get(datasetId) + length);
		}
	}

	private List<Region> getTSS(String strandPattern) {

		ArrayList<Region> regions = new ArrayList<>();

		Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
				.ifPresent(selectedGenomeVersion -> {
					selectedGenomeVersion.getAvailableDataContainers().stream().flatMap(dc -> dc.getDataSets().stream())
							.filter(dataSet -> dataSet.isVisible())
							.filter(dataSet -> dataSet.getDataSetName().equals("RefGene"))
							.map(dataSet -> ServerUtils.determineLoader(SymLoader.getExtension(dataSet.getURI()),
									dataSet.getURI(), Optional.empty(), DataSet.detemineFriendlyName(dataSet.getURI()),
									selectedGenomeVersion))

							.forEach(symLoader -> {
								selectedGenomeVersion.getSeqList().stream()
										// .filter(bioSeq ->
										// bioSeq.getId().length() < 6)
										.forEach(bioSeq -> {
											try {
												if (symLoader.getChromosome(bioSeq) != null
														&& symLoader.getChromosome(bioSeq).size() > 0) {
													symLoader.getChromosome(bioSeq).stream()
															.filter(seqSym -> seqSym.getSpanCount() > 0)
															.forEach(seqSym -> {

																SeqSpan geneSpan = seqSym.getSpan(bioSeq);

																boolean correctStrand = true;
																String strandRegion = geneSpan.isForward() ? "+" : "-";
																try {

																	if (false == strandPattern.equals(".")
																			&& false == strandPattern.equals("*")
																			&& false == strandPattern
																					.equals(strandRegion)) {
																		correctStrand = false;
																	}

																} catch (Exception e) {
																	logger
																			.info("No working: get strand for "
																					+ strandPattern);
																	e.printStackTrace();
																}

																if (correctStrand) {
																	Region region = new Region(
																			geneSpan.getBioSeq().getId(),
																			geneSpan.getStart(),
																			geneSpan.getStart() + 1,
																			geneSpan.isForward() ? "+" : "-",
																			seqSym.getID());
																	regions.add(region);
																}
															});
												}
											} catch (Exception ex) {
												logger.severe(ex.getMessage() + ". Feature1 "
														+ symLoader.getFeatureName() + ", " + bioSeq.getId());
											}
										});
							});
				});
		return regions;
	}

	private static final int MAX_RESIDUE_LEN_SEARCH = 1000000;

	public static List<Region> searchForRegexInResidues(boolean forward, java.util.regex.Pattern regex, BioSeq seq) {
		List<Region> results = new ArrayList<>();

		logger.info("Bioseq loader: " + seq.getResiduesProvider());

		int chrLength = seq.getLength();

		for (int residue_offset = 0; residue_offset < chrLength; residue_offset += MAX_RESIDUE_LEN_SEARCH) {
			try {

				String residues = seq.getResidues(residue_offset,
						Math.min(residue_offset + MAX_RESIDUE_LEN_SEARCH, chrLength - 1));

				Matcher matcher = regex.matcher(residues);

				while (matcher.find() && !Thread.currentThread().isInterrupted()) {

					int residue_start = residue_offset + (forward ? matcher.start(0) : -matcher.end(0));
					int residue_end = residue_offset + (forward ? matcher.end(0) : -matcher.start(0));
					// int end = matcher.end(0) + residue_offset;
					Region region = new Region(seq.getId(), residue_start, residue_end, forward ? "+" : "-", "motif");

					results.add(region);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}

		}
		return results;
	}

	@Override
	protected void process(List<String> chunks) {
		for (String message : chunks) {
			logger.info(message);
			tempPanel.remove(workingIcon);
			tempPanel.add(new JLabel(message));
			tempPanel.add(workingIcon);
			tempPanel.updateUI();
			workingIcon.setText("...");
		}
	}
		public static void main(String[] args) {
			System.out.println("Hello universe");
		}

}
