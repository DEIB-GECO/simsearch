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
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;
import it.unibo.disi.simsearch.core.model.Pattern;
import it.unibo.disi.simsearch.core.model.QueryRegion;
import it.unibo.disi.simsearch.core.model.QueryRegion.AttributeMapping;
import it.unibo.disi.simsearch.core.model.Region;

/**
 * @author Arnaud Ceol
 */
public class PatternTable extends JTable {
	
	private static Logger logger = Logger.getLogger(PatternTable.class.getName());	

	private double DEFAULT_PARTIAL_SCORE = 0.5;

	private static final long serialVersionUID = 1L;


	public PatternTable() {
		super(new PatternTableModel());

		getColumnModel().getColumn(PatternTableModel.COLUMN_STRAND).setPreferredWidth(120);
		getColumnModel().getColumn(PatternTableModel.COLUMN_STRAND).setMinWidth(120);
		getColumnModel().getColumn(PatternTableModel.COLUMN_STRAND).setMaxWidth(120);
		getColumnModel().getColumn(PatternTableModel.COLUMN_TYPE).setPreferredWidth(250);
		getColumnModel().getColumn(PatternTableModel.COLUMN_TYPE).setMinWidth(250);
		getColumnModel().getColumn(PatternTableModel.COLUMN_TYPE).setMaxWidth(250);
		getColumnModel().getColumn(PatternTableModel.COLUMN_ACTION).setPreferredWidth(90);
		getColumnModel().getColumn(PatternTableModel.COLUMN_ACTION).setMinWidth(90);
		getColumnModel().getColumn(PatternTableModel.COLUMN_ACTION).setMaxWidth(90);
		getColumnModel().getColumn(PatternTableModel.COLUMN_RANGE).setPreferredWidth(150);
		getColumnModel().getColumn(PatternTableModel.COLUMN_RANGE).setMinWidth(150);
		getColumnModel().getColumn(PatternTableModel.COLUMN_RANGE).setMaxWidth(150);

		setRowHeight(30);
		setFillsViewportHeight(true);

	}

	public void updateTable() {

		String[] availableDatasets = SourcePattern.getInstance().getAvailableDatasets();

		DefaultTableModel model = (DefaultTableModel) getModel();

		model.setRowCount(0);

		for (String datasetId : SourcePattern.getInstance().getAllDatasetIds()) {

			logger.info("Load pattern dataset: " + datasetId);
			String ranges = "";
			if (null != SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
				if (SourcePattern.getInstance().getNegativeMatchDatasetIds().contains(datasetId)) {
					if (null != SourcePattern.getInstance().getNegativeMatchDistance(datasetId)) {
						ranges = "" + SourcePattern.getInstance().getNegativeMatchDistance(datasetId);
					} else {
						ranges = "" + Pattern.DEFAULT_NEGATIVE_MATCH_DISTANCE;
					}
				} else if (SourcePattern.getInstance().getLoopsDatasetIds().contains(datasetId)) {
					if (null != SourcePattern.getInstance().getLoopDistance(datasetId)) {
						ranges = "" + SourcePattern.getInstance().getLoopDistance(datasetId);
					} else {
						ranges = "" + Pattern.DEFAULT_NEGATIVE_MATCH_DISTANCE;
					}
				} else if (SourcePattern.getInstance().getValidAreaDatasetIds().contains(datasetId)) {
					if (null != SourcePattern.getInstance().getValidAreaDistance(datasetId)) {
						ranges = "" + SourcePattern.getInstance().getValidAreaDistance(datasetId);
					} else {
						ranges = "" + Pattern.DEFAULT_NEGATIVE_MATCH_DISTANCE;
					}
				} else {
					for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(datasetId)) {
						if (false == ranges.equals("")) {
							ranges += ";";
						}

						ranges += region.getLeft();

						if (region.getRight() != region.getLeft()) {
							ranges += "-" + region.getRight();
						}
					}
				}
			} else {
				logger.info("Null regions for : " + datasetId);
			}

			String method = null;
			if (SourcePattern.getInstance().getPerfectMatchDatasetIds().contains(datasetId)) {
				method = SourcePattern.COMMAND_TRACK_PERFECT;
			} else if (SourcePattern.getInstance().getPartialMatchDatasetIds().contains(datasetId)) {
				method = SourcePattern.COMMAND_TRACK_PARTIAL;
			} else if (SourcePattern.getInstance().getNegativeMatchDatasetIds().contains(datasetId)) {
				method = SourcePattern.COMMAND_TRACK_NEGATIVE;
			} else if (SourcePattern.getInstance().getLoopsDatasetIds().contains(datasetId)) {
				method = SourcePattern.COMMAND_TRACK_LOOP;
			} else if (SourcePattern.getInstance().getValidAreaDatasetIds().contains(datasetId)) {
				method = SourcePattern.COMMAND_TRACK_VALID_AREA;
			}

			String dataset = "";
			if (Arrays.asList(availableDatasets).contains(datasetId)) {
				dataset = datasetId;
			} else if (availableDatasets.length > 0) {
				// find the best match
				for (String availableDataset : availableDatasets) {
					if (availableDataset.toLowerCase().contains(datasetId.toLowerCase())) {
						dataset = availableDataset;
						break;
					}
				}
				
				// MOTIF??
				if ("".equals(dataset) && datasetId.matches("[ACGTacgt]+")) {
					dataset = TargetDatasetsSelectionPanel.MOTIF_TRACK;
				}				
				
				if ("".equals(dataset)) {
					dataset = availableDatasets[0];
				}
			}

			ArrayList<String> attributes = new ArrayList<>();
			if (false == TargetDatasetsSelectionPanel.SKIP_ATTRIBUTES
					&& null != SourcePattern.getInstance().getRegionsSpecificDataset(dataset)) {
				for (Region region : SourcePattern.getInstance().getRegionsSpecificDataset(dataset)) {
					for (String attribute : region.getAttributes().keySet()) {
						attributes.add(attribute + ",," + region.getAttributeValue(attribute) + ",1.0");
					}
				}
			}

			boolean[] selectedtrands = { true, true };

			model.addRow(new Object[] { null, datasetId, new TrackTypeElement(method, DEFAULT_PARTIAL_SCORE), ranges,
					dataset, selectedtrands, String.join(";", attributes) });
		}

		model.fireTableDataChanged();
		repaint();

		logger.info(SourcePattern.getInstance().toString());

	}

	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		switch (column) {
		case PatternTableModel.COLUMN_TYPE:
			return new MenubarColumnRenderer();
		case PatternTableModel.COLUMN_ACTION:
			return new ButtonsRenderer();
		case PatternTableModel.COLUMN_STRAND:
			return new StrandEditor(this);
		case PatternTableModel.COLUMN_TRACK:
			return new DefaultTableCellRenderer();
		case PatternTableModel.COLUMN_ATTRIBUTE:

			return new DefaultTableCellRenderer() {

				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					Pattern sourcePattern = SourcePattern.getInstance();

					String datasetId = (String) getModel().getValueAt(row, PatternTableModel.COLUMN_DATASET_ID);

					ArrayList<String> regionMappings = new ArrayList<>();

					if (null != sourcePattern.getRegionsSpecificDataset(datasetId)) {
						for (QueryRegion region : sourcePattern.getRegionsSpecificDataset(datasetId)) {
							ArrayList<String> mappings = new ArrayList<>();

							for (String attributeName : region.getAttributeNames()) {

								AttributeMapping mapping = region.getMapping(attributeName);

								if (false == "".equals(mapping.getTargetAttribute())) {
									mappings.add(mapping.getTargetAttribute());
								}
							}
							regionMappings.add(String.join("; ", mappings));
						}
					}

					String output = "";

					int i = 0;
					for (String regionMapping : regionMappings) {
						i++;

						if ("".equals(regionMapping)) {
							continue;
						}

						if (false == "".equals(output)) {
							output += " ";
						}

						if (regionMappings.size() > 1) {
							output += "Region " + i + ": ";
						}

						output += regionMapping;
					}

					return new JLabel(output);
				}
			};

		default:
			return super.getCellRenderer(row, column);
		}
	}

	@Override
	public TableCellEditor getCellEditor(int row, int column) {
		switch (column) {
		case PatternTableModel.COLUMN_TYPE:
			return new MenubarColumnEditor();		
		case PatternTableModel.COLUMN_ACTION:
			return new ButtonsEditor(this);
		case PatternTableModel.COLUMN_STRAND:
			StrandEditor strand = new StrandEditor(this);
			strand.addCellEditorListener(this);
			return strand;
		case PatternTableModel.COLUMN_TRACK:
			// get type: 
			String datasetId =  (String) getModel().getValueAt(row, PatternTableModel.COLUMN_DATASET_ID);
			if (datasetId.startsWith("MOTIF:")) {
				String[] motifDatasets = {TargetDatasetsSelectionPanel.MOTIF_TRACK};
				return new DefaultCellEditor(new JComboBox<String>(motifDatasets));
			} else {
				return new DefaultCellEditor(new JComboBox<String>((SourcePattern.getInstance().getAvailableDatasets())));
			}
		default:
			return super.getCellEditor(row, column);
		}
	}

	@Override
	public String getToolTipText(MouseEvent event) {

		int row = rowAtPoint(event.getPoint());
		int column = columnAtPoint(event.getPoint());

		String value = "";

		if (column > 0) {
			try {
				value = getValueAt(row, column).toString();
			} catch (Exception e) {
				value = ""; 
			}
		}
		return value;
	}


	public int getRow(String datasetId) {
		for (int rowIndex = 0; rowIndex < getModel().getRowCount(); rowIndex++) {
			if (datasetId.equals(getModel().getValueAt(rowIndex, PatternTableModel.COLUMN_DATASET_ID))) {
				return rowIndex;
			}
		}
		return -1;
	}

}
