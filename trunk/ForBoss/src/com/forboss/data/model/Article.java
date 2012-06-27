package com.forboss.data.model;

import java.io.Serializable;

import com.google.gson.annotations.SerializedName;
import com.j256.ormlite.field.DatabaseField;

public class Article implements Serializable {
	@SerializedName("ID")
	@DatabaseField(id=true)
	private String id;

	@SerializedName("Title")
	@DatabaseField
	private String title;

	@SerializedName("Thumbnail")
	@DatabaseField
	private String thumbnail;

	@SerializedName("Body")
	@DatabaseField
	private String body;
	
	@DatabaseField
	private String htmlContent;

	@SerializedName("Category")
	@DatabaseField
	private String category;

	@SerializedName("Views")
	@DatabaseField
	private int views;

	@SerializedName("Likes")
	@DatabaseField
	private int likes;

	@SerializedName("Link")
	@DatabaseField
	private String link;

	@SerializedName("CreatedTime")
	@DatabaseField
	private long createdTime;
	
	@SerializedName("EventTime")
	@DatabaseField
	private String eventTime;
	
	@SerializedName("EventPlace")
	@DatabaseField
	private String eventPlace;

	@DatabaseField
	private boolean isLike;

	@DatabaseField
	private boolean isView;

	@DatabaseField
	private String pictureLocation;

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

	public String getThumbnail() {
		return thumbnail;
	}

	public void setThumbnail(String thumbnail) {
		this.thumbnail = thumbnail;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public int getViews() {
		return views;
	}

	public void setViews(int views) {
		this.views = views;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public long getCreatedTime() {
		return createdTime;
	}

	public void setCreatedTime(long createdTime) {
		this.createdTime = createdTime;
	}


	public String getPictureLocation() {
		return pictureLocation;
	}

	public void setPictureLocation(String pictureLocation) {
		this.pictureLocation = pictureLocation;
	}

	public boolean isLike() {
		return isLike;
	}

	public void setLike(boolean isLike) {
		this.isLike = isLike;
	}

	public boolean isView() {
		return isView;
	}

	public void setView(boolean isView) {
		this.isView = isView;
	}

	public String getEventTime() {
		return eventTime;
	}

	public void setEventTime(String eventTime) {
		this.eventTime = eventTime;
	}

	public String getEventPlace() {
		return eventPlace;
	}

	public void setEventPlace(String eventPlace) {
		this.eventPlace = eventPlace;
	}

	public String getHtmlContent() {
		return htmlContent;
	}

	public void setHtmlContent(String htmlContent) {
		this.htmlContent = htmlContent;
	}
	
	
}
