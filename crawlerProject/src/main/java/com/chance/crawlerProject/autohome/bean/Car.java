package com.chance.crawlerProject.autohome.bean;
// Generated 2018-1-12 17:58:33 by Hibernate Tools 4.3.1.Final

/**
 * Car generated by hbm2java
 */
public class Car implements java.io.Serializable {

	private Integer id;
	private String sign;
	private String brand;
	private String brand2;
	private String model;
	private String url;
	private String price;
	private Integer autohomeId;

	public Car() {
	}

	public Car(String sign, String brand, String brand2, String model, String url, String price, Integer autohomeId) {
		this.sign = sign;
		this.brand = brand;
		this.brand2 = brand2;
		this.model = model;
		this.url = url;
		this.price = price;
		this.autohomeId = autohomeId;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getSign() {
		return this.sign;
	}

	public void setSign(String sign) {
		this.sign = sign;
	}

	public String getBrand() {
		return this.brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getBrand2() {
		return this.brand2;
	}

	public void setBrand2(String brand2) {
		this.brand2 = brand2;
	}

	public String getModel() {
		return this.model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getPrice() {
		return this.price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public Integer getAutohomeId() {
		return this.autohomeId;
	}

	public void setAutohomeId(Integer autohomeId) {
		this.autohomeId = autohomeId;
	}

}
