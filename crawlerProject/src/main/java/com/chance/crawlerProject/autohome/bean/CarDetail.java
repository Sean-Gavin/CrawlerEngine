package com.chance.crawlerProject.autohome.bean;
// Generated 2018-1-12 17:58:33 by Hibernate Tools 4.3.1.Final

/**
 * CarDetail generated by hbm2java
 */
public class CarDetail implements java.io.Serializable {

	private Integer id;
	private String url;
	private Integer carId;
	private String newCarPrice;
	private String carMallPrice;
	private String usedCarPrice;
	private String engine;
	private String gearbox;
	private String carStructure;
	private String carStyle;
	private String carStyleAttentionRank;
	private String userScore;
	private String userPriseNum;
	private String carTroubleNum;
	private String carTroubleAverageScore;
	private String carSpaceScore;
	private String carPowerScore;
	private String carControlScore;
	private String carOilConsumptionScore;
	private String carComfortScore;
	private String carExteriorScore;
	private String carInteriorScore;
	private String carCostPerformanceScore;
	private String userAttentionCars;

	public CarDetail() {
	}

	public CarDetail(String url, Integer carId, String newCarPrice, String carMallPrice, String usedCarPrice,
			String engine, String gearbox, String carStructure, String carStyle, String carStyleAttentionRank,
			String userScore, String userPriseNum, String carTroubleNum, String carTroubleAverageScore,
			String carSpaceScore, String carPowerScore, String carControlScore, String carOilConsumptionScore,
			String carComfortScore, String carExteriorScore, String carInteriorScore, String carCostPerformanceScore,
			String userAttentionCars) {
		this.url = url;
		this.carId = carId;
		this.newCarPrice = newCarPrice;
		this.carMallPrice = carMallPrice;
		this.usedCarPrice = usedCarPrice;
		this.engine = engine;
		this.gearbox = gearbox;
		this.carStructure = carStructure;
		this.carStyle = carStyle;
		this.carStyleAttentionRank = carStyleAttentionRank;
		this.userScore = userScore;
		this.userPriseNum = userPriseNum;
		this.carTroubleNum = carTroubleNum;
		this.carTroubleAverageScore = carTroubleAverageScore;
		this.carSpaceScore = carSpaceScore;
		this.carPowerScore = carPowerScore;
		this.carControlScore = carControlScore;
		this.carOilConsumptionScore = carOilConsumptionScore;
		this.carComfortScore = carComfortScore;
		this.carExteriorScore = carExteriorScore;
		this.carInteriorScore = carInteriorScore;
		this.carCostPerformanceScore = carCostPerformanceScore;
		this.userAttentionCars = userAttentionCars;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUrl() {
		return this.url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Integer getCarId() {
		return this.carId;
	}

	public void setCarId(Integer carId) {
		this.carId = carId;
	}

	public String getNewCarPrice() {
		return this.newCarPrice;
	}

	public void setNewCarPrice(String newCarPrice) {
		this.newCarPrice = newCarPrice;
	}

	public String getCarMallPrice() {
		return this.carMallPrice;
	}

	public void setCarMallPrice(String carMallPrice) {
		this.carMallPrice = carMallPrice;
	}

	public String getUsedCarPrice() {
		return this.usedCarPrice;
	}

	public void setUsedCarPrice(String usedCarPrice) {
		this.usedCarPrice = usedCarPrice;
	}

	public String getEngine() {
		return this.engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public String getGearbox() {
		return this.gearbox;
	}

	public void setGearbox(String gearbox) {
		this.gearbox = gearbox;
	}

	public String getCarStructure() {
		return this.carStructure;
	}

	public void setCarStructure(String carStructure) {
		this.carStructure = carStructure;
	}

	public String getCarStyle() {
		return this.carStyle;
	}

	public void setCarStyle(String carStyle) {
		this.carStyle = carStyle;
	}

	public String getCarStyleAttentionRank() {
		return this.carStyleAttentionRank;
	}

	public void setCarStyleAttentionRank(String carStyleAttentionRank) {
		this.carStyleAttentionRank = carStyleAttentionRank;
	}

	public String getUserScore() {
		return this.userScore;
	}

	public void setUserScore(String userScore) {
		this.userScore = userScore;
	}

	public String getUserPriseNum() {
		return this.userPriseNum;
	}

	public void setUserPriseNum(String userPriseNum) {
		this.userPriseNum = userPriseNum;
	}

	public String getCarTroubleNum() {
		return this.carTroubleNum;
	}

	public void setCarTroubleNum(String carTroubleNum) {
		this.carTroubleNum = carTroubleNum;
	}

	public String getCarTroubleAverageScore() {
		return this.carTroubleAverageScore;
	}

	public void setCarTroubleAverageScore(String carTroubleAverageScore) {
		this.carTroubleAverageScore = carTroubleAverageScore;
	}

	public String getCarSpaceScore() {
		return this.carSpaceScore;
	}

	public void setCarSpaceScore(String carSpaceScore) {
		this.carSpaceScore = carSpaceScore;
	}

	public String getCarPowerScore() {
		return this.carPowerScore;
	}

	public void setCarPowerScore(String carPowerScore) {
		this.carPowerScore = carPowerScore;
	}

	public String getCarControlScore() {
		return this.carControlScore;
	}

	public void setCarControlScore(String carControlScore) {
		this.carControlScore = carControlScore;
	}

	public String getCarOilConsumptionScore() {
		return this.carOilConsumptionScore;
	}

	public void setCarOilConsumptionScore(String carOilConsumptionScore) {
		this.carOilConsumptionScore = carOilConsumptionScore;
	}

	public String getCarComfortScore() {
		return this.carComfortScore;
	}

	public void setCarComfortScore(String carComfortScore) {
		this.carComfortScore = carComfortScore;
	}

	public String getCarExteriorScore() {
		return this.carExteriorScore;
	}

	public void setCarExteriorScore(String carExteriorScore) {
		this.carExteriorScore = carExteriorScore;
	}

	public String getCarInteriorScore() {
		return this.carInteriorScore;
	}

	public void setCarInteriorScore(String carInteriorScore) {
		this.carInteriorScore = carInteriorScore;
	}

	public String getCarCostPerformanceScore() {
		return this.carCostPerformanceScore;
	}

	public void setCarCostPerformanceScore(String carCostPerformanceScore) {
		this.carCostPerformanceScore = carCostPerformanceScore;
	}

	public String getUserAttentionCars() {
		return this.userAttentionCars;
	}

	public void setUserAttentionCars(String userAttentionCars) {
		this.userAttentionCars = userAttentionCars;
	}

}
