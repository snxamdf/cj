package com.modern.datacollect.impl.twobatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class GawkersCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String description = elm.select(".description").text();
				Elements postLinks = elm.select(".post-links");
				String submitterHref = postLinks.select(".submitter").attr("href");
				String submitter = postLinks.select(".submitter").text();
				String postidHref = postLinks.select(".postid").attr("href");
				Elements picture = elm.select(".picture");
				String imgSrc = "https:" + elm.select(".picture-link").select("img").attr("src");
				String href = elm.select(".picture-link").attr("href");
				String title = elm.select(".picture-link").attr("title");

				StringBuffer keywords = new StringBuffer();

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
				if (href.lastIndexOf("http:") > 0) {
					href = href.substring(href.lastIndexOf("http:"), href.length());
				} else if (href.lastIndexOf("https:") > 0) {
					href = href.substring(href.lastIndexOf("https:"), href.length());
				}

				String author = "<br/><div>作者 : " + submitter + "</div>";
				String sourceUrl = author + "<br/><div>阅读文章 : <a href=\"" + href + "\">" + href + "</a></div>";
				String content = "<div>" + description + "</div>" + sourceUrl;
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
