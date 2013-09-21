package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class StateQuestions {
	protected Integer id;
    protected Integer stateId;
    protected Date created;
    
    public static ArrayList<StateQuestions> parseResultSet(ResultSet resultSet){
    	ArrayList<StateQuestions> responses = new ArrayList<StateQuestions>();
    	try{
			while(resultSet.next()){
				StateQuestions response = new StateQuestions();
				response.setId(resultSet.getInt("id"));
				response.setStateId(resultSet.getInt("stateId"));
				response.setCreated(resultSet.getDate("created"));
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
	public Integer getStateId() {
		return stateId;
	}
	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
}
