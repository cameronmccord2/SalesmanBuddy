package com.salesmanBuddy.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.salesmanBuddy.model.ContactInfo;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestions;
import com.salesmanBuddy.model.StateQuestionsResponses;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.StateQuestionsWithResponses;
import com.salesmanBuddy.model.States;

public interface SalesmanBuddyDAO {

	String getString();

	ArrayList<States> getAllStates(int getInactiveToo);

	ArrayList<Dealerships> getAllDealerships();

	ArrayList<LicensesListElement> getAllLicensesForUserId(int userId);

	ArrayList<LicensesListElement> putLicense(LicensesFromClient licenseFromClient);

	Integer deleteLicense(int licenseId);

	boolean userOwnsLicenseId(int licenseId);

	List<StateQuestions> getStateQuestionsForStateId(int stateId);
	
	ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateId(int stateId);
	
	ArrayList<StateQuestionsResponses> getStateQuestionsResponsesForLicenseId(int licenseId);
	
	ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateQuestionId(int stateQuestionId);
	
	ArrayList<StateQuestionsWithResponses> getStateQuestionsWithResponsesForLicenseId(int licenseId);

	File getLicenseImageForPhotoNameBucketId(String photoName, Integer bucketId);

	String saveStringAsFileForStateId(String data, int stateId, String extension);

	ContactInfo getContactInfoForLicenseId(int licenseId);

	ContactInfo getContactInfoForContactInfoId(int contactInfoId);

	File getLicenseImageForLicenseId(int licenseId);

	Licenses getLicenseForLicenseId(int licenseId);

	Integer putContactInfo(ContactInfo contactInfo);

}
