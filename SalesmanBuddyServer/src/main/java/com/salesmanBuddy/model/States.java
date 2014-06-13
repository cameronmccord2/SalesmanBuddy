package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class States implements ResultSetParser<States> {
	protected Integer id;
    protected String name;
    protected Integer status;
    
    @Override
	public List<States> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<States> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<States> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<States> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public States parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		States result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public States parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public States parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		States response = new States();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setName(resultSet.getString(prefix + "name"));
		response.setStatus(resultSet.getInt(prefix + "status"));
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
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}




















