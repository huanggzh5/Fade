package com.sysu.pro.fade.beans;

import java.io.Serializable;

public class SecondComment implements Serializable{
	private static final long serialVersionUID = -4285300475259012502L;
	//二级评论
	private Integer second_id;
	private Integer user_id;
	private String nickname;
	private Integer comment_id;
	private Integer to_user_id;
	private String to_nickname;
	private String comment_time;
	private String comment_content;
	private Integer note_id;

	private Integer first_id;//一级评论者的id, 前端发给后端的
	
	public Integer getSecond_id() {
		return second_id;
	}
	public void setSecond_id(Integer second_id) {
		this.second_id = second_id;
	}
	public Integer getUser_id() {
		return user_id;
	}
	public void setUser_id(Integer user_id) {
		this.user_id = user_id;
	}
	public String getNickname() {
		return nickname;
	}
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}
	public Integer getComment_id() {
		return comment_id;
	}
	public void setComment_id(Integer comment_id) {
		this.comment_id = comment_id;
	}
	public Integer getTo_user_id() {
		return to_user_id;
	}
	public void setTo_user_id(Integer to_user_id) {
		this.to_user_id = to_user_id;
	}
	public String getTo_nickname() {
		return to_nickname;
	}
	public void setTo_nickname(String to_nickname) {
		this.to_nickname = to_nickname;
	}
	public String getComment_time() {
		return comment_time;
	}
	public void setComment_time(String comment_time) {
		this.comment_time = comment_time;
	}
	public String getComment_content() {
		return comment_content;
	}
	public void setComment_content(String comment_content) {
		this.comment_content = comment_content;
	}
	public Integer getNote_id() {
		return note_id;
	}
	public void setNote_id(Integer note_id) {
		this.note_id = note_id;
	}

	public Integer getFirst_id() {
		return first_id;
	}

	public void setFirst_id(Integer first_id) {
		this.first_id = first_id;
	}
}
