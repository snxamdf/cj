package com.modern.datacollect.api;

import java.io.File;

/**
 * 数据采集器基类，所有数据采集程序都继承这个类<br/>
 * 
 * 
 * 玩法：
 * 
 * 1、在后台配置一个网站URL，初始参数，和一个处理类
 * com.xx.xx.xxColl（你写的，继承自com.modern.datacollect.api.Collector）
 * 2、程序跑起来的时候，反射得到这个类的对象，注入进去配置好的URL，初始参数 3、系统调用这个类的begin方法进行处理，
 * 4、begin就是你实现的，会使用URL和初始参数进行抓取数据
 * 5、当抓取到数据的时候，封装成Data，调用whenOneData()方法，通知系统进行数据入库
 * 6、当需要更新初始参数，比如抓完一页了，或者怎么样，调用updateSiteConfig()方法
 * 7、当没数据可抓或者要停止的时候，跳出循环，调用stop()方法
 * 
 * 特别注意： 1、只允许使用lib下面的包 2、Data里的contentId代表目标网站的内容的唯一性ID，重复抓取的时候，ID保持不变
 * 3、只需实现begin()方法，看 DemoCollector 4、其他方法均由系统实现，无需理会 5、java源文件编码使用UTF-8 6、JDK 使用
 * 1.6
 * 
 * */
public abstract class Collector {

	public Collector() {

	}

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
		return null;
	}

	/**
	 * 获取****临时****图片保存的目录<br/>
	 * 网络上下载下来的图片临时保存在这个目录下
	 * */
	public String getTempFileDir() {
		return null;
	}

	/**
	 * 判断数据是否已经抓取过，通过内容ID判断
	 * */
	public boolean isDataExists(String contentId) {

		return false;
	}

	/**
	 * 每抓取一条数据的时候，采集程序主动调用这个方法入库
	 * */
	public void whenOneData(Data data) {
		System.out.println(data.toString());
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

	/**
	 * 用于处理内容中的img标签的图片<br/>
	 * 1、解析img 的src属性 2、下载图片到 getTempFileDir 目录下 3、下载成功后将图片转存到 getSaveFileDir 下<br/>
	 * 4、将转存后的file对象调用此方法，得到Url<br/>
	 * 5、替换img 的src为此Url
	 * */
	public String getMySiteImgSrc(File file) {

		return file.getAbsolutePath();
	}
}
