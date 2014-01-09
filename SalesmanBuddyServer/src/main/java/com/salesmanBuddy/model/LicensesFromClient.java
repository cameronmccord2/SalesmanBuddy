package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LicensesFromClient {
	// Licenses
	protected Integer id;
    protected Integer showInUserList;
    protected Integer stateId;
    protected Date created;
    protected float longitude;
    protected float latitude;
    protected Integer userId;
    
    // custom here
    protected ArrayList<QuestionsAndAnswers> qaas;
    
    public static ArrayList<LicensesFromClient> parseResultSet(ResultSet resultSet){
    	ArrayList<LicensesFromClient> responses = new ArrayList<LicensesFromClient>();
    	try{
			while(resultSet.next()){
				LicensesFromClient response = new LicensesFromClient();
				response.setId(resultSet.getInt("id"));
				response.setStateId(resultSet.getInt("stateId"));
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
    
    public String toString(){
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.id);
    	sb.append(" ");
    	sb.append(this.stateId);
    	sb.append(" ");
    	sb.append(this.created);
    	sb.append(" ");
    	sb.append(this.longitude);
    	sb.append(" ");
    	sb.append(this.latitude);
    	sb.append(" ");
    	sb.append(this.userId);
    	sb.append(" ");
    	return sb.toString();
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

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	public ArrayList<QuestionsAndAnswers> getQaas() {
		return qaas;
	}

	public void setQaas(ArrayList<QuestionsAndAnswers> qaas) {
		this.qaas = qaas;
	}
}
