package it.iit.genomics.cru.simsearch.bundle;

import java.util.Collection;

import it.iit.genomics.cru.simsearch.bundle.utils.PantherBridge;

public class Test {

	public static void main(String[] args) {
		
		String organism = "Homo sapiens";
		String genesFileName = "/var/folders/0g/9l6bsd096gv4swlnb67r6mf80000gn/T/1542577917396-0/strong_enhancer-9-genes.txt";
		Collection<String[]> annotations = PantherBridge.getEnrichment(organism, genesFileName, 1);
		for (String[] annotation : annotations) {
			System.out.println(annotation[1]);
		}

	}

}
