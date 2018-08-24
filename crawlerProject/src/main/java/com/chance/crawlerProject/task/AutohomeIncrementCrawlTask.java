package com.chance.crawlerProject.task;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarNews;
import com.chance.crawlerProject.autohome.bean.CarQuestion;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarBrandCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarClubCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarConfigureCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarDetailCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarNewsCommentCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarNewsCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarPicCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarPraiseCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarPraiseDetailCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarPriceCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarQuestionCrawlerManager;
import com.chance.crawlerProject.autohome.manager.CarQuestionDetailCrawlerManager;
import com.chance.crawlerProject.autohome.manager.UserInfoCrawlerManager;
import com.chance.crawlerProject.utils.HibernateOperationUtils;


/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 15, 2018 11:42:41 AM
 * @version 1.0
 * 
 */
public class AutohomeIncrementCrawlTask extends TaskBase{
	
	private static Logger logger = LoggerFactory.getLogger(AutohomeIncrementCrawlTask.class);
	static int i = 0;
	public static void main(String[] args) {
		
		AutohomeIncrementCrawlTask task = new AutohomeIncrementCrawlTask();
		task.run();
	}
	
	@Override
	public void run() {
		logger.info("=============Start task=============");
		initStartTime();
		
		logger.info(new Date() + "-------clear all task!");
		clearAllTask();
		
		//添加Car 汽车增量任务
		logger.info("CarBrandCrawlerManager start");
		CarBrandCrawlerManager carBrandCrawlerManager = new CarBrandCrawlerManager(1);
		carBrandCrawlerManager.start();
		logger.info("CarBrandCrawlerManager end, Success!--------Time:--------Cost:" + carBrandCrawlerManager.getTaskCostTime() + "s");
		
		List<Object> allCarTaskList = HibernateOperationUtils.searchObjectFromDB(Car.class);
		addTask(allCarTaskList, TaskKey.ALL_CAR);
		
		//任务爬取完成重置任务列表，以便下次爬取任务使用
		logger.info("CarDetailCrawlerManager start");
		CarDetailCrawlerManager carDetailCrawlerManager = new CarDetailCrawlerManager(5);
		carDetailCrawlerManager.start();
		logger.info("CarDetailCrawlerManager end, Success!--------Time:--------Cost:" + carDetailCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR);
		
		logger.info("CarConfigureCrawlerManager start");
		CarConfigureCrawlerManager carConfigureCrawlerManager = new CarConfigureCrawlerManager(5);
		carConfigureCrawlerManager.start();
		logger.info("CarConfigureCrawlerManager end, Success!--------Time:--------Cost:" + carConfigureCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR);
		
		logger.info("CarPicCrawlerManager start");
		CarPicCrawlerManager carPicCrawlerManager = new CarPicCrawlerManager(40);
		carPicCrawlerManager.start();
		logger.info("CarPicCrawlerManager end, Success!--------Time:--------Cost:" + carPicCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR);
		
		logger.info("CarNewsCrawlerManager start");
		CarNewsCrawlerManager carNewsCrawlerManager = new CarNewsCrawlerManager(20);
		carNewsCrawlerManager.start();
		logger.info("CarNewsCrawlerManager end, Success!--------Time:--------Cost:" + carNewsCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.ALL_CAR);
		
		List<Object> allCarNewsTaskList = HibernateOperationUtils.searchObjectFromDB(CarNews.class);
		addTask(allCarNewsTaskList, TaskKey.ALL_CAR_NEWS);
		logger.info("CarNewsCommentCrawlerManager start");
		CarNewsCommentCrawlerManager carNewsCommentCrawlerManager = new CarNewsCommentCrawlerManager(30);
		carNewsCommentCrawlerManager.start();
		logger.info("CarNewsCommentCrawlerManager end, Success!--------Time:--------Cost:" + carNewsCommentCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.ALL_CAR_NEWS);
		
		//限速
		logger.info("CarPriceCrawlerManager start");
		CarPriceCrawlerManager carPriceCrawlerManager = new CarPriceCrawlerManager(1);
		carPriceCrawlerManager.start();
		logger.info("CarPriceCrawlerManager end, Success!--------Time:--------Cost:" + carPriceCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR);
		
		//添加TaskKey.CAR_QUESTION 增量任务
		logger.info("CarQuestionCrawlerManager start");
		CarQuestionCrawlerManager carQuestionCrawlerManager = new CarQuestionCrawlerManager(30);
		carQuestionCrawlerManager.start();
		logger.info("CarQuestionCrawlerManager end, Success!--------Time:--------Cost:" + carQuestionCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.ALL_CAR);
		
		List<Object> allCarQuestionTaskList = HibernateOperationUtils.searchObjectFromDB(CarQuestion.class);
		addTask(allCarQuestionTaskList, TaskKey.ALL_CAR_QUESTION);
		
		logger.info("CarQuestionDetailCrawlerManager start");
		CarQuestionDetailCrawlerManager carQuestionDetailCrawlerManager = new CarQuestionDetailCrawlerManager(30);
		carQuestionDetailCrawlerManager.start();
		logger.info("CarQuestionDetailCrawlerManager end, Success!--------Time:--------Cost:" + carQuestionDetailCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.ALL_CAR_QUESTION);
		
		logger.info("CarClubCrawlerManager start");
		CarClubCrawlerManager manager = new CarClubCrawlerManager(1);
		manager.start();
		logger.info("CarClubCrawlerManager end, Success!--------Time:--------Cost:" + manager.getTaskCostTime() + "s");
		
		//添加CAR_PRAISE_DETAIL 增量任务 ！限速！
		logger.info("CarPraiseCrawlerManager start");
		CarPraiseCrawlerManager carPraiseCrawlerManager = new CarPraiseCrawlerManager(1);
		carPraiseCrawlerManager.start();
		logger.info("CarPraiseCrawlerManager end, Success!--------Time:--------Cost:" + carPraiseCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR);
		
		//添加user 增量任务
		logger.info("CarPraiseDetailCrawlerManager start");
		CarPraiseDetailCrawlerManager carPraiseDetailCrawlerManager = new CarPraiseDetailCrawlerManager(1);
		carPraiseDetailCrawlerManager.start();
		logger.info("CarPraiseDetailCrawlerManager end, Success!--------Time:--------Cost:" + carPraiseDetailCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.CAR_PRAISE_DETAIL);
		
		logger.info("UserInfoCrawlerManager start");
		UserInfoCrawlerManager userInfoCrawlerManager = new UserInfoCrawlerManager(20);
		userInfoCrawlerManager.start();
		logger.info("UserInfoCrawlerManager end, Success!--------Time:--------Cost:" + userInfoCrawlerManager.getTaskCostTime() + "s");
		initTask(TaskKey.USER_INFO);
		
		logger.info("=============DONE=============Cost:" + getTaskCostTime() + "s");
	}
	
}

