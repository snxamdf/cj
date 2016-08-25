package com.modern.datacollect.impl.twobatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;

public abstract class TroversCollector extends Collector {
	public void dealwith(JSONArray array, String tempFileDir, String targetFileDir) {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("title");
				String place_name = json.getString("place_name");
				String url = json.getString("url");
				String user_img = json.getString("user_img");

			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {

	}
}
