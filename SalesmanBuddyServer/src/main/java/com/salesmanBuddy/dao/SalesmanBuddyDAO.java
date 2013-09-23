package com.salesmanBuddy.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.salesmanBuddy.model.Dealerships;
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

	ArrayList<LicensesListElement> deleteLicense(int licenseId);

	boolean userOwnsLicenseId(int licenseId);

	List<StateQuestions> getStateQuestionsForStateId(int stateId);
	
	ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateId(int stateId);
	
	ArrayList<StateQuestionsResponses> getStateQuestionsResponsesForLicenseId(int licenseId);
	
	ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateQuestionId(int stateQuestionId);
	
	ArrayList<StateQuestionsWithResponses> getStateQuestionsWithResponsesForLicenseId(int licenseId);

	File getLicenseImageForPhotoNameBucketName(String photoName, String bucketName);

	File getLicenseImageForPhotoNameBucketId(String photoName, Integer bucketId);

}
