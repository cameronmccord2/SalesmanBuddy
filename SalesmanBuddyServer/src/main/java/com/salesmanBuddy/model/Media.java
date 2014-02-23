package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/*
 * id                       int                      IDENTITY(1,1) NOT NULL PRIMARY KEY,
    name                     NVARCHAR(100)                          NOT NULL,
    filename                 NVARCHAR(100)                          NOT NULL,
    type                     NUMERIC(2) default 0                   NOT NULL,
    audioLanguageId          int                                    NOT NULL FOREIGN KEY REFERENCES languages(id),
    bucketId                 int                                    NULL FOREIGN KEY REFERENCES bucketsCE(id),
    extension                NVARCHAR(10)                           NULL,
    filenameInBucket         NVARCHAR(30)                           NULL
 */
public class Media {
	protected Integer id;
	protected String name;
	protected String filename;
	protected Integer type;
	protected Integer audioLanguageId;
	protected Integer bucketId;
	protected String extension;
	protected String filenameInBucket;
	protected String base64Data;
	protected String contentType;
	
	public static ArrayList<Media> parseResultSet(ResultSet resultSet){
    	ArrayList<Media> responses = new ArrayList<Media>();
    	try{
			while(resultSet.next()){
				Media response = new Media();
				response.setId(resultSet.getInt("id"));
				response.setName(resultSet.getString("name"));
				response.setFilename(resultSet.getString("filename"));
				response.setType(resultSet.getInt("type"));
				response.setAudioLanguageId(resultSet.getInt("audioLanguageId"));
				response.setBucketId(resultSet.getInt("bucketId"));
				response.setFilenameInBucket(resultSet.getString("filenameInBucket"));
				responses.add(response);
			}
			resultSet.close();
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	public Integer getAudioLanguageId() {
		return audioLanguageId;
	}
	public void setAudioLanguageId(Integer audioLanguageId) {
		this.audioLanguageId = audioLanguageId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Media [id=");
		builder.append(id);
		builder.append(", name=");
		builder.append(name);
		builder.append(", filename=");
		builder.append(filename);
		builder.append(", type=");
		builder.append(type);
		builder.append(", audioLanguageId=");
		builder.append(audioLanguageId);
		builder.append(", bucketId=");
		builder.append(bucketId);
		builder.append(", extension=");
		builder.append(extension);
		builder.append(", filenameInBucket=");
		builder.append(filenameInBucket);
		builder.append("]");
		return builder.toString();
	}

	public Integer getBucketId() {
		return bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public String getExtension() {
		return extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFilenameInBucket() {
		return filenameInBucket;
	}

	public void setFilenameInBucket(String filenameInBucket) {
		this.filenameInBucket = filenameInBucket;
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
}
