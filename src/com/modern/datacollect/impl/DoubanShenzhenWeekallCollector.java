package com.modern.datacollect.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;

public class DoubanShenzhenWeekallCollector extends Collector {

	@Override
	public void begin() {
		// 默认注入的配置
		Config config = getConfig();
		// 图片保存目录
		String targetFileDir = getSaveFileDir();
		// 图片**临时**保存目录
		String tempFileDir = getTempFileDir();
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			// 配置网站url 这个url是一个主要的，如果在抓取的时候变动需要自己拼接
			config.setSiteUrl("https://shenzhen.douban.com/events/future-all");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':0,'dataUrl':'?start={page}'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		String dataUrl = null;
		Integer page = 0;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
			dataUrl = obj.getString("dataUrl");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String html = null;
		for (;;) {
			try {
				url = config.getSiteUrl() + dataUrl.replace("{page}", (page * 10) + "");
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'?start={page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				Elements body = Tools.getBody("#db-events-list", html);
				Elements pages = body.select("div[class=\"paginator\"]");
				pages.select(".next").remove();
				this.dealwith(body.select(".events-list").select(".list-entry"), tempFileDir, targetFileDir);
				String num = pages.select("a").last().text();
				if (page >= Integer.parseInt(num)) {
					stop();
					break;
				}
			} catch (Exception e) {
			}

			page++;
		}
	}

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

				// 获取内容
				String content = ebody.toString();
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
