package com.salesmanBuddy.dao;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.BucketsCE;
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.GoogleRefreshTokenResponse;
import com.salesmanBuddy.model.GoogleUserInfo;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.Media;
import com.salesmanBuddy.model.Popups;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.QuestionsAndAnswers;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.UsersName;

public interface SalesmanBuddyDAO {

	String getString();

	ArrayList<States> getAllStates(int getInactiveToo);

	ArrayList<Dealerships> getAllDealerships();

	ArrayList<LicensesListElement> getAllLicensesForUserId(String googleUserId);

	LicensesListElement putLicense(LicensesFromClient licenseFromClient, String googleUserId);

	DeleteLicenseResponse deleteLicense(int licenseId);

	boolean userOwnsLicenseId(int licenseId, String googleUserId);

	File getLicenseImageForPhotoNameBucketId(String photoName, Integer bucketId);

	FinishedPhoto saveStringAsFileForStateId(String data, int stateId, String extension);

	Licenses getLicenseForLicenseId(int licenseId);

	Users getUserByGoogleId(String googleUserId);

	int createUser(Users userFromClient);

	Users getUserById(int userId);

	LicensesListElement updateLicense(LicensesFromClient licenseFromClient, String googleUserId);

	String randomAlphaNumericOfLength(Integer length);

	FinishedPhoto saveFileToS3ForStateId(int stateId, File file);

	ArrayList<QuestionsAndAnswers> getQuestionsAndAnswersForLicenseId(int licenseId, ArrayList<Questions> questions);

	ArrayList<Answers> getAnswersForLicenseId(int licenseId);

	boolean userOwnsQuestionId(int questionId, String googleUserId);

	File getLicenseImageForAnswerId(int answerId);

	Questions getQuestionById(Integer id);

	Questions putQuestion(Questions question);

	Questions updateQuestion(Questions question);

	ArrayList<Questions> getAllQuestions();
	
	List<Users> getAllUsers();

	Users updateUserToType(String googleUserId, int type);

	Users updateUserToDealershipCode(String googleUserId, String dealershipCode);

	List<LicensesListElement> getAllLicensesForDealershipForUserId(String googleUserId);

	Dealerships newDealership(Dealerships dealership);

	Dealerships updateDealership(Dealerships dealership);
	
	void updateRefreshTokenForUser(Users userFromClient);
	
	GoogleRefreshTokenResponse getValidTokenForUser(String googleUserId);
	
	UsersName getUsersName(String googleUserId);

	GoogleUserInfo getGoogleUserInfoWithId(String googleUserId);
	
	GoogleUserInfo getGoogleUserInfo(String tokenType, String accessToken);
	
	
//	trainer stuff

	ArrayList<Captions> putCaptions(List<Captions> captions);

	ArrayList<Captions> getAllCaptionsForMediaIdLanguageId(int mediaId, int languageId);

	Media putMedia(Media media);

	ArrayList<Media> getAllMedia();

	ArrayList<Languages> putLanguages(List<Languages> languages);

	ArrayList<Languages> getAllLanguages(int mtcTaught);

	Media getMediaById(int id);

	List<Popups> getAllPopups();

	List<Popups> getAllPopupsForLanguageId(int languageId);

	List<Popups> getAllPopupsForMediaId(int mediaId);

	List<Popups> getPopupsForMediaIdLanguageId(int languageId, int mediaId);

	Popups newPopup(Popups popup);

	Popups updatePopup(Popups popup);

	int deletePopup(int popupId);

	String saveStringAsFileForCaptionEditor(String data, String extension);

	String saveFileToS3ForCaptionEditor(File file, String extension, int mediaId, int popupId);

	File getFileForMediaId(int mediaId);

	BucketsCE getCaptionEditorBucket();

	List<Popups> putPopups(List<Popups> popups);

	Popups updatePopupWithUploadedFile(String newFilename, Integer bucketId, String extension, int popupId);

	int deleteCaption(int captionId);
}
