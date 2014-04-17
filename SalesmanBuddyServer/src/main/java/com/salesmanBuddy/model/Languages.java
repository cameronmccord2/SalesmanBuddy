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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((alternateName == null) ? 0 : alternateName.hashCode());
		result = prime * result + ((code1 == null) ? 0 : code1.hashCode());
		result = prime * result + ((code2 == null) ? 0 : code2.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((mtcId == null) ? 0 : mtcId.hashCode());
		result = prime * result
				+ ((mtcTaught == null) ? 0 : mtcTaught.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result
				+ ((nativeName == null) ? 0 : nativeName.hashCode());
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
		Languages other = (Languages) obj;
		if (alternateName == null) {
			if (other.alternateName != null)
				return false;
		} else if (!alternateName.equals(other.alternateName))
			return false;
		if (code1 == null) {
			if (other.code1 != null)
				return false;
		} else if (!code1.equals(other.code1))
			return false;
		if (code2 == null) {
			if (other.code2 != null)
				return false;
		} else if (!code2.equals(other.code2))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (mtcId == null) {
			if (other.mtcId != null)
				return false;
		} else if (!mtcId.equals(other.mtcId))
			return false;
		if (mtcTaught == null) {
			if (other.mtcTaught != null)
				return false;
		} else if (!mtcTaught.equals(other.mtcTaught))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (nativeName == null) {
			if (other.nativeName != null)
				return false;
		} else if (!nativeName.equals(other.nativeName))
			return false;
		return true;
	}
}
