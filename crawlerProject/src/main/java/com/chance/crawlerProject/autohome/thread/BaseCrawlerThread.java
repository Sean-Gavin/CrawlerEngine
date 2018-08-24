package com.chance.crawlerProject.autohome.thread;

import java.util.concurrent.CountDownLatch;


/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 18, 2017 6:37:32 PM
 * @version 1.0
 * @param <T>
 * 
 */
public abstract class BaseCrawlerThread<T> implements Runnable{

	protected int num;
	protected CountDownLatch countDownLatch;
	
	public BaseCrawlerThread(int num, CountDownLatch countDownLatch) {
		this.num = num;
		this.countDownLatch = countDownLatch;
	}
	
	public abstract void crawler(T t);
	
}

