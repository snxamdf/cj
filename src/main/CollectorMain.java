package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.CosmFashionHowtoCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new CosmFashionHowtoCollector();
		collFashion.begin();
	}

}
