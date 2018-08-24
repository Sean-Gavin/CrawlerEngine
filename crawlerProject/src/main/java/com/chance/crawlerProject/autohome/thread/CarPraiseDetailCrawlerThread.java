package com.chance.crawlerProject.autohome.thread;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.CarPraiseDetail;
import com.chance.crawlerProject.autohome.bean.UserInfo;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarPraiseDetailCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 任务：单条口碑信息
 * 爬虫策略：正常爬取信息
 * 增量策略：把口碑信息作为任务爬取。
 * 
 * @author Sean
 * @date 创建时间：Dec 18, 2017 6:26:06 PM
 * @version 1.0
 * 
 */
public class CarPraiseDetailCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarPraiseDetailCrawlerThread.class);
	
	private static String USER_BASE_URL = "https://i.autohome.com.cn/";
	
	private static String AUTH_REQUEST_BASE_URL = "https://k.autohome.com.cn/frontapi/FinalUserData?medalId=1&";
	private static String BEAR_REQUEST_BASE_URL = "https://k.autohome.com.cn/frontapi/GetDealerInfor?dearerandspecIdlist=";
	
	private static String USER_INFO_XPATH = "//a[@id='ahref_UserId']";
	private static String PRAISE_DATE_XPATH = "//div[@class='title-name name-width-01']/b";
	private static String PRAISE_TITLE_XPATH = "//div[@class='kou-tit']/h3";
	private static String PRAISE_CONTENT_XPATH = "//div[@class='text-con']";
	private static String PRAISE_CLIENT_XPATH = "//div[@class='title-name name-width-01']/span";
	
	private static String PRAISE_BUY_CAR_INFO_LIST_XPATH = "//div[@class='choose-con']/dl";
	
	private static String PRAISE_BUY_CAR_INFO_XPATH = "//div[@class='choose-con']/dl[1]/dd/a";
	private static String PRAISE_BUY_DEAR_XPATH = "/dd/a";
	private static String PRAISE_BUY_OIL_WEAR_SIZE_XPATH = "/dd/p[1]";
	private static String PRAISE_BUY_TRAVEL_DIS_XPATH = "/dd/p[2]";

	private static Set<Integer> userIdSet = new HashSet<Integer>();
	
	public CarPraiseDetailCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		
		while (true) {
			//全量
//			Object taskInfo = CarPraiseDetailCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.CAR_PRAISE_DETAIL);
			
			if (null == taskInfo) {
				
				countDownLatch.countDown();
				return;
			}
			
			crawler(taskInfo);
		}	
	}
	
	@Override
	public void crawler(Object t) {
		
		CarPraiseDetail taskInfo = (CarPraiseDetail) t;
		
		String url = taskInfo.getUrl();
		
		url = url.replaceAll("\\|", "%7c");
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA());

		String userUrl = "https:" + HtmlCleanerUtils.analyseNodeByXPath(htmlNode, USER_INFO_XPATH, "href");
		
		String autoUserId = StringTools.matcherAllStrByRegular(userUrl, "\\d+");
		
		String praiseDateStr = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, PRAISE_DATE_XPATH);
		Timestamp praiseDate = DateTools.FormatTimeStamp(praiseDateStr, "yyyy年MM月dd日");
		
		String title = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, PRAISE_TITLE_XPATH);
		
		TagNode contentNode = HtmlCleanerUtils.getTagNodeByXPath(htmlNode, PRAISE_CONTENT_XPATH);
		List<TagNode> nodelist = contentNode.getChildTagList();
		for (TagNode temp : nodelist) {
			contentNode.removeChild(temp);
		}
		String content = contentNode.getText().toString().replaceAll("\\s", "");
		
		String client = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, PRAISE_CLIENT_XPATH);
		
//		String buyCarName = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, PRAISE_BUY_CAR_INFO_XPATH);
		
		List<TagNode> buyCarNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PRAISE_BUY_CAR_INFO_XPATH);
		//  /4111
		int buyCarSerId = StringTools.parseIntFromStr(StringTools.matcherAllStrByRegular(
				buyCarNodeList.get(0).getAttributeByName("href"), "\\d+"));
		// /spec/30364
		int buyCarSpecId = StringTools.parseIntFromStr(StringTools.matcherAllStrByRegular(
				buyCarNodeList.get(1).getAttributeByName("href"), "\\d+"));
		//https://k.autohome.com.cn/frontapi/FinalUserData?medalId=1&seriesId=4111&userId=39649629&specId=30364
		StringBuffer tempSB = new StringBuffer();
		tempSB.append(AUTH_REQUEST_BASE_URL).append("seriesId=").append(buyCarSerId).append("&userId=")
				.append(autoUserId).append("&specId=").append(buyCarSpecId);
		String result = HtmlCleanerUtils.getHtmlByUrl(tempSB.toString(), null, null, false);
		JSONObject resultObj = JSON.parseObject(result);
		JSONObject userAuthInfoJsonObj = resultObj.getJSONObject("UserAuthSeries");
		int authSpecId = 0;
		String authcarName = "";
		if (null != userAuthInfoJsonObj) {
			authSpecId = userAuthInfoJsonObj.getIntValue("AuthSpecId");
			authcarName = userAuthInfoJsonObj.getString("AuthSeriesName") + userAuthInfoJsonObj.getString("AuthSpecName");
		}
		
		String buyCarCity = "";
		String carBearName = "";
		Timestamp buyCarDate = null;
		String cost = "";
		String oilWearSize = "";
		String travelDistance = "";
		int space = 0;
		int engine = 0;
		int carHold = 0;
		int oilWear = 0;
		int comfortable = 0;
		int exterior = 0;
		int interior = 0;
		int costPer = 0;
		String goal = null;
		
		List<TagNode> buyCarListNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PRAISE_BUY_CAR_INFO_LIST_XPATH);
		
		for (TagNode nodeInfo : buyCarListNodeList) {
			
			String name = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dt");
			
			if (StringTools.isBlank(name)) {
				
			} else if (name.contains("购买车型")) {
				
			} else if (name.contains("购买地点")) {
				
				buyCarCity = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd");
			} else if (name.contains("购车经销商")) {

				carBearName = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, PRAISE_BUY_DEAR_XPATH, "data-val");
				if (!StringTools.isBlank(carBearName)) {
					carBearName = getCarBearName(carBearName);
				}
			} else if (name.contains("购买时间")) {

				String buyCarTime = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd");
				buyCarDate = DateTools.FormatTimeStamp(buyCarTime, "yyyy年MM月");
			} else if (name.contains("裸车购买价")) {

				cost = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd");
			} else if (name.contains("油耗") || name.contains("目前行驶")) {
				if (!StringTools.isBlank(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dt/p"))) {
					oilWearSize = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, PRAISE_BUY_OIL_WEAR_SIZE_XPATH);
					travelDistance = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, PRAISE_BUY_TRAVEL_DIS_XPATH);
				}
			} else if (name.contains("空间")) {

				space = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("动力")) {

				engine = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("操控")) {

				carHold = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.equals("油耗")) {
				if (StringTools.isBlank(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dt/p"))) {
					oilWear = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
				}
			} else if (name.contains("舒适性")) {

				comfortable = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("外观")) {

				exterior = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("内饰")) {

				interior = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("性价比")) {

				costPer = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd"));
			} else if (name.contains("购车目的")) {

				goal = HtmlCleanerUtils.analyseNodeByXPath(nodeInfo, "/dd");
			}
		}
		
		CarPraiseDetail carPraiseDetail = new CarPraiseDetail(taskInfo.getCarId(), taskInfo.getUrl(), autoUserId, praiseDate, title, content, client, authSpecId, authcarName, 
				buyCarSpecId, buyCarCity, carBearName, buyCarDate, cost, oilWearSize, travelDistance, space, engine, carHold,
				oilWear, comfortable, exterior, interior, costPer, goal);
		
		carPraiseDetail.setId(taskInfo.getId());
		
		HibernateOperationUtils.updateObejct(carPraiseDetail);
		
		//全量
//		if (!userIdSet.contains(autoUserId)) {
//			userIdSet.add(autoUserId);
//			String tempUrl = USER_BASE_URL + autoUserId;
//			
//			UserInfo userInfo = new UserInfo();
//			userInfo.setAutoUserId(autoUserId);
//			userInfo.setUrl(tempUrl);
//			HibernateOperationUtils.saveObejct(userInfo);
//			AutohomeIncrementCrawlTask.addTask(userInfo, TaskKey.USER_INFO);
//		}
		//增量
		List<Object> searchResult = HibernateOperationUtils.searchObjectFromDBByCondition(UserInfo.class, "where autoUserId = " + autoUserId);
		
		if (null == searchResult || searchResult.size() == 0) {
			
			String tempUrl = USER_BASE_URL + autoUserId;
			UserInfo userInfo = new UserInfo();
			userInfo.setAutoUserId(autoUserId);
			userInfo.setUrl(tempUrl);
			HibernateOperationUtils.saveObejct(userInfo);
			logger.info("add task USER_INFO--" + tempUrl);
			AutohomeIncrementCrawlTask.addTask(userInfo, TaskKey.USER_INFO);
		}
	}

	private String getCarBearName(String carBearNameStr) {
		String tempUrl = BEAR_REQUEST_BASE_URL + carBearNameStr;
		String result = HtmlCleanerUtils.getHtmlByUrl(tempUrl, null, null, false);
		JSONObject resultObj = JSON.parseObject(result);
		if (null != resultObj.getJSONObject("result").getJSONArray("List") 
				&& resultObj.getJSONObject("result").getJSONArray("List").size() > 0) {
			JSONObject userAuthInfoJsonObj = resultObj.getJSONObject("result").getJSONArray("List").getJSONObject(0);
			String company = userAuthInfoJsonObj.getString("CompanySimple");
			return company;
		}
		return "";
	}
}

