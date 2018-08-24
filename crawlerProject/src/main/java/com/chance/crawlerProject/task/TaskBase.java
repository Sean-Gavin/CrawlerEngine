package com.chance.crawlerProject.task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.chance.crawlerProject.autohome.constant.TaskKey;

/** 
 * 
 * @author Sean
 * @date 创建时间：Jan 15, 2018 3:50:54 PM
 * @version 1.0
 * 
 */
public class TaskBase extends TimerTask {

	private long startTime;
	private long endTime;
	
	public TaskBase() {
		
		this.startTime = System.currentTimeMillis();
	}	
	
	static Map<TaskKey, List<Object>> taskMap = new HashMap<TaskKey, List<Object>>();
	static Map<TaskKey, Integer> keyMap = new HashMap<TaskKey, Integer>();
	
	public static synchronized Object getTask(TaskKey key) {
		if (null != taskMap && !taskMap.isEmpty() && taskMap.containsKey(key)
				&& null != keyMap && !keyMap.isEmpty() && keyMap.containsKey(key)) {
			
			List<Object> tempList = taskMap.get(key);
			int keyIndex = keyMap.get(key);
			
			if (null != tempList && tempList.size() > 0 && (keyIndex <= tempList.size() - 1)) {
				Object tempObj = tempList.get(keyIndex);
				keyIndex ++;
				keyMap.put(key, keyIndex);
				return tempObj;
			}
		}
		return null;
	}
	
	public static synchronized void addTask(Object obj, TaskKey key) {
		if (null != taskMap && !taskMap.isEmpty() && taskMap.containsKey(key)) {
			
			List<Object> tempList = taskMap.get(key);
			tempList.add(obj);
		} else {
			List<Object> tempList = new ArrayList<Object>();
			tempList.add(obj);
			taskMap.put(key, tempList);
			keyMap.put(key, 0);
		}
	}
	
	public static synchronized void addTask(List<Object> objList, TaskKey key) {
		if (null != taskMap && !taskMap.isEmpty() && taskMap.containsKey(key)) {
			
			List<Object> tempList = taskMap.get(key);
			tempList.addAll(objList);
		} else {
			taskMap.put(key, objList);
			keyMap.put(key, 0);
		}
	}
	
	public static synchronized void initTask(TaskKey key) {
		keyMap.put(key, 0);
	}
	
	/**
	 * 重置任务进度
	 */
	public static synchronized void initAllTask() {
		for (TaskKey key : TaskKey.values()) {
			keyMap.put(key, 0);
		}
	}
	
	public static synchronized void clearTask(TaskKey key) {
		List<Object> tempList = new ArrayList<Object>();
		taskMap.put(key, tempList);
	}
	
	/**
	 * 清除所有的任务列表
	 */
	public static synchronized void clearAllTask() {
		for (TaskKey key : TaskKey.values()) {
			List<Object> tempList = new ArrayList<Object>();
			taskMap.put(key, tempList);
			keyMap.put(key, 0);
		}
	}

	@Override
	public void run() {
		
	}
	
	public void initStartTime() {
		this.startTime = System.currentTimeMillis();
	}
	
	public long getTaskCostTime() {
		this.endTime = System.currentTimeMillis();
		return (this.endTime - this.startTime)/1000;
	}
}

