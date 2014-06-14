package com.salesmanBuddy.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.regions.Regions;
import com.salesmanBuddy.exceptions.GoogleRefreshTokenResponseException;
import com.salesmanBuddy.exceptions.GoogleUserInfoException;
import com.salesmanBuddy.exceptions.InvalidUserTreeType;
import com.salesmanBuddy.exceptions.MalformedSBEmailException;
import com.salesmanBuddy.exceptions.NoBucketFoundException;
import com.salesmanBuddy.exceptions.NoResultInResultSet;
import com.salesmanBuddy.exceptions.NoSqlResultsException;
import com.salesmanBuddy.exceptions.UserNameException;
import com.salesmanBuddy.model.Buckets;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.ErrorMessage;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.GoogleRefreshTokenResponse;
import com.salesmanBuddy.model.GoogleToken;
import com.salesmanBuddy.model.GoogleUserInfo;
import com.salesmanBuddy.model.ImageDetails;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.SBEmail;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.StockNumbers;
import com.salesmanBuddy.model.UserTree;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.QuestionsAndAnswers;
import com.salesmanBuddy.model.UsersName;

public class JDBCSalesmanBuddyDAO extends SharedDAO {
	
	static final private int isImage = 1;
	static final private int isText = 2;
	static final private int isBool = 3;
	static final private int isDropdown = 4;
	
	private static final Integer QUESTION_STOCK_NUMBER = 2;
	private final static Integer QUESTION_FIRST_NAME_TAG = 3;
	private final static Integer QUESTION_LAST_NAME_TAG = 4;
	
	private static final String GoogleClientIdWeb = "38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com";
	private static final String GoogleClientSecretWeb = "NRheOilfAEKqTatHltqNhV2y";
	private static final String GoogleClientIdAndroid = "";
	private static final String GoogleClientSecretAndroid = "";
	private static final String GoogleClientIdiOS = "38235450166-dgbh1m7aaab7kopia2upsdj314odp8fc.apps.googleusercontent.com";
	private static final String GoogleClientSecretiOS = "zC738ZbMHopT2C1cyKiKDBQ6";
	private static final String GoogleTokenEndpoint = "https://accounts.google.com/o/oauth2/token";
	private static final String GoogleUserEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";
	private static final String GoogleRefreshTokenEndpoint = "https://accounts.google.com/o/oauth2/token";
	private static final String SUPPORT_EMAIL = "support@salesmanbuddy.com";
	private static final String TEST_DRIVE_NOW_EMAIL = "reports@salesmanbuddy.com";
	private static final String REPORTS_EMAIL = "reports@salesmanbuddy.com";
	private static final String ERRORED_EMAIL = "errored@salesmanbuddy.com";
	
	public static final int ON_TEST_DRIVE_EMAIL_TYPE = 1;
	public static final int DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 2;
	public static final int DAILY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE = 14;
	public static final int DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 4;
	public static final int WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 5;
	public static final int WEEKLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE = 15;
	public static final int WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 7;
	public static final int BI_MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 8;
	public static final int BI_MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE = 9;
	public static final int BI_MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 10;
	public static final int MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 11;
	public static final int MONTHLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE = 16;
	public static final int MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 13;
	public static final int DAILY_SALESMAN_SUMMARY_EMAIL_TYPE = 3;
	public static final int WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE = 6;
	public static final int MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE = 12;
	public static final int DAILY_STOCK_NUMBERS_EMAIL_TYPE = 17;
	public static final int WEEKLY_STOCK_NUMBERS_EMAIL_TYPE = 18;
	public static final int MONTHLY_STOCK_NUMBERS_EMAIL_TYPE = 19;
	
	public static final Integer DAILY_TYPE = 1;// night
	public static final Integer WEEKLY_TYPE = 2;// monday
//	public static final Integer BI_MONTHLY_TYPE = 3;// 1, 15
	public static final Integer MONTHLY_TYPE = 4;// 1
	public static final Integer SO_FAR_MONTH_TYPE = 5;
	
	public static final Integer DEALERSHIP_TYPE = 1;
	public static final Integer SALESMAN_TYPE = 2;
	public static final Integer TEST_DRIVE_TYPE = 3;
	
	
	public final static Integer USER_TREE_TYPE = 1;
	public final static Integer SUPERVISOR_TREE_TYPE = 2;
	
	private static final Integer STOCK_NUMBER_NORMAL = 0;
	private static final Integer STOCK_NUMBER_SOLD = 1;
	
	enum ReportBeginEnd{
		AllSalesmen, DealershipSummary, Warnings, TestDriveNow, TestDriveSummary, StockNumberSummary
	};
	
	
	
	public JDBCSalesmanBuddyDAO(){
		super();
	}

	
	private Buckets getBucketForStateId(int stateId) throws NoBucketFoundException{
		final String sql = "SELECT * FROM buckets WHERE stateId = ?";
		List<Buckets> results = this.getList(sql, Buckets.class, stateId);
//		List<Buckets> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, stateId);
//			ResultSet resultSet = statement.executeQuery();
//			results = Buckets.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(results.size() > 1)
			throw new RuntimeException("There is more than one bucket for state: " + stateId);
		if(results.size() == 1)
			return results.get(0);
		throw new NoBucketFoundException("No bucket found for stateId: " + stateId);
	}
	
	public FinishedPhoto saveFileToS3ForStateId(int stateId, File file){
		try {
			if(file == null)
				throw new RuntimeException("file trying to save to s3 is null");
			FinishedPhoto fp = new FinishedPhoto();
			Buckets stateBucket = this.getBucketForStateId(stateId);
			if(stateBucket == null){
				this.makeBucketForStateId(stateId);
				stateBucket = this.getBucketForStateId(stateId);
			}
			if(stateBucket.getName() == null){
				throw new RuntimeException("statebucket name is null");
			}
			fp.setBucketId(stateBucket.getId());
			fp.setFilename(this.addFileToBucket(stateBucket.getName(), this.randomAlphaNumericOfLength(15), file));
			return fp;
			
		} catch (NoBucketFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
	}
	
	private String makeBucketForStateId(int stateId){
		String bucketName = "state-" + this.getStateNameForStateId(stateId).toLowerCase() + "-uuid-" + UUID.randomUUID();
		bucketName = this.createS3Bucket(bucketName, Regions.US_WEST_2);
		
		final String sql = "INSERT INTO buckets (stateId, name) VALUES (?, ?)";
		try {
			this.insertRow(sql, "id", stateId, bucketName);
			return bucketName;
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to make bucket for state id: " + stateId);
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, stateId);
//			statement.setString(2, bucketName);
//			statement.execute();
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(i == 0)
//			throw new RuntimeException("failed to make bucket for state id: " + stateId);
//		return bucketName;
	}
	
	private String getStateNameForStateId(int stateId) {
		States state = this.getStateForId(stateId);
		if(state == null)
			throw new RuntimeException("could not find state for id: " + stateId);
		return state.getName();
	}

	public List<States> getAllStates(int getInactiveToo) {// working 10/3/13
//		if(getInactiveToo > 0)
//			return this.getList("states", "name", null, States.class);
//		return this.getList("states", "status", 1, "name", null, States.class);
		String sql = "SELECT * FROM states WHERE status = 1";
		if(getInactiveToo > 0)
			sql = "SELECT * FROM states";
		return this.getList(sql, States.class);
//		List<States> states = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			ResultSet resultSet = statement.executeQuery();
//			states = States.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return states;
	}
	
	public States getStateForId(Integer stateId) {
		try {
			return this.getRow("SELECT * FROM states WHERE id = ?", States.class, stateId);
//			return this.getRow("states", "id", stateId, States.class);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("StateId: " + stateId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM states WHERE id = ?";
//		States result = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, stateId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			result = States.parseOneRowResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return result;
	}

	public List<Dealerships> getAllDealerships() {// working 10/3/13
		return this.getList("SELECT * FROM dealerships", Dealerships.class);
//		return this.getList("dealerships", "id", null, Dealerships.class);
//		final String sql = "SELECT * FROM dealerships";
//		List<Dealerships> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			ResultSet resultSet = statement.executeQuery();
//			results = Dealerships.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public Dealerships getDealershipWithDealershipCode(String dealershipCode) {
		try {
//			return this.getRow("dealerships", "dealershipCode", dealershipCode, Dealerships.class);
			return this.getRow("SELECT * FROM dealerships WHERE dealershipCode = ?", Dealerships.class, dealershipCode);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("dealershipCode: " + dealershipCode + ", error: " + e);
		}
//		final String sql = "SELECT * FROM dealerships WHERE dealershipCode = ?";
//		Dealerships result = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, dealershipCode);
//			
//			ResultSet resultSet = statement.executeQuery();
//			result = Dealerships.parseOneRowResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return result;
	}

	


	public List<LicensesListElement> getAllLicensesForUserId(String googleUserId, boolean getSubData) {
		final String sql = "SELECT * FROM licenses WHERE userId = (SELECT id FROM users WHERE googleUserId = ?) AND showInUserList = 1 ORDER BY created desc";
		List<LicensesListElement> results = this.getList(sql, LicensesListElement.class, googleUserId);
		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			ResultSet resultSet = statement.executeQuery();
//			results = LicensesListElement.class.newInstance().parseResultSetAll(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		} catch (InstantiationException e) {
//			throw new RuntimeException(e);
//		} catch (IllegalAccessException e) {
//			throw new RuntimeException(e);
//		}
		
		if(getSubData){
			List<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
			for(int i = 0; i < results.size(); i++){
				results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId(), questions));
			}
		}
		
		return results;
	}
	
	public List<LicensesListElement> getAllLicenses() {
//		return this.getList("licenses", "created", "desc", LicensesListElement.class);
		return this.getList("SELECT * FROM licenses ORDER BY created DESC", LicensesListElement.class);
	}
	
	public void addQuestionsAndAnswersToLicenseListElements(List<LicensesListElement> list){
		List<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
		for(int i = 0; i < list.size(); i++){
			list.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(list.get(i).getId(), questions));
		}
	}
	
	private LicensesListElement getLicenseListElementForLicenseId(Integer licenseId) {
		LicensesListElement result;
		try {
//			result = this.getRow("licenses", "id", id, LicensesListElement.class);
			result = this.getRow("SELECT * FROM licenses WHERE id = ?", LicensesListElement.class, licenseId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("id: " + licenseId + ", error: " + e.getLocalizedMessage());
		}
		
		List<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
		result.setQaas(this.getQuestionsAndAnswersForLicenseId(result.getId(), questions));
//		final String sql = "SELECT * FROM licenses WHERE id = ?";
//		List<LicensesListElement> results = new ArrayList<>();
//		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = LicensesListElement.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		throw new RuntimeException("couldnt find the license by id: " + licenseId);
	}

	


	public FinishedPhoto saveStringAsFileForStateId(String data, int stateId, String extension) {// working 10/3/13
		File f = null;
		Writer writer = null;
		FinishedPhoto fp = null;
		try {
			f = File.createTempFile(this.randomAlphaNumericOfLength(15), extension);
			f.deleteOnExit();
			writer = new OutputStreamWriter(new FileOutputStream(f));
			writer.write(data);
			writer.close();
			fp = this.saveFileToS3ForStateId(stateId, f);
			
		} catch (IOException e) {
			throw new RuntimeException("IOException for saveStringAsFileForStateId, error: " + e.getLocalizedMessage());
		}finally{
			if(f != null)
				f.delete();
		}
		if(fp == null || fp.getFilename() == null)
			throw new RuntimeException("failed to save data");
		return fp;
	}
	
	private int putLicenseInDatabase(Licenses license) throws NoSqlResultsException{
		final String sql = "INSERT INTO licenses (longitude, latitude, userId, stateId) VALUES (?, ?, ?, ?)";
		return this.insertRow(sql, "id", license.getLongitude(), license.getLatitude(), license.getUserId(), license.getStateId());
//		int id = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setFloat(1, license.getLongitude());
//			statement.setFloat(2, license.getLatitude());
//			statement.setInt(3, license.getUserId());
//			statement.setInt(4, license.getStateId());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			id = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return id;
	}
	
	public LicensesListElement putLicense(LicensesFromClient licenseFromClient, String googleUserId) {
		Users user = this.getUserByGoogleId(googleUserId);
		int licenseId = 0;
		if(user == null)
			throw new RuntimeException("couldnt find user for google id: " + googleUserId);
		licenseFromClient.setUserId(user.getId());
		if(licenseFromClient.getUserId() == 0)
			throw new RuntimeException("userid is " + licenseFromClient.getUserId() + ", its invalid");

		Licenses l = new Licenses(licenseFromClient);
		try {
			licenseId = this.putLicenseInDatabase(l);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to put license in database, licenseid returned: " + licenseId + ", license: " + l.toString());
		}
//		if(licenseId == 0)
//			throw new RuntimeException("failed to put license in database, licenseid returned: " + licenseId + ", license: " + l.toString());
		
		for(QuestionsAndAnswers qaa : licenseFromClient.getQaas()){
			qaa.getAnswer().setLicenseId(licenseId);
			if(this.putAnswerInDatabase(qaa.getAnswer()) == 0)
				throw new RuntimeException("Failed to insert answer into database, " + qaa.getAnswer().toString());
		}

		this.sendEmailsAboutTestDriveForGoogleUserIdLicenseId(googleUserId, licenseId);
		return this.getLicenseListElementForLicenseId(licenseId);
	}

	public DeleteLicenseResponse deleteLicense(int licenseId) {
		int i = this.updateShowInUserListForLicenseId(licenseId, 0);
		DeleteLicenseResponse dlr = new DeleteLicenseResponse();
		dlr.setLicenseId(licenseId);
		if(i != 0){
			dlr.setMessage("Success, user wont see license anymore, rows edited: " + i);
			dlr.setSuccess(1);
		}else{
			dlr.setMessage("failure, rows edited: " + i);
			dlr.setSuccess(0);
		}
		return dlr;
	}
	
	private Integer updateShowInUserListForLicenseId(int licenseId, int showInUserList){
		if(!(showInUserList == 1 || showInUserList == 0))
			throw new RuntimeException("updateShowInUserListForLicenseId failed because showInUserList was not 0 or 1");
		final String sql = "UPDATE licenses SET showInUserList = ? WHERE id = ?";
		return this.updateRow(sql, showInUserList, licenseId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, showInUserList);
//			statement.setInt(2, licenseId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return i;
	}
	
	public boolean userOwnsLicenseId(int licenseId, String googleUserId) {
		final String sql = "SELECT * FROM licenses WHERE id = ? AND userId = (SELECT id FROM users WHERE googleUserId = ?)";
		List<Licenses> results = this.getList(sql, Licenses.class, licenseId, googleUserId);
		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, licenseId);
//			statement.setString(2, googleUserId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(results.size() > 0)
			return true;
		return false;
	}

	private Buckets getBucketForBucketId(Integer bucketId) {
		try{
//			return this.getRow("buckets", "id", bucketId, Buckets.class);
			return this.getRow("SELECT * FROM buckets WHERE id = ?", Buckets.class, bucketId);
		}catch(NoSqlResultsException e){
			throw new RuntimeException("Couldnt get bucket by id: " + bucketId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM buckets WHERE id = ?";
//		List<Buckets> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, bucketId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Buckets.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() != 1)
//			throw new RuntimeException("expected number of buckets for bucket id to be 1, id: " + bucketId + ", got: " + results.size());
//		return results.get(0);
	}
	
	public Licenses getLicenseForLicenseId(int licenseId) {
		try{
//			return this.getRow("licenses", "id", licenseId, Licenses.class);
			return this.getRow("SELECT * FROM licenses WHERE id = ?", Licenses.class, licenseId);
		}catch(NoSqlResultsException e){
			throw new RuntimeException("Couldnt get license by id: " + licenseId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM licenses WHERE id = ?";
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, licenseId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() > 0)
//			return results.get(0);
//		throw new RuntimeException("licenseId does not match any in the database, id: " + licenseId);
	}
	
	public File getLicenseImageForPhotoNameBucketId(String photoName,Integer bucketId) {
		Buckets bucket = this.getBucketForBucketId(bucketId);
		return this.getFileFromBucket(photoName, bucket.getName(), ".jpeg", this.randomAlphaNumericOfLength(15), Regions.US_WEST_2);
	}

	public Users getUserByGoogleId(String googleUserId) {
		try {
//			return this.getRow("users", "googleUserId", googleUserId, Users.class);
			return this.getRow("SELECT * FROM users WHERE googleUserId = ?", Users.class, googleUserId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Couldnt get user by google id: " + googleUserId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM users WHERE googleUserId = ?";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() > 0)
//			return results.get(0);
//		return null;
	}
	
	public int createUser(Users user) {
		final String sql = "INSERT INTO users (deviceType, type, googleUserId, refreshToken) VALUES(?, ?, ?, ?)";
		try {
			return this.insertRow(sql, "id", user.getDeviceType(), 1, user.getGoogleUserId(), user.getRefreshToken());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed inserting user, user: " + user.toString());
		}
//		int id = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, user.getDeviceType());
//			statement.setInt(2, 1);// 1:normal/salesman, 2:can see all dealership users, 3:salesman buddy employees
//			statement.setString(3, user.getGoogleUserId());
//			statement.setString(4, user.getRefreshToken());
//			statement.executeUpdate();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			id = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(id == 0)
//			throw new RuntimeException("failed inserting user, user: " + user.toString());
//		return id;
	}
	
	public Users getUserById(Integer userId) {
		try {
//			return this.getRow("users", "id", userId, Users.class);
			return this.getRow("SELECT * FROM users WHERE id = ?", Users.class, userId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("cant find user by id: " + userId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM users WHERE id = ?";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, userId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() > 0)
//			return results.get(0);
//		return null;
	}
	
	public LicensesListElement updateLicense(LicensesFromClient licenseFromClient, String googleUserId) {
		if(licenseFromClient.getId() == null || licenseFromClient.getId() == 0)
			throw new RuntimeException("id is either null or 0: " + licenseFromClient.toString());
//		this.updateShowInUserListForLicenseId(licenseFromClient.getId(), licenseFromClient.getShowInUserList());
		for(QuestionsAndAnswers qaa : licenseFromClient.getQaas()){
			this.updateAnswerInDatabase(qaa.getAnswer());
		}
		return this.getLicenseListElementForLicenseId(licenseFromClient.getId());
	}

	public List<QuestionsAndAnswers> getQuestionsAndAnswersForLicenseId(int licenseId, List<Questions> questions) {
		List<Answers> answers = this.getAnswersForLicenseId(licenseId);
		this.addDetailsToAnswers(answers);
		List<QuestionsAndAnswers> qas = new ArrayList<>();
		for(Answers a : answers){
			QuestionsAndAnswers qa = new QuestionsAndAnswers();
			qa.setAnswer(a);
//			qa.setQuestion(this.getQuestionById(a.getQuestionId()));// dont get it from the db every time, many db calls
			qa.setQuestion(this.getQuestionFromListById(questions, a.getQuestionId()));// just get question from the pregotten questions
			qas.add(qa);
		}
		return qas;
	}
	
	private Questions getQuestionFromListById(List<Questions> questions, int id){
		for(Questions q : questions){
			if(q.getId() == id)
				return q;
		}
		return null;
	}
	
	private void addDetailsToAnswers(List<Answers> answers) {
		for(Answers a : answers){
			if(a.getAnswerType() == JDBCSalesmanBuddyDAO.isImage) {
				a.setImageDetails(this.getImageDetailsForAnswerId(a.getId()));
			}
		}
	}
	
	public List<Answers> getAnswersForLicenseId(int licenseId) {
//		return this.getList("answers", "licenseId", licenseId, null, null, Answers.class);
		return this.getList("SELECT * FROM answers WHERE licenseId = ?", Answers.class, licenseId);
//		final String sql = "SELECT * FROM answers WHERE licenseId = ?";
//		List<Answers> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, licenseId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Answers.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		
//		return results;
	}

	private ImageDetails getImageDetailsForAnswerId(Integer answerId) {
		try {
//			return this.getRow("imageDetails", "answerId", answerId, ImageDetails.class);
			return this.getRow("SELECT * FROM imageDetails WHERE answerId = ?", ImageDetails.class, answerId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant get image details for answer id: " + answerId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM imageDetails WHERE answerId = ?";
//		List<ImageDetails> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, answerId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = ImageDetails.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() == 1)
//			return results.get(0);
//		return null;
	}
	
	private int updateAnswerInDatabase(Answers answer) {
		final String sql = "UPDATE answers SET answerBool = ?, answerType = ?, answerText = ?, licenseId = ?, questionId = ? WHERE id = ?";
		int i = this.updateRow(sql, answer.getAnswerBool(), answer.getAnswerType(), answer.getAnswerText(), answer.getLicenseId(), answer.getQuestionId(), answer.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, answer.getAnswerBool());
//			statement.setInt(2, answer.getAnswerType());
//			statement.setString(3, answer.getAnswerText());
//			statement.setInt(4, answer.getLicenseId());
//			statement.setInt(5, answer.getQuestionId());
//			statement.setInt(6, answer.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("update answers failed for id: " + answer.getId());
		if(answer.getAnswerType() == JDBCSalesmanBuddyDAO.isImage)
			this.updateImageDetailsInDatabase(answer.getImageDetails());
		return i;
	}
	
	private int updateImageDetailsInDatabase(ImageDetails imageDetails) {
		final String sql = "UPDATE imageDetails SET photoName = ?, bucketId = ? WHERE id = ?";
		int i = this.updateRow(sql, imageDetails.getPhotoName(), imageDetails.getBucketId(), imageDetails.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, imageDetails.getPhotoName());
//			statement.setInt(2, imageDetails.getBucketId());
//			statement.setInt(3, imageDetails.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("update imageDetails failed for id: " + imageDetails.getId());
		return i;
	}

	private Integer updateQuestionInDatabase(Questions q){
		final String sql = "UPDATE questions SET version = ?, questionOrder = ?, questionTextEnglish = ?, questionTextSpanish = ?, required = ?, questionType = ? WHERE id = ?";
		return this.updateRow(sql, q.getVersion(), q.getQuestionOrder(), q.getQuestionTextEnglish(), q.getQuestionTextSpanish(), q.getRequired(), q.getQuestionType(), q.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, q.getVersion());
//			statement.setInt(2, q.getQuestionOrder());
//			statement.setString(3, q.getQuestionTextEnglish());
//			statement.setString(4, q.getQuestionTextSpanish());
//			statement.setInt(5, q.getRequired());
//			statement.setInt(6, q.getQuestionType());
//			statement.setInt(7, q.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return i;
	}
	
	private int putQuestionInDatabase(Questions q){
		final String sql = "INSERT INTO questions (version, questionOrder, questionTextEnglish, questionTextSpanish, required, questionType) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			return this.insertRow(sql, "id", q.getVersion(), q.getQuestionOrder(), q.getQuestionTextEnglish(), q.getQuestionTextSpanish(), q.getRequired(), q.getQuestionType());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Insert question failed: " + q.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, q.getVersion());
//			statement.setInt(2, q.getQuestionOrder());
//			statement.setString(3, q.getQuestionTextEnglish());
//			statement.setString(4, q.getQuestionTextSpanish());
//			statement.setInt(5, q.getRequired());
//			statement.setInt(6, q.getQuestionType());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return i;
	}
	
	private int putAnswerInDatabase(Answers answer) {
		final String sql = "INSERT INTO answers (answerText, answerBool, licenseId, questionId, answerType) VALUES (?, ?, ?, ?, ?)";
		int i;
		try {
			i = this.insertRow(sql, "id", answer.getAnswerText(), answer.getAnswerBool(), answer.getLicenseId(), answer.getQuestionId(), answer.getAnswerType());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to insert answer into database, " + answer.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			if(answer.getAnswerText() == null)
//				answer.setAnswerText("");
//			statement.setString(1, answer.getAnswerText());
//			statement.setInt(2, answer.getAnswerBool());
//			statement.setInt(3, answer.getLicenseId());
//			statement.setInt(4, answer.getQuestionId());
//			statement.setInt(5, answer.getAnswerType());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(i == 0)
//			throw new RuntimeException("failed to insert answer into database, " + answer.toString());
		if(answer.getAnswerType() == 1){
			answer.getImageDetails().setAnswerId(i);
			if(this.putImageDetailsInDatabase(answer.getImageDetails()) == 0)
				throw new RuntimeException("failed to insert image details into database, " + answer.getImageDetails().toString());
		}
		return i;
	}

	public Questions getQuestionById(Integer questionId) {
		try {
//			return this.getRow("questions", "id", questionId, Questions.class);
			return this.getRow("SELECT * FROM questions WHERE id = ?", Questions.class, questionId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant get question by id: " + questionId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM questions WHERE id = ?";
//		List<Questions> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, questionId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Questions.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() == 1)
//			return results.get(0);
//		return null;
	}
	
	public List<Questions> getAllQuestions() {
		return this.getList("SELECT * FROM questions ORDER BY questionOrder", Questions.class);
//		return this.getList("questions", "questionOrder", null, Questions.class);
//		final String sql = "SELECT * FROM questions ORDER BY questionOrder";
//		List<Questions> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			ResultSet resultSet = statement.executeQuery();
//			results = Questions.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public boolean userOwnsQuestionId(int questionId, String googleUserId) {
		// TODO Auto-generated method stub
		return true;
	}

	public File getLicenseImageForAnswerId(int answerId) {
		ImageDetails imageDetails = this.getImageDetailsForAnswerId(answerId);
		return this.getLicenseImageForPhotoNameBucketId(imageDetails.getPhotoName(), imageDetails.getBucketId());
	}
	
	private int putImageDetailsInDatabase(ImageDetails imageDetails){
		final String sql = "INSERT INTO imageDetails (photoName, bucketId, answerId) VALUES (?, ?, ?)";
		try {
			return this.insertRow(sql, "id", imageDetails.getPhotoName(), imageDetails.getBucketId(), imageDetails.getAnswerId());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert imageDetails failed, " + imageDetails.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setString(1, imageDetails.getPhotoName());
//			statement.setInt(2, imageDetails.getBucketId());
//			statement.setInt(3, imageDetails.getAnswerId());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle + ", imageDetials: " + imageDetails.toString());
//		}
//		if(i == 0)
//			throw new RuntimeException("insert imageDetails failed, " + imageDetails.toString());
//		return i;
	}

	public Questions putQuestion(Questions question) {
		this.putQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}

	public Questions updateQuestion(Questions question) {
		this.updateQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}
	
	public List<Users> getAllUsers() {
		return this.getList("SELECT * FROM users", Users.class);
//		return this.getList("users", null, null, Users.class);
//		final String sql = "SELECT * FROM users";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	public List<Users> getUsersForDealershipId(Integer dealershipId) {
		return this.getList("SELECT * FROM users WHERE dealershipId = ?", Users.class, dealershipId);
//		return this.getList("users", "dealershipId", dealershipId,  null, null, Users.class);
//		final String sql = "SELECT * FROM users WHERE dealershipId = ?";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, dealershipId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public Users updateUserToType(String googleUserId, int type) {
		final String sql = "UPDATE users SET type = ? WHERE googleUserId = ?";
		int i = this.updateRow(sql, type, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, type);
//			statement.setString(2, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to update googleUserId: " + googleUserId);
		return this.getUserByGoogleId(googleUserId);
	}

	public Users updateUserToDealershipCode(String googleUserId, String dealershipCode) {
		int dealershipId = this.getDealershipByDealershipCode(dealershipCode).getId();
		final String sql = "UPDATE users SET dealershipId = ? WHERE googleUserId = ?";
		int i = this.updateRow(sql, dealershipId, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setString(2, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException("dealershipId: " + dealershipId + ", " + sqle.getLocalizedMessage());
//		}
		if(i == 0)
			throw new RuntimeException("failed to update googleUserId: " + googleUserId);
		return this.getUserByGoogleId(googleUserId);
	}
	
	public Users updateUserToDealershipCodeType(String googleUserId, String dealershipCode, int type) {
		int dealershipId = this.getDealershipByDealershipCode(dealershipCode).getId();
		final String sql = "UPDATE users SET dealershipId = ?, type = ? WHERE googleUserId = ?";
		int i = this.updateRow(sql, dealershipId, type, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setInt(2, type);
//			statement.setString(3, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException("dealershipId: " + dealershipId + ", type:" + type + ", " + sqle.getLocalizedMessage());
//		}
		if(i == 0)
			throw new RuntimeException("failed to update googleUserId: " + googleUserId);
		return this.getUserByGoogleId(googleUserId);
	}

	private Dealerships getDealershipByDealershipCode(String dealershipCode) {
		try {
//			return this.getRow("dealerships", "dealershipCode", dealershipCode, Dealerships.class);
			return this.getRow("SELECT * FROM dealerships WHERE dealershipCode = ?", Dealerships.class, dealershipCode);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to get dealership by dealershipCode: " + dealershipCode + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM dealerships WHERE dealershipCode = ?";
//		List<Dealerships> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, dealershipCode);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Dealerships.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() == 1)
//			return results.get(0);
		
	}

	public List<LicensesListElement> getAllLicensesForDealershipId(Integer dealershipId, boolean getSubData) {
//		List<Users> users =  this.getList("users", "dealershipId", dealershipId, null, null, Users.class);
		List<Users> users = this.getList("SELECT * FROM users WHERE dealershipId = ?", Users.class, dealershipId);
//		final String sql = "SELECT * FROM users WHERE dealershipId = ?";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		List<LicensesListElement> licenses = new ArrayList<>();
		for(Users u : users){
			licenses.addAll(this.getAllLicensesForUserId(u.getGoogleUserId(), getSubData));
		}
		return licenses;
	}
	
	public Dealerships getDealershipById(Integer dealershipId) {
		try {
//			return this.getRow("dealerships", "id", dealershipId, Dealerships.class);
			return this.getRow("SELECT * FROM dealerships WHERE id = ?", Dealerships.class, dealershipId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to get dealership by dealershipId: " + dealershipId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM dealerships WHERE id = ?";
//		List<Dealerships> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Dealerships.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(results.size() == 1)
//			return results.get(0);
//		throw new RuntimeException("failed to get the dealership by id: " + dealershipId);
	}
	
	public Dealerships newDealership(Dealerships dealership) {
		final String sql = "INSERT INTO dealerships (name, city, stateId, dealershipCode, notes) VALUES (?, ?, ?, ?, ?)";
		int i;
		try {
			i = this.insertRow(sql, "id", dealership.getName(), dealership.getCity(), dealership.getStateId(), UUID.randomUUID().toString(), dealership.getNotes());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert dealership failed: " + dealership.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setString(1, dealership.getName());
//			statement.setString(2, dealership.getCity());
//			statement.setInt(3, dealership.getStateId());
//			statement.setString(4, UUID.randomUUID().toString());
//			statement.setString(5, dealership.getNotes());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(i == 0)
//			throw new RuntimeException("insert dealership failed");
		return this.getDealershipById(i);
	}
	
	public Dealerships updateDealership(Dealerships dealership) {
		final String sql = "UPDATE dealerships SET name = ?, city = ?, stateId = ?, notes = ? WHERE id = ?";
		int i = this.updateRow(sql, dealership.getName(), dealership.getCity(), dealership.getStateId(), dealership.getNotes(), dealership.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, dealership.getName());
//			statement.setString(2, dealership.getCity());
//			statement.setInt(3, dealership.getStateId());
//			statement.setString(4, dealership.getNotes());
//			statement.setInt(5, dealership.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to update dealership: " + dealership.toString());
		return this.getDealershipById(dealership.getId());
	}
	
	public void updateRefreshTokenForUser(Users userFromClient) {
		if(userFromClient.getDeviceType() < 1 || userFromClient.getDeviceType() > 3)
			throw new RuntimeException("their device type is not within the range 1-3, user: " + userFromClient.toString());
		
		final String sql = "UPDATE users SET refreshToken = ?, deviceType = ? WHERE id = ?";
		int i = this.updateRow(sql, userFromClient.getRefreshToken(), userFromClient.getDeviceType(), userFromClient.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, userFromClient.getRefreshToken());
//			statement.setInt(2, userFromClient.getDeviceType());
//			statement.setInt(3, userFromClient.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to update user's refresh token, refreshToken length: " + userFromClient.getRefreshToken().length() + ", userFromClient: " + userFromClient.toString());
		return;
	}
	
	public GoogleRefreshTokenResponse codeForToken(String code, String redirectUri, String state) throws GoogleRefreshTokenResponseException {
		String webString = "code=" + code +
				"&client_id=" + GoogleClientIdWeb +
				"&client_secret=" + GoogleClientSecretWeb +
				"&redirect_uri=" + redirectUri + 
				"&grant_type=authorization_code";

		String responseBody = this.postRequest(webString, GoogleTokenEndpoint, "application/x-www-form-urlencoded");
		
		JSONObject json = null;
		try{
			json = new JSONObject(responseBody);
		}catch(JSONException e){
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
		GoogleRefreshTokenResponse grtr = new GoogleRefreshTokenResponse(json);
		return grtr;
	}
	
	private String postRequest(String postData, String baseUrl, String contentType){
		 
		// Connect to google.com
		URL url;
		StringBuilder responseSB = new StringBuilder();
		try {
			url = new URL(baseUrl);
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST");
			connection.setRequestProperty("Content-Type", contentType);
			connection.setRequestProperty("Content-Length",  String.valueOf(postData.length()));
	//		connection.setRequestProperty("Accept", "application/json");
			 
			// Write data
			OutputStream os = connection.getOutputStream();
			os.write(postData.getBytes());
			 
			// Read response
			
			BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
			  
			String line;
			while ( (line = br.readLine()) != null)
				responseSB.append(line);
					 
			// Close streams
			br.close();
			os.close();
		} catch (MalformedURLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		} catch (ProtocolException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		} catch (IOException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getLocalizedMessage());
		}
		 
		return responseSB.toString();
	}
	
	private GoogleToken getTokenForUserFromCache(Integer userId){
//		List<GoogleToken> tokens = this.getList("tokens", "userId", userId, "expiresAt", "DESC", GoogleToken.class);
		List<GoogleToken> tokens = this.getList("SELECT * FROM tokens WHERE userid = ? order by expiresAt DESC", GoogleToken.class, userId);
//		final String sql = "SELECT * FROM tokens WHERE userid = ? order by expiresAt DESC";
//		GoogleToken gt = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, userId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			gt = GoogleToken.parseOneRowResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
////		java.util.Date now = new java.util.Date();
//		if(gt == null)
//			return null;
		if(tokens.size() == 0)
			return null;
		GoogleToken gt = tokens.get(0);
		DateTime expiresAt = new DateTime(gt.getExpiresAt()).minusMinutes(1);
//		DateTime expiresAt = new DateTime().plusSeconds((int)gt.getExpiresAt()).minusMinutes(1);
		DateTime now = new DateTime();
		if(expiresAt.isAfter(now))
			return gt;
		return null;
	}
	
	public int saveGoogleTokenInCache(GoogleRefreshTokenResponse grtr, Users user) {
		final String sql = "INSERT INTO tokens (userId, token, expiresAt, type) VALUES (?, ?, ?, ?)";
		DateTime expiresAt = new DateTime().plusSeconds((int)grtr.getExpiresIn());
		try {
			return this.insertRow(sql, "id", user.getId(), grtr.getTokenType() + " " + grtr.getAccessToken(), expiresAt.getMillis(), user.getType());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert into tokens failed, sql: " + sql + ", expiresAt: " + expiresAt.getMillis() + ", grtr: " + grtr.toString() + ", user: " + user.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, user.getId());
//			statement.setString(2, grtr.getTokenType() + " " + grtr.getAccessToken());
//			
//			statement.setLong(3, expiresAt.getMillis());
//			statement.setInt(4, user.getType());
//
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException("expiresAt: " + expiresAt.getMillis() + ", grtr: " + grtr.toString() + ", user: " + user.toString() + ", error: " + sqle.getLocalizedMessage());
//		}
//		if(i == 0)
//			throw new RuntimeException("insert token failed");
//		return i;
	}
	
	public GoogleToken getValidTokenForUser(String googleUserId, Users user) throws GoogleRefreshTokenResponseException {
		if(user == null)
			user = this.getUserByGoogleId(googleUserId);
		
		GoogleToken gt = this.getTokenForUserFromCache(user.getId());
		if(gt != null)
			return gt;
		
		String iosString = "client_secret=" + GoogleClientSecretiOS
				+ "&grant_type=refresh_token"
				+ "&refresh_token=" + user.getRefreshToken()
				+ "&client_id=" + GoogleClientIdiOS;
		String webString = "refresh_token=" + user.getRefreshToken() +
				"&client_id=" + GoogleClientIdWeb +
				"&client_secret=" + GoogleClientSecretWeb +
				"&grant_type=refresh_token";
		String androidString = "refresh_token=" + user.getRefreshToken() +
				"&client_id=" + GoogleClientIdAndroid +
				"&client_secret=" + GoogleClientSecretAndroid +
				"&grant_type=refresh_token";

		/*
		 * 
		 * client_id=8819981768.apps.googleusercontent.com&
client_secret={client_secret}&
refresh_token=1/6BMfW9j53gdGImsiyUH5kU5RsR4zwI9lUVX-tqf8JXQ&
grant_type=refresh_token



url: https://accounts.google.com/o/oauth2/auth, params:access_type=offline&client_id=38235450166-dgbh1m7aaab7kopia2upsdj314odp8fc.apps.googleusercontent.com&redirect_uri=urn%3Aietf%3Awg%3Aoauth%3A2.0%3Aoob&response_type=code&scope=https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fplus.me%20https%3A%2F%2Fwww.googleapis.com%2Fauth%2Fuserinfo.email
		 */
		byte[] body = null;
		
		if(user.getDeviceType() == 1)
			body = iosString.getBytes();
		else if(user.getDeviceType() == 2)
			body = webString.getBytes();
		else if(user.getDeviceType() == 3)
			body = androidString.getBytes();
		else
			throw new RuntimeException("the user's device type doesnt conform to any known types, their type: " + user.getDeviceType());
		
		URL url;
		JSONObject json = null;
		try {
			url = new URL(GoogleRefreshTokenEndpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(body.length);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.getOutputStream().write(body);
			body = IOUtils.toByteArray(conn.getInputStream());
			json = new JSONObject(new String(body));
		} catch (MalformedURLException e) {
			throw new RuntimeException("malformedUrlException: " + e.getLocalizedMessage());
		} catch (IOException e) {
			// TODO make this error handling more comprehensive, if refreshtoken is invalid we need to be able to handle it
			JDBCSalesmanBuddyDAO.sendErrorToMe("couldnt exchange refresh token for googleUserId: " + googleUserId + ", error: " + e.getLocalizedMessage());
			String jsonString = "";
			throw new RuntimeException("!IOException: " + e.getLocalizedMessage() + ", deviceType:" + user.getDeviceType() + ", " + new String(body) + ", json: " + jsonString);
		}catch(JSONException jse){
			throw new RuntimeException("JSONException: " + jse.getLocalizedMessage());
		}
		
		GoogleRefreshTokenResponse grtr = new GoogleRefreshTokenResponse(json);
		// put token in database for caching
		this.saveGoogleTokenInCache(grtr, user);
		return this.getTokenForUserFromCache(user.getId()); 
	}

	public UsersName getUsersName(String googleUserId) throws UserNameException {
		GoogleUserInfo gui;
		UsersName name = new UsersName();
		try {
			gui = this.getGoogleUserInfoWithId(googleUserId);
			name.setName(gui.getName());
		} catch (GoogleUserInfoException e) {
			e.printStackTrace();
			name.setName("<Error getting name>");
			return name;

		} catch (GoogleRefreshTokenResponseException e) {
			e.printStackTrace();
			throw new UserNameException(e.getLocalizedMessage());
		}
		return name;
	}
	
	public GoogleUserInfo getGoogleUserInfoWithId(String googleUserId) throws GoogleUserInfoException, GoogleRefreshTokenResponseException{
		GoogleToken gt = this.getValidTokenForUser(googleUserId, null);
		return this.getGoogleUserInfo(gt.getToken());
	}
	
	public GoogleUserInfo getGoogleUserInfo(String token) throws GoogleUserInfoException {
		URL url;
		byte[] body = null;
		JSONObject json = null;
		String whatItHas = "";
		try {
			url = new URL(GoogleUserEndpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			
			conn.setRequestProperty("Authorization", token);
			whatItHas = conn.getRequestProperty("Authorization");
			
			body = IOUtils.toByteArray(conn.getInputStream());
			json = new JSONObject(new String(body));
			
		} catch (ProtocolException pe){
			throw new RuntimeException("protocolExceptions: " + pe.getLocalizedMessage());
		}catch (MalformedURLException e) {
			throw new RuntimeException("malformedUrlException: " + e.getLocalizedMessage());
		} catch (IOException e) {
			throw new RuntimeException("IOException: " + e.getLocalizedMessage() + ", token:" + token + ", auth:" + whatItHas + ", json: " + json + ", e: " + e);
		}catch(JSONException jse){
			throw new RuntimeException("JSONException: " + jse.getLocalizedMessage());
		}
		GoogleUserInfo gui = new GoogleUserInfo(json);
		return gui;
	}
	
	// Email sending stuff
	
	public String sendOnDemandReport(Integer reportType, Integer dealershipId, String replacementEmail) {
		String finalMessage = "Email Sent";
		switch(reportType){
	
		// generates one email object
		case DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
		case DAILY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
		case DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
		case WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
		case WEEKLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
		case WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
		case MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
		case MONTHLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
		case MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
		case DAILY_STOCK_NUMBERS_EMAIL_TYPE:
		case WEEKLY_STOCK_NUMBERS_EMAIL_TYPE:
		case MONTHLY_STOCK_NUMBERS_EMAIL_TYPE:
			String subject = "On-demand report from Salesman Buddy";
			String body = this.generateEmailContentForDealershipIdReportType(dealershipId, reportType);
			SBEmail email = SBEmail.newPlainTextEmail(REPORTS_EMAIL, null, subject, body, true);
			email.replaceTo(replacementEmail);
			
			try {
				email.send();
			} catch (MalformedSBEmailException ex) {
				ex.printStackTrace();
				finalMessage = new StringBuilder().append("Error sending email about dealershipId: ").append(dealershipId).append(", type: ").append(reportType).append(", to:").append(replacementEmail).append(", on demand summary type, error:").append(ex.getLocalizedMessage()).toString();
				JDBCSalesmanBuddyDAO.sendErrorToMe(finalMessage);
			}
			break;
			
//		// generates multiple email objects
		case DAILY_SALESMAN_SUMMARY_EMAIL_TYPE:
		case WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE:
		case MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE:
			List<SBEmail> emails = generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, reportType);
			for(SBEmail e : emails){
				e.replaceTo(replacementEmail);
				try {
					e.send();
				} catch (MalformedSBEmailException ex) {
					ex.printStackTrace();
					finalMessage = new StringBuilder().append("Error sending email about dealershipId: ").append(dealershipId).append(", type: ").append(reportType).append(", to:").append(replacementEmail).append(", individual salesman on demand summary type, error:").append(ex.getLocalizedMessage()).toString();
					JDBCSalesmanBuddyDAO.sendErrorToMe(finalMessage);
				}
			}
			break;
	
			default:
				break;
		}
		return finalMessage;
	}
	
	private List<SBEmail> generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(Integer dealershipId, Integer reportType) {
		DateTime to = new DateTime(DateTimeZone.UTC);
		DateTime from = null;
		switch(reportType){
			case DAILY_SALESMAN_SUMMARY_EMAIL_TYPE:
				from = to.minusDays(1).minusMinutes(10);
				break;
				
			case WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE:
				from = to.minusWeeks(1).minusMinutes(10);
				break;
				
			case MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE:
				from = to.minusMonths(1).minusMinutes(10);
				break;
				
			default:
				throw new RuntimeException("report type invalid for generateIndividualSalesmanSummaryEmailsForDealershipIdReportType");
		}
		
		List<UserTree> userTrees = this.getAllUserTreeForDealershipIdType(dealershipId, reportType);
		List<SBEmail> emails = new ArrayList<>();
		for(UserTree ut : userTrees){
			
			try {
				String subject = "Report about " + this.getUsersName(ut.getUserId()).getName() + " from Salesman Buddy";
				String body = this.individualSalesmanSummaryReport(this.getUserByGoogleId(ut.getUserId()).getId(), from, to);
				SBEmail email = SBEmail.newPlainTextEmail(REPORTS_EMAIL, null, subject, body, true);
				email.replaceTo(this.getEmailForGoogleId(ut.getSupervisorId()));
				emails.add(email);
			} catch (UserNameException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error in generateIndividualSalesmanSummaryEmailsForDealershipIdReportType for getting user's name for userTree: ").append(ut.toString()).append(", error: ").append(e.getLocalizedMessage()).toString());
			}
		}
		return emails;
	}
	
	public void runReportsForType(Integer type){
		if(type == DAILY_TYPE){
			this.runDailyReports();
		}else if(type == WEEKLY_TYPE){
			this.runWeeklyReports();
		}else if(type == MONTHLY_TYPE){
			this.runMonthlyReports();
		}else
			throw new RuntimeException("run reports type not found: " + type);
	}
	
	private void runDailyReports() {
		List<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		List<String> toEmails = new ArrayList<>();
		Integer type = 0;
		Integer dealershipId = 0;
		
		for(Dealerships d : dealerships){
			dealershipId = d.getId();
			
			type = DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE;
			subject = "Daily Dealership Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Daily dealership summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = DAILY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE;
			subject = "Daily All Salesman Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Daily all salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE;
			subject = "Daily Test Drive Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Daily test drive summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = DAILY_SALESMAN_SUMMARY_EMAIL_TYPE;
			List<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Daily individual salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}

	private void runWeeklyReports() {
		List<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		List<String> toEmails = new ArrayList<>();
		Integer type = 0;
		Integer dealershipId = 0;
		
		for(Dealerships d : dealerships){
			dealershipId = d.getId();
			
			type = WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE;
			subject = "Weekly Dealership Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Weekly dealership summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = WEEKLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE;
			subject = "Weekly All Salesman Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Weekly all salesmen summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE;
			subject = "Weekly Test Drive Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Weekly test drive summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE;
			List<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Weekly individual salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}

	private void runMonthlyReports() {
		List<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		List<String> toEmails = new ArrayList<>();
		Integer type = 0;
		Integer dealershipId = 0;
		
		for(Dealerships d : dealerships){
			dealershipId = d.getId();
			
			type = MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE;
			subject = "Monthly Dealership Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Monthly dealership summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = MONTHLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE;
			subject = "Monthly All Salesman Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", monthly all salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE;
			subject = "Monthly Test Drive Summary from Salesman Buddy";
			body = this.generateEmailContentForDealershipIdReportType(dealershipId, type);
			toEmails = this.getEmailsForUserFromUserTrees(this.getAllUserTreeForDealershipIdType(dealershipId, type));
			try {
				SBEmail.newPlainTextEmail(REPORTS_EMAIL, toEmails, subject, body, true).send();
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", monthly test drive summary type, error:").append(e.getLocalizedMessage()).toString());
			}
			
			type = MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE;
			List<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", montly individual salesmen summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}
	
	private List<String> getEmailsForUserFromUserTrees(List<UserTree> userTrees){
		List<String> ids;
		try {
			ids = this.getUserTreeGoogleIdsForType(userTrees, USER_TREE_TYPE);
		} catch (InvalidUserTreeType e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return this.getEmailsForGoogleIds(ids);
	}
	
	private List<String> getEmailsForSupervisorFromUserTrees(List<UserTree> userTrees){
		List<String> ids;
		try {
			ids = this.getUserTreeGoogleIdsForType(userTrees, SUPERVISOR_TREE_TYPE);
		} catch (InvalidUserTreeType e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return this.getEmailsForGoogleIds(ids);
	}

	private String individualSalesmanSummaryReport(Integer userId, DateTime from, DateTime to){
		Users user = this.getUserById(userId);
		List<Licenses> licenses = this.getLicensesForDateRangeUserId(userId, to, from);
		StringBuilder sb = new StringBuilder();
		try {
			sb.append(this.getUsersName(user.getGoogleUserId()).getName());
		} catch (UserNameException e) {
			e.printStackTrace();
			sb.append("<Uknown salesman name>");
		}

		sb.append(" went on ").append(licenses.size()).append(" test drives.");
		if(licenses.size() > 0){
			sb.append("They are:\n");
			for(Licenses l : licenses){
				String stockNumber = this.getStockNumberForLicenseId(l.getId());
				sb.append("Stock Number: ").append(stockNumber);
				
				try {
					StockNumbers sn = this.getStockNumberByStockNumber(stockNumber);
					if(sn.getStatus() == STOCK_NUMBER_SOLD)
						sb.append("SOLD!");
					
				} catch (NoSqlResultsException e) {
					// Fail silently
				}
				
				sb.append(", Customer: ").append(this.getCustomerNameForLicenseId(l.getId()));
				sb.append(", When: ").append(this.printTimeDateForReports(this.getWhenForLicenseCreated(l.getCreated())));
				sb.append("\n");
			}
		}
		
		// TODO put number of sold here
		
		return sb.toString();
	}
	
	private DateTime getWhenForLicenseCreated(Date created) {
		// TODO this needs to get checked for time zone problems
		return new DateTime(created);
	}

	private String allSalesmanSummaryReport(Integer dealershipId, DateTime from, DateTime to){
		List<Users> salesmen = this.getAllUsersForDealershipId(dealershipId);
		StringBuilder sb = new StringBuilder();
		for(Users s : salesmen){
			sb.append(this.individualSalesmanSummaryReport(s.getId(), from, to));
			sb.append("\n\n");
		}
		String finalMessage = this.wrapReportContentWithBeginningEnd(sb.toString(), ReportBeginEnd.AllSalesmen, ReportBeginEnd.AllSalesmen, dealershipId, from, to);
		return finalMessage;
	}

	private String dealershipSummaryReport(Integer dealershipId, DateTime from, DateTime to){
		// TODO
		List<Licenses> licenses = this.getLicensesForDateRangeDealershipId(from, to, dealershipId);
		List<Users> users = this.getAllUsersForDealershipId(dealershipId);
		Dealerships d = this.getDealershipById(dealershipId);
		StringBuilder sb = new StringBuilder();
		sb.append(d.getName()).append(" had ").append(licenses.size()).append(" test drives by ").append(users.size());
		sb.append(" salesmen during this time period. The dealership also sold ");
		List<StockNumbers> stockNumbers = this.getStockNumbersForDealershipFromTo(dealershipId, from, to);
		sb.append(stockNumbers.size());
		if(stockNumbers.size() == 1)
			sb.append(" vehicle.");
		else
			sb.append(" vehicles.");
		String finalMessage = this.wrapReportContentWithBeginningEnd(sb.toString(), ReportBeginEnd.DealershipSummary, ReportBeginEnd.DealershipSummary, dealershipId, from, to);
		return finalMessage;
	}
	
	private String warningsReport(Integer dealershipId){
		// TODO for reporting that scans arent getting stock numbers inputted or insurance questions answered
		return null;
	}
	
	private String testDriveSummaryReport(Integer dealershipId, DateTime from, DateTime to){
		List<Licenses> licenses = this.getLicensesForDateRangeDealershipId(from, to, dealershipId);
		String licensesMessage = this.createLicensesSummaryForLicenses(licenses, from, to, dealershipId, DEALERSHIP_TYPE);
		String finalMessage = this.wrapReportContentWithBeginningEnd(licensesMessage, ReportBeginEnd.TestDriveSummary, ReportBeginEnd.TestDriveSummary, dealershipId, from, to);
		return finalMessage;
	}
	
	private String createLicensesSummaryForLicenses(List<Licenses> licenses, DateTime from, DateTime to, Integer dealershipId, Integer dealershipType) {
		// TODO
		StringBuilder sb = new StringBuilder();
		if(licenses.size() > 0){
			sb.append("Test drives taken during this period:\n");
			for(Licenses l : licenses){
				sb.append("\t");
				DateTime when = this.getWhenForLicenseCreated(l.getCreated());
				sb.append(this.printTimeDateForReports(when)).append(", Stock Number: ");
				sb.append(this.getStockNumberForLicenseId(l.getId())).append(", Salesman: ");
				// TODO mark as stock number sold here
				
				try {
					sb.append(this.getUsersName(this.getUserById(l.getUserId()).getGoogleUserId()).getName());
				} catch (UserNameException e) {
					e.printStackTrace();
					sb.append("<Unknown>");
				}
				
				sb.append(", Customer: ");
				sb.append(this.getCustomerNameForLicenseId(l.getId()));
				// TODO mark as customer has been sold to here
				sb.append("\n");
			}
		}else{
			sb.append("Your dealership had no test drives recorded during this time period.");
		}
		return sb.toString();
	}
	
	private String getCustomerNameForLicenseId(Integer id) {
		final String sql = "SELECT q.tag tag, a.answerText answerText FROM answers a, questions q WHERE a.licenseId = ? AND (q.tag = ? OR q.tag = ?) AND a.questionId = q.id";
		String firstName = "<Unknown>";
		String lastName = "<Unknown>";
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			statement.setInt(2, QUESTION_FIRST_NAME_TAG);
			statement.setInt(3, QUESTION_LAST_NAME_TAG);
			
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				if(resultSet.getInt("tag") == QUESTION_FIRST_NAME_TAG)
					firstName = resultSet.getString("answerText");
				else if(resultSet.getInt("tag") == QUESTION_LAST_NAME_TAG)
					lastName = resultSet.getString("answerText");
			}
			resultSet.close();

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		
		if(firstName.equals("<Unknown>") && lastName.equals("<Unknown>"))
			return "<Unknown>";// so it isnt <Unknown> <Unknown> on the report, just one <Unknown>
		return new StringBuilder().append(firstName).append(" ").append(lastName).toString();
	}

	private String getStockNumberForLicenseId(Integer licenseId) {
		final String sql = "SELECT a.answerText answerText FROM answers a, questions q WHERE a.licenseId = ? AND a.questionId = q.id AND q.tag = ?";
		String stockNumber = "<Unknown>";
		
		try {
			stockNumber = this.getRowOneColumn(sql, String.class, "answerText", licenseId, QUESTION_STOCK_NUMBER);
		} catch (NoSqlResultsException e) {
			// Fail silently
		}
		
		return stockNumber;
		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//			statement.setInt(2, QUESTION_STOCK_NUMBER);
//			
//			ResultSet resultSet = statement.executeQuery();
//			while(resultSet.next()){
//				stockNumber = resultSet.getString("answerText");
//				break;
//			}
//			resultSet.close();
//
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return stockNumber;
	}

	private String stockNumberSummaryReport(Integer dealershipId, DateTime from, DateTime to){
		List<String> stockNumbers = this.getUniqueStockNumbersForDealershipId(dealershipId);
		StringBuilder sb = new StringBuilder();
		for(String sn : stockNumbers){
			sb.append(this.stockNumberReportForThisStockNumber(sn, from, to));
			sb.append("\n");
		}
		String finalMessage = this.wrapReportContentWithBeginningEnd(sb.toString(), ReportBeginEnd.StockNumberSummary, ReportBeginEnd.StockNumberSummary, dealershipId, from, to);
		return finalMessage;
	}
	
	private String stockNumberReportForThisStockNumber(String stockNumber, DateTime from, DateTime to) {
		List<Licenses> licenses = this.getLicensesWithStockNumberFromTo(stockNumber, from, to);
		StockNumbers sn = null;
		try {
			sn = this.getStockNumberByStockNumber(stockNumber);
		} catch (NoSqlResultsException e) {
			// Do nothing
		}
		StringBuilder sb = new StringBuilder();
		sb.append("Stock Number '").append(stockNumber);
		if(sn != null){
			sb.append(" has a status of ");
			if(sn.getStatus() == STOCK_NUMBER_SOLD)
				sb.append("SOLD!");
			else if(sn.getStatus() == STOCK_NUMBER_NORMAL)
				sb.append("not sold");
			sb.append(" and");
		}
		
		sb.append("' has been on ").append(licenses.size());
		if(licenses.size() == 1)
			sb.append(" test drive, ");
		else if(licenses.size() > 1)
			sb.append(" test drives, ");
		else{
			sb.append(" test drives. The last time was ");
			List<Licenses> allLicenses = this.getLicensesWithStockNumber(stockNumber);
			if(allLicenses.size() > 0){
				Date date = allLicenses.get(allLicenses.size() - 1).getCreated();
				String lastTime = this.printTimeDateForReports(new DateTime(date));
				sb.append("on ").append(lastTime).append(".");
			}else
				sb.append("never.");
			return sb.toString();
		}
		
		if(licenses.size() > 0){
			sb.append("taken out by:\n");
			for(Licenses l : licenses){
				Users user = this.getUserById(l.getUserId());
				try {
					sb.append(this.getUsersName(user.getGoogleUserId()).getName());
				} catch (UserNameException e) {
					e.printStackTrace();
					sb.append("<Unknown salesman name>");
				}
				sb.append(" with ").append(this.getCustomerNameForLicenseId(l.getId())).append(" on ");
				sb.append(this.printTimeDateForReports(this.getWhenForLicenseCreated(l.getCreated()))).append(".\n");
			}
		}
		
		return sb.toString();
	}

	private List<Licenses> getLicensesWithStockNumberFromTo(String stockNumber, DateTime from, DateTime to) {
		final String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId AND l.created between ? and ? ORDER BY l.created";
		return this.getList(sql, Licenses.class, stockNumber, QUESTION_STOCK_NUMBER, from.toString(), to.toString());
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, stockNumber);
//			statement.setInt(2, QUESTION_STOCK_NUMBER);
//			statement.setString(3, from.toString());
//			statement.setString(4, to.toString());
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	private List<Licenses> getLicensesWithStockNumber(String stockNumber) {
		final String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId ORDER BY l.created";
		return this.getList(sql, Licenses.class, stockNumber, QUESTION_STOCK_NUMBER);
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, stockNumber);
//			statement.setInt(2, QUESTION_STOCK_NUMBER);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	private String wrapReportContentWithBeginningEnd(String content, ReportBeginEnd beginning, ReportBeginEnd ending, Integer dealershipId, DateTime from, DateTime to){
		
		boolean useDefaultEnding = false;
		String timePeriod = "";
		if(to.isAfter(from.plusMonths(1)))
			timePeriod = "Monthly ";
		else if(to.isAfter(from.plusWeeks(1)))
			timePeriod = "Weekly ";
		else if(to.isAfter(from.plusDays(1)))
			timePeriod = "Daily ";
		
		StringBuilder sb = new StringBuilder();
		Dealerships dealership = this.getDealershipById(dealershipId);
		String fromTo = new StringBuilder().append(this.printTimeDateForReports(from)).append(" to ").append(this.printTimeDateForReports(to)).toString();
		switch(beginning){
		case AllSalesmen:
			sb.append("All Salesmen ").append(timePeriod).append("summary report for ").append(dealership.getName()).append(" from ").append(fromTo).append(".\n\n");
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		case DealershipSummary:
			sb.append("Dealership-Wide ").append(timePeriod).append("summary report for ").append(dealership.getName()).append(" from ").append(fromTo).append(".\n\n");
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		case StockNumberSummary:
			sb.append("Stock Number ").append(timePeriod).append("summary report for ").append(dealership.getName()).append(" from ").append(fromTo).append(".\n\n");
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		case TestDriveNow:
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		case TestDriveSummary:
			sb.append("Test Drive ").append(timePeriod).append("summary report for ").append(dealership.getName()).append(" from ").append(fromTo).append(".\n\n");
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		case Warnings:
			sb.append("Warnings ").append(timePeriod).append("report for ").append(dealership.getName()).append(" from ").append(fromTo).append(".\n\n");
			sb.append(content);
			useDefaultEnding = true;
			break;
			
		default:
			break;
		
		}
		
		if(useDefaultEnding)
			sb.append("\n\nThank you for using Salesman Buddy. If you have any questions, contact us at ").append(SUPPORT_EMAIL);
		
		return sb.toString();
	}

	private String printTimeDateForReports(DateTime time) {
		DateTimeFormatter fmt = DateTimeFormat.forPattern("EEEE MMMM d, yyyy 'at' K a");
		return time.toString(fmt);
	}

	private String generateEmailContentForDealershipIdReportType(Integer dealershipId, Integer type) {
		DateTime now = new DateTime(DateTimeZone.forID("America/Denver"));
		final Integer BACK_MINUTES = 10;
		DateTime dayPrevious = now.minusDays(1).minusMinutes(BACK_MINUTES);
		DateTime weekPrevious = now.minusWeeks(1).minusMinutes(BACK_MINUTES);
		DateTime monthPrevious = now.minusMonths(1).minusMinutes(BACK_MINUTES);
		switch(type){
		
			case DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
				return this.testDriveSummaryReport(dealershipId, dayPrevious, now);
	
			case DAILY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
				return this.allSalesmanSummaryReport(dealershipId, dayPrevious, now);
	
			case DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
				return this.dealershipSummaryReport(dealershipId, dayPrevious, now);
				
			case DAILY_STOCK_NUMBERS_EMAIL_TYPE:
				return this.stockNumberSummaryReport(dealershipId, dayPrevious, now);
	
			case WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
				return this.testDriveSummaryReport(dealershipId, weekPrevious, now);
	
			case WEEKLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
				return this.allSalesmanSummaryReport(dealershipId, weekPrevious, now);
	
			case WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
				return this.dealershipSummaryReport(dealershipId, weekPrevious, now);
				
			case WEEKLY_STOCK_NUMBERS_EMAIL_TYPE:
				return this.stockNumberSummaryReport(dealershipId, weekPrevious, now);
	
			case MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE:
				return this.testDriveSummaryReport(dealershipId, monthPrevious, now);
	
			case MONTHLY_ALL_SALESMAN_SUMMARY_EMAIL_TYPE:
				return this.allSalesmanSummaryReport(dealershipId, monthPrevious, now);
	
			case MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE:
				return this.dealershipSummaryReport(dealershipId, monthPrevious, now);
				
			case MONTHLY_STOCK_NUMBERS_EMAIL_TYPE:
				return this.stockNumberSummaryReport(dealershipId, monthPrevious, now);
	
	//		case DAILY_SALESMAN_SUMMARY_EMAIL_TYPE:
	//			
	//			break;
	//
	//		case WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE:
	//			
	//			break;
	//
	//		case MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE:
	//			
	//			break;
				
			default:
				return "Error, couldn't find correct report type";
		}
	}
	
	private void sendEmailsAboutTestDriveForGoogleUserIdLicenseId(String googleUserId, Integer licenseId){
		List<UserTree> userTrees = this.getAllUserTreesForGoogleUserIdType(googleUserId, ON_TEST_DRIVE_EMAIL_TYPE);
		List<String> supervisorEmails = this.getEmailsForSupervisorFromUserTrees(userTrees);
		String subject = "Test drive subject for licenseId: " + licenseId;
		String message = this.createNowTestDriveMessageForLicenseId(licenseId);
		SBEmail email = SBEmail.newPlainTextEmail(TEST_DRIVE_NOW_EMAIL, supervisorEmails, subject, message, true);
		try {
			email.send();
		} catch (MalformedSBEmailException e) {
			e.printStackTrace();
			JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about googleUserId: ").append(googleUserId).append(", licenseId: ").append(licenseId).append(", Daily dealership summary type, error:").append(e.getLocalizedMessage()).toString());
		}
	}
	
	private String createNowTestDriveMessageForLicenseId(Integer licenseId) {
		StringBuilder sb = new StringBuilder();
		LicensesListElement lle = this.getLicenseListElementForLicenseId(licenseId);
		Licenses license = this.getLicenseForLicenseId(licenseId);
		Users user = this.getUserById(license.getUserId());
		String usersName = "<Error getting user's name>";
		try {
			usersName = this.getUsersName(user.getGoogleUserId()).getName();
		} catch (UserNameException e) {
			e.printStackTrace();
			JDBCSalesmanBuddyDAO.sendErrorToMe("Error getting user's name: " + user.toString());
		}
		String stockNumber = LicensesListElement.getStockNumberForLicensesListElement(lle);
		
		sb.append("A test drive just occurred with ");
		sb.append(usersName);
		sb.append(" on vehicle ").append(stockNumber).append(".\n");
		sb.append(this.getStatsAboutStockNumber(stockNumber, user.getDealershipId()));
		sb.append(this.getStatsAboutUserId(user.getId())).append("\n");
		String finalMessage = this.wrapReportContentWithBeginningEnd(sb.toString(), ReportBeginEnd.TestDriveNow, ReportBeginEnd.TestDriveNow, user.getDealershipId(), new DateTime(), new DateTime());
		return finalMessage;
	}

	private String getStatsAboutUserId(Integer userId) {
		DateTime to = this.getNowTime();
		DateTime from = to.minusWeeks(1);
		List<Licenses> licenses = this.getLicensesForDateRangeUserId(userId, to, from);
		StringBuilder sb = new StringBuilder();
		sb.append("This salesman has had ").append(licenses.size()).append(" test drives in the last week.\n");
		return sb.toString();
	}

	private List<Licenses> getLicensesForDateRangeUserId(Integer userId, DateTime to, DateTime from) {
		final String sql = "SELECT * FROM licenses WHERE userId = ? AND created BETWEEN ? AND ?";
		return this.getList(sql, Licenses.class, userId, from.toString(), to.toString());
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, userId);
//			statement.setString(2, from.toString());
//			statement.setString(3, to.toString());
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	private String getStatsAboutStockNumber(String stockNumber, Integer dealershipId) {
		DateTime to = this.getNowTime();
		DateTime from = to.minusWeeks(1);
		List<Licenses> licenses = this.getAllLicensesForStockNumberInDateRange(stockNumber, to, from);
		StringBuilder sb = new StringBuilder();
		sb.append("Stock Number ").append(stockNumber).append(" has been test driven ").append(licenses.size());
		sb.append(" times in the last week.\n");
		return sb.toString();
	}

	private List<Licenses> getLicensesForDateRangeDealershipId(DateTime from, DateTime to, Integer dealershipId) {
		final String sql = "SELECT * FROM licenses WHERE userId IN (SELECT id FROM users WHERE dealershipId = ?) AND created BETWEEN ? AND ?";
		return this.getList(sql, Licenses.class, dealershipId, from.toString(), to.toString());
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setString(2, from.toString());
//			statement.setString(3, to.toString());
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	private List<Licenses> getAllLicensesForStockNumberInDateRange(String stockNumber, DateTime to, DateTime from) {
		final String sql = "SELECT l.* FROM licenses l, answers a, questions q WHERE q.tag = ? AND a.questionId = q.id AND a.answerText = ? AND a.licenseId = l.id";
//		final String sql = "SELECT distinct a.answerText as stockNumber FROM answers a, licenses l, users u, questions q WHERE q.tag = ? AND len(a.answerText) > 0 AND a.questionId = q.id AND a.licenseId = l.id AND l.userId = u.id AND u.dealershipId = ?;";
		return this.getList(sql, Licenses.class, QUESTION_STOCK_NUMBER, stockNumber);
//		List<Licenses> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, QUESTION_STOCK_NUMBER);
//			statement.setString(2, stockNumber);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//			resultSet.close();
//
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	private List<String> getUserTreeGoogleIdsForType(List<UserTree> userTrees, Integer type) throws InvalidUserTreeType {
		List<String> ids = new ArrayList<>();
		for(UserTree u : userTrees){
			if(type == JDBCSalesmanBuddyDAO.SUPERVISOR_TREE_TYPE) 
				ids.add(u.getSupervisorId());
			else if(type == JDBCSalesmanBuddyDAO.USER_TREE_TYPE)
				ids.add(u.getUserId());
			else
				throw new InvalidUserTreeType("Type " + type + " unknown");
		}
		return ids;
	}

	// User Tree stuff
	
	public int newUserTreeNode(String googleUserId, String supervisorId, Integer type){
		final String sql = "INSERT INTO userTree (userId, supervisorId, type) VALUES(?, ?, ?)";
		try {
			return this.insertRow(sql, "id", googleUserId, supervisorId, type);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert userTree failed, sql: " + sql + ", googleUserId: " + googleUserId + ", supervisorId: " + supervisorId + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		if(supervisorId == null)// allows for dealership-wide reports
//			supervisorId = "";
//		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setString(1, googleUserId);
//			statement.setString(2, supervisorId);
//			statement.setInt(3, type);
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(i == 0)
//			throw new RuntimeException("insert userTree failed, i == 0, " + googleUserId + ", supervisorId: " + supervisorId);
//		return i;
	}
	
	private List<UserTree> getAllUserTreeForDealershipIdType(Integer dealershipId, Integer type) {
//		StringBuilder sb = new StringBuilder();
//		sb.append("SELECT * FROM userTree ut WHERE type = ").append(type).append(" AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ").append(dealershipId);
//		sb.append(" AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ").append(dealershipId);
//		sb.append(") OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ").append(dealershipId).append("))");
//		return this.getList(sb.toString(), UserTree.class);
		String sql = "SELECT * FROM userTree ut WHERE type = ? AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ? AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?))";
		return this.getList(sql, UserTree.class, type, dealershipId, dealershipId);
		//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, type);
//			statement.setInt(2, dealershipId);
//			statement.setInt(3, dealershipId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public UserTree getUserTreeById(Integer userTreeId){
		try {
//			return this.getRow("userTree", "id", userTreeId, UserTree.class);
			return this.getRow("SELECT * FROM userTree WHERE id = ?", UserTree.class, userTreeId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant find usertree by id: " + userTreeId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM userTree WHERE id = ?";
//		UserTree result = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//			
//			ResultSet resultSet = statement.executeQuery();
//			result = UserTree.parseOneResultFromSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return result;
	}
	
	public List<UserTree> getAllUserTreeForGoogleUserId(String googleUserId){
		return this.getList("SELECT * FROM userTree WHERE userId = ?", UserTree.class, googleUserId);
//		return this.getList("userTree", "userId", googleUserId, null, null, UserTree.class);
//		final String sql = "SELECT * FROM userTree WHERE userId = ?";
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	public List<UserTree> getAllUserTreesForGoogleUserIdType(String googleUserId, Integer type){
//		final String sql = ;
		return this.getList("SELECT * FROM userTree WHERE userId = ? AND type = ?", UserTree.class, googleUserId, type);
//		return this.getList(sql, UserTree.class);
//		return this.getList(", columnName, columnValue, orderColumn, orderDirection, c)
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			statement.setInt(2, type);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public List<UserTree> getAllUserTreeForGoogleSupervisorId(String googleSupervisorId){
		return this.getList("SELECT * FROM userTree WHERE supervisorId = ?", UserTree.class, googleSupervisorId);
//		final String sql = "SELECT * FROM userTree WHERE supervisorId = ?";
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleSupervisorId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public List<UserTree> getAllUserTree() {
		return this.getList("SELECT * FROM userTree ORDER BY userId", UserTree.class);
//		final String sql = "SELECT * FROM userTree ORDER BY userId";
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public List<UserTree> getAllUserTreeForGoogleSupervisorIdAndGoogleUserId(String googleUserId) {
		return this.getList("SELECT * FROM userTree WHERE supervisorId = ? OR userId = ?", UserTree.class, googleUserId);
//		final String sql = "SELECT * FROM userTree WHERE supervisorId = ? OR userId = ?";
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			statement.setString(2, googleUserId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	public List<UserTree> getAllUserTreeForDealershipId(Integer dealershipId) {
		final String sql = "SELECT * FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?);";
		return this.getList(sql, UserTree.class, dealershipId, dealershipId);
//		List<UserTree> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setInt(2, dealershipId);
//			
//			ResultSet resultSet = statement.executeQuery();
//			results = UserTree.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	private List<String> getUniqueStockNumbersForDealershipId(Integer dealershipId) {
		final String sql = "SELECT distinct a.answerText as stockNumber FROM answers a, licenses l, users u, questions q WHERE q.tag = ? AND len(a.answerText) > 0 AND a.questionId = q.id AND a.licenseId = l.id AND l.userId = u.id AND u.dealershipId = ?;";
		return this.getListOneColumn(sql, String.class, "stockNumber", QUESTION_STOCK_NUMBER, dealershipId);
//		List<String> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, QUESTION_STOCK_NUMBER);
//			statement.setInt(2, dealershipId);
//
//			ResultSet resultSet = statement.executeQuery();
//			while(resultSet.next()){
//				results.add(resultSet.getString("stockNumber"));
//			}
//			resultSet.close();
//
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	


	private List<Users> getAllUsersForDealershipId(Integer dealershipId) {
		return this.getList("SELECT * FROM users WHERE dealershipId = ?", Users.class, dealershipId);
//		final String sql = "SELECT * FROM users WHERE dealershipId = ?";
//		List<Users> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//
//			ResultSet resultSet = statement.executeQuery();
//			results = Users.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	private String getEmailForGoogleId(String supervisorId) {
		List<String> ids = new ArrayList<>();
		ids.add(supervisorId);
		List<String> emails = this.getEmailsForGoogleIds(ids);
		if(emails.size() > 0)
			return emails.get(0);
		return ERRORED_EMAIL;
	}
	
	public List<String> getEmailsForGoogleIds(List<String> googleIds){
		Integer unverifiedEmails = 0;
		Set<String> recipients = new HashSet<>();
		for(String id : googleIds){
			GoogleUserInfo gui;
			try {
				gui = this.getGoogleUserInfoWithId(id);
				if(gui.isVerifiedEmail()){
					recipients.add(gui.getEmail());
				}else{
					unverifiedEmails++;
				}
			} catch (GoogleUserInfoException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe("Error getting gui for getEmailsForGoogleIds, id: " + id + ", error" + e.getLocalizedMessage());
			} catch (GoogleRefreshTokenResponseException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe("Error getting refresh token response for getEmailsForGoogleIds, id: " + id + ", error" + e.getLocalizedMessage());
			}
		}
		if(unverifiedEmails != 0)
			JDBCSalesmanBuddyDAO.sendErrorToMe("found " + unverifiedEmails + " unverified emails");
		return new ArrayList<String>(recipients);
	}
	
	public int updateUserTreeNode(String googleUserId, String googleSupervisorId, Integer id, Integer type){
		final String sql = "UPDATE userTree SET userId = ?, supervisorId = ?, type = ? WHERE id = ?";
		int i = this.updateRow(sql, googleUserId, googleSupervisorId, type, id);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			statement.setString(2, googleSupervisorId);
//			statement.setInt(3, type);
//			statement.setInt(4, id);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to update userTree, id: " + id);
		return i;
	}
	
	public int deleteUserTreeNodeById(Integer id){
		final String sql = "DELETE FROM userTree WHERE id = ?";
		int i = this.updateRow(sql, id);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to delete userTree, id: " + id);
		return i;
	}
	
	public int deleteUserTreeNodesForGoogleUserIdAllNodes(String googleUserId){
		final String sql = "DELETE FROM userTree WHERE supervisorId = ? OR userId = ?";
		int i = this.updateRow(sql, googleUserId, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			statement.setString(2, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreeNodesForUserId(String googleUserId){
		final String sql = "DELETE FROM userTree WHERE userId = ?";
		int i = this.updateRow(sql, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to delete user's userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreeNodesForSupervisorId(String googleUserId){
		final String sql = "DELETE FROM userTree WHERE supervisorId = ?";
		int i = this.updateRow(sql, googleUserId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, googleUserId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to delete supervisor's userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public ErrorMessage deleteUserTreeNodesForDealershipId(Integer dealershipId) {
		final String sql = "DELETE FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?)";
		int i = this.updateRow(sql, dealershipId, dealershipId);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setInt(2, dealershipId);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree nodes for dealerhsipId: " + dealershipId);
		return new ErrorMessage("Not an error, successfully deleted " + i + " userTree nodes for dealerhsipId: " + dealershipId);
	}

	public ErrorMessage deleteAllUserTreeNodes() {
		final String sql = "DELETE FROM userTree";
		int i = this.updateRow(sql);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		if(i == 0){
			i = this.getCount("SELECT count(*) count FROM userTree");
			if(i != 0)
				throw new RuntimeException("Unable to delete all userTree, found: " + i);
		}
		return new ErrorMessage("this isnt an error, successfully deleted all userTree nodes");
	}
	
	private StockNumbers getStockNumberByStockNumber(String stockNumber) throws NoSqlResultsException {
		return this.getRow("SELECT * FROM stockNumbers WHERE stockNumber = ?", StockNumbers.class, stockNumber);
//		final String sql = "SELECT * FROM stockNumbers WHERE stockNumber = ?";
//		StockNumbers result = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, stockNumber);
//
//			ResultSet resultSet = statement.executeQuery();
//			result = StockNumbers.parseOneRowResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return result;
	}
	
	public StockNumbers getStockNumberById(Integer stockNumberId) {
		try {
			return this.getRow("SELECT * FROM stockNumbers WHERE id = ?", StockNumbers.class, stockNumberId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant find stock number by id: " + stockNumberId + ", error: " + e.getLocalizedMessage());
		}
//		final String sql = "SELECT * FROM stockNumbers WHERE id = ?";
//		StockNumbers result = null;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//
//			ResultSet resultSet = statement.executeQuery();
//			result = StockNumbers.parseOneRowResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return result;
	}

	public List<StockNumbers> getAllStockNumbers() {
		return this.getList("SELECT * FROM stockNumbers", StockNumbers.class);
//		final String sql = "SELECT * FROM stockNumbers";
//		List<StockNumbers> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			ResultSet resultSet = statement.executeQuery();
//			results = StockNumbers.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	public List<StockNumbers> getStockNumbersForDealershipId(Integer dealershipId) {
		return this.getList("SELECT * FROM stockNumbers WHERE dealershipId = ?", StockNumbers.class, dealershipId);
//		final String sql = "SELECT * FROM stockNumbers WHERE dealershipId = ?";
//		List<StockNumbers> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//
//			ResultSet resultSet = statement.executeQuery();
//			results = StockNumbers.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}
	
	private List<StockNumbers> getStockNumbersForDealershipFromTo(Integer dealershipId, DateTime from, DateTime to) {
		final String sql = "SELECT * FROM stockNumbers WHERE dealershipId = ? AND soldOn between ? and ?";
		return this.getList(sql, StockNumbers.class, dealershipId, from.toString(), to.toString());
//		List<StockNumbers> results = new ArrayList<>();
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, dealershipId);
//			statement.setString(2, from.toString());
//			statement.setString(3, to.toString());
//
//			ResultSet resultSet = statement.executeQuery();
//			results = StockNumbers.parseResultSet(resultSet);
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return results;
	}

	public StockNumbers newStockNumber(StockNumbers stockNumber) {
		final String sql = "INSERT INTO stockNumbers (dealershipId, stockNumber, status, createdBy, soldBy) VALUES(?, ?, ?, ?, ?)";
		try {
			int i = this.insertRow(sql, "id", stockNumber.getDealershipId(), stockNumber.getStockNumber(), stockNumber.getStatus(), stockNumber.getCreatedBy(), stockNumber.getSoldBy());
			return this.getStockNumberById(i);// we know this will work because of the above test
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert stockNumbers failed, sql: " + sql + ", stockNumber: " + stockNumber.toString() + ", error: " + e.getLocalizedMessage());
		}
//		int i = 0;
//		
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
//			statement.setInt(1, stockNumber.getDealershipId());
//			statement.setString(2, stockNumber.getStockNumber());
//			statement.setInt(3, stockNumber.getStatus());
//			statement.setInt(4, stockNumber.getCreatedBy());
//			statement.setInt(5, stockNumber.getSoldBy());
//			statement.execute();
//			
//			ResultSet resultSet = statement.getGeneratedKeys();
//			i = this.parseFirstInt(resultSet, "id");
//			resultSet.close();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		if(i == 0)
//			throw new RuntimeException("insert stockNumbers failed, i == 0, stockNumber: " + stockNumber.toString());
//		
//		try {
//			return this.getStockNumberById(i);// we know this will work because of the above test
//		} catch (NoResultInResultSet e) {
//			e.printStackTrace();
//		}
//		return null;
	}


	public Integer deleteStockNumberById(Integer id) {
		final String sql = "DELETE FROM stockNumbers WHERE id = ?";
		return this.updateRow(sql, id);
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, id);
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
//		return i;
	}

	public StockNumbers updateStockNumber(StockNumbers stockNumber) {
		final String sql = "UPDATE stockNumbers SET dealershipId = ?, stockNumber = ?, status = ?, soldOn = ?, soldBy = ? WHERE id = ?";
		int i = this.updateRow(sql, stockNumber.getDealershipId(), stockNumber.getStockNumber(), stockNumber.getStatus(), stockNumber.getSoldOn(), stockNumber.getSoldBy(), stockNumber.getId());
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setInt(1, stockNumber.getDealershipId());
//			statement.setString(2, stockNumber.getStockNumber());
//			statement.setInt(3, stockNumber.getStatus());
//			statement.setDate(4, stockNumber.getSoldOn());
//			statement.setInt(5, stockNumber.getSoldBy());
//			statement.setInt(6, stockNumber.getId());
//			
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException("StockNumbers: " + stockNumber.toString() + ", error: " + sqle.getLocalizedMessage());
//		}
		if(i == 0)
			throw new RuntimeException("update stockNumber failed for stockNumbers: " + stockNumber.toString());
		
		return stockNumber;
	}
	
//	public StockNumbers updateStockNumberSoldOn(Integer id, DateTime at) {
//		// TODO make sure this works properly
//		final String sql = "UPDATE stockNumbers SET soldOn = ? WHERE id = ?";
//		int i = 0;
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			statement.setString(1, at.toString());
//			statement.setInt(2, id);
//			i = statement.executeUpdate();
//			
//		}catch(SQLException sqle){
//			throw new RuntimeException("StockNumberId: " + id + ", dateTime: " + at.toString() + ", error: " + sqle.getLocalizedMessage());
//		}
//		if(i == 0)
//			throw new RuntimeException("update stockNumber failed for stockNumberId: " + id + ", dateTime: " + at.toString());
//		
//		try {
//			return this.getStockNumberById(id);
//		} catch (NoResultInResultSet e) {
//			// fail silently
//		}
//		return null;// will never happen because update was successful
//	}

	public boolean userHasRightsToStockNumberId(Integer stockNumberId, String googleUserId) {
		Users user = this.getUserByGoogleId(googleUserId);
		if(user.getType() > 2)
			return true;
		
		StockNumbers stockNumber = this.getStockNumberById(stockNumberId);
		
		if(user.getDealershipId() == stockNumber.getDealershipId())
			return true;
		
		return false;
	}
}

















































