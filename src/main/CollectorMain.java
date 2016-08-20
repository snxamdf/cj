package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.HuodongxingBJCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new HuodongxingBJCollector();
		collFashion.begin();
	}

}
