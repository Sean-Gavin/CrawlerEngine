package com.chance.crawlerProject.autohome.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.thread.CarConfigureCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 5, 2018 2:42:13 PM
 * @version 1.0
 * 
 */
public class CarConfigureCrawlerManager extends BaseCrawlerManager{
	
	private static Logger logger = LoggerFactory.getLogger(CarConfigureCrawlerManager.class);
	
	public CarConfigureCrawlerManager(int threadNm) {
		super(threadNm);
	}

	private static List<Object> taskList  = new ArrayList<Object>();
	
	public static void main(String[] args) {
		logger.info("CarConfigureCrawlerManager start");
		CarConfigureCrawlerManager manager = new CarConfigureCrawlerManager(20);
		taskList = HibernateOperationUtils.searchObjectFromDB(Car.class);
		manager.start();
		logger.info("CarConfigureCrawlerManager end, Success!--------Time:"+ new Date() +"--------Cost:" + manager.getTaskCostTime() + "s");
	}
	
	public void start() {
		//主线程等待
		CountDownLatch countDownLatch = new CountDownLatch(THREAD_NUM);
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			CarConfigureCrawlerThread taskCrawler = new CarConfigureCrawlerThread(i, countDownLatch);
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

