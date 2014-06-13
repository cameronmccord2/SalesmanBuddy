package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Buckets implements ResultSetParser<Buckets> {
	protected Integer id;
	protected Integer stateId;
	protected String name;
	protected Date created;
	
	@Override
	public List<Buckets> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<Buckets> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<Buckets> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Buckets> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Buckets parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Buckets result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public Buckets parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public Buckets parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Buckets response = new Buckets();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setStateId(resultSet.getInt(prefix + "stateId"));
		response.setName(resultSet.getString(prefix + "name"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		return response;
	}
	
	public static List<Buckets> parseResultSet(ResultSet resultSet){
    	try {
			return new Buckets().parseResultSetAll(resultSet);
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Buckets [id=");
		builder.append(id);
		builder.append(", stateId=");
		builder.append(stateId);
		builder.append(", name=");
		builder.append(name);
		builder.append(", created=");
		builder.append(created);
		builder.append("]");
		return builder.toString();
	}
}




















