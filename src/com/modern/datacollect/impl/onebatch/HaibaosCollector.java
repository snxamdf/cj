package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class HaibaosCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			try {
				String title = emt.select(".tit_focus_item").text();
				String href = emt.select("div.hb_fl").select("a").attr("href");
				String imgSrc = emt.select("a").eq(0).select("img").attr("data-lazy-src");
				Data data = new Data();
				URL url = null;
				try {
					url = new URL(href);
					data.setContentId(Tools.string2MD5(url.getPath()));
				} catch (MalformedURLException e) {
					e.printStackTrace();
				}

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
				Elements ebody = Tools.getBody("div[class=\"wr content\"]", html);

				Elements ebody1 = ebody.select("div[class=\"hb_fl contentleft\"]");
				Elements btcenter = Tools.getBody("#btcenter", html);
				String author = btcenter.select("span").eq(2).text();
				if (author.indexOf("编辑") == -1) {
					author = "";
				}
				Elements keyword = btcenter.select(".uldiv").select("a");
				StringBuffer keywords = new StringBuffer();
				StringBuffer tags = new StringBuffer();
				for (int i = 0; i < keyword.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(keyword.get(i).text());
					tags.append("<span>").append(keyword.get(i).text()).append("</span>&nbsp;");
				}
				Elements ebody2 = ebody1.select("#jsArticleDesc");
				this.downImg(ebody2, tempFileDir, targetFileDir);
				Tools.clearsAttr(ebody2);
				String content = ebody2.toString();

				Elements pages = ebody.select(".pages");
				String result = null;
				if (pages.size() > 0) {
					pages.select(".next").remove();
					String pageTemp = href.split("\\.htm")[0] + "_{page}.htm";
					String num = pages.select("a").last().text();
					result = this.contentPage(Integer.valueOf(num), pageTemp, tempFileDir, targetFileDir);
				}
				if (result != null) {
					content += result;
				}
				content += "<div>来源 : 海报时尚网</div>";
				content += "<br/><div>标签 : " + tags.toString() + "</div>";
				content += "<br/><div>原谅链接 : <a href=\"" + href + "\">" + href + "</a>&nbsp;" + author + "</div>";
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private String contentPage(Integer num, String pageTemp, String tempFileDir, String targetFileDir) {
		StringBuffer result = new StringBuffer();
		for (int i = 2; i < num; i++) {
			String url = pageTemp.replace("{page}", i + "");
			String html = Tools.getRequest1(url);
			Elements ebody = Tools.getBody("div[class=\"wr content\"]", html);
			Elements body2 = ebody.select("#jsArticleDesc");
			this.downImg(body2, tempFileDir, targetFileDir);
			Elements body1 = ebody.select(".desc_content");
			Tools.clearsAttr(body1);
			result.append(body1.toString());
			Tools.clearsAttr(body2);
			result.append(body2.toString());
		}
		return result.toString();
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
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
