package com.chance.crawlerProject.utils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 25, 2017 11:39:31 AM
 * @version 1.0
 * 
 */
public class DateTools {
	
	private static String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

	public static Date getNowDate(String format) {
		Date currentTime = new Date();
		if (StringUtils.isBlank(format)) {
			format = DEFAULT_DATE_FORMAT;
		}
		SimpleDateFormat formatter = new SimpleDateFormat(format);
		String dateString = formatter.format(currentTime);
		ParsePosition pos = new ParsePosition(0);
		Date currentTime_2 = formatter.parse(dateString, pos);
		return currentTime_2;
	}
	
	public static Date FormatDate(String timeStr, String format) {
		Date date = new Date();
		if (StringTools.isBlank(timeStr)) {
			return date;
		}
		
		if (StringTools.isBlank(format)) {
			format = DEFAULT_DATE_FORMAT;
		}

		SimpleDateFormat formatter = new SimpleDateFormat(format);
		try {
			date = formatter.parse(timeStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return date;
	}
	
	public static Timestamp FormatTimeStamp(String timeStr, String format) {
	
		Date date = FormatDate(timeStr, format);
		return getTimeStamp(date);
	}
	
	
	public static Timestamp getTimeStamp(Date date) {
		long time = date.getTime();
		Timestamp timestamp = new Timestamp(time);
		return timestamp;
	}
	
	public static void main(String[] args) {
		System.out.println(getTimeStamp(new Date()));
	}
	
	public static Timestamp getTimeStamp(Long time) {
		Timestamp date = new Timestamp(time);
		return date;
	}
	
    public static long getTimeMillis(String time) {
    	
        try { 
        	
            DateFormat dateFormat = new SimpleDateFormat("yy-MM-dd HH:mm:ss"); 
            
            DateFormat dayFormat = new SimpleDateFormat("yy-MM-dd");  
            
            Date curDate = dateFormat.parse(dayFormat.format(new Date()) + " " + time);
            
            return curDate.getTime();  
            
        } catch (ParseException e) {  
        	
            e.printStackTrace();  
            
        }  
        return 0;  
    }
}

