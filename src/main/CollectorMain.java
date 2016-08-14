package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.CosmFashionNowCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new CosmFashionNowCollector();
		collFashion.begin();
	}

}
