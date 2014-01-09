package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LicensesListElement{
	// licenses
	protected Integer id;
    protected Date created;
    protected Integer stateId;
    //custom here
    protected ArrayList<QuestionsAndAnswers> qaas;
	
	public static ArrayList<LicensesListElement> parseResultSet(ResultSet resultSet){
    	ArrayList<LicensesListElement> responses = new ArrayList<LicensesListElement>();
    	try{
			while(resultSet.next()){
				LicensesListElement response = new LicensesListElement();
				response.setId(resultSet.getInt("id"));
				response.setCreated(resultSet.getDate("created"));
				response.setStateId(resultSet.getInt("stateId"));
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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public ArrayList<QuestionsAndAnswers> getQaas() {
		return qaas;
	}

	public void setQaas(ArrayList<QuestionsAndAnswers> qaas) {
		this.qaas = qaas;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
}
