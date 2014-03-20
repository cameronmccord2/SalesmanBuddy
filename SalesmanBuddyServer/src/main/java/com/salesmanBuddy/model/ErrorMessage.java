package com.salesmanBuddy.model;

public class ErrorMessage {
	protected String message;
	
	public ErrorMessage(String m){
		this.message = m;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ErrorMessage [message=");
		builder.append(message);
		builder.append("]");
		return builder.toString();
	}
}
