package com.chance.crawlerProject.utils;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;


/** 
 * 
 * @author Sean
 * @date 创建时间：Nov 6, 2017 6:10:00 PM
 * @version 1.0
 * 
 */
public class MysqlConnectionManager {
	

    private static Connection conn = null;
    
    private final static String DB_URL = "jdbc:mysql://localhost:3306/sean?useUnicode=true&characterEncoding=utf-8";
    private final static String USER = "root";
    private final static String PASSWORD = "123456";
    
    
    public static void main(String[] args) {
		
    	String filePath = "C:/Users/Jack/Desktop/test.txt";
    	String sql = "select keyname from asm_keywords_dict";
//    	FileOutputStream out = null;
    	FileWriter fw = null;
    	Connection conn = null;
    	try {
    		fw = new FileWriter(filePath);
    		conn = getConnection();
			Statement stmt = conn.createStatement();
			ResultSet rs = stmt.executeQuery(sql);
			while (rs.next()) {
				String keyword = rs.getString("keyname");
				StringBuffer infoSB = new StringBuffer();
				infoSB.append(keyword).append("\t").append("n").append("\t").append("1").append("\n");
				fw.write(infoSB.toString());
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				fw.close();
				conn.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
    	
	}
    
    
    
    
    
	public static Connection getConnection() {
        try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = DriverManager.getConnection(DB_URL,USER,PASSWORD);
			
			return conn;
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
	}
}

