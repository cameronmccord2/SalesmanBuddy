package com.salesmanBuddy.model;

import java.util.ArrayList;

public class SBEmail {
	private String from;
	private ArrayList<String> to;
	private String subject;
	private String body;
	private String bodyHtml;
	private boolean individualEmailsToRecipients;
	
	public static SBEmail newHtmlEmail(String from, ArrayList<String> to, String subject, String bodyHtml, boolean individualEmailsToRecipients){
		SBEmail e = new SBEmail();
		e.setBody("");
		e.setBodyHtml(bodyHtml);
		e.setFrom(from);
		e.setTo(to);
		e.setSubject(subject);
		e.setIndividualEmailsToRecipients(individualEmailsToRecipients);
		return e;
	}
	
	public static SBEmail newPlainTextEmail(String from, ArrayList<String> to, String subject, String body, boolean individualEmailsToRecipients){
		SBEmail e = new SBEmail();
		e.setBody(body);
		e.setBodyHtml("");
		e.setFrom(from);
		e.setTo(to);
		e.setSubject(subject);
		e.setIndividualEmailsToRecipients(individualEmailsToRecipients);
		return e;
	}
	
	public static SBEmail newEmail(String from, ArrayList<String> to, String subject, String body, String bodyHtml, boolean individualEmailsToRecipients){
		SBEmail e = new SBEmail();
		e.setBody(body);
		e.setBodyHtml(bodyHtml);
		e.setFrom(from);
		e.setTo(to);
		e.setIndividualEmailsToRecipients(individualEmailsToRecipients);
		return e;
	}

	public void addTo(String newTo) {
		this.to.add(newTo);
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public ArrayList<String> getTo() {
		return to;
	}

	public void setTo(ArrayList<String> to) {
		this.to = to;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public String getBodyHtml() {
		return bodyHtml;
	}

	public void setBodyHtml(String bodyHtml) {
		this.bodyHtml = bodyHtml;
	}

	public boolean isIndividualEmailsToRecipients() {
		return individualEmailsToRecipients;
	}

	public void setIndividualEmailsToRecipients(boolean individualEmailsToRecipients) {
		this.individualEmailsToRecipients = individualEmailsToRecipients;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SBEmail [from=");
		builder.append(from);
		builder.append(", to=");
		builder.append(to);
		builder.append(", subject=");
		builder.append(subject);
		builder.append(", body=");
		builder.append(body);
		builder.append(", bodyHtml=");
		builder.append(bodyHtml);
		builder.append(", individualEmailsToRecipients=");
		builder.append(individualEmailsToRecipients);
		builder.append("]");
		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((body == null) ? 0 : body.hashCode());
		result = prime * result
				+ ((bodyHtml == null) ? 0 : bodyHtml.hashCode());
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + (individualEmailsToRecipients ? 1231 : 1237);
		result = prime * result + ((subject == null) ? 0 : subject.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
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
		SBEmail other = (SBEmail) obj;
		if (body == null) {
			if (other.body != null)
				return false;
		} else if (!body.equals(other.body))
			return false;
		if (bodyHtml == null) {
			if (other.bodyHtml != null)
				return false;
		} else if (!bodyHtml.equals(other.bodyHtml))
			return false;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (individualEmailsToRecipients != other.individualEmailsToRecipients)
			return false;
		if (subject == null) {
			if (other.subject != null)
				return false;
		} else if (!subject.equals(other.subject))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		return true;
	}

	@Override
	public SBEmail clone() {
		// TODO Auto-generated method stub
		SBEmail e = new SBEmail();
		e.setBody(new String(this.getBody()));
		e.setBodyHtml(new String(this.getBodyHtml()));
		e.setFrom(new String(this.getFrom()));
		e.setIndividualEmailsToRecipients(this.isIndividualEmailsToRecipients());
		e.setSubject(new String(this.getSubject()));
		e.setTo(new ArrayList<String>());
		for(String to : this.getTo()){
			e.getTo().add(new String(to));
		}
		return e;
	}

}
