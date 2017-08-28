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

import javax.swing.table.DefaultTableModel;

/**
 * @author Arnaud Ceol
 */
public class PatternTableModel extends DefaultTableModel {

	private static final long serialVersionUID = 1L;

	public static final int COLUMN_ACTION = 0;
	public static final int COLUMN_DATASET_ID = 1;
	public static final int COLUMN_TYPE = 2;
	public static final int COLUMN_RANGE = 3;
	public static final int COLUMN_TRACK = 4;
	public static final int COLUMN_STRAND = 5;
	public static final int COLUMN_ATTRIBUTE = 6;

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case COLUMN_TYPE:
			return TrackTypeElement.class;
		case COLUMN_STRAND:
			return double[].class;
		default:
			return String.class;
		}
		
	}
	
	public PatternTableModel() {
		super(new Object[] { "", "Dataset ID", "Type", "Distance/Range", "Target Dataset", "strand", "Attributes" }, 0);		
	}
	
	
    @Override
    public boolean isCellEditable(int row, int col) {
    	switch(col) {
    		case COLUMN_DATASET_ID:
    			return true;
    		case COLUMN_STRAND:
    			return true;
    		case COLUMN_TYPE:
    			return true;
    		case COLUMN_RANGE:
    			return true;
    		case COLUMN_TRACK:
    			return true;
    		case COLUMN_ACTION:
    			return true;
    		case COLUMN_ATTRIBUTE:
    			return false;
    		default:
    			return true;
    	}
    }

    
    
}
