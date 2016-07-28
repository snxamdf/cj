package com.modern.datacollect.api;

import java.io.File;

import com.modern.datacollect.impl.Tools;

/**
 * 数据采集器基类，所有数据采集程序都继承这个类<br/>
 * */
public abstract class Collector {

	/**
	 * 数据采集配置，由系统注入<br/>
	 * 初始配置由管理员在系统后台设置
	 * */
	private Config config;

	/**
	 * 更新当前站点的采集配置<br/>
	 * 这个采集配置为自定义字符串<br/>
	 * 比如：<br/>
	 * 采集系统初始化的时候，在后台配置的是 {"pn":1,"auth":"xxxx"}<br/>
	 * 那么当处理完第一页的时候，调用这个方法，更新配置为：{"pn":2,"auth":"xxxx"}<br/>
	 * 这个配置会被写入数据库，当下次启动采集程序的时候，由系统注入给采集程序
	 * 
	 * @see Config#getSiteConfig()
	 * */
	public void updateSiteConfig(String siteConfig) {
		if (config != null) {
			config.setSiteConfig(siteConfig);
		}
	}

	/**
	 * 供采集程序获取采集配置<br/>
	 * 采集程序只关注 siteConfig 和 siteUrl<br/>
	 * 这两个字段由管理员在系统后台配置
	 * 
	 * @see Config#getSiteConfig()
	 * @see Config#getSiteUrl()
	 */
	public Config getConfig() {
		return config;
	}

	/**
	 * 获取图片保存的目录<br/>
	 * 当图片都下载完成，准备调用 {@link #whenOneData(Data)}方法的时候<br/>
	 * 将该文章的所有临时图片保存在这个目录下<br/>
	 * <br/>
	 * 严禁直接将图片保存在这个目录，
	 * */
	public String getSaveFileDir() {
		return "";
	}

	/**
	 * 获取****临时****图片保存的目录<br/>
	 * 网络上下载下来的图片临时保存在这个目录下
	 * */
	public String getTempFileDir() {
		return "";
	}

	/**
	 * 每抓取一条数据的时候，采集程序主动调用这个方法入库
	 * */
	public void whenOneData(Data data) {
		System.out.println("Got one data:" + data);
		Tools.write(data.toString());
	}

	/**
	 * 停止采集程序<br/>
	 * 当采集程序因为某种原因需要停止的时候主动调用
	 * */
	public void stop() {

	}

	/**
	 * 采集程序的启动入口<br/>
	 * 真正的取抓取数据<br/>
	 * *****由开发者实现******
	 * */
	public abstract void begin();

	public String getMySiteImgSrc(File file) {
		return file.getPath();
	}
}
