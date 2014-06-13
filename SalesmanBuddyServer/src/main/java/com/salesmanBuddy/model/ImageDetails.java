package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ImageDetails implements ResultSetParser<ImageDetails> {
	
	protected Integer id;
	protected String photoName;
    protected Integer bucketId;
    protected Date created;
    protected Integer answerId;
    
    @Override
	public List<ImageDetails> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<ImageDetails> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<ImageDetails> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<ImageDetails> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public ImageDetails parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		ImageDetails result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public ImageDetails parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public ImageDetails parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		ImageDetails response = new ImageDetails();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setPhotoName(resultSet.getString(prefix + "photoName"));
		response.setBucketId(resultSet.getInt(prefix + "bucketId"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setAnswerId(resultSet.getInt(prefix + "answerId"));
		return response;
	}
    
    public static List<ImageDetails> parseResultSet(ResultSet resultSet) throws SQLException{
    	return new ImageDetails().parseResultSetAll(resultSet);
    }

	public Integer getBucketId() {
		return bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getPhotoName() {
		return photoName;
	}

	public void setPhotoName(String photoName) {
		this.photoName = photoName;
	}

	public Integer getAnswerId() {
		return answerId;
	}

	public void setAnswerId(Integer answerId) {
		this.answerId = answerId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ImageDetails [id=");
		builder.append(id);
		builder.append(", photoName=");
		builder.append(photoName);
		builder.append(", bucketId=");
		builder.append(bucketId);
		builder.append(", created=");
		builder.append(created);
		builder.append(", answerId=");
		builder.append(answerId);
		builder.append("]");
		return builder.toString();
	}
}
