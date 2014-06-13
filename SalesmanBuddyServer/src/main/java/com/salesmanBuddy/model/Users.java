package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Users implements ResultSetParser<Users> {
	protected Integer id;
    protected Integer dealershipId;
    protected Integer deviceType;
    protected Integer type;
    protected Date created;
    protected String googleUserId;
    protected String refreshToken;
    
    @Override
	public List<Users> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<Users> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}


	@Override
	public Set<Users> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Users> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}


	@Override
	public Users parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Users result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}


	@Override
	public Users parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}


	@Override
	public Users parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Users response = new Users();
		response.setCreated(resultSet.getDate("created"));
		response.setId(resultSet.getInt("id"));
		response.setDealershipId(resultSet.getInt("dealershipId"));
		response.setDeviceType(resultSet.getInt("deviceType"));
		response.setType(resultSet.getInt("type"));
		response.setGoogleUserId(resultSet.getString("googleUserId"));
		response.setRefreshToken(resultSet.getString("refreshToken"));
		return response;
	}
    
    public static List<Users> parseResultSet(ResultSet resultSet) throws SQLException{
		return new Users().parseResultSetAll(resultSet);
    }
    
    
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDealershipId() {
		return dealershipId;
	}
	public void setDealershipId(Integer dealershipId) {
		this.dealershipId = dealershipId;
	}
	public Integer getDeviceType() {
		return deviceType;
	}
	public void setDeviceType(Integer deviceType) {
		this.deviceType = deviceType;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public String getGoogleUserId() {
		return googleUserId;
	}
	public void setGoogleUserId(String googleUserId) {
		this.googleUserId = googleUserId;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}


	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Users [id=");
		builder.append(id);
		builder.append(", dealershipId=");
		builder.append(dealershipId);
		builder.append(", deviceType=");
		builder.append(deviceType);
		builder.append(", type=");
		builder.append(type);
		builder.append(", created=");
		builder.append(created);
		builder.append(", googleUserId=");
		builder.append(googleUserId);
		builder.append(", refreshToken=");
		builder.append(refreshToken);
		builder.append("]");
		return builder.toString();
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result
				+ ((dealershipId == null) ? 0 : dealershipId.hashCode());
		result = prime * result
				+ ((deviceType == null) ? 0 : deviceType.hashCode());
		result = prime * result
				+ ((googleUserId == null) ? 0 : googleUserId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((refreshToken == null) ? 0 : refreshToken.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Users other = (Users) obj;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (dealershipId == null) {
			if (other.dealershipId != null)
				return false;
		} else if (!dealershipId.equals(other.dealershipId))
			return false;
		if (deviceType == null) {
			if (other.deviceType != null)
				return false;
		} else if (!deviceType.equals(other.deviceType))
			return false;
		if (googleUserId == null) {
			if (other.googleUserId != null)
				return false;
		} else if (!googleUserId.equals(other.googleUserId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
}

















