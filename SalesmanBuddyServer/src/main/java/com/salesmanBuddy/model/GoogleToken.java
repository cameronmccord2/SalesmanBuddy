package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GoogleToken implements ResultSetParser<GoogleToken> {

	protected Integer id;
	protected Integer userId;
	protected String token;
	protected Date created;
	protected long expiresAt;
	protected Integer type;
	
	@Override
	public List<GoogleToken> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<GoogleToken> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<GoogleToken> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<GoogleToken> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public GoogleToken parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		GoogleToken result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public GoogleToken parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public GoogleToken parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		GoogleToken result = new GoogleToken();
		result.setId(resultSet.getInt(prefix + "id"));
		result.setUserId(resultSet.getInt(prefix + "userId"));
		result.setToken(resultSet.getString(prefix + "token"));
		result.setCreated(resultSet.getDate(prefix + "created"));
		result.setExpiresAt(resultSet.getLong(prefix + "expiresAt"));
		result.setType(resultSet.getInt(prefix + "type"));
		return result;
	}
	
	public static GoogleToken parseOneRowResultSet(ResultSet resultSet) throws SQLException {
		return new GoogleToken().parseResultSetOneRow(resultSet);
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
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public long getExpiresAt() {
		return expiresAt;
	}
	public void setExpiresAt(long expiresAt) {
		this.expiresAt = expiresAt;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
}
