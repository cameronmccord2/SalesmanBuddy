package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Questions {
	protected Integer id;
    protected Integer version;
    protected Date created;
    protected Integer questionOrder;
    protected String questionTextEnglish;
    protected String questionTextSpanish;
    protected Integer questionType;
    protected Integer required;

    
    public static ArrayList<Questions> parseResultSet(ResultSet resultSet){
    	ArrayList<Questions> responses = new ArrayList<Questions>();
    	try{
			while(resultSet.next()){
				Questions response = new Questions();
				response.setId(resultSet.getInt("id"));
				response.setVersion(resultSet.getInt("version"));
				response.setCreated(resultSet.getDate("created"));
				response.setQuestionOrder(resultSet.getInt("questionOrder"));
				response.setQuestionTextEnglish(resultSet.getString("questionTextEnglish"));
				response.setQuestionTextSpanish(resultSet.getString("questionTextSpanish"));
				response.setQuestionType(resultSet.getInt("questionType"));
				responses.add(response);
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }

	public String getQuestionTextEnglish() {
		return questionTextEnglish;
	}

	public void setQuestionTextEnglish(String questionTextEnglish) {
		this.questionTextEnglish = questionTextEnglish;
	}

	public String getQuestionTextSpanish() {
		return questionTextSpanish;
	}

	public void setQuestionTextSpanish(String questionTextSpanish) {
		this.questionTextSpanish = questionTextSpanish;
	}

	public Integer getRequired() {
		return required;
	}

	public void setRequired(Integer required) {
		this.required = required;
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

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getQuestionOrder() {
		return questionOrder;
	}

	public void setQuestionOrder(Integer questionOrder) {
		this.questionOrder = questionOrder;
	}

	public Integer getQuestionType() {
		return questionType;
	}

	public void setQuestionType(Integer questionType) {
		this.questionType = questionType;
	}
}

