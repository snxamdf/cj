package com.modern.datacollect.impl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;
import org.apache.http.util.EntityUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Collector;
import com.modern.datacollect.api.Data;

public abstract class BehanceSearchsCollector extends Collector {

	public String getRequest(String url) {
		CloseableHttpClient client = HttpClients.createDefault();
		HttpGet httpPost = new HttpGet(url);

		try {
			httpPost.addHeader(new BasicHeader(
					"Cookie",
					"bgk=78181456; bcp=739779; AMCVS_9E1005A551ED61CA0A490D45%40AdobeOrg=1; AMCV_9E1005A551ED61CA0A490D45%40AdobeOrg=-227196251%7CMCIDTS%7C17031%7CMCMID%7C20220525614731512692748062213865416657%7CMCAID%7CNONE%7CMCOPTOUT-1471447161s%7CNONE%7CMCAAMLH-1472044761%7C11%7CMCAAMB-1472044761%7Chmk_Lq6TPIBMW925SPhw3Q; __utma=6448045.1778075610.1471439960.1471439960.1471439960.1; __utmb=6448045.43.10.1471439960; __utmc=6448045; __utmz=6448045.1471439960.1.1.utmcsr=(direct)|utmccn=(direct)|utmcmd=(none); ilo0=true; mbox=session#1471439958979-878233#1471444857|PC#1471439958979-878233.24_10#1474466997; s_sess=%20s_demandbase_v2.2%3Ddone%3B%20s_dmdbase%3D%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253AWireless%253AMobile%2520Network%3B%20s_dmdbase_custom%3D%255Bn%252Fa%255D%253A22%253A%255Bn%252Fa%255D%253ACN%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%253A%255Bn%252Fa%255D%3B%20s_cpc%3D0%3B%20s_ppv%3D%255B%2522www.behance.net%252Fsearch%2522%252C55%252C0%252C954%252C1920%252C954%252C1920%252C1080%252C1%252C%2522P%2522%255D%3B%20s_cc%3Dtrue%3B; aam_uuid=20376490774848362672768584689122497375; s_pers=%20gpv%3Dbehance.net%253Asearch%7C1471444796192%3B%20s_nr%3D1471442996195-New%7C1502978996195%3B%20s_vs%3D1%7C1471444816413%3B"));
			httpPost.addHeader("Host", "www.behance.net");
			httpPost.addHeader("If-Modified-Since", "Wed, 17 Aug 2016 13:58:15 +0000");
			httpPost.addHeader("If-None-Match", "W/\"aa7612f3137d17ac21c3327316fb2e9f\"");
			httpPost.addHeader("Upgrade-Insecure-Requests", "1");
			httpPost.addHeader("X-IMS-ClientId", "BehanceWebSusi1");
			httpPost.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/51.0.2704.106 Safari/537.36");
			httpPost.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
			httpPost.addHeader("Accept-Encoding", "gzip, deflate, sdch, br");
			httpPost.addHeader("Accept-Language", "zh-CN,zh;q=0.8");
			httpPost.addHeader("Cache-Control", "no-cache");
			httpPost.addHeader("Connection", "keep-alive");
			httpPost.addHeader("Pragma", "no-cache");

			HttpResponse httpResponse = client.execute(httpPost);
			HttpEntity entity = httpResponse.getEntity();
			int code = httpResponse.getStatusLine().getStatusCode();
			if (code == 200) {
				if (entity != null) {
					String html = EntityUtils.toString(entity);
					return html;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				String title = elm.select(".cover-name").select("a").text();
				String href = elm.select(".cover-name").select("a").attr("href");
				String imgSrc = elm.select(".cover-img").select("img").attr("srcset");
				String[] iss = imgSrc.split(",");
				if (iss.length > 1) {
					imgSrc = iss[1].trim().split(" ")[0];
				} else {
					imgSrc = iss[0];
				}
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
				String html = getRequest(href);
				Elements ebody = Tools.getBody("#project-modules", html);
				ebody.select("#project-spacer").remove();

				Elements sidebarGroup = Tools.getBody("div[class=\"sidebar-group\"]", html);
				Elements listName = sidebarGroup.select(".profile-list").select(".profile-list-name");
				String listNames = "";
				for (int lni = 0; lni < listName.size(); lni++) {
					if (lni > 0) {
						listNames += ",&nbsp;";
					}
					listNames += listName.get(lni).text();
				}
				if (!"".equals(listNames)) {
					listNames = "<div>项目作者 : " + listNames + "</div><br/>";
				}
				ebody.select("#project-spacer").remove();
				ebody.select("iframe").remove();
				ebody.select("source").remove();

				Elements abouts = sidebarGroup.select("#project-block-sidebar-about");
				abouts.select(".project-published").remove();
				abouts.select(".variable-text-short").remove();
				abouts.select(".js-more-toggle-link").remove();
				abouts = abouts.select(".variable-text");
				abouts.before("<br/>");

				this.downImg(ebody, tempFileDir, targetFileDir);

				String content = listNames + ebody.toString() + abouts.toString();
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
			if ("".equals(cimgSrc)) {
				cimgSrc = cimgemt.attr("srcset");
				String[] iss = cimgSrc.split(",");
				if (iss.length > 1) {
					cimgSrc = iss[1].trim().split(" ")[0];
				} else {
					cimgSrc = iss[0];
				}
			}
			if (!"".equals(cimgSrc) && cimgSrc.indexOf("http") != -1) {
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
					cimgemt.removeAttr("media");
				}

			}
		}
	}
}
