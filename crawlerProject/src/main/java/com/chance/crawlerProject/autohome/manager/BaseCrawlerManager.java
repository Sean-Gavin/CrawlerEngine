package com.chance.crawlerProject.autohome.manager;

import java.util.Date;
/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 14, 2017 8:39:28 PM
 * @version 1.0
 * @param <T>
 * 
 */
public class BaseCrawlerManager {
	
	protected int THREAD_NUM;
	private long startTime;
	private long endTime;
	
	public BaseCrawlerManager(int threadNm) {
		
		this.startTime = System.currentTimeMillis();
		this.THREAD_NUM = threadNm;
		
	}	
	
	public long getTaskCostTime() {
		this.endTime = System.currentTimeMillis();
		return (this.endTime - this.startTime)/1000;
	}
	
	public int getThreadNum() {
		return this.THREAD_NUM;
	}
	
}

