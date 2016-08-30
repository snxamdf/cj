package com.modern.datacollect.impl.twobatch.fubiz;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class FubizCollectors extends Collector {

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = "";
				Elements titleElm = elm.select(".title-post").select("a");
				if (titleElm.size() == 0) {
					titleElm = elm.select("a[class=\"thumb container-img\"]");
				}
				String href = titleElm.attr("href");
				Elements imgElm = elm.select("picture[class=\"img-responsive first\"]").select("img");
				String imgSrc = imgElm.attr("data-src");

				Data data = new Data();
				data.setContentId(Tools.string2MD5(Tools.url(href).getPath()));
				if (isDataExists(data.getContentId())) {
					continue;
				}
				String tempFilePath = Tools.getLineFile1(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest1(href);
				Elements ebody = Tools.getBody(".inner-post-content", html);
				ebody.select("noscript").remove();
				titleElm = Tools.getBody("h1[class=\"title-part white-title\"]", html);
				title = titleElm.text();

				Elements authorElm = Tools.getBody(".left-content", html);
				authorElm.select(".function-author").remove();
				authorElm.select("writtenby").append(" ");

				Tools.clearsAttr(authorElm);
				Elements tagpostElm = Tools.getBody(".tag-post", html);
				Elements tags = tagpostElm.select("a");
				Tools.clearsAttr(tagpostElm);
				StringBuffer keywords = new StringBuffer();
				for (int i = 0; i < tags.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(tags.get(i).text());
				}
				ebody.select(".gallery").remove();
				this.downImg(ebody, tempFileDir, targetFileDir);
				Tools.clearsAttr(ebody);

				String content = ebody.toString() + "<br/><div>" + authorElm.text() + "</div>" + "<br/><div>原文链接 : <a href=\"" + href + "\">" + href + "</a></div>" + tagpostElm.toString();

				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("data-original");
			if ("".equals(cimgSrc)) {
				cimgSrc = cimgemt.attr("src");
			}
			if (cimgSrc.indexOf("http") == -1) {
				cimgSrc = "http://www.fubiz.net" + cimgSrc;
			}
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				} else {
					cimgemt.remove();
				}
			} else {
				cimgemt.remove();
			}
		}
	}
}
