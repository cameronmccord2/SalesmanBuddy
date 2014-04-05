package com.salesmanBuddy.model;

public class CombinedUser {
	private GoogleUserInfo google;
	private Users sb;
	
	public CombinedUser(GoogleUserInfo gui, Users user){
		super();
		this.setGoogle(gui);
		this.setSb(user);
	}
	
	public CombinedUser(){
		super();
	}
	
	public GoogleUserInfo getGoogle() {
		return google;
	}
	public void setGoogle(GoogleUserInfo google) {
		this.google = google;
	}
	public Users getSb() {
		return sb;
	}
	public void setSb(Users sb) {
		this.sb = sb;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CombinedUser [google=");
		builder.append(google);
		builder.append(", sb=");
		builder.append(sb);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((google == null) ? 0 : google.hashCode());
		result = prime * result + ((sb == null) ? 0 : sb.hashCode());
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
		CombinedUser other = (CombinedUser) obj;
		if (google == null) {
			if (other.google != null)
				return false;
		} else if (!google.equals(other.google))
			return false;
		if (sb == null) {
			if (other.sb != null)
				return false;
		} else if (!sb.equals(other.sb))
			return false;
		return true;
	}
}
