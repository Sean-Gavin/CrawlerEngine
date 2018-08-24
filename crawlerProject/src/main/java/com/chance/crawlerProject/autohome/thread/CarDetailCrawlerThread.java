package com.chance.crawlerProject.autohome.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarDetail;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarDetailCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 任务：汽车详细信息
 * 爬虫策略：正常爬取信息,其中车商城价格，与二手车价格页面中没有展示，需要单独请求接口
 * 增量策略：把汽车的增量信息作为任务爬取。
 * 
 * @author Sean
 * @date 创建时间：Dec 11, 2017 6:16:30 PM
 * @version 1.0
 * 
 */
public class CarDetailCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarDetailCrawlerThread.class);
	
	private static String MALL_REQUEST_BASE_URL = "https://api.mall.autohome.com.cn/gethomemallad/price/";
	private static String REUSE_REQUEST_BASE_URL = "https://api.che168.com/auto/ForAutoCarPCInterval.ashx?callback=che168CallBack&_appid=cms&yid=0&pid=110000&cid=110100&sid=";
	
	private static String newCarXPath = "//*[@class='autoseries-info']/dl[1]/dt[1]/a[1]";
	private static String engineXPath = "//*[@class='autoseries-info']/dl[1]/dd[2]/a";
	private static String carStructureAndgearboxXPath = "//*[@class='autoseries-info']/dl[1]/dd[3]";
	private static String carStyleXPath = "//*[@class='path fn-clear']/div[1]/a[2]";
	private static String carStyleAttentionRankXPath = "//*[@class='subnav-title-rank']/span";
	private static String userScoreXPath = "//*[@class='koubei-score']/div[1]/a[2]";
	private static String userPriseNumXPath = "//*[@class='koubei-score']/div[1]/span[2]/span";
	private static String carTroubleNumXPath = "//*[@class='koubei-score']/div[2]/a[1]";
	private static String carTroubleAverageScoreXPath = "//*[@class='koubei-score']/div[2]/span[2]/em";
	private static String carSpaceScoreXPath = "//*[@class='table-rank']/tbody/tr[1]/td[1]/a[1]";
	private static String carPowerScoreXPath = "//*[@class='table-rank']/tbody/tr[2]/td[1]/a[1]";
	private static String carControlScoreXPath = "//*[@class='table-rank']/tbody/tr[3]/td[1]/a[1]";
	private static String carOilConsumptionScoreXPath = "//*[@class='table-rank']/tbody/tr[4]/td[1]/a[1]";
	private static String carComfortScoreXPath = "//*[@class='table-rank']/tbody/tr[5]/td[1]/a[1]";
	private static String carExteriorScoreXPath = "//*[@class='table-rank']/tbody/tr[6]/td[1]/a[1]";
	private static String carInteriorScoreXPath = "//*[@class='table-rank']/tbody/tr[7]/td[1]/a[1]";
	private static String carCostPerformanceScoreXPath = "//*[@class='table-rank']/tbody/tr[8]/td[1]/a[1]";
	
	public CarDetailCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}
	
	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarDetailCrawlerManager.getTaskSign();
			
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.CAR);
			
			if (null == taskInfo) {
				
				countDownLatch.countDown();
				return;
			}
			
			crawler(taskInfo);
		}
	}

	private static Map<String, String> getStructureAndGearboxFromNode(TagNode htmlNode, String engineAndgearboxXPath2) {
		
		Map<String, String> map = new HashMap<String, String>();
		
		TagNode rootNode = HtmlCleanerUtils.getTagNodeByXPath(htmlNode, engineAndgearboxXPath2);
		
		if (null == rootNode) {
			return new HashMap<String, String>();
		}
		
		List<TagNode> childList = rootNode.getChildTagList();
		
		List<TagNode> gearboxNodeList = new ArrayList<TagNode> ();
		
		for (TagNode node : childList) {
			if ("ma_l25".equals(node.getAttributeByName("class"))) {
				
				childList.remove(node);
				break;
			}
			gearboxNodeList.add(node);
		}
		childList.removeAll(gearboxNodeList);
		
		StringBuilder sb = new StringBuilder();
		for (TagNode gearboxNode : gearboxNodeList) {
			
			sb.append(gearboxNode.getText().toString() + ",");
		}
		
		if (sb.length() > 1) {
			map.put("gearbox", sb.substring(0, sb.length() - 1));
		}
		
		sb = new StringBuilder();
		for (TagNode structureNode : childList) {
			
			sb.append(structureNode.getText().toString() + ",");
		}
		if (sb.length() > 1) {
			map.put("structure", sb.substring(0, sb.length() - 1));
		}
		
		return map;
	}


	@Override
	public void crawler(Object t) {
		try {
			Car carInfo = (Car) t;
			
    		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(carInfo.getUrl(), 
    				RequestUtils.getHeaderWithIPAndUA());
    		
        	String newCarPrice = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, newCarXPath);
        	
        	String carMallPrice = "";
        	String usedCarPrice = "";
        	
        	String mallRequestUrl = MALL_REQUEST_BASE_URL + carInfo.getAutohomeId() + "/110100?_appid=cms&callback=mallCallback";
        	
			String result = HtmlCleanerUtils.getHtmlByUrl(mallRequestUrl, RequestUtils.getHeaderWithIPAndUA(), null, false);
			result = result.replaceAll(";mallCallback\\(", "").replaceAll("\\);", "");
			JSONObject resultObj = JSON.parseObject(result);
			JSONObject mallObject = resultObj.getJSONObject("result");
			
			if (null != mallObject) {
				JSONObject temp = mallObject.getJSONObject("list");
				if (null != temp) {
					int minPrice = temp.getIntValue("minPirce");
					int maxPrice = temp.getIntValue("maxPrice");
					carMallPrice = minPrice + "-" + maxPrice;
				}
			}
			
			String reusePriceUrl = REUSE_REQUEST_BASE_URL + carInfo.getAutohomeId();
			result = HtmlCleanerUtils.getHtmlByUrl(reusePriceUrl, RequestUtils.getHeaderWithIPAndUA(), null, false);
			result = result.replaceAll("che168CallBack\\(", "").replaceAll("\\)", "");
			resultObj = JSON.parseObject(result);
			JSONObject reuseObject = resultObj.getJSONObject("result");
			
			if (null != reuseObject) {
				String minPrice = reuseObject.getString("minPrice");
				String maxPrice = reuseObject.getString("maxPrice");
				usedCarPrice = minPrice + "-" + maxPrice + "万";
			}
			
        	String engine = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, engineXPath);
        	
        	Map<String, String> map = new HashMap<String, String>();
        	map = getStructureAndGearboxFromNode(htmlNode, carStructureAndgearboxXPath);
        	String gearbox = map.get("gearbox");
        	String carStructure = map.get("structure");
        	String carStyle = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carStyleXPath);
        	
        	String carStyleAttentionRank = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carStyleAttentionRankXPath);
        	String userScore = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, userScoreXPath);
        	//StringTools.matcherStrByRegular(userPriseNum, "\\w+")
        	String userPriseNum = StringTools.matcherStrByRegular(
        			HtmlCleanerUtils.analyseNodeByXPath(htmlNode, userPriseNumXPath), "\\w+");
        	String carTroubleNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carTroubleNumXPath);
        	String carTroubleAverageScore = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carTroubleAverageScoreXPath);
        	
        	String carSpaceScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carSpaceScoreXPath), "\\d+\\.\\d+");
        	String carPowerScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carPowerScoreXPath), "\\d+\\.\\d+");
        	String carControlScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carControlScoreXPath), "\\d+\\.\\d+");
        	String carOilConsumptionScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carOilConsumptionScoreXPath), "\\d+\\.\\d+");
        	String carComfortScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carComfortScoreXPath), "\\d+\\.\\d+");
        	String carExteriorScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carExteriorScoreXPath), "\\d+\\.\\d+");
        	String carInteriorScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carInteriorScoreXPath), "\\d+\\.\\d+");
        	String carCostPerformanceScore = StringTools.matcherStrByRegular(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, carCostPerformanceScoreXPath), "\\d+\\.\\d+");
        	//TODO not found
        	String userAttentionCars = "";

        	CarDetail carDetail = new CarDetail(carInfo.getUrl(), carInfo.getId(), newCarPrice, carMallPrice, usedCarPrice,
        			engine, gearbox, carStructure, carStyle, carStyleAttentionRank, userScore, userPriseNum, carTroubleNum,
        			carTroubleAverageScore, carSpaceScore, carPowerScore, carControlScore, carOilConsumptionScore,
        			carComfortScore, carExteriorScore, carInteriorScore, carCostPerformanceScore, userAttentionCars);
        	
//        	logger.info("seve carDetail" + carDetail.getUrl());
        	HibernateOperationUtils.saveObejct(carDetail);

        } catch (Exception e) {
			logger.info("Catch exception" + e);
		}
	}
}

