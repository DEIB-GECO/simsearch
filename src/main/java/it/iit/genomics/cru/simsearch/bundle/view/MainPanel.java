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
package it.iit.genomics.cru.simsearch.bundle.view;

import java.awt.BorderLayout;

import javax.swing.JTabbedPane;

import org.lorainelab.igb.services.window.tabs.IgbTabPanel;
import org.lorainelab.igb.services.window.tabs.IgbTabPanelI;

import aQute.bnd.annotation.component.Component;
import it.iit.genomics.cru.simsearch.bundle.view.pattern.TargetDatasetsSelectionPanel;


/**
 * @author Arnaud Ceol
 */
@Component(name = MainPanel.COMPONENT_NAME, provide = IgbTabPanelI.class, immediate = true)
public final class MainPanel extends IgbTabPanel {

	public static final String COMPONENT_NAME = "PE";
	
	private static final long serialVersionUID = 1L;

	private final static JTabbedPane tabbedPan = new  JTabbedPane();


	public static JTabbedPane getTabbedPan() {
		return tabbedPan;
	}
	
	public MainPanel() throws Exception {
		super("SimSearch", "SimSearch", null, false, 8);

		getContentPane().setLayout(new BorderLayout(10, 1));

		TargetDatasetsSelectionPanel targetDatasets = TargetDatasetsSelectionPanel.getInstance();
		tabbedPan.add("Search", targetDatasets);

		this.add(tabbedPan, BorderLayout.CENTER);

		this.pack();

	}

}
