package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class MediaForApp {
	protected Integer id;
	protected String name;
	protected String filename;
	protected Integer type;
	protected Integer audioLanguageId;
	protected Integer bucketId;
	protected String extension;
	protected String filenameInBucket;
	protected ArrayList<Captions> captions;
	protected ArrayList<Popups> popups;
	protected String bucketName;
	protected Languages language;
	
	public static ArrayList<MediaForApp> parseResultSet(ResultSet resultSet){
    	ArrayList<MediaForApp> responses = new ArrayList<MediaForApp>();
    	try{
			while(resultSet.next()){
				MediaForApp response = new MediaForApp();
				response.setId(resultSet.getInt("id"));
				response.setName(resultSet.getString("name"));
				response.setFilename(resultSet.getString("filename"));
				response.setType(resultSet.getInt("type"));
				response.setAudioLanguageId(resultSet.getInt("audioLanguageId"));
				response.setBucketId(resultSet.getInt("bucketId"));
				response.setFilenameInBucket(resultSet.getString("filenameInBucket"));
				response.setExtension(resultSet.getString("extension"));
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((audioLanguageId == null) ? 0 : audioLanguageId.hashCode());
		result = prime * result
				+ ((bucketId == null) ? 0 : bucketId.hashCode());
		result = prime * result
				+ ((bucketName == null) ? 0 : bucketName.hashCode());
		result = prime * result
				+ ((captions == null) ? 0 : captions.hashCode());
		result = prime * result
				+ ((extension == null) ? 0 : extension.hashCode());
		result = prime * result
				+ ((filename == null) ? 0 : filename.hashCode());
		result = prime
				* result
				+ ((filenameInBucket == null) ? 0 : filenameInBucket.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result
				+ ((language == null) ? 0 : language.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((popups == null) ? 0 : popups.hashCode());
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
		MediaForApp other = (MediaForApp) obj;
		if (audioLanguageId == null) {
			if (other.audioLanguageId != null)
				return false;
		} else if (!audioLanguageId.equals(other.audioLanguageId))
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
		if (captions == null) {
			if (other.captions != null)
				return false;
		} else if (!captions.equals(other.captions))
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
		if (language == null) {
			if (other.language != null)
				return false;
		} else if (!language.equals(other.language))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (popups == null) {
			if (other.popups != null)
				return false;
		} else if (!popups.equals(other.popups))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MediaForApp [id=");
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
		builder.append(", captions=");
		builder.append(captions);
		builder.append(", popups=");
		builder.append(popups);
		builder.append(", bucketName=");
		builder.append(bucketName);
		builder.append(", language=");
		builder.append(language);
		builder.append("]");
		return builder.toString();
	}

	public ArrayList<Captions> getCaptions() {
		return captions;
	}

	public void setCaptions(ArrayList<Captions> captions) {
		this.captions = captions;
	}

	public ArrayList<Popups> getPopups() {
		return popups;
	}

	public void setPopups(ArrayList<Popups> popups) {
		this.popups = popups;
	}

	public Languages getLanguage() {
		return language;
	}

	public void setLanguage(Languages language) {
		this.language = language;
	}

	public String getBucketName() {
		return bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}
}
