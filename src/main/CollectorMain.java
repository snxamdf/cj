package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.onebatch.YokaDna370Collector;

public class CollectorMain {

	public static void main(String[] args) {
		// final String[] classes = {
		// "com.modern.datacollect.impl.onebatch.BehanceSearch102Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch109Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch132Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch4Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch44Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch48Collector",
		// "com.modern.datacollect.impl.onebatch.BehanceSearch49Collector" };
		// for (int i = 0; i < classes.length; i++) {
		// final int ii = i;
		// Thread thread = new Thread(new Runnable() {
		// public void run() {
		// try {
		// Collector collFashion = (Collector)
		// Class.forName(classes[ii]).newInstance();
		// collFashion.begin();
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// }
		// });
		// thread.start();
		// }
		try {
			Collector collFashion = new YokaDna370Collector();
			collFashion.begin();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
