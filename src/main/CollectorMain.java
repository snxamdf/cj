package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.BehanceSearch102Collector;

public class CollectorMain {

	public static void main(String[] args) {

		Collector collFashion = new BehanceSearch102Collector();
		collFashion.begin();
	}

}
