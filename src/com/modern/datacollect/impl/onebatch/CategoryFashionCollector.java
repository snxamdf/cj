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

public class CategoryFashionCollector extends Collector {

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
			config.setSiteUrl("http://www.fashionbeans.com/category/mens-fashion");
			// 更新配置每次抓取一页数据,可用用于配置，当前抓取第几页，第几条数据。
			config.setSiteConfig("{'page':1,'url':'?seeall=true'}");
			// 文件的保存正式目录
			targetFileDir = "D:\\sitepage\\targetFileDir\\";
			// 文件的保存临时目录
			tempFileDir = "D:\\sitepage\\tempFileDir\\";
			writeIndex("index.html", "</br><a href=\"" + config.getSiteUrl() + "\" target='_blank'>" + config.getSiteUrl() + "</a><br/><br/>");
		}

		// 目录不存在，创建目录
		Tools.mkDir(new File(targetFileDir));
		Tools.mkDir(new File(tempFileDir));

		Elements pageNumbers;// 存放page翻页节点
		Elements catmainBody;// 存放要抓取的数据节点
		String html;// 存放html
		String url = null;// 存放准备抓取的url

		int page = 1;
		try {
			// 初始执行时，获取后台配置参数，从第几页开始
			// 或配置了更详细的参数也可以利用到
			JSONObject obj = new JSONObject(config.getSiteConfig());
			page = obj.getInt("page");
		} catch (JSONException e1) {
			e1.printStackTrace();
		}
		// 处理当前打开页面
		for (;;) {
			String num = "-1";
			try {
				if (page == 1) {// 判断第一页的url
					url = config.getSiteUrl() + "/?seeall=true";
				} else {// 判断除了第一页的url
					url = config.getSiteUrl() + "/page/" + (page) + "/?seeall=true";
				}
				// 更新配置参数 begin
				config.setSiteConfig("{'page':" + (page) + ",'param':'?seeall=true'}");
				updateSiteConfig(config.getSiteConfig());
				// 更新配置参数 end

				// 通过工具类 获得当前要获取的url响应的html代码
				html = Tools.getRequest1(url);
				// 通过工具类 获得分页元素
				pageNumbers = Tools.getBody(".pageNumbers", html);
				// 总页数
				num = pageNumbers.select("li").last().text();
				// 通过工具类 获得要获取的数据
				catmainBody = Tools.getBody("#catmainBody", html);
				// 处理数据
				this.dealwith(catmainBody, tempFileDir, targetFileDir);// 处理当前打开页面
			} catch (Exception e) {
				e.printStackTrace();
			}
			// 如果当前页大于等于总页数调用 stop方法 并跳出循环
			// 如果未获取到总页数，说明页面不正确 调用stop方法 并跳出循环
			if (page >= Integer.parseInt(num) || "-1".equals(num)) {
				stop();
				break;
			}
			page++;// 自动+1页,也就是将要获取下一页的数字
		}
	}

	/**
	 * 处理获取数据的方法
	 * 
	 * @param catmainBody
	 *            html元素
	 * @param tempFileDir
	 *            临时文件目录
	 * @param targetFileDir
	 *            正式文件目录
	 */
	public void dealwith(Elements catmainBody, String tempFileDir, String targetFileDir) {
		Elements catArticles = catmainBody.select(".catArticles");
		for (int i = 0; i < catArticles.size(); i++) {
			Element el = catArticles.get(i);
			try {
				Tools.sleep();
				Data data = new Data();
				Elements atitle = el.select("h2").select("a");
				String href = atitle.attr("href");
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
				String imgSrc = el.select("img").attr("src");
				String tempFilePath = Tools.getLineFile(imgSrc, tempFileDir);
				File dest = Tools.copyFileChannel(tempFilePath, targetFileDir);
				if (dest != null) {
					List<File> picList = new ArrayList<File>();
					picList.add(dest);
					data.setPicList(picList);
				}
				String title = atitle.text();
				String html = Tools.getRequest1(href);
				Elements articleImageLrg = Tools.getBody(".articleImageLrg", html);
				Elements crimg = articleImageLrg.select("img");
				Elements credit = articleImageLrg.select(".credit");
				downImg(crimg, tempFileDir, targetFileDir);
				Tools.clearsAttr(crimg);
				String crauthor = crimg.toString() + "<br/>" + credit.toString() + "<br/>";

				Elements metaNewArticles = Tools.getBody(".metaNewArticles", html);
				Elements wrapper = Tools.getBody("div[class=\"articleBody articleBodyNew\"]", html);
				wrapper.select(".sidebar").remove();
				wrapper.select("h1[class=\"articles articlesNew\"]").remove();
				wrapper.select("#singleOutline").select(".metaNewArticles").remove();
				wrapper.select("#singleOutline").select(".social-container-small").remove();
				wrapper.select("#singleOutline").select(".social-sharing-footer").remove();
				wrapper.select("#singleOutline").select("#ob_holder").remove();
				wrapper.select("#singleOutline").select(".OUTBRAIN").remove();
				wrapper.select("#singleOutline").select(".articleComments").remove();
				wrapper.select(".wrapper").remove();
				wrapper.select(".blacktop").remove();
				wrapper.select(".fbtvNavigation").remove();
				wrapper.select("#ob_holder").remove();
				wrapper.select("a").attr("href", "javascript:void(0)");
				wrapper.select(".articleBreadcrumb").before("<br/>");
				wrapper.select(".sponsoredPost").remove();
				downImg(wrapper, tempFileDir, targetFileDir);
				Tools.clearsAttr(metaNewArticles);
				Tools.clearsAttr(wrapper);
				// 获取内容
				String content = crauthor + wrapper.toString() + metaNewArticles.toString();
				data.setTitle(title);// title
				data.setContent(content);// 获取内容
				// 最终调用 whenOneData
				whenOneData(data);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void downImg(Elements elmt, String tempFileDir, String targetFileDir) {
		Elements cimg = elmt.select("img");
		for (Element cimgemt : cimg) {
			String cimgSrc = cimgemt.attr("src");
			if (!"".equals(cimgSrc)) {
				String ctempFilePath = Tools.getLineFile(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null || "".equals(mydest))
					cimgemt.attr("src", mydest);
				else
					cimgemt.remove();
			}
		}
	}
}
