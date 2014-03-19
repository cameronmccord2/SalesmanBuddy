package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ImageDetails {
	protected Integer id;
	protected String photoName;
    protected Integer bucketId;
    protected Date created;
    protected Integer answerId;
    
    public static ArrayList<ImageDetails> parseResultSet(ResultSet resultSet){
    	ArrayList<ImageDetails> responses = new ArrayList<ImageDetails>();
    	try{
			while(resultSet.next()){
				ImageDetails response = new ImageDetails();
				response.setId(resultSet.getInt("id"));
				response.setPhotoName(resultSet.getString("photoName"));
				response.setBucketId(resultSet.getInt("bucketId"));
				response.setCreated(resultSet.getDate("created"));
				response.setAnswerId(resultSet.getInt("answerId"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }

	public Integer getBucketId() {
		return bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public Integer getAnswerId() {
		return answerId;
	}

	public void setAnswerId(Integer answerId) {
		this.answerId = answerId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageDetails [id=");
		builder.append(id);
		builder.append(", photoName=");
		builder.append(photoName);
		builder.append(", bucketId=");
		builder.append(bucketId);
		builder.append(", created=");
		builder.append(created);
		builder.append(", answerId=");
		builder.append(answerId);
		builder.append("]");
		return builder.toString();
	}
}
