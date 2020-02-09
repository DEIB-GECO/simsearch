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
import java.awt.event.ActionEvent;
import java.util.EventObject;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;

/**
 * @author Arnaud Ceol
 */
public class ButtonsEditor extends ButtonsPanel implements TableCellEditor {

	private static final long serialVersionUID = 1L;
	protected transient ChangeEvent changeEvent;
	private final PatternTable table;

	protected ButtonsEditor(PatternTable table) {
		super();
		this.table = table;
		
		buttons.get(0).setAction(new RemoveAction(table));
		buttons.get(1).setAction(new EditAttributesAction(table));
	}

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row,
			int column) {
		this.setBackground(table.getSelectionBackground());
		return this;
	}

	@Override
	public Object getCellEditorValue() {
		return "";
	}

	@Override
	public boolean isCellEditable(EventObject e) {
		return true;
	}

	@Override
	public boolean shouldSelectCell(EventObject anEvent) {
		return true;
	}

	@Override
	public boolean stopCellEditing() {
		fireEditingStopped();
		return true;
	}

	@Override
	public void cancelCellEditing() {
		fireEditingCanceled();
	}

	@Override
	public void addCellEditorListener(CellEditorListener l) {
		listenerList.add(CellEditorListener.class, l);
	}

	@Override
	public void removeCellEditorListener(CellEditorListener l) {
		listenerList.remove(CellEditorListener.class, l);
	}

	public CellEditorListener[] getCellEditorListeners() {
		return listenerList.getListeners(CellEditorListener.class);
	}

	protected void fireEditingStopped() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CellEditorListener.class) {
				// Lazily create the event:
				try {
					if (Objects.isNull(changeEvent)) {
						changeEvent = new ChangeEvent(this);
					}
					((CellEditorListener) listeners[i + 1]).editingStopped(changeEvent);
				} catch (Exception e) {
					// one ow has been cancelled, don't worry
				}
			}
		}
	}

	protected void fireEditingCanceled() {
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();
		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == CellEditorListener.class) {
				// Lazily create the event:
				if (Objects.isNull(changeEvent)) {
					changeEvent = new ChangeEvent(this);
				}
				((CellEditorListener) listeners[i + 1]).editingCanceled(changeEvent);
			}
		}
	}
	

	public void removeDataset(String datasetId) {
		SourcePattern.getInstance().removeDatasetIds(datasetId);
		table.updateTable();
	}

	
	class RemoveAction extends AbstractAction {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private final JTable table;

		protected RemoveAction(JTable table) {
			super("Remove");
			this.table = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.convertRowIndexToModel(table.getEditingRow());

			String datasetId = (String) table.getModel().getValueAt(row, PatternTableModel.COLUMN_DATASET_ID);
			removeDataset(datasetId);
			ButtonsEditor.this.cancelCellEditing();

		}
	}

	class EditAttributesAction extends AbstractAction {
		private static final long serialVersionUID = 1L;
		private final JTable table;

		protected EditAttributesAction(JTable table) {
			super("edit");
			this.table = table;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			int row = table.convertRowIndexToModel(table.getEditingRow());

			JFrame attributeFrame = new JFrame();
			attributeFrame.setSize(600, 500);
			attributeFrame.setVisible(true);
			attributeFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

			AttributeEditorPanel pane = new AttributeEditorPanel((PatternTable)table, row, attributeFrame);

			attributeFrame.add(pane);
			attributeFrame.setVisible(true);
		}
	}
	
	
	
}