package com.salesmanBuddy.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.Buckets;
import com.salesmanBuddy.model.BucketsCE;
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
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
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.QuestionsAndAnswers;
import com.salesmanBuddy.model.UsersName;



public class JDBCSalesmanBuddyDAO implements SalesmanBuddyDAO{
//	static Logger log = Logger.getLogger("log.dao");
//	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	static final private int isImage = 1;
	static final private int isText = 2;
	static final private int isBool = 3;
	static final private int isDropdown = 4;
	
	private static String GoogleClientIdWeb = "185872110398-icdle47mq6dtff0ktdpc7qrpojkh5jrj.apps.googleusercontent.com";
	private static String GoogleClientSecretWeb = "BWJTZ4AGamoJ4rmPnIHPs2Ak";
	private static String GoogleClientIdAndroid = "";
	private static String GoogleClientSecretAndroid = "";
	private static String GoogleClientIdiOS = "38235450166-dgbh1m7aaab7kopia2upsdj314odp8fc.apps.googleusercontent.com";
	private static String GoogleClientSecretiOS = "zC738ZbMHopT2C1cyKiKDBQ6";
	private static String GoogleTokenEnpoint = "https://accounts.google.com/o/oauth2/token";
	private static String GoogleUserEndpoint = "https://www.googleapis.com/oauth2/v1/userinfo?alt=json";
	
	private SecureRandom random = new SecureRandom();
	
	public JDBCSalesmanBuddyDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
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
	
	@Override
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
	
	@Override
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
		ArrayList<States> states = this.getAllStates(1);
		for(States state : states){
			if(state.getId().intValue() == stateId)
				return state.getName();
		}
		throw new RuntimeException("could not find state for id: " + stateId);
	}

	@Override
	public String getString() {
		return "From the dao";
	}

	@Override
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

	@Override
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

	@Override
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

	@Override
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


	@Override
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
				throw new RuntimeException("Failed to insert answer into database");
		}
		
		return this.getLicenseListElementForLicenseId(licenseId);
	}

	@Override
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

	@Override
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
	
	@Override
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


	@Override
	public File getLicenseImageForPhotoNameBucketId(String photoName,Integer bucketId) {
		Buckets bucket = this.getBucketForBucketId(bucketId);
		return this.getFileFromBucket(photoName, bucket.getName());
	}

	@Override
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
	
	@Override
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


	@Override
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


	@Override
	public LicensesListElement updateLicense(LicensesFromClient licenseFromClient, String googleUserId) {
		if(licenseFromClient.getId() == null || licenseFromClient.getId() == 0)
			throw new RuntimeException("id is either null or 0: " + licenseFromClient.toString());
		this.updateShowInUserListForLicenseId(licenseFromClient.getId(), licenseFromClient.getShowInUserList());
		for(QuestionsAndAnswers qaa : licenseFromClient.getQaas()){
			this.updateAnswerInDatabase(qaa.getAnswer());
		}
		return this.getLicenseListElementForLicenseId(licenseFromClient.getId());
	}


	@Override
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
	
	@Override
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
			throw new RuntimeException("failed to insert question into database");
		if(answer.getImageDetails() != null){
			answer.getImageDetails().setAnswerId(i);
			if(this.putImageDetailsInDatabase(answer.getImageDetails()) == 0)
				throw new RuntimeException("failed to insert image details into database");
		}
		return i;
	}


	@Override
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
	
	@Override
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


	@Override
	public boolean userOwnsQuestionId(int questionId, String googleUserId) {
		// TODO Auto-generated method stub
		return true;
	}


	@Override
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
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert image failed");
		return i;
	}


	@Override
	public Questions putQuestion(Questions question) {
		this.putQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}


	@Override
	public Questions updateQuestion(Questions question) {
		this.updateQuestionInDatabase(question);
		return this.getQuestionById(question.getId());
	}
	
	
	
	@Override
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


	@Override
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


	@Override
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


	@Override
	public Users updateUserToDealershipCode(String googleUserId, String dealershipCode) {
		int dealershipId = this.getDealershipByDealershipCode(dealershipCode).getId();
		String sql = "UPDATE users SET dealershipId = ? WHERE googleUserId = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, dealershipCode);
			statement.setInt(2, dealershipId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
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


	@Override
	public List<LicensesListElement> getAllLicensesForDealershipForUserId(String googleUserId) {
		int dealershipId = this.getUserByGoogleId(googleUserId).getDealershipId();
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
	
	
	
	
	private Dealerships getDealershipById(Integer dealershipId) {
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
	
	@Override
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


	@Override
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
	
	@Override
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
	
	@Override
	public GoogleRefreshTokenResponse getValidTokenForUser(String googleUserId) {
		Users user = this.getUserByGoogleId(googleUserId);
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
			url = new URL(GoogleTokenEnpoint);
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
			throw new RuntimeException("IOException: " + e.getLocalizedMessage());
		}catch(JSONException jse){
			throw new RuntimeException("JSONException: " + jse.getLocalizedMessage());
		}
		
        GoogleRefreshTokenResponse grtr = new GoogleRefreshTokenResponse(json);
        if(grtr.isInError())
        	throw new RuntimeException("the GoogleRefreshTokenResponse is in error, message: " + grtr.getErrorMessage() + ", body: " + new String(body));
        // TODO put token in database for caching?
        
        return grtr;
	}
	
	@Override
	public UsersName getUsersName(String googleUserId) {
		GoogleUserInfo gui = this.getGoogleUserInfoWithId(googleUserId);
		if(gui.isInError())
			throw new RuntimeException("error getGoogleUserInfo, message: " + gui.getErrorMessage());

		UsersName name = new UsersName();
		name.setName(gui.getName());
		return name;
	}
	
	@Override
	public GoogleUserInfo getGoogleUserInfoWithId(String googleUserId){
		GoogleRefreshTokenResponse grtr = this.getValidTokenForUser(googleUserId);
		return this.getGoogleUserInfo(grtr.getTokenType(), grtr.getAccessToken());
	}
	
	@Override
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
	        body = IOUtils.toByteArray(conn.getInputStream());// dying here
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
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

//	trainer stuff


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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


	@Override
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

	@Override
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
	
	@Override
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

	@Override
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
	
	@Override
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


	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
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
	
	@Override
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


	@Override
	public File getFileForMediaId(int mediaId) {
		Media media = this.getMediaById(mediaId);
		return this.getFileFromBucketCaptionEditor(media.getFilenameInBucket(), this.getCaptionEditorBucket().getName(), media.getExtension(), media.getFilename());
	}
}










































