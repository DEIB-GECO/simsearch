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
package it.iit.genomics.cru.simsearch.bundle.utils;

/**
 * @author Arnaud Ceol
 */
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import com.google.common.collect.ArrayListMultimap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.MultipartPostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;

/**
 * Use panther webservice to get GO enrichment of a list of genes.
 * 
 * @author arnaudceol
 *
 */
public class PantherBridge {

	public final static int MAX_GENES_OUTPUT = 10;

	public static Collection<String[]> getEnrichment(String organism, String fileName, double threshold) {
		ArrayList<String[]> results = new ArrayList<>();

		ArrayListMultimap<String, String> genes = ArrayListMultimap.create();
		ArrayListMultimap<Double, String> pvalues = ArrayListMultimap.create();

		HashSet<String> uniqueGenes = new HashSet<>();

		try {
			String[] enrichmentTypes = { "process", "pathway" };

			for (String enrichmentType : enrichmentTypes) {

				HttpClient client = new HttpClient();
				MultipartPostMethod method = new MultipartPostMethod(
						"http://pantherdb.org/webservices/garuda/tools/enrichment/VER_2/enrichment.jsp?");

				// Define name-value pairs to set into the QueryString
				method.addParameter("organism", organism);
				method.addParameter("type", "enrichment");
				method.addParameter("enrichmentType", enrichmentType); // "function",
																		// "process",
																		// "cellular_location",
																		// "protein_class",
																		// "pathway"
				File inputFile = new File(fileName);
				method.addPart(new FilePart("geneList", inputFile, "text/plain", "ISO-8859-1"));

				// PANTHER does not use the ID type
				// method.addParameter("IdType", "UniProt");

				// Execute and print response
				client.executeMethod(method);
				String response = method.getResponseBodyAsString();

				for (String line : response.split("\n")) {
					if (false == "".equals(line.trim())) {

						String[] row = line.split("\t");
						// Id Name GeneId P-value
						if ("Id".equals(row[0])) {
							// header
							continue;
						}
// if (row.length > 1) {
						String name = row[1];

						String gene = row[2];
						Double pvalue = Double.valueOf(row[3]);

						uniqueGenes.add(gene);

						if (pvalue < threshold) {
							if (false == genes.containsKey(name)) {
								pvalues.put(pvalue, name);
							}
							genes.put(name, gene);
						}
					// } else {
					// 	System.out.println("oups: " + row[0]);
					// }
					}
				}

				method.releaseConnection();
			}
			ArrayList<Double> pvalueList = new ArrayList<>();
			Collections.sort(pvalueList);

			pvalueList.addAll(pvalues.keySet());
			Collections.sort(pvalueList);

			int numGenes = uniqueGenes.size();

			for (Double pvalue : pvalueList) {
				for (String name : pvalues.get(pvalue)) {
					String geneList = 
							String.join(",", genes.get(name));
					String result[] = { name, "" + pvalue, genes.get(name).size() + "/" + numGenes, geneList };
					results.add(result);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return results;
	}

	public static void main(String args[]) {
		Collection<String[]> results = PantherBridge.getEnrichment("Homo sapiens", "/tmp/genes.txt", 1);
		for (String[] result : results) {
			System.out.println(result[0] + " -> " + result[1] + " (" + result[2] + ")");
			;
		}
	}

}
