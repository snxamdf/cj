package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.PocoCookbook_diyCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new PocoCookbook_diyCollector();
		collFashion.begin();
	}

}
