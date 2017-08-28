package it.iit.genomics.cru.simsearch.bundle.view.pattern;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import it.iit.genomics.cru.simsearch.bundle.model.SourcePattern;

/**
 * from : http://stackoverflow.com/questions/8854841/jslider-in-jtable
 */
public class MenubarColumnEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {

	private static final long serialVersionUID = 1L;

	String[] typesString = { SourcePattern.COMMAND_TRACK_PERFECT, SourcePattern.COMMAND_TRACK_PARTIAL,
			SourcePattern.COMMAND_TRACK_NEGATIVE, SourcePattern.COMMAND_TRACK_LOOP,
			SourcePattern.COMMAND_TRACK_VALID_AREA };

	private TrackTypeElement typeElement;

	@Override
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {

		if (value instanceof TrackTypeElement) {
			typeElement = (TrackTypeElement) value;
		}

		JComboBox<String> combotype = new JComboBox<>(typesString);

		combotype.setSelectedItem(typeElement.getType());
		combotype.addActionListener(this);

		if (isSelected) {
			combotype.setBackground(table.getSelectionBackground());
			combotype.setForeground(table.getSelectionForeground());
		} else {
			combotype.setBackground(table.getBackground());
			combotype.setForeground(table.getForeground());
		}

		return combotype;
	}

	@Override
	public Object getCellEditorValue() {
		return this.typeElement;
	}

	@Override
	public void actionPerformed(ActionEvent event) {
		JComboBox<String> comboType = (JComboBox<String>) event.getSource();
		this.typeElement = new TrackTypeElement((String) comboType.getSelectedItem(), 0.1);
		fireEditingStopped();		
	}

}