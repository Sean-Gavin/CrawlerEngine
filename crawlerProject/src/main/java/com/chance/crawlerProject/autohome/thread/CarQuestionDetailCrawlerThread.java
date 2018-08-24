package com.chance.crawlerProject.autohome.thread;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.CarQuestion;
import com.chance.crawlerProject.autohome.bean.QuestionResponse;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarQuestionDetailCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 切换header
 * 
 * 
 * @author Sean
 * @date 创建时间：Dec 28, 2017 7:01:52 PM
 * @version 1.0
 * 
 */
public class CarQuestionDetailCrawlerThread extends BaseCrawlerThread<Object>{

	private static Logger logger = LoggerFactory.getLogger(CarQuestionDetailCrawlerThread.class);
	
	private static String HOST_XPATH = "https://club.autohome.com.cn/bbs/";
	
	private static String CONTENT_LIST_XPATH = "//div[@class='clearfix contstxt outer-section']";
	
	private static String QUESTION_TITLE_XPATH = "//div[@class='qa-maxtitle']";
	private static String QUESTION_CHECK_NUM_XPATH = "//font[@id='x-views']";
	
	private static String CONTENT_CONTENT_XPATH = "//div[@class='w740']";
	private static String CONTENT_CONTENT2_XPATH = "//div[@class='w740']/div[@class='yy_reply_cont']";
	private static String CONTENT_TIME_XPATH = "//span[@xname='date']";
	private static String CONTENT_USER_XPATH = "//a[@xname='uname']";
	
	private static String RESPONSE_FLOOR_XPATH = "//button[@class='rightbutlz']";
	private static String RESPONSE_REPLY_FLOOR_XPATH = "//div[@class='relyhfcon']/p/a[2]";
//	private static String RESPONSE_AGREE_NUM_XPATH = "//b[@id='ubtn-count955430717']";
	
	
	public CarQuestionDetailCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarQuestionDetailCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.ALL_CAR_QUESTION);
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
			CarQuestion taskInfo = (CarQuestion) t;
			
			String url = taskInfo.getUrl();
			int page = 1;
			while (!HOST_XPATH.equals(url)) {
				TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(taskInfo.getUrl(), RequestUtils.getHeaderWithIPAndUA());
				
				String check = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//*[@id='consnav']/span[4]");
				//如果有问题，重复请求一次。
				if ("唉，等车的日子真难熬啊".equals(check)) {
					htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(taskInfo.getUrl(), RequestUtils.getHeaderWithIPAndUA());
				}
				List<TagNode> nodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, CONTENT_LIST_XPATH);
				int i = 0;
				for (TagNode node : nodeList) {
					
					String title = HtmlCleanerUtils.analyseNodeByXPath(node, QUESTION_TITLE_XPATH);
					
					String time = HtmlCleanerUtils.analyseNodeByXPath(node, CONTENT_TIME_XPATH, false);
					
					String content = HtmlCleanerUtils.analyseNodeByXPath(node, CONTENT_CONTENT2_XPATH);
					
					if (StringTools.isBlank(content)) {
						content = HtmlCleanerUtils.analyseNodeByXPath(node, CONTENT_CONTENT_XPATH);
					}
//					String userIdStr = HtmlCleanerUtils.analyseNodeByXPath(node, CONTENT_USER_XPATH, "href");
//					userIdStr = StringTools.matcherAllStrByRegular(userIdStr, "\\d+");
					
					String userIdStr = node.getAttributeByName("uid");
					
					Timestamp timestamp = DateTools.getTimeStamp(DateTools.FormatDate(time, null));
					
					
					if (StringTools.isBlank(title)) {
						i ++;
						//回复
						String floorStr = HtmlCleanerUtils.analyseNodeByXPath(node, RESPONSE_FLOOR_XPATH, "rel");
						floorStr = StringTools.matcherAllStrByRegular(floorStr, "\\d+");
						int floor = StringTools.parseIntFromStr(floorStr);
						
						String replyFloorStr = HtmlCleanerUtils.analyseNodeByXPath(node, RESPONSE_REPLY_FLOOR_XPATH, "href");
						replyFloorStr = StringTools.matcherAllStrByRegular(StringTools.matcherAllStrByRegular(replyFloorStr, "html#\\d+"), "\\d+");
						int replyFloor = StringTools.parseIntFromStr(replyFloorStr);
						//首页第一个评论即为被采纳的
						int isAccept = 0;
						if (page == 1 && i == 1) {
							isAccept = 1;
						}
						
						List<Object> searchObject = HibernateOperationUtils.searchObjectFromDBByCondition(QuestionResponse.class, 
								"where questionId = "+ taskInfo.getId() +" and floor = "+ floor +"");
						if (null == searchObject || searchObject.size() == 0) {
							QuestionResponse questResponse = new QuestionResponse(taskInfo.getId(), url, userIdStr, replyFloor, floor,
									isAccept, timestamp, content);
							HibernateOperationUtils.saveObejct(questResponse);
						}
					} else {
						//问题
						String checkNumStr = HtmlCleanerUtils.analyseNodeByXPath(node, QUESTION_CHECK_NUM_XPATH);
						int checkNum = StringTools.parseIntFromStr(checkNumStr);
						CarQuestion carQuestion = new CarQuestion(taskInfo.getCarId(), url, title, content, taskInfo.getSubjectId(), timestamp, userIdStr, checkNum);
						carQuestion.setId(taskInfo.getId());
						HibernateOperationUtils.updateObejct(carQuestion);
					}
					
				}
				page ++;
				url = HOST_XPATH + HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//a[@class='afpage'][1]", "href");
			}
		} catch (Exception e) {
			logger.info("Catch exception" + e);
			e.printStackTrace();
		}
	}
}

