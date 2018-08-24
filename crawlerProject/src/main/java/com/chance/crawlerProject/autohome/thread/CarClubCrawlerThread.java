package com.chance.crawlerProject.autohome.thread;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarPost;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarClubCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 反爬
 * 
 * @author Sean
 * @date 创建时间：Dec 29, 2017 2:39:48 PM
 * @version 1.0
 * 
 */
public class CarClubCrawlerThread extends BaseCrawlerThread<Object>{

	private static Logger logger = LoggerFactory.getLogger(CarClubCrawlerThread.class);
	
	private static String BASE_URL = "https://club.autohome.com.cn/bbs/forum-c-";
	private static String HOST_URL = "https://club.autohome.com.cn";
	private static String TOPIC_REQUEST_URL = "https://clubajax.autohome.com.cn/topic/rv?fun=jsonprv&callback=jsonprv";
	
	private static String NEXT_PAGE_XPATH = "//a[@class='afpage']";
	private static String ALL_PAGE_NUM_XPATH = "//div[@id='subcontent']/div[@class='pagearea']/span";
	
	private static String OFFICAL_POST_LIST_XPATH = "//div[@class='area']/dl[@class='list_dl']";
	
	private static String COMMON_POST_LIST_XPATH = "//div[@id='subcontent']/dl[@class='list_dl']";
	
	private static String POST_TITLE_XPATH = "//a[@class='a_topic']";
	private static String POST_TYPE_XPATH = "//dd[1]/a";
	private static String POST_DATE_XPATH = "//dd[1]/span";
	private static String POST_ID_PATH = "//dd[@class='cli_dd']";
	private static String POST_LAST_REPLY_PATH = "//dd[3]/span";
	
	
	public CarClubCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarClubCrawlerManager.getTaskSign();
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
			Car taskInfo = (Car) t;
			
			String url = BASE_URL + taskInfo.getAutohomeId() + "-1.html";
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA(), null, true);
			String pageNumStr = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, ALL_PAGE_NUM_XPATH);
			int pageNum = StringTools.parseIntFromStr(StringTools.matcherAllStrByRegular(pageNumStr, "\\d+"));
			
			for (int i = 1 ; i <= pageNum ; i ++) {
				
				url = BASE_URL + taskInfo.getAutohomeId() + "-" + i + ".html";
				
				String testUrl = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEXT_PAGE_XPATH, "href");
				//校验2次
				if (StringTools.isBlank(testUrl)) {
					Thread.sleep(300);
					htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA(), null, true);
					testUrl = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, NEXT_PAGE_XPATH, "href");
				}
				
				List<TagNode> nodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, OFFICAL_POST_LIST_XPATH);
				if (nodeList != null && nodeList.size() > 0) {
					crawlerDetail(nodeList, taskInfo.getId(), true);
				}
				List<TagNode> commonList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, COMMON_POST_LIST_XPATH);
				if (commonList != null && commonList.size() > 0) {
					crawlerDetail(commonList, taskInfo.getId(), false);
				}
				
				htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA(), null, true);
				
			}
		} catch (Exception e) {
			logger.info("Catch exception" + e);
			e.printStackTrace();
		}
	}

	private void crawlerDetail(List<TagNode> nodeList, int id, boolean isOffical) {
		for (TagNode node : nodeList) {
			
			String title = HtmlCleanerUtils.analyseNodeByXPath(node, POST_TITLE_XPATH);
			String postUrl = HOST_URL + HtmlCleanerUtils.analyseNodeByXPath(node, POST_TITLE_XPATH, "href");
			
			String userIdStr = HtmlCleanerUtils.analyseNodeByXPath(node, POST_TYPE_XPATH, "href");
			int userId = StringTools.parseIntFromStr(StringTools.matcherAllStrByRegular(userIdStr, "\\d+"));
			
			String timeStr = HtmlCleanerUtils.analyseNodeByXPath(node, POST_DATE_XPATH, false);
			Date time = DateTools.FormatDate(timeStr, "yyyy-MM-dd");
			
			String subject = "官方活动";
			if (!isOffical) {
				String signId = node.getAttributeByName("lang");
				TagNode iconNode = HtmlCleanerUtils.getTagNodeByXPath(node, "//dt[1]/span");
				if (null != iconNode && null != iconNode.getAttributeByName("class")
						&& iconNode.getAttributeByName("class").contains("icon_")) {
					//板内置顶
					subject = "版内置顶";
				} else if (signId.contains("|0|18|0|")) {
					subject = "问题";
				} else if (signId.contains("|3|0|1|")) {
					if (signId.contains("False|1")) {
						subject = "钻石";
					} else {
						subject = "精";
					}
				} else if (signId.contains("|0|0|1|")) {
					subject = "图";
				} else if (signId.contains("|0|0|0|")) {
					subject = "空";
				} else {
					subject = "空";
				}
			}
			
			String postId = HtmlCleanerUtils.analyseNodeByXPath(node, POST_ID_PATH, "lang");
			Date now = new Date();
			StringBuffer requestUrlSB = new StringBuffer();
			requestUrlSB.append(TOPIC_REQUEST_URL).append("&ids=")
				.append(postId).append("&r=").append(URLEncoder.encode(now.toString()));
			String result = HtmlCleanerUtils.getHtmlByUrl(requestUrlSB.toString(), null, null, false);
			result = result.replaceAll("jsonprv\\(", "").replaceAll("\\)", "");
			JSONArray resultObjArr = JSON.parseArray(result);
			JSONObject obj = resultObjArr.getJSONObject(0);
			int clickNum = obj.getIntValue("replys");
			int viewNum = obj.getIntValue("views");
			String lastReply = HtmlCleanerUtils.analyseNodeByXPath(node, POST_LAST_REPLY_PATH, false);
			Date lastReplyDate = DateTools.FormatDate(lastReply, "yyyy-MM-dd HH:mm");
			Timestamp lastReplyTime = DateTools.getTimeStamp(lastReplyDate);
			if (null != lastReplyTime) {
				Long timeLength = (System.currentTimeMillis() - lastReplyTime.getTime())/1000;
				if (timeLength < 6 * 30 * 24 * 60 * 60) {
					
					CarPost carPost = new CarPost(id, postUrl, userId, title, time, subject, clickNum, viewNum, lastReplyTime);
					List<Object> searchResult = HibernateOperationUtils.searchObjectFromDBByCondition(CarPost.class, "where url = '"+ postUrl +"'");
					if (null == searchResult || searchResult.size() == 0) {
						HibernateOperationUtils.saveObejct(carPost);
					}
				}
			}
		}
	}
}

