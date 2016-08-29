package com.modern.datacollect.impl.onebatch;

import java.io.File;
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

public class LookbookCollector extends Collector {

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
			config.setSiteUrl("http://lookbook.nu/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		Integer page = 1;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String html = null;

		for (;;) {
			try {
				url = config.getSiteUrl() + page.toString();
				config.setSiteConfig("{'page':" + page + "}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				if (html == null) {
					stop();
					break;
				}
				Elements body = Tools.getBody("#looks", html);
				body = body.select(".look_v2");
				this.dealwith(body, tempFileDir, targetFileDir);
				if (body.size() == 0) {
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
		for (Element elm : body) {
			try {
				String title = elm.select(".title").text();
				String href = elm.select(".title").attr("href");
				href = "http://lookbook.nu" + href;
				String imgSrc = elm.select(".look_photo").select("img").attr("src");
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
				Elements subheaderlinespaced = Tools.getBody("p[class=\"subheader linespaced\"]", html);
				// Elements ebody = Tools.getBody("div[itemprop=\"encoding\"]",
				// html).select("#look_photo_container");
				// Elements itemtaglist = Tools.getBody(".item-tag-list", html);

				Elements bottomspaced = Tools.getBody(".bottomspaced", html);
				String author = bottomspaced.select(".name").text();
				subheaderlinespaced.select("a[data-page-track]").eq(0).html("photos by " + author);

				// this.downImg(ebody, tempFileDir, targetFileDir);
				Tools.clearsAttr(subheaderlinespaced);
				// Tools.clearsAttr(ebody);
				// Tools.clearsAttr(itemtaglist);
				String content = subheaderlinespaced.toString();/*
																 * +
																 * ebody.toString
																 * () +
																 * itemtaglist
																 * .toString()+
																 * "<br/><div>作者 : "
																 * + author +
																 * "</div>";
																 */
				// content += "<br/><div>原文链接 : <a href=\"" + href + "\">" +
				// href + "</a></div>";
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
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
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				}

			}
		}
	}

}
