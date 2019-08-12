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
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import it.unibo.disi.simsearch.core.business.regionComparator.RegionsComparatorCentroidAttribute;
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
		
		for (Loop cr: regions.get(r.getChromosome())) {

			int sourceLoopX = 0;
			int sourceLoopY = 0;
			int sourceCentroid = 0;

			int targetLoopX = 0;
			int targetLoopY = 0;
			int targetCentroid = 0;

			if (r.getCentroid() >= cr.x1 && r.getCentroid() <= cr.y1) {
				 sourceLoopX = cr.x1;
			 sourceLoopY = cr.y1;

			 targetLoopX = cr.x2;
			 targetLoopY = cr.y2;
			} 
			else if (r.getCentroid() >= cr.x2 && r.getCentroid() <= cr.y2) {
				sourceLoopX = cr.x2;
				sourceLoopY = cr.y2;
   
				targetLoopX = cr.x1;
				targetLoopY = cr.y1;
			} else {
				continue;
			}

			sourceCentroid = sourceLoopY - (sourceLoopY - sourceLoopX) /2;	
			targetCentroid = targetLoopY - (targetLoopY - targetLoopX) /2;

			// System.out.println(sourceLoopX);
			// System.out.println(sourceLoopY);
			// System.out.println(sourceCentroid);
			// System.out.println(targetLoopX);
			// System.out.println(targetLoopY);
			// System.out.println(targetCentroid);

			// Distance between the centers of the two loops, 
			int vector = targetCentroid - sourceCentroid;

			// Distance between the centroid of the region 
			// and the one of the DNA contact region: when 
			// transfering the region, we should "mirror" it,
			// i.e. move it to the other side of the center.
			int relativeDistance = r.getCentroid() - sourceCentroid;

			int transferLength = vector - 2* relativeDistance;

			// System.out.println(vector);
			// System.out.println(relativeDistance);
			// System.out.println(transferLength);
			// int centroidTox1 = r.getCentroid() - cr.x1;
			// int newY = cr.y2 - centroidTox1 + r.getLength() /2;
			// int newX = newY - r.getLength(); 

			i++;
			Region tr = new Region(r.getChromosome(), r.getLeft() + transferLength, r.getRight() + transferLength, r.getStrand(), r.getId() + "-c" + i, r.getLeft(), r.getRight());
			transferredRegions.add(tr);

		}
		
		return transferredRegions;
	}
	
	public static void main(String[] args) {
		LoopsManager l = new LoopsManager();

		l.addLoop("chr1", 3, 5, 9, 11);

		ArrayList<Region> regions = new ArrayList<>();
		regions.add(new Region("chr1", 8, 10, ".", "1"));
		regions.add(new Region("chr1", 4, 6, ".", "1"));

		ArrayList<Region> transferred = new ArrayList<>();

		for (Region r :  regions) {
			transferred.addAll(l.transferedRegions(r));
		}	

		regions.addAll(transferred);

		for (Region r :  regions) {	
			System.out.println("Before sort: " + r.toString());
		}
		// Collections.sort(regions);

		for (Region r :  regions) {	
			System.out.println("After sort: " + r.toString());
		}
		Comparator<Region> comp = new RegionsComparatorCentroidAttribute();
			Collections.sort(regions, comp);

			
		for (Region r :  regions) {	
			System.out.println("After sort 2: " + r.toString());
		}
	}

}
