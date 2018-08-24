package com.chance.crawlerProject.task;

import java.util.ArrayList;
import java.util.List;

import com.chance.crawlerProject.utils.FileOperationUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 28, 2017 10:22:37 AM
 * @version 1.0
 * 
 */
public class ProxyCrawlerTaskManager {
	
	static List<String> ipList = new ArrayList<String>();
	static int index = 0;
	public static void main(String[] args) {
		
		String IP_PATH = "src/main/resources/ip.txt";
		
		ipList = FileOperationUtils.readFileByLine(IP_PATH);
		
		FileOperationUtils.writeToFile(IP_PATH, "".getBytes(), false);
		
		for (int i = 0; i < 40; i++) {
			
			ProxyCrawlerTaskThread task = new ProxyCrawlerTaskThread();
			
			Thread thread = new Thread(task);
			
			thread.start();
		}
		System.out.println("DONE!");
	}
	
	
	public static synchronized String getTaskSign() {
		if (index >= ipList.size()) {
			return  null;
		}
		return ipList.get(index ++);
	}
}

