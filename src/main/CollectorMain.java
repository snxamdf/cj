package main;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.impl.T36krNews;

public class CollectorMain {

	public static void main(String[] args) {
		Collector collFashion = new T36krNews();
		collFashion.begin();
	}

}
