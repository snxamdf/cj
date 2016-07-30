package com.modern.datacollect.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;

public class GqTag39705Collector extends Collector {

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
			config.setSiteUrl("http://www.gq.com.cn/tag/39705");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'http://www.gq.com.cn/front_ajax/read_list_more/39705?type=2&pg={page}'}");
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
			url = dataUrl.replace("{page}", page.toString());
			config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'http://www.gq.com.cn/front_ajax/read_list_more/39705?type=2&pg={page}'}");
			updateSiteConfig(config.getSiteConfig());
			html = Tools.getRequest(url);
			try {
				JSONObject object = new JSONObject(html);
				JSONArray array = object.getJSONArray("data");
				if (array != null && array.length() > 0) {
					this.dealwith(array, tempFileDir, targetFileDir);
				} else {
					stop();
					break;
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
			page++;
		}
	}

	public void dealwith(JSONArray array, String tempFileDir, String targetFileDir) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject jsonObj = array.getJSONObject(i);
				Data data = new Data();
				String title = jsonObj.getString("title");
				String imgSrc = jsonObj.getString("normalpic");
				String href = jsonObj.getString("link");
				StringBuffer keywords = new StringBuffer();
				JSONArray tagObj = jsonObj.getJSONArray("tag");
				for (int j = 0; tagObj != null && j < tagObj.length(); j++) {
					if (j > 0) {
						keywords.append(",");
					}
					keywords.append(tagObj.getJSONObject(j).getString("tagname"));
				}
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
				Elements ebody = null;
				if (href.indexOf("pic_") != -1) {
					ebody = Tools.getBody("#left", html);
				} else if (href.indexOf("news_") != -1) {
					ebody = Tools.getBody(".content", html);
				}
				if (ebody == null) {
					return;
				}
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
						cimgemt.removeAttr("data-original");
						cimgemt.removeAttr("realsrc");
						cimgemt.removeAttr("class");
						cimgemt.removeAttr("onload");
						cimgemt.removeAttr("onclick");
					}
				}

				// 获取内容
				String content = ebody.toString();
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}
}
