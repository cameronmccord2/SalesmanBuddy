package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Questions implements ResultSetParser<Questions> {
	
	protected Integer id;
    protected Integer version;
    protected Date created;
    protected Integer questionOrder;
    protected String questionTextEnglish;
    protected String questionTextSpanish;
    protected Integer questionType;
    protected Integer required;
    protected Integer tag;

    @Override
	public List<Questions> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<Questions> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<Questions> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Questions> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Questions parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Questions result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public Questions parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public Questions parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Questions response = new Questions();
		response.setId(resultSet.getInt("id"));
		response.setVersion(resultSet.getInt("version"));
		response.setCreated(resultSet.getDate("created"));
		response.setQuestionOrder(resultSet.getInt("questionOrder"));
		response.setQuestionTextEnglish(resultSet.getString("questionTextEnglish"));
		response.setQuestionTextSpanish(resultSet.getString("questionTextSpanish"));
		response.setQuestionType(resultSet.getInt("questionType"));
		response.setTag(resultSet.getInt("tag"));
		response.setRequired(resultSet.getInt("required"));
		return response;
	}
    
    public static List<Questions> parseResultSet(ResultSet resultSet) throws SQLException{
    	return new Questions().parseResultSetAll(resultSet);
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

	public Integer getTag() {
		return tag;
	}

	public void setTag(Integer tag) {
		this.tag = tag;
	}
}























