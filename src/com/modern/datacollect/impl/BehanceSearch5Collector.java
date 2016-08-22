package com.modern.datacollect.impl;

import java.io.File;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Config;

public class BehanceSearch5Collector extends BehanceSearchsCollector {
	@Override
	public void begin() {
		// 默认注入的配置
		Config config = getConfig();
		// 图片保存目录
		String targetFileDir = getSaveFileDir();
		// 图片**临时**保存目录
		String tempFileDir = getTempFileDir();
		String field = "5";
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			// 配置网站url 这个url是一个主要的，如果在抓取的时候变动需要自己拼接
			config.setSiteUrl("https://www.behance.net/search?field=5&content=projects&sort=appreciations&time=week");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':0,'dataUrl':'https://www.behance.net/search?ts={time}&ordinal={page}&per_page=24&field=" + field + "&content=projects&sort=appreciations&time=week'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
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
				url = dataUrl.replace("{time}", new Date().getTime() + "").replace("{page}", page.toString()).replace("{field}", field);
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'https://www.behance.net/search?ts={time}&ordinal={page}&per_page=24&field=" + field + "&content=projects&sort=appreciations&time=week'}");
				updateSiteConfig(config.getSiteConfig());
				html = getRequest(url);
				if (html != null) {
					Elements body = Tools.getBody("#content", html);
					body = body.select(".js-item");
					this.dealwith(body, tempFileDir, targetFileDir);
					if (body.size() == 0) {
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
			page = page + 24;

		}
	}
}
