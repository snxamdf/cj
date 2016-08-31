package com.modern.datacollect.impl.twobatch.notcot;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Collectors;
import com.modern.datacollect.impl.Tools;

public class Notcot extends Collectors {

	@Override
	public void begin() {
		Config config = getConfig();
		String targetFileDir = getSaveFileDir();
		String tempFileDir = getTempFileDir();
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			config.setSiteUrl("http://www.notcot.org/");
			config.setSiteConfig("{'page':1,'dataUrl':'page/{page}'}");
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
			writeIndex("index.html", "</br><a href=\"" + config.getSiteUrl() + "\" target='_blank'>" + config.getSiteUrl() + "</a><br/><br/>");
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
		url = config.getSiteUrl();
		String html = null;
		html = Tools.getRequest1(url);
		Elements numbers = Tools.getBody("#numbers", html);
		String number = numbers.select("#currentPage").select(".page").text();
		page = Integer.parseInt(number);
		for (;;) {
			try {
				url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'page/{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				if (html != null) {
					Elements body = Tools.getBody("#contentwrap", html);
					body = body.select(".shadow");
					this.dealwith(body, tempFileDir, targetFileDir);
					if (page <= 1) {
						stop();
						break;
					}
				} else {
					stop();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			page--;
			if (page <= 1) {
				stop();
				break;
			}
		}
	}

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

				String author = "<br/><div>Photos by : " + submitter + "</div>";
				String sourceUrl = author + "<br/>";
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
