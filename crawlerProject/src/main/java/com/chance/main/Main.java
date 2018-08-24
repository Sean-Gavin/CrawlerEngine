package com.chance.main;

import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.RequestUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 11, 2018 10:55:43 AM
 * @version 1.0
 * 
 */
public class Main {

	private static Logger logger = LoggerFactory.getLogger(Main.class);
	
	private static int THREAD_NUMBER = 10;
	
	public static void main(String[] args) {
		
		ScheduledExecutorService scheduler  = Executors.newScheduledThreadPool(THREAD_NUMBER);
		logger.info("Start the scheduler task,The thread number is :" + THREAD_NUMBER);
//    	long oneDay = 24 * 60 * 60;
//    	
//    	long initDelay  = (DateTools.getTimeMillis("00:00:01") - System.currentTimeMillis())/1000;  
//    	initDelay = initDelay > 0 ? initDelay : oneDay + initDelay;
//    	//每天跑一次
//		scheduler.scheduleAtFixedRate(new AutohomeIncrementCrawlTask(), initDelay, oneDay, TimeUnit.SECONDS);

		scheduler.schedule(new AutohomeIncrementCrawlTask(), 10, TimeUnit.SECONDS);
		scheduler.shutdown();
	}
	
}

