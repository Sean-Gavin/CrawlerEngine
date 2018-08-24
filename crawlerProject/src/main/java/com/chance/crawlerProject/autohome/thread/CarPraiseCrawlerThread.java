package com.chance.crawlerProject.autohome.thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarPraise;
import com.chance.crawlerProject.autohome.bean.CarPraiseDetail;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;

/** 
 * 任务：汽车口碑页面
 * 爬虫策略：正常爬取 + 请求特殊字段
 * 增量策略：把汽车的增量信息作为任务爬取。
 * 
 * PS:由于此页面有反爬策略，需要切换代理以及限制放问速度请求
 * 
 * 此功能为详细口碑页面爬取提供任务列表（详细页面URL）
 * 
 * @author Sean
 * @date 创建时间：Dec 15, 2017 3:46:52 PM
 * @version 1.0
 * 
 */
public class CarPraiseCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarPraiseCrawlerThread.class);
	
	private static String BASE_URL = "https://k.autohome.com.cn/";
	
	private static String USER_SCORE_XPATH = "//*[@id='0']/dl/dd/ul/li[2]/span[1]/span[2]";
	private static String FUEL_ECONOMY_XPATH = "//*[@id='0']/dl/dd/ul/li[3]/div/div[2]";
	private static String PRAISE_SUM_XPATH = "//*[@id='0']/div/div/a";
	private static String NEW_CAR_BREAK_NUM_XPATH = "//div[@class='quality-show quality-show-chexi']/div[2]/a[1]/b/span";
	private static String OLD_CAR_BREAK_NUM_XPATH = "//div[@class='quality-show quality-show-chexi']/div[2]/a[2]/b/span";
	private static String EXTERIOR_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[1]/dl/dd/span";
	private static String DRIVE_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[2]/dl/dd/span";
	private static String FUNCTION_BREAK_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[3]/dl/dd/span";
	private static String ED_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[4]/dl/dd/span";
	private static String CHAIR_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[5]/dl/dd/span";
	private static String AIR_CON_BREAK_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[6]/dl/dd/span";
	private static String INTERIOR_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[7]/dl/dd/span";
	private static String ENGINE_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[8]/dl/dd/span";
	private static String SHIFT_BREAK_NUM_XPATH = "//*[@id='quality-chart-box-01']/div[1]/div[2]/a[9]/dl/dd/span";
	
	private static String PRAISE_INFO_LIST_XPATH = "//div[@class='mouthcon-cont fn-clear']";
	private static String PRAISE_DETAIL_XPATH = "/div[2]/div[1]/div[2]/div[1]/div/a";
	
	public CarPraiseCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}
	
	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarPraiseCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.CAR);
			if (null == taskInfo) {
				
				countDownLatch.countDown();
				return;
			}
			
			crawler(taskInfo);
		}
	}
	
	@Override
	public void crawler(Object t) {
		try {
			Car carInfo = (Car) t;
			
			String url = BASE_URL + carInfo.getAutohomeId();
			
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getPriseHeader(), null, true);
			
			String userScore = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, USER_SCORE_XPATH);
			String fuelEconomy = getFuelEconomy(htmlNode);
			String praiseInfoSum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, PRAISE_SUM_XPATH);
			praiseInfoSum = praiseInfoSum.replaceAll("\\(\\d+\\)", "");
			if (praiseInfoSum.contains("我来写口碑")){
				praiseInfoSum = "";
			}
			String newCarBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEW_CAR_BREAK_NUM_XPATH);
			String oldCarBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, OLD_CAR_BREAK_NUM_XPATH);
			String exteriorBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, EXTERIOR_BREAK_NUM_XPATH);
			String interiorBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, INTERIOR_BREAK_NUM_XPATH);
			String driveBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, DRIVE_BREAK_NUM_XPATH);
			String functionBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, FUNCTION_BREAK_XPATH);
			String EDBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, ED_BREAK_NUM_XPATH);
			String chairBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, CHAIR_BREAK_NUM_XPATH);
			String airConBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, AIR_CON_BREAK_XPATH);
			String engineBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, ENGINE_BREAK_NUM_XPATH);
			String shiftBreakNum = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, SHIFT_BREAK_NUM_XPATH);
			
			int page = 1;
			int praiseNum = 0;
			while (true) {
				List<TagNode> praiseInfoList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PRAISE_INFO_LIST_XPATH);
				
				for (TagNode praiseInfo : praiseInfoList) {
					String praiseUrl = "https:" + HtmlCleanerUtils.analyseNodeByXPath(praiseInfo, PRAISE_DETAIL_XPATH, "href");
					if (!praiseUrl.equals("https:")) {
						CarPraiseDetail carPraiseDetail = new CarPraiseDetail();
						carPraiseDetail.setCarId(carInfo.getId());
						carPraiseDetail.setUrl(praiseUrl);
						HibernateOperationUtils.saveObejct(carPraiseDetail);
						
						//提供增量任务
						logger.info("add Task : CAR_PRAISE_DETAIL--" + carPraiseDetail.getUrl());
						AutohomeIncrementCrawlTask.addTask(carPraiseDetail, TaskKey.CAR_PRAISE_DETAIL);
					}
					praiseNum ++;
				}

				//不足15条，或者总数为0，表示不足一页，退出。
				if (praiseInfoList.size() != 15 || praiseInfoList.size() == 0) {
					break;
				} else {
					//限制频率
					Thread.sleep(1000);
					String tempUrl = BASE_URL + carInfo.getAutohomeId() + "/index_" + (++page) + ".html?";
					htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(tempUrl, RequestUtils.getPriseHeader(), null, true);
				}
			}
			//限制频率
			Thread.sleep(1000);
			
			CarPraise carPraise = new CarPraise(carInfo.getId(), praiseNum, url, userScore, fuelEconomy, praiseInfoSum, newCarBreakNum, 
					oldCarBreakNum, exteriorBreakNum, interiorBreakNum, driveBreakNum, functionBreakNum, EDBreakNum,
					chairBreakNum, airConBreakNum, engineBreakNum, shiftBreakNum);
//			logger.info("save carPraise--" + carPraise.getPageUrl());
			HibernateOperationUtils.saveObejct(carPraise);
			
		} catch (Exception e) {
			logger.info("catch exception:" + e);
			e.printStackTrace();
		}
	}

	//油耗要单独解析，组装格式示例： 1.4T:8.0L,2.0T:10.0L
	private static String getFuelEconomy(TagNode htmlNode) {
		
		StringBuffer result = new StringBuffer();
		
		TagNode fuelEconomyNode = HtmlCleanerUtils.getTagNodeByXPath(htmlNode, FUEL_ECONOMY_XPATH);
		
		if (null == fuelEconomyNode) {
			return "";
		} 
		
		List<TagNode> divNodeList = fuelEconomyNode.getChildTagList();
		
		for (TagNode divNode : divNodeList) {
			
			String fuelId = "#" + divNode.getAttributeByName("id");
			String fuelXPath = "//a[@data-target='" + fuelId + "']";
			String fuel = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, fuelXPath);
			
			String economyXPath = "/div/em[1]"; 
			String economy = HtmlCleanerUtils.analyseNodeByXPath(divNode, economyXPath);
			result.append(fuel).append(":").append(economy).append(",");
			
		}
		
		return result.substring(0, result.length()-1);
	}

}

