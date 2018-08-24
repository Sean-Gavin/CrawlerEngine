package com.chance.crawlerProject;

import java.util.List;

import org.htmlcleaner.TagNode;

import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 12, 2017 5:09:37 PM
 * @version 1.0
 * 
 */
public class CrawlerTest {
	//1-0/s4-1.html
	private static String BASE_URL = "https://www.zybang.com/course/favorable/salecoursedetail?grade=14&subject=2";

	static String XPATH = "//div[@class='introduce']/span[3]/i";
	public static void main(String[] args) {
		
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(BASE_URL, null, null, false);
		
//		String html = HtmlCleanerUtils.getHtmlByUrl(BASE_URL, null, null, false);
//		System.out.println(html);
		
		TagNode node = HtmlCleanerUtils.getTagNodeByXPath(htmlNode, XPATH);
		
		System.out.println(node.getText());
		
//		List<TagNode> nodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, XPATH);
//		for (TagNode node : nodeList) {
//
//			String info = node.getAttributeByName("uid");
//			System.out.println(info);
//		}
	}

}

