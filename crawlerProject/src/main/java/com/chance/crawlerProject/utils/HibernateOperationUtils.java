package com.chance.crawlerProject.utils;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Query;
import org.hibernate.Session;


/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 15, 2018 11:47:41 AM
 * @version 1.0
 * 
 */
public class HibernateOperationUtils {

	public static void main(String[] args) {
		
//		List<Object> result = new ArrayList<Object>();
//		
//		result = searchObjectFromDBByCondition(TestCar.class, "where id > 1");
//		
//		if (result.get(0) instanceof String) {
//			TestCar info = (TestCar) result.get(0);
//			System.out.println(info.getAutohomeid());
//		} else {
//			System.out.println("ERROR");
//		}

	}
	
	public static List<Object> searchObjectFromDB(Class<?> objectClass) {
		
        return searchObjectFromDBByCondition(objectClass, null);
	}

	public static List<Object> searchObjectFromDBByCondition(Class<?> objectClass, String condition) {
		
		Session session = null; 
		try {
			session = HibernateUtils.getSession();
	        session.beginTransaction();  
	        String hql = " from " + objectClass.getSimpleName();
	        if (!StringTools.isBlank(condition)) {
	        	hql += " " + condition;
	        }
	        Query query = session.createQuery(hql);
	        List<Object> result = new ArrayList<Object>();
	        result = query.list();
	        session.getTransaction().commit();
	        return result;
		} catch (Exception e) {  
            e.printStackTrace();  
            session.getTransaction().rollback();  
        } finally {  
            HibernateUtils.closeSession(session);  
        }
		return null;
	}
	
	public static void saveObejct(Object object){
		Session session = null; 
		try {
			session = HibernateUtils.getSession();
			session.beginTransaction();
	        session.save(object);
	        session.getTransaction().commit();
		} catch (Exception e) {  
            e.printStackTrace();  
            session.getTransaction().rollback();  
        } finally {  
            HibernateUtils.closeSession(session);  
        }
	}
	
	public static void updateObejct(Object object){
		Session session = null; 
		try {
			session = HibernateUtils.getSession();
			session.beginTransaction();
	        session.update(object);
	        session.getTransaction().commit();
	        session.close();
		} catch (Exception e) {  
            e.printStackTrace();  
            session.getTransaction().rollback();  
        } finally {  
            HibernateUtils.closeSession(session);  
        }
	}
	
	public static void deleteObejct(Object object){
		Session session = null; 
		try {
			session = HibernateUtils.getSession();
			session.beginTransaction();
	        session.delete(object);
	        session.getTransaction().commit();
	        session.close();
		} catch (Exception e) {  
            e.printStackTrace();  
            session.getTransaction().rollback();  
        } finally {  
            HibernateUtils.closeSession(session);  
        }
	}
}

