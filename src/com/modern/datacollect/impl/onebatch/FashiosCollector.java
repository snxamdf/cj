package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class FashiosCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			try {
				String title = emt.select(".entry-title").select("a").text();
				String href = emt.select(".entry-title").select("a").attr("href");
				String imgSrc = emt.select(".entry-image-link").select("img").attr("src");
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
				Elements eimgBody = Tools.getBody(".entry-content", html);

				Elements footer = Tools.getBody(".entry-footer", html);
				Elements entryMeta = footer.select(".entry-meta");
				Tools.clearsAttr(entryMeta);

				eimgBody.select(".fashi-in-post-wide-ad").remove();
				eimgBody.select(".sharedaddy").remove();
				eimgBody.select("#jp-relatedposts").remove();
				eimgBody.select(".jp-relatedposts").remove();
				eimgBody.select(".footer-widgets").remove();

				Elements cimg = eimgBody.select("img");
				for (Element cimgemt : cimg) {
					String cimgSrc = cimgemt.attr("src");
					if (cimgSrc.indexOf("http") == -1) {
						cimgSrc = "http:" + cimgSrc;
					}
					if (!"".equals(cimgSrc)) {
						String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
						File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
						String mydest = getMySiteImgSrc(cdest);
						if (mydest != null)
							cimgemt.attr("src", mydest);
						else
							cimgemt.remove();
					}
				}

				Tools.clearsAttr(eimgBody);
				// 获取内容
				String content = eimgBody.toString() + entryMeta;
				data.setTitle(title);
				data.setContent(content);
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
