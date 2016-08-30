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
				String[] llat = { "", "" };
				for (int j = 0; j < scripts.size(); j++) {
					Element elm = scripts.get(j);
					if (elm.data().indexOf("location_from_ip") != -1) {
						String[] locals = elm.data().split("function");
						for (String str : locals) {
							if (str.indexOf("location_from_ip") != -1) {
								str = str.substring(str.indexOf("[") + 1, str.indexOf("]"));
								llat[0] = str.split(",")[0].trim();
								llat[1] = str.split(",")[1].trim();
								break;
							}
						}
						break;
					}
				}

				String tempFilePath = Tools.getLineFile1(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				Elements bcolwrap = Tools.getBody(".bcol-wrap", html);
				bcolwrap = bcolwrap.select(".body");
				Tools.clearsAttr(bcolwrap);
				desc = bcolwrap.toString();
				String photo4xHtml = "<img src=\"" + photo4x.replace("{photo_id}", photo_id) + "\"/>";
				Elements imgElm = Tools.getBody("img", photo4xHtml);
				Elements stats = Tools.getBody(".discovery-popup-2-stats", html);
				stats = stats.select(".listed").select("a");
				StringBuffer keywords = new StringBuffer();
				for (int j = 0; j < stats.size(); j++) {
					if (j > 0) {
						keywords.append(",");
					}
					keywords.append(stats.get(i).text());
				}
				this.downImg(imgElm, tempFileDir, targetFileDir);
				Tools.clearsAttr(imgElm);
				Tools.clearsAttr(stats);

				String content = "<div>" + imgElm.toString() + "</div><br/><div>" + desc + "</div>";
				content += "<br/><div>Photos by : " + user_name + "</div>";
				content += "<br/><div>Tags : " + stats.toString() + "</div>";
				//content += "<br/><div>原文链接 : <a href=\"" + href + "\">" + href + "</a></div>";
				data.setLongitude(llat[0]);
				data.setLatitude(llat[1]);
				data.setIsBaidu(false);
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
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
