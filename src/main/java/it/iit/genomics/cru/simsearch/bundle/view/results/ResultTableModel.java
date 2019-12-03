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

import javax.swing.table.DefaultTableModel;

/**
 * @author Arnaud Ceol
 */
class ResultsTableModel extends DefaultTableModel {

	public static final int COLUMN_RESULT_ID = 0;
	public static final int COLUMN_SIMSCORE = 1;
	public static final int COLUMN_POSITION = 2;
	public static final int COLUMN_LENGTH = 3;
	public static final int COLUMN_GENE = 4;
	public static final int COLUMN_DISTANCE_TSS = 5;
	public static final int COLUMN_DISTANCE_TYPE = 6;
	public static final int COLUMN_SCORES = 7;
	
	public ResultsTableModel(Object[] columnNames, int rowCount) {
		super(columnNames, rowCount);
		// TODO Auto-generated constructor stub
	}

	private static final long serialVersionUID = 1L;

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COLUMN_RESULT_ID:
			return Integer.class;
		case COLUMN_SIMSCORE:
			return Double.class;
		case COLUMN_POSITION:
			return String.class;
		case COLUMN_DISTANCE_TYPE:
			return String.class;
		default:
			return Integer.class;
		}
	}

	@Override
	public boolean isCellEditable(int row, int col) {
		return false;
	}

}