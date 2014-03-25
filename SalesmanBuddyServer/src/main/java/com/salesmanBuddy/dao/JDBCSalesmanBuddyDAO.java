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
	
	private static final String GoogleClientIdWeb = "38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com";
	private static final String GoogleClientSecretWeb = "NRheOilfAEKqTatHltqNhV2y";
	private static final String GoogleClientIdAndroid = "";
	private static final String GoogleClientSecretAndroid = "";
	private static final String GoogleClientIdiOS = "38235450166-dgbh1m7aaab7kopia2upsdj314odp8fc.apps.googleusercontent.com";
	private static final String GoogleClientSecretiOS = "zC738ZbMHopT2C1cyKiKDBQ6";
	private static final String GoogleTokenEndpoint = "https://accounts.google.com/o/oauth2/token";
	private static final String GoogleUserEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";
	private static final String GoogleRefreshTokenEndpoint = "https://accounts.google.com/o/oauth2/token";
	private static final String EMAIL_USER_NAME = "cameronmccord@salesmanbuddy.com";  // GMail user name (just the part before "@gmail.com")
    private static final String EMAIL_PASSWORD = "27&M2rk4$k"; // GMail password
    private static final String THIS_SERVER_URL = "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/";
    private static final String REPORTS_ENDPOINT = "reports";
    private static final Integer HOUR_TO_RUN_REPORTS = new DateTime().getHourOfDay();
    private static final String SUPPORT_EMAIL = "support@salesmanbuddy.com";
    private static final String TEST_DRIVE_NOW_EMAIL = "billing@salesmanbuddy.com";
	
	private SecureRandom random = new SecureRandom();
	
	public JDBCSalesmanBuddyDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
		EmailSender.initEmailSender(EMAIL_USER_NAME, EMAIL_PASSWORD);
//		SBScheduler.startSchedulerWithTimeOfDay(HOUR_TO_RUN_REPORTS, new DateTime().getMinuteOfDay() + 1, 0, THIS_SERVER_URL + REPORTS_ENDPOINT);
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
//		JDBCSalesmanBuddyDAO.sendErrorToMe("saved license: " + this.getLicenseListElementForLicenseId(licenseId));
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
	
	
	public GoogleRefreshTokenResponse codeForToken(String code, String redirectUri, String state) {
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
		JDBCSalesmanBuddyDAO.sendErrorToMe("Got refresh token again for this: " + grtr.toString());
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
	//        connection.setRequestProperty("Accept", "application/json");
	         
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
	
	
	public GoogleRefreshTokenResponse getValidTokenForUser(String googleUserId, Users user) {
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
        if(grtr.isInError())
        	throw new RuntimeException("the GoogleRefreshTokenResponse is in error, message: " + grtr.getErrorMessage() + ", body: " + new String(body));
        // TODO put token in database for caching?
        
        return grtr; 
	}
	
	public static void sendErrorToMe(String errorString){
		ArrayList<String> to = new ArrayList<String>();
		to.add("cameronmccord2@gmail.com");
		EmailSender.sendEmail(SBEmail.newPlainTextEmail("logging@salesmanbuddy.com", to, "error", errorString, true));
	}

	
	
	
	public UsersName getUsersName(String googleUserId) {
		GoogleUserInfo gui = this.getGoogleUserInfoWithId(googleUserId);
		if(gui.isInError())
			throw new RuntimeException("error getGoogleUserInfo, message: " + gui.getErrorMessage());

		UsersName name = new UsersName();
		name.setName(gui.getName());
		return name;
	}
	
	
	public GoogleUserInfo getGoogleUserInfoWithId(String googleUserId){
		GoogleRefreshTokenResponse grtr = this.getValidTokenForUser(googleUserId, null);
		return this.getGoogleUserInfo(grtr.getTokenType(), grtr.getAccessToken());
	}
	
	
	public GoogleUserInfo getGoogleUserInfo(String tokenType, String accessToken) {
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
	        
		} catch (MalformedURLException e) {
			throw new RuntimeException("malformedUrlException: " + e.getLocalizedMessage());
		} catch (IOException e) {
			throw new RuntimeException("IOException: " + e.getLocalizedMessage() + ", tokenType:" + tokenType + ", accessToken: " + accessToken + ", auth:" + whatItHas);
		}catch(JSONException jse){
			throw new RuntimeException("JSONException: " + jse.getLocalizedMessage());
		}
		GoogleUserInfo gui = new GoogleUserInfo(json);
		return gui;
	}
	
	
	// Email sending stuff
	
	public final static Integer ON_TEST_DRIVE_EMAIL_TYPE = 1;
	public final static Integer DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 2;
	public final static Integer DAILY_SALESMAN_SUMMARY_EMAIL_TYPE = 3;
	public final static Integer DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 4;
	public final static Integer WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 5;
	public final static Integer WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE = 6;
	public final static Integer WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 7;
//	public final static Integer BI_MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 8;
//	public final static Integer BI_MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE = 9;
//	public final static Integer BI_MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 10;
	public final static Integer MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE = 11;
	public final static Integer MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE = 12;
	public final static Integer MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE = 13;
	
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
	
	// about who - to who - how often(daily, weekly, bi-monthly, monthly) - about what(dealership, salesman, test drives)
	
	public void sendSummaryEmailsForType(Integer type, Integer dealershipId){
		if(type == DAILY_TEST_DRIVE_SUMMARY_EMAIL_TYPE)
			this.sendDailyDealershipEmail(dealershipId);
		else if(type == DAILY_SALESMAN_SUMMARY_EMAIL_TYPE)
			this.sendDailySalesmanEmail(dealershipId);
		else if(type == DAILY_DEALERSHIP_SUMMARY_EMAIL_TYPE)
			this.sendDailyDealershipEmail(dealershipId);
		else if(type == WEEKLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE)
			this.sendWeeklyTestDriveEmail(dealershipId);
		else if(type == WEEKLY_SALESMAN_SUMMARY_EMAIL_TYPE)
			this.sendWeeklySalesmanEmail(dealershipId);
		else if(type == WEEKLY_DEALERSHIP_SUMMARY_EMAIL_TYPE)
			this.sendWeeklyDealershipEmail(dealershipId);
//		else if(type == BI_MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE)
//			this.sendBiMonthlyTestDriveEmail(dealershipId);
//		else if(type == BI_MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE)
//			this.sendBiMonthlySalesmanEmail(dealershipId);
//		else if(type == BI_MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE)
//			this.sendBiMonthlyDealershipEmail(dealershipId);
		else if(type == MONTHLY_TEST_DRIVE_SUMMARY_EMAIL_TYPE)
			this.sendMonthlyTestDriveEmail(dealershipId);
		else if(type == MONTHLY_SALESMAN_SUMMARY_EMAIL_TYPE)
			this.sendMonthlySalesmanEmail(dealershipId);
		else if(type == MONTHLY_DEALERSHIP_SUMMARY_EMAIL_TYPE)
			this.sendMonthlyDealershipEmail(dealershipId);
		else
			throw new RuntimeException("Invalid type found for sendSummaryEmailsForType: " + type + ", dealershipId: " + dealershipId);
	}
	
	public void sendSummaryEmailsForOftenAboutDealershipId(Integer oftenType, Integer aboutType, Integer dealershipId){// called by the scheduler
		if(aboutType == DEALERSHIP_TYPE)
			this.sendDealershipSummaryEmail(oftenType, dealershipId);
		else if(aboutType == SALESMAN_TYPE)
			this.sendSalesmanSummaryEmail(oftenType, dealershipId);
		else if(aboutType == TEST_DRIVE_TYPE)
			this.sendTestDriveSummaryEmail(oftenType, dealershipId);
		else
			throw new RuntimeException("invalid aboutType found: " + aboutType);
	}
	
	public void sendDealershipSummaryEmail(Integer type, Integer dealershipId){
		if(type == DAILY_TYPE){
			this.sendDailyDealershipEmail(dealershipId);
		}else if(type == WEEKLY_TYPE){
			this.sendWeeklyDealershipEmail(dealershipId);
//		}else if(type == BI_MONTHLY_TYPE){
//			this.sendBiMonthlyDealershipEmail(dealershipId);
		}else if(type == MONTHLY_TYPE){
			this.sendMonthlyDealershipEmail(dealershipId);
		}else
			throw new RuntimeException("send dealership summary email type not found: " + type);
	}
	
	public void sendSalesmanSummaryEmail(Integer type, Integer dealershipId){
		if(type == DAILY_TYPE){
			this.sendDailySalesmanEmail(dealershipId);
		}else if(type == WEEKLY_TYPE){
			this.sendWeeklySalesmanEmail(dealershipId);
//		}else if(type == BI_MONTHLY_TYPE){
//			this.sendBiMonthlySalesmanEmail(dealershipId);
		}else if(type == MONTHLY_TYPE){
			this.sendMonthlySalesmanEmail(dealershipId);
		}else
			throw new RuntimeException("send dealership summary email type not found: " + type);
	}
	
	public void sendTestDriveSummaryEmail(Integer type, Integer dealershipId){
		if(type == DAILY_TYPE){
			this.sendDailyTestDriveEmail(dealershipId);
		}else if(type == WEEKLY_TYPE){
			this.sendWeeklyTestDriveEmail(dealershipId);
//		}else if(type == BI_MONTHLY_TYPE){
//			this.sendBiMonthlyTestDriveEmail(dealershipId);
		}else if(type == MONTHLY_TYPE){
			this.sendMonthlyTestDriveEmail(dealershipId);
		}else
			throw new RuntimeException("send dealership summary email type not found: " + type);
	}
	
	public void runReportsForType(Integer type){
//		JDBCSalesmanBuddyDAO.sendErrorToMe("Got a reports hit at " + new DateTime().toString());
		if(type == DAILY_TYPE){
			this.runDailyReports();
		}else if(type == WEEKLY_TYPE){
			this.runWeeklyReports();
//		}else if(type == BI_MONTHLY_TYPE){
			// dont care right now
		}else if(type == MONTHLY_TYPE){
			this.runMonthlyReports();
		}else
			throw new RuntimeException("run reports type not found: " + type);
	}
	
	private void runDailyReports() {
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		for(Dealerships d : dealerships){
			this.sendDailyDealershipEmail(d.getId());
			this.sendDailySalesmanEmail(d.getId());
			this.sendDailyTestDriveEmail(d.getId());
		}
	}

	private void runWeeklyReports() {
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		for(Dealerships d : dealerships){
			this.sendWeeklyDealershipEmail(d.getId());
			this.sendWeeklySalesmanEmail(d.getId());
			this.sendWeeklyTestDriveEmail(d.getId());
		}
	}

	private void runMonthlyReports() {
		ArrayList<Dealerships> dealerships = this.getAllDealerships();
		for(Dealerships d : dealerships){
			this.sendMonthlyDealershipEmail(d.getId());
			this.sendMonthlySalesmanEmail(d.getId());
			this.sendMonthlyTestDriveEmail(d.getId());
		}
	}

	private void sendDailyDealershipEmail(Integer dealershipId){
		DateTime to = new DateTime(DateTimeZone.UTC);
		DateTime from = to.minusDays(1).minusMinutes(1);
		ArrayList<Licenses> licenses = this.getLicensesForDateRangeDealershipId(from, to, dealershipId);
		String licensesMessage = this.createLicensesSummaryForLicenses(licenses, from, to, dealershipId, DEALERSHIP_TYPE);
		String salesmenMessage = this.createSalesmenSummaryForLicenses(licenses, from, to, dealershipId, DEALERSHIP_TYPE);
		String finalMessage = this.createFinalMessageForDealership(licensesMessage, salesmenMessage, dealershipId, from, to);
		JDBCSalesmanBuddyDAO.sendErrorToMe(finalMessage);
	}

	private String createFinalMessageForDealership(String licensesMessage, String salesmenMessage, Integer dealershipId, DateTime from, DateTime to) {
		StringBuilder sb = new StringBuilder();
		sb.append("Here is the summary email for ");
		sb.append(this.getDealershipById(dealershipId).getName());
		sb.append(" between ");
		sb.append(from.toString());
		sb.append(" and ");
		sb.append(to.toString());
		sb.append(".\n\nTest drives taken during this period:\n");
		sb.append(licensesMessage);
		sb.append("\n\nSalesmen Summary:\n");
		sb.append(salesmenMessage);
		sb.append("\n\nThank you for using Salesman Buddy. If you have any questions, contact us at ");
		sb.append(SUPPORT_EMAIL);
		return sb.toString();
	}


	private String createSalesmenSummaryForLicenses(ArrayList<Licenses> licenses, DateTime from, DateTime to, Integer dealershipId, Integer dealershipType) {
		StringBuilder sb = new StringBuilder();
		sb.append("salesman summary goes here");
		return sb.toString();
	}


	private String createLicensesSummaryForLicenses(ArrayList<Licenses> licenses, DateTime from, DateTime to, Integer dealershipId, Integer dealershipType) {
		StringBuilder sb = new StringBuilder();
		if(licenses.size() > 0){
			for(Licenses l : licenses){
				sb.append(l.getReportString());
			}
		}else{
			sb.append("Your dealership had no test drives recorded during this time period");
		}
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


	private void sendWeeklyDealershipEmail(Integer dealershipId){
		
	}

	private void sendMonthlyDealershipEmail(Integer dealershipId){
		
	}
	
	private void sendDailyTestDriveEmail(Integer dealershipId){
		// gather test drives for today
	}

	private void sendWeeklyTestDriveEmail(Integer dealershipId){
		// gather test drives for this past week
	}

	private void sendMonthlyTestDriveEmail(Integer dealershipId){
		
	}
	
	private void sendDailySalesmanEmail(Integer dealershipId){
		
	}

	private void sendWeeklySalesmanEmail(Integer dealershipId){
		
	}

	private void sendMonthlySalesmanEmail(Integer dealershipId){
		
	}
	
	public void sendEmailsAboutTestDriveForGoogleUserIdLicenseId(String googleUserId, Integer licenseId){
		ArrayList<UserTree> userTrees = this.getAllUserTreesForGoogleUserIdType(googleUserId, ON_TEST_DRIVE_EMAIL_TYPE);
		ArrayList<String> supervisorEmails = this.getSupervisorEmailsFromUserTrees(userTrees);
		String subject = this.createNowTestDriveSubjectForLicenseId(licenseId);
		String message = this.createNowTestDriveMessageForLicenseId(licenseId);
		SBEmail email = SBEmail.newPlainTextEmail(TEST_DRIVE_NOW_EMAIL, supervisorEmails, subject, message, true);
		EmailSender.sendEmail(email);
	}
	
	private String createNowTestDriveMessageForLicenseId(Integer licenseId) {
		StringBuilder sb = new StringBuilder();
		LicensesListElement lle = this.getLicenseListElementForLicenseId(licenseId);
		Licenses license = this.getLicenseForLicenseId(licenseId);
		Users user = this.getUserById(license.getUserId());
		UsersName un = this.getUsersName(user.getGoogleUserId());
		String stockNumber = LicensesListElement.getStockNumberForLicensesListElement(lle);
		
		sb.append("A test drive just occurred with ");
		if(un.isInError())
			sb.append("<Error getting name: ").append(un.getErrorMessage()).append(">");
		else
			sb.append(un.getName());
		sb.append(" on vehicle ").append(stockNumber).append(".\n");
		sb.append(this.getStatsAboutStockNumber(stockNumber, user.getDealershipId()));
		sb.append(this.getStatsAboutUserId(user.getId()));
		sb.append("\nIf you have any questions about Salesman Buddy, email us at ").append(SUPPORT_EMAIL).append(".");
		return sb.toString();
	}


	private String getStatsAboutUserId(Integer userId) {
		// TODO Finish this
		return "<Put stats about this salesman here>\n";
	}


	private String getStatsAboutStockNumber(String stockNumber, Integer dealershipId) {
		// TODO Finish this
		return "<Put stats about this vehicle here>\n";
	}


	private String createNowTestDriveSubjectForLicenseId(Integer licenseId) {
		// TODO make this better
		return "Test drive subject for licenseId: " + licenseId;
	}

	private ArrayList<String> getSupervisorEmailsFromUserTrees(ArrayList<UserTree> userTrees) {
		ArrayList<String> ids = this.getUserTreeGoogleIdsForType(userTrees, SUPERVISOR_TREE_TYPE);
		return this.getEmailsForGoogleIds(ids);
	}

	private ArrayList<String> getUserTreeGoogleIdsForType(ArrayList<UserTree> userTrees, Integer type) {
		ArrayList<String> ids = new ArrayList<String>();
		for(UserTree u : userTrees){
			if(type == JDBCSalesmanBuddyDAO.SUPERVISOR_TREE_TYPE) 
				ids.add(u.getSupervisorId());
			else
				ids.add(u.getUserId());
		}
		return ids;
	}


	// User Tree stuff
	
	public int newUserTreeNode(String googleUserId, String supervisorId, Integer type){
		String sql = "INSERT INTO userTree (userId, supervisorId, type) VALUES(?, ?, ?)";
		int i = 0;
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
	
	public ArrayList<String> getEmailsForGoogleIds(ArrayList<String> googleIds){
		Integer unverifiedEmails = 0;
		HashSet<String> recipients = new HashSet<String>();
		for(String id : googleIds){
			GoogleUserInfo gui = this.getGoogleUserInfoWithId(id);
			if(gui.isInError()){
				// TODO, figure out an error for this
				JDBCSalesmanBuddyDAO.sendErrorToMe("gui was in error for getAllRecipientsForUserIdLicenseScan, googleUserId: " + id + ", message:" + gui.getErrorMessage());
			}else{
				if(gui.isVerifiedEmail()){
					recipients.add(gui.getEmail());
				}else{
					unverifiedEmails++;
				}
			}
		}
		if(unverifiedEmails != 0)
			JDBCSalesmanBuddyDAO.sendErrorToMe("found " + unverifiedEmails + " unverified emails");
		return new ArrayList<String>(recipients);
	}
	
	public ArrayList<String> getAllRecipientEmailsForGoogleUserIdType(String googleUserId, Integer type){
		ArrayList<UserTree> nodes = this.getAllUserTreeForGoogleUserId(googleUserId);
		Integer unverifiedEmails = 0;
		HashSet<String> recipients = new HashSet<String>();
		for(UserTree n : nodes){
			GoogleUserInfo gui = this.getGoogleUserInfoWithId(n.getSupervisorId());
			if(gui.isInError()){
				// TODO, figure out an error for this
				JDBCSalesmanBuddyDAO.sendErrorToMe("gui was in error for getAllRecipientsForUserIdLicenseScan, googleUserId: " + googleUserId + ", message:" + gui.getErrorMessage());
			}else{
				if(gui.isVerifiedEmail()){
					recipients.add(gui.getEmail());
				}else{
					unverifiedEmails++;
				}
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
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
			statement.setInt(5, media.getId());
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update media failed for id: " + media.getId() + ", object: " + media.toString());
		return this.getMediaById(media.getId());
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










































