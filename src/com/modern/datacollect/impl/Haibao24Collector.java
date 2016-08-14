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

public class Haibao24Collector extends Collector {

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
			config.setSiteUrl("http://beauty.haibao.com/rightnow/24/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'{page}.htm'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
		}
		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		Integer page = 1;
		String dataUrl = null;
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
				url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'{page}.htm'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url, "UTF-8");
				Elements body = Tools.getBody("div[class=\"hb_fl contentleft\"]", html);
				Elements pages = Tools.getBody(".pages", html);
				body = body.select(".todya_new_list").select("li");
				pages.select(".next").remove();
				String num = pages.select("a").last().text();
				this.dealwith(body, tempFileDir, targetFileDir, config);
				if (page >= Integer.parseInt(num)) {
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
				String title = emt.select("div.hb_fl").text();
				String href = emt.select("div.hb_fl").select("a").attr("href");
				String imgSrc = emt.select("a").eq(0).select("img").attr("data-lazy-src");
				Data data = new Data();
				URL url = null;
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
				String html = Tools.getRequest1(href);
				Elements ebody = Tools.getBody("div[class=\"wr content\"]", html);

				Elements ebody1 = ebody.select("div[class=\"hb_fl contentleft\"]");
				String body1 = ebody1.select(".desc_content").toString();
				Elements btcenter = Tools.getBody("#btcenter", html);
				btcenter.select("a").attr("href", "javascript:void(0)");
				Elements ebody2 = ebody1.select("#jsArticleDesc");
				this.downImg(ebody2, tempFileDir, targetFileDir);
				String content = body1 + ebody2.toString() + btcenter.toString();
				Elements pages = ebody.select(".pages");
				String result = null;
				if (pages.size() > 0) {
					pages.select(".next").remove();
					String pageTemp = "http://" + url.getHost().toString() + href.split("\\.")[0] + "_{page}.htm";
					String num = pages.select("a").last().text();
					result = this.contentPage(Integer.valueOf(num), pageTemp, tempFileDir, targetFileDir);
				}
				if (result != null) {
					content += result;
				}
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}

	private String contentPage(Integer num, String pageTemp, String tempFileDir, String targetFileDir) {
		StringBuffer result = new StringBuffer();
		for (int i = 2; i < num; i++) {
			String url = pageTemp.replace("{page}", i + "");
			String html = Tools.getRequest1(url);
			Elements ebody = Tools.getBody("div[class=\"wr content\"]", html);
			String body1 = ebody.select(".desc_content").toString();
			result.append(body1);
			Elements body2 = ebody.select("#jsArticleDesc");
			this.downImg(body2, tempFileDir, targetFileDir);
			result.append(body2.toString());
		}
		return result.toString();
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		ebody.select("a").attr("href", "javascript:void(0)");
		Elements cimg = ebody.select("img");
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
	}
}
