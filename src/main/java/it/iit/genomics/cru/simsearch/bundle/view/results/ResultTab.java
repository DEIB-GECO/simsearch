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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingConstants;
import javax.swing.SwingWorker;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.table.DefaultTableModel;

import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
//import com.affymetrix.genoviz.bioviews.GlyphI;
import org.lorainelab.igb.services.IgbService;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenomeVersion;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.color.RGB;
import com.affymetrix.genometry.parsers.CytobandParser;
import com.affymetrix.genometry.span.SimpleSeqSpan;
import com.affymetrix.genometry.style.ITrackStyleExtended;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.symmetry.impl.TypeContainerAnnot;
import com.google.common.io.Files;

import it.iit.genomics.cru.simsearch.bundle.model.ColorPalette;
import it.iit.genomics.cru.simsearch.bundle.model.ResultStatistics;
import it.iit.genomics.cru.simsearch.bundle.model.SimSearchParameters;
import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;
import it.iit.genomics.cru.simsearch.bundle.utils.ServiceManager;
import it.iit.genomics.cru.simsearch.bundle.worker.AnnotationsWorker;
import it.iit.genomics.cru.simsearch.bundle.worker.TrackAnnotationsWorker;
import it.unibo.disi.simsearch.core.business.ResultImagesGenerator;
import it.unibo.disi.simsearch.core.model.Pattern;
import it.unibo.disi.simsearch.core.model.Region;
import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * @author Arnaud Ceol
 */
public final class ResultTab extends JPanel {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(ResultTab.class.getName());

	private ArrayList<SeqSymmetry> nearestGenes = new ArrayList<>();

	private final static String LINE_SEPARATOR = System.getProperty("line.separator");

	JTable resultsTable;

	GenometryModel gmodel = GenometryModel.getInstance();

	IgbService igbService;

	JPanel headerPanel;

	String label;

	Pattern pattern;

	JPanel descriptionPanel;

	ResultStatistics summary;

	public ResultTab(String label, Pattern pattern, HashMap<String, Integer> datasetCoverage) {
		logger.info("create result tab.");

		this.summary = new ResultStatistics(datasetCoverage, pattern.getPartialMatchDatasetIds());

		this.label = label;
		this.pattern = pattern;

		igbService = ServiceManager.getInstance().getService();
		logger.info("Service called.");
		this.setLayout(new BorderLayout());

		headerPanel = new JPanel(new BorderLayout());

		descriptionPanel = new JPanel();
		descriptionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		descriptionPanel.setLayout(new BoxLayout(descriptionPanel, BoxLayout.Y_AXIS));

		int datasetIndex = 0;

		try {

			Dimension dimension = new Dimension(20, 20);

			if (false == pattern.getPerfectMatchDatasetIds().isEmpty()) {
				JPanel perfectPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

				perfectPanel.add(new JLabel("Perfect matches:"));

				for (String datasetId : pattern.getPerfectMatchDatasetIds()) {
					datasetIndex++;
					String text = datasetId;
					String strand = pattern.getRegionsSpecificDataset(datasetId).get(0).getStrand();
					if (false == ".".equals(strand) && false == "*".equals(strand)) {
						text += " " + strand;
					}

					Color bg = ColorPalette.getInstance().getColor(datasetIndex - 1);
					Color fg = Color.WHITE;

					JLabel datasetLabel = new JLabel("" + datasetIndex, SwingConstants.CENTER);
					datasetLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

					datasetLabel.setPreferredSize(dimension);
					datasetLabel.setMinimumSize(dimension);
					datasetLabel.setMaximumSize(dimension);
					datasetLabel.setOpaque(true);
					datasetLabel.setBackground(bg);
					datasetLabel.setForeground(fg);
					perfectPanel.add(datasetLabel);
					perfectPanel.add(new JLabel(text));
				}

				descriptionPanel.add(perfectPanel);
			}

			if (false == pattern.getPartialMatchDatasetIds().isEmpty()) {
				JPanel partialPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));

				partialPanel.add(new JLabel("Partial matches:"));

				for (String datasetId : pattern.getPartialMatchDatasetIds()) {
					datasetIndex++;
					String text = datasetId;
					String strand = pattern.getRegionsSpecificDataset(datasetId).get(0).getStrand();
					if (false == ".".equals(strand) && false == "*".equals(strand)) {
						text += " " + strand;
					}

					Color bg = ColorPalette.getInstance().getColor(datasetIndex - 1);
					Color fg = Color.WHITE;

					JLabel datasetLabel = new JLabel("" + datasetIndex, SwingConstants.CENTER);
					datasetLabel.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));

					datasetLabel.setPreferredSize(dimension);
					datasetLabel.setMinimumSize(dimension);
					datasetLabel.setMaximumSize(dimension);
					datasetLabel.setOpaque(true);
					datasetLabel.setBackground(bg);
					datasetLabel.setForeground(fg);
					partialPanel.add(datasetLabel);
					partialPanel.add(new JLabel(text));
				}
				descriptionPanel.add(partialPanel);
			}

			if (false == pattern.getNegativeMatchDatasetIds().isEmpty()) {
				JPanel negativePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
				negativePanel.add(new JLabel("Negative matches:"));

				for (String datasetId : pattern.getNegativeMatchDatasetIds()) {
					negativePanel.add(new JLabel(datasetId + ": " + pattern.getNegativeMatchDistance(datasetId)));
				}

				descriptionPanel.add(negativePanel);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		headerPanel.add(descriptionPanel);

		add(headerPanel, BorderLayout.NORTH);
	}

	public void done(List<TopkResult> results, String resultsDirectory, SimSearchParameters parameters) {

		String bedFileName = resultsDirectory + File.separator + label.toLowerCase() + ".bed";
		String resultsFileName = resultsDirectory + File.separator + label.toLowerCase() + "-results.txt";
		String genesFileName = resultsDirectory + File.separator + label.toLowerCase() + "-genes.txt";

		Dimension buttonDimension = new Dimension(250, 24);

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.PAGE_AXIS));

		JButton saveAs = new JButton();
		saveAs.setIcon(UIManager.getIcon("FileView.fileIcon"));
		saveAs.setPreferredSize(buttonDimension);
		saveAs.setMaximumSize(buttonDimension);
		saveAs.setAction(new AbstractAction("Save") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser fileChooser = new JFileChooser();

				if (fileChooser.showSaveDialog(saveAs) == JFileChooser.APPROVE_OPTION) {
					File file = fileChooser.getSelectedFile();
					try {
						Files.copy(new File(resultsFileName), file);
					} catch (IOException e1) {
						JOptionPane.showMessageDialog(buttonPanel, "Cannot write to file " + file.getPath(),
								"Save file error", JOptionPane.ERROR_MESSAGE);
					}
				}

			}
		});

		saveAs.setIcon(UIManager.getIcon("FileView.floppyDriveIcon"));
		buttonPanel.add(saveAs);

		JButton functionalAnnotations = new JButton();

		functionalAnnotations.setPreferredSize(buttonDimension);
		functionalAnnotations.setMaximumSize(buttonDimension);

		functionalAnnotations.setAction(new AbstractAction("Functional Annotations") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String organism = igbService.getSelectedSpecies();

				AnnotationsWorker worker = new AnnotationsWorker(organism, resultsDirectory, label,
						functionalAnnotations);
				worker.execute();
			}
		});

		functionalAnnotations.setIcon(new ImageIcon(getClass().getResource("/go.jpg")));
		buttonPanel.add(functionalAnnotations);

		JComboBox<String> annotationTrack = new JComboBox<>(SourcePattern.getInstance().getAvailableTrackDatasets());

		annotationTrack.setPreferredSize(buttonDimension);
		annotationTrack.setMaximumSize(buttonDimension);

		annotationTrack.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				annotationTrack.removeAllItems();
				for (String track : SourcePattern.getInstance().getAvailableTrackDatasets()) {
					annotationTrack.addItem(track);
				}
			}

			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}
		});

		JButton trackAnnotations = new JButton();

		trackAnnotations.setPreferredSize(buttonDimension);
		trackAnnotations.setMaximumSize(buttonDimension);

		trackAnnotations.setAction(new AbstractAction("Track Annotations") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String track = (String) annotationTrack.getSelectedItem();

				TrackAnnotationsWorker worker = new TrackAnnotationsWorker(track, resultsDirectory, label,
						trackAnnotations, results);
				worker.execute();
			}
		});

		buttonPanel.add(trackAnnotations);

		annotationTrack.setPreferredSize(buttonDimension);
		annotationTrack.setMaximumSize(buttonDimension);

		buttonPanel.add(annotationTrack);

		JButton stats = new JButton();
		stats.setPreferredSize(buttonDimension);
		stats.setMaximumSize(buttonDimension);
		stats.setAction(new AbstractAction("summary") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				final JFrame frame = new JFrame("summary " + label);

				// Merge results if not already done
				summary.collapse();
				
				JPanel statsPanel = new StatisticsGraphicalPanel(pattern.getPositiveMatchDatasetIds(),
					 summary);

				frame.add(statsPanel);

				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
				frame.setSize(550, 200);
				frame.setVisible(true);
			}
		});

		stats.setIcon(UIManager.getIcon("FileView.computerIcon"));
		buttonPanel.add(stats);

		headerPanel.add(buttonPanel, BorderLayout.EAST);

		descriptionPanel.add(new JLabel("Number of results: " + results.size()));

		resultsTable = new ResultTable();
		StringBuffer content = new StringBuffer();
		String titleLine = "track name=" + label + " type=bedDetail description=\"" + label + "\" itemRgb=\"On\" ";
		content.append(titleLine + System.lineSeparator());
		SeqSpan firstSpan = null;

		HashSet<String> genes = new HashSet<>();

		try {
			File resultFile = new File(resultsFileName);
			logger.info("Write local resource: " + resultsFileName);
			Writer resultWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(resultFile), "utf-8"));

			resultWriter.write(
					"Result\tScore\tPosition\tdistance between regions\tNearest gene\tDistance to nearest gene\tUp/Downstream\tMatching tracks"
							+ LINE_SEPARATOR);

			int i = 0;
			for (TopkResult result : results) {
				i++;
				// add symmetry
				int min = Integer.MAX_VALUE;
				int max = 0;

				int minCentroid = Integer.MAX_VALUE;
				int maxCentroid = 0;

				ArrayList<String> blockMins = new ArrayList<>();
				ArrayList<String> blockSizes = new ArrayList<>();
				// ArrayList<Integer> centers = new ArrayList<>();
				String chromosome = "";

				ArrayList<String> positiveMatchFound = new ArrayList<>();

				int lenghOfFirstRegion = 0;

				boolean firstResult = true;

				/*
				 * The center of the root region, used to find the nearest gene. If one of the
				 * matching region is a TSS, we choose it. In other cases choose the first
				 * perfect match.
				 */
				int refCenterForNearestGene = -1;

				for (String datasetID : pattern.getPositiveMatchDatasetIds()) {
					if (result.getDataset(datasetID) != null) {

						boolean added = false;

						for (Region region : result.getDataset(datasetID).getRegions()) {
							if (region == null) {
								continue;
							}

							if (added == false) {
								positiveMatchFound.add(datasetID);
								added = true;
							}

							if (firstResult) {
								lenghOfFirstRegion = region.getLength();
								refCenterForNearestGene = region.getCentroid();
								firstResult = false;
							}

							/**
							 * TODO: add some checking, it is technically possible to name TSS a dataset
							 * that has nothing to do with TSS.
							 */
							if (datasetID.startsWith("TSS")) {
								refCenterForNearestGene = region.getCentroid();
							}

							chromosome = region.getChromosome();
							min = Math.min(min, region.getLeftOrigin());
							max = Math.max(max, region.getRightOrigin());

							minCentroid = Math.min(minCentroid, getCentroidOrigin(region));
							maxCentroid = Math.max(maxCentroid, getCentroidOrigin(region));

							int blockMin = Math.min(region.getLeftOrigin(), region.getRightOrigin());

							/* blocks should be ordered */
							int blockIndex = 0;
							for (; blockIndex < blockMins.size(); blockIndex++) {
								if (Integer.parseInt(blockMins.get(blockIndex)) > blockMin) {
									break;
								}
							}
							if (blockIndex <= blockMins.size()) {
								blockMins.add(blockIndex, Integer.toString(blockMin));
								blockSizes.add(blockIndex, Integer.toString(region.getLength()));
							} else {
								blockMins.add(Integer.toString(blockMin));
								blockSizes.add(Integer.toString(region.getLength()));
							}
						}
					} else {
						logger.info("No dataset for ID: " + datasetID);
					}
				}

				summary.addResult(positiveMatchFound, lenghOfFirstRegion);

				DefaultTableModel model = (DefaultTableModel) resultsTable.getModel();

				String geneName;
				int distanceFromNearestGene = 0;

				SeqSymmetry nearestGene;

				if (refCenterForNearestGene < 0) {
					geneName = "-";
				} else {
					nearestGene = getNearestGeneSym(chromosome, refCenterForNearestGene);
					nearestGenes.add(nearestGene);
					geneName = null == nearestGene ? ""
							: ((SymWithProps) nearestGene).getProperty("title") + " (" + nearestGene.getID() + ")";

					// distance from nearest gene:
					if (null != nearestGene) {
						distanceFromNearestGene = nearestGene.getSpan(0).isForward()
								? nearestGene.getSpan(0).getStart() - refCenterForNearestGene
								: refCenterForNearestGene - nearestGene.getSpan(0).getStart();
					}

					if (false == "".equals(geneName)) {
						try {
							String gene = null != ((SymWithProps) nearestGene).getProperty("title")
									? (String) ((SymWithProps) nearestGene).getProperty("title")
									: "";
							genes.add(gene);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}

				double score = Math.round(1000.0 * result.getSimilarityScore()) / 1000.0;
				String coordinate = chromosome + ":" + min + "-" + max;
				int length = maxCentroid - minCentroid;

				String matchPosititionReletedToGene = distanceFromNearestGene <= 0 ? "Downstream" : "Upstream";

				model.addRow(new Object[] { i, score, coordinate, length, geneName, Math.abs(distanceFromNearestGene),
						matchPosititionReletedToGene, result });


				ArrayList<String> similarities = new ArrayList<>();
				
				for (String dataset: result.getAlignmentScores().keySet()) {
					for (Double d : result.getAlignmentScores().get(dataset)) {
						if (null != d && d > 0) {
							similarities.add(dataset + " ("+String.format("%.2f", d)+")");
						}
					}
				}

				String matchingTracks = String.join(",", similarities);

				resultWriter.write(i + "\t" + score + "\t" + coordinate + "\t" + length + "\t" + geneName + "\t"
						+ Math.abs(distanceFromNearestGene) + "\t" + matchPosititionReletedToGene + "\t" + matchingTracks + LINE_SEPARATOR);

				BioSeq seq = GenometryModel.getInstance().getSelectedGenomeVersion().getSeq(chromosome);
				if (seq == null) {
					logger.info("No seq for " + chromosome);
				} else {
					if (firstSpan == null) {
						SimpleSeqSpan span = new SimpleSeqSpan(min, max, seq);
						firstSpan = span;
					}
					for (int j = 0; j < blockSizes.size(); j++) {
						blockMins.set(j, Integer.toString((Integer.parseInt(blockMins.get(j)) - min)));
					}
					content.append(seq.getId() + "\t" + min + "\t" + max + "\t" + label + "Topk" + i + "\t"
							+ (new Double(result.getSimilarityScore() * 1000)).intValue() + "\t" + "." + "\t" + min
							+ "\t" + min // result.getSimilarityScore()
							+ "\t" + parameters.getTrackColor().getRed() + "," + parameters.getTrackColor().getGreen()
							+ "," + parameters.getTrackColor().getBlue() + "\t" + blockMins.size() + "\t"
							+ String.join(",", blockSizes) + "\t" + String.join(",", blockMins) + "\t" + label + "\t"
							+ System.lineSeparator());
				}
			}

			logger.info("Process summary");
			// summary.collapse();
			// summary.calculatePValues();
			logger.info("Process summary done");

			resultWriter.close();

			// new track
			String datasetId = label.toLowerCase();
			logger.info("Track id: " + datasetId);

			File file = new File(bedFileName);
			logger.info("Write local resource: " + bedFileName);
			Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8"));
			writer.write(content.toString());
			writer.close();
			logger.info("File writen, uri: " + file.toURI());
			GenomeVersion loadGroup = GenometryModel.getInstance().getSelectedGenomeVersion();
			logger.info("GenomeVersion: " + loadGroup.getName());
			String speciesName = loadGroup.getSpeciesName();
			logger.info("Species: " + speciesName);
			URI uri = file.toURI();
			logger.info("Open uri: " + uri);
			igbService.openURI(uri, label, loadGroup, speciesName, false);
			logger.info("Content loaded");

			// Write gene list

			File genesFile = new File(genesFileName);
			logger.info("Write local resource: " + genesFileName);
			Writer genesWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(genesFile), "utf-8"));

			for (String gene : genes) {
				genesWriter.write(gene + LINE_SEPARATOR);
			}
			genesWriter.close();

		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		this.add(new JScrollPane(resultsTable), BorderLayout.CENTER);
		resultsTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent event) {
				if (event.getValueIsAdjusting() || resultsTable.getSelectedRow() < 0) {
					return;
				}
				TopkResult result = results.get(resultsTable.convertRowIndexToModel(resultsTable.getSelectedRow()));
				int min = Integer.MAX_VALUE;
				int max = 0;
				String chromosome = "";
				for (String datasetID : pattern.getPositiveMatchDatasetIds()) {
					// if (null != result.getDataset(datasetID)) {
					for (Region region : result.getDataset(datasetID).getRegions()) {
						if (region == null) {
							continue;
						}
						chromosome = region.getChromosome();

						min = Math.min(min, region.getLeftOrigin());
						max = Math.max(max, region.getRightOrigin());
					}
					// }
				}
				List<Image> resultsImage = ResultImagesGenerator.getResultsImage(pattern, results, true,
						resultsTable.getSelectedRow() + 1, resultsTable.getSelectedRow() + 1);
				Image img = resultsImage.get(0);
				Double newWidth = 300.0;
				Double newHeight = img.getHeight(null) * newWidth / img.getWidth(null);
				JLabel label = new JLabel();
				label.setIcon(new ImageIcon(
						img.getScaledInstance(newWidth.intValue(), newHeight.intValue(), java.awt.Image.SCALE_SMOOTH)));// your
				// image
				// here
				BorderLayout layout = (BorderLayout) getLayout();
				if (null != layout.getLayoutComponent(BorderLayout.EAST)) {
					remove(layout.getLayoutComponent(BorderLayout.EAST));
				}
				add(new JScrollPane(label, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
						ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.EAST);
				ServiceManager.getInstance().getService().zoomToCoord(chromosome, min - 1000, max + 1000);
				SwingWorker<Integer, String> aWorker = new SwingWorker<Integer, String>() {
					@Override
					protected Integer doInBackground() throws Exception {
						try {
							ServiceManager.getInstance().getService().loadVisibleFeatures();
						} catch (Exception ex) {
							ex.printStackTrace();
							logger.info("Cannot load Symms: " + ex.getMessage());
						}
						return 0;
					}
				};
				aWorker.execute();

			}
		});
		if (results.size() > 0) {
			resultsTable.setRowSelectionInterval(0, 0);
		}
		for (TierGlyph tg : igbService.getVisibleTierGlyphs()) {
			if (tg.getAnnotStyle().getTrackName().equals(label)) {
				ITrackStyleExtended style = tg.getAnnotStyle();
				style.setColorProvider(new RGB());
			}
		}
		logger.info("ResultTab done.");
	}

	/**
	 * Get nearest gene: the one which start position is closer to the matching
	 * region center
	 * 
	 * @param chromosome
	 * @param position
	 * @return
	 */
	public SeqSymmetry getNearestGeneSym(String chromosome, int position) {

		java.util.regex.Pattern CYTOBAND_TIER_REGEX = java.util.regex.Pattern
				.compile(".*" + CytobandParser.CYTOBAND_TIER_NAME);

		SeqSymmetry chromosomeSym = null;

		BioSeq seq = GenometryModel.getInstance().getSelectedGenomeVersion().getSeq(chromosome);

		// Find the chromosome
		for (int a = 0; a < seq.getAnnotationCount(); a++) {
			SeqSymmetry annotSym = seq.getAnnotation(a);
			if (annotSym instanceof TypeContainerAnnot) {
				TypeContainerAnnot tca = (TypeContainerAnnot) annotSym;
				if (false == CYTOBAND_TIER_REGEX.matcher(tca.getType()).matches()) {
					chromosomeSym = annotSym;
					break;
				}
			}
		}

		if (chromosomeSym == null) {
			logger.warning("No chromosome found for " + seq.getId());
			return null;
		}

		int countChildren = chromosomeSym.getChildCount();
		int distance = Integer.MAX_VALUE;

		SeqSymmetry nearestGene = null;

		for (int j = 0; j < countChildren; j++) {
			SymWithProps geneSym = (SymWithProps) chromosomeSym.getChild(j);
			SeqSpan geneSeqSpan = geneSym.getSpan(0);
			boolean currentGeneIsBefore = geneSeqSpan.getStart() < position;
			int localDistance = Math.abs(position - geneSeqSpan.getStart());
			// We move from one side of the query position to the other.
			// Check which one is closer
			if (false == currentGeneIsBefore) {
				return localDistance < distance ? geneSym : nearestGene;
			}
			distance = localDistance;
			nearestGene = geneSym;
		}
		return nearestGene;
	}

	private int getCentroidOrigin(Region region) {
		return (region.getLeftOrigin() + region.getRightOrigin()) / 2;
	}

}
