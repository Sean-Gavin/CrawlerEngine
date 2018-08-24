package com.chance.crawlerProject.task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.htmlcleaner.TagNode;

import com.chance.crawlerProject.utils.FileOperationUtils;
import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.HttpConnectManager;
import com.chance.crawlerProject.utils.RequestUtils;
import com.chance.crawlerProject.utils.StringTools;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 27, 2017 7:22:57 PM
 * @version 1.0
 * 
 */
public class ProxyCrawlerTaskThread implements Runnable{
	
	private static int PROXY_NUM = 0;
	private static String FILE_PATH = "src/main/resources/proxy.txt";
	
//	private static String CHECK_UEL = "https://k.autohome.com.cn/52/";
	private static String CHECK_UEL = "https://jiage.autohome.com.cn/web/price/otherlist?memberid=6844490";
	
	private static String XICI_RESULT_XPath = "//table[@id='ip_list']/tbody/tr[position()>1]";
	private static String XICI_NEXT_PAGE_XPATH = "//a[@class='next_page']";
	
	private static String GOUBANJIA_RESULT_XPATH = "//table[@class='table']/tbody/tr";
	
	private static String PROXYDB_XPATH = "//table[@class]/tbody/tr";
	
	
	
	public static void main(String[] args) {
//		FileOperationUtils.writeToFile(FILE_PATH, "".getBytes(), false);
//		checkAllIp();
//		checkAllProxy();
//		String result = HttpConnectManager.httpRequestWithGet(CHECK_UEL, null, null, "222.186.52.136", "28080");
//		String result = HttpConnectManager.httpRequestWithGet(CHECK_UEL, null, null);
//		System.out.println(CHECK_UEL.charAt(83));
//		System.out.println(CHECK_UEL.charAt(84));
//		CHECK_UEL = CHECK_UEL.replaceAll("\\|", "%7c");
		String result = request(CHECK_UEL, RequestUtils.getHeaderWithIPAndUA());
//		String result = request(CHECK_UEL, getHeader());
		System.out.println(result);
	}

	public void run() {
		while (true) {
			String taskInfo = ProxyCrawlerTaskManager.getTaskSign();
			
			if (null == taskInfo) {
				
				return;
			}
			checkAllIp(taskInfo);
		}	
	}
	
	public static Map<String, String> getHeader() {
		Map<String, String> header = new HashMap<String, String>();
		
		String ahrlid = "ahpvno=14; fvlid=1515556862446dNL1RfwIxg; "
				+ "sessionip=111.200.220.210; sessionid=2461250D-395C-47BE-8248-B85E593F116B%7C%7C2018-01-10+12%3A01%3A02.901%7C%7C0; "
				+ "ref=0%7C0%7C0%7C0%7C2018-01-10+14%3A04%3A45.034%7C2018-01-10+12%3A01%3A02.901; "
				+ "area=110199; ahpau=1; sessionvid=E8B52B6D-0230-45C7-984B-9520298ED264; "
				+ "sessionuid=2461250D-395C-47BE-8248-B85E593F116B%7C%7C2018-01-10+12%3A01%3A02.901%7C%7C0;"
				+ " __utma=1.402872547.1515564007.1515564007.1515564007.1; __utmb=1.0.10.1515564007;"
				+ " __utmc=1; __utmz=1.1515564007.1.1.utmcsr=i.autohome.com.cn|utmccn=(referral)|utmcmd=referral|utmcct=/6844490; pcpopclub=B281A0E8402D1BAC6FA815A4C644703194CB15A79412EDFFC5558D45235BD60A38A3FEEF71BC5DB873F170865D05E7E1AB03C6AD2CA40B9707012502F2EF04099D33D70EF178BDCE656F42E63C9CB50C706A210FD3C58974D86BF9B37D539AC9153A165802BAFFFA528BB558381986A5C0D77F2598076688BE541608AE290C5F821BDD3FB5E7B545B0BAE4137B7BE51CE4B92A7EF21FF74FECD92B3D6378F9615DF339239BD23A26F5CB2E3A6E75B7FC9D2D2FBF34545B5C43409F1D5E292BE816F25215EA305E81DDFB4687F140C48772B4F6ABC57D581F7A98B5AF9B88D46139BE7971FBA92E4E86C102E40667FAE5E8B661B0E3CE0DDE4A36725543E6853B8861ACBC963FF70C1A5A6199E7DF82408FF27D48F40CE6A906A68361461A26C2449FC3FAB2D5583904149A58; clubUserShow=65766407|4305|2|%e5%af%b93%e5%90%a6|0|0|0||2018-01-10 14:03:06|0; autouserid=65766407; sessionuserid=65766407; sessionlogin=f84024b8080f469e85b4727d2c1eca9703eb8407; ASP.NET_SessionId=bnh0qne4olfwx5snb0fv5rqp; ahrlid=1515564281462HPFUocAvPz-1515564483597";
//		
		header.put("Cookie", ahrlid);
		header.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:57.0) Gecko/20100101 Firefox/57.0");
		header.put("Host", "i.autohome.com.cn");
		header.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
		header.put("Cache-Control", "no-cache");
//		header.put("Referer", "https://i.autohome.com.cn/6844490");
		header.put("Connection", "keep-alive");
		header.put("Upgrade-Insecure-Requests", "1");
		
		return header;
	}
	
	public static String request(String url, Map<String, String> headerMap) {

//		HttpHost proxy = new HttpHost("45.63.58.133", 8989);
//		HttpHost proxy = new HttpHost("45.77.122.131", 8989);

	    //把代理设置到请求配置
		RequestConfig defaultRequestConfig = RequestConfig.custom()
//				.setProxy(proxy)
				.build();

	        //实例化CloseableHttpClient对象
	    CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();

	        //访问目标地址
	    HttpGet httpGet = new HttpGet(url);
	    
	    if(null != headerMap && !headerMap.isEmpty()){
	    	
	    	for(Map.Entry<String, String> entry : headerMap.entrySet()){
	    		httpGet.addHeader(entry.getKey(), entry.getValue());
	    	}
	    }
	    try {
	    	CloseableHttpResponse response = httpclient.execute(httpGet);
	        int statusCode = response.getStatusLine().getStatusCode();
	        System.out.println(statusCode);
	        System.out.println(response.getHeaders("Location"));
	        HttpEntity entity = response.getEntity();
	        String result = EntityUtils.toString(entity);
	        return result;
	    } catch (Exception e) {
	    	e.printStackTrace();
	    }
	    return null;
	}
	
	public static void checkAllIp(String ip) {
		String IP_PATH = "src/main/resources/ip.txt";
		String url = "https://club.autohome.com.cn/bbs/threadqa-c-692-61059244-1.html#pvareaid=2037182";
		
		Map<String, String> headerMap = new HashMap<String, String>();
		headerMap.put("X-FORWARDED-FOR", ip);
		headerMap.put("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/63.0.3239.84 Safari/537.36");
	
		TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, headerMap);
		String result = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//*[@id='consnav']/span[4]");
		if (!"唉，等车的日子真难熬啊".equals(result)) {
			FileOperationUtils.writeToFile(IP_PATH, (ip + "\n").getBytes(), true);
		} else {
			htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, headerMap);
			result = HtmlCleanerUtils.analyseNodeByXPath(htmlNode, "//*[@id='consnav']/span[4]");
			
			if (!"唉，等车的日子真难熬啊".equals(result)) {
				FileOperationUtils.writeToFile(IP_PATH, (ip + "\n").getBytes(), true);
			} else {
				System.out.println(ip);
			}
		} 
	}
	
	public static void checkAllProxy () {
		Set<String> oldList = new HashSet<String>();
		
		File file = new File(FILE_PATH);
        try {
        	BufferedReader br = new BufferedReader(new FileReader(file));
        	String str = "";
			while((str = br.readLine()) != null) {
				
				oldList.add(str);
			}
		} catch (IOException e) {
		}
        FileOperationUtils.writeToFile(FILE_PATH, "".getBytes(), false);
        for (String proxy : oldList) {
			String[] info = proxy.split(":");
			
			String ip = info[0];
			String port = info[1];
			String result = HttpConnectManager.httpRequestWithGet(CHECK_UEL, null, null, ip, port);
			if (!StringTools.isBlank(result)) {
				System.out.println("OK:------" + proxy);
				FileOperationUtils.writeToFile(FILE_PATH, (proxy + "\n").getBytes(), true);
			} else {
				System.out.println("ERROR:------" + proxy);
			}
        }
        
        System.out.println("DONE");
	}
	
	
	public static void crawlerXiCiProxy() {
		String url = "http://www.xicidaili.com/nn/";
		int page = 0;
		while (page < 10) {
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url, 
					RequestUtils.getHeaderWithIPAndUA(), null);
	        
			List<TagNode> trNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, XICI_RESULT_XPath);
			
			for (TagNode trNode : trNodeList) {
				List<TagNode> tdNodeList = trNode.getChildTagList();
				String ip = tdNodeList.get(1).getText().toString();
				String port = tdNodeList.get(2).getText().toString();
				String status = tdNodeList.get(4).getText().toString();
				String speedStr = HtmlCleanerUtils.analyseNodeByXPath(trNode, "/td[7]/div/div", "style");
				int speed = Integer.parseInt(StringTools.matcherStrByRegular(speedStr, "\\d+"));
				String connectTimeStr = HtmlCleanerUtils.analyseNodeByXPath(trNode, "/td[8]/div/div", "style");
				int connectTime = Integer.parseInt(StringTools.matcherStrByRegular(connectTimeStr, "\\d+"));
				String proxy = ip + ":" + port + "\n";
				if (status.equals("高匿") && speed > 90 && connectTime > 90) {
					String result = HttpConnectManager.httpRequestWithGet(CHECK_UEL, null, null, ip, port);
					if (!StringTools.isBlank(result)) {
						if (PROXY_NUM ++ > 1000) {
							return;
						}
						FileOperationUtils.writeToFile(FILE_PATH, proxy.getBytes(), true);
					}
				} 
			}
			page ++;
			url = "http://www.xicidaili.com" + HtmlCleanerUtils.analyseNodeByXPath(htmlNode, XICI_NEXT_PAGE_XPATH, "href");
		}
	}
	
	public static void crawlerGouBanJiaProxy() {
		String url = "http://www.goubanjia.com/free/gngn/index";
		int page = 1;
		while (page < 10) {
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url + page + ".shtml", 
					RequestUtils.getHeaderWithIPAndUA(), null);
	        
			List<TagNode> trNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, GOUBANJIA_RESULT_XPATH);
			
			for (TagNode trNode : trNodeList) {
				List<TagNode> tdNodeList = trNode.getChildTagList();
//				String ip = tdNodeList.get(0).getText().toString();
				String ip = "";
				
				List<TagNode> childList = tdNodeList.get(0).getChildTagList();
				
				for (int i = 0 ; i < childList.size() - 1 ; i++) {
					
					if (!"p".equals(childList.get(i).getName())) {
						String info = childList.get(i).getText().toString();
						info = info.replaceAll("\\n", "").replaceAll("\\s", "").replaceAll("\\t", "");
						if (!StringTools.isBlank(info) && (info.matches(".*\\d+.*") || info.contains("."))) {
							ip += info;
						}
					}
				}
				String port = HtmlCleanerUtils.analyseNodeByXPath(tdNodeList.get(0), "//span[@class]");
				String speedStr = tdNodeList.get(5).getText().toString();
				int speed = Integer.parseInt(StringTools.matcherAllStrByRegular(speedStr, "\\d+"));
				System.out.println("get one" + ip + ":" + port + "--" + speed);
				if (speed < 2000) {
					String result = HttpConnectManager.httpRequestWithGet(CHECK_UEL, null, null, ip, port);
					if (!StringTools.isBlank(result)) {
						if (PROXY_NUM ++ > 1000) {
							return;
						}
						String proxy = ip + ":" + port + "\n";
						System.out.println(proxy);
						FileOperationUtils.writeToFile(FILE_PATH, proxy.getBytes(), true);
					}
				} 
			}
			page ++;
		}
	}

	
	public static void crawlerProxyDBProxy() {
		
		String url = "http://proxydb.net/?protocol=https&offset=";
		int page = 0;
		while (page < 1) {
			TagNode htmlNode = HtmlCleanerUtils.getHtmlTagNodeHttpClient(url + 15*page, 
					RequestUtils.getHeaderWithIPAndUA(), null);
	        
			List<TagNode> trNodeList = HtmlCleanerUtils.getTagNodeListByXPath(htmlNode, PROXYDB_XPATH);
			
			for (TagNode trNode : trNodeList) {
				List<TagNode> tdNodeList = trNode.getChildTagList();
				String proxy = HtmlCleanerUtils.analyseNodeByXPath(tdNodeList.get(0), "/a[href]");
				String speedStr = HtmlCleanerUtils.analyseNodeByXPath(tdNodeList.get(5), "/span");
				int speed = Integer.parseInt(StringTools.matcherAllStrByRegular(speedStr, "\\d+"));
				
				String[] info = proxy.split(":");
				System.out.println(proxy);
				System.out.println(speedStr);
				String ip = info[0];
				String port = info[1];
				
				System.out.println("get one" + ip + ":" + port + "--" + speed);
				
				
//				if (status.equals("高匿") && speed > 90 && connectTime > 90) {
//					String result = HttpConnectManager.httpRequestWithGet("https://www.baidu.com", null, null, ip, port);
//					if (!StringTools.isBlank(result)) {
//						if (PROXY_NUM ++ > 1000) {
//							return;
//						}
//						FileOperationUtils.writeToFile(FILE_PATH, proxy.getBytes(), true);
//					}
//				} 
			}
			page ++;
		}
	}
}

