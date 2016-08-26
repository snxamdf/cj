package main;

import com.modern.datacollect.api.Collector;

public class CollectorMain {

	public static void main(String[] args) {
		final String[] classes = { "com.modern.datacollect.impl.twobatch.TroverhioaeCollector", "com.modern.datacollect.impl.twobatch.TroveriSDwCollector", "com.modern.datacollect.impl.twobatch.TroverhTAbDCollector", "com.modern.datacollect.impl.twobatch.Troverhgj5LCollector", "com.modern.datacollect.impl.twobatch.TroverhgiwcCollector", "com.modern.datacollect.impl.twobatch.Troverh7pzCollector", "com.modern.datacollect.impl.twobatch.TrovergixHCollector" };
		for (int i = 0; i < classes.length; i++) {
			final int ii = i;
			Thread thread = new Thread(new Runnable() {
				public void run() {
					try {
						Collector collFashion = (Collector) Class.forName(classes[ii]).newInstance();
						collFashion.begin();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			thread.start();
		}
		// try {
		// Collector collFashion = new TroverhioaeCollector();
		// collFashion.begin();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
	}
}
