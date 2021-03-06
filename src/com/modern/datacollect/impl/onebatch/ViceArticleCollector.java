package com.modern.datacollect.impl.onebatch;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Tools;

public class ViceArticleCollector extends Collector {

	@Override
	public void begin() {
		// 默认注入的配置
		Config config = getConfig();
		// 图片保存目录
		String targetFileDir = getSaveFileDir();
		// 图片**临时**保存目录
		String tempFileDir = getTempFileDir();
		if (config == null) {// 开发时用到的，自己配置
			config = new Config();
			// 配置网站url 这个url是一个主要的，如果在抓取的时候变动需要自己拼接
			config.setSiteUrl("http://www.vice.cn/vice/viceArticle/index/category/read/Article_page/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		Integer page = 1;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String html = null;

		for (;;) {
			try {
				url = config.getSiteUrl() + page.toString();
				config.setSiteConfig("{'page':" + (page) + "}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url, "UTF-8");
				if (html == null) {
					stop();
					break;
				}
				Elements body = Tools.getBody(".story_list", html);
				body = body.select("article");
				this.dealwith(body, tempFileDir, targetFileDir);
				if (page >= 685) {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".entry-title").select("a").text();
				String href = elm.select(".entry-title").select("a").attr("href");
				href = "http://www.vice.cn" + href;
				String imgSrc = elm.select(".entry-image").select("img").attr("src");
				// 数据保存对像
				Data data = new Data();
				URL url;
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
				// 通过工具类 将图片保存到正式目录
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					// 将文件对像保存到picList
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String html = Tools.getRequest(href, "UTF-8");
				Elements ebody = Tools.getBody(".article_content", html);
				Elements author = Tools.getBody(".article_meta", html);
				Elements keyword = Tools.getBody("#tags-box", html);
				keyword = keyword.select("a");
				StringBuffer keywords = new StringBuffer();
				for (int i = 0; i < keyword.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(keyword.get(i).text());
				}
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
				Tools.clearsAttr(keyword);
				Tools.clearsAttr(ebody);
				Tools.clearsAttr(author);
				// 获取内容
				String content = ebody.toString();
				content += author.toString() + "<br/>";
				content += keyword.toString() + "<br/><br/><div>原文链接 : <a href=\"" + href + "\">" + href + "</a></div><br/>";
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
