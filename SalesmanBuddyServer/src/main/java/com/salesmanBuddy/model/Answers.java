package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class Answers {
	protected Integer id;
    protected Integer answerBool;
    protected Integer answerType;
    protected String answerText;
    protected Integer licenseId;
    protected Date created;
    protected Integer questionId;
    protected ImageDetails imageDetails;
    
    /*
     * id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    answerText                 NVARCHAR(100)                        NOT NULL,
    answerBool                 BIT          default 0                NOT NULL,
    answerIsBool               BIT          default 0                NOT NULL,
    driverId                   int                                   NOT NULL FOREIGN KEY REFERENCES drivers(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL(non-Javadoc)
     * @see java.lang.Object#toString()
     */
    
    public String toString(){
    	StringBuilder sb = new StringBuilder("");
    	sb.append("id:");
    	sb.append(this.id);
    	sb.append(", licenseId:");
    	return sb.toString();
    }
    
    public static ArrayList<Answers> parseResultSet(ResultSet resultSet){
    	ArrayList<Answers> responses = new ArrayList<Answers>();
    	try{
			while(resultSet.next()){
				Answers response = new Answers();
				response.setId(resultSet.getInt("id"));
				response.setAnswerBool(resultSet.getInt("answerBool"));
				response.setAnswerType(resultSet.getInt("answerType"));
				response.setAnswerText(resultSet.getString("answerText"));
				response.setLicenseId(resultSet.getInt("licenseId"));
				response.setCreated(resultSet.getDate("created"));
				response.setQuestionId(resultSet.getInt("questionId"));
				responses.add(response);
			}
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
}

