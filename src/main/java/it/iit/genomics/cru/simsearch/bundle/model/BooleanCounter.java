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

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Arnaud Ceol
 */
public class BooleanCounter {

	int[] counter; 
	
	public BooleanCounter(int size) {
		 counter = new int[size];		 
		 for (int i = 0; i < size; i++) {
			 counter[i] = 0;
		 }
		 this.next();		 
	}
	
	public boolean hasNext() {
		for (int i = 0; i< counter.length; i++) {
			if (counter[i] == 0) {
				return true;
			}
		}
		
		return false;
	}
		
	public void next() {		
		for (int i = 0; i< counter.length; i++) {
			int newValue = counter[i] ^ 1;
			counter[i] = newValue;
			if (newValue == 1) {
				break;
			}
		}
	}
		
	public Collection<Boolean> getcounter() {
		ArrayList<Boolean> bcounter = new ArrayList<>();
		for (int i = 0; i< counter.length; i++) {
			bcounter.add(counter[i] == 1);
		}
		return bcounter;
	}
	
	public boolean getValueAt(int i) {
		return i < counter.length && 1 == counter[i];
	}
	
	public String toString() {
		String result = "";
		for (int i = 0; i< counter.length; i++) {
			result +=  counter[i]  + "";
		}
		return result;
	}
	
	public static void main(String[] args) {
		BooleanCounter counter = new BooleanCounter(5);

		System.out.println(counter.toString());
		for (int i = 0; i< 5; i++) {
			counter.next();
			System.out.println(counter.toString());
		}
		
	}
	
	
}
