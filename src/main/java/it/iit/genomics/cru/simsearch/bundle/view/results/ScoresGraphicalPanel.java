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

import java.awt.Color;
import java.awt.Dimension;
import java.text.DecimalFormat;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import it.iit.genomics.cru.simsearch.bundle.model.ColorPalette;
import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * @author Arnaud Ceol
 */
public class ScoresGraphicalPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	DecimalFormat df = new DecimalFormat("#.###");

	public ScoresGraphicalPanel(TopkResult result) {

		super();

		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

		int datasetIndex = 0;
		
		for (String datasetId : result.getPositiveMatchDatasetIds()) {
			Color color  = ColorPalette.getInstance().getColor(datasetIndex) ;
			
			datasetIndex++;
			for (Double score : result.getAlignmentScores().get(datasetId)) {
				Color c = score == null ? Color.WHITE : color; //new Color(1 - score.floatValue(), 0, score.floatValue());
				JLabel label = new JLabel("" + datasetIndex, SwingConstants.CENTER);
				if (score != null) {
					label.setForeground(Color.WHITE);
				}
				label.setBorder(BorderFactory.createLineBorder(Color.WHITE, 2));
				
				// height: depends from score:
				int height = score != null ? Math.max(2, (int) (20 * score)) : 1;
				Dimension dimension =  new Dimension(20, height);
				label.setPreferredSize(dimension);
				label.setMinimumSize(dimension);
				label.setMaximumSize(dimension);
				label.setOpaque(true);
				label.setBackground(c);
				if (score == null) {
					label.setToolTipText(datasetId + ": NA");
				} else {
					label.setToolTipText(datasetId + ": " + df.format(score));
				}

				this.add(label);
			}
		}
	

	}

}
