package com.modern.datacollect.impl;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Config;
import com.modern.datacollect.api.Data;

public class ShijueRatingCollector extends Collector {

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
			config.setSiteUrl("http://www.shijue.me/community/rating");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'http://www.shijue.me/community/search?type=json&page={page}&size=20&license=-1&orderby=rating'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
		}

		// 目录不存在，创建目录
		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));

		Elements pageNumbers;// 存放page翻页节点
		Elements catmainBody;// 存放要抓取的数据节点
		String html;// 存放html
		String url = null;// 存放准备抓取的url

		Integer page = 1;
		String dataUrl = "";
		try {
			// 初始执行时，获取后台配置参数，从第几页开始
			// 或配置了更详细的参数也可以利用到
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
			dataUrl = obj.getString("dataUrl");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 处理当前打开页面
		for (;;) {
			try {
				url = dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'http://www.shijue.me/community/search?type=json&page={page}&size=20&license=-1&orderby=rating'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest(url);
				JSONObject jsonArray = new JSONObject(html);
				JSONArray dataArray = jsonArray.getJSONArray("dataArray");
				if (dataArray.length() > 0) {
					this.dealwith(dataArray, tempFileDir, targetFileDir);
				} else {
					stop();
					break;
				}
			} catch (Exception e) {
			}
			page++;
		}
	}

	public void dealwith(JSONArray dataArray, String tempFileDir, String targetFileDir) {
		for (int i = 0; i < dataArray.length(); i++) {
			try {
				JSONObject jsonData = dataArray.getJSONObject(i);
				String title = jsonData.getString("title");
				String imgSrc = jsonData.getString("url");
				String href = "http://www.shijue.me/community/photo-details/" + jsonData.getString("id");
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
				String html = Tools.getRequest(href);
				Elements ebody = Tools.getBody("#links", html);
				ebody = Tools.getBody("script", html);
				html = null;
				for (Element element : ebody) {
					String[] dataa = element.data().toString().split("var");
					for (String variable : dataa) {
						variable = variable.trim();
						if (variable.contains("=") && variable.contains("VCG.result =")) {
							for (String s : variable.split("VCG.result")) {
								html = s.substring(s.indexOf("{"), s.lastIndexOf("}") + 1);
							}
						}
					}
				}
				if (html != null) {
					JSONObject edata = new JSONObject(html);
					StringBuffer content = new StringBuffer();
					String keywords = "";
					if (edata.length() > 0) {
						keywords = edata.getString("tags");
						String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(edata.getLong("createdTime")));
						JSONArray earray = edata.getJSONArray("setSets");
						for (int j = 0; j < earray.length(); j++) {
							JSONObject ejsonData = earray.getJSONObject(j);
							try {
								if (ejsonData.getString("url") != "") {
									String eimgSrc = "http://pic.shijue.me/picurl/" + ejsonData.getString("url");
									String ctempFilePath = Tools.getLineFile(eimgSrc, tempFileDir);
									File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
									String mydest = getMySiteImgSrc(cdest);
									content.append("<div><img src='" + mydest + "'/></div>");
								}
							} catch (Exception e) {
							}
							try {
								String description = ejsonData.getString("description");
								content.append("<div>" + description + "</div>");
							} catch (Exception e) {
							}
						}
						content.append("<div>上传时间：" + time + "</div>");
					}
					data.setTitle(title);
					data.setContent(content.toString());
					data.setKeywords(keywords);
					whenOneData(data);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}
		}
	}
}
