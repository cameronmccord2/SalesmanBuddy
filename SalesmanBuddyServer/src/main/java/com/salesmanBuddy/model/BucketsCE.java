package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class BucketsCE {
	protected Integer id;
	protected String name;
	protected Date created;
	
	public static ArrayList<BucketsCE> parseResultSet(ResultSet resultSet){
    	ArrayList<BucketsCE> responses = new ArrayList<BucketsCE>();
    	try{
			while(resultSet.next()){
				BucketsCE response = new BucketsCE();
				response.setId(resultSet.getInt("id"));
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("BucketsCE [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", created=");
		builder.append(created);
		builder.append("]");
		return builder.toString();
	}
}
