package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSON;
import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public class T36krNews extends Collector {

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
			config.setSiteUrl("http://36kr.com/news");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':0,'dataUrl':'http://36kr.com/api/info-flow/main_site/posts?column_id=&b_id={page}&per_page=20'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
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
		String html = null;
		String firstId = null;
		String b_id = page.toString();
		for (;;) {
			try {
				url = dataUrl.replace("{page}", b_id);
				config.setSiteConfig("{'page':" + (b_id) + ",'dataUrl':'http://36kr.com/api/info-flow/main_site/posts?column_id=&b_id={page}&per_page=20'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				JSONObject json = new JSONObject(html);
				json = json.getJSONObject("data");
				if (json != null && json.length() > 0) {
					JSONArray array = json.getJSONArray("items");
					if (array != null && array.length() > 0) {
						if (firstId == null) {
							firstId = array.getJSONObject(0).getString("id");
						} else {
							if (firstId.equals(array.getJSONObject(0).getString("id"))) {
								stop();
								break;
							}
						}
						b_id = array.getJSONObject(array.length() - 1).getString("id");
						this.dealwith(array, tempFileDir, targetFileDir);
					} else {
						stop();
						break;
					}
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
				Data data = new Data();
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("title");
				String imgSrc = json.getString("cover");
				String extraction_tags = json.getString("extraction_tags");
				com.alibaba.fastjson.JSONArray tagArr = JSON.parseArray(extraction_tags);
				StringBuffer keywords = new StringBuffer();
				for (int j = 0; tagArr != null && j < tagArr.size(); j++) {
					if (j > 0) {
						keywords.append(",");
					}
					keywords.append(tagArr.getJSONArray(j).getString(0));
				}
				String href = "http://36kr.com/p/" + json.getString("id") + ".html";
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
				String html = Tools.getRequest1(href);
				Elements ebody = Tools.getBody("script", html);
				html = null;
				for (Element element : ebody) {
					if (element.toString().indexOf("var props") != -1) {
						String[] dataa = element.data().toString().split("var");
						for (String variable : dataa) {
							variable = variable.trim();
							if (variable.contains("=") || variable.contains("props")) {
								html = variable.substring(6, variable.length());
								break;
							}
						}
						break;
					}
				}
				if (html != null) {
					JSONObject jsonObj = new JSONObject(html);
					jsonObj = jsonObj.getJSONObject("detailArticle|post");
					String content = jsonObj.getString("content");
					org.jsoup.nodes.Document doc = Jsoup.parse(content);
					ebody = doc.select("body");
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
					Tools.clearsAttr(ebody);
					// 获取内容
					content = ebody.html();
					JSONObject user = json.getJSONObject("user");
					if (user != null && user.length() > 0) {
						content = "<div>作者 ： " + user.getString("name") + "</div>" + content;
					}
					data.setTitle(title);
					data.setContent(content);
					data.setKeywords(keywords.toString());
					whenOneData(data);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
