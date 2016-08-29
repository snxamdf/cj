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

public class YokaMenAutoCollector extends Collector {
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
			config.setSiteUrl("http://www.yokamen.cn/auto/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':2,'dataUrl':'http://brandservice.yoka.com/v1/?_c=cmsbrandindex&_a=getCmsForZhu&_moduleId=25&_ln=gbk&channelId=88&k=11211life&p={page}'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		String dataUrl = null;
		Integer page = 1;
		try {
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
			dataUrl = obj.getString("dataUrl");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		String html = null;

		for (;;) {
			try {
				if (page == 1) {
					url = config.getSiteUrl();
				} else {
					url = dataUrl.replace("{page}", page.toString());
				}
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'/list_{page}.shtml'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url, "GB2312");
				if (html == null) {
					stop();
					break;
				}
				Elements body = null;
				if (page == 1) {
					body = Tools.getBody("div[class=\"listBox\"]", html);
				} else {
					JSONObject json = new JSONObject(html);
					html = json.getString("context");
					body = Tools.getBody("div[class=\"listBox\"]", html);
				}
				if (body.size() == 0) {
					stop();
					break;
				}
				this.dealwith(body, tempFileDir, targetFileDir, config);
			} catch (Exception e) {
				e.printStackTrace();
			}
			page++;
			if (page > 80) {
				stop();
				break;
			}
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir, Config config) {
		for (Element emt : body) {
			try {
				Elements imgElm = emt.select(".img");
				String href = imgElm.select("a").attr("href");
				String imgSrc = imgElm.select("img").attr("src");
				if (imgSrc.lastIndexOf("http") > 0) {
					imgSrc = imgSrc.substring(imgSrc.lastIndexOf("http"), imgSrc.length());
				}
				Elements txt = emt.select(".txt");
				String title = txt.select(".tit").text();
				String tag = txt.select(".tag").text();
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
				List<File> picList = new ArrayList<File>();
				if (!"".equals(tempFilePath)) {
					File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
					if (dest != null) {
						picList.add(dest);
					}
				}
				String html = Tools.getRequest1(href, "GB2312");
				Elements main = Tools.getBody("div[class=\"g-main fleft\"]", html);
				String content = "";
				StringBuffer keywords = new StringBuffer();
				if (main.size() > 0) {
					Elements infoTime = main.select(".infoTime");
					infoTime.select(".name").select("dt").remove();
					Elements quote = main.select(".double_quotes");
					Tools.clearsAttr(quote);
					main = main.select(".textCon");
					this.downImg(main, tempFileDir, targetFileDir);
					Tools.clearsAttr(main);
					content = infoTime.select(".name").text() + "<br/>" + quote.toString() + main.toString();
					keywords.append(tag);
				} else {
					Elements keyword = Tools.getBody(".tags", html);
					keyword = keyword.select(".tags-l").select("a");
					for (int i = 0; i < keyword.size(); i++) {
						if (i > 0) {
							keywords.append(",");
						}
						keywords.append(keyword.get(i).text());
					}
					Tools.clearsAttr(keyword);
					System.out.println();
					Elements list = Tools.getBody("#list", html);
					list = list.select("a");
					Elements picCon = Tools.getBody("div[class=\"g-content clearfix articlePic\"]", html);
					Tools.clearsNodes(picCon);
					if (list.size() > 0) {
						content = this.ebody(list, tempFileDir, targetFileDir);
						content += picCon.toString();
						if (keyword.size() > 0) {
							content += "<br/>" + keyword.toString() + "<br/>";
						}
					}
				}
				if ("".equals(content)) {
					continue;
				}
				content += "<br/><div>原文链接 : <a href=\"" + href + "\">" + href + "</a></div>";
				data.setTitle(title);// title
				data.setContent(content);// 获取内容
				data.setPicList(picList);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public String ebody(Elements body, String tempFileDir, String targetFileDir) {
		StringBuffer result = new StringBuffer();
		for (int i = 1; i < body.size(); i++) {
			String href = body.get(i).attr("href");
			String html = Tools.getRequest1(href, "GB2312");
			Elements img = Tools.getBody("#picCon", html);
			img = img.select("img");
			this.downImg(img, tempFileDir, targetFileDir);
			Tools.clearsAttr(img);
			result.append(img.toString());
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
