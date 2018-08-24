package com.chance.crawlerProject.autohome.thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarBrandCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 
 * 任务：汽车品牌，车型，指导价
 * 爬虫策略：此类功能为爬取所有的汽车信息任务，具体是按汽车字母A-Z遍历所有的汽车页面。
 * 增量策略： 由于车辆信息较少，增量策略为遍历所有页面，然后数据库查找出增加的车。
 * 爬取页面策略：
 * 分析后发现，汽车按照字母排序，每个字母都有单独的页面。单独抓页面即可实现抓取所有的汽车
 * 
 * 
 * @author Sean
 * @date 创建时间：Dec 11, 2017 12:12:49 PM
 * @version 1.0
 * 
 */
public class CarBrandCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarBrandCrawlerThread.class);
	
	private static String baseUrl = "https://www.autohome.com.cn/grade/carhtml/";
	private static String FIRST_LEVEL_BRAND_XPATH = "/dt/div/a";
	private static String ESCOND_LEVEL_BRAND_XPATH = "/dd/div[@class='h3-tit']";
	private static String THIRD_LEVEL_BRAND_XPATH = "/dd/ul[@class='rank-list-ul']";
	private static String CAR_BRAND_LIST_XPATH = "//dl";
	private static String PRICE_XPATH = "/div[1]/a";
	private static String BRAND_XPATH = "/h4/a";
	
	public CarBrandCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}
	
	public void run() {
		while (true) {
			String url = CarBrandCrawlerManager.getTaskSign();
			
			if (null == url) {
				countDownLatch.countDown();
				return;
			}
			
			crawler(url);
		}
	}

	@Override
	public void crawler(Object t) {
		try {
			StringBuffer url = new StringBuffer();
			url.append(baseUrl).append(String.valueOf(t)).append(".html");
			
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url.toString(), 
					RequestUtils.getHeaderWithIPAndUA(), null);
			
			if (null == htmlNode) {
				return;
			}
			
			List<TagNode> dlNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, CAR_BRAND_LIST_XPATH);
			
			for (TagNode node : dlNodeList) {
				
				TagNode fisrtLevelBrandNode = HtmlCleanerUtils.getTagNodeByXPath(node, FIRST_LEVEL_BRAND_XPATH);
				List<TagNode> secondLevelBrandNodeList = HtmlCleanerUtils.getTagNodeListByXPath(node, ESCOND_LEVEL_BRAND_XPATH);
				List<TagNode> thirdLevelBrandNodeList = HtmlCleanerUtils.getTagNodeListByXPath(node, THIRD_LEVEL_BRAND_XPATH);
				int j = 0;
				for (TagNode secondNode : secondLevelBrandNodeList) {
					
					TagNode tagNode = thirdLevelBrandNodeList.get(j ++);
					List<TagNode> liNodeList = HtmlCleanerUtils.getTagNodeListByXPath(tagNode, "/li[@id]");
					for (TagNode liNode : liNodeList) {
						TagNode brand = HtmlCleanerUtils.getTagNodeByXPath(liNode, BRAND_XPATH);
						TagNode price = HtmlCleanerUtils.getTagNodeByXPath(liNode, PRICE_XPATH);
						
						String brandUrl = "http:" + brand.getAttributeByName("href");
						
		    			String priceStr = price.getText().toString();
						
						if (StringTools.isBlank(priceStr) || !priceStr.contains("万")) {
							priceStr = "暂无报价";
						}
						int autohomeId = Integer.parseInt(StringTools.deleteStartAndEndChar(StringTools.matcherStrByRegular(brandUrl, "/(\\d+)/")));
		    			
						Car car = new Car(String.valueOf(t), fisrtLevelBrandNode.getText().toString(), secondNode.getText().toString(), 
		    					brand.getText().toString(), brandUrl, priceStr, autohomeId);
		    			//全量
//		    			HibernateOperationUtils.saveObejct(car);
		    			//增量
		    			List<Object> result= HibernateOperationUtils.searchObjectFromDBByCondition(Car.class, "where autohomeId = " + autohomeId);
		    			if (result.isEmpty()) {
		    				HibernateOperationUtils.saveObejct(car);
		    				logger.info("add a task--" + car.getUrl());
		    				AutohomeIncrementCrawlTask.addTask(car, TaskKey.CAR);
		    			}
					}
				}
			}
		} catch (Exception e) {
			logger.info("Catch exception" + e);
		}
	}
}

