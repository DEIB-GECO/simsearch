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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import com.affymetrix.genometry.GenometryModel;
import com.affymetrix.genometry.general.DataSet;

import it.iit.genomics.cru.simsearch.bundle.view.pattern.TargetDatasetsSelectionPanel;
import it.iit.genomics.cru.simsearch.bundle.view.pattern.TrackTypeElement;
import it.unibo.disi.simsearch.core.model.Pattern;

/**
 * Extends the Pattern class adding methods to edit it.
 * 
 * @author Arnaud Ceol
 *
 */
public class SourcePattern extends Pattern {

	private static final long serialVersionUID = 1L;

	public static final String COMMAND_TRACK_PERFECT = "perfect";
	public static final String COMMAND_TRACK_PARTIAL = "partial";
	public static final String COMMAND_TRACK_NEGATIVE = "negative";
	public static final String COMMAND_TRACK_VALID_AREA = "valid area";
	public static final String COMMAND_TRACK_LOOP = "loops";

	public final static String[] defaultDatasets = { TargetDatasetsSelectionPanel.MOTIF_TRACK, TargetDatasetsSelectionPanel.TSS_IGB_TRACK, TargetDatasetsSelectionPanel.SPLICE_SITE_IGB_TRACK};
	
	
	private PropertyChangeSupport changes = new PropertyChangeSupport(this);

	private HashMap<String, Collection<String>> datasetToAttributes;

	private HashMap<String, DataSet> datasets;

	private HashMap<String, String> inputDatasetsSources;

	private static SourcePattern instance = null;

	public static boolean SKIP_ATTRIBUTES = true;

	private SourcePattern() {
		super();
		datasetToAttributes = new HashMap<>();
		datasets = new HashMap<>();
		inputDatasetsSources = new HashMap<>();
	}

	public static SourcePattern getInstance() {
		if (instance == null) {
			instance = new SourcePattern();
		}
		return instance;
	}

	public HashMap<String, Collection<String>> getDatasetToAttributes() {
		return datasetToAttributes;
	}

	public HashMap<String, DataSet> getDatasets() {
		return datasets;
	}

	public HashMap<String, String> getInputDatasetsSources() {
		return inputDatasetsSources;
	}

	public String[] getAvailableDatasets() {
		datasets.clear();
		inputDatasetsSources.clear();

		Optional.ofNullable(GenometryModel.getInstance().getSelectedGenomeVersion())
				.ifPresent(selectedGenomeVersion -> {
					selectedGenomeVersion.getAvailableDataContainers().stream().flatMap(dc -> dc.getDataSets().stream())
							.filter(dataSet -> dataSet.isVisible())
							.filter(dataSet -> false == "RefGene".equals(dataSet.getDataSetName()) && false == "RefSeq Curated".equals(dataSet.getDataSetName())
									&& false == "Cytobands".equals(dataSet.getDataSetName()))
							.forEach(dataSet -> {
								datasets.put(dataSet.getDataSetName(), dataSet);
								inputDatasetsSources.put(dataSet.getDataSetName(), dataSet.getMethod());
								//

								//// if (false ==
								//// datasetToAttributes.containsKey(dataSet.getDataSetName()))
								//// {
								// datasetToAttributes.put(dataSet.getDataSetName(),
								//// getAttributes(dataSet));
								//// }
							});
				});

		datasets.put(TargetDatasetsSelectionPanel.TSS_IGB_TRACK, null);

		datasets.put(TargetDatasetsSelectionPanel.SPLICE_SITE_IGB_TRACK, null);
		
		datasets.put(TargetDatasetsSelectionPanel.MOTIF_TRACK, null);

		return datasets.keySet().toArray(new String[datasets.keySet().size()]);
	}


	/**
	 * Exclude predefined datasets like TSS or residue pattern
	 * @return
	 */
	public String[] getAvailableTrackDatasets() {
		
		ArrayList<String> trackDatasets = new ArrayList<>();
		
		
		for (String dataset: this.getAvailableDatasets()) {
			if (false == Arrays.asList(defaultDatasets).contains(dataset)) {
				trackDatasets.add(dataset);
			}
		}
		
		return trackDatasets.toArray(new String[trackDatasets.size()]);
	}
	
	
	public void loadPattern(Pattern pattern) {

		clear();

		for (String datasetId : pattern.getNegativeMatchDatasetIds()) {
			instance.addNegativeMatchDatasetIds(datasetId, pattern.getNegativeMatchDistance(datasetId));
		}

		for (String datasetId : pattern.getLoopsDatasetIds()) {
			instance.addLoopsDatasetIds(datasetId, pattern.getLoopDistance(datasetId));
		}

		for (String datasetId : pattern.getPartialMatchDatasetIds()) {
			instance.addPartialMatchDatasetIds(datasetId, pattern.getPartialMatchScore(datasetId));
		}

		for (String datasetId : pattern.getAllDatasetIds()) {
			instance.regions.put(datasetId, pattern.getRegionsSpecificDataset(datasetId));
		}

		instance.getPerfectMatchDatasetIds().addAll(pattern.getPerfectMatchDatasetIds());
		instance.getValidAreaDatasetIds().addAll(pattern.getValidAreaDatasetIds());

		instance.datasetToAttributes = new HashMap<>();
		instance.datasets = new HashMap<>();

		changes.firePropertyChange("pattern", false, true);
	}

	public void updateName(String datasetId, String value) {

		setRegionsSpecificDataset(value, getRegionsSpecificDataset(datasetId));

		if (getNegativeMatchDatasetIds().contains(datasetId)) {
			addNegativeMatchDatasetIds(value, getNegativeMatchDistance(datasetId));
			removeNegativeMatchDatasetIds(datasetId);
		} else if (getPartialMatchDatasetIds().contains(datasetId)) {
			addPartialMatchDatasetIds(value, getPartialMatchScore(datasetId));
			removePartialMatchDatasetIds(datasetId);
		} else if (getPerfectMatchDatasetIds().contains(datasetId)) {
			removePerfectMatchDatasetIds(datasetId);
			addPerfectMatchDatasetIds(value);
		}

		changes.firePropertyChange("name", false, true);
	}

	public void updateType(String datasetId, TrackTypeElement value) {

		if (value.getType().equals(COMMAND_TRACK_PERFECT)) {
			if (false == getPerfectMatchDatasetIds().contains(datasetId)) {
				addPerfectMatchDatasetIds(datasetId);
			}
			removePartialMatchDatasetIds(datasetId);
			removeNegativeMatchDatasetIds(datasetId);
			removeValidAreaDatasetIds(datasetId);
			removeLoopsDatasetIds(datasetId);
		} else if (value.getType().equals(COMMAND_TRACK_VALID_AREA)) {
			removePerfectMatchDatasetIds(datasetId);
			removePartialMatchDatasetIds(datasetId);
			removeNegativeMatchDatasetIds(datasetId);
			if (false == getValidAreaDatasetIds().contains(datasetId)) {
				addValidAreaDatasetIds(datasetId, 0);
			}
			removeLoopsDatasetIds(datasetId);
		} else if (value.getType().equals(COMMAND_TRACK_PARTIAL)) {
			removePerfectMatchDatasetIds(datasetId);
			if (false == getPartialMatchDatasetIds().contains(datasetId)) {
				addPartialMatchDatasetIds(datasetId, value.getScore()); // Pattern.DEFAULT_PARTIAL_MATCH_SCORE
			}
			removeNegativeMatchDatasetIds(datasetId);
			removeValidAreaDatasetIds(datasetId);
			removeLoopsDatasetIds(datasetId);
		} else if (value.getType().equals(COMMAND_TRACK_NEGATIVE)) {
			removePerfectMatchDatasetIds(datasetId);
			removePartialMatchDatasetIds(datasetId);
			if (false == getNegativeMatchDatasetIds().contains(datasetId)) {
				addNegativeMatchDatasetIds(datasetId, Pattern.DEFAULT_NEGATIVE_MATCH_DISTANCE);
			}
			removeValidAreaDatasetIds(datasetId);
			removeLoopsDatasetIds(datasetId);
		} else if (value.getType().equals(COMMAND_TRACK_LOOP)) {
			removePerfectMatchDatasetIds(datasetId);
			removePartialMatchDatasetIds(datasetId);
			removeNegativeMatchDatasetIds(datasetId);
			removeValidAreaDatasetIds(datasetId);
			if (false == getLoopsDatasetIds().contains(datasetId)) {
				addLoopsDatasetIds(datasetId, Pattern.DEFAULT_LOOP_CONTACT_DISTANCE_ALLOWED);
			}
		}

		changes.firePropertyChange("type", false, true);
	}

	public void addPropertyChangeListener(PropertyChangeListener l) {
		changes.addPropertyChangeListener(l);
	}

	public void removePropertyChangeListener(PropertyChangeListener l) {
		changes.removePropertyChangeListener(l);
	}

	public String getDatasetType(String datasetId) {

		if (perfectMatchDatasetIds.contains(datasetId)) {
			return COMMAND_TRACK_PERFECT;
		}
		if (partialMatchDatasetIds.containsKey(datasetId)) {
			return COMMAND_TRACK_PARTIAL;
		}
		if (negativeMatchDatasetIds.containsKey(datasetId)) {
			return COMMAND_TRACK_NEGATIVE;
		}
		if (validAreaDatasetIds.containsKey(datasetId)) {
			return COMMAND_TRACK_VALID_AREA;
		}

		return null;
	}

	public void removeDatasetIds(String datasetId) {
		SourcePattern.getInstance().removePerfectMatchDatasetIds(datasetId);		
		SourcePattern.getInstance().removePartialMatchDatasetIds(datasetId);
		SourcePattern.getInstance().removeNegativeMatchDatasetIds(datasetId);
		SourcePattern.getInstance().removeLoopsDatasetIds(datasetId);
		SourcePattern.getInstance().removeValidAreaDatasetIds(datasetId);
	}

}
