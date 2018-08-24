package com.chance.crawlerProject.utils;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 15, 2018 11:43:40 AM
 * @version 1.0
 * 
 */
public class HibernateUtils {
	public static Configuration cfg;
	public static SessionFactory sessionFactory;
  
	static {
		//启动原本设定好的配置管理文件
		cfg=new Configuration().configure();
		//建立会话工厂用来产生会话，工厂可以只有一个
	    StandardServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
	    		.applySettings(cfg.getProperties()).build();
	    sessionFactory=cfg.buildSessionFactory(serviceRegistry);
	}
	
    //方法返回session
    public static Session getSession(){
        return sessionFactory.openSession();
    }
    
    //关闭Session
    
    public static void closeSession(Session session){
        if (session != null) {  
            if (session.isOpen()) {  
                session.close();  
            }  
        }
    }
	
}

