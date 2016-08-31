package com.modern.datacollect.impl.twobatch.homedit;

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

public class Homedit extends Collectors {

	@Override
	public void begin() {
		Config config = getConfig();
		String targetFileDir = getSaveFileDir();
		String tempFileDir = getTempFileDir();
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			config.setSiteUrl("http://www.homedit.com/");
			config.setSiteConfig("{'page':3,'dataUrl':'page/{page}'}");
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
		String html = null;
		for (;;) {
			try {
				url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'page/{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				if (html != null) {
					Elements body = Tools.getBody("#content", html);
					body = body.select("article");
					this.dealwith(body, tempFileDir, targetFileDir);
					if (body.size() < 1) {
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
			page++;
			if (page > 1000) {
				stop();
				break;
			}
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				Elements postimage = elm.select(".post-image");

				String imgSrc = postimage.select("img").attr("src");
				String href = postimage.select("a").attr("href");
				String title = postimage.select("a").attr("title");

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

				String html = Tools.getRequest1(href);
				Elements contentElm = Tools.getBody("#content", html);
				contentElm.select(".share-post-wrapper").remove();
				contentElm.select("header").remove();
				Elements main = contentElm.select("article").select("main");
				main.select(".share-post-wrapper").remove();
				main.select("header").remove();
				main.select(".goto-gallery").remove();
				Elements author = contentElm.select(".author-description").select("h4");
				this.downImg(main, tempFileDir, targetFileDir);

				Tools.clearsAttr(main);

				String content = main.toString();
				content += bdiv + "作者 : " + author.text() + ediv;
				content += "<br/>" + bdiv + ba.replace("{href}", href) + "原文链接 : " + href + ea + ediv;
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				} else {
					cimgemt.remove();
				}
			} else {
				cimgemt.remove();
			}
		}
	}
}
