package com.chance.crawlerProject.autohome.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.htmlcleaner.TagNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.autohome.bean.Car;
import com.chance.crawlerProject.autohome.bean.CarPicture;
import com.chance.crawlerProject.autohome.bean.CarSeriesPicDetail;
import com.chance.crawlerProject.autohome.constant.TaskKey;
import com.chance.crawlerProject.autohome.manager.CarPicCrawlerManager;
import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;
import com.chance.crawlerProject.utils.HibernateOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 
 * 任务：汽车图片页面
 * 爬虫策略：正常解析
 * 增量策略：把汽车的增量信息作为任务爬取。
 * 
 * 图片的爬取策略：
 * 1、从最早爬取到的每个车的页面。进入到图片的主页面。
 * 2、然后按照车型把每个车型抓取下来。
 * 3、进入车型图片页面，按照分类继续抓取所有的页面。
 * 4、进入详细分类，然后抓取所有的车图片页面。（注意分页）
 * 5、在车图片页面抓取最终的所有信息。
 * 
 * @author Sean
 * @date 创建时间：Dec 12, 2017 7:31:35 PM
 * @version 1.0
 * 
 */
public class CarPicCrawlerThread extends BaseCrawlerThread<Object>{
	
	private static Logger logger = LoggerFactory.getLogger(CarPicCrawlerThread.class);
	
	private static String autohomeHost = "https://car.autohome.com.cn";
	private static String PIC_BASE_URL = "https://car.autohome.com.cn/pic/series/";
	private static String SERIES_GRAND_XPATH = "//*[@class='search-pic-cardl']";
	private static String SERIES_XPATH = "/ul/li";
	private static String TYPE_XPATH = "//*[@class='search-pic-sortul']/li/a";
	private static String PIC_XPATH = "//*[@class='main']/div[1]/img[1]";
	private static String PIC_HTML_XPATH = "//*[@class='uibox']/div[2]/ul/li/a";
	private static String PAGE_COUNT_XPATH = "//*[@class='page']/a";
	private static String EX_COLOR_XPATH = "//*[@class='side']/div[2]/ul/li/a[@class='red']/span[@style]";
	private static String IN_COLOR_XPATH = "//*[@class='side']/div[3]/ul/li/a[@class='red']/span[@style]";
	
	public CarPicCrawlerThread(int num, CountDownLatch countDownLatch) {
		super(num, countDownLatch);
		logger.info("Thread:" + num + "----------start");
	}

	public void run() {
		while (true) {
			//全量
//			Object taskInfo = CarPicCrawlerManager.getTaskSign();
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
			Car car = (Car) t;
			String url = PIC_BASE_URL + car.getAutohomeId() + ".html"; 
	        
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
					RequestUtils.getHeaderWithIPAndUA(), null);
			//获取所有的车系
			List<String> carSeriesPicList = new ArrayList<String>(); 
			carSeriesPicList = crawlerCarSeries(car, htmlNode);
			
			for (String carSeriesUrl :carSeriesPicList) {
				//获取所有的车系分类
				Map<String, String> carSeriesDetailPicMap = new HashMap<String, String>();
				carSeriesDetailPicMap = crawlerCarSeriesDetail(carSeriesUrl);
				
				for (String type : carSeriesDetailPicMap.keySet()) {
					
					//获取具体的图片页面地址
					List<String> carPicUrlList = new ArrayList<String>();
					carPicUrlList = crawlerCarPicUrl(carSeriesDetailPicMap.get(type));
					
					for (String carUrl : carPicUrlList) {
						TagNode carUrlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(carUrl, 
								RequestUtils.getHeaderWithIPAndUA(), null);
						String pictureUrl = "https" + HtmlCleanerUtils.analyseNodeByXPath(carUrlNode, PIC_XPATH, "src");
						
						TagNode exNode = HtmlCleanerUtils.getTagNodeByXPath(carUrlNode, EX_COLOR_XPATH);
						TagNode inNode = HtmlCleanerUtils.getTagNodeByXPath(carUrlNode, IN_COLOR_XPATH);
						//background:#A5B2A8
						String exteriorColorCode = null;
						String exteriorColor = null;
						String interiorColorCode = null;
						String interiorColor = null;
						if (null != exNode) {
							exteriorColorCode = exNode.getAttributeByName("style");
							exteriorColorCode = StringTools.matcherStrByRegular(exteriorColorCode, "#[0-9A-Z]+");
							exteriorColor = exNode.getParent().getAttributeByName("title");
						}
						if (null != inNode) {
							interiorColorCode = inNode.getAttributeByName("style");
							interiorColorCode = StringTools.matcherStrByRegular(interiorColorCode, "#[0-9A-Z]+");
							interiorColor = inNode.getParent().getAttributeByName("title");
						}
						
						int seriesId = Integer.parseInt(StringTools.matcherStrByRegular(carSeriesUrl, "series-\\w\\d+").replaceAll("series-\\w", ""));
						
						CarPicture carPicture = new CarPicture(car.getId(), seriesId, carUrl, pictureUrl, Integer.parseInt(type),
								exteriorColorCode, exteriorColor, interiorColorCode, interiorColor);
						
//						logger.info("save carPicture" + carPicture.getPicPageUrl());
						
			        	HibernateOperationUtils.saveObejct(carPicture);
					}
				}
			}
		} catch (Exception e) {
			logger.info("catch exception:" + e);
			e.printStackTrace();
		}
	}

	private static List<String> crawlerCarPicUrl(String carSeriesDetailUrl) {
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(carSeriesDetailUrl, 
				RequestUtils.getHeaderWithIPAndUA(), null);
		
		List<String> resultList = new ArrayList<String>();
		
		List<String> carSeriesDetailUrlList = new ArrayList<String>();
		carSeriesDetailUrlList.add(carSeriesDetailUrl);

		//判断是否有多页
		List<TagNode> aChildList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PAGE_COUNT_XPATH);
		
		if (aChildList.size() > 3) {
			for (TagNode node : aChildList) {
				if (node.getAttributeByName("href").endsWith("html")) {
					carSeriesDetailUrlList.add(autohomeHost + node.getAttributeByName("href"));
				}
			}
		}
		
		for (String url : carSeriesDetailUrlList) {
			TagNode urlHtmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
					RequestUtils.getHeaderWithIPAndUA(), null);
			List<TagNode> liChildList = HtmlCleanerUtils.getTagNodeListByXPath(urlHtmlNode, PIC_HTML_XPATH);
			
			for (TagNode node : liChildList) {
				resultList.add(autohomeHost + node.getAttributeByName("href"));
			}
		}
		
		return resultList;
	}

	//按照分类把每个类别的详细页抓取下来。
	private static Map<String, String> crawlerCarSeriesDetail(String carSeriesUrl) {
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(carSeriesUrl, 
				RequestUtils.getHeaderWithIPAndUA(), null);
        
        List<TagNode> liChildList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, TYPE_XPATH);
        
        Map<String, String> resultMap = new HashMap<String, String>();
        //分类：外观-1，座椅-3，中控-10，细节-12，测评13，重要特点-14，其他-99
        for (TagNode node : liChildList) {
        	String style;
        	if (node.getText().toString().contains("外观")) {
        		style = "1";
        	} else if (node.getText().toString().contains("中控")) {
        		style = "10";
        	} else if (node.getText().toString().contains("座椅")) {
        		style = "3";
        	} else if (node.getText().toString().contains("细节")) {
        		style = "12";
        	} else if (node.getText().toString().contains("评测")) {
        		style = "13";
        	} else if (node.getText().toString().contains("特点")) {
        		style = "14";
        	} else {
        		style = "99";
        	}
        	
        	resultMap.put(style, autohomeHost + node.getAttributeByName("href"));
        }
        
		return resultMap;
	}

	private static List<String> crawlerCarSeries(Car car, TagNode htmlNode) {
		
		List<String> seriesUrlList = new ArrayList<String>();
		
		TagNode seriesNode = HtmlCleanerUtils.getTagNodeByXPath(htmlNode, SERIES_GRAND_XPATH);
		
		if (null == seriesNode) {
			return seriesUrlList;
		}
		
		List<TagNode> childList = seriesNode.getChildTagList();
		
		for (int i = 0 ; i < childList.size() ; i += 2) {
			
			TagNode yearNode = childList.get(i);
			TagNode carSeriesParNode = childList.get(i + 1);
			
			String yearStr = StringTools.matcherStrByRegular(yearNode.getText().toString(), "\\d+");
			int year = StringTools.parseIntFromStr(yearStr);
			List<TagNode> carSeriesListNode = HtmlCleanerUtils.getTagNodeListByXPath(carSeriesParNode, SERIES_XPATH);
			for (TagNode liNode : carSeriesListNode) {
				
				TagNode span = HtmlCleanerUtils.getTagNodeByXPath(liNode, "/a/span");
				
				String picNumStr = StringTools.matcherStrByRegular(span.getText().toString(), "\\d+");
				int picNum = StringTools.parseIntFromStr(picNumStr);
				String carSeriesPicUrl = autohomeHost + HtmlCleanerUtils.analyseNodeByXPath(liNode, "/a" , "href");
				String autoSeriesIdStr = StringTools.matcherStrByRegular(carSeriesPicUrl, "series-\\w\\d+").
						replaceAll("series-\\w", "");
				int autoSeriesId = StringTools.parseIntFromStr(autoSeriesIdStr);
				TagNode nameNode = HtmlCleanerUtils.getTagNodeByXPath(liNode, "/a");
				nameNode.removeChild(span);
				String name = nameNode.getText().toString().trim();
				
				seriesUrlList.add(carSeriesPicUrl);
				CarSeriesPicDetail carSeriesPicDetail = new CarSeriesPicDetail(car.getId(), autoSeriesId, year, name, picNum, carSeriesPicUrl);
//				logger.info("save carSeriesPicDetail" + carSeriesPicDetail.getAutoSeriesId());
	        	HibernateOperationUtils.saveObejct(carSeriesPicDetail);
			}
			
		}
		
		return seriesUrlList;
	}


}

