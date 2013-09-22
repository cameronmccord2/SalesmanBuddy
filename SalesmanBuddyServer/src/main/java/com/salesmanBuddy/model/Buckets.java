package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Buckets {
	protected Integer id;
	protected Integer stateId;
	protected String name;
	protected Date created;
	
	public static ArrayList<Buckets> parseResultSet(ResultSet resultSet){
    	ArrayList<Buckets> responses = new ArrayList<Buckets>();
    	try{
			while(resultSet.next()){
				Buckets response = new Buckets();
				response.setId(resultSet.getInt("id"));
				response.setStateId(resultSet.getInt("stateId"));
				response.setName(resultSet.getString("name"));
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
}
