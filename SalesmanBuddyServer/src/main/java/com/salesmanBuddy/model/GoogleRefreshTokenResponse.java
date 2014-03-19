package com.salesmanBuddy.model;

import java.util.Calendar;

import org.codehaus.jettison.json.JSONObject;

public class GoogleRefreshTokenResponse {

	protected String accessToken;
	protected long expiresIn;
	protected String tokenType;
	protected boolean inError;
	protected String errorMessage;
	protected String refreshToken;
	
	public GoogleRefreshTokenResponse(JSONObject json) {
		if(json.optString("error").length() != 0){
			this.inError = true;
			
			if(json.optString("error_description") != null)
				this.errorMessage = json.toString();
			
		}else{
			this.accessToken = json.optString("access_token");
	        if (this.accessToken == null)
	            throw new RuntimeException("Refresh token yielded no access token");

	        int expiresInSeconds = json.optInt("expires_in");
	        Calendar calendar = Calendar.getInstance(); // gets a calendar using the default time zone and locale.
	        calendar.add(Calendar.SECOND, expiresInSeconds);
	        this.expiresIn = calendar.getTimeInMillis();
	        
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
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("GoogleRefreshTokenResponse [accessToken=");
		builder.append(accessToken);
		builder.append(", expiresIn=");
		builder.append(expiresIn);
		builder.append(", tokenType=");
		builder.append(tokenType);
		builder.append(", inError=");
		builder.append(inError);
		builder.append(", errorMessage=");
		builder.append(errorMessage);
		builder.append(", refreshToken=");
		builder.append(refreshToken);
		builder.append("]");
		return builder.toString();
	}
	public String getRefreshToken() {
		return refreshToken;
	}
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}
}
