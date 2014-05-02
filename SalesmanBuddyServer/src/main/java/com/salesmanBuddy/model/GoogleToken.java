package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

public class GoogleToken {

	protected Integer id;
	protected Integer userId;
	protected String token;
	protected Date created;
	protected long expiresAt;
	protected Integer type;
	
	public static GoogleToken parseOneRowResultSet(ResultSet resultSet) throws SQLException {
		GoogleToken result = null;
		if(resultSet.next()){
			result = new GoogleToken();
			result.setId(resultSet.getInt("id"));
			result.setUserId(resultSet.getInt("userId"));
			result.setToken(resultSet.getString("token"));
			result.setCreated(resultSet.getDate("created"));
			result.setExpiresAt(resultSet.getLong("expiresAt"));
			result.setType(resultSet.getInt("type"));
		}
		return result;
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
