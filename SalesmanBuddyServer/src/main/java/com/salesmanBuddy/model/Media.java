package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Media {
	protected Integer id;
	protected String name;
	protected String filename;
	protected Integer type;
	protected Integer audioLanguageId;
	
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
		sb.append(name);
		sb.append("-");
		sb.append(filename);
		sb.append("-");
		sb.append(type);
		sb.append("-");
		sb.append(audioLanguageId);
		sb.append("\n");
		return sb.toString();
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
}
