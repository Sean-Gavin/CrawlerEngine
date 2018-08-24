package com.chance.crawlerProject.utils;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.net.ssl.SSLContext;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLContextBuilder;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Sep 5, 2017 3:27:38 PM
 * @version 1.0
 * 
 */
public class HttpConnectManager {

//	final static Logger logger = LoggerFactory.getLogger(HttpConnectManager.class);
	
    private static PoolingHttpClientConnectionManager poolConnManager = null;
    private static int maxTotalPool = 200;
    private static int maxConPerRoute = 150;
    private static int socketTimeout = 2000;
    private static int connectionRequestTimeout = 2000;
    private static int connectTimeout = 2000;
			
    static{
    	try {
	        SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy(){

				public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
					// TODO Auto-generated method stub
					return true;
				}
	        	
	        }).build();
	        
	        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory( sslContext, new String[] { "TLSv1" }, null,  
	        		SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);  
	        
	        ConnectionSocketFactory plainsf = PlainConnectionSocketFactory.getSocketFactory();
	        
 	        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory> create()
	                .register("https", sslsf)
	                .register("http", plainsf)
	                .build();
 	        
 	       poolConnManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
 	       poolConnManager.setMaxTotal(maxTotalPool);
 	       poolConnManager.setDefaultMaxPerRoute(maxConPerRoute);
 	       SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(socketTimeout).build();
 	       poolConnManager.setDefaultSocketConfig(socketConfig);
 	       
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    private static CloseableHttpClient getConnection() {  
        RequestConfig requestConfig = RequestConfig
        		.custom()
        		.setConnectionRequestTimeout(connectionRequestTimeout)  
                .setConnectTimeout(connectTimeout)
                .setSocketTimeout(socketTimeout)
                .build(); 
        CloseableHttpClient httpClient = HttpClients
        		.custom()
        		.setConnectionManager(poolConnManager)
        		.setDefaultRequestConfig(requestConfig)
        		.build();  
        return httpClient;  
    }  
	
    
    public static String httpRequestWithGet(String url, Map<String, String> headerMap){
    	return httpRequestWithGet(url, headerMap, null, null, null);
    }
    
    public static String httpRequestWithGet(String url, Map<String, String> headerMap, String charset){
    	return httpRequestWithGet(url, headerMap, charset, null, null);
    }
	
	public static String httpRequestWithGet(String url, Map<String, String> headerMap, String charset, String proxyIP, String proxyPort){
		
		CloseableHttpClient httpClient = getConnection();
		HttpGet httpGet = null;
		CloseableHttpResponse response = null;
		try {
			httpGet = new HttpGet(url);
			if (!StringUtils.isBlank(proxyIP)) {
				HttpHost proxy = new HttpHost(proxyIP, Integer.parseInt(proxyPort));
				RequestConfig config = RequestConfig.custom().setProxy(proxy).build();
				httpGet.setConfig(config);
			}
			
			if(null != headerMap && !headerMap.isEmpty()){

				for(Map.Entry<String, String> entry : headerMap.entrySet()){

					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}

//			if(null != headers && !headers.isEmpty()){
//
//				System.out.println("executing request headers are: " + headers + " and URI is: [ " + httpGet.getURI() + " ].");
////				httpSendLogger.info("executing request headers are: [{}] and URI is: [{}].", headers, httpGet.getURI());
//			}
//			else{
//				System.out.println("executing request  URI is: [ " + httpGet.getURI() + " ].");
////				httpSendLogger.info("executing request URI is: [{}].", httpGet.getURI());
//			}

			response = httpClient.execute(httpGet);
			try {
//				if (logger.isDebugEnabled()) {
//
//					logger.debug("----------------------------------------");
//					logger.debug(response.getStatusLine().toString());
//				}


				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

//					if (logger.isDebugEnabled()) {
//						logger.debug("The response is 200 OK.");
//						logger.debug("----------------------------------------");
//					}
//					System.out.println("The response is 200 OK.");
				}
				else{

//					logger.info("The response code is {}.", response.getStatusLine().getStatusCode());
//					System.out.println("The response code is {}." + response.getStatusLine().getStatusCode());
					return null;
				}
							
				
				// Get hold of the response entity
				HttpEntity entity = response.getEntity();;

				// If the response does not enclose an entity, there is no need
				// to bother about connection release
				if (entity != null) {

					String result = EntityUtils.toString(entity, getCharset(response, charset));
//					if(result.length() > 100){
//						
//						httpSendLogger.info("The response entity is {}  ",result.substring(0, 50));
//					} else {
//						
//						httpSendLogger.info("The response entity is {}  ",result);
//					}
					
					return result;
				}
			} finally {
		 		if (response != null) {
		             try {
		                 EntityUtils.consume(response.getEntity());
		                 response.close();
		             } catch (IOException e) {

		             }
		 		}
			}
		} catch (ClientProtocolException e) {
//			httpSendLogger.error("Catch ClientProtocolException : {}", e);
			
//			e.printStackTrace();
		} catch (IOException e) {
//			httpSendLogger.error("Catch IOException : {}", e);
//			e.printStackTrace();
		} catch (Exception e) {

//			httpSendLogger.error("Catch exception : {}", e);
//			e.printStackTrace();
		} finally {
			
			httpGet.releaseConnection();
	 		if (response != null) {
	             try {
	                 EntityUtils.consume(response.getEntity());
	                 response.close();
	             } catch (IOException e) {

	             }
	 		}
		}
		return null;
	}
	


	private static String getCharset(CloseableHttpResponse response, String charset) {
		
		if (StringUtils.isBlank(charset)) {
			
			Header header = response.getFirstHeader("Content-Type");
			
			if (null != header) {
				String contentType = header.getValue();
				
				if (contentType.contains("UTF-8") || contentType.contains("utf-8")) {
					
					return "utf-8";
				} else if (contentType.contains("gb2312")) {
					
					return "gb2312";
				} else if (contentType.contains("gbk")) {
					
					return "gbk";
				}
			}
			
			return "UTF-8";
		}
		
		return charset;
	}

	public static String httpRequestWithPost(String host, int port , String path, Map<String, String> headers, String jsonBody){
		
		CloseableHttpClient httpClient = getConnection();
		HttpPost httpPost = null;
		CloseableHttpResponse response = null;
		try {
			
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost(host).setPath(path);
            if(80 != port){
            	
            	builder.setPort(port);
            }
			
			httpPost = new HttpPost(builder.build());

			if(null != headers && !headers.isEmpty()){

				for(Map.Entry<String, String> entry : headers.entrySet()){

					httpPost.addHeader(entry.getKey(), entry.getValue());
				}
			}

//			if(null != headers && !headers.isEmpty()){
//
//				System.out.println("executing request headers are: " + headers + " and URI is: [ " + httpPost.getURI() + " ].");
////				httpSendLogger.info("executing request headers are: [{}] and URI is: [{}].", headers, httpGet.getURI());
//			}
//			else{
//				System.out.println("executing request  URI is: [ " + httpPost.getURI() + " ].");
////				httpSendLogger.info("executing request URI is: [{}].", httpGet.getURI());
//			}

			if(!StringUtils.isBlank(jsonBody)){
				
				StringEntity bodyEntity = new StringEntity(jsonBody, "UTF-8");
				bodyEntity.setContentEncoding("UTF-8");

				bodyEntity.setContentType("application/json");
				httpPost.setEntity(bodyEntity);
			}
			
			
			response = httpClient.execute(httpPost);
			try {
//				if (logger.isDebugEnabled()) {
//
//					logger.debug("----------------------------------------");
//					logger.debug(response.getStatusLine().toString());
//				}

				System.out.println(response.getStatusLine().toString());

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

//					if (logger.isDebugEnabled()) {
//						logger.debug("The response is 200 OK.");
//						logger.debug("----------------------------------------");
//					}
					
					System.out.println("The response is 200 OK.");
				}
				else{

//					logger.info("The response code is {}.", response.getStatusLine().getStatusCode());
					
					System.out.println("The response code is {}." + response.getStatusLine().getStatusCode());
					return null;
				}

				// Get hold of the response entity
				HttpEntity entity = response.getEntity();

				// If the response does not enclose an entity, there is no need
				// to bother about connection release
				if (entity != null) {

					String result = EntityUtils.toString(entity);
//					if(result.length() > 100){
//						
//						httpSendLogger.info("The response entity is {}  ",result.substring(0, 50));
//					} else {
//						
//						httpSendLogger.info("The response entity is {}  ",result);
//					}
					
					return result;
				}
			} finally {
		 		if (response != null) {
		             try {
		                 EntityUtils.consume(response.getEntity());
		                 response.close();
		             } catch (IOException e) {

		             }
		 		}
			}
		} catch (ClientProtocolException e) {
//			httpSendLogger.error("Catch ClientProtocolException : {}", e);
			
			e.printStackTrace();
		} catch (IOException e) {
//			httpSendLogger.error("Catch IOException : {}", e);
			e.printStackTrace();
		} catch (Exception e) {

//			httpSendLogger.error("Catch exception : {}", e);
			e.printStackTrace();
		} finally {
			
			httpPost.releaseConnection();
	 		if (response != null) {
	             try {
	                 EntityUtils.consume(response.getEntity());
	                 response.close();
	             } catch (IOException e) {

	             }
	 		}
		}
		return null;
	}
	
	
	
	public static String httpRequestWithGet(String host, int port , String path, Map<String, String> headers,  List<NameValuePair> qparams){
		
		CloseableHttpClient httpClient = getConnection();
		HttpGet httpGet = null;
		CloseableHttpResponse response = null;
		try {
			
            URIBuilder builder = new URIBuilder();
            builder.setScheme("http").setHost(host).setPath(path).addParameters(qparams);
            if(80 != port){
            	
            	builder.setPort(port);
            }
			
            httpGet = new HttpGet(builder.build());

			if(null != headers && !headers.isEmpty()){

				for(Map.Entry<String, String> entry : headers.entrySet()){

					httpGet.addHeader(entry.getKey(), entry.getValue());
				}
			}
			
			
			response = httpClient.execute(httpGet);
			try {
//				if (logger.isDebugEnabled()) {
//
//					logger.debug("----------------------------------------");
//					logger.debug(response.getStatusLine().toString());
//				}

				System.out.println(response.getStatusLine().toString());

				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

//					if (logger.isDebugEnabled()) {
//						logger.debug("The response is 200 OK.");
//						logger.debug("----------------------------------------");
//					}
					
					System.out.println("The response is 200 OK.");
				}
				else{

//					logger.info("The response code is {}.", response.getStatusLine().getStatusCode());
					
					System.out.println("The response code is {}." + response.getStatusLine().getStatusCode());
					return null;
				}

				// Get hold of the response entity
				HttpEntity entity = response.getEntity();

				// If the response does not enclose an entity, there is no need
				// to bother about connection release
				if (entity != null) {

					String result = EntityUtils.toString(entity);
//					if(result.length() > 100){
//						
//						httpSendLogger.info("The response entity is {}  ",result.substring(0, 50));
//					} else {
//						
//						httpSendLogger.info("The response entity is {}  ",result);
//					}
					
					return result;
				}
			} finally {
		 		if (response != null) {
		             try {
		                 EntityUtils.consume(response.getEntity());
		                 response.close();
		             } catch (IOException e) {

		             }
		 		}
			}
		} catch (ClientProtocolException e) {
//			httpSendLogger.error("Catch ClientProtocolException : {}", e);
			
			e.printStackTrace();
		} catch (IOException e) {
//			httpSendLogger.error("Catch IOException : {}", e);
			e.printStackTrace();
		} catch (Exception e) {

//			httpSendLogger.error("Catch exception : {}", e);
			e.printStackTrace();
		} finally {
			
			httpGet.releaseConnection();
	 		if (response != null) {
	             try {
	                 EntityUtils.consume(response.getEntity());
	                 response.close();
	             } catch (IOException e) {

	             }
	 		}
		}
		return null;
	}
}

