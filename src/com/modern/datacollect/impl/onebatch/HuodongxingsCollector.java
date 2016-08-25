package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class HuodongxingsCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select("h3").select("a").text();
				String href = elm.select("h3").select("a").attr("href");
				href = "http://www.huodongxing.com" + href;
				String imgSrc = elm.select("img").attr("src");
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
				Elements ebody = Tools.getBody("#container-lg", html);
				String time = ebody.select(".jumbotron").select(".media-body").select(".icon-time").get(0).parent().text();
				String address = ebody.select(".address").text();
				if (ebody.select(".address").select("a").size() > 0) {
					String baiduAddress = ebody.select(".address").select("a").attr("href");
					String baidu = Tools.getRequest1(baiduAddress);
					Elements scripts = Tools.getBody("script ", baidu);
					for (Element elmt : scripts) {
						if (elmt.toString().indexOf("function initializeMap") != -1) {
							String[] pppp = elmt.toString().split("position");
							if (pppp.length > 0) {
								pppp = pppp[1].split("\"");
								if (pppp.length > 0) {
									pppp = pppp[1].split(",");
									if (pppp.length > 0) {
										data.setLongitude(pppp[0].trim());
										data.setLatitude(pppp[1].trim());
										data.setIsBaidu(true);
										break;
									}
								}
							}
						}
					}
				}
				Elements objArrays = ebody.select(".tags").select("a");
				StringBuffer keywords = new StringBuffer();
				for (int i = 0; i < objArrays.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(objArrays.get(i).text());
				}
				Elements econtent = ebody.select("#event_desc_page");

				this.downImg(econtent, tempFileDir, targetFileDir);
				Tools.clearsAttr(econtent);
				String content = econtent.toString();
				content += "<br/><div>报名连接：<a href=\"" + href + "\">" + href + "</a></div>";
				data.setTitle(title);
				data.setContent(content);
				data.setAddress(address);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
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
