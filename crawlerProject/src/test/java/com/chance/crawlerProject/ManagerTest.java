package com.chance.crawlerProject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.thread.CarNewsCommentCrawlerThread;
import com.chance.crawlerProject.autohome.thread.CarPicCrawlerThread;
import com.chance.crawlerProject.utils.HibernateOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 14, 2017 4:24:00 PM
 * @version 1.0
 * 
 */
public class ManagerTest {

	static int THREAD_NUM = 3;
	static List<Object> taskList = new ArrayList<Object>();
	
	public static void main(String[] args) {

		taskList = HibernateOperationUtils.searchObjectFromDB(Car.class);
		
		for (int i=0; i < THREAD_NUM ; i++ ) {
			ThreadTest taskCrawler = new ThreadTest();
			Thread crawlerThread = new Thread(taskCrawler);
			crawlerThread.start();
		}
	}

	public static synchronized Object getTask() {
		if (taskList.size() > 0) {
			Object result = taskList.get(0);
			taskList.remove(0);
			return result;
		} else {
			return null;
		}
	}
}

