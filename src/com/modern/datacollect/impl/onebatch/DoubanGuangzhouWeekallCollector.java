package com.modern.datacollect.impl.onebatch;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Config;
import com.modern.datacollect.impl.Tools;

public class DoubanGuangzhouWeekallCollector extends DoubansCollector {

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
			config.setSiteUrl("https://guangzhou.douban.com/events/future-all");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':0,'dataUrl':'?start={page}'}");
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
				url = config.getSiteUrl() + dataUrl.replace("{page}", (page * 10) + "");
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'?start={page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				Elements body = Tools.getBody("#db-events-list", html);
				Elements pages = body.select("div[class=\"paginator\"]");
				pages.select(".next").remove();
				this.dealwith(body.select(".events-list").select(".list-entry"), tempFileDir, targetFileDir);
				String num = pages.select("a").last().text();
				if (page >= Integer.parseInt(num)) {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			page++;
		}
	}
}
