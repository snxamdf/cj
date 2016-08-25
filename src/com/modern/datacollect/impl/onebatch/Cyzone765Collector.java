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

public class Cyzone765Collector extends Collector {

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
			config.setSiteUrl("http://www.cyzone.cn/category/765/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'index_{page}.html'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		String dataUrl = null;
		Integer page = 1;
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
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'index_{page}.html'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url, "UTF-8");
				Elements body = Tools.getBody(".school-list", html);
				Elements pages = body.select(".page-box");
				pages.select("#lastpage").remove();
				String num = pages.select("a").last().text();
				this.dealwith(body.select(".article-item"), tempFileDir, targetFileDir);
				if (page >= Integer.parseInt(num)) {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			if (page >= 90) {
				stop();
				break;
			}
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				Data data = new Data();
				String title = elm.select(".item-title").text();
				String href = elm.select(".item-title").attr("href");
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
				String imgSrc = elm.select("img").attr("src");
				String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest(href, "UTF-8");
				Elements articlehd = Tools.getBody(".article-hd", html);
				articlehd = articlehd.select(".author-time");
				Elements ebody = Tools.getBody(".article-content", html);
				Elements tags = Tools.getBody(".article-tags", html);
				ebody.select("a").attr("href", "javascript:void(0)");
				tags.select("a").attr("href", "javascript:void(0)");
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
				Tools.clearsAttr(tags);
				Tools.clearsAttr(ebody);
				Tools.clearsAttr(articlehd);
				// 获取内容
				String content = tags.toString() + ebody.toString() + articlehd.toString();
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}
}
