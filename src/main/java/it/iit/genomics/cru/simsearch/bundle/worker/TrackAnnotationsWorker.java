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
package it.iit.genomics.cru.simsearch.bundle.worker;

import java.awt.BorderLayout;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingWorker;
import javax.swing.table.DefaultTableModel;

import it.iit.genomics.cru.simsearch.bundle.utils.AnnotationsFromTrack;
import it.unibo.disi.simsearch.core.model.TopkResult;

/**
 * @author Arnaud Ceol
 */
public class TrackAnnotationsWorker extends SwingWorker<ArrayList<String>, String> {

	private static Logger logger = Logger.getLogger(TrackAnnotationsWorker.class.getName());

	String annotsFileName;

	String track;

	String label;

	JButton sourceButton;

	List<TopkResult> results;

	public TrackAnnotationsWorker(String track, String resultsDirectory, String label, JButton sourceButton,
			List<TopkResult> results) { // HashMap<String,
		this.track = track;
		this.label = label;
		this.sourceButton = sourceButton;
		this.results = results;
		annotsFileName = resultsDirectory + File.separator + label.toLowerCase() + "-"
				+ track.replaceAll("[\\/ \t]", "-") + "-track-annotations.txt";
	}

	@Override
	public ArrayList<String> doInBackground() throws Exception {
		// super.run();
		logger.info("Get annotations from track: " + track);

		File annotsFile = new File(annotsFileName);

		Icon sourceIcon = sourceButton.getIcon();

		sourceButton.setIcon(new ImageIcon(getClass().getResource("/ajax-loader.gif")));

		try {
			if (false == annotsFile.exists()) {

				// FunctionalAnnotationPanel p = new
				// FunctionalAnnotationPanel(organism, genesFileName);

				AnnotationsFromTrack annotator = new AnnotationsFromTrack();

				annotator.process(track, results);

				String[] columnNames = { "Name", "p-value", "matching regions", "annotations covered", "all annotations" };

				logger.info("Write local resource: " + annotsFile);
				Writer annotationsWriter = new BufferedWriter(
						new OutputStreamWriter(new FileOutputStream(annotsFile), "utf-8"));

				annotationsWriter.write(String.join("\t", columnNames) + System.getProperty("line.separator"));

				DecimalFormat format = new DecimalFormat("0.0E0");

				for (String annotation : annotator.getAnnotationsByNumberOfOverlap()) {

					double probability = annotator.getProbability(annotation);

					annotationsWriter.write(annotation + "\t" + format.format(probability) + "\t"
							+ annotator.getNumberOfMatchings(annotation) + "\t"
							+ annotator.getNumberOfAnnotationsMapped(annotation) + "\t"
							+ annotator.getNumberOfAnnotations(annotation) + System.getProperty("line.separator"));
				}

				annotationsWriter.close();
			}

			final JFrame frame = new JFrame("Annotations " + label);

			FileReader fr = new FileReader(annotsFile);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();

			String[] columnNames = line.split("\t");

			DefaultTableModel model = new DefaultTableModel(columnNames, 0);
			JTable table = new JTable(model); // annotations.toArray(new
												// String[annotations.size()][3]),
												// columnNames);

			while ((line = br.readLine()) != null) {
				model.addRow(line.split("\t"));
			}

			br.close();
			fr.close();

			JPanel annotationsPanel = new JPanel(new BorderLayout());

			JScrollPane scrollPane = new JScrollPane(table);
			table.setFillsViewportHeight(true);

			annotationsPanel.add(scrollPane, BorderLayout.CENTER);

			frame.getContentPane().add(annotationsPanel);

			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(550, 200);
			frame.setVisible(true);

		} catch (IOException ex) {
			System.out.println("problem accessing file" + annotsFile.getAbsolutePath());
		}

		sourceButton.setIcon(sourceIcon);

		publish("Annotations done");

		return new ArrayList<>();
	}

	@Override
	protected void process(List<String> chunks) {
		for (String message : chunks) {
			logger.info(message);
		}
	}

}
