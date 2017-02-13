/*******************************************************************************
 *
 * Copyright (c) 2012-2015, Business Process Technology (BPT),
 * http://bpt.hpi.uni-potsdam.de. 
 * All Rights Reserved.
 *
 *******************************************************************************/
package de.hpi.unicorn.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hpi.unicorn.bpmn.element.BPMNXORGateway;

/**
 * This class combines various methods for modification of lists.
 * 
 * @author micha
 */
public class SetUtil {

	/**
	 * Calculates the intersection between a list of sets.
	 * 
	 * @param sets
	 * @return
	 */
	public static <T> Set<T> intersection(final List<Set<T>> sets) {
		Set<T> differenceSet = new HashSet<T>();
		if (sets.size() > 0) {
			differenceSet = new HashSet<T>(sets.get(0));
			for (int i = 1; i < sets.size(); i++) {
				differenceSet.retainAll(new HashSet<T>(sets.get(i)));
			}
		}
		return differenceSet;
	}

	/**
	 * Calculates the union between a list of sets.
	 * 
	 * @param sets
	 * @return
	 */
	public static <T> Set<T> union(final List<Set<T>> sets) {
		Set<T> unionSet = new HashSet<T>();
		if (sets.size() > 0) {
			unionSet = new HashSet<T>(sets.get(0));
			for (int i = 1; i < sets.size(); i++) {
				unionSet.addAll(new HashSet<T>(sets.get(i)));
			}
		}
		return unionSet;
	}

	/**
	 * Returns the element at the ordered position of the given index.
	 * 
	 * @param <T>
	 * @param successors
	 * @param index
	 */
	public static <T> T getElement(final Set<T> successors, final int index) {
		int i = 0;
		for (final T element : successors) {
			if (i == index) {
				return element;
			}
			i++;
		}
		return null;
	}

	/**
	 * Returns the elements of the given set as list.
	 * 
	 * @param elements
	 * @return
	 */
	public static <T> List<T> asList(final Set<T> elements) {
		final List<T> elementList = new ArrayList<T>();
		elementList.addAll(elements);
		return elementList;
	}

	public static <T> Boolean containsXorSplit(final Set<T> set) {
		for (final T element : set) {
			if (element instanceof BPMNXORGateway) {
				if (((BPMNXORGateway) element).isSplitGateway()) {
					return true;
				}
			}
		}
		return false;
	}
}
