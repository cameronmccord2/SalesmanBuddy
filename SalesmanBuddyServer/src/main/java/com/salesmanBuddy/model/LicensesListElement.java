package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LicensesListElement{
	// licenses
	protected Integer id;
    protected Date created;
    protected Integer stateId;
    //custom here
    protected List<QuestionsAndAnswers> qaas;
	
	public static List<LicensesListElement> parseResultSet(ResultSet resultSet){
    	List<LicensesListElement> responses = new ArrayList<>();
    	try{
			while(resultSet.next()){
				LicensesListElement response = new LicensesListElement();
				response.setId(resultSet.getInt("id"));
				response.setCreated(resultSet.getDate("created"));
				response.setStateId(resultSet.getInt("stateId"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }
	
	public static String getStockNumberForLicensesListElement(LicensesListElement lle) {
//		return "IMPLEMENT_GETTING_STOCK_NUMBER";
		for(QuestionsAndAnswers qaa : lle.getQaas()){
			if(qaa.getQuestion().getQuestionTextEnglish().equalsIgnoreCase("Stock Number"))
				return qaa.getAnswer().getAnswerText();
		}
		return "No stock number found";
	}
	
	public String getReportString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.toString());
		sb.append("\n");
		return sb.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LicensesListElement [id=");
		builder.append(id);
		builder.append(", created=");
		builder.append(created);
		builder.append(", stateId=");
		builder.append(stateId);
		builder.append(", qaas=");
		builder.append(qaas);
		builder.append("]");
		return builder.toString();
	}

	public List<QuestionsAndAnswers> getQaas() {
		return qaas;
	}

	public void setQaas(List<QuestionsAndAnswers> qaas) {
		this.qaas = qaas;
	}
}
