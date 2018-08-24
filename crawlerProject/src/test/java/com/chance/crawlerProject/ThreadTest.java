package com.chance.crawlerProject;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlcleaner.TagNode;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;


/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 11, 2017 7:02:26 PM
 * @version 1.0
 * 
 */
public class ThreadTest implements Runnable{
	//1-0/s4-1.html
	private static String BASE_URL = "https://club.autohome.com.cn/bbs/forum-c-";
	
	
	public ThreadTest() {
		System.out.println("start");
	}

	public void run() {
	
		while (true) {
			Object taskInfo = ManagerTest.getTask();
			if (null == taskInfo) {
				return;
			}
			Car carInfo = (Car) taskInfo;
			String url = BASE_URL + carInfo.getAutohomeId() + "-1.html";
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
					RequestUtils.getHeaderWithIPAndUA(), null);
			System.out.println(htmlNode);
		}
	}
	
}

