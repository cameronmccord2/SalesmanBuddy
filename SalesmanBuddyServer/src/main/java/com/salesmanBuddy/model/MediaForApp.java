package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MediaForApp {
	protected Integer id;
	protected String name;
	protected String filename;
	protected Integer type;
	protected Integer audioLanguageId;
	protected Integer bucketId;
	protected String extension;
	protected String filenameInBucket;
	protected List<Captions> captions;
	protected List<Popups> popups;
	protected String bucketName;
	protected List<Languages> languages;
	
	public static List<MediaForApp> parseResultSet(ResultSet resultSet){
    	List<MediaForApp> responses = new ArrayList<>();
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
				response.setLanguages(new ArrayList<Languages>());
				response.setCaptions(new ArrayList<Captions>());
				response.setPopups(new ArrayList<Popups>());
				responses.add(response);
			}
			resultSet.close();
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }

	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFilename() {
		return this.filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getAudioLanguageId() {
		return this.audioLanguageId;
	}

	public void setAudioLanguageId(Integer audioLanguageId) {
		this.audioLanguageId = audioLanguageId;
	}

	public Integer getBucketId() {
		return this.bucketId;
	}

	public void setBucketId(Integer bucketId) {
		this.bucketId = bucketId;
	}

	public String getExtension() {
		return this.extension;
	}

	public void setExtension(String extension) {
		this.extension = extension;
	}

	public String getFilenameInBucket() {
		return this.filenameInBucket;
	}

	public void setFilenameInBucket(String filenameInBucket) {
		this.filenameInBucket = filenameInBucket;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((this.audioLanguageId == null) ? 0 : this.audioLanguageId.hashCode());
		result = prime * result
				+ ((this.bucketId == null) ? 0 : this.bucketId.hashCode());
		result = prime * result
				+ ((this.bucketName == null) ? 0 : this.bucketName.hashCode());
		result = prime * result
				+ ((this.captions == null) ? 0 : this.captions.hashCode());
		result = prime * result
				+ ((this.extension == null) ? 0 : this.extension.hashCode());
		result = prime * result
				+ ((this.filename == null) ? 0 : this.filename.hashCode());
		result = prime
				* result
				+ ((this.filenameInBucket == null) ? 0 : this.filenameInBucket.hashCode());
		result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
		result = prime * result
				+ ((this.languages == null) ? 0 : this.languages.hashCode());
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.popups == null) ? 0 : this.popups.hashCode());
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
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
		if (this.audioLanguageId == null) {
			if (other.audioLanguageId != null)
				return false;
		} else if (!this.audioLanguageId.equals(other.audioLanguageId))
			return false;
		if (this.bucketId == null) {
			if (other.bucketId != null)
				return false;
		} else if (!this.bucketId.equals(other.bucketId))
			return false;
		if (this.bucketName == null) {
			if (other.bucketName != null)
				return false;
		} else if (!this.bucketName.equals(other.bucketName))
			return false;
		if (this.captions == null) {
			if (other.captions != null)
				return false;
		} else if (!this.captions.equals(other.captions))
			return false;
		if (this.extension == null) {
			if (other.extension != null)
				return false;
		} else if (!this.extension.equals(other.extension))
			return false;
		if (this.filename == null) {
			if (other.filename != null)
				return false;
		} else if (!this.filename.equals(other.filename))
			return false;
		if (this.filenameInBucket == null) {
			if (other.filenameInBucket != null)
				return false;
		} else if (!this.filenameInBucket.equals(other.filenameInBucket))
			return false;
		if (this.id == null) {
			if (other.id != null)
				return false;
		} else if (!this.id.equals(other.id))
			return false;
		if (this.languages == null) {
			if (other.languages != null)
				return false;
		} else if (!this.languages.equals(other.languages))
			return false;
		if (this.name == null) {
			if (other.name != null)
				return false;
		} else if (!this.name.equals(other.name))
			return false;
		if (this.popups == null) {
			if (other.popups != null)
				return false;
		} else if (!this.popups.equals(other.popups))
			return false;
		if (this.type == null) {
			if (other.type != null)
				return false;
		} else if (!this.type.equals(other.type))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MediaForApp [id=");
		builder.append(this.id);
		builder.append(", name=");
		builder.append(this.name);
		builder.append(", filename=");
		builder.append(this.filename);
		builder.append(", type=");
		builder.append(this.type);
		builder.append(", audioLanguageId=");
		builder.append(this.audioLanguageId);
		builder.append(", bucketId=");
		builder.append(this.bucketId);
		builder.append(", extension=");
		builder.append(this.extension);
		builder.append(", filenameInBucket=");
		builder.append(this.filenameInBucket);
		builder.append(", captions=");
		builder.append(this.captions);
		builder.append(", popups=");
		builder.append(this.popups);
		builder.append(", bucketName=");
		builder.append(this.bucketName);
		builder.append(", languages=");
		builder.append(this.languages);
		builder.append("]");
		return builder.toString();
	}

	public String getBucketName() {
		return this.bucketName;
	}

	public void setBucketName(String bucketName) {
		this.bucketName = bucketName;
	}

	public List<Captions> getCaptions() {
		return this.captions;
	}

	public void setCaptions(List<Captions> captions) {
		this.captions = captions;
	}

	public List<Popups> getPopups() {
		return this.popups;
	}

	public void setPopups(List<Popups> popups) {
		this.popups = popups;
	}

	public List<Languages> getLanguages() {
		return this.languages;
	}

	public void setLanguages(List<Languages> languages) {
		this.languages = languages;
	}
}
