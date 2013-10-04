package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LicensesFromClient {
	// Licenses
	protected Integer id;
    protected Integer showInUserList;
    protected String photo;
    protected Integer stateId;
    protected Date created;
    protected float longitude;
    protected float latitude;
    protected Integer userId;
    protected ContactInfo contactInfo;
    
    // custom here
    protected ArrayList<StateQuestionsResponses> stateQuestionsResponses;
    
    public static ArrayList<LicensesFromClient> parseResultSet(ResultSet resultSet, ResultSet stateQuestionsResponsesResultSet){
    	ArrayList<LicensesFromClient> responses = new ArrayList<LicensesFromClient>();
    	try{
			while(resultSet.next()){
				LicensesFromClient response = new LicensesFromClient();
				response.setId(resultSet.getInt("id"));
				response.setPhoto(resultSet.getString("photo"));
				response.setStateId(resultSet.getInt("stateId"));
				response.setCreated(resultSet.getDate("created"));
				response.setLongitude(resultSet.getFloat("longitude"));
				response.setLatitude(resultSet.getFloat("latitude"));
				response.setUserId(resultSet.getInt("userId"));
				response.setStateQuestionsResponses(StateQuestionsResponses.parseResultSet(stateQuestionsResponsesResultSet));
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

	public ArrayList<StateQuestionsResponses> getStateQuestionsResponses() {
		return stateQuestionsResponses;
	}

	public void setStateQuestionsResponses(
			ArrayList<StateQuestionsResponses> stateQuestionsResponses) {
		this.stateQuestionsResponses = stateQuestionsResponses;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	public ContactInfo getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(ContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}
}
