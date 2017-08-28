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

import java.awt.Color;
import java.awt.Font;

/**
 * @author Arnaud Ceol
 */
public class StyleFactory {
	
	private StyleFactory(){}
	
	public static Font getPlainFont(int dimension){
		return new Font("MS Sans Serif", Font.PLAIN, dimension);
	}
	
	public static Color getApplicationColor(){
		return new Color(0, 0, 102);
	}

	public static Font getBoldFont(int dimension) {
		return new Font("MS Sans Serif", Font.BOLD, dimension);
	}
}
