package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.salesmanBuddy.exceptions.NoResultInResultSet;

public class EmailBackup {

	protected Integer id;
	protected Date created;
	protected Date sentAt;
	protected String toEmails;
	protected String fromEmail;
	protected String subject;
	protected String message;
	protected Integer status;
	
	private ArrayList<String> toEmailsList;
	
	public synchronized ArrayList<String> getToEmailsList(){
		if(this.toEmailsList == null){
			this.toEmailsList = new ArrayList<String>();
			String[] emails = toEmails.split(",");
			for(String s : emails)
				this.toEmailsList.add(s);
		}
		return this.toEmailsList;
	}
	
	public static ArrayList<EmailBackup> parseResultSet(ResultSet resultSet){
    	ArrayList<EmailBackup> responses = new ArrayList<EmailBackup>();
    	try{
			while(resultSet.next()){
				responses.add(EmailBackup.parseThisResultSet(resultSet));
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return responses;
    }
	
	public static EmailBackup parseOneRowResultSet(ResultSet resultSet) throws SQLException, NoResultInResultSet{
		while(resultSet.next())
			return EmailBackup.parseThisResultSet(resultSet);
		throw new NoResultInResultSet();
	}
	
	private static EmailBackup parseThisResultSet(ResultSet resultSet) throws SQLException{
		EmailBackup response = new EmailBackup();
		response.setId(resultSet.getInt("id"));
		response.setCreated(resultSet.getDate("created"));
		response.setSentAt(resultSet.getDate("sentAt"));
		response.setStatus(resultSet.getInt("status"));
		response.setToEmails(resultSet.getString("toEmails"));
		response.setFromEmail(resultSet.getString("fromEmail"));
		response.setSubject(resultSet.getString("subject"));
		response.setMessage(resultSet.getString("message"));
		return response;
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
	public Date getSentAt() {
		return sentAt;
	}
	public void setSentAt(Date sentAt) {
		this.sentAt = sentAt;
	}
	public String getToEmails() {
		return toEmails;
	}
	public void setToEmails(String toEmails) {
		this.toEmails = toEmails;
	}
	public String getFromEmail() {
		return fromEmail;
	}
	public void setFromEmail(String fromEmail) {
		this.fromEmail = fromEmail;
	}
	public String getSubject() {
		return subject;
	}
	public void setSubject(String subject) {
		this.subject = subject;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result
				+ ((fromEmail == null) ? 0 : fromEmail.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((sentAt == null) ? 0 : sentAt.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result
				+ ((toEmails == null) ? 0 : toEmails.hashCode());
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
		EmailBackup other = (EmailBackup) obj;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (fromEmail == null) {
			if (other.fromEmail != null)
				return false;
		} else if (!fromEmail.equals(other.fromEmail))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (sentAt == null) {
			if (other.sentAt != null)
				return false;
		} else if (!sentAt.equals(other.sentAt))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (toEmails == null) {
			if (other.toEmails != null)
				return false;
		} else if (!toEmails.equals(other.toEmails))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("EmailBackup [id=");
		builder.append(id);
		builder.append(", created=");
		builder.append(created);
		builder.append(", sentAt=");
		builder.append(sentAt);
		builder.append(", toEmails=");
		builder.append(toEmails);
		builder.append(", fromEmail=");
		builder.append(fromEmail);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", message=");
		builder.append(message);
		builder.append(", status=");
		builder.append(status);
		builder.append("]");
		return builder.toString();
	}
}
