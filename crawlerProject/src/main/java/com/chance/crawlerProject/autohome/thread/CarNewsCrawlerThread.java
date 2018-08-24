package com.chance.crawlerProject.autohome.thread;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarNews;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarNewsCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 任务：爬取文章/新闻信息
 * 爬虫策略：正常爬取信息
 * 增量策略：把所有汽车信息作为任务爬取，由于新闻默认时间排序，
 * 			所以判断每条新闻页的第一条信息是否在已经爬取的库中。如果不存在继续寻找下一条，直到找到数据库中存在的新闻。
 * 
 * @author Sean
 * @date 创建时间：Dec 21, 2017 6:25:10 PM
 * @version 1.0
 * 
 */
public class CarNewsCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarNewsCrawlerThread.class);
	
	private static String BASE_URL = "https://www.autohome.com.cn/";
	
	private static String NEWS_LIST_XPATH = "//*[@class='cont-info']/ul/li";
	private static String NEWS_TITLE_XPATH = "/h3/a";
	private static String NEWS_URL_XPATH = "/h3/a";
	private static String NEWS_REPORTTIME_XPATH = "/p[@class='name-tx']/span[2]";
	private static String NEWS_VIEWNUM_XPATH = "/p[@class='name-tx']/span[3]";
	private static String NEWS_COMMENTNUM_XPATH = "/p[@class='name-tx']/span[4]";
	private static String NEWS_TAGINFOS_XPATH = "/p[@class='car']/a";
	private static String NEWS_NEXT_PAGE_XPATH = "//a[@class='page-item-next']";
	
	private static Set<Integer> arcticleIdSet = new HashSet<Integer>();
	
	public CarNewsCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarNewsCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.ALL_CAR);
			
			if (null == taskInfo) {
				
				countDownLatch.countDown();
				return;
			}
			
			crawler(taskInfo);
		}	
	}

	@Override
	public void crawler(Object t) {
		try {	
			Car carInfo = (Car) t;
			
			String url = BASE_URL + carInfo.getAutohomeId() + "/0/0-0-1-0/";
			boolean nextPage = true;
			while (nextPage) {
				
				TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
						RequestUtils.getHeaderWithIPAndUA(), null);
				
				List<TagNode> newsPageNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, NEWS_LIST_XPATH);
				
				for (TagNode node : newsPageNodeList) {
					
					String newsTitle = HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_TITLE_XPATH);
					String newsUrl = "https:" + HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_URL_XPATH, "href");
					String reportTime = HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_REPORTTIME_XPATH);
					String viewNumStr = HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_VIEWNUM_XPATH);
					int viewNum = StringTools.parseIntFromStr(viewNumStr);
					String commentNumStr = HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_COMMENTNUM_XPATH);
					int commentNum = StringTools.parseIntFromStr(commentNumStr);
					String tagInfos = HtmlCleanerUtils.analyseNodeByXPath(node, NEWS_TAGINFOS_XPATH);
					
					String autoArticleIdStr = StringTools.matcherStrByRegular(newsUrl, "/\\d+\\-*\\d*\\.html");
					int autoArticleId = StringTools.parseIntFromStr(StringTools.matcherStrByRegular(autoArticleIdStr, "\\d+"));
					if (!arcticleIdSet.contains(autoArticleId)) {
						arcticleIdSet.add(autoArticleId);
						
						CarNews carNews = new CarNews(carInfo.getId(), autoArticleId, newsTitle, newsUrl, reportTime, viewNum, commentNum, tagInfos);
						//全量策略
//						HibernateOperationUtils.saveObejct(carNews);
						//增量策略
						List<Object> searchResult = HibernateOperationUtils.searchObjectFromDBByCondition(CarNews.class, "where url = '"+ newsUrl +"'");
						
						if (null == searchResult || searchResult.size() == 0) {
//							logger.info("save object carNews" + newsUrl);
							HibernateOperationUtils.saveObejct(carNews);
						} else {
							//当遇到一条在数据库中的数据时，结束爬取该汽车型号新闻。
							return ;
						}
					}
				}
				
				String nextPageUrl = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEWS_NEXT_PAGE_XPATH, "href");
				
				if (!StringUtils.isBlank(nextPageUrl)) {
					
					url = "https:" + nextPageUrl;
				} else {
					
					nextPage = false;
				}
			}
		} catch (Exception e) {
			logger.info("Catch exception" + e);
			e.printStackTrace();
		}
	}
}

