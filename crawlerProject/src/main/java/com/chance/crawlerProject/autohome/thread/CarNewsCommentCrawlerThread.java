package com.chance.crawlerProject.autohome.thread;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.CarNews;
import com.chance.crawlerProject.autohome.bean.NewsComment;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 
 * 评论通过分析找到一个接口，直接请求接口获取结果。
 * 
 * 根据评论数目判断是否有更新
 * @author Sean
 * @date 创建时间：Dec 20, 2017 7:58:50 PM
 * @version 1.0
 * 
 */
public class CarNewsCommentCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarNewsCommentCrawlerThread.class);
	
	private static String REQUEST_BASE_URL = "https://reply.autohome.com.cn/api/comments/show.json?count=50&appid=1&id=";
	
	public CarNewsCommentCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarNewsCommentCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.ALL_CAR_NEWS);
			if (null == taskInfo) {
				countDownLatch.countDown();
				return;
			}
			
			crawler(taskInfo);
		}	
	}

	@Override
	public void crawler(Object t) {
		try  {
			CarNews carNewsInfo = (CarNews) t;
			boolean flag = true;
			int pageIndex = 1;
			while (flag) {
				String url = REQUEST_BASE_URL + carNewsInfo.getAutoArticleId() + "&page=" + pageIndex;
				
				String result = HtmlCleanerUtils.getHtmlByUrl(url, null, null, false);
				
				JSONObject resultObj = JSON.parseObject(result);

				if (null != resultObj) {
					
					JSONArray commentsArray = resultObj.getJSONArray("commentlist");
					
					if (null == commentsArray || commentsArray.size() == 0) {
						return ;
					}
					
					for (int i = 0 ; i < commentsArray.size() ; i ++) {
						
						JSONObject comment = commentsArray.getJSONObject(i);
						
						int autoUserId = comment.getInteger("RMemberId");
						int commentFloor = comment.getInteger("RFloor");
						String commentTime = comment.getString("RReplyDate");
						int replyAutoLevelId = comment.getInteger("RTargetReplyId");
						String commentContent = comment.getString("RContent");
						int autoCommentId = comment.getInteger("ReplyId");
						String deviceType = comment.getString("SpType");
						int upNumber = comment.getInteger("RUp");
						Long time = Long.parseLong(StringTools.matcherStrByRegular(commentTime, "\\d{13}"));
						Timestamp date = DateTools.getTimeStamp(time);
						
						NewsComment newsComment = new NewsComment(carNewsInfo.getId(), autoUserId, upNumber, deviceType, commentFloor,
								autoCommentId, replyAutoLevelId, commentContent, date);
						//全量
//						HibernateOperationUtils.saveObejct(newsComment);

						List<Object> searchResult = HibernateOperationUtils.searchObjectFromDBByCondition(NewsComment.class,
								"where newsId = "+ carNewsInfo.getId() +" and commentFloor = "+ commentFloor +"");
						//增量
						if (null == searchResult || searchResult.size() == 0) {
//							logger.info("save newsComment" + carNewsInfo.getId() + "--floor--"+ commentFloor);
							HibernateOperationUtils.saveObejct(newsComment);
						} else {
							//由于新闻评论为倒叙，按楼层排列，所以顺序查找到一个已存在数据库中的楼层，即可退出该新闻的增量策略。
							return ;
						}
					}
				}
				pageIndex ++;
			}
			
		} catch (Exception e){
			logger.info("Catch exception" + e);
		} 
	}
}

