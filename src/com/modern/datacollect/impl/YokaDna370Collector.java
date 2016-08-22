package com.modern.datacollect.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;

public class YokaDna370Collector extends Collector {

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
			config.setSiteUrl("http://www.yoka.com/dna/203/370/index.html");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'currUrl':'http://www.yoka.com/dna/203/370/index.html'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
			writeIndex("index.html", "</br><a href=\"" + config.getSiteUrl() + "\" target='_blank'>" + config.getSiteUrl() + "</a><br/><br/>");
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		Integer page = 1;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
			url = obj.getString("currUrl");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		for (;;) {
			try {
				config.setSiteConfig("{'page':" + page + ",'currUrl':'" + url + "'}");
				updateSiteConfig(config.getSiteConfig());
				String html = Tools.getRequest1(url);
				Elements body = Tools.getBody("#pbox", html);
				Elements pages = Tools.getBody("#m-pages", html);
				body = body.select("dl");
				this.dealwith(body.select("dl"), tempFileDir, targetFileDir, config);
				url = this.nextUrl(page, pages);
				if (url == null) {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			try {
				String imgSrc = emt.select("dt").select("img").attr("_src");
				if (imgSrc == null || "".equals(imgSrc) || "null".equals(imgSrc)) {
					System.out.println();
				}
				if (!"".equals(imgSrc)) {
					// 数据保存对像
					Data data = new Data();
					String href = emt.select("dt").get(0).parent().parent().attr("href");
					href = "http://www.yoka.com" + href;
					String title = emt.select("dt").select("img").attr("alt");
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
					List<File> picList = new ArrayList<File>();
					if (dest != null) {
						picList.add(dest);
					}
					String html = Tools.getRequest1(href);
					Elements ebody = Tools.getBody("#topic-context", html).select(".conts");
					ebody.select("h1").remove();
					ebody.select("a").attr("href", "javascript:void(0)");
					String time = ebody.select(".time").toString();
					Elements cimg = ebody.select("img");
					for (Element cimgemt : cimg) {
						String cimgSrc = cimgemt.attr("_src");
						cimgemt.removeAttr("_src");
						if (!"".equals(cimgSrc)) {
							String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
							File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
							String mydest = getMySiteImgSrc(cdest);
							if (mydest != null)
								cimgemt.attr("src", mydest);
						}
					}
					Elements ctags = Tools.getBody(".ctags", html);
					ctags.select("a").attr("href", "javascript:void(0)");
					String keywords = StringUtils.join(ctags.select("a").toArray(), ",");
					data.setKeywords(keywords);

					String content = ebody.toString() + time;
					data.setTitle(title);// title
					data.setContent(content);// 获取内容
					data.setPicList(picList);
					whenOneData(data);
				}
			} catch (Exception e) {
			}
		}
	}

	public String nextUrl(Integer page, Elements pages) {
		pages = pages.select("td");
		for (int i = 0; i < pages.size(); i++) {
			String text = pages.get(i).text().trim();
			if (page.toString().equals(text)) {
				if (i < pages.size() - 1) {
					return "http://www.yoka.com" + pages.get(i + 1).select("a").attr("href");
				} else {
					return null;
				}
			}
		}
		return null;
	}
}
