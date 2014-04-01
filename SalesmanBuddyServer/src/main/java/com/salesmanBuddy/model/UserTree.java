package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import org.codehaus.jackson.map.annotate.JsonSerialize;

@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class UserTree {
	
	public static UserTree copy(UserTree ut) {
		UserTree u = new UserTree();
		u.id = ut.getId();
		u.userId = new String(ut.getUserId());
		u.supervisorId = new String(ut.getSupervisorId());
		u.created = new Date(ut.getCreated().getTime());
		u.type = ut.getType();
		return u;
	}

	protected Integer id;
	protected String userId;
	protected String supervisorId;
	protected Date created;
	protected Integer type;
	
	public static ArrayList<UserTree> parseResultSet(ResultSet resultSet) throws SQLException{
    	ArrayList<UserTree> responses = new ArrayList<UserTree>();
		while(resultSet.next()){
			responses.add(UserTree.parseARow(resultSet));
		}
    	return responses;
    }
	
	public static UserTree parseOneResultFromSet(ResultSet resultSet) throws SQLException{
		UserTree response = null;
		while(resultSet.next()){
			response = UserTree.parseARow(resultSet);
			break;
		}
		return response;
	}
	
	private static UserTree parseARow(ResultSet resultSet) throws SQLException{
		UserTree response = new UserTree();
		response.setId(resultSet.getInt("id"));
		response.setUserId(resultSet.getString("userId"));
		response.setSupervisorId(resultSet.getString("supervisorId"));
		response.setType(resultSet.getInt("type"));
		response.setCreated(resultSet.getDate("created"));
		return response;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UserTree [id=");
		builder.append(id);
		builder.append(", userId=");
		builder.append(userId);
		builder.append(", supervisorId=");
		builder.append(supervisorId);
		builder.append(", created=");
		builder.append(created);
		builder.append(", type=");
		builder.append(type);
		builder.append("]");
		return builder.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getSupervisorId() {
		return supervisorId;
	}

	public void setSupervisorId(String supervisorId) {
		this.supervisorId = supervisorId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((supervisorId == null) ? 0 : supervisorId.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
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
		UserTree other = (UserTree) obj;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (supervisorId == null) {
			if (other.supervisorId != null)
				return false;
		} else if (!supervisorId.equals(other.supervisorId))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		return true;
	}
	
}
