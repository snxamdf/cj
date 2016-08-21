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

public class YokaShouShenCollector extends Collector {

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
			config.setSiteUrl("http://www.yoka.com/shoushen/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':400,'dataUrl':'/list_{page}.shtml'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
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
				if (page == 1) {
					url = config.getSiteUrl();
				} else {
					url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				}
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'/list_{page}.shtml'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url, "GB2312");
				Elements body = Tools.getBody("#gotolist", html);
				Elements pages = body.select(".pages");
				pages.select(".next").remove();
				String num = pages.select("a").last().text();

				body = body.select(".listInfo");
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
				Elements emtTitle = emt.select("dd").select("h3").select("a");
				String title = emtTitle.text();
				String href = emtTitle.attr("href");
				Elements emtImg = emt.select("dt").select("a").select("img");
				String imgSrc = emtImg.attr("src");
				if ("".equals(imgSrc)) {
					System.out.println();
				}
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
				List<File> picList = new ArrayList<File>();
				if (!"".equals(tempFilePath)) {
					File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
					if (dest != null) {
						picList.add(dest);
					}
				}
				String html = Tools.getRequest(href, "GB2312");
				String content = this.ebody(html, 0, "", null, tempFileDir, targetFileDir, config);
				Elements source = Tools.getBody(".infoTime", html);
				source.select("a").attr("href", "javascript:void(0)");
				if (source.toString().equals("")) {
					source = Tools.getBody(".time2", html);
					source.select("#share").remove();
				}
				source.select(".textShare").remove();
				content += source.toString();
				data.setTitle(title);// title
				data.setContent(content);// 获取内容
				data.setPicList(picList);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}

	public String ebody(String html, int idx, String result, String[] urls, String tempFileDir, String targetFileDir, Config config) {
		int f = 1;
		Elements ebody = Tools.getBody("#viewbody", html);
		Elements pages = null;
		if (ebody.size() == 0) {
			ebody = Tools.getBody(".textCon", html);
			f = 3;
			if (ebody.size() == 0) {
				ebody = Tools.getBody(".con", html);
				f = 2;
			}
		}
		if (ebody.size() < 1) {
			return result;
		}
		if (urls == null) {
			if (f == 1) {
				pages = ebody.select("#_function_code_page").select("a");
				if (pages.size() > 0) {
					urls = new String[pages.size() - 1];
					for (int i = 0; i < pages.size() - 1; i++) {
						urls[i] = pages.get(i).attr("href");
					}
				}
			} else if (f == 2) {
				pages = ebody.select("p[align]").select("a");
				if (pages.size() > 0) {
					urls = new String[pages.size() - 1];
					for (int i = 0; i < pages.size() - 1; i++) {
						urls[i] = pages.get(i).attr("href");
					}
				}
			} else if (f == 3) {
				pages = Tools.getBody(".pages", html);
				pages.select(".prev").remove();
				pages.select(".next").remove();
				pages = pages.select("a");
				if (pages.size() > 0) {
					urls = new String[pages.size()];
					for (int i = 0; i < pages.size(); i++) {
						urls[i] = pages.get(i).attr("href");
					}
				}
			}
		}
		if (f == 1) {
			ebody.select("#_function_code_page").remove();
			ebody.select(".pb").remove();
		} else if (f == 2) {
			ebody.select("p[align]").remove();
			ebody.select("p[red]").remove();
		}
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
		ebody.select("a").attr("href", "javascript:void(0)");
		result += ebody.toString();
		if (urls != null) {
			for (; idx < urls.length;) {
				String url = "http://www.yoka.com/" + urls[idx];
				html = Tools.getRequest(url, "GB2312");
				return this.ebody(html, ++idx, result, urls, tempFileDir, targetFileDir, config);
			}
		}
		return result;
	}
}
