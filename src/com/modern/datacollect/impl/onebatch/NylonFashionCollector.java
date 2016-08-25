package com.modern.datacollect.impl.onebatch;

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
import com.modern.datacollect.impl.Tools;

public class NylonFashionCollector extends Collector {

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
			config.setSiteUrl("http://www.nylon.com/fashion");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'https://api.nylon.com/api/v2/page/fashion/page-{page}'}");
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
				url = dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'https://api.nylon.com/api/v2/page/fashion/page-{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				JSONObject obj = new JSONObject(html);
				obj = obj.getJSONObject("posts");
				if (obj.length() > 0) {
					JSONArray array = obj.getJSONArray("list");
					this.dealwith(array, tempFileDir, targetFileDir, config);
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

	public void dealwith(JSONArray array, String tempFileDir, String targetFileDir, Config config) throws JSONException {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject dataObj = array.getJSONObject(i);
				String href = "http://www.nylon.com/articles/" + dataObj.getString("permalink");
				String title = dataObj.getString("page_title");
				JSONArray keysArr = dataObj.getJSONArray("tags");
				StringBuffer keywords = new StringBuffer();
				for (int j = 0; j < keysArr.length(); j++) {
					if (j > 0) {
						keywords.append(",");
					}
					keywords.append(keysArr.get(j).toString());
				}
				JSONObject featuredImage = dataObj.getJSONObject("featured_image");
				String imgSrc = "http:" + featuredImage.getString("url");
				// 数据保存对像
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
				// 通过工具类 将图片保存到正式目录
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					// 将文件对像保存到picList
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest(href);
				Elements ebody = Tools.getBody("#article-content-body", html);
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
