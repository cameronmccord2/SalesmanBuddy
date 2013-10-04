package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class LicensesListElement{
	// licenses
	protected Integer id;
    protected String photo;
    protected Integer bucketId;
    protected Date created;
    protected ContactInfo contactInfo;
    //custom here
	protected ArrayList<StateQuestionsWithResponses> stateQuestions;
	
	public static ArrayList<LicensesListElement> parseResultSet(ResultSet resultSet){
    	ArrayList<LicensesListElement> responses = new ArrayList<LicensesListElement>();
    	try{
			while(resultSet.next()){
				LicensesListElement response = new LicensesListElement();
				response.setId(resultSet.getInt("id"));
				response.setPhoto(resultSet.getString("photo"));
				response.setBucketId(resultSet.getInt("bucketId"));
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

	
	public ArrayList<StateQuestionsWithResponses> getStateQuestions() {
		return stateQuestions;
	}

	public void setStateQuestions(
			ArrayList<StateQuestionsWithResponses> stateQuestions) {
		this.stateQuestions = stateQuestions;
	}

	public Integer getBucketId() {
		return bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public ContactInfo getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(ContactInfo contactInfo) {
		this.contactInfo = contactInfo;
	}
}
