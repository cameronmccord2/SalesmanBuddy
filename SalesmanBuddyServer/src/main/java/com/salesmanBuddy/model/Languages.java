package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Languages {
	protected Integer id;
	protected String mtcId;
	protected String code1;
	protected String code2;
	protected String name;
	protected Integer mtcTaught;
	protected String alternateName;
	protected String nativeName;
	
	
	public static ArrayList<Languages> parseResultSet(ResultSet resultSet){
    	ArrayList<Languages> responses = new ArrayList<Languages>();
    	try{
			while(resultSet.next()){
				Languages response = new Languages();
				response.setId(resultSet.getInt("id"));
				response.setMtcId(resultSet.getString("mtcId"));
				response.setCode1(resultSet.getString("code1"));
				response.setCode2(resultSet.getString("code2"));
				response.setName(resultSet.getString("name"));
				response.setMtcTaught(resultSet.getInt("mtcTaught"));
				response.setAlternateName(resultSet.getString("alternateName"));
				response.setNativeName(resultSet.getString("nativeName"));
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
		sb.append(mtcId);
		sb.append("-");
		sb.append(code1);
		sb.append("-");
		sb.append(code2);
		sb.append("-");
		sb.append(name);
		sb.append("-");
		sb.append(mtcTaught);
		sb.append("-");
		sb.append(alternateName);
		sb.append("-");
		sb.append(nativeName);
		sb.append("\n");
		return sb.toString();
	}
	
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getMtcId() {
		return mtcId;
	}
	public void setMtcId(String mtcId) {
		this.mtcId = mtcId;
	}
	public String getCode1() {
		return code1;
	}
	public void setCode1(String code1) {
		this.code1 = code1;
	}
	public String getCode2() {
		return code2;
	}
	public void setCode2(String code2) {
		this.code2 = code2;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getMtcTaught() {
		return mtcTaught;
	}
	public void setMtcTaught(Integer mtcTaught) {
		this.mtcTaught = mtcTaught;
	}
	public String getAlternateName() {
		return alternateName;
	}
	public void setAlternateName(String alternateName) {
		this.alternateName = alternateName;
	}
	public String getNativeName() {
		return nativeName;
	}
	public void setNativeName(String nativeName) {
		this.nativeName = nativeName;
	}
}
