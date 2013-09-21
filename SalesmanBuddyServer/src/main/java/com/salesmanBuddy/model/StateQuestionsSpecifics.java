package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StateQuestionsSpecifics {
	protected Integer id;
    protected Integer stateQuestionId;
    protected String questionText;
    protected Integer responseType;
    protected Integer questionOrder;
    
    public static ArrayList<StateQuestionsSpecifics> parseResultSet(ResultSet resultSet){
    	ArrayList<StateQuestionsSpecifics> responses = new ArrayList<StateQuestionsSpecifics>();
    	try{
			while(resultSet.next()){
				StateQuestionsSpecifics response = new StateQuestionsSpecifics();
				response.setId(resultSet.getInt("id"));
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
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
