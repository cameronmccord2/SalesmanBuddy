package com.salesmanBuddy.model;

public class DeleteLicenseResponse {
	protected String message;
	protected Integer success;
	protected Integer licenseId;
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public Integer getSuccess() {
		return success;
	}
	public void setSuccess(Integer success) {
		this.success = success;
	}
	public Integer getLicenseId() {
		return licenseId;
	}
	public void setLicenseId(Integer licenseId) {
		this.licenseId = licenseId;
	}
}
