package com.modern.datacollect.impl.onebatch;

import java.io.File;
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

public class PocoCookbook_diyCollector extends Collector {

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
			config.setSiteUrl("http://cook.poco.cn/cookbook_diy.php");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'http://cook.poco.cn/cookbook_diy.htx&p={page}&good=0&o=&show_type=img&b_id=0#main_list'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
		}

		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));
		String url = null;
		String dataUrl = null;
		Integer page = 0;
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
				url = dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + (page) + ",'dataUrl':'http://cook.poco.cn/cookbook_diy.htx&p={page}&good=0&o=&show_type=img&b_id=0#main_list'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				Elements body = Tools.getBody("div[class=\"ul768_pic165 clear mt10\"]", html);
				body = body.select("li");
				Elements pages = Tools.getBody(".page", html);
				if (!pages.toString().contains("下一页")) {
					stop();
					break;
				}
				this.dealwith(body, tempFileDir, targetFileDir);
			} catch (Exception e) {
				e.printStackTrace();
			}
			page++;
		}
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".show_author").select("a").text();
				String href = elm.select(".show_author").select("a").attr("href");
				href = "http://cook.poco.cn/" + href;
				String imgSrc = elm.select(".imgbox").select("img").attr("src");
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
				Elements ebody = Tools.getBody("#plus_content", html);
				Elements author_info = Tools.getBody(".author_info", html);
				String type = author_info.select("tr").eq(0).select("td").last().text();
				Elements keyword = Tools.getBody(".keyword", html);
				keyword = keyword.select("a");
				StringBuffer keywords = new StringBuffer();
				for (int i = 0; i < keyword.size(); i++) {
					if (i > 0) {
						keywords.append(",");
					}
					keywords.append(keyword.get(i).text());
				}
				if (type.indexOf("类型") == -1) {
					type = "";
				}
				String author = "<div>作者 : " + author_info.select("strong").text() + "&nbsp;" + type + "</div>";

				this.downImg(ebody, tempFileDir, targetFileDir);

				Tools.clearsAttr(ebody);
				Tools.clearsAttr(keyword);

				String content = author + ebody.toString() + "<br/><div>原文链接 : <a href=\"" + href + "\">" + href + "</a></div>";
				// String iso = new String(content.getBytes("UTF-8"),
				// "ISO-8859-1");
				// String utf8 = new String(iso.getBytes("ISO-8859-1"),
				// "UTF-8");
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void downImg(Elements ebody, String tempFileDir, String targetFileDir) {
		Elements cimg = ebody.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				}
			}
		}
	}
}
