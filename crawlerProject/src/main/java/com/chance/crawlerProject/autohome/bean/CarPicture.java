package com.chance.crawlerProject.autohome.bean;
// Generated 2018-1-12 17:58:33 by Hibernate Tools 4.3.1.Final

/**
 * CarPicture generated by hbm2java
 */
public class CarPicture implements java.io.Serializable {

	private Integer id;
	private Integer carId;
	private Integer seriesId;
	private String picPageUrl;
	private String pictureUrl;
	private Integer type;
	private String exteriorColorCode;
	private String exteriorColor;
	private String interiorColorCode;
	private String interiorColor;

	public CarPicture() {
	}

	public CarPicture(Integer carId, Integer seriesId, String picPageUrl, String pictureUrl, Integer type,
			String exteriorColorCode, String exteriorColor, String interiorColorCode, String interiorColor) {
		this.carId = carId;
		this.seriesId = seriesId;
		this.picPageUrl = picPageUrl;
		this.pictureUrl = pictureUrl;
		this.type = type;
		this.exteriorColorCode = exteriorColorCode;
		this.exteriorColor = exteriorColor;
		this.interiorColorCode = interiorColorCode;
		this.interiorColor = interiorColor;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getCarId() {
		return this.carId;
	}

	public void setCarId(Integer carId) {
		this.carId = carId;
	}

	public Integer getSeriesId() {
		return this.seriesId;
	}

	public void setSeriesId(Integer seriesId) {
		this.seriesId = seriesId;
	}

	public String getPicPageUrl() {
		return this.picPageUrl;
	}

	public void setPicPageUrl(String picPageUrl) {
		this.picPageUrl = picPageUrl;
	}

	public String getPictureUrl() {
		return this.pictureUrl;
	}

	public void setPictureUrl(String pictureUrl) {
		this.pictureUrl = pictureUrl;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getExteriorColorCode() {
		return this.exteriorColorCode;
	}

	public void setExteriorColorCode(String exteriorColorCode) {
		this.exteriorColorCode = exteriorColorCode;
	}

	public String getExteriorColor() {
		return this.exteriorColor;
	}

	public void setExteriorColor(String exteriorColor) {
		this.exteriorColor = exteriorColor;
	}

	public String getInteriorColorCode() {
		return this.interiorColorCode;
	}

	public void setInteriorColorCode(String interiorColorCode) {
		this.interiorColorCode = interiorColorCode;
	}

	public String getInteriorColor() {
		return this.interiorColor;
	}

	public void setInteriorColor(String interiorColor) {
		this.interiorColor = interiorColor;
	}

}
