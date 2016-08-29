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

public class PocoResListCollector extends Collector {

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
			config.setSiteUrl("http://food.poco.cn/resList.php#location_id1=");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'http://food.poco.cn/module/get_res_topic_list.js.php?p={page}&food_series=0'}");
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
				url = dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'http://food.poco.cn/module/get_res_topic_list.js.php?p={page}&food_series=0'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				Elements body = Tools.getBody(".w768", html);
				Elements pages = Tools.getBody(".show_page", html);
				if (!pages.toString().contains("下一页")) {
					stop();
					break;
				}
				this.dealwith(body, tempFileDir, targetFileDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
			page++;

		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".text_box").select(".rbox1").text();
				String href = elm.select(".text_box").select(".rbox1").select("a").attr("href");
				String imgSrc = elm.select(".img_box").select("img").attr("src");
				StringBuffer keywords = new StringBuffer();
				Elements elmKey = elm.select(".rbox3").select("a");
				for (int i = 0; elmKey != null && i < elmKey.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(elmKey.get(i).text());
				}
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
				String html = Tools.getRequest1(href);
				Elements ebodypd120 = Tools.getBody(".pdl20", html);
				Elements ebody = ebodypd120.select(".text_center");
				if (ebody.size() == 0) {
					ebody = ebodypd120.select(".pic_text_content");
				}
				if (ebody.select(".content_text_con").size() > 0 && ebody.select(".content_text_con").last().text().indexOf("目录：") != -1) {
					ebody.select(".content_text_con").last().remove();
				}
				Elements info = Tools.getBody("div[class=\"tc mt5 zt_listItem_info pb10\"]", html);
				Tools.clearsAttr(info);
				Elements source1 = info.select("span").eq(1);
				Elements author = info.select("span").eq(2);
				Elements author2 = info.select("span").eq(3);

				this.downImg(ebody, tempFileDir, targetFileDir);

				Tools.clearsAttr(ebody);

				Elements page = Tools.getBody(".ztpage", html);
				page = page.select("a");
				String ecotent = "";
				if (page.size() > 0) {
					String p = page.get(page.size() - 2).text();
					String url = href.substring(0, href.lastIndexOf(".html")) + "-p-{page}.html";
					ecotent = this.ebody(url, Integer.parseInt(p), tempFileDir, targetFileDir);
				}
				String content = source1.toString() + "&nbsp;" + author.toString() + "&nbsp;" + author2.toString() + "&nbsp;<br/>" + ebody.toString() + ecotent;
				content += "<br/><div>原文链接 : " + href + "</div>";
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String ebody(String url, int p, String tempFileDir, String targetFileDir) {
		StringBuffer result = new StringBuffer();
		for (int i = 2; i < p; i++) {
			try {
				String href = url.replace("{page}", i + "");
				String html = Tools.getRequest1(href);
				Elements ebody = Tools.getBody(".pdl20", html);
				ebody = ebody.select(".text_center");
				if (ebody.select(".content_text_con").last().text().indexOf("目录：") != -1) {
					ebody.select(".content_text_con").last().remove();
				}
				this.downImg(ebody, tempFileDir, targetFileDir);
				Tools.clearsAttr(ebody);
				result.append(ebody.toString());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return result.toString();
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
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
