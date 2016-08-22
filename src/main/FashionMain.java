package main;

import org.jsoup.select.Elements;

import com.modern.datacollect.impl.Tools;

public class FashionMain {

	public static void main(String[] args) {
		String html = "<img border=\"0\" src=\"http://www.modengvip.com/real_file_upload/coll_54/201608/201412041341135386056_650.jpeg\" title=\"wKgB4lMR7EaAcbIPAA6EUOTYPWI14.groupinfo.w600.jpeg\" alt=\"新西兰旅游攻略图片\" width=\"650px\" height=\"487px\" album_id=\"30618445\" br=\"true\">";
		Elements es = Tools.getBody("img", html);
		Tools.clearImgAttr(es.get(0));
	}
}
