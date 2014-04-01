package com.salesmanBuddy.model;

import org.codehaus.jettison.json.JSONObject;

import com.salesmanBuddy.exceptions.GoogleRefreshTokenResponseException;

public class GoogleRefreshTokenResponse {

	protected String accessToken;
	protected long expiresIn;
	protected String tokenType;
	protected String refreshToken;
	
	public GoogleRefreshTokenResponse(JSONObject json) throws GoogleRefreshTokenResponseException {
		if(json.optString("error").length() != 0){
			
			String errorMessage = "No error message";
			if(json.optString("error_description") != null)
				errorMessage = json.toString();
			throw new GoogleRefreshTokenResponseException("the GoogleRefreshTokenResponse is in error, message: " + errorMessage + ", body: " + new String(json.toString()));
		}else{
			this.accessToken = json.optString("access_token");
	        if (this.accessToken == null)
	            throw new RuntimeException("Refresh token yielded no access token");

//	        int expiresInSeconds = json.optInt("expires_in");
//	        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
//	        calendar.add(Calendar.SECOND, expiresInSeconds);
//	        this.expiresIn = calendar.getTimeInMillis();
	        this.expiresIn = json.optLong("expires_in");
	        
	        this.tokenType = json.optString("token_type");
	        
	        this.refreshToken = json.optString("refresh_token");
		}
	}
	public String getAccessToken() {
		return accessToken;
	}
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	public long getExpiresIn() {
		return expiresIn;
	}
	public void setExpiresIn(long expiresIn) {
		this.expiresIn = expiresIn;
	}
	public String getTokenType() {
		return tokenType;
	}
	public void setTokenType(String tokenType) {
		this.tokenType = tokenType;
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GoogleRefreshTokenResponse [accessToken=");
		builder.append(accessToken);
		builder.append(", expiresIn=");
		builder.append(expiresIn);
		builder.append(", tokenType=");
		builder.append(tokenType);
		builder.append(", refreshToken=");
		builder.append(refreshToken);
		builder.append("]");
		return builder.toString();
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((accessToken == null) ? 0 : accessToken.hashCode());
		result = prime * result + (int) (expiresIn ^ (expiresIn >>> 32));
		result = prime * result
				+ ((refreshToken == null) ? 0 : refreshToken.hashCode());
		result = prime * result
				+ ((tokenType == null) ? 0 : tokenType.hashCode());
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
		GoogleRefreshTokenResponse other = (GoogleRefreshTokenResponse) obj;
		if (accessToken == null) {
			if (other.accessToken != null)
				return false;
		} else if (!accessToken.equals(other.accessToken))
			return false;
		if (expiresIn != other.expiresIn)
			return false;
		if (refreshToken == null) {
			if (other.refreshToken != null)
				return false;
		} else if (!refreshToken.equals(other.refreshToken))
			return false;
		if (tokenType == null) {
			if (other.tokenType != null)
				return false;
		} else if (!tokenType.equals(other.tokenType))
			return false;
		return true;
	}
}
