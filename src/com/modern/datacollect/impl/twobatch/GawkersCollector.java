package com.modern.datacollect.impl.twobatch;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public abstract class GawkersCollector extends Collector {
	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String description = elm.select(".description").text();
				Elements postLinks = elm.select(".post-links");
				String submitterHref = postLinks.select(".submitter").attr("href");
				String submitter = postLinks.select(".submitter").text();
				String postidHref = postLinks.select(".postid").attr("href");
				Elements picture = elm.select(".picture");
				String imgSrc = "https:" + elm.select(".picture-link").select("img").attr("src");
				String href = elm.select(".picture-link").attr("href");
				String title = elm.select(".picture-link").attr("title");

				StringBuffer keywords = new StringBuffer();

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
				// String html = Tools.getRequest(href);
				// String meta = "";
				//
				// Elements header =
				// Tools.getBody("header[class=\"entry-header\"]", html);
				// if (header.size() > 0) {
				// String entryTitle = header.select(".entry-title").text();
				// Elements entryMeta = header.select(".entry-meta");
				// Tools.clearsAttr(entryMeta);
				// meta = entryMeta.toString();
				// }
				// Elements ebody = Tools.getBody(".entry-content", html);
				// if (ebody.size() == 0) {
				// ebody = Tools.getBody(".theme-content", html);
				// if (ebody.size() > 0) {
				// Elements key = ebody.select(".single-post-tags").select("a");
				// for (int i = 0; i < key.size(); i++) {
				// if (i > 0) {
				// keywords.append(",");
				// }
				// keywords.append(key.get(i).text());
				// }
				// }
				// }
				// ebody.select(".blog-similar-posts").remove();
				// ebody.select("#wp_rp_first").remove();
				// ebody.select("#fsb-social-bar").remove();
				// ebody.select("#happenstance-post-nav").remove();
				// ebody.select(".single-post-tags").remove();
				// ebody.select(".wp-post-navigation").remove();
				// ebody.select(".essb_links").remove();
				// ebody.select(".mk-about-author-wrapper").remove();
				// ebody.select("#comments").remove();
				// ebody.select(".clearboth").remove();
				// ebody.select(".sharedaddy").remove();
				// ebody.select(".jp-relatedposts").remove();
				// ebody.select(".pin-it-btn-wrapper").remove();
				// ebody.select(".entry-footer").remove();
				// ebody.select(".y-printed-permalink").remove();
				// ebody.select(".yrecipe-beacon").remove();

				// this.downImg(ebody, tempFileDir, targetFileDir);
				// Tools.clearsAttr(ebody);
				if (href.lastIndexOf("http:") > 0) {
					href = href.substring(href.lastIndexOf("http:"), href.length());
				} else if (href.lastIndexOf("https:") > 0) {
					href = href.substring(href.lastIndexOf("https:"), href.length());
				}

				String author = "<br/><div>作者 : " + submitter + "</div>";
				String sourceUrl = author + "<br/><div>阅读文章 : <a href=\"" + href + "\">" + href + "</a></div>";
				String content = "<div>" + description + "</div>" + sourceUrl;
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
