package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Captions {
	protected Integer id;
	protected Integer version;
	protected String caption;
	protected Integer mediaId;
	protected Integer startTime;
	protected Integer endTime;
	protected Integer type;
	protected Date created;
	protected Integer languageId;
	
	public static ArrayList<Captions> parseResultSet(ResultSet resultSet){
    	ArrayList<Captions> responses = new ArrayList<Captions>();
    	try{
			while(resultSet.next()){
				Captions response = new Captions();
				response.setId(resultSet.getInt("id"));
				response.setVersion(resultSet.getInt("version"));
				response.setCreated(resultSet.getDate("created"));
				response.setCaption(resultSet.getString("caption"));
				response.setMediaId(resultSet.getInt("mediaId"));
				response.setStartTime(resultSet.getInt("startTime"));
				response.setEndTime(resultSet.getInt("endTime"));
				response.setType(resultSet.getInt("type"));
				response.setLanguageId(resultSet.getInt("languageId"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder("");
		sb.append(id);
		sb.append("-");
		sb.append(version);
		sb.append("-");
		sb.append(caption);
		sb.append("-");
		sb.append(mediaId);
		sb.append("-");
		sb.append(startTime);
		sb.append("-");
		sb.append(endTime);
		sb.append("-");
		sb.append(type);
		sb.append("-");
		sb.append(created);
		sb.append("-");
		sb.append(languageId);
		sb.append("\n");
		return sb.toString();
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getVersion() {
		return version;
	}
	public void setVersion(Integer version) {
		this.version = version;
	}
	public String getCaption() {
		return caption;
	}
	public void setCaption(String caption) {
		this.caption = caption;
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

	public Integer getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Integer languageId) {
		this.languageId = languageId;
	}

	public Integer getMediaId() {
		return mediaId;
	}

	public void setMediaId(Integer mediaId) {
		this.mediaId = mediaId;
	}
}
