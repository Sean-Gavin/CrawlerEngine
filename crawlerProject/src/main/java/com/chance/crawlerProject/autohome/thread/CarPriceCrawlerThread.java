package com.chance.crawlerProject.autohome.thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarPrice;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarPriceCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 切换proxy
 * @author Sean
 * @date 创建时间：Dec 27, 2017 11:17:50 AM
 * @version 1.0
 * 
 */
public class CarPriceCrawlerThread extends BaseCrawlerThread<Object> {
	
	private static Logger logger = LoggerFactory.getLogger(CarPriceCrawlerThread.class);
	
	private static String URL_BASE = "https://jiage.autohome.com.cn/price/carlist/s-";
	
	private static String BUY_CAR_XPATH = "//div[@class='main-info']//em[@class='red']";
	private static String NEW_CAR_GUIDE_XPATH = "//div[@class='main-info']//span[@class='fr mr20']/em";
	private static String RECOMMEND_CAR_XPATH = "//ul[@class='recommended']/li";
	
	public CarPriceCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarPriceCrawlerManager.getTaskSign();
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
			
			String url = URL_BASE + carInfo.getAutohomeId();
			
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA(), null, false);
			
			String carPrice = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, BUY_CAR_XPATH);
			
			String guidePrice = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEW_CAR_GUIDE_XPATH);
			
			StringBuffer recommendCarSB = new StringBuffer();
			
			List<TagNode> recommendNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, RECOMMEND_CAR_XPATH);
			
			for (TagNode node : recommendNodeList) {
				
				String autoCarUrl = HtmlCleanerUtils.analyseNodeByXPath(node, "/a", "href");
				int autoCarId = StringTools.parseIntFromStr(StringTools.matcherStrByRegular(autoCarUrl, "\\d+"));
				String carName = HtmlCleanerUtils.analyseNodeByXPath(node, "/a");
				String price = HtmlCleanerUtils.analyseNodeByXPath(node, "//strong");
				price = price.replace(",", "-");
				recommendCarSB.append(carName).append("-").append(autoCarId).append(":").append(price).append(",");
			}
			String recommendCar = null;
			if (!StringTools.isBlank(recommendCarSB)) {
				recommendCar = recommendCarSB.substring(0, recommendCarSB.length() - 1);
			}
			
			CarPrice carPriceBean = new CarPrice(carInfo.getId(), url, carPrice, guidePrice, recommendCar);
			
			logger.info("save carPriceBean--" + url);
			HibernateOperationUtils.saveObejct(carPriceBean);
			Thread.sleep(500);
		} catch (Exception e){
			logger.info("Catch exception" + e);
		} 
	}

}

