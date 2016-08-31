package com.modern.datacollect.impl.twobatch.baubauhaus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.modern.datacollect.api.Data;
import com.modern.datacollect.impl.Collectors;
import com.modern.datacollect.impl.Tools;

public abstract class BaubauhausCollectors extends Collectors {

	public void dealwith(Elements body, String tempFileDir, String targetFileDir) {
		for (Element elm : body) {
			try {
				Elements aElm = elm.select("a");
				String href = "http://www.baubauhaus.com" + aElm.attr("href");
				String imgSrc = aElm.select("img").attr("data-src");
				String title = "";
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
				String html = Tools.getRequest1(href);
				Elements imageElm = Tools.getBody("#image", html);
				Elements contentElm = imageElm.select(".content");
				contentElm.select(".overlay").remove();
				Elements relatedImagesElm = imageElm.select("#related-images");
				relatedImagesElm.select(".overlay").remove();
				this.downImg(contentElm, tempFileDir, targetFileDir);
				this.downImg(relatedImagesElm, tempFileDir, targetFileDir);
				Tools.clearsAttr(contentElm);
				Tools.clearsAttr(relatedImagesElm);
				String content = contentElm.toString() + bdiv + relatedImagesElm.select("img").toString() + ediv;
				data.setTitle(title);
				data.setContent(content);
				data.setKeywords(keywords.toString());
				whenOneData(data);
				System.out.println();
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
				String ctempFilePath = Tools.getLineFile1(cimgSrc, tempFileDir);
				File cdest = Tools.copyFileChannel(ctempFilePath, targetFileDir);
				String mydest = getMySiteImgSrc(cdest);
				if (mydest != null) {
					cimgemt.attr("src", mydest);
				} else {
					cimgemt.remove();
				}
			} else {
				cimgemt.remove();
			}
		}
	}
}
