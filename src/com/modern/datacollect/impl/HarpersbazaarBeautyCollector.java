package com.modern.datacollect.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;

public class HarpersbazaarBeautyCollector extends Collector {

	@Override
	public void begin() {
		// 默认注入的配置
		Config config = getConfig();
		// 图片保存目录
		String targetFileDir = getSaveFileDir();
		// 图片**临时**保存目录
		String tempFileDir = getTempFileDir();
		if (config == null) {
			config = new Config();
			config.setSiteUrl("http://www.harpersbazaar.com/beauty/");
			config.setSiteConfig("{'page':1,'dataUrl':'http://www.harpersbazaar.com/landing-feed/','param':'?template=section&landingTemplate=standard&id=4&pageNumber={page}&feedTime={feedTime}'}");
			targetFileDir = "D:\\targetFileDir\\";
			tempFileDir = "D:\\tempFileDir\\";
		}
		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null, dataUrl = null, param = null;
		Integer page = 1;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
			param = obj.getString("param");
			dataUrl = obj.getString("dataUrl");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String html = null;
		for (;;) {
			url = dataUrl + param.replace("{page}", page.toString()).replace("{feedTime}", new Date().getTime() + "");
			config.setSiteConfig("{'page':" + page + ",'dataUrl':'http://www.harpersbazaar.com/landing-feed/','param':'?template=section&landingTemplate=standard&id=3&pageNumber={page}&feedTime={feedTime}'}");
			updateSiteConfig(config.getSiteConfig());
			html = Tools.getRequest(url);
			Elements body = Tools.getBody(".landing-feed--story", html);
			if (body.size() == 0) {
				stop();
				break;
			}
			this.dealwith(body, tempFileDir, targetFileDir, config);
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			Elements emtImage = emt.select(".landing-feed--story-image").select("img");
			String imgSrc = emtImage.attr("data-src");
			String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
			Elements emtContent = emt.select(".landing-feed--story-content");
			Elements emtTitle = emtContent.select(".landing-feed--story-title");
			Elements emtAbstract = emtContent.select(".landing-feed--story-abstract");
			String text = emtAbstract.select("span").text();
			String href = emtTitle.attr("href");
			if (href != null && href.indexOf("beauty") != -1) {
				href = href.substring(7, href.length());
			}
			String contentUrl = config.getSiteUrl() + href;
			String title = emtTitle.text();
			if (tempFilePath != null) {
				// 数据保存对像
				Data data = new Data();
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				List<File> picList = new ArrayList<File>();
				if (dest != null) {
					picList.add(dest);
				}
				String html = Tools.getRequest(contentUrl);
				Elements container = Tools.getBody(".standard-article", html);
				container.select(".embed--iframe-container").remove();
				container.select(".standard-article--secondary-content").remove();
				container.select(".zoomable-expand").remove();
				container.select(".embedded-image--lead-image-share").remove();
				container.select(".embedded-image--lead-copyright").remove();
				container.select(".social-byline").remove();
				Elements cimg = container.select("img");
				for (Element cimgemt : cimg) {
					String cimgSrc = cimgemt.attr("data-src");
					if (!"".equals(cimgSrc)) {
						String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
						File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
						String mydest = getMySiteImgSrc(cdest);
						cimgemt.attr("src", mydest);
					}
				}
				String content = container.toString();
				data.setTitle(title);// title
				data.setContent(content);// 获取内容
				data.setKeywords(text);
				URL url;
				try {
					url = new URL(contentUrl);
					contentUrl = url.getPath();
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}
				data.setContentId(Tools.string2MD5(contentUrl));
				data.setPicList(picList);
				whenOneData(data);
			}
		}
	}
}
