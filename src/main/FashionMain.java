package main;

import org.jsoup.select.Elements;

import com.modern.datacollect.impl.Tools;

public class FashionMain {

	public static void main(String[] args) {
		String html = "<div>aaa<a align=\"centeer\">bbb</a><b>ccc</b></div>";
		Elements es = Tools.getBody("div", html);
		System.out.println(es.select("a[align=\"centeer\"]"));
	}
}
