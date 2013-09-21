package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StateQuestionsResponses {
	protected Integer id;
    protected Integer licenseId;
    protected Integer stateQuestionsSpecificsId;
    protected String responseText;
    protected Integer responseBool;
    
    public static ArrayList<StateQuestionsResponses> parseResultSet(ResultSet resultSet){
    	ArrayList<StateQuestionsResponses> responses = new ArrayList<StateQuestionsResponses>();
    	try{
			while(resultSet.next()){
				StateQuestionsResponses response = new StateQuestionsResponses();
				response.setId(resultSet.getInt("id"));
				response.setLicenseId(resultSet.getInt("licenseId"));
				response.setStateQuestionsSpecificsId(resultSet.getInt("stateQuestionsSpecificsId"));
				response.setResponseText(resultSet.getString("responseText"));
				response.setResponseBool(resultSet.getInt("responseBool"));
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
	public Integer getLicenseId() {
		return licenseId;
	}
	public void setLicenseId(Integer licenseId) {
		this.licenseId = licenseId;
	}
	public Integer getStateQuestionsSpecificsId() {
		return stateQuestionsSpecificsId;
	}
	public void setStateQuestionsSpecificsId(Integer stateQuestionsSpecificsId) {
		this.stateQuestionsSpecificsId = stateQuestionsSpecificsId;
	}
	public String getResponseText() {
		return responseText;
	}
	public void setResponseText(String responseText) {
		this.responseText = responseText;
	}
	public Integer getResponseBool() {
		return responseBool;
	}
	public void setResponseBool(Integer responseBool) {
		this.responseBool = responseBool;
	}
}
