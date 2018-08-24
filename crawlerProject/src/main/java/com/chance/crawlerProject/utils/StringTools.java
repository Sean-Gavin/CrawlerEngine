package com.chance.crawlerProject.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

/** 
 * 
 * @author Sean
 * @date 创建时间：Dec 12, 2017 11:58:38 AM
 * @version 1.0
 * 
 */
public class StringTools extends StringUtils{

	public static String matcherStrByRegular(String input, String regex) {
		
		if (isBlank(regex) || isBlank(input) ) {
			return "";
		}
		
		try {
		    Pattern pattern = Pattern.compile(regex);
		    Matcher matcher = pattern.matcher(input);
	        while (matcher.find()) {
	        	
	            return matcher.group();
	        }
		} catch (Exception e) {
			
			return "";
		}
		return "";
	}

	public static String removeUselessChar(String str) {
		
		String result = str.replaceAll("&nbsp;", "")
						.replaceAll("&nbsp", "")
						.replaceAll("\\s", "");
		return result.trim();
	}
	
	public static String deleteStartAndEndChar(String str) {
		String result = str.substring(1, str.length() - 1);
		
		return result.trim();
	}
	
	public static int parseIntFromStr(String input) {
		int result = 0;
		
		if (!isBlank(input)) {
			result = Integer.parseInt(input);
		}
		return result;
	}

	public static String formatStr(String content) {
		content = content.replaceAll("\\\\", "")
					.replaceAll("'", "\\\"")
					.replaceAll("\"", "\\\"");
		return content;
	}
	
	public static String matcherAllStrByRegular(String input, String regex) {
		
		if (isBlank(regex) || isBlank(input) ) {
			return "";
		}
		StringBuffer resultSB = new StringBuffer();
		try {
		    Pattern pattern = Pattern.compile(regex);
		    Matcher matcher = pattern.matcher(input);
	        while (matcher.find()) {
	        	resultSB.append(matcher.group());
	        }
	        return resultSB.toString();
		} catch (Exception e) {
			
			return "";
		}
	}
	
	public static String matcherChineseFromStr(String input) {
		if (isBlank(input) ) {
			return "";
		}
		
		String result = matcherAllStrByRegular(input, "[\u4E00-\u9FA5]");
		
		return result;
	}
}

