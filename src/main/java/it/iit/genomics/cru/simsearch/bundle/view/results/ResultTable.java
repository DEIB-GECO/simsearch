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
import java.awt.Component;
import java.util.Collection;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableRowSorter;

import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * @author Arnaud Ceol
 */
public class ResultTable extends JTable {

	private static final long serialVersionUID = 1L;

	/**
	 * Color associated to each positive match of the pattern. 
	 */
	private Collection<Color> colors;
	
	public ResultTable() {
		super(new ResultsTableModel(
				new Object[] { "Result", "Score", "Position", "Length", "Nearest gene", "Distance", "Up/Downstream", "Sim. scores" }, 0));

		getColumnModel().getColumn(ResultsTableModel.COLUMN_RESULT_ID).setPreferredWidth(50);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_RESULT_ID).setMinWidth(50);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_RESULT_ID).setMaxWidth(50);
		
		getColumnModel().getColumn(ResultsTableModel.COLUMN_LENGTH).setPreferredWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_LENGTH).setMinWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_LENGTH).setMaxWidth(90);
		
		getColumnModel().getColumn(ResultsTableModel.COLUMN_SIMSCORE).setPreferredWidth(50);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_SIMSCORE).setMinWidth(50);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_SIMSCORE).setMaxWidth(50);

		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TSS).setPreferredWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TSS).setMinWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TSS).setMaxWidth(90);

		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TYPE).setPreferredWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TYPE).setMinWidth(90);
		getColumnModel().getColumn(ResultsTableModel.COLUMN_DISTANCE_TYPE).setMaxWidth(90);
		
		TableRowSorter<ResultsTableModel> rowSorter = new TableRowSorter<ResultsTableModel>(
				(ResultsTableModel) getModel());
		setRowSorter(rowSorter);
		rowSorter.setComparator(2, new GenomePositionComparator());
	}
	
	@Override
	public TableCellRenderer getCellRenderer(int row, int column) {
		switch (column) {
		case ResultsTableModel.COLUMN_SCORES:
			
			return new DefaultTableCellRenderer() {

				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					
					TopkResult result = (TopkResult) getModel().getValueAt(row, ResultsTableModel.COLUMN_SCORES);
					
					return new ScoresGraphicalPanel(result);
				}
			};

		default:
			return super.getCellRenderer(row, column);
		}
	}

}
