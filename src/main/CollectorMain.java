package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.DoubanChengduWeekallCollector;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new DoubanChengduWeekallCollector();
		collFashion.begin();
	}

}
