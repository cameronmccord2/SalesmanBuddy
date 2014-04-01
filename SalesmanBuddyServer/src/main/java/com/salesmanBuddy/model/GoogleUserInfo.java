package com.salesmanBuddy.model;

import org.codehaus.jettison.json.JSONObject;

import com.salesmanBuddy.exceptions.GoogleUserInfoException;

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
	private String locale;
	
//	{
//		 "id": "106488367357914471898",
//		 "email": "cameronmccord2@gmail.com",
//		 "verified_email": true,
//		 "name": "Cameron McCord",
//		 "given_name": "Cameron",
//		 "family_name": "McCord",
//		 "link": "https://plus.google.com/106488367357914471898",
//		 "picture": "https://lh3.googleusercontent.com/-jUTjY6YngV0/AAAAAAAAAAI/AAAAAAAAAD8/csTPwvpAaMc/photo.jpg",
//		 "gender": "male",
//		 "locale": "en"
//		}

	public GoogleUserInfo(JSONObject json) throws GoogleUserInfoException {
		if(json.optString("error").length() != 0){
			
			String errorMessage = "No error message";
			if(json.optString("error_description") != null)
				errorMessage = json.toString();
			
			throw new GoogleUserInfoException(new StringBuilder().append("Error Message: ").append(errorMessage).append(", json: ").append(json.toString()).toString());
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
			this.locale = json.optString("locale");
		}
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
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
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
	public String getLocale() {
		return locale;
	}
	public void setLocale(String locale) {
		this.locale = locale;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GoogleUserInfo [id=");
		builder.append(id);
		builder.append(", email=");
		builder.append(email);
		builder.append(", verifiedEmail=");
		builder.append(verifiedEmail);
		builder.append(", name=");
		builder.append(name);
		builder.append(", givenName=");
		builder.append(givenName);
		builder.append(", familyName=");
		builder.append(familyName);
		builder.append(", link=");
		builder.append(link);
		builder.append(", picture=");
		builder.append(picture);
		builder.append(", gender=");
		builder.append(gender);
		builder.append(", locale=");
		builder.append(locale);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result
				+ ((familyName == null) ? 0 : familyName.hashCode());
		result = prime * result + ((gender == null) ? 0 : gender.hashCode());
		result = prime * result
				+ ((givenName == null) ? 0 : givenName.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((link == null) ? 0 : link.hashCode());
		result = prime * result + ((locale == null) ? 0 : locale.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((picture == null) ? 0 : picture.hashCode());
		result = prime * result + (verifiedEmail ? 1231 : 1237);
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
		GoogleUserInfo other = (GoogleUserInfo) obj;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (familyName == null) {
			if (other.familyName != null)
				return false;
		} else if (!familyName.equals(other.familyName))
			return false;
		if (gender == null) {
			if (other.gender != null)
				return false;
		} else if (!gender.equals(other.gender))
			return false;
		if (givenName == null) {
			if (other.givenName != null)
				return false;
		} else if (!givenName.equals(other.givenName))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (link == null) {
			if (other.link != null)
				return false;
		} else if (!link.equals(other.link))
			return false;
		if (locale == null) {
			if (other.locale != null)
				return false;
		} else if (!locale.equals(other.locale))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (picture == null) {
			if (other.picture != null)
				return false;
		} else if (!picture.equals(other.picture))
			return false;
		if (verifiedEmail != other.verifiedEmail)
			return false;
		return true;
	}
}
