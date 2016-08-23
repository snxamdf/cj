package com.modern.datacollect.impl;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Config;

public class FashioBeautyNewsCollector extends FashiosCollector {

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
			config.setSiteUrl("http://www.fashiongonerogue.com/beauty-news/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
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
				if (page == 1) {
					url = config.getSiteUrl();
				} else {
					url = config.getSiteUrl() + "/page/" + page;
				}
				config.setSiteConfig("{'page':" + page + "}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				Elements content = Tools.getBody(".content", html);
				Elements article = content.select("article");
				this.dealwith(article, tempFileDir, targetFileDir, config);
				if (article.size() < 2) {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			page++;
		}
	}
}
