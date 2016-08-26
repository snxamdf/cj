package com.modern.datacollect.impl.twobatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class TroversCollector extends Collector {
	public void dealwith(JSONArray array, String tempFileDir, String targetFileDir) {
		System.out.println(array.toString());
		String photo2x = "http://media4.trover.com/T/{photo_id}/fixedw_large_2x.jpg";
		String photo4x = "http://media4.trover.com/T/{photo_id}/fixedw_large_4x.jpg";
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("place_name");
				String desc = json.getString("desc");
				String href = "http://www.trover.com" + json.getString("url");
				String photo_id = json.getString("photo_id");
				String user_name = json.getString("user_name");
				String imgSrc = photo2x.replace("{photo_id}", photo_id);

				Data data = new Data();
				data.setContentId(Tools.string2MD5(Tools.url(href).getPath()));
				if (isDataExists(data.getContentId())) {
					continue;
				}

				String html = Tools.getRequest1(href);

				Elements head = Tools.getBody("head", html);
				Elements scripts = head.select("script");
				for (int j = 0; j < scripts.size(); j++) {
					//
					Element elm = scripts.get(i);
					if (elm.data().indexOf("location_from_ip") != -1) {

					}
				}

				String tempFilePath = Tools.getLineFile1(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}

				String photo4xHtml = "<img src=\"" + photo4x.replace("{photo_id}", photo_id) + "\"/>";
				Elements imgElm = Tools.getBody("img", photo4xHtml);
				this.downImg(imgElm, tempFileDir, targetFileDir);
				Tools.clearsAttr(imgElm);
				String content = "<div>" + imgElm.toString() + "</div><br/><div>" + desc + "</div>";
				content += "<div>作者 : " + user_name + "</div>";
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (JSONException e) {
				e.printStackTrace();
			}

		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				}
			}
		}
	}
}
