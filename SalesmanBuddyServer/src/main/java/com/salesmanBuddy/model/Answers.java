package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Answers implements ResultSetParser<Answers> {
	
	protected Integer id;
    protected Integer answerBool;
    protected Integer answerType;
    protected String answerText;
    protected Integer licenseId;
    protected Date created;
    protected Integer questionId;
    protected ImageDetails imageDetails;
    
    @Override
	public List<Answers> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<Answers> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<Answers> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<Answers> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Answers parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		Answers result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public Answers parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public Answers parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		Answers response = new Answers();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setAnswerBool(resultSet.getInt(prefix + "answerBool"));
		response.setAnswerType(resultSet.getInt(prefix + "answerType"));
		response.setAnswerText(resultSet.getString(prefix + "answerText"));
		response.setLicenseId(resultSet.getInt(prefix + "licenseId"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setQuestionId(resultSet.getInt(prefix + "questionId"));
		return response;
	}
    
    public static List<Answers> parseResultSet(ResultSet resultSet) throws SQLException{
    	return new Answers().parseResultSetAll(resultSet);
    }

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getAnswerBool() {
		return answerBool;
	}

	public void setAnswerBool(Integer answerBool) {
		this.answerBool = answerBool;
	}

	public String getAnswerText() {
		return answerText;
	}

	public void setAnswerText(String answerText) {
		this.answerText = answerText;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getQuestionId() {
		return questionId;
	}

	public void setQuestionId(Integer questionId) {
		this.questionId = questionId;
	}

	public ImageDetails getImageDetails() {
		return imageDetails;
	}

	public void setImageDetails(ImageDetails imageDetails) {
		this.imageDetails = imageDetails;
	}

	public Integer getLicenseId() {
		return licenseId;
	}

	public void setLicenseId(Integer licenseId) {
		this.licenseId = licenseId;
	}

	public Integer getAnswerType() {
		return answerType;
	}

	public void setAnswerType(Integer answerType) {
		this.answerType = answerType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Answers [id=");
		builder.append(id);
		builder.append(", answerBool=");
		builder.append(answerBool);
		builder.append(", answerType=");
		builder.append(answerType);
		builder.append(", answerText=");
		builder.append(answerText);
		builder.append(", licenseId=");
		builder.append(licenseId);
		builder.append(", created=");
		builder.append(created);
		builder.append(", questionId=");
		builder.append(questionId);
		builder.append(", imageDetails=");
		builder.append(imageDetails);
		builder.append("]");
		return builder.toString();
	}
}



























