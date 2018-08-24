package com.chance.crawlerProject.autohome.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarConfigDetail;
import com.chance.crawlerProject.autohome.bean.CarConfigItem;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarConfigureCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 任务：汽车配置页面
 * 爬虫策略：从JS中匹配出所有的配置信息，然后再解析。
 * 增量策略：把汽车的增量信息作为任务爬取。
 * 
 * 
 * @author Sean
 * @date 创建时间：Jan 5, 2018 2:40:41 PM
 * @version 1.0
 * 
 */
public class CarConfigureCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarConfigureCrawlerThread.class);
	//地点北京市
	private static String DEALER_SEARCH_URL = "https://carif.api.autohome.com.cn/dealer/LoadDealerPrice.ashx?_callback=LoadDealerPrice&type=1&city=110100&seriesid=";
	private static String BASE_URL = "https://car.autohome.com.cn/config/series/";
	
	private static String ALL_CONFIG_XPATH = "//script[@type='text/javascript']";
	
	private static String CONFIG_CONFIG_REGEX = "var config =";
	private static String CONFIG_OPTION_REGEX = "var option =";
	private static String CONFIG_BAG_REGEX = "var bag =";
	private static String CONFIG_COLOR_REGEX = "var color =";
	private static String CONFIG_INNERCOLOR_REGEX = "var innerColor =";
	
	public CarConfigureCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarConfigureCrawlerManager.getTaskSign();
			
			//增量
			Object taskInfo = AutohomeIncrementCrawlTask.getTask(TaskKey.CAR);
			
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
			//单独爬取价格开始
			String dealerUrl = DEALER_SEARCH_URL + taskInfo.getAutohomeId();
			String dealerResult = HtmlCleanerUtils.getHtmlByUrl(dealerUrl, RequestUtils.getHeaderWithIPAndUA(), null, false);
			dealerResult = dealerResult.replaceAll("LoadDealerPrice\\(", "").replaceAll("\\)", "");
			JSONObject dealerResultObject = JSON.parseObject(dealerResult);
			JSONArray dealerItems = dealerResultObject.getJSONObject("body").getJSONArray("item");
			//单独爬取价格结束
			String pageUrl = BASE_URL + taskInfo.getAutohomeId() + ".html";
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(pageUrl, RequestUtils.getHeaderWithIPAndUA(), null, false);
			
			List<TagNode> resultList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, ALL_CONFIG_XPATH);
			String allConfig = resultList.get(8).getText().toString();
			
			Map<Integer, String> baseConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carBodyMap = new HashMap<Integer, String>();
			Map<Integer, String> carEngineMap = new HashMap<Integer, String>();
			Map<Integer, String> carGearBoxMap = new HashMap<Integer, String>();
			Map<Integer, String> carChassisSteeringMap = new HashMap<Integer, String>();
			Map<Integer, String> carBrakingMap = new HashMap<Integer, String>();
			Map<Integer, String> carSecurityMap = new HashMap<Integer, String>();
			Map<Integer, String> carControlMap = new HashMap<Integer, String>();
			Map<Integer, String> carUnBurglarsMap = new HashMap<Integer, String>();
			Map<Integer, String> carInnerConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carChairConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carMultiMediaConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carLightConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carWindowConfigMap = new HashMap<Integer, String>();
			Map<Integer, String> carAirAndFridgeMap = new HashMap<Integer, String>();
			Map<Integer, String> carOptionBagMap = new HashMap<Integer, String>();
			
			//第1部分
			String config = StringTools.matcherAllStrByRegular(allConfig, CONFIG_CONFIG_REGEX + ".*");
			config = config.replaceAll(CONFIG_CONFIG_REGEX, "").replace(";", "");
			JSONObject configOBJ = JSON.parseObject(config);
			if (null != configOBJ && null != configOBJ.getJSONObject("result")) {
				JSONArray paramTypeItemArray = configOBJ.getJSONObject("result").getJSONArray("paramtypeitems");
				
				for (int i = 0 ; i < paramTypeItemArray.size() ; i ++ ) {
					
					JSONObject paramTypeItemObj = paramTypeItemArray.getJSONObject(i);

					Map<Integer, String> map = new HashMap<Integer, String>();
					
					JSONArray paramItemArray = paramTypeItemObj.getJSONArray("paramitems");
					map = analyseInfo(pageUrl, paramItemArray);
					
					dealMapValue(map);
					
					if ("基本参数".equals(paramTypeItemObj.getString("name"))) {
						addDealerPrice(map, dealerItems);
						baseConfigMap = map;
					} else if ("车身".equals(paramTypeItemObj.getString("name"))) {
						carBodyMap = map;
					} else if ("发动机".equals(paramTypeItemObj.getString("name"))) {
						carEngineMap = map;
					} else if ("变速箱".equals(paramTypeItemObj.getString("name"))) {
						carGearBoxMap = map;
					} else if ("底盘转向".equals(paramTypeItemObj.getString("name"))) {
						carChassisSteeringMap = map;
					} else if ("车轮制动".equals(paramTypeItemObj.getString("name"))) {
						carBrakingMap = map;
					}
				}
			}
			
			
			//第2部分
			String option = StringTools.matcherAllStrByRegular(allConfig, CONFIG_OPTION_REGEX + ".*");
			option = option.replaceAll(CONFIG_OPTION_REGEX, "").replace(";", "");
			JSONObject optionOBJ = JSON.parseObject(option);
			if (null != optionOBJ && null != optionOBJ.getJSONObject("result")) {
				JSONArray configTypeItemArray = optionOBJ.getJSONObject("result").getJSONArray("configtypeitems");
				for (int i = 0 ; i < configTypeItemArray.size() ; i ++ ) {
					
					JSONObject paramTypeItemObj = configTypeItemArray.getJSONObject(i);

					Map<Integer, String> map = new HashMap<Integer, String>();
					
					JSONArray paramItemArray = paramTypeItemObj.getJSONArray("configitems");
					
					map = analyseInfo(pageUrl, paramItemArray);
					
					dealMapValue(map);

					if ("主/被动安全装备".equals(paramTypeItemObj.getString("name"))) {
						carSecurityMap = map;
					} else if ("辅助/操控配置".equals(paramTypeItemObj.getString("name"))) {
						carControlMap = map;
					} else if ("外部/防盗配置".equals(paramTypeItemObj.getString("name"))) {
						carUnBurglarsMap = map;
					} else if ("内部配置".equals(paramTypeItemObj.getString("name"))) {
						carInnerConfigMap = map;
					} else if ("座椅配置".equals(paramTypeItemObj.getString("name"))) {
						carChairConfigMap = map;
					} else if ("多媒体配置".equals(paramTypeItemObj.getString("name"))) {
						carMultiMediaConfigMap = map;
					} else if ("灯光配置".equals(paramTypeItemObj.getString("name"))) {
						carLightConfigMap = map;
					} else if ("玻璃/后视镜".equals(paramTypeItemObj.getString("name"))) {
						carWindowConfigMap = map;
					}  else if ("空调/冰箱".equals(paramTypeItemObj.getString("name"))) {
						carAirAndFridgeMap = map;
					}
				}
			}
			
			//第3部分
			String bag = StringTools.matcherAllStrByRegular(allConfig, CONFIG_BAG_REGEX + ".*");
			bag = bag.replaceAll(CONFIG_BAG_REGEX, "").replace(";", "");
			JSONObject bagOBJ = JSON.parseObject(bag);
			if (null != bagOBJ && null != bagOBJ.getJSONObject("result")) {
				JSONArray bagTypeItemArray = bagOBJ.getJSONObject("result").getJSONArray("bagtypeitems");
				
				for (int i = 0 ; i < bagTypeItemArray.size() ; i ++ ) {
					
					JSONObject paramTypeItemObj = bagTypeItemArray.getJSONObject(i);

					Map<Integer, String> map = new HashMap<Integer, String>();
					
					JSONArray paramItemArray = paramTypeItemObj.getJSONArray("bagitems");
					map = analyseInfo(pageUrl, paramItemArray);
					
					dealMapValue(map);
					
					if ("选装包".equals(paramTypeItemObj.getString("name"))) {
						carOptionBagMap = map;
					}
				}
			}
			
			//第4部分
			String color = StringTools.matcherAllStrByRegular(allConfig, CONFIG_COLOR_REGEX + ".*");
			color = color.replaceAll(CONFIG_COLOR_REGEX, "").replace(";", "");
			JSONObject colorOBJ = JSON.parseObject(color);
			if (null != colorOBJ && null != colorOBJ.getJSONObject("result")) {
				JSONArray speciItemArray = colorOBJ.getJSONObject("result").getJSONArray("specitems");
				for (int i = 0 ; i < speciItemArray.size() ; i ++ ) {
					
					JSONObject paramTypeItemObj = speciItemArray.getJSONObject(i);
					int specId = paramTypeItemObj.getIntValue("specid");
					String value = paramTypeItemObj.getString("coloritems");
					StringBuffer tempBuffer = new StringBuffer();
					if (carOptionBagMap.containsKey(specId)) {
						tempBuffer.append(carOptionBagMap.get(specId)).append(",").append("\"").append("111")
							.append("\"").append(":").append("\"").append(value).append("\"");
					} else {
						tempBuffer.append("{").append("\"").append("111")
							.append("\"").append(":").append("\"").append(value).append("\"");
					}
					carOptionBagMap.put(specId, tempBuffer.toString().replaceAll("\"", "\\\\\""));
				}
			}
			
			//第5部分
			String innercolor = StringTools.matcherAllStrByRegular(allConfig, CONFIG_INNERCOLOR_REGEX + ".*");
			innercolor = innercolor.replaceAll(CONFIG_INNERCOLOR_REGEX, "").replace(";", "");
			JSONObject innercolorOBJ = JSON.parseObject(innercolor);
			if (null != innercolorOBJ && null != innercolorOBJ.getJSONObject("result")) {
				JSONArray speciItemArray2 = innercolorOBJ.getJSONObject("result").getJSONArray("specitems");
				for (int i = 0 ; i < speciItemArray2.size() ; i ++ ) {
					
					JSONObject paramTypeItemObj = speciItemArray2.getJSONObject(i);
					int specId = paramTypeItemObj.getIntValue("specid");
					String value = paramTypeItemObj.getString("coloritems");
					StringBuffer tempBuffer = new StringBuffer();
					if (carOptionBagMap.containsKey(specId)) {
						tempBuffer.append(carOptionBagMap.get(specId)).append(",").append("\"").append("112")
							.append("\"").append(":").append("\"").append(value).append("\"");
					} else {
						tempBuffer.append("{").append("\"").append("112")
							.append("\"").append(":").append("\"").append(value).append("\"");
					}
					carOptionBagMap.put(specId, tempBuffer.toString().replaceAll("\"", "\\\\\""));
				}
			}
			
			for (int key : baseConfigMap.keySet()) {
				String tempUrl = "https://www.autohome.com.cn/spec/" + key;
				
				CarConfigDetail carConfigDetail = new CarConfigDetail(taskInfo.getId(), key, tempUrl, baseConfigMap.get(key), carBodyMap.get(key),
						carEngineMap.get(key), carGearBoxMap.get(key), carChassisSteeringMap.get(key), carBrakingMap.get(key), carSecurityMap.get(key),
						carControlMap.get(key), carUnBurglarsMap.get(key), carInnerConfigMap.get(key), carChairConfigMap.get(key),carMultiMediaConfigMap.get(key),
						carLightConfigMap.get(key), carWindowConfigMap.get(key), carAirAndFridgeMap.get(key), carOptionBagMap.get(key));
				//TODO delete 
//				DBUtils.writeToCarConfigDetail(taskInfo.getId(), key, tempUrl, baseConfigMap.get(key), 
//						carBodyMap.get(key), carEngineMap.get(key), carGearBoxMap.get(key), 
//						carChassisSteeringMap.get(key), carBrakingMap.get(key), carSecurityMap.get(key), 
//						carControlMap.get(key), carUnBurglarsMap.get(key), carInnerConfigMap.get(key), 
//						carChairConfigMap.get(key), carMultiMediaConfigMap.get(key), carLightConfigMap.get(key), 
//						carWindowConfigMap.get(key), carAirAndFridgeMap.get(key), carOptionBagMap.get(key));
				
	        	HibernateOperationUtils.saveObejct(carConfigDetail);
//	        	logger.info("seve carConfigDetail--" + carConfigDetail.getUrl());
			}
			
		} catch (Exception e) {
			logger.info("catch exception:" + e);
			e.printStackTrace();
		} 
	}

	private Map<Integer, String> analyseInfo(String url, JSONArray inputJsonArray) {
		Map<Integer, String> map = new HashMap<Integer, String>();
		
		for (int j = 0 ; j < inputJsonArray.size() ; j ++ ) {
			
			JSONObject paramItemObj = inputJsonArray.getJSONObject(j);
			int itemId = paramItemObj.getIntValue("id");
			
			if (0 == itemId) {
				String name = paramItemObj.getString("name").replaceAll("<span.*?></span>", "").replaceAll("\\s", "");
//				itemId = DBUtils.getIdByNameFromCarConfigItem(name);
				List<Object> searchObject = HibernateOperationUtils.searchObjectFromDBByCondition(CarConfigItem.class, " where name like '%"+ name +"%'");
				itemId = ((CarConfigItem)searchObject.get(0)).getAutoId();
				if (itemId == 0) {
					logger.info("ERROR:" + name);
					logger.info("ERROR:" + url);
				}
			}
			
			JSONArray valueItemArray = paramItemObj.getJSONArray("valueitems");
			for (int z = 0 ; z < valueItemArray.size() ; z ++) {
				
				JSONObject valueItemObj = valueItemArray.getJSONObject(z);
				
				int specId = valueItemObj.getInteger("specid");
				String value = valueItemObj.getString("value").replaceAll("<span.*?></span>", "").replaceAll("\\s", "");
				if (value.equals("-") || value.equals("") || "○".equals(value)) {
					value = "0";
				} else if (value.equals("●")) {
					value = "1";
				}
				StringBuffer tempBuffer = new StringBuffer();
				if (map.containsKey(specId)) {
					tempBuffer.append(map.get(specId)).append(",").append("\"").append(itemId)
						.append("\"").append(":").append("\"").append(value).append("\"");
				} else {
					tempBuffer.append("{").append("\"").append(itemId)
						.append("\"").append(":").append("\"").append(value).append("\"");
				}
				map.put(specId, tempBuffer.toString());
			}
		}

		return map;
	}

	private void dealMapValue(Map<Integer, String> map) {
		
		for(int key : map.keySet()) {
			String value = map.get(key) + "}";
//			System.out.println("key" + key);
//			System.out.println("value" + value);
			value = StringTools.removeUselessChar(value);
			map.put(key, value.replaceAll("\"", "\\\\\""));
		}
	}

	private void addDealerPrice(Map<Integer, String> map, JSONArray dealerItems) {
		
		for (int key : map.keySet()) {
			String value = map.get(key);
			value = value.substring(0, value.length() - 1);
			
			for (int i = 0; i < dealerItems.size() ; i ++) {
				if (key == dealerItems.getJSONObject(i).getIntValue("SpecId")) {
					StringBuffer tempBuffer = new StringBuffer();
					tempBuffer.append(value).append(",").append("\"").append("838")
							.append("\"").append(":").append("\"")
							.append(dealerItems.getJSONObject(i).getIntValue("Price") / 10000.00).append("\"}");

					map.put(key, tempBuffer.toString());
					break ;
				}
			}
		}
	}
	
}

