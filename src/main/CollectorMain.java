package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.onebatch.FashioNewsCollector;

public class CollectorMain {

	public static void main(String[] args) {

		Collector collFashion = new FashioNewsCollector();
		collFashion.begin();
	}

}
