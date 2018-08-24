package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.thread.CarNewsCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 22, 2017 5:49:46 PM
 * @version 1.0
 * 
 */
public class CarNewsCrawlerManager  extends BaseCrawlerManager{

	private static Logger logger = LoggerFactory.getLogger(CarNewsCrawlerManager.class);
	
	private static List<Object> taskList  = new ArrayList<Object>();
	
	public CarNewsCrawlerManager(int threadNm) {
		super(threadNm);
	}
	
	public static void main(String[] args) {
		logger.info("CarNewsCrawlerManager start");
		CarNewsCrawlerManager manager = new CarNewsCrawlerManager(1);
		taskList = HibernateOperationUtils.searchObjectFromDB(Car.class);
		manager.start();
		logger.info("CarNewsCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			CarNewsCrawlerThread taskCrawler = new CarNewsCrawlerThread(i, countDownLatch);
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

