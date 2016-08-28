package com.modern.datacollect.impl.onebatch;

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
import com.modern.datacollect.impl.Tools;

public class GarancedoreStoriesCollector extends Collector {

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
			config.setSiteUrl("http://www.garancedore.com/fr/category/stories/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'page/{page}'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
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
				if (page == 1) {
					url = config.getSiteUrl();
				} else {
					url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				}
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'page/{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				if (html == null) {
					stop();
					break;
				}
				Elements body = Tools.getBody("div[class=\"row\"]", html);
				Elements pager = body.select(".pager");
				pager.select("li[class=\"next active\"]").remove();
				String num = pager.select("a").last().text();
				// body.select("div[class=\"col-md-4 col-sm-6 col-xs-12 hidden-xs hidden-md hidden-lg\"]").remove();
				// body.select("div[class=\"col-md-12 col-sm-12 col-xs-12\"]").remove();
				// body.select("div[class=\"col-md-4 col-sm-6 col-xs-12 hidden-sm\"]").remove();
				// body.select("div[class=\"col-sm-12 visible-sm\"]").remove();

				this.dealwith(body.select(".col-md-4"), tempFileDir, targetFileDir);
				this.dealwith(body.select(".col-md-8"), tempFileDir, targetFileDir);
				if (page >= Integer.parseInt(num)) {
					stop();
					break;
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		String tmpTitle = null;
		for (Element elm : body) {
			try {
				String title = elm.select("h3[class=\"post-title\"]").text();
				if (!"".equals(title)) {
					if (tmpTitle == null) {
						tmpTitle = title;
					}

					Data data = new Data();
					String href = elm.select("h3[class=\"post-title\"]").select("a").attr("href");

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
					String imgSrc = elm.select(".post-thumb").select("img").attr("data-src");
					String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
					File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
					if (dest != null) {
						List<File> picList = new ArrayList<File>();
						picList.add(dest);
						data.setPicList(picList);
					}
					String html = Tools.getRequest(href);
					Elements ebody = Tools.getBody(".main-post-content", html);
					Elements footer = ebody.select(".post-footer");
					Elements category = footer.select("div[class=\"item-taxonomy item-category\"]");
					Tools.clearsAttr(category);
					Elements post_tag = footer.select("div[class=\"item-taxonomy item-post_tag\"]").select("a");
					StringBuffer keywords = new StringBuffer();
					for (int i = 0; i < post_tag.size(); i++) {
						if (i > 0) {
							keywords.append(",");
						}
						keywords.append(post_tag.get(i).text());
					}

					ebody = ebody.select(".post-txt-loop");
					Elements subinfos = ebody.select(".post-header").select(".post-subinfos");
					Tools.clearsAttr(subinfos);
					ebody = ebody.select(".post-content");

					Elements cimg = ebody.select("img");
					for (Element cimgemt : cimg) {
						String cimgSrc = cimgemt.attr("src");
						if ("".equals(cimgSrc)) {
							cimgSrc = cimgemt.attr("data-src");
						}
						if (!"".equals(cimgSrc)) {
							String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
							File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
							String mydest = getMySiteImgSrc(cdest);
							if (mydest != null)
								cimgemt.attr("src", mydest);
							cimgemt.removeAttr("sizes");
							cimgemt.removeAttr("srcset");
							cimgemt.removeAttr("class");
						}
					}
					Tools.clearsAttr(ebody);
					// 获取内容
					String content = ebody.toString() + subinfos.toString() + category.toString();

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
