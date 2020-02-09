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

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.affymetrix.genometry.BioSeq;
import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.general.DataSet;
import com.affymetrix.genometry.symloader.SymLoader;
import com.affymetrix.genometry.symmetry.SymWithProps;
import com.affymetrix.genometry.symmetry.impl.SeqSymmetry;
import com.affymetrix.genometry.util.ServerUtils;
import com.google.common.collect.HashMultimap;

import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;
import it.unibo.disi.simsearch.core.model.QueryRegion;
import it.unibo.disi.simsearch.core.model.QueryRegion.AttributeMapping;

/**
 * @author Arnaud Ceol
 */
public class AttributeEditorPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static Logger logger = Logger.getLogger(AttributeEditorPanel.class.getName());	
	
	private static HashMultimap<String, String> datasetAttributes = HashMultimap.create();

	public AttributeEditorPanel(PatternTable patternTable, int row, JFrame frame) {
		super();

		/**
		 * Keep elements in array, in order to give access to it form the submit
		 * button
		 */
		ArrayList<String> attributes = new ArrayList<>();
		ArrayList<JComboBox<String>> comboBoxes = new ArrayList<>();
		ArrayList<JTextField> valueFields = new ArrayList<>();
		ArrayList<JTextField> weightFields = new ArrayList<>();
		ArrayList<QueryRegion> regions = new ArrayList<>();

		NumberFormat integerNumberInstance = NumberFormat.getNumberInstance();

		JPanel mainPanel = new JPanel();

		BoxLayout layout = new BoxLayout(mainPanel, BoxLayout.Y_AXIS);

		mainPanel.setLayout(layout);

		mainPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
		add(mainPanel);

		String datasetId = (String) patternTable.getModel().getValueAt(row, PatternTableModel.COLUMN_DATASET_ID);
		String trackName = (String) patternTable.getModel().getValueAt(row, PatternTableModel.COLUMN_TRACK);

		// Get attributes
		if (false == TargetDatasetsSelectionPanel.TSS_IGB_TRACK.equals(trackName)
				&& false == TargetDatasetsSelectionPanel.MOTIF_TRACK.equals(trackName)
				&& false == SourcePattern.getInstance().getDatasetToAttributes().containsKey(trackName)) {
			SourcePattern.getInstance().getDatasetToAttributes().put((String) trackName,
					getAttributes(SourcePattern.getInstance().getDatasets().get((String) trackName)));
		}

		// If not attributes for the query region and dataset == track
		if (SourcePattern.getInstance().getRegionsSpecificDataset(datasetId).isEmpty() && datasetId.equals(trackName)) {
			for (QueryRegion region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				for (String attribute : SourcePattern.getInstance().getDatasetToAttributes().get(datasetId)) {
					region.addAttribute(attribute, 1.0);
				}
			}
		}

		int i = 1;
		for (QueryRegion region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
			mainPanel.add(new JLabel("Region " + i + ":"));
			i++;

			if (region.getAttributeNames().isEmpty()) {
				mainPanel.add(new JLabel("No attributes."));
			} else {

				GridLayout optionsLayout = new GridLayout(region.getAttributesNumber() + 1, 4);
				optionsLayout.setVgap(4);
				optionsLayout.setHgap(2);
				JPanel panel = new JPanel(optionsLayout);

				panel.setAlignmentX(Component.CENTER_ALIGNMENT);

				if (false == region.getAttributeNames().isEmpty()) {
					panel.add(new JLabel(""));
					panel.add(new JLabel("Target attribute"));
					panel.add(new JLabel("Max. expected value"));
					panel.add(new JLabel("Weight"));
				}

				for (String attributeName : region.getAttributeNames()) {

					// map to
					JComboBox<String> comboBox = new JComboBox<>();
					comboBox.addItem("");

					AttributeMapping mapping = region.getMapping(attributeName);

					if (false == SourcePattern.getInstance().getDatasetToAttributes().containsKey(trackName)) {
						continue;
					}
					
					for (String available : SourcePattern.getInstance().getDatasetToAttributes().get(trackName)) {
						comboBox.addItem(available);
					}
					comboBox.setSelectedItem(mapping.getTargetAttribute());

					// expected value
					JTextField fieldExpectedValue = new JTextField(); // JFormattedTextField(integerNumberInstance);
					fieldExpectedValue.setText("" + mapping.getExpectedValue());

					// weight
					JFormattedTextField fieldWeight = new JFormattedTextField(integerNumberInstance);
					fieldWeight.setText("" + mapping.getWeight());

					panel.add(new JLabel(attributeName));
					panel.add(comboBox);
					panel.add(fieldExpectedValue);
					panel.add(fieldWeight);

					regions.add(region);
					attributes.add(attributeName);
					comboBoxes.add(comboBox);
					valueFields.add(fieldExpectedValue);
					weightFields.add(fieldWeight);

					mainPanel.add(panel);
				}
			}
			mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		}

		JButton autoFill = new JButton();

		autoFill.setAction(new AbstractAction("Auto fill") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				for (int i = 0; i < comboBoxes.size(); i++) {
					String attribute = attributes.get(i);
					JComboBox<String> comboBox = comboBoxes.get(i);

					for (int j = 0; j < comboBox.getItemCount(); j++) {
						if (attribute.equals(comboBox.getItemAt(j))) {
							comboBox.setSelectedIndex(j);
							valueFields.get(i)
									.setText(SourcePattern.getInstance().getAttributeValue(datasetId, attribute) + "");
							break;
						}
					}
				}
			}

		});

		JButton submit = new JButton();

		submit.setAction(new AbstractAction("Submit") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {

				for (int i = 0; i < comboBoxes.size(); i++) {
					String attribute = attributes.get(i);
					QueryRegion region = regions.get(i);
					JComboBox<String> comboBox = comboBoxes.get(i);
					JTextField value = valueFields.get(i);
					JTextField weight = valueFields.get(i);

					AttributeMapping mapping = region.getMapping(attribute);
					mapping.setTargetAttribute((String) comboBox.getSelectedItem());
					mapping.setExpectedValue(Double.parseDouble(value.getText()));
					mapping.setWeight(Double.parseDouble(weight.getText()));
				}
				patternTable.updateUI();
				frame.dispose();
			}

		});

		JButton close = new JButton();

		close.setAction(new AbstractAction("Close") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}

		});

		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(autoFill);
		buttonPanel.add(close);
		buttonPanel.add(submit);
		mainPanel.add(buttonPanel);

		mainPanel.add(new JPanel());

	}

	private Collection<String> getAttributes(DataSet dataSet) {
		ArrayList<String> attributes = new ArrayList<>();

		SymLoader symLoader = ServerUtils.determineLoader(SymLoader.getExtension(dataSet.getURI()), dataSet.getURI(),
				Optional.empty(), DataSet.detemineFriendlyName(dataSet.getURI()),
				GenometryModel.getInstance().getSelectedGenomeVersion());

		/**
		 * Get first symmetry only
		 */
		for (BioSeq bioSeq : GenometryModel.getInstance().getSelectedGenomeVersion().getSeqList()) {
			try {
				logger.info("Seq: " + bioSeq.getId());
				if (symLoader.getChromosome(bioSeq) != null && symLoader.getChromosome(bioSeq).size() > 0) {

					SeqSymmetry firstSym = symLoader.getChromosome(bioSeq).get(0);

					if (false == TargetDatasetsSelectionPanel.SKIP_ATTRIBUTES
							&& SymWithProps.class.isInstance(firstSym)) {
						SymWithProps sym = ((SymWithProps) firstSym);
						for (String key : sym.getProperties().keySet()) {
							if (String.class.isInstance(sym.getProperties().get(key))) {
								String value = (String) sym.getProperties().get(key);
								// double ?
								try {
									Double.parseDouble(value);
									attributes.add(key);
								} catch (NumberFormatException nbe) {
									// Not a double, ignore
								}
							} else if (Number.class.isInstance(sym.getProperties().get(key))) {
								attributes.add(key);
							}
						}
					}

					logger.info("Get attributes for: " + dataSet.getDataSetName());
					if (false == TargetDatasetsSelectionPanel.SKIP_ATTRIBUTES && attributes.isEmpty()
							&& firstSym.getChildCount() > 0 && SymWithProps.class.isInstance(firstSym.getChild(0))) {
						for (String key : ((SymWithProps) firstSym.getChild(0)).getProperties().keySet()) {
							if (String.class
									.isInstance(((SymWithProps) firstSym.getChild(0)).getProperties().get(key))) {
								String value = (String) ((SymWithProps) firstSym.getChild(0)).getProperties().get(key);
								// double ?

								try {
									Double.parseDouble(value);
									attributes.add(key);
									logger.info("Add attributes: " + key);

								} catch (NumberFormatException nbe) {
									// Not a double, ignore
								}
							} else if (Number.class
									.isInstance(((SymWithProps) firstSym.getChild(0)).getProperties().get(key))) {
								attributes.add(key);

								logger.info("Add attributes: " + key);
							}

						}
					}
					logger.info("# attributes: " + attributes.size());
					return attributes;
				}
			} catch (Exception ex) {
				logger
						.severe(ex.getMessage() + ". Feature " + symLoader.getFeatureName() + ", " + bioSeq.getId());
			}
		}

		return attributes;

	}

}