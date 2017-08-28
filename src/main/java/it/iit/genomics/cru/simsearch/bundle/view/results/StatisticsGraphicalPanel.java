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
import java.awt.event.ActionEvent;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;

import it.iit.genomics.cru.simsearch.bundle.model.ColorPalette;
import it.iit.genomics.cru.simsearch.bundle.model.ResultStatistics;
import it.iit.genomics.cru.simsearch.bundle.view.pattern.TargetDatasetsSelectionPanel;

/**
 * @author Arnaud Ceol
 */
public class StatisticsGraphicalPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	

	Dimension dimension = new Dimension(20, 20);
	
	public StatisticsGraphicalPanel(Collection<String> datasetIds, Collection<String> partialMatchDatasetIds, ResultStatistics statistics) {

		super();
		
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		JPanel legendPanel = new JPanel();
		
		legendPanel.setLayout(new BoxLayout(legendPanel, BoxLayout.Y_AXIS));
				
		int datasetIndex = 0;
		
		JPanel rowPanel;// = new JPanel();
		
		legendPanel.add(new JLabel("Datasets: "));

		/**
		 * Show legend
		 */
		for (String datasetId : datasetIds) {
			
			rowPanel = new JPanel();
			
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
			
			datasetIndex++;
			
			JLabel label = new JLabel("" + datasetIndex, SwingConstants.CENTER); 
			label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
			
			label.setPreferredSize(dimension);
			label.setMinimumSize(dimension);
			label.setMaximumSize(dimension);
			label.setOpaque(true);
			label.setBackground(ColorPalette.getInstance().getColor(datasetIndex -1));
			label.setForeground(Color.WHITE);

			rowPanel.add(label);

			legendPanel.add(rowPanel);
			
			rowPanel.add(new JLabel(" " + datasetId + " (" + statistics.getNumberOfResults(datasetId) + " results)"));
			
			rowPanel.add(Box.createHorizontalGlue());			
		}
		
		rowPanel = new JPanel();
		legendPanel.add(rowPanel);
		
		this.add(legendPanel, BorderLayout.WEST);

		JPanel statsPanel = new JPanel();
		
		statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));
		
		
		for (String key : statistics.getKeysOrderByCount()) {
			rowPanel = new JPanel();
			
			rowPanel.setBackground(Color.white);
			
			rowPanel.setLayout(new BoxLayout(rowPanel, BoxLayout.X_AXIS));
			
			datasetIndex = 0;

			for (String datasetId : datasetIds) {
				datasetIndex++;

				List<String> matchDatasets = Arrays.stream(key.split("#")).collect(Collectors.toList());
				
				Color bg = matchDatasets.contains(datasetId) ? ColorPalette.getInstance().getColor(datasetIndex - 1): Color.WHITE;
				Color fg = Color.WHITE;

				JLabel label = new JLabel("" + datasetIndex, SwingConstants.CENTER); 
				label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
				
				label.setPreferredSize(dimension);
				label.setMinimumSize(dimension);
				label.setMaximumSize(dimension);
				label.setOpaque(true);
				label.setBackground(bg);
				label.setForeground(fg);

				rowPanel.add(label);
			}

			JLabel count = new JLabel( key.split("#").length + " positive datasets, " + statistics.getStatistics().get(key) + " results");
			
			Color bg = Color.WHITE;

			count.setBackground(bg);
			count.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
			count.setOpaque(true);
			rowPanel.add(count, Component.RIGHT_ALIGNMENT);

			LinkButton addPattern = new LinkButton("select as pattern");
			
			addPattern.setAction(
				new AbstractAction("create pattern") {
					private static final long serialVersionUID = 1L;

					@Override
					public void actionPerformed(ActionEvent e) {						
						TargetDatasetsSelectionPanel.getInstance().loadDatasetsAsPattern(key.split("#"));	
						TargetDatasetsSelectionPanel.getInstance().focus();
					}
			});
			
			
			rowPanel.add(addPattern);
			
			rowPanel.add(Box.createHorizontalGlue());
			
			statsPanel.add(rowPanel);
		} 
		this.add(
				new JScrollPane(statsPanel), BorderLayout.CENTER);

	}

	public static void main(String[] args) {
		ResultStatistics statistics = new ResultStatistics(null, null);

		String[] datasetIds = { "a", "b", "c", "d" };

		String[][] results = { { "a", "b", "c" }, { "a", "b", "d" }, { "a", "b", "c", "d" }, { "a", "b", "c" },
				{ "a", "b", "d" }, { "a", "b", "c" }, { "a", "b" }, { "a" }, };

		for (String[] result : results) {
			statistics.addResult(Arrays.stream(result).collect(Collectors.toList()), 1);
		}

		statistics.collapse();

		final JFrame frame = new JFrame("Test stats");

		JPanel statsPanel = new StatisticsGraphicalPanel(null, Arrays.stream(datasetIds).collect(Collectors.toList()),
				statistics);

		frame.add(statsPanel);

		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.setSize(550, 200);
		frame.setVisible(true);

	}

}
