package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


public class Dealerships implements ResultSetParser<Dealerships> {
	protected Integer id;
    protected String name;
    protected String city;
    protected Integer stateId;
    protected Date created;
    protected String dealershipCode;
    protected String notes;
    
    @Override
	public List<Dealerships> parseResultSetAll(ResultSet resultSet) throws SQLException {
    	List<Dealerships> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<Dealerships> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Dealerships> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Dealerships parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Dealerships result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public Dealerships parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public Dealerships parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Dealerships response = new Dealerships();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setName(resultSet.getString(prefix + "name"));
		response.setCity(resultSet.getString(prefix + "city"));
		response.setStateId(resultSet.getInt(prefix + "stateId"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setDealershipCode(resultSet.getString(prefix + "dealershipCode"));
		response.setNotes(resultSet.getString(prefix + "notes"));
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
