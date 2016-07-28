package com.modern.datacollect.api;

/**
 * 采集配置
 * **/
public class Config {
	/**
	 * 采集ID 全局唯一 由系统生成
	 */
	private Long collId;
	/**
	 * 采集用户ID <br/>
	 * 后台配置 开发人员不关心
	 */
	private Long userId;
	/**
	 * 采集类目ID <br/>
	 * 后台配置 开发人员不关心
	 */
	private Long cataId;
	/**
	 * 采集站点名称 <br/>
	 * 后台配置 开发人员不关心
	 */
	private String siteName;
	/**
	 * 采集网站URL <br/>
	 * 后台配置
	 */
	private String siteUrl;
	/**
	 * 采集网站配置 <br/>
	 * 后台配置
	 */
	private String siteConfig;

	public Long getCollId() {
		return collId;
	}

	public void setCollId(Long collId) {
		this.collId = collId;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getCataId() {
		return cataId;
	}

	public void setCataId(Long cataId) {
		this.cataId = cataId;
	}

	public String getSiteName() {
		return siteName;
	}

	public void setSiteName(String siteName) {
		this.siteName = siteName;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public void setSiteUrl(String siteUrl) {
		this.siteUrl = siteUrl;
	}

	public String getSiteConfig() {
		return siteConfig;
	}

	public void setSiteConfig(String siteConfig) {
		this.siteConfig = siteConfig;
	}

	@Override
	public String toString() {
		return "Config [collId=" + collId + ", userId=" + userId + ", cataId="
				+ cataId + ", siteName=" + siteName + ", siteUrl=" + siteUrl
				+ ", siteConfig=" + siteConfig + "]";
	}
}
