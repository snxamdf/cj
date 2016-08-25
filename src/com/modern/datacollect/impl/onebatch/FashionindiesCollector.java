package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class FashionindiesCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			try {
				String title = emt.select(".info-wrapper").text();
				String href = "http://" + Tools.url(config.getSiteUrl()).getHost() + emt.select(".info").attr("href");
				String imgSrc = emt.select(".cover").attr("src");
				Data data = new Data();
				data.setContentId(Tools.string2MD5(Tools.url(href).getPath()));
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
				Elements eimgBody = Tools.getBody("#post-carousel", html);
				eimgBody.select("a").attr("href", "javascript:void(0)");
				Elements cimg = eimgBody.select("img");
				for (Element cimgemt : cimg) {
					String cimgSrc = cimgemt.attr("src");
					if (!"".equals(cimgSrc)) {
						String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
						File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
						String mydest = getMySiteImgSrc(cdest);
						if (mydest != null)
							cimgemt.attr("src", mydest);
					}
				}
				Elements ebody = Tools.getBody(".post-text", html);
				ebody.select("a").attr("href", "javascript:void(0)");
				ebody.select(".small-ad-rectangle").remove();
				Elements cimg1 = ebody.select("img");
				for (Element cimgemt : cimg1) {
					String cimgSrc = cimgemt.attr("src");
					if (!"".equals(cimgSrc)) {
						String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
						File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
						String mydest = getMySiteImgSrc(cdest);
						if (mydest != null)
							cimgemt.attr("src", mydest);
					}
				}
				Elements postsidebar = Tools.getBody(".post-sidebar", html);
				String author = postsidebar.select("a[rel=\"user\"]").text();
				Elements keys = postsidebar.select("ul[class=\"info group\"]").select("a");
				String keywords = StringUtils.join(keys.toArray(), ",");

				Tools.clearsAttr(ebody);
				Tools.clearsAttr(eimgBody);
				// 获取内容
				String content = eimgBody.toString() + ebody.toString();
				if (!"".equals(author)) {
					content = "<div>作者 : " + author + "</div><br/>" + content;
				}
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords);
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
