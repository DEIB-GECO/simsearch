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
package it.iit.genomics.cru.simsearch.bundle.view.pattern;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;

import org.lorainelab.igb.genoviz.extensions.glyph.StyledGlyph;
import org.lorainelab.igb.genoviz.extensions.glyph.TierGlyph;
import org.lorainelab.igb.services.IgbService;

import com.affymetrix.common.CommonUtils;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.SeqSpan;
import com.affymetrix.genometry.general.DataContainer;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.google.common.io.Files;

import it.iit.genomics.cru.simsearch.bundle.model.SimSearchParameters;
import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;
import it.iit.genomics.cru.simsearch.bundle.utils.ServiceManager;
import it.iit.genomics.cru.simsearch.bundle.worker.ExecuteQueryWorker;
import it.unibo.disi.simsearch.core.business.ResultImagesGenerator;
import it.unibo.disi.simsearch.core.model.Dataset;
import it.unibo.disi.simsearch.core.model.Pattern;
import it.unibo.disi.simsearch.core.model.QueryRegion;
import it.unibo.disi.simsearch.core.model.Region;
import it.unibo.disi.simsearch.core.model.TopkResult;
import it.unibo.disi.simsearch.core.utils.IOHandler;

/**
 * @author Arnaud Ceol
 */
public class TargetDatasetsSelectionPanel extends JPanel
		implements ActionListener, TableModelListener, PropertyChangeListener {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(TargetDatasetsSelectionPanel.class.getName());	

	public static boolean SKIP_ATTRIBUTES = false;

	public String[] trackCommands = { SourcePattern.COMMAND_TRACK_PERFECT, SourcePattern.COMMAND_TRACK_PARTIAL,
			SourcePattern.COMMAND_TRACK_NEGATIVE, SourcePattern.COMMAND_TRACK_LOOP,
			SourcePattern.COMMAND_TRACK_VALID_AREA };

	private JButton btnAdd;

	private JComboBox<String> selectionTypeList = new JComboBox<>(trackCommands);

	private JButton btnClear;
	private JButton btnAlign;
	private JButton btnAddAll;
	private JButton btnAddTSS;
	private JButton btnMotif;
	private JButton btnConfirm;
	private JButton btnShow;
	private JButton btnLoadFile;
	private JComboBox<String> patternList;
	private JTextField labelEditor;

	private JButton trackColour;
	private JTextField numResults;
	private JCheckBox isRegionSimilarityRelevant;

	private JButton showAdvanceParameters;

	private JFrame advancedParametersFrame;

	public static final boolean[] BOTH_STRANDS = { true, true };
	private final static String NULL_PATTERN_LABEL = "-- load pre-defined pattern --";

	public static final String TSS_IGB_TRACK = "TSS (RefGene)";
	public static final String MOTIF_TRACK = "MOTIF SEARCH";

	PatternTable patternTable;

	private static TargetDatasetsSelectionPanel instance;

	public static TargetDatasetsSelectionPanel getInstance() {
		if (instance == null) {
			instance = new TargetDatasetsSelectionPanel();
		}
		return instance;
	}

	private TargetDatasetsSelectionPanel() {
		super();
		SourcePattern.getInstance().addPropertyChangeListener(this);
		patternTable = new PatternTable();
		this.setLayout(new BorderLayout(10, 10));

		JPanel confirmPanel = new JPanel(new GridLayout(1, 4, 15, 15));

		confirmPanel.setOpaque(false);

		confirmPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

		btnLoadFile = new JButton(CommonUtils.getInstance().getIcon("16x16/actions/document-open.png"));
		btnLoadFile.setMaximumSize(new Dimension(Integer.MAX_VALUE, btnLoadFile.getMinimumSize().height));
		btnLoadFile.addActionListener(this);

		Dimension buttonDimension = new Dimension(24, 24);

		btnLoadFile.setPreferredSize(buttonDimension);
		btnLoadFile.setMaximumSize(buttonDimension);

		patternList = new JComboBox<String>(this.getDefaultPatterns());
		patternList.insertItemAt(NULL_PATTERN_LABEL, 0);
		patternList.setSelectedIndex(0);
		patternList.addActionListener(this);

		patternList.setMaximumSize(patternList.getPreferredSize());

		labelEditor = new JTextField("SimSearch");
		labelEditor.setSize(350, 24);

		btnAdd = new JButton("Selection");
		btnAdd.addActionListener(this);
		btnAdd.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/list-add.png"));

		btnAddTSS = new JButton("TSS");
		btnAddTSS.addActionListener(this);
		btnAddTSS.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/list-add.png"));

		btnAddAll = new JButton("All tracks");
		btnAddAll.addActionListener(this);
		btnAddAll.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/list-add.png"));

		
		btnMotif = new JButton("Motif");
		btnMotif.addActionListener(this);
		btnMotif.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/list-add.png"));

		btnClear = new JButton("Clear");
		btnClear.addActionListener(this);
		btnClear.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/delete.gif"));

		btnAlign = new JButton("Align");
		btnAlign.addActionListener(this);
		btnAlign.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/center_on_zoom_stripe.png"));

		btnShow = new JButton(CommonUtils.getInstance().getIcon("16x16/actions/show.png"));
		btnShow.setPreferredSize(buttonDimension);
		btnShow.setMaximumSize(buttonDimension);
		btnShow.addActionListener(this);

		btnConfirm = new JButton("Submit");
		btnConfirm.addActionListener(this);
		btnConfirm.setIcon(CommonUtils.getInstance().getIcon("16x16/status/clear script.png"));

		Dimension buttonDimension2 = new Dimension(150, 24);

		btnAdd.setPreferredSize(buttonDimension2);
		btnAdd.setMaximumSize(buttonDimension2);

		selectionTypeList.setPreferredSize(buttonDimension2);
		selectionTypeList.setMaximumSize(buttonDimension2);

		btnAddTSS.setPreferredSize(buttonDimension2);
		btnAddTSS.setMaximumSize(buttonDimension2);

		btnMotif.setPreferredSize(buttonDimension2);
		btnMotif.setMaximumSize(buttonDimension2);

		btnAddAll.setPreferredSize(buttonDimension2);
		btnAddAll.setMaximumSize(buttonDimension2);

		
		btnClear.setPreferredSize(buttonDimension2);
		btnClear.setMaximumSize(buttonDimension2);

		btnAlign.setPreferredSize(buttonDimension2);
		btnAlign.setMaximumSize(buttonDimension2);

		btnConfirm.setPreferredSize(buttonDimension2);
		btnConfirm.setMaximumSize(buttonDimension2);

		// Max results
		JLabel paramKLabel = new JLabel("Max results: ");

		numResults = new JTextField();
		Dimension numResultsDimension = new Dimension(80, 24);
		numResults.setText("2000");
		numResults.setSize(numResultsDimension);
		numResults.setPreferredSize(numResultsDimension);
		numResults.setMinimumSize(numResultsDimension);
		numResults.setColumns(6);

		JPanel buildPanel = new JPanel(new GridLayout(3, 2));
		buildPanel.setBorder(BorderFactory.createTitledBorder("Build pattern"));

		buildPanel.add(btnAdd);
		buildPanel.add(selectionTypeList);
		buildPanel.add(btnAddTSS);
		buildPanel.add(btnAlign);
		buildPanel.add(btnMotif);
		buildPanel.add(btnClear);
		buildPanel.add(btnAddAll);

		JPanel outputPanel = new JPanel(new GridLayout(3, 2, 5, 5));
		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));

		outputPanel.add(new JLabel("Label: "));

		Dimension labelDimension = new Dimension(250, 24);
		labelEditor.setSize(labelDimension);
		labelEditor.setPreferredSize(labelDimension);
		labelEditor.setMinimumSize(labelDimension);
		labelEditor.setColumns(25);

		outputPanel.add(labelEditor);

		Color defaultColor = Color.RED;

		trackColour = new JButton();
		trackColour.setAction(new AbstractAction() {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Color newColor = JColorChooser.showDialog(trackColour, "Choose Track Color",
						trackColour.getBackground());
				if (newColor != null) {
					trackColour.setBackground(newColor);
				}
			}

		});

		trackColour.setBackground(defaultColor);
		trackColour.setForeground(defaultColor);

		trackColour.setContentAreaFilled(false);
		trackColour.setOpaque(true);
		trackColour.addActionListener(this);

		outputPanel.add(new JLabel("Track color:"));
		outputPanel.add(trackColour);

		outputPanel.add(paramKLabel);
		outputPanel.add(numResults);

		JPanel advancedPanel = new JPanel(new GridLayout(3, 1));
		advancedPanel.setBorder(BorderFactory.createTitledBorder("Misc."));
		patternList.setPrototypeDisplayValue(NULL_PATTERN_LABEL);

		advancedPanel.add(patternList);

		// Use attributes
		isRegionSimilarityRelevant = new JCheckBox("Use attributes");
		isRegionSimilarityRelevant.setSelected(false);
		isRegionSimilarityRelevant.addActionListener(this);

		advancedPanel.add(isRegionSimilarityRelevant);

		advancedParametersFrame = new JFrame("Advanced parameters");
		advancedParametersFrame.setSize(600, 500);
		advancedParametersFrame.add(ParametersPanel.getInstance());
		advancedParametersFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		showAdvanceParameters = new JButton(CommonUtils.getInstance().getIcon("16x16/actions/equalizer.png"));
		showAdvanceParameters.setPreferredSize(buttonDimension);
		showAdvanceParameters.setMaximumSize(buttonDimension);

		showAdvanceParameters.setAction(new AbstractAction("advanced parameters") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				advancedParametersFrame.setVisible(true);
			}

		});

		showAdvanceParameters.setIcon(CommonUtils.getInstance().getIcon("16x16/actions/equalizer.png"));

		advancedPanel.add(showAdvanceParameters);

		JPanel submitPanel = new JPanel();
		submitPanel.setBorder(BorderFactory.createTitledBorder("submit"));

		submitPanel.add(btnShow);
		submitPanel.add(btnConfirm);

		confirmPanel.add(buildPanel);
		confirmPanel.add(advancedPanel);
		confirmPanel.add(outputPanel);
		confirmPanel.add(submitPanel);

		add(confirmPanel, BorderLayout.NORTH);

		patternTable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				int type = e.getType();
				switch (type) {
				case TableModelEvent.UPDATE:
					if (e.getFirstRow() - e.getLastRow() == 0) {

						TableModel model = (TableModel) e.getSource();

						int row = e.getFirstRow();
						int col = e.getColumn();
						if (col == PatternTableModel.COLUMN_DATASET_ID) {
							String oldValue = SourcePattern.getInstance().getAllDatasetIds().get(row);
							updateName(oldValue, (String) model.getValueAt(row, col));
						} else if (col == PatternTableModel.COLUMN_TYPE) {
							updateType((String) model.getValueAt(row, PatternTableModel.COLUMN_DATASET_ID),
									(TrackTypeElement) model.getValueAt(row, col));
						} else if (col == PatternTableModel.COLUMN_STRAND) {
							logger.info("Change strand.");
							logger
									.info("Change strand: "
											+ (String) model.getValueAt(row, PatternTableModel.COLUMN_DATASET_ID)
											+ getStrand((boolean[]) model.getValueAt(row, col)));
							updateStrand((String) model.getValueAt(row, PatternTableModel.COLUMN_DATASET_ID),
									(boolean[]) model.getValueAt(row, col));
						} else if (col == PatternTableModel.COLUMN_RANGE) {
							updateRange((String) model.getValueAt(row, PatternTableModel.COLUMN_DATASET_ID),
									(String) model.getValueAt(row, col));
						} else if (col == PatternTableModel.COLUMN_TRACK) {
							// Do nothing.
						} else if (col == PatternTableModel.COLUMN_ACTION) {
							// Do nothing.
						}
					}
					break;
				}
			}
		});
		add(new JScrollPane(patternTable), BorderLayout.CENTER);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		logger.info("Table data management changed");
		refreshDatasets();
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnShow) {
			this.showPattern();
		} else if (e.getSource() == btnAdd) {
			this.addSelected();
		} else if (e.getSource() == btnClear) {
			this.clear();
		} else if (e.getSource() == btnAddAll) {
			this.addAllTracks();;
		} else if (e.getSource() == btnAlign) {
			for (String datasetId : SourcePattern.getInstance().getPositiveMatchDatasetIds()) {
				updateRange(datasetId, "0");
			}
			patternTable.updateTable();
		} else if (e.getSource() == this.isRegionSimilarityRelevant) {
			ParametersPanel.getInstance().setIsSimilarityRelevant(isRegionSimilarityRelevant.isSelected());
		} else if (e.getSource() == btnConfirm) {
			logger.info(SourcePattern.getInstance().toString());
			// Get corresponding data from table
			HashMap<String, String> datasetToTrack = new HashMap<>();
			ArrayList<String> noStrandDatasets = new ArrayList<>();
			for (int i = 0; i < patternTable.getModel().getRowCount(); i++) {
				datasetToTrack.put((String) patternTable.getModel().getValueAt(i, PatternTableModel.COLUMN_DATASET_ID),
						(String) patternTable.getModel().getValueAt(i, PatternTableModel.COLUMN_TRACK));
				logger.info("Dataset to track: "
						+ (String) patternTable.getModel().getValueAt(i, PatternTableModel.COLUMN_DATASET_ID) + " -> "
						+ (String) patternTable.getModel().getValueAt(i, PatternTableModel.COLUMN_TRACK));
			}
			File resultsDirectory = Files.createTempDir();
			logger.info("Create directory for results: " + resultsDirectory.getPath());
			SimSearchParameters parameters = ParametersPanel.getInstance().getParameters(SourcePattern.getInstance());

			parameters.setLabel(labelEditor.getText());
			parameters.setTrackColor(trackColour.getBackground());

			try {
				if (Integer.parseInt(numResults.getText()) <= 0) {
					throw new NumberFormatException();
				}
				parameters.setK(Integer.parseInt(numResults.getText()));
			} catch (NumberFormatException exc) {
				JOptionPane.showMessageDialog(this,
						"Please set a valid value for the parameter k. It must be a positive integer value.");
				numResults.requestFocus();
				return;
			}

			parameters.setRegionSimilarityRelevant(isRegionSimilarityRelevant.isSelected());

			ExecuteQueryWorker executeQueryThread = new ExecuteQueryWorker(SourcePattern.getInstance().clone(),
					datasetToTrack, noStrandDatasets, parameters, resultsDirectory.getPath()); // inputDatasetsSources,
			executeQueryThread.execute();
		} else if (e.getSource() == btnAddTSS) {
			this.addTSS();
		} else if (e.getSource() == btnMotif) {
			String motif = JOptionPane.showInputDialog("Please input a motif (e.g. CACGTG");
			/**
			 * TODO: verify that it is a valid motif.
			 */
			if (null != motif) {
				this.addMotif(motif);
			}
		} else if (e.getSource() == btnLoadFile) {
			this.loadFile();
		} else if (e.getSource() == patternList) {
			String patternName = (String) patternList.getSelectedItem();
			if (false == NULL_PATTERN_LABEL.equals(patternName)) {
				URL url = getClass().getResource("/patterns/" + patternName + ".txt");
				InputStream is;
				try {
					is = url.openStream();
					loadFile(is);
					Color color = patternColors.get(patternName);
					logger.info("Change color: " + color.toString());
					trackColour.setBackground(color);
					trackColour.setContentAreaFilled(false);
					trackColour.setOpaque(true);
				} catch (IOException e1) {
					logger.severe("Cannot load pattern from file: /patterns/" + patternName + ".txt");
					e1.printStackTrace();
				}
				
				try {
					this.labelEditor.setText(patternName.split("[0-9]+")[1].substring(1));
				} catch (Exception e2) {
					logger.warning("The name of the file is not well formatted: " + patternName );
				}
				
			}
		}
	}

	private String getStrand(boolean[] strands) {
		if (strands[0] && false == strands[1]) {
			return "+";
		}
		if (false == strands[0] && strands[1]) {
			return "-";
		}
		return ".";
	}

	public void updateStrand(String datasetId, boolean[] values) {
		for (QueryRegion region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
			region.setStrand(getStrand(values));
		}
	}

	public void updateRange(String datasetId, String values) {

		if (SourcePattern.getInstance().getPositiveMatchDatasetIds().contains(datasetId)) {

			if (null != SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				SourcePattern.getInstance().getRegionsSpecificDataset(datasetId).clear();
			} else {
				SourcePattern.getInstance().setRegionsSpecificDataset(datasetId, new ArrayList<>());
			}

			logger.info("Update range: " + datasetId + " -> " + values);
			String[] ranges = values.split(";");
			for (String range : ranges) {
				try {
					int start;
					int end;
					if (range.contains("-")) {
						start = Integer.parseInt(range.split("-")[0].trim());
						end = Integer.parseInt(range.split("-")[1].trim());
					} else {
						start = Integer.parseInt(range.trim());
						end = start;
					}
					QueryRegion region = new QueryRegion(datasetId, start, end, getStrand(BOTH_STRANDS), null);
					SourcePattern.getInstance().getRegionsSpecificDataset(datasetId).add(region);
				} catch (java.lang.NumberFormatException e) {
					logger.info("Bad format: " + range);
				}
			}

		} else {
			logger.info("Update distance: " + datasetId + " -> " + values);
			Integer distance = Integer.parseInt(values);

			if (SourcePattern.getInstance().getNegativeMatchDatasetIds().contains(datasetId)) {
				SourcePattern.getInstance().addNegativeMatchDatasetIds(datasetId, distance);
			} else if (SourcePattern.getInstance().getLoopsDatasetIds().contains(datasetId)) {
				SourcePattern.getInstance().addLoopsDatasetIds(datasetId, distance);
			} else if (SourcePattern.getInstance().getValidAreaDatasetIds().contains(datasetId)) {
				SourcePattern.getInstance().addValidAreaDatasetIds(datasetId, distance);
			}

		}

	}

	int numNewElement = 1;

	/**
	 * Add selected tracks or symmetries
	 */
	public void addSelected() {
		/// Tracks
		if (false == ServiceManager.getInstance().getService().getSelectedTierGlyphs().isEmpty()) {

			// region: average of all other ones
			int min = 0;
			int max = 0;
			int numRanges = 0;
			for (String id : SourcePattern.getInstance().getAllDatasetIds()) {
				if (SourcePattern.getInstance().getRegionsSpecificDataset(id) != null) {
					for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(id)) {
						min += region.getLeft();
						max += region.getRight();
						numRanges++;
					}
				}
			}
			if (numRanges > 0) {
				min = min / numRanges;
				max = max / numRanges;
			}

			ArrayList<String> tracks = new ArrayList<>();
			for (TierGlyph track : ServiceManager.getInstance().getService().getSelectedTierGlyphs()) {
				if (StyledGlyph.class.isInstance(track)) {
					tracks.add(((StyledGlyph) track).getAnnotStyle().getTrackName());
				}
			}
			if (tracks.isEmpty()) {
				tracks.add("element" + numNewElement);
				numNewElement++;
			}
			for (String datasetId : tracks) {

				switch (trackCommands[selectionTypeList.getSelectedIndex()]) {
				case SourcePattern.COMMAND_TRACK_NEGATIVE:
					SourcePattern.getInstance().addNegativeMatchDatasetIds(datasetId,
							SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
					break;
				case SourcePattern.COMMAND_TRACK_LOOP:
					SourcePattern.getInstance().addLoopsDatasetIds(datasetId,
							SourcePattern.DEFAULT_LOOP_CONTACT_DISTANCE_ALLOWED);
					break;
				case SourcePattern.COMMAND_TRACK_VALID_AREA:
					SourcePattern.getInstance().addValidAreaDatasetIds(datasetId,
							SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
					break;
				case SourcePattern.COMMAND_TRACK_PERFECT:
					SourcePattern.getInstance().addPerfectMatchDatasetIds(datasetId);
					break;
				case SourcePattern.COMMAND_TRACK_PARTIAL:
					SourcePattern.getInstance().addPartialMatchDatasetIds(datasetId,
							SourcePattern.DEFAULT_PARTIAL_MATCH_SCORE);
				}
				
				ArrayList<QueryRegion> rangesPartial = new ArrayList<>();
				QueryRegion regionPartial = new QueryRegion(datasetId, min, max, getStrand(BOTH_STRANDS), null);
				rangesPartial.add(regionPartial);
				SourcePattern.getInstance().setRegionsSpecificDataset(datasetId, rangesPartial);
			
			}
		} else {
			// Symmetries
			// Check attributes
			IgbService igbService = ServiceManager.getInstance().getService();
			for (TierGlyph t : igbService.getAllTierGlyphs()) {
				if (TierGlyph.TierType.ANNOTATION.equals(t.getTierType())) {
					for (SeqSymmetry sym : t.getSelected()) {
						SeqSpan span = sym.getSpan(0);
						String name = t.getAnnotStyle().getTrackName();

						switch (trackCommands[selectionTypeList.getSelectedIndex()]) {
						case SourcePattern.COMMAND_TRACK_NEGATIVE:
							SourcePattern.getInstance().addNegativeMatchDatasetIds(name,
									SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
							break;
						case SourcePattern.COMMAND_TRACK_LOOP:
							SourcePattern.getInstance().addLoopsDatasetIds(name,
									SourcePattern.DEFAULT_LOOP_CONTACT_DISTANCE_ALLOWED);
							break;
						case SourcePattern.COMMAND_TRACK_VALID_AREA:
							SourcePattern.getInstance().addValidAreaDatasetIds(name,
									SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
							break;
						case SourcePattern.COMMAND_TRACK_PERFECT:
							SourcePattern.getInstance().addPerfectMatchDatasetIds(name);
							break;
						case SourcePattern.COMMAND_TRACK_PARTIAL:
							SourcePattern.getInstance().addPartialMatchDatasetIds(name,
									SourcePattern.DEFAULT_PARTIAL_MATCH_SCORE);
							;
						}

							// Only for positive match
							ArrayList<QueryRegion> rl = new ArrayList<>();
							SourcePattern.getInstance().setRegionsSpecificDataset(name, rl);

							QueryRegion region = new QueryRegion(name, span.getMin(), span.getMax(),
									getStrand(BOTH_STRANDS), null);
							// Check attributes
							if (false == SKIP_ATTRIBUTES && SymWithProps.class.isInstance(sym)) {
								logger.info("SymWithProps, look for attributes");
								SymWithProps symWithProps = ((SymWithProps) sym);
								for (String key : symWithProps.getProperties().keySet()) {
									Object value = symWithProps.getProperties().get(key);
									if (String.class.isInstance(value)) {
										// double ?
										try {
											region.addAttribute(key, Double.parseDouble((String) value));
											logger.info("Add attributes in source: " + key);
										} catch (NumberFormatException nbe) {
											nbe.printStackTrace();// Not a
																	// double,
											// ignore
										}
									} else if (Number.class.isInstance(value)) {
										region.addAttribute(key, ((Number) value).doubleValue());
										logger.info("Add attributes in source: " + key);
									}
								}
							}
							SourcePattern.getInstance().getRegionsSpecificDataset(name).add(region);
						}
				}
			}
		}
		patternTable.updateTable();
	}

	/**
	 * 
	 * Add selected tracks or symmetries
	 * 
	 */
	public void addTSS() {
		// region: average of all other ones
		int min = 0;
		int max = 0;
		int numRanges = 0;
		for (String datasetId : SourcePattern.getInstance().getAllDatasetIds()) {
			if (null != SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
					min += region.getLeft();
					max += region.getRight();
					numRanges++;
				}
			}
		}
		if (numRanges > 0) {
			min = min + (max - min) / numRanges;
			max = min;
		}
		SourcePattern.getInstance().getPerfectMatchDatasetIds().add(TSS_IGB_TRACK);
		QueryRegion region = new QueryRegion(TSS_IGB_TRACK, min, max, getStrand(BOTH_STRANDS), null);
		ArrayList<QueryRegion> ranges = new ArrayList<>();
		ranges.add(region);
		SourcePattern.getInstance().setRegionsSpecificDataset(TSS_IGB_TRACK, ranges);
		patternTable.updateTable();
		// loadPattern(SourcePattern.getInstance());
	}

	/**
	 * 
	 * Add selected tracks or symmetries
	 * 
	 */
	public void addAllTracks() {
		// region: average of all other ones
		int min = 0;
		int max = 0;
		int numRanges = 0;
		for (String datasetId : SourcePattern.getInstance().getAllDatasetIds()) {
			if (null != SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
					min += region.getLeft();
					max += region.getRight();
					numRanges++;
				}
			}
		}
		if (numRanges > 0) {
			min = min + (max - min) / numRanges;
			max = min;
		}

		for (String datasetId : SourcePattern.getInstance().getAvailableDatasets()) {
			
			switch (trackCommands[selectionTypeList.getSelectedIndex()]) {
			case SourcePattern.COMMAND_TRACK_NEGATIVE:
				SourcePattern.getInstance().addNegativeMatchDatasetIds(datasetId,
						SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
				break;
			case SourcePattern.COMMAND_TRACK_LOOP:
				SourcePattern.getInstance().addLoopsDatasetIds(datasetId,
						SourcePattern.DEFAULT_LOOP_CONTACT_DISTANCE_ALLOWED);
				break;
			case SourcePattern.COMMAND_TRACK_VALID_AREA:
				SourcePattern.getInstance().addValidAreaDatasetIds(datasetId,
						SourcePattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
				break;
			case SourcePattern.COMMAND_TRACK_PERFECT:
				SourcePattern.getInstance().addPerfectMatchDatasetIds(datasetId);
				break;
			case SourcePattern.COMMAND_TRACK_PARTIAL:
				SourcePattern.getInstance().addPartialMatchDatasetIds(datasetId,
						SourcePattern.DEFAULT_PARTIAL_MATCH_SCORE);
			}
			
			ArrayList<QueryRegion> rangesPartial = new ArrayList<>();
			QueryRegion regionPartial = new QueryRegion(datasetId, min, max, getStrand(BOTH_STRANDS), null);
			rangesPartial.add(regionPartial);
			SourcePattern.getInstance().setRegionsSpecificDataset(datasetId, rangesPartial);
		
			patternTable.updateTable();
		}
	}

	/**
	 * 
	 * Add selected tracks or symmetries
	 * 
	 */
	public void addMotif(String motif) {
		// region: average of all other ones
		int min = 0;
		int max = 0;
		int numRanges = 0;
		for (String datasetId : SourcePattern.getInstance().getAllDatasetIds()) {
			if (null != SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
					min += region.getLeft();
					max += region.getRight();
					numRanges++;
				}
			}
		}
		if (numRanges > 0) {
			min = min + (max - min) / numRanges;
			max = min;
		}
		SourcePattern.getInstance().getPerfectMatchDatasetIds().add(motif);
		QueryRegion region = new QueryRegion(motif, min, max, getStrand(BOTH_STRANDS), null);
		ArrayList<QueryRegion> ranges = new ArrayList<>();
		ranges.add(region);
		SourcePattern.getInstance().setRegionsSpecificDataset(motif, ranges);
		patternTable.updateTable();
	}

	public void clear() {
		SourcePattern.getInstance().clear();
		patternTable.updateTable();
	}

	public void updateName(String datasetId, String value) {
		logger.info("Update " + datasetId + " -> " + value);
		if (SourcePattern.getInstance().getAllDatasetIds().contains(value)) {
			JOptionPane.showMessageDialog(this,
					"The selected dataset " + datasetId + " already exists. Please choose another name.");
			return;
		}
		SourcePattern.getInstance().updateName(datasetId, value);
		patternTable.updateTable();
	}

	public void updateType(String datasetId, TrackTypeElement value) {
		SourcePattern.getInstance().updateType(datasetId, value);
		logger.info("Update " + datasetId + " -> " + value.getType());
		/**
		 * 
		 * TODO: set score
		 * 
		 */
		patternTable.updateTable();
	}

	private static final GenometryModel gmodel = GenometryModel.getInstance();

	public void newPattern() {
		HashMap<String, List<QueryRegion>> regions = new HashMap<>();
		SourcePattern.getInstance().loadPattern(new Pattern(regions, new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>(), new ArrayList<>()));
		DefaultTableModel model = (DefaultTableModel) patternTable.getModel();
		model.setRowCount(0);
		model.fireTableDataChanged();
		patternTable.repaint();
	}

	public void loadDatasetsAsPattern() {
		refreshDatasets();
		HashMap<String, List<QueryRegion>> regions = new HashMap<>();
		List<String> perfectMatchDatasetIds = new ArrayList<>();
		if (null == gmodel.getSelectedGenomeVersion()) {
			return;
		}
		for (DataContainer dc : gmodel.getSelectedGenomeVersion().getAvailableDataContainers()) {
			for (DataSet dataSet : dc.getDataSets()) {
				if (dataSet.isVisible() && false == "RefGene".equals(dataSet.getDataSetName())
						&& false == "Cytobands".equals(dataSet.getDataSetName())) {
					String name = dataSet.getDataSetName();
					if (false == perfectMatchDatasetIds.contains(name)) {
						perfectMatchDatasetIds.add(name);
						ArrayList<QueryRegion> rl = new ArrayList<>();
						regions.put(name, rl);
					}
					QueryRegion region = new QueryRegion(name, 0, 0, getStrand(BOTH_STRANDS), null);
					regions.get(name).add(region);
				}
			}
		}
		Pattern pattern = new Pattern(regions, perfectMatchDatasetIds, new ArrayList<>(), new ArrayList<>(),
				new ArrayList<>(), new ArrayList<>());
		SourcePattern.getInstance().loadPattern(pattern);
		patternTable.updateTable();
	}

	public void loadDatasetsAsPattern(String[] datasetIds) {
		refreshDatasets();

		SourcePattern.getInstance().clear();

		for (String datasetId : datasetIds) {
			ArrayList<QueryRegion> ranges = new ArrayList<>();

			SourcePattern.getInstance().getPerfectMatchDatasetIds().add(datasetId);
			QueryRegion region = new QueryRegion(datasetId, 0, 0, getStrand(BOTH_STRANDS), null);
			ranges.add(region);
			SourcePattern.getInstance().setRegionsSpecificDataset(datasetId, ranges);
		}

		patternTable.updateTable();

	}

	public void refreshDatasets() {
		// Check that the column exists (i.e. the table is not empty)
		if (patternTable.getColumnModel() != null
				&& patternTable.getColumnModel().getColumnCount() >= PatternTableModel.COLUMN_TRACK) {
			String[] datasets = SourcePattern.getInstance().getAvailableDatasets();
		}
	}

	public void loadFile() {
		JFileChooser fc = new JFileChooser("./Patterns");
		FileNameExtensionFilter filter = new FileNameExtensionFilter("TXT file", new String[] { "txt" });
		fc.addChoosableFileFilter(filter);
		fc.setFileFilter(filter);
		int returnState = fc.showDialog(this, "Select Pattern");
		if (returnState == JFileChooser.APPROVE_OPTION) {
			try {
				loadFile(new FileInputStream(fc.getSelectedFile()));
				labelEditor.setText(fc.getSelectedFile().getName());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void loadFile(InputStream is) {
		Pattern pattern = IOHandler.readPattern(is);
		SourcePattern.getInstance().loadPattern(pattern);
		patternTable.updateTable();
	}

	private HashMap<String, Color> patternColors = new HashMap<>();

	private String[] getDefaultPatterns() {
		ArrayList<String> patterns = new ArrayList<>();
		URL url = getClass().getResource("/patterns/pattern_list.txt");
		InputStream is;
		try {
			is = url.openStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));
			String line;
			while ((line = br.readLine()) != null) {
				String patternName = line.split(" ")[0].replace(".txt", "");
				patterns.add(patternName);
				String colorRGB[] = line.split(" ")[1].split(",");
				patternColors.put(patternName, new Color(Integer.parseInt(colorRGB[0]), Integer.parseInt(colorRGB[1]),
						Integer.parseInt(colorRGB[2])));
			}
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return patterns.toArray(new String[patterns.size()]);
	}

	class EditAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final JTable table;

		protected EditAction(JTable table) {
			super("edit");
			this.table = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			// this.table.getCellEditor().stopCellEditing();
			int row = table.convertRowIndexToModel(table.getEditingRow());
			String oldName = (String) patternTable.getModel().getValueAt(row, PatternTableModel.COLUMN_DATASET_ID);
			JTextField field1 = new JTextField(oldName);
			JPanel panel = new JPanel(new GridLayout(0, 1));
			panel.add(new JLabel("New name:"));
			panel.add(field1);
			int result = JOptionPane.showConfirmDialog(null, panel, "Test", JOptionPane.OK_CANCEL_OPTION,
					JOptionPane.PLAIN_MESSAGE);
			if (result == JOptionPane.OK_OPTION) {
				updateName(oldName, field1.getText());
			} else {
				System.out.println("Cancelled");
			}
		}
	}

	public class AttributeTableCellRenderer extends JTextField implements TableCellRenderer {
		public AttributeTableCellRenderer() {
			super();
			this.addMouseListener(new MouseListener() {
				@Override
				public void mouseClicked(MouseEvent e) {
					refreshDatasets();
				}

				@Override
				public void mousePressed(MouseEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void mouseReleased(MouseEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					// TODO Auto-generated method stub
				}

				@Override
				public void mouseExited(MouseEvent e) {
					// TODO Auto-generated method stub
				}
			});
		}

		/**
		
		 * 
		
		 */
		private static final long serialVersionUID = 1L;

		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
				int row, int column) {
			String dataset = (String) patternTable.getValueAt(row, PatternTableModel.COLUMN_TRACK);
			ArrayList<String> attributes = new ArrayList<>();
			for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(dataset)) {
				for (String attribute : region.getAttributes().keySet()) {
					attributes.add(attribute + ": \"" + region.getAttributeValue(attribute) + "\"");
				}
			}
			this.setText(String.join(";", attributes));
			return this;
		}
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// patternTable.updateTable();
	}

	public void showPattern() {
		TopkResult k = new TopkResult();
		SourcePattern pattern = SourcePattern.getInstance();
		HashMap<String, Double[]> scoreMap = new HashMap<>();
		for (String datasetId : pattern.getPerfectMatchDatasetIds()) {
			ArrayList<Region> regions = new ArrayList<Region>();
			Double[] scores = new Double[pattern.getRegionsSpecificDataset(datasetId).size()];
			int i = 0;
			for (Region region : pattern.getRegionsSpecificDataset(datasetId)) {
				regions.add(region);
				scores[i] = new Double(1);
				i++;
			}
			k.addPerfectMatchDataset(datasetId, new Dataset(datasetId, regions, new HashMap<String, String>()));
			scoreMap.put(datasetId, scores);
		}
		for (String datasetId : pattern.getPartialMatchDatasetIds()) {
			ArrayList<Region> regions = new ArrayList<Region>();
			Double[] scores = new Double[pattern.getRegionsSpecificDataset(datasetId).size()];
			int i = 0;
			for (Region region : pattern.getRegionsSpecificDataset(datasetId)) {
				regions.add(region);
				scores[i] = pattern.getPartialMatchScore(datasetId);
				i++;
			}
			k.addPartialMatchDataset(datasetId, new Dataset(datasetId, regions, new HashMap<String, String>()));
			scoreMap.put(datasetId, scores);
		}
		k.setRegionSimilarities(scoreMap);
		k.setAlignmentScores(scoreMap);
		ArrayList<TopkResult> results = new ArrayList<>();
		results.add(k);
		List<Image> resultsImage = ResultImagesGenerator.getResultsImage(pattern, results, true, 1, 1);
		Image img = resultsImage.get(0);
		Double newWidth = 300.0;
		Double newHeight = img.getHeight(null) * newWidth / img.getWidth(null);
		JLabel label = new JLabel();
		label.setIcon(new ImageIcon(
				img.getScaledInstance(newWidth.intValue(), newHeight.intValue(), java.awt.Image.SCALE_SMOOTH)));// your
		// image
		JFrame f = new JFrame("Pattern");
		f.setSize(new Dimension(300, 300));
		f.add(label);
		f.setVisible(true);
	}

	public void focus() {
		((JTabbedPane) this.getParent()).setSelectedIndex(0);
	}

	/** Source: http://stackoverflow.com/a/11034405 */
	public class JMultilineLabel extends JTextArea {
		private static final long serialVersionUID = 1L;

		public JMultilineLabel(String text) {
			super(text);
			setEditable(false);
			setCursor(null);
			setOpaque(false);
			setFocusable(false);
			setFont(UIManager.getFont("Label.font"));
			setWrapStyleWord(true);
			setLineWrap(true);
		}
	}
}
