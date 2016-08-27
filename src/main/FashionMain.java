package main;

import java.util.List;

import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import com.modern.datacollect.impl.Tools;

public class FashionMain {

	public static void main(String[] args) {
		Tools.getRequest("https://www.behance.net/search?field=102&content=projects&sort=appreciations&time=week");
		// String html =
		// "<body><div id='a' b='c'>bbb<div></div><div><img border=\"0\" src=\"http://www.modengvip.com/real_file_upload/coll_54/201608/201412041341135386056_650.jpeg\" title=\"wKgB4lMR7EaAcbIPAA6EUOTYPWI14.groupinfo.w600.jpeg\" alt=\"新西兰旅游攻略图片\" width=\"650px\" height=\"487px\" album_id=\"30618445\" br=\"true\"></div></div><img border=\"0\" src=\"http://www.modengvip.com/real_file_upload/coll_54/201608/201412041341135386056_650.jpeg\" title=\"wKgB4lMR7EaAcbIPAA6EUOTYPWI14.groupinfo.w600.jpeg\" alt=\"新西兰旅游攻略图片\" width=\"650px\" height=\"487px\" album_id=\"30618445\" br=\"true\"><img border=\"0\" src=\"http://www.modengvip.com/real_file_upload/coll_54/201608/201412041341135386056_650.jpeg\" title=\"wKgB4lMR7EaAcbIPAA6EUOTYPWI14.groupinfo.w600.jpeg\" alt=\"新西兰旅游攻略图片\" width=\"650px\" height=\"487px\" album_id=\"30618445\" br=\"true\"><img border=\"0\" src=\"http://www.modengvip.com/real_file_upload/coll_54/201608/201412041341135386056_650.jpeg\" title=\"wKgB4lMR7EaAcbIPAA6EUOTYPWI14.groupinfo.w600.jpeg\" alt=\"新西兰旅游攻略图片\" width=\"650px\" height=\"487px\" album_id=\"30618445\" br=\"true\"></body>";
		// Elements es = Tools.getBody("body", html);
		// clearAttr(es);
		// System.out.println(es.toString());
	}

	public static void clearAttr(Elements elements) {
		for (Element e : elements) {
			Node node = (Node) e;
			clearNodeAttr(node.childNodes());
			e.select(".clsRemoveTag").remove();
		}
	}

	public static void clearNodeAttr(List<Node> nodes) {
		for (Node n : nodes) {
			if (n.childNodes().size() > 0) {
				clearNodeAttr(n.childNodes());
			}
			Attributes attrNodes = n.attributes();
			if (attrNodes.size() == 0 && n.childNodes().size() == 0) {
				n.attr("class", "clsRemoveTag");
				continue;
			}
			for (Attribute node : attrNodes) {
				if (!"href".equals(node.getKey()) && !"src".equals(node.getKey()) && !"title".equals(node.getKey()) && !"alt".equals(node.getKey()) && !"text".equals(node.getKey())) {
					n.removeAttr(node.getKey());
				} else if ("href".equals(node.getKey())) {
					n.attr(node.getKey(), "javascript:void(0)");
				}
			}
			if ("img".equals(n.nodeName())) {
				n.attr("lazy-src", n.attr("src"));
				n.attr("src", "http://modengvip.com/res/rec/images/moimg.jpg");
			}
		}
	}
}
