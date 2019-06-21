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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


import it.unibo.disi.simsearch.core.model.Region;

/**
 * Composite regions are regions composed of several subregions 
 * of a single chromosome (TODO: extends to different chromosomes)
 * if two positions fall within two parts of a composite region,
 * the distance should be considered as if both sub-regions were a single regions.
 * Example: composite region: 100-200 and 1000-1200
 * Positions: 150 and 1050
 * Real distance = 1050 - 150 = 9000, adjusted distance =  1050 - 150 - (1000 - 200) = 100
 * 
 * @author arnaudceol
 *
 */
public class LoopsManager {

	private HashMap<String, List<Loop>> regions = new HashMap<>();
	
	int numRegions = 0;
	
	public int getNumRegions() {
		return numRegions;
	}

	public void addLoop(String chromosome, int left1, int right1, int left2, int right2) {
		Loop region = new Loop(left1, right1, left2, right2);
		
		if (false == regions.containsKey(chromosome)) {
			regions.put(chromosome, new ArrayList<>());
		}
		
		regions.get(chromosome).add(region);	
		numRegions++;
	}
	
	
	public static class Loop {

		int x1;
		int y1;
		int x2;
		int y2;

		public Loop(int x1, int y1, int x2, int y2) {
			super();
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		public int getDistance() {
			return x2 - x1;
		}
		
		public int getX1() {
			return x1;
		}
		public int getY1() {
			return y1;
		}
		public int getX2() {
			return x2;
		}
		public int getY2() {
			return y2;
		}
	}
	
	/**
	 * if the region is contained in a composit one, return the list of 
	 * regions with coordinates in the other side, e.g. in contact with another part of chromosome
	 * @return
	 */
	public List<Region> transferedRegions(Region r) {
		ArrayList<Region> transferredRegions = new ArrayList<>();
		
			
		if (false == regions.containsKey(r.getChromosome())) {
			return transferredRegions;
		}
		
		int i = 0;
		
if (r.getLeft() > r.getRight()) {
	System.out.println("ERROR: x > y");
}

		for (Loop cr: regions.get(r.getChromosome())) {
			if (r.getCentroid() >= cr.x1 && r.getCentroid() <= cr.y1) {
				
				int centroidTox1 = r.getCentroid() - cr.x1;
				int newY = cr.y2 - centroidTox1 + r.getLength() /2;
				int newX = newY - r.getLength(); 

				i++;
				Region tr = new Region(r.getChromosome(), newX, newY, r.getStrand(), r.getId() + "-c" + i, r.getLeft(), r.getRight());
				transferredRegions.add(tr);
			} 
			else if (r.getCentroid() >= cr.x2 && r.getCentroid() <= cr.y2) {
				int centroidTox2 = r.getCentroid() - cr.x2;
				int newY = cr.y1 - centroidTox2 + r.getLength() /2;
				int newX = newY - r.getLength(); 
			
				// int distance = cr.x2 - cr.x1;
				i++;
				Region tr = new Region(r.getChromosome(), newX, newY, r.getStrand(), r.getId() + "-c" + i, r.getLeft(), r.getRight());
				transferredRegions.add(tr);
			} 
		}
		
		return transferredRegions;
	}
	
	
}
