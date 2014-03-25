package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Dealerships {
	protected Integer id;
    protected String name;
    protected String city;
    protected Integer stateId;
    protected Date created;
    protected String dealershipCode;
    protected String notes;
    
    public static ArrayList<Dealerships> parseResultSet(ResultSet resultSet) throws SQLException{
    	ArrayList<Dealerships> responses = new ArrayList<Dealerships>();
		while(resultSet.next()){
			Dealerships response = new Dealerships();
			response.setId(resultSet.getInt("id"));
			response.setName(resultSet.getString("name"));
			response.setCity(resultSet.getString("city"));
			response.setStateId(resultSet.getInt("stateId"));
			response.setCreated(resultSet.getDate("created"));
			response.setDealershipCode(resultSet.getString("dealershipCode"));
			response.setNotes(resultSet.getString("notes"));
			responses.add(response);
		}
    	return responses;
    }
    
    public static Dealerships parseOneRowResultSet(ResultSet resultSet) throws SQLException {
		Dealerships response = null;
		while(resultSet.next()){
			response = new Dealerships();
			response.setId(resultSet.getInt("id"));
			response.setName(resultSet.getString("name"));
			response.setCity(resultSet.getString("city"));
			response.setStateId(resultSet.getInt("stateId"));
			response.setCreated(resultSet.getDate("created"));
			response.setDealershipCode(resultSet.getString("dealershipCode"));
			response.setNotes(resultSet.getString("notes"));
			break;
		}
		return response;
	}
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCity() {
		return city;
	}
	public void setCity(String city) {
		this.city = city;
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

	public String getDealershipCode() {
		return dealershipCode;
	}

	public void setDealershipCode(String dealershipCode) {
		this.dealershipCode = dealershipCode;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}
}
