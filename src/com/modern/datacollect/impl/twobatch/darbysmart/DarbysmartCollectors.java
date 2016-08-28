package com.modern.datacollect.impl.twobatch.darbysmart;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class DarbysmartCollectors extends Collector {

	public void dealwith(JSONArray array, String tempFileDir, String targetFileDir) {
		for (int i = 0; i < array.length(); i++) {
			try {
				JSONObject json = array.getJSONObject(i);
				String title = json.getString("title"), href = "http://www.darbysmart.com/ideas/" + json.getString("slug"), imgSrc = "";
				JSONArray discoverable_photos = json.getJSONArray("discoverable_photos");

				Data data = new Data();
				data.setContentId(Tools.string2MD5(Tools.url(href).getPath()));
				if (isDataExists(data.getContentId())) {
					continue;
				}
				String tempFilePath = Tools.getLineFile1(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest(href);
				String content = "";

				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("data-original");
			if ("".equals(cimgSrc)) {
				cimgSrc = cimgemt.attr("src");
			}
			if (cimgSrc.indexOf("http") == -1) {
				cimgSrc = "http://www.fubiz.net" + cimgSrc;
			}
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				} else {
					cimgemt.remove();
				}
			} else {
				cimgemt.remove();
			}
		}
	}
}
