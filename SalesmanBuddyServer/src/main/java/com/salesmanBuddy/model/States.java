package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class States {
	protected Integer id;
    protected String name;
    protected Integer status;
    
    public static ArrayList<States> parseResultSet(ResultSet resultSet){
    	ArrayList<States> states = new ArrayList<States>();
    	try{
			while(resultSet.next()){
				States state = new States();
				state.setId(resultSet.getInt("id"));
				state.setName(resultSet.getString("name"));
				state.setStatus(resultSet.getInt("status"));
				states.add(state);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return states;
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}
