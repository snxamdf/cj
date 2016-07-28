package com.modern.datacollect.api;

import java.io.File;
import java.io.Serializable;
import java.util.List;

/**
 * 采集到的每一条数据<br/>
 * **/
public class Data implements Serializable{
	private static final long serialVersionUID = -41725340683493632L;
	/** 
	 * 内容ID <br/>
	 * 开发人员生成 唯一 <br/>
	 * <strong>必须</strong>
	 */
	private String contentId;
	/** 
	 * 内容标题  <br/>
	 * <strong>必须</strong>
	 */
	private String title;
	/** 
	 * 内容 富文本  <br/>
	 * <strong>必须</strong>
	 */
	private String content;
	/** 
	 * 关键字 <br/>
	 * <strong>非必须</strong>
	 */
	private String keywords;
	/** 
	 * 图片列表 - 保存在正式目录下 <br/>
	 * <strong>必须</strong>
	 */
	private List<File> picList;
	/** 
	 * 经度 <br/>
	 * <strong>非必须</strong>
	 */
	private String longitude;
	/** 
	 * 纬度 <br/>
	 * <strong>非必须</strong>
	 */
	private String latitude;
	/** 
	 * 是否百度坐标系<br/>
	 * <strong>非必须</strong>
	 */
	private Boolean isBaidu;
	/** 
	 * 地址 <br/>
	 * <strong>非必须</strong>
	 */
	private String address;
	
	public String getContentId() {
		return contentId;
	}
	public void setContentId(String contentId) {
		this.contentId = contentId;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public String getKeywords() {
		return keywords;
	}
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}
	public List<File> getPicList() {
		return picList;
	}
	public void setPicList(List<File> picList) {
		this.picList = picList;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public Boolean getIsBaidu() {
		return isBaidu;
	}
	public void setIsBaidu(Boolean isBaidu) {
		this.isBaidu = isBaidu;
	}
	public String getAddress() {
		return address;
	}
	public void setAddress(String address) {
		this.address = address;
	}
	@Override
	public String toString() {
		return "Data [contentId=" + contentId + ", title=" + title
				+ ", content=" + content + ", keywords=" + keywords
				+ ", picList=" + picList + ", longitude=" + longitude
				+ ", latitude=" + latitude + ", isBaidu=" + isBaidu
				+ ", address=" + address + "]";
	}
}
