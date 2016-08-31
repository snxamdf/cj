package com.modern.datacollect.impl.twobatch.baubauhaus;

import java.io.File;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Config;
import com.modern.datacollect.impl.Tools;

public class GraphicDesign extends BaubauhausCollectors {

	@Override
	public void begin() {
		Config config = getConfig();
		String targetFileDir = getSaveFileDir();
		String tempFileDir = getTempFileDir();
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			config.setSiteUrl("http://www.baubauhaus.com/graphic-design");
			config.setSiteConfig("{'page':1,'dataUrl':'/page/{page}'}");
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
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'/page/{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				if (html != null) {
					Elements body = Tools.getBody(".wall-columns", html);
					body = body.select(".image");
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
			if (page > 600) {
				stop();
				break;
			}
		}
	}
}
