package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.CarPraiseDetail;
import com.chance.crawlerProject.autohome.thread.CarPraiseDetailCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 18, 2017 6:27:19 PM
 * @version 1.0
 * 
 */
public class CarPraiseDetailCrawlerManager extends BaseCrawlerManager{
	
	private static Logger logger = LoggerFactory.getLogger(CarPraiseDetailCrawlerManager.class);
	
	private static List<Object> taskList  = new ArrayList<Object>();

	public CarPraiseDetailCrawlerManager(int threadNm) {
		super(threadNm);
	}
	
	public static void main(String[] args) {
		logger.info("CarPraiseDetailCrawlerManager start");
		CarPraiseDetailCrawlerManager manager = new CarPraiseDetailCrawlerManager(1);
		taskList = HibernateOperationUtils.searchObjectFromDB(CarPraiseDetail.class);
		manager.start();
		logger.info("CarPraiseDetailCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		for (int i = 0; i < THREAD_NUM ; i++ ) {
			CarPraiseDetailCrawlerThread taskCrawler = new CarPraiseDetailCrawlerThread(i, countDownLatch);
			Thread taskCrawlerThread = new Thread(taskCrawler);
			taskCrawlerThread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public static synchronized Object getTaskSign() {
		
		if (taskList.size() > 0) {
			Object result = taskList.get(0);
			taskList.remove(0);
			return result;
		} else {
			return null;
		}
	}
}

