package com.salesmanBuddy.model;

import org.codehaus.jettison.json.JSONObject;

public class GoogleUserInfo {
	/*
	 * { "id": "106488367357914471898", "email": "cameronmccord2@gmail.com", "verified_email": true, "name": "Cameron McCord", "given_name": "Cameron", 
	 * "family_name": "McCord", "link": "https://plus.google.com/106488367357914471898", "picture": "https://lh3.googleusercontent.com/-jUTjY6YngV0/AAAAAAAAAAI/AAAAAAAAAD8/csTPwvpAaMc/photo.jpg?sz=50", "gender": "male" }
	 */
	private String id;
	private String email;
	private boolean verifiedEmail;
	private String name;
	private String givenName;
	private String familyName;
	private String link;
	private String picture;
	private String gender;
	
	private boolean inError;
	private String errorMessage;

	public GoogleUserInfo(JSONObject json) {
		if(json.optString("error").length() != 0){
			
			this.inError = true;
			
			if(json.optString("error_description") != null)
				this.errorMessage = json.toString();
			
		}else{
			this.id = json.optString("id");
			this.email = json.optString("email");
			this.verifiedEmail = json.optBoolean("verified_email");
			this.name = json.optString("name");
			this.givenName = json.optString("given_name");
			this.familyName = json.optString("family_name");
			this.link = json.optString("link");
			this.picture = json.optString("picture");
			this.gender = json.optString("gender");
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("UsersName [name=");
		builder.append(name);
		builder.append("]");
		return builder.toString();
	}

	public boolean isInError() {
		return inError;
	}

	public void setInError(boolean inError) {
		this.inError = inError;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public boolean isVerifiedEmail() {
		return verifiedEmail;
	}

	public void setVerifiedEmail(boolean verifiedEmail) {
		this.verifiedEmail = verifiedEmail;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public String getFamilyName() {
		return familyName;
	}

	public void setFamilyName(String familyName) {
		this.familyName = familyName;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getPicture() {
		return picture;
	}

	public void setPicture(String picture) {
		this.picture = picture;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}
}
