package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.twobatch.TroveriSDwCollector;

public class CollectorMain {

	public static void main(String[] args) {

		Collector collFashion = new TroveriSDwCollector();
		collFashion.begin();
	}

}
