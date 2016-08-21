package com.modern.datacollect.impl;

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

public class HdbGuangzhouCollector extends Collector {

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
			config.setSiteUrl("http://www.hdb.com/find/");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'dataUrl':'guangzhou-sjbx-p{page}'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\tempFileDir\\";
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
				url = config.getSiteUrl() + dataUrl.replace("{page}", page.toString());
				config.setSiteConfig("{'page':" + page + ",'dataUrl':'guangzhou-sjbx-p{page}'}");
				updateSiteConfig(config.getSiteConfig());
				html = Tools.getRequest1(url);
				Elements body = Tools.getBody(".find_main_ul", html);
				body = body.select(".find_main_li");
				this.dealwith(body, tempFileDir, targetFileDir);
				if (body.size() == 0) {
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

				String content = econtent.toString();
				content += "<br/><div>报名连接：<a href=\"" + href + "\">" + href + "</a></div>";
				data.setTitle(title);
				data.setContent(content);
				data.setAddress(address);
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
