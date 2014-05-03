package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class SubPopups {
	
	protected Integer id;
	protected String popupText;
	protected Integer popupId;
	protected Integer startTime;
	protected Integer endTime;
	protected String filename;
	protected Integer bucketId;
	protected String filenameInBucket;
	protected String extension;
	protected Integer assetPosition;
	protected Date created;
	
	public static ArrayList<SubPopups> parseResultSet(ResultSet resultSet){
    	ArrayList<SubPopups> responses = new ArrayList<SubPopups>();
    	try{
			while(resultSet.next()){
				SubPopups r = new SubPopups();
				r.setId(resultSet.getInt("id"));
				r.setCreated(resultSet.getDate("created"));
				r.setPopupText(resultSet.getString("popupText"));
				r.setPopupId(resultSet.getInt("popupId"));
				r.setStartTime(resultSet.getInt("startTime"));
				r.setEndTime(resultSet.getInt("endTime"));
				r.setFilename(resultSet.getString("filename"));
				r.setBucketId(resultSet.getInt("bucketId"));
				r.setFilenameInBucket(resultSet.getString("filenameInBucket"));
				r.setExtension(resultSet.getString("extension"));
				r.setAssetPosition(resultSet.getInt("assetPosition"));
				responses.add(r);
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
	public String getPopupText() {
		return popupText;
	}
	public void setPopupText(String popupText) {
		this.popupText = popupText;
	}
	public Integer getPopupId() {
		return popupId;
	}
	public void setPopupId(Integer popupId) {
		this.popupId = popupId;
	}
	public Integer getStartTime() {
		return startTime;
	}
	public void setStartTime(Integer startTime) {
		this.startTime = startTime;
	}
	public Integer getEndTime() {
		return endTime;
	}
	public void setEndTime(Integer endTime) {
		this.endTime = endTime;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Integer getBucketId() {
		return bucketId;
	}
	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}
	public String getFilenameInBucket() {
		return filenameInBucket;
	}
	public void setFilenameInBucket(String filenameInBucket) {
		this.filenameInBucket = filenameInBucket;
	}
	public String getExtension() {
		return extension;
	}
	public void setExtension(String extension) {
		this.extension = extension;
	}
	public Integer getAssetPosition() {
		return assetPosition;
	}
	public void setAssetPosition(Integer assetPosition) {
		this.assetPosition = assetPosition;
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
		builder.append("SubPopups [id=");
		builder.append(id);
		builder.append(", popupText=");
		builder.append(popupText);
		builder.append(", popupId=");
		builder.append(popupId);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", filename=");
		builder.append(filename);
		builder.append(", bucketId=");
		builder.append(bucketId);
		builder.append(", filenameInBucket=");
		builder.append(filenameInBucket);
		builder.append(", extension=");
		builder.append(extension);
		builder.append(", assetPosition=");
		builder.append(assetPosition);
		builder.append(", created=");
		builder.append(created);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((assetPosition == null) ? 0 : assetPosition.hashCode());
		result = prime * result
				+ ((bucketId == null) ? 0 : bucketId.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime
				* result
				+ ((filenameInBucket == null) ? 0 : filenameInBucket.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((popupId == null) ? 0 : popupId.hashCode());
		result = prime * result
				+ ((popupText == null) ? 0 : popupText.hashCode());
		result = prime * result
				+ ((startTime == null) ? 0 : startTime.hashCode());
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
		SubPopups other = (SubPopups) obj;
		if (assetPosition == null) {
			if (other.assetPosition != null)
				return false;
		} else if (!assetPosition.equals(other.assetPosition))
			return false;
		if (bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!bucketId.equals(other.bucketId))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (endTime == null) {
			if (other.endTime != null)
				return false;
		} else if (!endTime.equals(other.endTime))
			return false;
		if (extension == null) {
			if (other.extension != null)
				return false;
		} else if (!extension.equals(other.extension))
			return false;
		if (filename == null) {
			if (other.filename != null)
				return false;
		} else if (!filename.equals(other.filename))
			return false;
		if (filenameInBucket == null) {
			if (other.filenameInBucket != null)
				return false;
		} else if (!filenameInBucket.equals(other.filenameInBucket))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (popupId == null) {
			if (other.popupId != null)
				return false;
		} else if (!popupId.equals(other.popupId))
			return false;
		if (popupText == null) {
			if (other.popupText != null)
				return false;
		} else if (!popupText.equals(other.popupText))
			return false;
		if (startTime == null) {
			if (other.startTime != null)
				return false;
		} else if (!startTime.equals(other.startTime))
			return false;
		return true;
	}
}
