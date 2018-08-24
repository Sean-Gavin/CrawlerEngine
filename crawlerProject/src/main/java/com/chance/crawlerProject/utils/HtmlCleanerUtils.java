package com.chance.crawlerProject.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;
import org.jsoup.Connection;
import org.jsoup.Jsoup;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 11, 2017 11:17:33 AM
 * @version 1.0
 * 
 */
public class HtmlCleanerUtils {

	private static String SINGLE_XPATH_END_REGEX = "\\[\\d\\]$";
	
	public static TagNode getHtmlTagNodeHttpClient(String url, Map<String, String> headerMap, String charset) {
		return getHtmlTagNodeHttpClient(url, headerMap, charset, false);
	}
	
	public static TagNode getHtmlTagNodeHttpClient(String url, Map<String, String> headerMap) {
		return getHtmlTagNodeHttpClient(url, headerMap, null, false);
	}
	
	public static String getHtmlByUrl(String url, Map<String, String> headerMap, String charset, boolean useProxy) {
		if (StringTools.isBlank(url)) {
			return null;
		}
		String resultHtml = null;
		
		if (useProxy) {
			Map<String, String> proxy = RequestUtils.getProxy();
			if (null != proxy && !proxy.isEmpty()) {
//				if (null != headerMap) {
//					headerMap.put("X-FORWARDED-FOR", proxy.get("ip"));
//				}
				resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset, proxy.get("ip"), proxy.get("port"));
			} else {
				resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset);
			}
		} else {
			resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset);
		}

		//请求2次，排除网络异常影响
		if (StringTools.isBlank(resultHtml)) {
			if (useProxy) {
				Map<String, String> proxy = RequestUtils.getProxy();
				if (null != proxy && !proxy.isEmpty()) {
					if (null != headerMap) {
						headerMap.put("X-FORWARDED-FOR", proxy.get("ip"));
					}
					resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset, proxy.get("ip"), proxy.get("port"));
				} else {
					resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset);
				}
			} else {
				resultHtml = HttpConnectManager.httpRequestWithGet(url, headerMap, charset);
			}
			
		}
		
		if (StringTools.isBlank(resultHtml)) {
			Connection connect = Jsoup.connect(url);  
			try {
				resultHtml = connect.get().body().html();
			} catch (IOException e) {
	        	return null;
			}
		}
		return resultHtml;
	}
	
	public static TagNode getHtmlTagNodeHttpClient(String url, Map<String, String> headerMap, String charset, boolean useProxy) {
		
		String resultHtml = null;
		
		resultHtml = getHtmlByUrl(url, headerMap, charset, useProxy);
		
		try {
	        if (!StringTools.isBlank(resultHtml)) {
	            HtmlCleaner hc = new HtmlCleaner();  
	            TagNode htmlNode = hc.clean(resultHtml);
	            return htmlNode;
	        } else {
	        	System.out.println("ERROR1 URL----" + url);
	        	return null;
	        }
		} catch (Exception e) {
			//如果解析失败换一种请求html的方法
	        try {
	        	Connection connect = Jsoup.connect(url);  
				resultHtml = connect.get().body().html();
		        if (!StringTools.isBlank(resultHtml)) {
		            HtmlCleaner hc = new HtmlCleaner();  
		            TagNode htmlNode = hc.clean(resultHtml);
		            return htmlNode;
		        } else {
		        	System.out.println("ERROR2 URL----" + url);
		        	return null;
		        }
			} catch (IOException e1) {
	        	System.out.println("ERROR3 URL----" + url);
	        	return null;
			}  
		}
	}
	
	public static TagNode getHtmlTagNodeJsoup(String url, Map<String, String> headerMap, String charset) {
		if (StringTools.isBlank(url)) {
			return null;
		}
		
	    String resultHtml = null;  
	    try {  
	        Connection connect = Jsoup.connect(url);  
	        resultHtml = connect.get().body().html();  
	    } catch (IOException e) {  
	        e.printStackTrace();  
	    }  
        HtmlCleaner hc = new HtmlCleaner();  
        TagNode htmlNode = hc.clean(resultHtml);
        
        return htmlNode;
	}
	
	public static String analyseNodeByXPath(TagNode node, String XPath) {
	
		return analyseNodeByXPath(node, XPath, null, true);
	}
	
	public static String analyseNodeByXPath(TagNode node, String XPath, boolean removeUselessChar) {
		
		return analyseNodeByXPath(node, XPath, null, removeUselessChar);
	}
	
	public static String analyseNodeByXPath(TagNode node, String XPath, String attribute) {
		
		return analyseNodeByXPath(node, XPath, attribute, true);
	}
	
	public static String analyseNodeByXPath(TagNode node, String XPath, String attribute, boolean removeUselessChar) {
		if (null == node) {
			return "";
		}
		
		try {
			Pattern pattern = Pattern.compile(SINGLE_XPATH_END_REGEX);
			
			Matcher matcher = pattern.matcher(XPath);
			
			if (matcher.find()) {
				//抓取固定的标签内容，直接抓取
				String result = "";
				Object[] objects = node.evaluateXPath(XPath);
				if (null != objects && objects.length > 0) {
					TagNode targetNode = (TagNode) objects[0];
					if (!StringUtils.isBlank(attribute)) {
						
						result = targetNode.getAttributeByName(attribute);
					} else {
						result =targetNode.getText().toString();
					}
				}
				if (removeUselessChar) {
					result = StringTools.removeUselessChar(result);
				}
				return result;
			} else {
				//抓取所有符合的字标签，判断数组然后抓取
				Object[] objects = node.evaluateXPath(XPath);
				
				StringBuilder sb = new StringBuilder(); 
				
				for (Object object : objects) {
					TagNode targetNode = (TagNode) object;
					String result = "";
					if (!StringUtils.isBlank(attribute)) {
						result = targetNode.getAttributeByName(attribute);
//						sb.append(StringTools.removeUselessChar(targetNode.getAttributeByName(attribute))).append(",");
					} else {
						result = targetNode.getText().toString();
//						sb.append(StringTools.removeUselessChar(targetNode.getText().toString())).append(",");
					}
					if (removeUselessChar) {
						result = StringTools.removeUselessChar(result);
					}
					sb.append(result).append(",");
				}
				if (StringTools.isBlank(sb)) {
					return "";
				}
				return sb.substring(0, sb.length() -1);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return "";
	}
	
	public static TagNode getTagNodeByXPath(TagNode node, String XPath) {
		
		if (getTagNodeListByXPath(node, XPath).size() > 0) {
			return getTagNodeListByXPath(node, XPath).get(0);
		} else {
			return null;
		}
		
		
	}
	
	public static List<TagNode> getTagNodeListByXPath(TagNode node, String XPath) {
		
		List<TagNode> resultNode = new ArrayList<TagNode>();
		
		if (null == node || StringTools.isBlank(XPath)){
			return resultNode;
		}
		
		try {
			Object [] objects = node.evaluateXPath(XPath);
			for (Object obj : objects) {
				resultNode.add((TagNode) obj);
			}
			return resultNode;
		} catch (XPatherException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return resultNode;
		
	}
}

