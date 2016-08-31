package main;

import org.jsoup.select.Elements;

import com.modern.datacollect.impl.Tools;

public class FashionMain {

	public static void main(String[] args) {
		String html = "<body>bb&nbsp;aa</body>";
		Elements es = Tools.getBody("body", html);
		Tools.clearsAttr(es);
		Tools.clearsNodes(es);
		System.out.println(es.toString());
	}

}
