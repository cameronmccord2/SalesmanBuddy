package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Users {
	protected Integer id;
    protected Integer dealershipId;
    protected Integer deviceType;
    protected Integer type;
    protected Date created;
    protected String googleUserId;
    
    public static ArrayList<Users> parseResultSet(ResultSet resultSet){
    	ArrayList<Users> responses = new ArrayList<Users>();
    	try{
			while(resultSet.next()){
				Users response = new Users();
				response.setCreated(resultSet.getDate("created"));
				response.setId(resultSet.getInt("id"));
				response.setDealershipId(resultSet.getInt("dealershipId"));
				response.setDeviceType(resultSet.getInt("deviceType"));
				response.setType(resultSet.getInt("type"));
				response.setGoogleUserId(resultSet.getString("googleUserId"));
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
	public Integer getDealershipId() {
		return dealershipId;
	}
	public void setDealershipId(Integer dealershipId) {
		this.dealershipId = dealershipId;
	}
	public Integer getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}


	public String getGoogleUserId() {
		return googleUserId;
	}


	public void setGoogleUserId(String googleUserId) {
		this.googleUserId = googleUserId;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Users [id=");
		builder.append(id);
		builder.append(", dealershipId=");
		builder.append(dealershipId);
		builder.append(", deviceType=");
		builder.append(deviceType);
		builder.append(", type=");
		builder.append(type);
		builder.append(", created=");
		builder.append(created);
		builder.append(", googleUserId=");
		builder.append(googleUserId);
		builder.append("]");
		return builder.toString();
	}
}
