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

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;

import it.iit.genomics.cru.simsearch.bundle.model.SimSearchParameters;
import it.unibo.disi.simsearch.core.business.distances.RegionsDistanceCalculatorCentroidAttribute;
import it.unibo.disi.simsearch.core.business.distances.RegionsDistanceCalculatorCentroidAttributeNormalized;
import it.unibo.disi.simsearch.core.business.distances.RegionsDistanceCalculatorRightLeftAttributes;
import it.unibo.disi.simsearch.core.business.regionComparator.RegionsComparatorCentroidAttribute;
import it.unibo.disi.simsearch.core.business.regionComparator.RegionsComparatorLeftAttribute;

import it.unibo.disi.simsearch.core.model.Pattern;

/**
 * @author Arnaud Ceol
 */
public class ParametersPanel extends JPanel implements ActionListener {

	private static final long serialVersionUID = 1L;

	// private JTextField k;
	private JTextField kScoreThreshold;

	private JTextField perfectMatchThreshold;

	private JTextField partialMatchDefaultThreshold;

	private JComboBox<String> distanceMismatchStrategyComboBox;

	private JComboBox<String> distanceScoreStrategyComboBox;

	private JTextField midpoint;
	private JTextField slope;

	private JTextField maxPeakLength;

	// private JCheckBox isRegionSimilarityRelevant;

	private JCheckBox isRegionLengthRelevant;

	private JCheckBox useDiversity;

	private JMultilineLabel regionLengthWeightLabel;
	private JMultilineLabel regionLengthExpectedMaxLabel;

	private JTextField regionLengthWeight;
	private JTextField regionLengthExpectedMax;

	private final static String DISTANCE_CENTROID = "Centroid";
	private final static String DISTANCE_RIGHT_LEFT_EXTREMES = "Right/left extremes";
	private final static String DISTANCE_CENTROID_NORMALIZED = "Centroid/Normalized";

	private boolean isRegionSimilarityRelevant = false;

	private static ParametersPanel instance;

	public static ParametersPanel getInstance() {
		if (instance == null) {
			instance = new ParametersPanel();
		}
		return instance;
	}

	private ParametersPanel() {

		this.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.NORTH;
		c.weighty = 1;

		Dimension dimension = new Dimension(80, Integer.MAX_VALUE);
		this.setMaximumSize(dimension);

		kScoreThreshold = new JTextField();
		kScoreThreshold.setText("0.5");
		kScoreThreshold.setColumns(4);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(new JMultilineLabel("Minimum similarity score of results ([0..1]) "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(kScoreThreshold, c);

		perfectMatchThreshold = new JTextField();
		perfectMatchThreshold.setText("0.5");
		perfectMatchThreshold.setColumns(4);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(new JMultilineLabel("Minimum alignment score for perfect match ([0..1]) "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(perfectMatchThreshold, c);

		partialMatchDefaultThreshold = new JTextField();
		partialMatchDefaultThreshold.setText("0.5");
		partialMatchDefaultThreshold.setColumns(4);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(new JMultilineLabel("Score for missing partial match ([0..1]) "), c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(partialMatchDefaultThreshold, c);

		JMultilineLabel distancebasedonlabel = new JMultilineLabel("Distance between regions based on:     ");

		distanceScoreStrategyComboBox = new JComboBox<String>();
		distanceScoreStrategyComboBox.addItem(DISTANCE_CENTROID);
		distanceScoreStrategyComboBox.addItem(DISTANCE_RIGHT_LEFT_EXTREMES);
		distanceScoreStrategyComboBox.addItem(DISTANCE_CENTROID_NORMALIZED);
		distanceScoreStrategyComboBox.setSelectedIndex(0);

		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(distancebasedonlabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(distanceScoreStrategyComboBox, c);

		JMultilineLabel distance_score_based_on_label = new JMultilineLabel("Distance mismatch computed using: ");
		distanceMismatchStrategyComboBox = new JComboBox<String>();
		distanceMismatchStrategyComboBox.addItem("L1");
		distanceMismatchStrategyComboBox.addItem("L2");
		distanceMismatchStrategyComboBox.setSelectedIndex(0);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(distance_score_based_on_label, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(distanceMismatchStrategyComboBox, c);

		JMultilineLabel param_midpoint_label = new JMultilineLabel("Alignment score sigmoid mid-point");
		// (i.e.,
		// the
		// distance
		// mismatch
		// yielding
		// a
		// 0.5
		// alignment
		// score)
		// ");
		midpoint = new JTextField();
		midpoint.setText("1000");
		midpoint.setColumns(10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(param_midpoint_label, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(midpoint, c);

		JMultilineLabel param_slope_label = new JMultilineLabel("Alignment score sigmoid slope");// (i.e.,
																									// steepness
																									// of
																									// the
																									// sigmoid)
																									// ");
		slope = new JTextField();
		slope.setText("0.001");
		slope.setColumns(10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(param_slope_label, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(slope, c);

		useDiversity = new JCheckBox("Use diversity? ");
		useDiversity.setSelected(true);
		useDiversity.addActionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;
		add(useDiversity, c);

		// isRegionSimilarityRelevant = new JCheckBox("Is region similarity
		// relevant (i.e., regions' attributes are considered) ? ");
		// isRegionSimilarityRelevant.setSelected(false);
		// isRegionSimilarityRelevant.addActionListener(this);
		//
		// c.gridwidth = GridBagConstraints.REMAINDER;
		// add(isRegionSimilarityRelevant, c);

		isRegionLengthRelevant = new JCheckBox("Is region length relevant for computing region similarity? ");
		isRegionLengthRelevant.setSelected(false);
		isRegionLengthRelevant.addActionListener(this);

		c.gridwidth = GridBagConstraints.REMAINDER;
		add(isRegionLengthRelevant, c);

		regionLengthWeightLabel = new JMultilineLabel(
				"Region length weight in computing region similarity (in [0..1]) ");

		regionLengthWeight = new JTextField();
		regionLengthWeight.setText("1.0");
		regionLengthWeight.setColumns(10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(regionLengthWeightLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(regionLengthWeight, c);

		regionLengthExpectedMaxLabel = new JMultilineLabel("Region length expected maximum value ");

		regionLengthExpectedMax = new JTextField();
		regionLengthExpectedMax.setText("1000");
		regionLengthExpectedMax.setColumns(10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(regionLengthExpectedMaxLabel, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(regionLengthExpectedMax, c);

		regionLengthWeightLabel.setEnabled(false);
		regionLengthExpectedMaxLabel.setEnabled(false);

		regionLengthWeight.setEnabled(false);
		regionLengthExpectedMax.setEnabled(false);

		c.gridheight = GridBagConstraints.REMAINDER;


		
		JMultilineLabel param_maxpeakLength_label = new JMultilineLabel("Max peak length (regions larger will be splitted)");// (i.e.,
	
		maxPeakLength = new JTextField();
		maxPeakLength.setText("0");
		maxPeakLength.setColumns(10);
		c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 2.0;
		add(param_maxpeakLength_label, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(maxPeakLength, c);

		c.gridheight = GridBagConstraints.REMAINDER;

		add(new JPanel(), c);

	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == isRegionLengthRelevant) {
			if (isRegionLengthRelevant.isSelected()) {
				regionLengthWeightLabel.setEnabled(true);
				regionLengthExpectedMaxLabel.setEnabled(true);

				regionLengthWeight.setEnabled(true);
				regionLengthExpectedMax.setEnabled(true);
			} else {
				regionLengthWeightLabel.setEnabled(false);
				regionLengthExpectedMaxLabel.setEnabled(false);

				regionLengthWeight.setEnabled(false);
				regionLengthExpectedMax.setEnabled(false);
			}

		}

		// if (e.getSource() == isRegionSimilarityRelevant) {
		// if (isRegionSimilarityRelevant.isSelected()) {
		//
		// isRegionLengthRelevant.setEnabled(true);
		//
		// if (isRegionLengthRelevant.isSelected()) {
		// regionLengthWeightLabel.setEnabled(true);
		// regionLengthExpectedMaxLabel.setEnabled(true);
		//
		// regionLengthWeight.setEnabled(true);
		// regionLengthExpectedMax.setEnabled(true);
		// }
		// else {
		// regionLengthWeightLabel.setEnabled(false);
		// regionLengthExpectedMaxLabel.setEnabled(false);
		//
		// regionLengthWeight.setEnabled(false);
		// regionLengthExpectedMax.setEnabled(false);
		// }
		// }
		// else {
		//
		// isRegionLengthRelevant.setEnabled(false);
		//
		// regionLengthWeightLabel.setEnabled(false);
		// regionLengthExpectedMaxLabel.setEnabled(false);
		//
		// regionLengthWeight.setEnabled(false);
		// regionLengthExpectedMax.setEnabled(false);
		// }
		//
		// }
		//
		// if (e.getSource() == trackColour) {
		// Color newColor = JColorChooser.showDialog(
		// this,
		// "Choose Track Color",
		// trackColour.getBackground());
		// if (newColor != null) {
		// trackColour.setBackground(newColor);
		// }
		// }

	}

	public void setIsSimilarityRelevant(boolean isSimilarityRelevant) {
		this.isRegionSimilarityRelevant = isSimilarityRelevant;
		isRegionLengthRelevant.setEnabled(isSimilarityRelevant);
		regionLengthWeightLabel.setEnabled(isSimilarityRelevant);
		regionLengthExpectedMaxLabel.setEnabled(isSimilarityRelevant);

		regionLengthWeight.setEnabled(isSimilarityRelevant);
		regionLengthExpectedMax.setEnabled(isSimilarityRelevant);
	}

	public SimSearchParameters getParameters(Pattern pattern) {
		SimSearchParameters parameters = new SimSearchParameters();
		
		try {
			double param_threshold = Double.parseDouble(kScoreThreshold.getText());
			if (param_threshold < 0 || param_threshold > 1) {
				throw new NumberFormatException();
			}
			parameters.setSimilarityThreshold(param_threshold);
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter similarity threshold. It must be a numeric value between 0 and 1.");
			kScoreThreshold.requestFocus();
			return null;
		}

		try {
			double param_threshold = Double.parseDouble(perfectMatchThreshold.getText());
			if (param_threshold < 0 || param_threshold > 1) {
				throw new NumberFormatException();
			}
			parameters.setAlignmentPerfectMatchScoreThreshold(param_threshold);
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter similarity threshold. It must be a numeric value between 0 and 1.");
			perfectMatchThreshold.requestFocus();
			return null;
		}

		try {
			double param_threshold = Double.parseDouble(partialMatchDefaultThreshold.getText());
			if (param_threshold < 0 || param_threshold > 1) {
				throw new NumberFormatException();
			}
			parameters.setPartialMatchdefaultScore(param_threshold);
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter similarity threshold. It must be a numeric value between 0 and 1.");
			partialMatchDefaultThreshold.requestFocus();
			return null;
		}

		for (String datasetId : pattern.getPartialMatchDatasetIds()) { // int
																		// partialMatchDatasetIndex
																		// = 0;
																		// partialMatchDatasetIndex
																		// <
																		// pattern.getNumberOfPartialMatchDatasets();
																		// partialMatchDatasetIndex++)
																		// {
			// parameters.setPartialMatchScores(datasetId,
			// pattern.getPartialMatchScore(datasetId));
			/**
			 * TODO: we use the default one, latter we will restore the possibility to have
			 * one per dataset.
			 */
			parameters.setPartialMatchScores(datasetId, parameters.getPartialMatchdefaultScore());
		}

		for (String datasetId : pattern.getNegativeMatchDatasetIds()) { // int
																		// negativeMatchDatasetIndex
																		// = 0;
																		// negativeMatchDatasetIndex
																		// <
																		// pattern.getNumberOfNegativeMatchDatasets();
																		// negativeMatchDatasetIndex++)
																		// {
			parameters.setNegativeMatchLegalDistances(datasetId, pattern.getNegativeMatchDistance(datasetId));
		}

		for (String datasetId : pattern.getValidAreaDatasetIds()) { // int
			// negativeMatchDatasetIndex
			// = 0;
			// negativeMatchDatasetIndex
			// <
			// pattern.getNumberOfNegativeMatchDatasets();
			// negativeMatchDatasetIndex++)
			// {
			parameters.setValidAreaAllowedDistancesDistances(datasetId, pattern.getValidAreaDistance(datasetId));
		}

		if (distanceMismatchStrategyComboBox.getSelectedItem().toString().compareTo("L1") == 0) {
			parameters.setAlignmentScoreBasedOnL1();
		}
		if (distanceMismatchStrategyComboBox.getSelectedItem().toString().compareTo("L2") == 0) {
			parameters.setAlignmentScoreBasedOnL2();
		}

		if (DISTANCE_CENTROID.equals(this.distanceScoreStrategyComboBox.getSelectedItem())) {
			parameters.setRegionsComparator(new RegionsComparatorCentroidAttribute());
			parameters.setRegionsDistanceCalculator(new RegionsDistanceCalculatorCentroidAttribute());
		} else if (DISTANCE_RIGHT_LEFT_EXTREMES.equals(this.distanceScoreStrategyComboBox.getSelectedItem())) {
			parameters.setRegionsComparator(new RegionsComparatorLeftAttribute());
			parameters.setRegionsDistanceCalculator(new RegionsDistanceCalculatorRightLeftAttributes());
		} else {
			parameters.setRegionsComparator(new RegionsComparatorCentroidAttribute());
			parameters.setRegionsDistanceCalculator(new RegionsDistanceCalculatorCentroidAttributeNormalized());
		}

		try {
			int mp = Integer.parseInt(midpoint.getText());
			if (mp <= 0) {
				throw new NumberFormatException();
			}
			parameters.setDistanceMidpoint(mp);
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter sigmoid midpoint. It must be a positive integer value.");
			midpoint.requestFocus();
			return null;
		}

		try {
			parameters.setDistanceSlope(Double.parseDouble(slope.getText()));
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter sigmoid slope. It must be a numeric value.");
			slope.requestFocus();
			return null;
		}

		parameters.setDiversityUsed(useDiversity.isSelected());

		// if (!isRegionSimilarityRelevant.isSelected()) {
		// parameters.setRegionSimilarityRelevant(false);
		// }
		// else {
		// parameters.setRegionSimilarityRelevant(true);
		//
		if (false == isRegionLengthRelevant.isSelected()) {
			parameters.setRegionLengthWeight(0.0);
		} else {
			String param_region_length_weight_string = regionLengthWeight.getText();
			double param_region_length_weight;
			try {
				param_region_length_weight = Double.parseDouble(param_region_length_weight_string);
				if (param_region_length_weight < 0 || param_region_length_weight > 1) {
					throw new NumberFormatException();
				}
			} catch (NumberFormatException exc) {
				JOptionPane.showMessageDialog(this,
						"Please set a valid value for the parameter region length weight. It must be a numeric value between 0 and 1.");
				regionLengthWeight.requestFocus();
				return null;
			}
			parameters.setRegionLengthWeight(param_region_length_weight);

			if (param_region_length_weight != 0) {
				String param_expected_region_length_max_value_string = regionLengthExpectedMax.getText();
				int param_expected_region_length_max_value;
				try {
					param_expected_region_length_max_value = Integer
							.parseInt(param_expected_region_length_max_value_string);
					if (param_expected_region_length_max_value < 0) {
						throw new NumberFormatException();
					}
				} catch (NumberFormatException exc) {
					JOptionPane.showMessageDialog(this,
							"Please set a valid value for the parameter region length expected max value. It must be a positive integer value.");
					regionLengthExpectedMax.requestFocus();
					return null;
				}
				parameters.setExpectedRegionLengthMaxValue(param_expected_region_length_max_value);
			}
		}


		try {
			parameters.setMaxPeakLength(Integer.parseInt(maxPeakLength.getText()));
		} catch (NumberFormatException exc) {
			JOptionPane.showMessageDialog(this,
					"Please set a valid value for the parameter max peak length. It must be a positive integer value.");
			slope.requestFocus();
			return null;
		}

		return parameters;
	}

	// public void setTrackColor(Color color) {
	// trackColour.setBackground(color);
	// trackColour.setContentAreaFilled(false);
	// trackColour.setOpaque(true);
	// }

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

	public boolean isSimilarityRelevant() {
		return isRegionSimilarityRelevant;
	}
}
