package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.thread.CarDetailCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 15, 2017 2:56:37 PM
 * @version 1.0
 * 
 */
public class CarDetailCrawlerManager extends BaseCrawlerManager{
	
	private static Logger logger = LoggerFactory.getLogger(CarDetailCrawlerManager.class);
	private static List<Object> taskList  = new ArrayList<Object>();
	
	public CarDetailCrawlerManager(int threadNm) {
		super(threadNm);
	}
	
	public static void main(String[] args) {
		logger.info("CarDetailCrawlerManager start");
		CarDetailCrawlerManager manager = new CarDetailCrawlerManager(20);
		taskList = HibernateOperationUtils.searchObjectFromDB(Car.class);
		manager.start();
		logger.info("CarDetailCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			CarDetailCrawlerThread taskCrawler = new CarDetailCrawlerThread(i, countDownLatch);
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

