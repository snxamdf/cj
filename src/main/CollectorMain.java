package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.Haibao24Collector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new Haibao24Collector();
		collFashion.begin();
	}

}
