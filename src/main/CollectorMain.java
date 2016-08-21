package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.BehanceSearch132Collector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new BehanceSearch132Collector();
		collFashion.begin();
	}

}
