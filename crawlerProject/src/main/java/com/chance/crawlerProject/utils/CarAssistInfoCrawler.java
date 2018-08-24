package com.chance.crawlerProject.utils;

import java.util.List;

import org.htmlcleaner.TagNode;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.chance.crawlerProject.autohome.bean.CarConfigItem;
import com.chance.crawlerProject.autohome.bean.CarQuestionLevel;

/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 5, 2018 3:37:51 PM
 * @version 1.0
 * 
 */
public class CarAssistInfoCrawler {

	public static void main(String[] args) {
		crawlerCarConfigItem();
	}

	
	private static void crawlerCarConfigItem() {
		
		String ALL_CONFIG_XPATH = "//script[@type='text/javascript']";
		String url = "https://car.autohome.com.cn/config/series/3103.html";
		String CONFIG_KEYLINK_REGEX = "var keyLink =";
		
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, RequestUtils.getHeaderWithIPAndUA(), null, false);
		
		List<TagNode> resultList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, ALL_CONFIG_XPATH);
		String allConfig = resultList.get(8).getText().toString();
		
		String key = StringTools.matcherAllStrByRegular(allConfig, CONFIG_KEYLINK_REGEX + ".*");
		key = key.replaceAll(CONFIG_KEYLINK_REGEX, "").replace(";", "");
		JSONObject keyOBJ = JSON.parseObject(key);
		
		JSONArray array = keyOBJ.getJSONObject("result").getJSONArray("items");
		
		for (int i = 0 ; i < array.size() ; i ++ ) {
			
			JSONObject itemObj = array.getJSONObject(i);
			String link = itemObj.getString("link");
//			System.out.println("name--" + itemObj.getString("name"));
//			System.out.println("name--" + itemObj.getString("name").replaceAll("<span.*?></span>", ""));
			
			
			htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(link, RequestUtils.getHeaderWithIPAndUA(), null, false);
			String name =  HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//span[@id='lblName']");
			int id = itemObj.getIntValue("id");
//			DBUtils.writeToCarConfigItems(id, name, link);
			CarConfigItem configItem = new CarConfigItem(id, name, link);
			HibernateOperationUtils.saveObejct(configItem);
		}
		System.out.println("DONE！");
		
	}
	
	
	private static void crawlerQuestionSubject() {
		String BASE_URL = "https://zhidao.autohome.com.cn";
		String XPATH = "//div[@data-type='_c2idselect']/a";
		for (int i = 1; i < 10 ; i ++) {
			String sign = "/list/" + i + "-0/s4-1.html";
			
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(BASE_URL + sign, null, null, false);
			
			
			String topLevelName = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//div[@class='choise-type-infolist']/a[@href='" +sign+"']");
			
			List<TagNode> nodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, XPATH);
			
			for (TagNode node : nodeList) {
				String childLevelStr = node.getAttributeByName("href");
				childLevelStr = StringTools.matcherAllStrByRegular(childLevelStr, "\\/\\d+\\-\\d+\\/");
				childLevelStr = StringTools.matcherAllStrByRegular(childLevelStr, "\\d+\\/");
				childLevelStr = childLevelStr.replaceAll("\\/", "");
				
				int childLevel = Integer.parseInt(childLevelStr);
				
				String childLevelName = HtmlCleanerUtils.analyseNodeByXPath(node, "span");

//				DBUtils.writeToCarQuestionSubject(i, topLevelName, childLevel, childLevelName);
				CarQuestionLevel carQuestionLevel = new CarQuestionLevel(i, topLevelName, childLevel, childLevelName);
				
				HibernateOperationUtils.saveObejct(carQuestionLevel);
			}
		}
	}
}

