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
import java.awt.GridLayout;
import java.util.Arrays;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.affymetrix.common.CommonUtils;

/**
 * @author Arnaud Ceol
 */
public class ButtonsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public final List<JButton> buttons = Arrays.asList(
			new JButton(CommonUtils.getInstance().getIcon("16x16/actions/delete.gif")),
			new JButton(CommonUtils.getInstance().getIcon("16x16/actions/equalizer.png")));

	protected Dimension buttonDimension = new Dimension(19, 19);

	protected ButtonsPanel() {
		super(new GridLayout(1, 3));

		buttons.get(0).setPreferredSize(buttonDimension);
		buttons.get(1).setPreferredSize(buttonDimension);

		buttons.get(1).setEnabled(ParametersPanel.getInstance().isSimilarityRelevant());
		
		setOpaque(true);
		for (JButton b : buttons) {
			b.setFocusable(false);
			b.setRolloverEnabled(false);
			add(b);
		}
	}

	@Override
	public void updateUI() {
		super.updateUI();
	}
}
