package com.chance.crawlerProject;


import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.chance.crawlerProject.utils.HtmlCleanerUtils;
import com.chance.crawlerProject.utils.RequestUtils;


/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 11, 2018 12:07:56 PM
 * @version 1.0
 * 
 */
public class HibernateTest {
	
//    public static Configuration cfg;
//    public static SessionFactory sessionFactory;
//    static{
//        //启动原本设定好的配置管理文件
//        cfg=new Configuration().configure();
//        //建立会话工厂用来产生会话，工厂可以只有一个
//        StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()  
//                .applySettings(cfg.getProperties()).build();  
//        sessionFactory=cfg.buildSessionFactory(serviceRegistry);
//        
//    }
//    public static Session openSessons(){
//        return sessionFactory.openSession();
//    }
//
//    public static Transaction beginTransaction(Session session){
//        return session.beginTransaction();
//    }
//	
	private static Logger logger = LoggerFactory.getLogger(HibernateTest.class);
	
    public static void main(String[] args) {
    	
    	String url = "https://i.autohome.com.cn/ajax/home/GetUserInfo?r=0.17298081138793964&userid=6844490&_=1515555304241";
    	
    	String result = HtmlCleanerUtils.getHtmlByUrl(url, RequestUtils.getHeaderWithReferer("https://i.autohome.com.cn"), null, false);
    	System.out.println(result);
	}
}

