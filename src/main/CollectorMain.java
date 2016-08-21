package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.YokaDna370Collector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new YokaDna370Collector();
		collFashion.begin();
	}

}
