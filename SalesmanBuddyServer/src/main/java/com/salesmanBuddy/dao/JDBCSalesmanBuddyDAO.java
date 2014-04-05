package com.salesmanBuddy.dao;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.salesmanBuddy.exceptions.GoogleRefreshTokenResponseException;
import com.salesmanBuddy.exceptions.GoogleUserInfoException;
import com.salesmanBuddy.exceptions.InvalidUserTreeType;
import com.salesmanBuddy.exceptions.MalformedSBEmailException;
import com.salesmanBuddy.exceptions.NoResultInResultSet;
import com.salesmanBuddy.exceptions.UserNameException;
import com.salesmanBuddy.model.Buckets;
import com.salesmanBuddy.model.BucketsCE;
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.ErrorMessage;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.GoogleRefreshTokenResponse;
import com.salesmanBuddy.model.GoogleUserInfo;
import com.salesmanBuddy.model.ImageDetails;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.MaxValue;
import com.salesmanBuddy.model.Media;
import com.salesmanBuddy.model.Popups;
import com.salesmanBuddy.model.SBEmail;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.StockNumbers;
import com.salesmanBuddy.model.UserTree;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.QuestionsAndAnswers;
import com.salesmanBuddy.model.UsersName;



public class JDBCSalesmanBuddyDAO {
//	static Logger log = Logger.getLogger("log.dao");
//	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
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
	
	private SecureRandom random = new SecureRandom();
	
	public JDBCSalesmanBuddyDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
//		JDBCSalesmanBuddyDAO.sendErrorToMe("Initialized stuff, The current joda time is: " + new DateTime().toString() + ", utc: " + new DateTime(DateTimeZone.UTC).toString());
	}

	
	private Buckets getBucketForStateId(int stateId){
		String sql = "SELECT * FROM buckets WHERE stateId = ?";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, stateId);
			ResultSet resultSet = statement.executeQuery();
			results = Buckets.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 1)
			throw new RuntimeException("There is more than one bucket for state: " + stateId);
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	private AmazonS3 getAmazonS3(){
		AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);
		return s3;
	}
	
	private String addFileToBucket(String bucketName, String fileName, File file){
		AmazonS3 s3 = this.getAmazonS3();
		s3.putObject(new PutObjectRequest(bucketName, fileName, file));
		return fileName;
	}
	
	private File getFileFromBucket(String fileName, String bucketName){
		System.out.println("Downloading an object");
		AmazonS3 s3 = this.getAmazonS3();
		S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
		File tempFile = null;
		try{
			tempFile = File.createTempFile(this.randomAlphaNumericOfLength(15), ".jpeg");
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(object.getObjectContent(), out);
		}catch(IOException e){
			throw new RuntimeException("error copying inputstream from s3 to temporary file");
		}
		return tempFile;
	}
	
	private String createS3Bucket(String bucketName){
		AmazonS3 s3 = this.getAmazonS3();
		Bucket newBucket = s3.createBucket(bucketName);
		return newBucket.getName();
	}
	
	
	public FinishedPhoto saveFileToS3ForStateId(int stateId, File file){
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
	}
	
	
	public String randomAlphaNumericOfLength(Integer length){
		switch(length.intValue()){
		case 15:
			int tries = 0;
			while(true){
				String s = new BigInteger(130, random).toString(32);
				tries++;
				if(s.length() == 26 && s.charAt(0) >= 'a' && s.charAt(0) <= 'z')
					return s;
				else if(tries > 10000)
					throw new RuntimeException("couldnt get a random string length 26 not starting with a number after 10000 tries");
			}
		default:
			return "";
		}
	}
	
	private String makeBucketForStateId(int stateId){
		String bucketName = "state-" + this.getStateNameForStateId(stateId).toLowerCase() + "-uuid-" + UUID.randomUUID();
		bucketName = this.createS3Bucket(bucketName);
		
		String sql = "INSERT INTO buckets (stateId, name) VALUES (?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, stateId);
			statement.setString(2, bucketName);
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to make bucket for state id: " + stateId);
		return bucketName;
	}
	
	private String getStateNameForStateId(int stateId) {
		States state = this.getStateForId(stateId);
		if(state == null)
			throw new RuntimeException("could not find state for id: " + stateId);
		return state.getName();
	}

	
	public String getString() {
		return "From the dao";
	}

	
	public ArrayList<States> getAllStates(int getInactiveToo) {// working 10/3/13
		String sql = "SELECT * FROM states WHERE status = 1";
		if(getInactiveToo > 0)
			sql = "SELECT * FROM states";
		ArrayList<States> states = new ArrayList<States>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			states = States.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return states;
	}
	
	public States getStateForId(Integer stateId) {
		String sql = "SELECT * FROM states WHERE id = ?";
		States result = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, stateId);
			ResultSet resultSet = statement.executeQuery();
			result = States.parseOneRowResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return result;
	}

	
	public ArrayList<Dealerships> getAllDealerships() {// working 10/3/13
		String sql = "SELECT * FROM dealerships";
		ArrayList<Dealerships> results = new ArrayList<Dealerships>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = Dealerships.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public Dealerships getDealershipWithDealershipCode(String dealershipCode) {
		String sql = "SELECT * FROM dealerships WHERE dealershipCode = ?";
		Dealerships result = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, dealershipCode);
			ResultSet resultSet = statement.executeQuery();
			result = Dealerships.parseOneRowResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return result;
	}

	
	public ArrayList<LicensesListElement> getAllLicensesForUserId(String googleUserId) {
		String sql = "SELECT * FROM licenses WHERE userId = (SELECT id FROM users WHERE googleUserId = ?) AND showInUserList = 1 ORDER BY created desc";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			ResultSet resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		
		ArrayList<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
		for(int i = 0; i < results.size(); i++){
			results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId(), questions));
		}
		
		return results;
	}
	
	public List<LicensesListElement> getAllLicenses() {
		String sql = "SELECT * FROM licenses ORDER BY created desc";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		
		ArrayList<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
		for(int i = 0; i < results.size(); i++){
			results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId(), questions));
		}
		
		return results;
	}
	
	private LicensesListElement getLicenseListElementForLicenseId(int id) {
		String sql = "SELECT * FROM licenses WHERE id = ?";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}

		ArrayList<Questions> questions = this.getAllQuestions();// this makes it so getQuestionsAndAnswers doesnt have to poll the database for every question
		for(int i = 0; i < results.size(); i++){
			results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId(), questions));
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt find the license by id: " + id);
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
	
	private int putLicenseInDatabase(Licenses license){
		String sql = "INSERT INTO licenses (longitude, latitude, userId, stateId) VALUES (?, ?, ?, ?)";
		int id = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setFloat(1, license.getLongitude());
			statement.setFloat(2, license.getLatitude());
			statement.setInt(3, license.getUserId());
			statement.setInt(4, license.getStateId());
			statement.execute();
			id = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return id;
	}
	
	private int parseFirstInt(ResultSet generatedKeys, String key) {
		try {
			while(generatedKeys.next())
				return (int) generatedKeys.getLong(1);
		} catch (SQLException e) {
			throw new RuntimeException("failed parseFirstInt, error: " + e.getLocalizedMessage());
		}
		try {
			throw new RuntimeException("" + generatedKeys.getLong(1));
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
	
	private Licenses convertLicenseFromClientToLicense(LicensesFromClient lfc){
		Licenses l = new Licenses();
		l.setLatitude(lfc.getLatitude());
		l.setLongitude(lfc.getLongitude());
		l.setUserId(lfc.getUserId());
		l.setStateId(lfc.getStateId());
		return l;
	}


	
	public LicensesListElement putLicense(LicensesFromClient licenseFromClient, String googleUserId) {
		Users user = this.getUserByGoogleId(googleUserId);
		int licenseId = 0;
		if(user == null)
			throw new RuntimeException("couldnt find user for google id: " + googleUserId);
		licenseFromClient.setUserId(user.getId());
		if(licenseFromClient.getUserId() == 0)
			throw new RuntimeException("userid is " + licenseFromClient.getUserId() + ", its invalid");

		Licenses l = this.convertLicenseFromClientToLicense(licenseFromClient);
		licenseId = this.putLicenseInDatabase(l);
		if(licenseId == 0)
			throw new RuntimeException("failed to put license in database, licenseid returned: " + licenseId + ", license: " + l.toString());
		
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
	
	private int updateShowInUserListForLicenseId(int licenseId, int showInUserList){
		if(!(showInUserList == 1 || showInUserList == 0))
			throw new RuntimeException("updateShowInUserListForLicenseId failed because showInUserList was not 0 or 1");
		String sql = "UPDATE licenses SET showInUserList = ? WHERE id = ?";

		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, showInUserList);
			statement.setInt(2, licenseId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}

	
	public boolean userOwnsLicenseId(int licenseId, String googleUserId) {
		String sql = "SELECT * FROM licenses WHERE id = ? AND userId = (SELECT id FROM users WHERE googleUserId = ?)";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, licenseId);
			statement.setString(2, googleUserId);
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 0)
			return true;
		return false;
	}

	private Buckets getBucketForBucketId(Integer bucketId) {
		String sql = "SELECT * FROM buckets WHERE id = ?";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, bucketId);
			ResultSet resultSet = statement.executeQuery();
			results = Buckets.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() != 1)
			throw new RuntimeException("expected number of buckets for bucket id to be 1, id: " + bucketId + ", got: " + results.size());
		return results.get(0);
	}
	
	
	public Licenses getLicenseForLicenseId(int licenseId) {
		String sql = "SELECT * FROM licenses WHERE id = ?";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, licenseId);
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 0)
			return results.get(0);
		throw new RuntimeException("licenseId does not match any in the database");
	}


	
	public File getLicenseImageForPhotoNameBucketId(String photoName,Integer bucketId) {
		Buckets bucket = this.getBucketForBucketId(bucketId);
		return this.getFileFromBucket(photoName, bucket.getName());
	}

	
	public Users getUserByGoogleId(String googleUserId) {
		String sql = "SELECT * FROM users WHERE googleUserId = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 0)
			return results.get(0);
		return null;
	}
	
	
	public int createUser(Users user) {
		String sql = "INSERT INTO users (deviceType, type, googleUserId, refreshToken) VALUES(?, ?, ?, ?)";
		int id = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, user.getDeviceType());
			statement.setInt(2, 1);// 1:normal/salesman, 2:can see all dealership users, 3:salesman buddy employees
			statement.setString(3, user.getGoogleUserId());
			statement.setString(4, user.getRefreshToken());
			statement.executeUpdate();
			id = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(id == 0)
			throw new RuntimeException("failed inserting user, user: " + user.toString());
		return id;
	}


	
	public Users getUserById(int userId) {
		String sql = "SELECT * FROM users WHERE id = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, userId);
			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 0)
			return results.get(0);
		return null;
	}


	
	public LicensesListElement updateLicense(LicensesFromClient licenseFromClient, String googleUserId) {
		if(licenseFromClient.getId() == null || licenseFromClient.getId() == 0)
			throw new RuntimeException("id is either null or 0: " + licenseFromClient.toString());
		this.updateShowInUserListForLicenseId(licenseFromClient.getId(), licenseFromClient.getShowInUserList());
		for(QuestionsAndAnswers qaa : licenseFromClient.getQaas()){
			this.updateAnswerInDatabase(qaa.getAnswer());
		}
		return this.getLicenseListElementForLicenseId(licenseFromClient.getId());
	}


	
	public ArrayList<QuestionsAndAnswers> getQuestionsAndAnswersForLicenseId(int licenseId, ArrayList<Questions> questions) {
		ArrayList<Answers> answers = this.getAnswersForLicenseId(licenseId);
		ArrayList<QuestionsAndAnswers> qas = new ArrayList<QuestionsAndAnswers>();
		for(Answers a : answers){
			QuestionsAndAnswers qa = new QuestionsAndAnswers();
			qa.setAnswer(a);
//			qa.setQuestion(this.getQuestionById(a.getQuestionId()));// dont get it from the db every time, many db calls
			qa.setQuestion(this.getQuestionFromListById(questions, a.getQuestionId()));// just get question from the pregotten questions
			qas.add(qa);
		}
		return qas;
	}
	
	private Questions getQuestionFromListById(ArrayList<Questions> questions, int id){
		for(Questions q : questions){
			if(q.getId() == id)
				return q;
		}
		return null;
	}
	
	
	public ArrayList<Answers> getAnswersForLicenseId(int licenseId) {
		String sql = "SELECT * FROM answers WHERE licenseId = ?";
		ArrayList<Answers> results = new ArrayList<Answers>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, licenseId);
			ResultSet resultSet = statement.executeQuery();
			results = Answers.parseResultSet(resultSet);
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		for(Answers a : results){
			if(a.getAnswerType() == JDBCSalesmanBuddyDAO.isImage) {
				a.setImageDetails(this.getImageDetailsForAnswerId(a.getId()));
			}
		}
		return results;
	}


	private ImageDetails getImageDetailsForAnswerId(Integer answerId) {
		String sql = "SELECT * FROM imageDetails WHERE answerId = ?";
		ArrayList<ImageDetails> results = new ArrayList<ImageDetails>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, answerId);
			ResultSet resultSet = statement.executeQuery();
			results = ImageDetails.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	private int updateAnswerInDatabase(Answers answer) {
		String sql = "UPDATE answers SET answerBool = ?, answerType = ?, answerText = ?, licenseId = ?, questionId = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, answer.getAnswerBool());
			statement.setInt(2, answer.getAnswerType());
			statement.setString(3, answer.getAnswerText());
			statement.setInt(4, answer.getLicenseId());
			statement.setInt(5, answer.getQuestionId());
			statement.setInt(6, answer.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update answers failed for id: " + answer.getId());
		if(answer.getAnswerType() == JDBCSalesmanBuddyDAO.isImage)
			this.updateImageDetailsInDatabase(answer.getImageDetails());
		return i;
	}
	
	private int updateImageDetailsInDatabase(ImageDetails imageDetails) {
		String sql = "UPDATE imageDetails SET photoName = ?, bucketId = ? WHERE id = ?";

		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, imageDetails.getPhotoName());
			statement.setInt(2, imageDetails.getBucketId());
			statement.setInt(3, imageDetails.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update imageDetails failed for id: " + imageDetails.getId());
		return i;
	}


	private int updateQuestionInDatabase(Questions q){
		String sql = "UPDATE questions SET version = ?, questionOrder = ?, questionTextEnglish = ?, questionTextSpanish = ?, required = ?, questionType = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, q.getVersion());
			statement.setInt(2, q.getQuestionOrder());
			statement.setString(3, q.getQuestionTextEnglish());
			statement.setString(4, q.getQuestionTextSpanish());
			statement.setInt(5, q.getRequired());
			statement.setInt(6, q.getQuestionType());
			statement.setInt(7, q.getId());
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	
	private int putQuestionInDatabase(Questions q){
		String sql = "INSERT INTO questions (version, questionOrder, questionTextEnglish, questionTextSpanish, required, questionType) VALUES (?, ?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, q.getVersion());
			statement.setInt(2, q.getQuestionOrder());
			statement.setString(3, q.getQuestionTextEnglish());
			statement.setString(4, q.getQuestionTextSpanish());
			statement.setInt(5, q.getRequired());
			statement.setInt(6, q.getQuestionType());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	
	private int putAnswerInDatabase(Answers answer) {
		String sql = "INSERT INTO answers (answerText, answerBool, licenseId, questionId, answerType) VALUES (?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			if(answer.getAnswerText() == null)
				answer.setAnswerText("");
			statement.setString(1, answer.getAnswerText());
			statement.setInt(2, answer.getAnswerBool());
			statement.setInt(3, answer.getLicenseId());
			statement.setInt(4, answer.getQuestionId());
			statement.setInt(5, answer.getAnswerType());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to insert answer into database, " + answer.toString());
		if(answer.getAnswerType() == 1){
			answer.getImageDetails().setAnswerId(i);
			if(this.putImageDetailsInDatabase(answer.getImageDetails()) == 0)
				throw new RuntimeException("failed to insert image details into database, " + answer.getImageDetails().toString());
		}
		return i;
	}

	public Questions getQuestionById(Integer questionId) {
		String sql = "SELECT * FROM questions WHERE id = ?";
		ArrayList<Questions> results = new ArrayList<Questions>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, questionId);
			ResultSet resultSet = statement.executeQuery();
			results = Questions.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		return null;
	}
	
	public ArrayList<Questions> getAllQuestions() {
		String sql = "SELECT * FROM questions ORDER BY questionOrder";
		ArrayList<Questions> results = new ArrayList<Questions>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			ResultSet resultSet = statement.executeQuery();
			results = Questions.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
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
		String sql = "INSERT INTO imageDetails (photoName, bucketId, answerId) VALUES (?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, imageDetails.getPhotoName());
			statement.setInt(2, imageDetails.getBucketId());
			statement.setInt(3, imageDetails.getAnswerId());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle + ", imageDetials: " + imageDetails.toString());
		}
		if(i == 0)
			throw new RuntimeException("insert imageDetails failed, " + imageDetails.toString());
		return i;
	}


	
	public Questions putQuestion(Questions question) {
		this.putQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}


	
	public Questions updateQuestion(Questions question) {
		this.updateQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}
	
	
	
	
	public ArrayList<Languages> getAllLanguages(int onlyMtcTaught) {
		String sql = "SELECT * FROM languages";
		if(onlyMtcTaught == 1)
			sql = "SELECT * FROM languages WHERE mtcTaught = 1";
		ArrayList<Languages> results = new ArrayList<Languages>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = Languages.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public List<Users> getAllUsers() {
		String sql = "SELECT * FROM users";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public ArrayList<Users> getUsersForDealershipId(Integer dealershipId) {
		String sql = "SELECT * FROM users WHERE dealershipId = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, dealershipId);
			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public Users updateUserToType(String googleUserId, int type) {
		String sql = "UPDATE users SET type = ? WHERE googleUserId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, type);
			statement.setString(2, googleUserId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to update googleUserId: " + googleUserId);
		return this.getUserByGoogleId(googleUserId);
	}


	
	public Users updateUserToDealershipCode(String googleUserId, String dealershipCode) {
		int dealershipId = this.getDealershipByDealershipCode(dealershipCode).getId();
		String sql = "UPDATE users SET dealershipId = ? WHERE googleUserId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			statement.setString(2, googleUserId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException("dealershipId: " + dealershipId + ", " + sqle.getLocalizedMessage());
		}
		if(i == 0)
			throw new RuntimeException("failed to update googleUserId: " + googleUserId);
		return this.getUserByGoogleId(googleUserId);
	}


	private Dealerships getDealershipByDealershipCode(String dealershipCode) {
		String sql = "SELECT * FROM dealerships WHERE dealershipCode = ?";
		ArrayList<Dealerships> results = new ArrayList<Dealerships>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, dealershipCode);
			ResultSet resultSet = statement.executeQuery();
			results = Dealerships.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("failed to get dealership by dealershipCode: " + dealershipCode + ", count: " + results.size());
	}


	
	public List<LicensesListElement> getAllLicensesForDealershipId(Integer dealershipId) {
		String sql = "SELECT * FROM users WHERE dealershipId = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		ArrayList<LicensesListElement> licenses = new ArrayList<LicensesListElement>();
		for(Users u : results){
			licenses.addAll(this.getAllLicensesForUserId(u.getGoogleUserId()));
		}
		return licenses;
	}
	
	
	
	
	public Dealerships getDealershipById(Integer dealershipId) {
		String sql = "SELECT * FROM dealerships WHERE id = ?";
		ArrayList<Dealerships> results = new ArrayList<Dealerships>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			ResultSet resultSet = statement.executeQuery();
			results = Dealerships.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("failed to get the dealership by id: " + dealershipId);
	}
	
	
	public Dealerships newDealership(Dealerships dealership) {
		String sql = "INSERT INTO dealerships (name, city, stateId, dealershipCode, notes) VALUES (?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, dealership.getName());
			statement.setString(2, dealership.getCity());
			statement.setInt(3, dealership.getStateId());
			statement.setString(4, UUID.randomUUID().toString());
			statement.setString(5, dealership.getNotes());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert dealership failed");
		return this.getDealershipById(i);
	}


	
	public Dealerships updateDealership(Dealerships dealership) {
		String sql = "UPDATE dealerships SET name = ?, city = ?, stateId = ?, notes = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, dealership.getName());
			statement.setString(2, dealership.getCity());
			statement.setInt(3, dealership.getStateId());
			statement.setString(4, dealership.getNotes());
			statement.setInt(5, dealership.getId());
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to update dealership");
		return this.getDealershipById(dealership.getId());
	}
	
	
	public void updateRefreshTokenForUser(Users userFromClient) {
		if(userFromClient.getDeviceType() < 1 || userFromClient.getDeviceType() > 3)
			throw new RuntimeException("their device type is not within the range 1-3, user: " + userFromClient.toString());
		
		String sql = "UPDATE users SET refreshToken = ?, deviceType = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, userFromClient.getRefreshToken());
			statement.setInt(2, userFromClient.getDeviceType());
			statement.setInt(3, userFromClient.getId());
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
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
//		JDBCSalesmanBuddyDAO.sendErrorToMe("Got refresh token again for this: " + grtr.toString());
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
	
	
	public GoogleRefreshTokenResponse getValidTokenForUser(String googleUserId, Users user) throws GoogleRefreshTokenResponseException {
		if(user == null)
			user = this.getUserByGoogleId(googleUserId);
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
			throw new RuntimeException("IOException: " + e.getLocalizedMessage() + ", deviceType:" + user.getDeviceType() + ", " + webString);
		}catch(JSONException jse){
			throw new RuntimeException("JSONException: " + jse.getLocalizedMessage());
		}
		
		GoogleRefreshTokenResponse grtr = new GoogleRefreshTokenResponse(json);
		// TODO put token in database for caching?
		
		return grtr; 
	}
	
	public static void sendErrorToMe(String errorString){
		ArrayList<String> to = new ArrayList<String>();
		to.add("cameronmccord2@gmail.com");
		try {
			SBEmail.newPlainTextEmail("logging@salesmanbuddy.com", to, "error", errorString, true).send();
		} catch (MalformedSBEmailException e) {
			e.printStackTrace();
			System.out.println(new StringBuilder().append("BIGERROR, there was an error sending an error email, message: ").append(e.getLocalizedMessage()));
		}
	}

	
	
	
	public UsersName getUsersName(String googleUserId) throws UserNameException {
		GoogleUserInfo gui;
		UsersName name = new UsersName();
		try {
			gui = this.getGoogleUserInfoWithId(googleUserId);
			name.setName(gui.getName());
		} catch (GoogleUserInfoException e) {
			e.printStackTrace();
			throw new UserNameException(e.getLocalizedMessage());
		} catch (GoogleRefreshTokenResponseException e) {
			e.printStackTrace();
			throw new UserNameException(e.getLocalizedMessage());
		}
		return name;
	}
	
	
	public GoogleUserInfo getGoogleUserInfoWithId(String googleUserId) throws GoogleUserInfoException, GoogleRefreshTokenResponseException{
		GoogleRefreshTokenResponse grtr = this.getValidTokenForUser(googleUserId, null);
		return this.getGoogleUserInfo(grtr.getTokenType(), grtr.getAccessToken());
	}
	
	
	public GoogleUserInfo getGoogleUserInfo(String tokenType, String accessToken) throws GoogleUserInfoException {
		URL url;
		byte[] body = null;
		JSONObject json = null;
		String whatItHas = "";
		try {
			url = new URL(GoogleUserEndpoint);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setDoOutput(true);
			conn.setRequestMethod("GET");
			
			conn.setRequestProperty("Authorization", tokenType + " " + accessToken);
			whatItHas = conn.getRequestProperty("Authorization");
			
			body = IOUtils.toByteArray(conn.getInputStream());
			json = new JSONObject(new String(body));
			
		} catch (ProtocolException pe){
			throw new RuntimeException("protocolExceptions: " + pe.getLocalizedMessage());
		}catch (MalformedURLException e) {
			throw new RuntimeException("malformedUrlException: " + e.getLocalizedMessage());
		} catch (IOException e) {
			throw new RuntimeException("IOException: " + e.getLocalizedMessage() + ", tokenType:" + tokenType + ", accessToken: " + accessToken + ", auth:" + whatItHas + ", json: " + json + ", e: " + e);
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
			ArrayList<SBEmail> emails = generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, reportType);
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
	
	private ArrayList<SBEmail> generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(Integer dealershipId, Integer reportType) {
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
		
		ArrayList<UserTree> userTrees = this.getAllUserTreeForDealershipIdType(dealershipId, reportType);
		ArrayList<SBEmail> emails = new ArrayList<SBEmail>();
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
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		ArrayList<String> toEmails = new ArrayList<String>();
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
			ArrayList<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Daily individual salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}

	private void runWeeklyReports() {
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		ArrayList<String> toEmails = new ArrayList<String>();
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
			ArrayList<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", Weekly individual salesman summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}

	private void runMonthlyReports() {
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		String subject = null;
		String body = null;
		ArrayList<String> toEmails = new ArrayList<String>();
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
			ArrayList<SBEmail> emails = this.generateIndividualSalesmanSummaryEmailsForDealershipIdReportType(dealershipId, type);
			try {
				EmailSender.sendEmails(emails);
			} catch (MalformedSBEmailException e) {
				e.printStackTrace();
				JDBCSalesmanBuddyDAO.sendErrorToMe(new StringBuilder().append("Error sending email about dealership: ").append(d.toString()).append(", montly individual salesmen summary type, error:").append(e.getLocalizedMessage()).toString());
			}
		}
	}
	
	private ArrayList<String> getEmailsForUserFromUserTrees(ArrayList<UserTree> userTrees){
		ArrayList<String> ids;
		try {
			ids = this.getUserTreeGoogleIdsForType(userTrees, USER_TREE_TYPE);
		} catch (InvalidUserTreeType e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return this.getEmailsForGoogleIds(ids);
	}
	
	private ArrayList<String> getEmailsForSupervisorFromUserTrees(ArrayList<UserTree> userTrees){
		ArrayList<String> ids;
		try {
			ids = this.getUserTreeGoogleIdsForType(userTrees, SUPERVISOR_TREE_TYPE);
		} catch (InvalidUserTreeType e) {
			throw new RuntimeException(e.getLocalizedMessage());
		}
		return this.getEmailsForGoogleIds(ids);
	}

	private String individualSalesmanSummaryReport(Integer userId, DateTime from, DateTime to){
		Users user = this.getUserById(userId);
		ArrayList<Licenses> licenses = this.getLicensesForDateRangeUserId(userId, to, from);
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
					
				} catch (NoResultInResultSet e) {
					// fail silently
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
		ArrayList<Users> salesmen = this.getAllUsersForDealershipId(dealershipId);
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
		ArrayList<Licenses> licenses = this.getLicensesForDateRangeDealershipId(from, to, dealershipId);
		ArrayList<Users> users = this.getAllUsersForDealershipId(dealershipId);
		Dealerships d = this.getDealershipById(dealershipId);
		StringBuilder sb = new StringBuilder();
		sb.append(d.getName()).append(" had ").append(licenses.size()).append(" test drives by ").append(users.size());
		sb.append(" salesmen during this time period. The dealership also sold ");
		ArrayList<StockNumbers> stockNumbers = this.getStockNumbersForDealershipFromTo(dealershipId, from, to);
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
		ArrayList<Licenses> licenses = this.getLicensesForDateRangeDealershipId(from, to, dealershipId);
		String licensesMessage = this.createLicensesSummaryForLicenses(licenses, from, to, dealershipId, DEALERSHIP_TYPE);
		String finalMessage = this.wrapReportContentWithBeginningEnd(licensesMessage, ReportBeginEnd.TestDriveSummary, ReportBeginEnd.TestDriveSummary, dealershipId, from, to);
		return finalMessage;
	}
	
	private String createLicensesSummaryForLicenses(ArrayList<Licenses> licenses, DateTime from, DateTime to, Integer dealershipId, Integer dealershipType) {
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
		String sql = "SELECT q.tag tag, a.answerText answerText FROM answers a, questions q WHERE a.licenseId = ? AND (q.tag = ? OR q.tag = ?) AND a.questionId = q.id";
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

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		
		if(firstName.equals("<Unknown>") && lastName.equals("<Unknown>"))
			return "<Unknown>";// so it isnt <Unknown> <Unknown> on the report, just one <Unknown>
		return new StringBuilder().append(firstName).append(" ").append(lastName).toString();
	}

	private String getStockNumberForLicenseId(Integer id) {
		String sql = "SELECT a.answerText answerText FROM answers a, questions q WHERE a.licenseId = ? AND a.questionId = q.id AND q.tag = ?";
		String stockNumber = "<Unknown>";
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			statement.setInt(2, QUESTION_STOCK_NUMBER);
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				stockNumber = resultSet.getString("answerText");
				break;
			}

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return stockNumber;
	}

	private String stockNumberSummaryReport(Integer dealershipId, DateTime from, DateTime to){
		ArrayList<String> stockNumbers = this.getUniqueStockNumbersForDealershipId(dealershipId);
		StringBuilder sb = new StringBuilder();
		for(String sn : stockNumbers){
			sb.append(this.stockNumberReportForThisStockNumber(sn, from, to));
			sb.append("\n");
		}
		String finalMessage = this.wrapReportContentWithBeginningEnd(sb.toString(), ReportBeginEnd.StockNumberSummary, ReportBeginEnd.StockNumberSummary, dealershipId, from, to);
		return finalMessage;
	}
	
	private String stockNumberReportForThisStockNumber(String stockNumber, DateTime from, DateTime to) {
		ArrayList<Licenses> licenses = this.getLicensesWithStockNumberFromTo(stockNumber, from, to);
		StockNumbers sn = null;
		try {
			sn = this.getStockNumberByStockNumber(stockNumber);
		} catch (NoResultInResultSet e1) {
			// do nothing
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
			ArrayList<Licenses> allLicenses = this.getLicensesWithStockNumber(stockNumber);
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

	private ArrayList<Licenses> getLicensesWithStockNumberFromTo(String stockNumber, DateTime from, DateTime to) {
		String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId AND l.created between ? and ? ORDER BY l.created";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, stockNumber);
			statement.setInt(2, QUESTION_STOCK_NUMBER);
			statement.setString(3, from.toString());
			statement.setString(4, to.toString());
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	private ArrayList<Licenses> getLicensesWithStockNumber(String stockNumber) {
		String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId ORDER BY l.created";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, stockNumber);
			statement.setInt(2, QUESTION_STOCK_NUMBER);
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
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
		ArrayList<UserTree> userTrees = this.getAllUserTreesForGoogleUserIdType(googleUserId, ON_TEST_DRIVE_EMAIL_TYPE);
		ArrayList<String> supervisorEmails = this.getEmailsForSupervisorFromUserTrees(userTrees);
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
	
	private DateTime getNowTime(){
		return new DateTime(DateTimeZone.UTC);
	}


	private String getStatsAboutUserId(Integer userId) {
		DateTime to = this.getNowTime();
		DateTime from = to.minusWeeks(1);
		ArrayList<Licenses> licenses = this.getLicensesForDateRangeUserId(userId, to, from);
		StringBuilder sb = new StringBuilder();
		sb.append("This salesman has had ").append(licenses.size()).append(" test drives in the last week.\n");
		return sb.toString();
	}


	private ArrayList<Licenses> getLicensesForDateRangeUserId(Integer userId, DateTime to, DateTime from) {
		String sql = "SELECT * FROM licenses WHERE userId = ? AND created BETWEEN ? AND ?;";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, userId);
			statement.setString(2, from.toString());
			statement.setString(3, to.toString());
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	private String getStatsAboutStockNumber(String stockNumber, Integer dealershipId) {
		DateTime to = this.getNowTime();
		DateTime from = to.minusWeeks(1);
		ArrayList<Licenses> licenses = this.getAllLicensesForStockNumberInDateRange(stockNumber, to, from);
		StringBuilder sb = new StringBuilder();
		sb.append("Stock Number ").append(stockNumber).append(" has been test driven ").append(licenses.size());
		sb.append(" times in the last week.\n");
		return sb.toString();
	}

	private ArrayList<Licenses> getLicensesForDateRangeDealershipId(DateTime from, DateTime to, Integer dealershipId) {
		String sql = "SELECT * FROM licenses WHERE userId IN (SELECT id FROM users WHERE dealershipId = ?) AND created BETWEEN ? AND ?;";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			statement.setString(2, from.toString());
			statement.setString(3, to.toString());
			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	private ArrayList<Licenses> getAllLicensesForStockNumberInDateRange(String stockNumber, DateTime to, DateTime from) {
		String sql = "SELECT l.* FROM licenses l, answers a, questions q WHERE q.tag = ? AND a.questionId = q.id AND a.answerText = ? AND a.licenseId = l.id";
//		String sql = "SELECT distinct a.answerText as stockNumber FROM answers a, licenses l, users u, questions q WHERE q.tag = ? AND len(a.answerText) > 0 AND a.questionId = q.id AND a.licenseId = l.id AND l.userId = u.id AND u.dealershipId = ?;";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, QUESTION_STOCK_NUMBER);
			statement.setString(2, stockNumber);
			

			ResultSet resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	private ArrayList<String> getUserTreeGoogleIdsForType(ArrayList<UserTree> userTrees, Integer type) throws InvalidUserTreeType {
		ArrayList<String> ids = new ArrayList<String>();
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
		String sql = "INSERT INTO userTree (userId, supervisorId, type) VALUES(?, ?, ?)";
		int i = 0;
		if(supervisorId == null)// allows for dealership-wide reports
			supervisorId = "";
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, googleUserId);
			statement.setString(2, supervisorId);
			statement.setInt(3, type);
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert userTree failed, i == 0, " + googleUserId + ", supervisorId: " + supervisorId);
		return i;
	}
	
	private ArrayList<UserTree> getAllUserTreeForDealershipIdType(Integer dealershipId, Integer type) {
		String sql = "SELECT * FROM userTree ut WHERE type = ? AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?));";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, type);
			statement.setInt(2, dealershipId);
			statement.setInt(3, dealershipId);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public UserTree getUserTreeById(Integer id){
		String sql = "SELECT * FROM userTree WHERE id = ?";
		UserTree result = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			result = UserTree.parseOneResultFromSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return result;
	}
	
	public ArrayList<UserTree> getAllUserTreeForGoogleUserId(String googleUserId){
		String sql = "SELECT * FROM userTree WHERE userId = ?";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public ArrayList<UserTree> getAllUserTreesForGoogleUserIdType(String googleUserId, Integer type){
		String sql = "SELECT * FROM userTree WHERE userId = ? AND type = ?";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			statement.setInt(2, type);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public ArrayList<UserTree> getAllUserTreeForGoogleSupervisorId(String googleSupervisorId){
		String sql = "SELECT * FROM userTree WHERE supervisorId = ?";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleSupervisorId);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public ArrayList<UserTree> getAllUserTree() {
		String sql = "SELECT * FROM userTree ORDER BY userId";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public ArrayList<UserTree> getAllUserTreeForGoogleSupervisorIdAndGoogleUserId(String googleUserId) {
		String sql = "SELECT * FROM userTree WHERE supervisorId = ? OR userId = ?";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			statement.setString(2, googleUserId);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public ArrayList<UserTree> getAllUserTreeForDealershipId(Integer dealershipId) {
		String sql = "SELECT * FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?);";
		ArrayList<UserTree> results = new ArrayList<UserTree>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			statement.setInt(2, dealershipId);
			ResultSet resultSet = statement.executeQuery();
			results = UserTree.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	private ArrayList<String> getUniqueStockNumbersForDealershipId(Integer dealershipId) {
		String sql = "SELECT distinct a.answerText as stockNumber FROM answers a, licenses l, users u, questions q WHERE q.tag = ? AND len(a.answerText) > 0 AND a.questionId = q.id AND a.licenseId = l.id AND l.userId = u.id AND u.dealershipId = ?;";
		ArrayList<String> results = new ArrayList<String>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, QUESTION_STOCK_NUMBER);
			statement.setInt(2, dealershipId);

			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				results.add(resultSet.getString("stockNumber"));
			}

		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	private ArrayList<Users> getAllUsersForDealershipId(Integer dealershipId) {
		String sql = "SELECT * FROM users WHERE dealershipId = ?;";
		ArrayList<Users> results = new ArrayList<Users>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);

			ResultSet resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	private String getEmailForGoogleId(String supervisorId) {
		ArrayList<String> ids = new ArrayList<String>();
		ids.add(supervisorId);
		ArrayList<String> emails = this.getEmailsForGoogleIds(ids);
		if(emails.size() > 0)
			return emails.get(0);
		return ERRORED_EMAIL;
	}
	
	public ArrayList<String> getEmailsForGoogleIds(ArrayList<String> googleIds){
		Integer unverifiedEmails = 0;
		HashSet<String> recipients = new HashSet<String>();
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
		String sql = "UPDATE userTree SET userId = ?, supervisorId = ?, type = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			statement.setString(2, googleSupervisorId);
			statement.setInt(3, type);
			statement.setInt(4, id);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to update userTree, id: " + id);
		return i;
	}
	
	public int deleteUserTreeNodeById(Integer id){
		String sql = "DELETE FROM userTree WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete userTree, id: " + id);
		return i;
	}
	
	public int deleteUserTreeNodesForGoogleUserIdAllNodes(String googleUserId){
		String sql = "DELETE FROM userTree WHERE supervisorId = ? OR userId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			statement.setString(2, googleUserId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreeNodesForUserId(String googleUserId){
		String sql = "DELETE FROM userTree WHERE userId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete user's userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreeNodesForSupervisorId(String googleUserId){
		String sql = "DELETE FROM userTree WHERE supervisorId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, googleUserId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete supervisor's userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public ErrorMessage deleteUserTreeNodesForDealershipId(Integer dealershipId) {
		String sql = "DELETE FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			statement.setInt(2, dealershipId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree nodes for dealerhsipId: " + dealershipId);
		return new ErrorMessage("Not an error, successfully deleted " + i + " userTree nodes for dealerhsipId: " + dealershipId);
	}

	public ErrorMessage deleteAllUserTreeNodes() {
		String sql = "DELETE FROM userTree";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree nodes");
		return new ErrorMessage("this isnt an error, successfully deleted all userTree nodes");
	}
	
	private StockNumbers getStockNumberByStockNumber(String stockNumber) throws NoResultInResultSet {
		String sql = "SELECT * FROM stockNumbers WHERE stockNumber = ?";
		StockNumbers result = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, stockNumber);

			ResultSet resultSet = statement.executeQuery();
			result = StockNumbers.parseOneRowResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return result;
	}
	
	public StockNumbers getStockNumberById(Integer id) throws NoResultInResultSet {
		String sql = "SELECT * FROM stockNumbers WHERE id = ?;";
		StockNumbers result = null;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);

			ResultSet resultSet = statement.executeQuery();
			result = StockNumbers.parseOneRowResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return result;
	}


	public List<StockNumbers> getAllStockNumbers() {
		String sql = "SELECT * FROM stockNumbers";
		ArrayList<StockNumbers> results = new ArrayList<StockNumbers>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = StockNumbers.parseResultSet(resultSet);
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	public List<StockNumbers> getStockNumbersForDealershipId(Integer dealershipId) {
		String sql = "SELECT * FROM stockNumbers WHERE dealershipId = ?;";
		ArrayList<StockNumbers> results = new ArrayList<StockNumbers>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);

			ResultSet resultSet = statement.executeQuery();
			results = StockNumbers.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	private ArrayList<StockNumbers> getStockNumbersForDealershipFromTo(Integer dealershipId, DateTime from, DateTime to) {
		String sql = "SELECT * FROM stockNumbers WHERE dealershipId = ? AND soldOn between ? and ?";
		ArrayList<StockNumbers> results = new ArrayList<StockNumbers>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, dealershipId);
			statement.setString(2, from.toString());
			statement.setString(3, to.toString());

			ResultSet resultSet = statement.executeQuery();
			results = StockNumbers.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public StockNumbers newStockNumber(StockNumbers stockNumber) {
		String sql = "INSERT INTO stockNumbers (dealershipId, stockNumber, status, createdBy) VALUES(?, ?, ?, ?)";
		int i = 0;
		
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, stockNumber.getDealershipId());
			statement.setString(2, stockNumber.getStockNumber());
			statement.setInt(3, stockNumber.getStatus());
			statement.setInt(4, stockNumber.getCreatedBy());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert stockNumbers failed, i == 0, stockNumber: " + stockNumber.toString());
		
		try {
			return this.getStockNumberById(i);// we know this will work because of the above test
		} catch (NoResultInResultSet e) {
			e.printStackTrace();
		}
		return null;
	}


	public Integer deleteStockNumberById(Integer id) {
		String sql = "DELETE FROM stockNumbers WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}

	public StockNumbers updateStockNumber(StockNumbers stockNumber) {
		String sql = "UPDATE stockNumbers SET dealershipId = ?, stockNumber = ?, status = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, stockNumber.getDealershipId());
			statement.setString(2, stockNumber.getStockNumber());
			statement.setInt(3, stockNumber.getStatus());
			statement.setInt(4, stockNumber.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("StockNumbers: " + stockNumber.toString() + ", error: " + sqle.getLocalizedMessage());
		}
		if(i == 0)
			throw new RuntimeException("update stockNumber failed for stockNumbers: " + stockNumber.toString());
		
		return stockNumber;
	}
	
	public StockNumbers updateStockNumberSoldOn(Integer id, DateTime at) {
		// TODO make sure this works properly
		String sql = "UPDATE stockNumbers SET soldOn = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, at.toString());
			statement.setInt(2, id);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("StockNumberId: " + id + ", dateTime: " + at.toString() + ", error: " + sqle.getLocalizedMessage());
		}
		if(i == 0)
			throw new RuntimeException("update stockNumber failed for stockNumberId: " + id + ", dateTime: " + at.toString());
		
		try {
			return this.getStockNumberById(id);
		} catch (NoResultInResultSet e) {
			// fail silently
		}
		return null;// will never happen because update was successful
	}

	public boolean userHasRightsToStockNumberId(Integer stockNumberId, String googleUserId) {
		try {
			Users user = this.getUserByGoogleId(googleUserId);
			if(user.getType() > 2)
				return true;
			
			StockNumbers stockNumber = this.getStockNumberById(stockNumberId);
			
			if(user.getDealershipId() == stockNumber.getDealershipId())
				return true;
			
		} catch (NoResultInResultSet e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

//	trainer stuff


	
	public ArrayList<Captions> putCaptions(List<Captions> captions) {
		if(captions.size() == 0)
			return new ArrayList<Captions>();
		
		// get latest version to use
		int version = this.getLatestCaptionVersionForMediaIdLanguageId(captions.get(0).getMediaId(), captions.get(0).getLanguageId());
		version++;
		
		for(Captions c : captions){
			c.setVersion(version);
			this.putCaption(c);
		}
		return this.getAllCaptionsForMediaIdLanguageId(captions.get(0).getMediaId(), captions.get(0).getLanguageId());
	}
	
	private int getLatestCaptionVersionForMediaIdLanguageId(Integer mediaId, Integer languageId) {
		String sql = "SELECT MAX(version) AS maxValue FROM captions WHERE mediaId = ? AND languageId = ?";
		Integer maxValue = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			ResultSet resultSet = statement.executeQuery();
			maxValue = MaxValue.parseResultSetForMaxValue(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return maxValue;
	}


	private int putCaption(Captions caption){
		String sql = "INSERT INTO captions (version, caption, mediaId, startTime, endTime, type, languageId) VALUES (?, ?, ?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setInt(1, caption.getVersion());
			statement.setString(2, caption.getCaption());
			statement.setInt(3, caption.getMediaId());
			statement.setInt(4, caption.getStartTime());
			statement.setInt(5, caption.getEndTime());
			statement.setInt(6, caption.getType());
			statement.setInt(7, caption.getLanguageId());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert captions failed, i == 0");
		return i;
	}


	
	public ArrayList<Captions> getAllCaptionsForMediaIdLanguageId(int mediaId, int languageId) {
		Integer latestVersion = this.getLatestCaptionVersionForMediaIdLanguageId(mediaId, languageId);
		
		String sql = "SELECT * FROM captions WHERE mediaId = ? AND languageId = ? AND version = ? ORDER BY startTime";
		ArrayList<Captions> results = new ArrayList<Captions>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			statement.setInt(3, latestVersion);
			ResultSet resultSet = statement.executeQuery();
			results = Captions.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public Media putMedia(Media media) {
		if(media.getId() == 0)
			return this.putNewMedia(media);
		else
			return this.updateMedia(media);
	}
		
	private Media updateMedia(Media media){
		String sql = "UPDATE media SET name = ?, filename = ?, type = ?, audioLanguageId = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
//			statement.setString(5, media.getExtension());
			statement.setInt(5, media.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("Media: " + media.toString() + ", error: " + sqle.getLocalizedMessage());
		}
		if(i == 0)
			throw new RuntimeException("update media failed for id: " + media.getId() + ", object: " + media.toString());
		
		// save off the file here
		if(media.getBase64Data() != null && media.getBase64Data().length() > 0)
			return this.saveFileThatWasPutWithNewMedia(media);
		else
			return this.getMediaById(media.getId());
		
//		return this.getMediaById(media.getId());
	}
	
	public int deleteMediaById(int mediaId) {
		this.deletePopupsWithMediaId(mediaId);
		this.deleteCaptionsWithMediaId(mediaId);
		String sql = "DELETE FROM media WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("Delete media with id: " + mediaId + ", error: " + sqle.getLocalizedMessage());
		}
		return i;
	}
	
	private int deleteCaptionsWithMediaId(int mediaId) {
		String sql = "DELETE FROM captions WHERE mediaId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("delete captions with mediaId: " + mediaId + ", error: " + sqle.getLocalizedMessage());
		}
		return i;
	}


	private int deletePopupsWithMediaId(int mediaId) {
		String sql = "DELETE FROM popups WHERE mediaId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException("delete popups with mediaId: " + mediaId + ", error: " + sqle.getLocalizedMessage());
		}
		return i;
	}


	private Media putNewMedia(Media media){
		String sql = "INSERT INTO media (name, filename, type, audioLanguageId) VALUES (?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert media failed, i == 0");
		media.setId(i);
		// save off the file here
		if(media.getBase64Data() != null && media.getBase64Data().length() > 0)
			return this.saveFileThatWasPutWithNewMedia(media);
		else
			return this.getMediaById(i);
	}

	private Media saveFileThatWasPutWithNewMedia(Media media) {
//		String mimeType = media.getContentType();
		String extension = media.getExtension();
		File file = null;
		FileOutputStream fos = null;
		
		try{// working 10/25
			file = File.createTempFile(this.randomAlphaNumericOfLength(15), extension);
			file.deleteOnExit();
			fos = new FileOutputStream(file);
			byte [] fileBytes = DatatypeConverter.parseBase64Binary(media.getBase64Data());
			IOUtils.write(fileBytes, fos);
		}catch (IOException e){
			throw new RuntimeException(e);
		}finally{
			try {
				fos.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		
		String filenameInBucket = this.saveFileToS3ForCaptionEditor(file, extension, media.getId(), 0);// file from this is usable everywhere else, works in chrome
		file.delete();
		Media newMedia = this.updateMediaForFileUpload(filenameInBucket, this.getCaptionEditorBucket().getId(), extension, media.getId());
		return newMedia;
	}


	
	public Media getMediaById(int id) {
		String sql = "SELECT * FROM media WHERE id = ?";
		ArrayList<Media> results = new ArrayList<Media>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			ResultSet resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt find media by id: " + id + ", result set size was: " + results.size());
	}


	
	public ArrayList<Media> getAllMedia() {
		String sql = "SELECT * FROM media";
		ArrayList<Media> results = new ArrayList<Media>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			ResultSet resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public ArrayList<Languages> putLanguages(List<Languages> languages) {
		for(Languages l : languages){
			this.putLanguage(l);
		}
		return this.getAllLanguages(0);
	}
	
	private int putLanguage(Languages language){
		String sql = "INSERT INTO languages (mtcId, code1, code2, name, mtcTaught, alternateName, nativeName) VALUES (?, ?, ?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, language.getMtcId());
			statement.setString(2, language.getCode1());
			statement.setString(3, language.getCode2());
			statement.setString(4, language.getName());
			statement.setInt(5, language.getMtcTaught());
			statement.setString(6, language.getAlternateName());
			statement.setString(7, language.getNativeName());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert language failed, i == 0, object: " + language.toString());
		return i;
	}


	
	public List<Popups> getAllPopups() {
		String sql = "SELECT * FROM popups ORDER BY startTime";
		ArrayList<Popups> results = new ArrayList<Popups>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public List<Popups> getAllPopupsForLanguageId(int languageId) {
		String sql = "SELECT * FROM popups WHERE languageId = ? ORDER BY startTime";
		ArrayList<Popups> results = new ArrayList<Popups>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, languageId);

			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public List<Popups> getAllPopupsForMediaId(int mediaId) {
		String sql = "SELECT * FROM popups WHERE mediaId = ? ORDER BY startTime";
		ArrayList<Popups> results = new ArrayList<Popups>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);

			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}


	
	public List<Popups> getPopupsForMediaIdLanguageId(int languageId, int mediaId) {
		String sql = "SELECT * FROM popups WHERE languageId = ? AND mediaId = ? ORDER BY startTime";
		ArrayList<Popups> results = new ArrayList<Popups>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, languageId);
			statement.setInt(2, mediaId);
			
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	
	public Popups newPopup(Popups popup) {
		String sql = "INSERT INTO popups (displayName, popupText, mediaId, languageId, startTime, endTime, filename) VALUES (?, ?, ?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
			statement.setString(1, popup.getDisplayName());
			statement.setString(2, popup.getPopupText());
			statement.setInt(3, popup.getMediaId());
			statement.setInt(4, popup.getLanguageId());
			statement.setInt(5, popup.getStartTime());
			statement.setInt(6, popup.getEndTime());
			statement.setString(7, popup.getFilename());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert media failed, i == 0");
		return this.getPopupById(i);
	}
	
	
	public List<Popups> putPopups(List<Popups> popups) {
		ArrayList<Popups> newList = new ArrayList<Popups>();
		for(Popups popup : popups){
			if(popup.getId() == 0)
				newList.add(this.newPopup(popup));
			else
				newList.add(this.updatePopup(popup));
		}
		return newList;
	}

	private Popups getPopupById(int popupId) {
		String sql = "SELECT * FROM popups WHERE id = ?";
		ArrayList<Popups> results = new ArrayList<Popups>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, popupId);
			
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt get popup by id: " + popupId);
	}

	
	public Popups updatePopup(Popups popup) {
		String sql = "UPDATE popups SET displayName = ?, popupText = ?, startTime = ?, endTime = ?, filename = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, popup.getDisplayName());
			statement.setString(2, popup.getPopupText());
			statement.setInt(3, popup.getStartTime());
			statement.setInt(4, popup.getEndTime());
			statement.setString(5, popup.getFilename());
			statement.setInt(6, popup.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update popup failed for id: " + popup.getId() + ", object: " + popup.toString());
		return this.getPopupById(popup.getId());
	}
	
	
	public Popups updatePopupWithUploadedFile(String newFilename, Integer bucketId, String extension, int popupId){
		String sql = "UPDATE popups SET bucketId = ?, filenameInBucket = ?, extension = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, bucketId);
			statement.setString(2, newFilename);
			statement.setString(3, extension);
			statement.setInt(4, popupId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("updatePopupWithUploadedFile failed, i: " + i);
		return this.getPopupById(popupId);
	}


	
	public int deletePopup(int popupId) {
		String sql = "DELETE FROM popups WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, popupId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	
	
	public int deleteCaption(int captionId) {
		String sql = "DELETE FROM captions WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, captionId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	
	
	// Caption editor bucket stuff start
	
	
	public String saveStringAsFileForCaptionEditor(String data, String extension) {// working 10/3/13
		File f = null;
		Writer writer = null;
		String filename = null;
		try {
			f = File.createTempFile(this.randomAlphaNumericOfLength(15), extension);
			f.deleteOnExit();
			writer = new OutputStreamWriter(new FileOutputStream(f));
			writer.write(data);
			writer.close();
			filename = this.saveFileToS3ForCaptionEditor(f, extension, 0, 0);
		} catch (IOException e) {
			throw new RuntimeException("failed saveStringAsFileForCaptionEditor, error: " + e.getLocalizedMessage());
		}finally{
			if(f != null)
				f.delete();
		}
		if(filename == null)
			throw new RuntimeException("failed to save data");
		return filename;
	}
	
	
	public BucketsCE getCaptionEditorBucket(){
		String sql = "SELECT * FROM bucketsCE";
		ArrayList<BucketsCE> results = new ArrayList<BucketsCE>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = BucketsCE.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() > 1)
			throw new RuntimeException("There is more than one bucket one bucket, count: " + results.size());
		else if(results.size() == 1)
			return results.get(0);
		else
			return null;
	}
	
	private AmazonS3 getAmazonS3CaptionEditor(){
		AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);
		return s3;
	}
	
	private String addFileToCaptionEditorBucket(String bucketName, String fileName, File file){
		AmazonS3 s3 = this.getAmazonS3CaptionEditor();
		PutObjectRequest por = new PutObjectRequest(bucketName, fileName, file);
		por.setCannedAcl(CannedAccessControlList.PublicRead);
		int seconds = 60*60*24;
		if(por.getMetadata() == null)
			por.setMetadata(new ObjectMetadata());
		if(por.getMetadata() == null)
			throw new RuntimeException("metadata is null");
		por.getMetadata().setCacheControl("max-age=" + seconds);
		s3.putObject(por);
		return fileName;
	}
	
	private File getFileFromBucketCaptionEditor(String fileName, String bucketName, String extension, String realFilename){
		AmazonS3 s3 = this.getAmazonS3CaptionEditor();
		S3Object object = s3.getObject(new GetObjectRequest(bucketName, fileName));
		File tempFile = null;
		try{
			tempFile = File.createTempFile(realFilename, extension);
			tempFile.deleteOnExit();
			FileOutputStream out = new FileOutputStream(tempFile);
			IOUtils.copy(object.getObjectContent(), out);
		}catch(IOException e){
			throw new RuntimeException("error copying inputstream from s3 to temporary file");
		}
		return tempFile;
	}
	
	private String createS3BucketCaptionEditor(String bucketName){
		AmazonS3 s3 = this.getAmazonS3CaptionEditor();
		Bucket newBucket = s3.createBucket(bucketName);
		return newBucket.getName();
	}
	
	
	public String saveFileToS3ForCaptionEditor(File file, String extension, int mediaId, int popupId){
		if(file == null)
			throw new RuntimeException("file trying to save to s3 is null");
		BucketsCE captionEditorBucket = this.getCaptionEditorBucket();
		if(captionEditorBucket == null){
			this.makeBucketForCaptionEditor();
			captionEditorBucket = this.getCaptionEditorBucket();
		}
		if(captionEditorBucket.getName() == null){
			throw new RuntimeException("caption editor bucket name is null");
		}
		String newFilename = this.addFileToCaptionEditorBucket(captionEditorBucket.getName(), this.randomAlphaNumericOfLength(15), file);
		if(mediaId != 0)
			this.updateMediaForFileUpload(newFilename, captionEditorBucket.getId(), extension, mediaId);
		else if(popupId != 0)
			this.updatePopupWithUploadedFile(newFilename, captionEditorBucket.getId(), extension, popupId);
		else
			throw new RuntimeException("File that was just uploaded had 0 for popupId and mediaId, needs one of them");
		return newFilename;
	}


	private Media updateMediaForFileUpload(String filenameInBucket, Integer bucketId, String extension, int mediaId) {
		String sql = "UPDATE media SET filenameInBucket = ?, bucketId = ?, extension = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, filenameInBucket);
			statement.setInt(2, bucketId);
			statement.setString(3, extension);
			statement.setInt(4, mediaId);
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update media for file upload failed for id: " + mediaId + ", extension: " + extension + ", bucketId: " + bucketId + ", filenameInBucket: " + filenameInBucket);
		return this.getMediaById(mediaId);
	}

	private String makeBucketForCaptionEditor(){
		String bucketName = "captioneditor-uuid-" + UUID.randomUUID();
		bucketName = this.createS3BucketCaptionEditor(bucketName);
		int i = 0;
		String sql = "INSERT INTO bucketsCE (name) VALUES (?)";
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, bucketName);
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i != 1)
			throw new RuntimeException("failed to make bucket for caption editor, returned: " + i);
		return bucketName;
	}


	
	public File getFileForMediaId(int mediaId) {
		Media media = this.getMediaById(mediaId);
		return this.getFileFromBucketCaptionEditor(media.getFilenameInBucket(), this.getCaptionEditorBucket().getName(), media.getExtension(), media.getFilename());
	}


	


	


	


	


	


	


	


	


	


	


	


	


	
}










































