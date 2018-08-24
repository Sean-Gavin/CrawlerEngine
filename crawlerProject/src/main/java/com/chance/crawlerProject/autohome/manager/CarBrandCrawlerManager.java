package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.thread.CarBrandCrawlerThread;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 14, 2017 8:27:34 PM
 * @version 1.0
 * 
 */
public class CarBrandCrawlerManager extends BaseCrawlerManager{
	
	private static Logger logger = LoggerFactory.getLogger(CarBrandCrawlerManager.class);
	private static List<String> taskList  = new ArrayList<String>();
	
	public CarBrandCrawlerManager(int threadNm) {
		super(threadNm);
	}
	
	public static void main(String[] args) {
		logger.info("CarBrandCrawlerManager start");
		CarBrandCrawlerManager manager = new CarBrandCrawlerManager(1);
		manager.start();
		logger.info("CarBrandCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		initTaskSign();
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			CarBrandCrawlerThread taskCrawler = new CarBrandCrawlerThread(i, countDownLatch);
			Thread crawlerThread = new Thread(taskCrawler);
			crawlerThread.start();
		}
		try {
			countDownLatch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private static void initTaskSign() {
		for (char start = 'A' ; start <= 'Z' ;start ++) {
			taskList.add(String.valueOf(start));
		}
	}

	public static synchronized String getTaskSign() {
		
		if (taskList.size() > 0) {
			String task = taskList.get(0);
			taskList.remove(0);
			return task;
		} else {
			return null;
		}
	}

}

