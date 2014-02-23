package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/*
CREATE TABLE popups (
    id                       int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    displayName              NVARCHAR(30)                          NOT NULL,
    popupText                NVARCHAR(4000)                        NULL,
    mediaId                  int                                   NOT NULL FOREIGN KEY REFERENCES media(id),
    languageId               int                                   NOT NULL FOREIGN KEY REFERENCES languages(id),
    startTime                int                                   NOT NULL,
    endTime                  int                                   NOT NULL,
    filename                 NVARCHAR(100)                         NULL,
    bucketId                 int                                   NULL FOREIGN KEY REFERENCES bucketsCE(id),
    filenameInBucket         NVARCHAR(30)                          NULL,
    extension                NVARCHAR(10)                          NULL,
    created                  DATETIME2    default SYSUTCDATETIME() NOT NULL
);
 */
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
		builder.append(", base64Data=");
		builder.append(base64Data);
		builder.append(", contentType=");
		builder.append(contentType);
		builder.append("]");
		return builder.toString();
	}
}
