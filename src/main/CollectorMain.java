package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.Cyzone765Collector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new Cyzone765Collector();
		collFashion.begin();
	}

}
