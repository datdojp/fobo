package com.forboss.data.model;

import java.io.Serializable;
import java.util.Date;

import com.j256.ormlite.field.DatabaseField;

public class Article implements Serializable {
	@DatabaseField(id=true)
	private String id;
	
	@DatabaseField
	private String category;
	
	@DatabaseField
	private String title;
	
	@DatabaseField
	private String content;
	
	@DatabaseField
	private Integer nLikes;
	
	@DatabaseField
	private Integer nReads;
	
	@DatabaseField
	private Date dateTime;
	
	@DatabaseField
	private Boolean isLike;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Integer getnLikes() {
		return nLikes;
	}

	public void setnLikes(Integer nLikes) {
		this.nLikes = nLikes;
	}

	public Integer getnReads() {
		return nReads;
	}

	public void setnReads(Integer nReads) {
		this.nReads = nReads;
	}

	public Date getDateTime() {
		return dateTime;
	}

	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}

	public Boolean getIsLike() {
		return isLike;
	}

	public void setIsLike(Boolean isLike) {
		this.isLike = isLike;
	}
}
