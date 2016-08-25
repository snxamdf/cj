package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.twobatch.GawkerFoodCollector;

public class CollectorMain {

	public static void main(String[] args) {

		Collector collFashion = new GawkerFoodCollector();
		collFashion.begin();
	}

}
