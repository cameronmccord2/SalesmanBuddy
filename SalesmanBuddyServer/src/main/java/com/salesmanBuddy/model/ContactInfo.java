package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ContactInfo {
	protected Integer id;
	protected Integer userId;
	protected Integer licenseId;
	protected Date created;
	protected String firstName;
	protected String lastName;
	protected String email;
	protected String phoneNumber;
	protected String streetAddress;
	protected String city;
	protected Integer stateId;
	protected String notes;
	
	public String toString(){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("id:");
    	sb.append(this.id);
    	sb.append(", userId:");
    	sb.append(this.userId);
    	sb.append(", licenseId:");
    	sb.append(this.licenseId);
    	sb.append(", created:");
    	sb.append(this.created);
    	sb.append(", firstName:");
    	sb.append(this.firstName);
    	sb.append(", lastName:");
    	sb.append(this.lastName);
    	sb.append(", email:");
    	sb.append(this.email);
    	sb.append(", phoneNumber:");
    	sb.append(this.phoneNumber);
    	sb.append(", streetAddress:");
    	sb.append(this.streetAddress);
    	sb.append(", city:");
    	sb.append(this.city);
    	sb.append(", stateId:");
    	sb.append(this.stateId);
    	sb.append(", notes:");
    	sb.append(this.notes);
    	return sb.toString();
    }
	
	public static ArrayList<ContactInfo> parseResultSet(ResultSet resultSet){
    	ArrayList<ContactInfo> responses = new ArrayList<ContactInfo>();
    	try{
			while(resultSet.next()){
				ContactInfo response = new ContactInfo();
				response.setId(resultSet.getInt("id"));
				response.setUserId(resultSet.getInt("userId"));
				response.setLicenseId(resultSet.getInt("licenseId"));
				response.setFirstName(resultSet.getString("firstName"));
				response.setLastName(resultSet.getString("lastName"));
				response.setEmail(resultSet.getString("email"));
				response.setPhoneNumber(resultSet.getString("phoneNumber"));
				response.setStreetAddress(resultSet.getString("streetAddress"));
				response.setNotes(resultSet.getString("notes"));
				response.setCity(resultSet.getString("city"));
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
	public Integer getUserId() {
		return userId;
	}
	public void setUserId(Integer userId) {
		this.userId = userId;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getFirstName() {
		return firstName;
	}
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	public String getLastName() {
		return lastName;
	}
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getStreetAddress() {
		return streetAddress;
	}
	public void setStreetAddress(String streetAddress) {
		this.streetAddress = streetAddress;
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
	public String getNotes() {
		return notes;
	}
	public void setNotes(String notes) {
		this.notes = notes;
	}

	public Integer getLicenseId() {
		return licenseId;
	}

	public void setLicenseId(Integer licenseId) {
		this.licenseId = licenseId;
	}
}
