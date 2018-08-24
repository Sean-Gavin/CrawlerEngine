package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.CarNews;
import com.chance.crawlerProject.autohome.thread.CarNewsCommentCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 25, 2017 10:50:04 AM
 * @version 1.0
 * 
 */
public class CarNewsCommentCrawlerManager extends BaseCrawlerManager{

	private static Logger logger = LoggerFactory.getLogger(CarNewsCommentCrawlerManager.class);
	
	private static List<Object> taskList  = new ArrayList<Object>();
	
	public CarNewsCommentCrawlerManager(int threadNm) {
		super(threadNm);
	}
	
	public static void main(String[] args) {
		logger.info("CarNewsCommentCrawlerManager start");
		CarNewsCommentCrawlerManager manager = new CarNewsCommentCrawlerManager(1);
		taskList = HibernateOperationUtils.searchObjectFromDB(CarNews.class);
		manager.start();
		logger.info("CarNewsCommentCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			CarNewsCommentCrawlerThread taskCrawler = new CarNewsCommentCrawlerThread(i, countDownLatch);
			Thread crawlerThread = new Thread(taskCrawler);
			crawlerThread.start();
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

