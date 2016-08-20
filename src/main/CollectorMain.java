package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.GqTag39705Collector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new GqTag39705Collector();
		collFashion.begin();
	}

}
