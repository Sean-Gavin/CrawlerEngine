package com.chance.crawlerProject.autohome.bean;
// Generated 2018-1-12 17:58:33 by Hibernate Tools 4.3.1.Final

import java.util.Date;

/**
 * NewsComment generated by hbm2java
 */
public class NewsComment implements java.io.Serializable {

	private Integer id;
	private Integer newsId;
	private Integer autoUserId;
	private Integer upNumber;
	private String deviceType;
	private Integer commentFloor;
	private Integer autoCommentId;
	private Integer replyAutoLevelId;
	private String commentContent;
	private Date commentTime;

	public NewsComment() {
	}

	public NewsComment(Integer newsId, Integer autoUserId, Integer upNumber, String deviceType, Integer commentFloor,
			Integer autoCommentId, Integer replyAutoLevelId, String commentContent, Date commentTime) {
		this.newsId = newsId;
		this.autoUserId = autoUserId;
		this.upNumber = upNumber;
		this.deviceType = deviceType;
		this.commentFloor = commentFloor;
		this.autoCommentId = autoCommentId;
		this.replyAutoLevelId = replyAutoLevelId;
		this.commentContent = commentContent;
		this.commentTime = commentTime;
	}

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getNewsId() {
		return this.newsId;
	}

	public void setNewsId(Integer newsId) {
		this.newsId = newsId;
	}

	public Integer getAutoUserId() {
		return this.autoUserId;
	}

	public void setAutoUserId(Integer autoUserId) {
		this.autoUserId = autoUserId;
	}

	public Integer getUpNumber() {
		return this.upNumber;
	}

	public void setUpNumber(Integer upNumber) {
		this.upNumber = upNumber;
	}

	public String getDeviceType() {
		return this.deviceType;
	}

	public void setDeviceType(String deviceType) {
		this.deviceType = deviceType;
	}

	public Integer getCommentFloor() {
		return this.commentFloor;
	}

	public void setCommentFloor(Integer commentFloor) {
		this.commentFloor = commentFloor;
	}

	public Integer getAutoCommentId() {
		return this.autoCommentId;
	}

	public void setAutoCommentId(Integer autoCommentId) {
		this.autoCommentId = autoCommentId;
	}

	public Integer getReplyAutoLevelId() {
		return this.replyAutoLevelId;
	}

	public void setReplyAutoLevelId(Integer replyAutoLevelId) {
		this.replyAutoLevelId = replyAutoLevelId;
	}

	public String getCommentContent() {
		return this.commentContent;
	}

	public void setCommentContent(String commentContent) {
		this.commentContent = commentContent;
	}

	public Date getCommentTime() {
		return this.commentTime;
	}

	public void setCommentTime(Date commentTime) {
		this.commentTime = commentTime;
	}

}
