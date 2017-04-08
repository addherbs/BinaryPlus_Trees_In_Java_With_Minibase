package com.thechat.chatmessenger.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UniqueID {

	private static List<Integer> ids = new ArrayList<Integer>(); // Stores the
																	// ID
	private static final int RANGE = 10000; // Maximum 10000 is th number of
											// clients

	private static int index = 0; // Strats from index 0 and increment on each
									// Client connection

	static {
		for (int i = 0; i < RANGE; i++) {
			ids.add(i); // Initializes ids arrayList will 10000 number from 1 to
						// 10000
		}
		Collections.shuffle(ids); // Shuffles the ids list
	}

	/*
	 * Returns an Unique ID to the caller function for each client
	 */
	public static int getID() {
		if (index > ids.size() - 1)
			index = 0;
		return ids.get(index++);
	}

}