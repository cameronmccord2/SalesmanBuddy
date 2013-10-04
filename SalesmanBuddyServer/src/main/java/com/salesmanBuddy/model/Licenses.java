package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Licenses {
	protected Integer id;
    protected Integer showInUserList;
    protected String photo;
    protected Integer bucketId;
    protected Date created;
    protected float longitude;
    protected float latitude;
    protected Integer userId;
    protected ContactInfo contactInfo;
    
    public String toString(){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("id:");
    	sb.append(this.id);
    	sb.append(", showInUserList:");
    	sb.append(this.showInUserList);
    	sb.append(", photo:");
    	sb.append(this.photo);
    	sb.append(", bucketId:");
    	sb.append(this.bucketId);
    	sb.append(", created:");
    	sb.append(this.created);
    	sb.append(", longitude:");
    	sb.append(this.longitude);
    	sb.append(", latitude:");
    	sb.append(this.latitude);
    	sb.append(", userId:");
    	sb.append(this.userId);
    	sb.append(", contactInfo:{");
    	sb.append(this.contactInfo.toString());
    	sb.append("}");
    	return sb.toString();
    }
    
    public static ArrayList<Licenses> parseResultSet(ResultSet resultSet){
    	ArrayList<Licenses> responses = new ArrayList<Licenses>();
    	try{
			while(resultSet.next()){
				Licenses response = new Licenses();
				response.setId(resultSet.getInt("id"));
				response.setPhoto(resultSet.getString("photo"));
				response.setBucketId(resultSet.getInt("bucketId"));
				response.setCreated(resultSet.getDate("created"));
				response.setLongitude(resultSet.getFloat("longitude"));
				response.setLatitude(resultSet.getFloat("latitude"));
				response.setUserId(resultSet.getInt("userId"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getShowInUserList() {
		return showInUserList;
	}
	public void setShowInUserList(Integer showInUserList) {
		this.showInUserList = showInUserList;
	}
	public String getPhoto() {
		return photo;
	}
	public void setPhoto(String photo) {
		this.photo = photo;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public float getLongitude() {
		return longitude;
	}
	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}
	public float getLatitude() {
		return latitude;
	}
	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	public Integer getBucketId() {
		return bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public ContactInfo getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(ContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}
}
