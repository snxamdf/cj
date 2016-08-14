package com.modern.datacollect.impl;

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

public class PocoCommendDetailCollector extends Collector {
	// 页面内容是flash加载获取不到
	@Override
	public void begin() {
		// // 默认注入的配置
		// Config config = getConfig();
		// // 图片保存目录
		// String targetFileDir = getSaveFileDir();
		// // 图片**临时**保存目录
		// String tempFileDir = getTempFileDir();
		// if (config == null) {// 开发时用到的，自己配置
		// config = new Config();
		// // 配置网站url 这个url是一个主要的，如果在抓取的时候变动需要自己拼接
		// config.setSiteUrl("http://food.poco.cn/commend_list.php");
		// // 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
		// config.setSiteConfig("{'page':1,'dataUrl':'http://food.poco.cn/commend_list.htx&p={page}&good=0&o=&show_type=img#main_list'}");
		// // 文件的保存正式目录
		// targetFileDir = "D:\\targetFileDir\\";
		// // 文件的保存临时目录
		// tempFileDir = "D:\\tempFileDir\\";
		// }
		//
		// Tools.mkDir(new File(targetFileDir));
		// Tools.mkDir(new File(tempFileDir));
		// String url = null;
		// String dataUrl = null;
		// Integer page = 0;
		// try {
		// JSONObject obj = new JSONObject(config.getSiteConfig());
		// page = obj.getInt("page");
		// dataUrl = obj.getString("dataUrl");
		// } catch (JSONException e1) {
		// e1.printStackTrace();
		// }
		// String html = null;
		// for (;;) {
		// try {
		// url = dataUrl.replace("{page}", page.toString());
		// config.setSiteConfig("{'page':" + (page) +
		// ",'dataUrl':'http://food.poco.cn/commend_list.htx&p={page}&good=0&o=&show_type=img#main_list'}");
		// updateSiteConfig(config.getSiteConfig());
		// html = Tools.getRequest(url);
		// Elements body = Tools.getBody("li", html);
		// Elements pages = Tools.getBody(".page", html);
		// this.dealwith(body, tempFileDir, targetFileDir);
		// if (!pages.toString().contains("下一页")) {
		// stop();
		// break;
		// }
		// } catch (Exception e) {
		// e.printStackTrace();
		// }
		// page++;
		//
		// }
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".show_author").select("a").text();
				String href = elm.select(".text_box").select("a").attr("href");
				String imgSrc = elm.select(".imgbox").select("img").attr("src");
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
				Elements ebody = Tools.getBody(".content_text_con", html);
				Elements time = Tools.getBody("div[class=\"tc mt5 zt_listItem_info pb10\"]", html);
				time.select("a").attr("href", "javascript:void(0)");
				time = time.select("span").eq(0);

				this.downImg(ebody, tempFileDir, targetFileDir);

				String content = ebody.toString() + time.toString();
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		ebody.select("a").attr("href", "javascript:void(0)");
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("data-src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
					cimgemt.removeAttr("data-src");
				}

			}
		}
	}
}
