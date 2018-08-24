package com.chance.crawlerProject.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 11, 2017 6:17:02 PM
 * @version 1.0
 * 
 */
public class RequestUtils {
	private static  List<String> proxyList = new ArrayList<String>();
	private static List<String> ipList = new ArrayList<String>();
	
	private static String PROXY_PATH = "proxy.txt";
	private static String IP_PATH = "ip.txt";
	
	static{
		proxyList = FileOperationUtils.readFileByLine("src/main/resources/" + PROXY_PATH);
		ipList = FileOperationUtils.readFileByLine("src/main/resources/" + IP_PATH);
		
		if (null == ipList || ipList.size() == 0) {
			proxyList = FileOperationUtils.readFileByLine(
					Thread.currentThread().getContextClassLoader().getResource("").getPath() + PROXY_PATH);
			ipList = FileOperationUtils.readFileByLine(
					Thread.currentThread().getContextClassLoader().getResource("").getPath() + IP_PATH);
		}
	}
	
	public static Map<String, String> getHeader() {
		
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Connection", "keep-alive");
//		headerMap.put("Cache-Control", "max-age=0");
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Host", "k.autohome.com.cn");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
		
		return headerMap;
	}
	
	public static Map<String, String> getHeader(String host) {
		
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("Upgrade-Insecure-Requests", "1");
		headerMap.put("Host", "host");
		headerMap.put("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8");
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
		
		return headerMap;
	}
	
	
	public static Map<String, String> getPriseHeader() {
		Map<String, String> header = new HashMap<String, String>();
		
		String ahrlid = "ahrlid=1515384855148FgD44mc8-" + System.currentTimeMillis();
		
		header.put("Cookie", ahrlid);
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
		header.put("Host", "k.autohome.com.cn");
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		header.put("Cache-Control", "no-cache");
		header.put("Referer", "https://car.autohome.com.cn/config/series/2090.html");
		header.put("Connection", "keep-alive");
		header.put("Upgrade-Insecure-Requests", "1");
		
		return header;
	}
	
	public static synchronized Map<String, String> getHeaderWithIPAndUA() {
		
		Map<String, String> headerMap = new HashMap<String, String>();
		if (null != ipList && ipList.size() > 0) {
			Random r = new Random();
			int index = r.nextInt(ipList.size());
			headerMap.put("X-FORWARDED-FOR", ipList.get(index));
			headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
		}
		return headerMap;
	}
	
	public static  Map<String, String> getHeaderWithReferer(String referer) {
		
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap = getHeaderWithIPAndUA();
		
		headerMap.put("Referer", referer);
		
		return headerMap;
	} 
	
	public static  Map<String, String> getHeaderWithCookie() {
		
		Map<String, String> header = new HashMap<String, String>();
		
		String ahrlid = "ahpvno=14; fvlid=1515556862446dNL1RfwIxg; "
				+ "sessionip=111.200.220.210; sessionid=2461250D-395C-47BE-8248-B85E593F116B%7C%7C2018-01-10+12%3A01%3A02.901%7C%7C0; "
				+ "ref=0%7C0%7C0%7C0%7C2018-01-10+14%3A04%3A45.034%7C2018-01-10+12%3A01%3A02.901; "
				+ "area=110199; ahpau=1; sessionvid=E8B52B6D-0230-45C7-984B-9520298ED264; "
				+ "sessionuid=2461250D-395C-47BE-8248-B85E593F116B%7C%7C2018-01-10+12%3A01%3A02.901%7C%7C0;"
				+ " __utma=1.402872547.1515564007.1515564007.1515564007.1; __utmb=1.0.10.1515564007;"
				+ " __utmc=1; __utmz=1.1515564007.1.1.utmcsr=i.autohome.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/6844490; pcpopclub=B281A0E8402D1BAC6FA815A4C644703194CB15A79412EDFFC5558D45235BD60A38A3FEEF71BC5DB873F170865D05E7E1AB03C6AD2CA40B9707012502F2EF04099D33D70EF178BDCE656F42E63C9CB50C706A210FD3C58974D86BF9B37D539AC9153A165802BAFFFA528BB558381986A5C0D77F2598076688BE541608AE290C5F821BDD3FB5E7B545B0BAE4137B7BE51CE4B92A7EF21FF74FECD92B3D6378F9615DF339239BD23A26F5CB2E3A6E75B7FC9D2D2FBF34545B5C43409F1D5E292BE816F25215EA305E81DDFB4687F140C48772B4F6ABC57D581F7A98B5AF9B88D46139BE7971FBA92E4E86C102E40667FAE5E8B661B0E3CE0DDE4A36725543E6853B8861ACBC963FF70C1A5A6199E7DF82408FF27D48F40CE6A906A68361461A26C2449FC3FAB2D5583904149A58; clubUserShow=65766407|4305|2|%e5%af%b93%e5%90%a6|0|0|0||2018-01-10 14:03:06|0; autouserid=65766407; sessionuserid=65766407; sessionlogin=f84024b8080f469e85b4727d2c1eca9703eb8407; ASP.NET_SessionId=bnh0qne4olfwx5snb0fv5rqp; ahrlid=1515564281462HPFUocAvPz-1515564483597";
		
		header.put("Cookie", ahrlid);
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
		header.put("Host", "i.autohome.com.cn");
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		header.put("Cache-Control", "no-cache");
		header.put("Connection", "keep-alive");
		header.put("Upgrade-Insecure-Requests", "1");
		
		return header;
	} 
	
	public static Map<String, String> getProxy() {
		Map<String, String> proxyMap = new HashMap<String, String>();
		if (null != proxyList && proxyList.size() > 0) {
			Random r = new Random();
			int index = r.nextInt(proxyList.size());
			
			String[] info = proxyList.get(index).split(":");
			for (String proxy : info) {
				if (proxy.contains(".")) {
					proxyMap.put("ip", proxy);
				} else {
					proxyMap.put("port", proxy);
				}
			}
			return proxyMap;
		} 
		return proxyMap;
	}
}

