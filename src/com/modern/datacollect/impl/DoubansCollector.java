package com.modern.datacollect.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;

public abstract class DoubansCollector extends Collector {

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".title").select("a").text();
				String href = elm.select(".title").select("a").attr("href");
				StringBuffer keywords = new StringBuffer();
				Elements elmKey = elm.select(".event-cate-tag").select("a");
				for (int i = 0; elmKey != null && i < elmKey.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(elmKey.get(i).text());
				}
				String time = elm.select(".event-meta").select(".event-time").text();
				String addr = elm.select(".event-meta").select("li[title]").attr("title");
				String imgSrc = elm.select(".pic").select("img").attr("data-lazy");
				Data data = new Data();

				URL url;
				try {
					url = new URL(href);
					data.setContentId(Tools.string2MD5(url.getPath()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

				if (isDataExists(data.getContentId())) {
					continue;
				}
				String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest(href);
				Elements ebody = Tools.getBody("#link-report", html);
				ebody.select("div").removeAttr("style");
				ebody.select("a").attr("href", "javascript:void(0)");
				Elements cimg = ebody.select("img");
				for (Element cimgemt : cimg) {
					String cimgSrc = cimgemt.attr("data-lazy");
					if ("".equals(cimgSrc)) {
						cimgSrc = cimgemt.attr("src");
					}
					if (!"".equals(cimgSrc)) {
						String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
						File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
						String mydest = getMySiteImgSrc(cdest);
						if (mydest != null)
							cimgemt.attr("src", mydest);
						cimgemt.removeAttr("data-original");
					}
				}
				Tools.clearsAttr(ebody);
				// 获取内容
				String content = ebody.toString();
				content += "<br/><div>报名连接：<a href=\"" + href + "\">" + href + "</a></div>";
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				data.setAddress(addr);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}
}
