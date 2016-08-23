package com.modern.datacollect.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;

public abstract class CosmopolitansCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".bt").select("a").attr("title");
				String href = elm.select(".bt").select("a").attr("href");
				String imgSrc = elm.select("img").attr("src");
				String time = elm.select(".time").text();
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
				href = href.split(".shtml")[0] + "all.shtml";
				String html = Tools.getRequest1(href, "gbk");
				Elements ebody = Tools.getBody(".c_left", html);
				Elements body1 = ebody.select(".detail_info");
				Elements body2 = ebody.select(".detail_daodu");
				Elements body3 = ebody.select(".detail_c");
				this.downImg(body1, tempFileDir, targetFileDir);
				this.downImg(body2, tempFileDir, targetFileDir);
				this.downImg(body3, tempFileDir, targetFileDir);
				Tools.clearsAttr(body2);
				Tools.clearsAttr(body3);
				Tools.clearsAttr(body1);
				String content = body2.toString() + body3.toString() + body1.toString();
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		ebody.select("a").attr("href", "javascript:void(0)");
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null)
					cimgemt.attr("src", mydest);
			}
		}
	}
}
