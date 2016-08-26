package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.onebatch.HarpersbazaarFashionCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new HarpersbazaarFashionCollector();
		collFashion.begin();
	}

}
