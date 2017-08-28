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
package it.iit.genomics.cru.simsearch.bundle.model;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Random;

/**
 * In order to use always the same color in the same order 
 * for positive matches, we use a Singleton that generate random colors. 
 * @author Arnaud Ceol
 *
 */
public class ColorPalette {

	private ArrayList<Color> palette;
	
	private Random rand;
	
	
	private static ColorPalette instance;
	
	private ColorPalette() {
		palette = new ArrayList<>();
		rand = new Random();
	}
	
	public static ColorPalette getInstance() {
		if (instance == null) {
			instance = new ColorPalette();
		}
		
		return instance;
	}
	
	public Color getColor(int index) {
		if (index >= palette.size()) {
			Color newColor = newColor();
			palette.add(newColor);
		}
		
		return palette.get(index);		
	}
	
	private Color newColor() {
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		
		return new Color(r, g, b);
	}
	
	
}
