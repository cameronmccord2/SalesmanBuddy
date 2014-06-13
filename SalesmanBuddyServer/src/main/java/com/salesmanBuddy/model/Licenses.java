package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Licenses implements ResultSetParser<Licenses> {
	
	protected Integer id;
	protected Integer showInUserList;
	protected float longitude;
	protected float latitude;
	protected Integer userId;
	protected Integer stateId;
	protected Date created;

	public Licenses(LicensesFromClient lfc) {
		this.setLatitude(lfc.getLatitude());
		this.setLongitude(lfc.getLongitude());
		this.setUserId(lfc.getUserId());
		this.setStateId(lfc.getStateId());
	}
	
	public Licenses(){
		
	}
	
	@Override
	public List<Licenses> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<Licenses> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<Licenses> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Licenses> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Licenses parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Licenses result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public Licenses parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public Licenses parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Licenses response = new Licenses();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setLongitude(resultSet.getFloat(prefix + "longitude"));
		response.setLatitude(resultSet.getFloat(prefix + "latitude"));
		response.setUserId(resultSet.getInt(prefix + "userId"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setStateId(resultSet.getInt(prefix + "stateId"));
		return response;
	}
	
	public static List<Licenses> parseResultSet(ResultSet resultSet) {
		try{
			return new Licenses().parseResultSetAll(resultSet);
		}catch (SQLException e){
			throw new RuntimeException(e);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("");
		sb.append("id:");
		sb.append(this.id);
		sb.append(", showInUserList:");
		sb.append(this.showInUserList);
		sb.append(", longitude:");
		sb.append(this.longitude);
		sb.append(", latitude:");
		sb.append(this.latitude);
		sb.append(", userId:");
		sb.append(this.userId);
		sb.append(", stateId:");
		sb.append(this.stateId);
		sb.append("}");
		return sb.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getShowInUserList() {
		return showInUserList;
	}

	public void setShowInUserList(Integer showInUserList) {
		this.showInUserList = showInUserList;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
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

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}
}























