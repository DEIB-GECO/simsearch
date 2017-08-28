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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;

/**
 * @author Arnaud Ceol
 */
public class StrandEditor extends AbstractCellEditor implements TableCellEditor, TableCellRenderer {

	private static final long serialVersionUID = 1L;
	protected transient ChangeEvent changeEvent;

	public final static String LABEL_PLUS_STRAND = "+";
	public final static String LABEL_MINUS_STRAND = "-";

	private JPanel component;

	JCheckBox plusStrand = new JCheckBox(LABEL_PLUS_STRAND);
	JCheckBox minusStrand = new JCheckBox(LABEL_MINUS_STRAND);

	JTable jTable;

	protected StrandEditor(JTable jTable) {
		super();

		this.jTable = jTable;

		component = new JPanel();
		component.setLayout(new GridLayout(0, 6));

		// mini
		plusStrand.putClientProperty("JComponent.sizeVariant", "mini");
		// mini
		minusStrand.putClientProperty("JComponent.sizeVariant", "mini");

		component.add(plusStrand);
		component.add(new JLabel(LABEL_PLUS_STRAND));
		component.add(minusStrand);
		component.add(new JLabel(LABEL_MINUS_STRAND));

		plusStrand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});

		minusStrand.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				fireEditingStopped();
			}
		});
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (isSelected) {
			component.setBackground(table.getSelectionBackground());
		} else {
			component.setBackground(table.getBackground());
		}

		if (value != null) {
			setSelected((boolean[]) value);
		}
		return component;
	}

	private void setSelected(boolean[] value) {
		plusStrand.setSelected(value[0]);
		minusStrand.setSelected(value[1]);
	}

	@Override
	public Object getCellEditorValue() {
		boolean[] selectedStrands = new boolean[2];
		
		selectedStrands[0] = plusStrand.isSelected();
		selectedStrands[1] = minusStrand.isSelected();	
		
		return selectedStrands;
	}

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {

		if (isSelected) {
			component.setBackground(table.getSelectionBackground());
		} else {
			component.setBackground(table.getBackground());
		}

		if (value != null) {
			setSelected((boolean[]) value);
		}

		return component;
	}

}