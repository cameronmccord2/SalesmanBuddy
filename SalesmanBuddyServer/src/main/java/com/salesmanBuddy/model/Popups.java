package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Popups {
	protected Integer id;
	protected String displayName;
	protected String popupText;
	protected Integer mediaId;
	protected Integer languageId;
	protected Integer startTime;
	protected Integer endTime;
	protected String filename;
	protected Date created;
	protected Integer bucketId;
	protected String filenameInBucket;
	protected String extension;
	protected String bucketName;
	
	// convenience, not used now
	protected String base64Data;
	protected String contentType;
	
	public static ArrayList<Popups> parseResultSet(ResultSet resultSet){
    	ArrayList<Popups> responses = new ArrayList<Popups>();
    	try{
			while(resultSet.next()){
				Popups response = new Popups();
				response.setId(resultSet.getInt("id"));
				response.setDisplayName(resultSet.getString("displayName"));
				response.setPopupText(resultSet.getString("popupText"));
				response.setMediaId(resultSet.getInt("mediaId"));
				response.setLanguageId(resultSet.getInt("languageId"));
				response.setStartTime(resultSet.getInt("startTime"));
				response.setEndTime(resultSet.getInt("endTime"));
				response.setFilename(resultSet.getString("filename"));
				response.setCreated(resultSet.getDate("created"));
				response.setBucketId(resultSet.getInt("bucketId"));
				response.setFilenameInBucket(resultSet.getString("filenameInBucket"));
				response.setExtension(resultSet.getString("extension"));
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
	public String getDisplayName() {
		return displayName;
	}
	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	public String getPopupText() {
		return popupText;
	}
	public void setPopupText(String popupText) {
		this.popupText = popupText;
	}
	public Integer getMediaId() {
		return mediaId;
	}
	public void setMediaId(Integer mediaId) {
		this.mediaId = mediaId;
	}
	public Integer getLanguageId() {
		return languageId;
	}
	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
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
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}

	public String getBase64Data() {
		return base64Data;
	}

	public void setBase64Data(String base64Data) {
		this.base64Data = base64Data;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
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

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Popups [id=");
		builder.append(id);
		builder.append(", displayName=");
		builder.append(displayName);
		builder.append(", popupText=");
		builder.append(popupText);
		builder.append(", mediaId=");
		builder.append(mediaId);
		builder.append(", languageId=");
		builder.append(languageId);
		builder.append(", startTime=");
		builder.append(startTime);
		builder.append(", endTime=");
		builder.append(endTime);
		builder.append(", filename=");
		builder.append(filename);
		builder.append(", created=");
		builder.append(created);
		builder.append(", bucketId=");
		builder.append(bucketId);
		builder.append(", filenameInBucket=");
		builder.append(filenameInBucket);
		builder.append(", extension=");
		builder.append(extension);
		builder.append(", bucketName=");
		builder.append(bucketName);
		builder.append(", base64Data=");
		builder.append(base64Data);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append("]");
		return builder.toString();
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((base64Data == null) ? 0 : base64Data.hashCode());
		result = prime * result
				+ ((bucketId == null) ? 0 : bucketId.hashCode());
		result = prime * result
				+ ((bucketName == null) ? 0 : bucketName.hashCode());
		result = prime * result
				+ ((contentType == null) ? 0 : contentType.hashCode());
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result
				+ ((displayName == null) ? 0 : displayName.hashCode());
		result = prime * result + ((endTime == null) ? 0 : endTime.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime
				* result
				+ ((filenameInBucket == null) ? 0 : filenameInBucket.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((languageId == null) ? 0 : languageId.hashCode());
		result = prime * result + ((mediaId == null) ? 0 : mediaId.hashCode());
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
		Popups other = (Popups) obj;
		if (base64Data == null) {
			if (other.base64Data != null)
				return false;
		} else if (!base64Data.equals(other.base64Data))
			return false;
		if (bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!bucketId.equals(other.bucketId))
			return false;
		if (bucketName == null) {
			if (other.bucketName != null)
				return false;
		} else if (!bucketName.equals(other.bucketName))
			return false;
		if (contentType == null) {
			if (other.contentType != null)
				return false;
		} else if (!contentType.equals(other.contentType))
			return false;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (displayName == null) {
			if (other.displayName != null)
				return false;
		} else if (!displayName.equals(other.displayName))
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
		if (languageId == null) {
			if (other.languageId != null)
				return false;
		} else if (!languageId.equals(other.languageId))
			return false;
		if (mediaId == null) {
			if (other.mediaId != null)
				return false;
		} else if (!mediaId.equals(other.mediaId))
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
