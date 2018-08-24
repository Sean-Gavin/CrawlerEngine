package com.chance.crawlerProject.autohome.thread;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.UserCar;
import com.chance.crawlerProject.autohome.bean.UserInfo;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.UserInfoCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.DateTools;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 任务：爬取用户信息
 * 爬虫策略：正常爬取信息+模拟登录（保存cookie）
 * 增量策略：把口碑信息作为任务爬取。
 * 
 * @author Sean
 * @date 创建时间：Jan 10, 2018 9:47:19 AM
 * @version 1.0
 * 
 */
public class UserInfoCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(UserInfoCrawlerThread.class);

	private static String USER_NAME_XPATH = "//h1[@class='user-name']/b";
	private static String FOLLOWING_NUM_XPATH = "//div[@class='user-lv']/a[@class='state-mes'][1]/span";
	private static String FOLLOWER_NUM_XPATH = "//div[@class='user-lv']/a[@class='state-mes'][2]/span";
	private static String CITY_XPATH = "//div[@class='user-lv']/a[@class='state-pos']";
	private static String USER_DETAIL_INFO_LIST_XPATH = "//div[@id='divuserinfo']/p";
	
	private static String USER_CAR_REQUEST_BASE_URL = "https://i.autohome.com.cn/ajax/home/OtherHomeAppsData?appname=Car&r=0.1042290475965868";
	private static String USER_ACHIEVE_IFNO_REQUEST_BASE_URL = "https://i.autohome.com.cn/ajax/home/GetUserInfo?r=0.17298081138793964";
	private static String USER_PRICE_REQUEST_BASE_URL = "https://jiage.autohome.com.cn/web/price/otherlist?memberid=";
	
	private static String PRICE_USER_CAR_LIST_XPATH = "//div[@class='price-boxs mt10 cz-user-price']";
	private static String PRICE_CAR_NAME_AND_URL_XPATH = "/div[@class='cx-price-title mt10']/span[1]/a";
	private static String PRICE_GUIDE_PRICE_XPATH = "/div[@class='cx-price-title mt10']/span[2]";
	private static String PRICE_COST_ALL_PRICE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[@class='price-title-total']/h3/em";
	private static String PRICE_CAR_PRICE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[2]/span[2]";
	private static String PRICE_PURCHASE_TAX_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[2]/span[4]";
	private static String PRICE_COMPULSORY_INSURANCE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[2]/span[6]";
	private static String PRICE_VAVUT_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[3]/span[2]";
	private static String PRICE_COMMERCIAL_INSURANCE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[3]/span[4]";
	private static String PRICE_LICENSE_FEE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[3]/span[6]";
	private static String PRICE_PAY_TYPE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/div[1]/li[1]/span[2]";
	private static String PRICE_DOWN_PAY_PRICE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/div[1]/li[1]/span[4]";
	private static String PRICE_REPAYMENT_NUM_XPATH = "/div[@class='cz-price-title color6']/ul[1]/div[1]/li[1]/span[6]";
	private static String PRICE_SERVICE_FEE_XPATH = "/div[@class='cz-price-title color6']/ul[1]/div[1]/li[2]/span[2]";
	private static String PRICE_PROMOTION_GOODS_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[4]/span[2]";
	private static String PRICE_BUY_CAR_TIME_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[5]/span[2]";
	private static String PRICE_CITY_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[5]/span[4]";
	private static String PRICE_MANUFACTOR_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[6]/span";
	private static String PRICE_SALE_TIPS_XPATH = "/div[@class='cz-price-title color6']/ul[1]/li[7]/span[2]";
	
	public UserInfoCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = UserInfoCrawlerManager.getTaskSign();
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.USER_INFO);
			
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
			UserInfo taskInfo = (UserInfo) t;
			
			crawlerUserInfo(taskInfo);
			
			crawlerUserCarInfo(taskInfo);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	
	private void crawlerUserInfo(UserInfo taskInfo) throws Exception{
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(taskInfo.getUrl(), RequestUtils.getHeaderWithIPAndUA());
		
		String name = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, USER_NAME_XPATH);
		int followingNum = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, FOLLOWING_NUM_XPATH));
		int followerNum = StringTools.parseIntFromStr(HtmlCleanerUtils.analyseNodeByXPath(htmlNode, FOLLOWER_NUM_XPATH));
		String city = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, CITY_XPATH);
		
		StringBuffer tempSB = new StringBuffer();
		
		//https://i.autohome.com.cn/ajax/home/GetUserInfo?r=0.17298081138793964&userid=6844490&_=1515555304241
		tempSB.append(USER_ACHIEVE_IFNO_REQUEST_BASE_URL).append("&userid=").append(taskInfo.getAutoUserId())
					.append("&_=").append(System.currentTimeMillis()).toString();
		String result = HtmlCleanerUtils.getHtmlByUrl(tempSB.toString(), RequestUtils.getHeaderWithReferer(taskInfo.getUrl()), null, false);
		JSONObject resultObj = JSON.parseObject(result);
		int helpScore = resultObj.getIntValue("HelpScore");
		int bestPostNum = resultObj.getIntValue("JHTopicCount");
		int postNum = resultObj.getIntValue("TopicCount");
		
		//https://i.autohome.com.cn/ajax/home/OtherHomeAppsData?appname=Car&r=0.1042290475965868&TuserId=6844490
		String ownCarList = "";
		tempSB = new StringBuffer();
		tempSB.append(USER_CAR_REQUEST_BASE_URL).append("&TuserId=").append(taskInfo.getAutoUserId()).toString();
		result = HtmlCleanerUtils.getHtmlByUrl(tempSB.toString(), RequestUtils.getHeaderWithReferer(taskInfo.getUrl()), null, false);
		JSONArray resultArray = JSON.parseObject(result).getJSONArray("ConcernInfoList");
		for (int i = 0 ; i < resultArray.size() ; i ++) {
			JSONObject tempObj = resultArray.getJSONObject(i);
			String carName = tempObj.getString("BrandName") + tempObj.getString("SeriesName") + tempObj.getString("SpecName");
			ownCarList = ownCarList + carName + ",";
		}
		if (!StringTools.isBlank(ownCarList)) {
			ownCarList = ownCarList.substring(0, ownCarList.length() - 1);
		}

		String sex = "";
		Timestamp birthday = null;
		String phoneAuth = "";
		String userDetailInfoUrl = taskInfo.getUrl() + "/info";
		htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(userDetailInfoUrl, RequestUtils.getHeaderWithCookie());
		List<TagNode> infoList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, USER_DETAIL_INFO_LIST_XPATH);
		for (TagNode node : infoList) {
			TagNode spanNode = HtmlCleanerUtils.getTagNodeByXPath(node, "/span");
			node.removeChild(spanNode);
			if (null == spanNode) {
				
			} else if (spanNode.getText().toString().contains("性别")) {
				
				sex = StringTools.removeUselessChar(node.getText().toString());
			} else if (spanNode.getText().toString().contains("生日")) {
				
				String tempStr = StringTools.removeUselessChar(node.getText().toString());
				birthday = DateTools.FormatTimeStamp(tempStr, "yyyy-MM-dd");
			} else if (spanNode.getText().toString().contains("手机认证")) {
				
				phoneAuth = StringTools.removeUselessChar(node.getText().toString());
			}
		}
		
		UserInfo userInfo = new UserInfo(taskInfo.getAutoUserId(), taskInfo.getUrl(), name, sex, birthday,
				phoneAuth, city, followingNum, followerNum, helpScore, bestPostNum, postNum, ownCarList);
		
		userInfo.setId(taskInfo.getId());
		
		HibernateOperationUtils.updateObejct(userInfo);
//		DBUtils.updateUserInfoBuId(taskInfo.getId(), name, sex, birthday, phoneAuth, city, followingNum, followerNum, helpScore, bestPostNum, postNum, ownCarList);
	}
	
	private void crawlerUserCarInfo(UserInfo taskInfo) {

		String buyCarUrl = USER_PRICE_REQUEST_BASE_URL + taskInfo.getAutoUserId();
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(buyCarUrl, RequestUtils.getHeaderWithIPAndUA());
		
		List<TagNode> resultList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PRICE_USER_CAR_LIST_XPATH);
		for (TagNode carNode : resultList) {
			
			String carName = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_CAR_NAME_AND_URL_XPATH);
			String carUrl = "https:" + HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_CAR_NAME_AND_URL_XPATH, "href");
			String guidePrice = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_GUIDE_PRICE_XPATH) + "万";
			String costAllPrice = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_COST_ALL_PRICE_XPATH) + "万";
			//裸车价格
			String carPrice = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_CAR_PRICE_XPATH);
			//购置税
			String purchaseTax = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_PURCHASE_TAX_XPATH).replaceAll(",", "");
			//交强险
			String compulsoryInsurance = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_COMPULSORY_INSURANCE_XPATH).replaceAll(",", "");
			//车船使用税
			String VAVUT = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_VAVUT_XPATH).replaceAll(",", "");
			//商业险
			String commercialInsurance = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_COMMERCIAL_INSURANCE_XPATH).replaceAll(",", "");
			//上牌费用
			String licenseFee = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_LICENSE_FEE_XPATH).replaceAll(",", "");
			//付款方式
			String payType = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_PAY_TYPE_XPATH).replaceAll(",", "");
			//首付款
			String downPaymentPrice = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_DOWN_PAY_PRICE_XPATH).replaceAll(",", "");
			//分期数
			String repaymentNum = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_REPAYMENT_NUM_XPATH).replaceAll(",", "");			
			//手续费
			String serviceFee = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_SERVICE_FEE_XPATH).replaceAll(",", "");		
			//促销套餐
			String promotionGoods = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_PROMOTION_GOODS_XPATH).replaceAll(",", "");
			//购车时间
			String buyCarStr = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_BUY_CAR_TIME_XPATH).replaceAll(",", "");
			Timestamp buyCarTime = DateTools.FormatTimeStamp(buyCarStr, "yyyy年MM月dd日");
			
			//购车地点
			String city = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_CITY_XPATH).replaceAll(",", "");			
			//购买商家
			String manufactorInfo = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_MANUFACTOR_XPATH).replaceAll(",", "");			
			//砍价技巧
			String saleTips = HtmlCleanerUtils.analyseNodeByXPath(carNode, PRICE_SALE_TIPS_XPATH).replaceAll(",", "");
			
			UserCar userCarInfo = new UserCar(taskInfo.getId(), carUrl, carName, guidePrice, costAllPrice, carPrice, 
					purchaseTax, compulsoryInsurance, VAVUT, commercialInsurance, licenseFee, payType, downPaymentPrice, 
					repaymentNum, serviceFee, promotionGoods, buyCarTime, city, manufactorInfo, saleTips);
			
			HibernateOperationUtils.saveObejct(userCarInfo);
			
//			DBUtils.writeToUserCarInfo(taskInfo.getId(), carUrl, carName, guidePrice, costAllPrice, carPrice, purchaseTax, compulsoryInsurance, VAVUT, commercialInsurance, 
//					licenseFee, payType, downPaymentPrice, repaymentNum, serviceFee, promotionGoods, buyCarTime, city, manufactorInfo, saleTips);
		}
	}
}

