package com.chance.crawlerProject.autohome.thread;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarQuestion;
import com.chance.crawlerProject.autohome.bean.CarQuestionLevel;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarQuestionCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 28, 2017 4:18:44 PM
 * @version 1.0
 * 
 */
public class CarQuestionCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarQuestionCrawlerThread.class);
	
	private static String BASE_URL = "https://zhidao.autohome.com.cn/list/";
	private static String HOST_URL = "https://zhidao.autohome.com.cn";
	private static String QUESTION_URL_XPATH = "/div/em/a";
	private static String NEXT_PAGE_XPATH = "/div/em/a";
	
	public CarQuestionCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarQuestionCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.ALL_CAR);
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
			
			List<Object> signList = HibernateOperationUtils.searchObjectFromDB(CarQuestionLevel.class);
			
			for (Object obj : signList) {
				
				CarQuestionLevel sign = (CarQuestionLevel) obj;
				
				StringBuffer urlSB = new StringBuffer();
				String url = urlSB.append(BASE_URL).append(carInfo.getAutohomeId()).append("/")
						.append(sign.getTopLevelId()).append("-").append(sign.getChildLevelId())
						.append("/s2-1.html").toString();
				
				while (!HOST_URL.equals(url)) {
					
					TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
							RequestUtils.getHeaderWithIPAndUA(), null);
					
					List<TagNode> questionNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, "//ul[@class='qa-list-con']/li");
					
					for (TagNode node : questionNodeList) {
						
						String questionUrl = HtmlCleanerUtils.analyseNodeByXPath(node, QUESTION_URL_XPATH, "href");
						questionUrl = questionUrl.replaceAll("http", "https");
						
						List<Object> searchResult = HibernateOperationUtils.searchObjectFromDBByCondition(CarQuestion.class, "where url = '"+ questionUrl +"'");
						
						if (null == searchResult || searchResult.size() ==0) {
							CarQuestion carQuestion = new CarQuestion();
							carQuestion.setCarId(carInfo.getId());
							carQuestion.setSubjectId(sign.getId());
							carQuestion.setUrl(questionUrl);
							HibernateOperationUtils.saveObejct(carQuestion);
							logger.info("save CAR_QUESTION--" + questionUrl);
						}
					}
					url = HOST_URL + HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEXT_PAGE_XPATH);
				}
			}
		} catch (Exception e){
			logger.info("Catch exception" + e);
		} 
	}
}

