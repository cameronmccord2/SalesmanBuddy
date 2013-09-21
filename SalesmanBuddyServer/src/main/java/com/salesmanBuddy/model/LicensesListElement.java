package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LicensesListElement{
	// licenses
	protected Integer id;
    protected Integer showInUserList;
    protected String photo;
    protected String bucket;
    protected Date created;
    protected float longitude;
    protected float latitude;
    protected Integer userId;
    
    //custom here
	protected ArrayList<StateQuestionsWithResponses> stateQuestions;
	
	public static ArrayList<LicensesListElement> parseResultSet(ResultSet resultSet){
    	ArrayList<LicensesListElement> responses = new ArrayList<LicensesListElement>();
    	try{
			while(resultSet.next()){
				LicensesListElement response = new LicensesListElement();
				response.setId(resultSet.getInt("id"));
				response.setPhoto(resultSet.getString("photo"));
				response.setBucket(resultSet.getString("bucket"));
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

	public String getBucket() {
		return bucket;
	}

	public void setBucket(String bucket) {
		this.bucket = bucket;
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

	public ArrayList<StateQuestionsWithResponses> getStateQuestions() {
		return stateQuestions;
	}

	public void setStateQuestions(
			ArrayList<StateQuestionsWithResponses> stateQuestions) {
		this.stateQuestions = stateQuestions;
	}
}
