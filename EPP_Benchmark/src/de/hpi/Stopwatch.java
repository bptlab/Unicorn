package de.hpi;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

public class Stopwatch {

	private static HashMap<String, Map<String, Long>> watches;

	public static void main(String[] args) {

	}

	public static synchronized void reset() {
		Stopwatch.watches = null;
	}

	public static synchronized String start(String watch) {
		long currentTimeMillis = System.currentTimeMillis();
		String watchID;
		Map<String, Long> watchHistory = null;
		watchHistory = getWatches().get(watch);
		if (watchHistory == null) {
			watchHistory = new HashMap<String, Long>();
			getWatches().put(watch, watchHistory);
		}

		watchID = UUID.randomUUID().toString();
		watchHistory.put(watchID, currentTimeMillis);
		return watchID;
	}

	public static void stop(String watch, String id) {
		long currentTimeMillis = System.currentTimeMillis();
		synchronized (Stopwatch.class) {
			Map<String, Long> watchHistory = getWatches().get(watch);
			Long startTime = watchHistory.get(id);
			watchHistory.put(id, currentTimeMillis - startTime);
		}
	}

	private static Map<String, Map<String, Long>> getWatches() {
		if (Stopwatch.watches == null) {
			synchronized (Stopwatch.class) {
				Stopwatch.watches = new HashMap<String, Map<String, Long>>();
			}
		}
		return Stopwatch.watches;
	}

	public static void print() {
		for (Entry<String, Map<String, Long>> watch : getWatches().entrySet()) {
			System.out.println(String.format("%s: average time %d with %d operations at total %d \n%s\n\n", watch.getKey(), average(watch.getValue().values()), watch.getValue().values().size(), sum(watch.getValue().values()), watch.getValue().values()));
		}
	}

	private static Long sum(Collection<Long> values) {
		Long sum = (long) 0;
		for (Long value : values) {
			sum += value;
		}
		return sum;
	}

	private static Long average(Collection<Long> values) {
		Long average = (long) 0;
		for (Long value : values) {
			average += value;
		}
		return (average / values.size());
	}

	public static void write(String name) throws IOException {
		//		CSVWriter writer = new CSVWriter(new FileWriter(String.format("D:/GET/%s.csv", name)), ';');
		//		for (Entry<String, Map<String, Long>> watch : getWatches().entrySet()) {
		//			LinkedList<String> list = new LinkedList<String>();
		//			list.add(watch.getKey());
		//			for (Long time : watch.getValue().values()) {
		//				list.add(time.toString());
		//			}
		//			String[] string = new String[list.size()];
		//			writer.writeNext(list.toArray(string));
		//		}
		//		writer.close();
	}

	public static synchronized void drop(String watch, String id) {
		getWatches().get(watch).remove(id);
	}

}
