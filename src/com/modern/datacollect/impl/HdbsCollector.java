package com.modern.datacollect.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;

public abstract class HdbsCollector extends Collector {

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".find_main_title").select("a").text();
				String href = elm.select(".find_main_title").select("a").attr("href");
				href = "http://www.hdb.com" + href + "?hdb_pos=find";
				String imgSrc = elm.select(".hd_pic").attr("data-src");
				Data data = new Data();
				data.setContentId(Tools.string2MD5(Tools.url(href).getPath()));
				if (isDataExists(data.getContentId())) {
					continue;
				}
				String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest1(href);
				Elements ebody = Tools.getBody(".detail_time_attr_det_con", html);
				String time = ebody.select(".detail_Time_t").text();
				if (ebody.select(".detail_Attr").select("a").size() > 0) {
					String lldedu = ebody.select(".detail_Attr").select("a").attr("href");
					String[] dedus = lldedu.split("&");
					String de = dedus[0].split("=")[1];
					String du = dedus[1].split("=")[1];
					data.setLongitude(de);
					data.setLatitude(du);
					data.setIsBaidu(false);
				}
				String address = ebody.select(".detail_Attr_K").text();
				Elements econtent = ebody.select("#dt_content");

				this.downImg(econtent, tempFileDir, targetFileDir);
				Tools.clearsAttr(econtent);
				String content = econtent.toString();
				content += "<br/><div>报名连接：<a href=\"" + href + "\">" + href + "</a></div>";
				data.setTitle(title);
				data.setContent(content);
				data.setAddress(address);
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		ebody.select("a").attr("href", "javascript:void(0)");
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("data-src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
					cimgemt.removeAttr("data-src");
				}

			}
		}
	}
}
