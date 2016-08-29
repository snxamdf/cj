package com.modern.datacollect.impl.twobatch.darbysmart;

import java.io.File;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.modern.datacollect.api.Config;
import com.modern.datacollect.impl.Tools;

public class Jewelry extends DarbysmartCollectors {

	@Override
	public void begin() {
		Config config = getConfig();
		String targetFileDir = getSaveFileDir();
		String tempFileDir = getTempFileDir();
		String filter = "jewelry";
		if (config == null) {
			config = new Config();
			config.setSiteUrl("http://www.darbysmart.com/c/jewelry/");
			config.setSiteConfig("{'page':1,'dataUrl':'http://www.darbysmart.com/api/listicles?page={page}&per_page=20&categoryFilter=" + filter + "'}");
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
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'http://www.darbysmart.com/api/listicles?page={page}&per_page=20&categoryFilter=" + filter + "'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				if (html != null) {
					JSONArray array = new JSONArray(html);
					JSONObject total_entries = array.getJSONObject(0);
					array = array.getJSONArray(1);
					this.dealwith(array, tempFileDir, targetFileDir);
					if (array.length() == 0) {
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
		}
	}

}
