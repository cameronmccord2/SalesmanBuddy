package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StateQuestionsWithResponses{
	// responses
	protected Integer responseId;
    protected Integer licenseId;
    protected Integer stateQuestionsSpecificsId;
    protected String responseText;
    protected Integer responseBool;
    // questions
    protected Integer questionId;
    protected Integer stateQuestionId;
    protected String questionText;
    protected Integer responseType;
    protected Integer questionOrder;
    
    public static ArrayList<StateQuestionsWithResponses> parseResultSet(ResultSet resultSet){
    	ArrayList<StateQuestionsWithResponses> responses = new ArrayList<StateQuestionsWithResponses>();
    	try{
			while(resultSet.next()){
				StateQuestionsWithResponses response = new StateQuestionsWithResponses();
				response.setResponseId(resultSet.getInt("responseId"));
				response.setLicenseId(resultSet.getInt("licenseId"));
				response.setStateQuestionsSpecificsId(resultSet.getInt("stateQuestionsSpecificsId"));
				response.setResponseText(resultSet.getString("responseText"));
				response.setResponseBool(resultSet.getInt("responseBool"));
				response.setQuestionId(resultSet.getInt("questionId"));
				response.setStateQuestionId(resultSet.getInt("stateQuestionId"));
				response.setQuestionText(resultSet.getString("questionText"));
				response.setResponseType(resultSet.getInt("responseType"));
				response.setQuestionOrder(resultSet.getInt("questionOrder"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }

	public Integer getResponseId() {
		return responseId;
	}

	public void setResponseId(Integer responseId) {
		this.responseId = responseId;
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

	public Integer getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public Integer getStateQuestionId() {
		return stateQuestionId;
	}

	public void setStateQuestionId(Integer stateQuestionId) {
		this.stateQuestionId = stateQuestionId;
	}

	public String getQuestionText() {
		return questionText;
	}

	public void setQuestionText(String questionText) {
		this.questionText = questionText;
	}

	public Integer getResponseType() {
		return responseType;
	}

	public void setResponseType(Integer responseType) {
		this.responseType = responseType;
	}

	public Integer getQuestionOrder() {
		return questionOrder;
	}

	public void setQuestionOrder(Integer questionOrder) {
		this.questionOrder = questionOrder;
	}
}
