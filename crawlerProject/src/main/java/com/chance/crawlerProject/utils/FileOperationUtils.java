package com.chance.crawlerProject.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.task.AutohomeIncrementCrawlTask;

/** 
 * 
 * @author Sean
 * @date 创建时间：Nov 6, 2017 8:11:38 PM
 * @version 1.0
 * 
 */
public class FileOperationUtils {
	
	private static Logger logger = LoggerFactory.getLogger(FileOperationUtils.class);
	
	public static boolean writeToFileByFileWriter(String path, byte[] contentBytes, String code) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(path, true);
			fw.write(new String(contentBytes, code));
			return true;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return false;
	}
	
	public static boolean writeToFileByBufferWriter(String path, byte[] contentBytes, String code) {
		BufferedWriter out = null;     
        try {     
            out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true)));     
            out.write(new String(contentBytes, code));    
            return true;
        } catch (Exception e) {     
            e.printStackTrace();     
        } finally {     
            try {     
                if(out != null){  
                    out.close();     
                }  
            } catch (IOException e) {     
                e.printStackTrace();     
            }     
        }    
        return false;
	}
	
	public static void writeToFile(String path, byte[] contentBytes, boolean append) {
		FileWriter fw = null;
		try {
			fw = new FileWriter(path, append);
			fw.write(new String(contentBytes));
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				fw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	public static List<String> readFileByLine(String path) {
		List<String> resultList = new ArrayList<String>();
		if (!StringTools.isBlank(path)) {
			
			File file = new File(path);
	        try {
	        	BufferedReader br = new BufferedReader(new FileReader(file));
	        	String str = "";
				while((str = br.readLine()) != null) {
					resultList.add(str);
				}
			} catch (IOException e) {
				logger.error("path error, the error path is " + path);
				return null;
			}
		}
		return resultList;
	}
	
	
}

