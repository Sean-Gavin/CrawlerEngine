package com.chance.crawlerProject.autohome.bean;
// Generated 2018-1-12 17:58:33 by Hibernate Tools 4.3.1.Final

/**
 * CarConfigItem generated by hbm2java
 */
public class CarConfigItem implements java.io.Serializable {

	private Integer id;
	private Integer autoId;
	private String name;
	private String detailUrl;

	public CarConfigItem() {
	}

	public CarConfigItem(Integer autoId, String name, String detailUrl) {
		this.autoId = autoId;
		this.name = name;
		this.detailUrl = detailUrl;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAutoId() {
		return this.autoId;
	}

	public void setAutoId(Integer autoId) {
		this.autoId = autoId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDetailUrl() {
		return this.detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

}
