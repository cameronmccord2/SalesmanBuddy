package com.salesmanBuddy.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.Date;
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

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.ListObjectsRequest;
import com.amazonaws.services.s3.model.ObjectListing;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.PutObjectResult;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.google.common.io.Files;
import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.Buckets;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.ImageDetails;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestions;
import com.salesmanBuddy.model.StateQuestionsResponses;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.StateQuestionsWithResponses;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.Users;
import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.QuestionsAndAnswers;



public class JDBCSalesmanBuddyDAO implements SalesmanBuddyDAO{
//	static Logger log = Logger.getLogger("log.dao");
//	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	static final private int isImage = 1;
	static final private int isText = 2;
	static final private int isBool = 3;
	static final private int isDropdown = 4;
	
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

	
	@SuppressWarnings("finally")
	private Buckets getBucketForStateId(int stateId){
		String sql = "SELECT * FROM buckets WHERE stateId = ?";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			resultSet = statement.executeQuery();
			results = Buckets.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() > 1)
							throw new RuntimeException("There is more than one bucket for state: " + stateId);
						if(results.size() == 1)
							return results.get(0);
						return null;
					}
				}
			}
		}
	}
	
	private AmazonS3 getAmazonS3(){
		AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
		s3.setRegion(usWest2);
		return s3;
	}
	
	private String addFileToBucket(String bucketName, String fileName, File file){
		AmazonS3 s3 = this.getAmazonS3();
		System.out.println("Uploading a new object to S3 from a file\n");
        s3.putObject(new PutObjectRequest(bucketName, fileName, file));
        return fileName;
	}
	
	@SuppressWarnings("finally")
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
        }finally{
        	return tempFile;
        }
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

	

	@SuppressWarnings("finally")
	private String makeBucketForStateId(int stateId){
		String bucketName = "state-" + this.getStateNameForStateId(stateId).toLowerCase() + "-uuid-" + UUID.randomUUID();
		System.out.println("sent: " + bucketName);
		bucketName = this.createS3Bucket(bucketName);
		System.out.println("got back: " + bucketName);
		
		String sql = "INSERT INTO buckets (stateId, name) VALUES (?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			statement.setString(2, bucketName);
			statement.execute();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return bucketName;
				}
			}
		}
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<States> getAllStates(int getInactiveToo) {// working 10/3/13
		String sql = "SELECT * FROM states WHERE status = 1";
		if(getInactiveToo > 0)
			sql = "SELECT * FROM states";
		ArrayList<States> states = new ArrayList<States>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			states = States.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						return states;
					}
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<Dealerships> getAllDealerships() {// working 10/3/13
		String sql = "SELECT * FROM dealerships";
		ArrayList<Dealerships> results = new ArrayList<Dealerships>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			results = Dealerships.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						return results;
					}
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<LicensesListElement> getAllLicensesForUserId(String googleUserId) {
		String sql = "SELECT * FROM licenses WHERE userId = (SELECT id FROM users WHERE googleUserId = ?) AND showInUserList = 1 ORDER BY created desc";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, googleUserId);
			resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			for(int i = 0; i < results.size(); i++){
				results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId()));
			}
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						return results;
					}
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private LicensesListElement getLicenseListElementForLicenseId(int id) {
		String sql = "SELECT * FROM licenses WHERE id = ?";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			for(int i = 0; i < results.size(); i++){
				results.get(i).setQaas(this.getQuestionsAndAnswersForLicenseId(results.get(i).getId()));
			}
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() == 1)
							return results.get(0);
						throw new RuntimeException("couldnt find the license by id: " + id);
					}
				}
			}
		}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(f != null)
				f.delete();
		}
		if(fp == null || fp.getFilename() == null)
			throw new RuntimeException("failed to save data");
		return fp;
	}	
	
	@SuppressWarnings("finally")
	private int putLicenseInDatabase(Licenses license){
		String sql = "INSERT INTO licenses (photo, bucketId, longitude, latitude, userId, stateIs) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int id = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setFloat(3, license.getLongitude());
			statement.setFloat(4, license.getLatitude());
			statement.setInt(5, license.getUserId());
			statement.setInt(6, license.getStateId());
			statement.execute();
			id = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return id;
				}
			}
		}
	}
	
	private int parseFirstInt(ResultSet generatedKeys, String key) {
		try {
			while(generatedKeys.next())
				return (int) generatedKeys.getLong(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			throw new RuntimeException("couldnt find user for google id");
		licenseFromClient.setUserId(user.getId());
		if(licenseFromClient.getUserId() == 0)
			throw new RuntimeException("userid is 0, its invalid");

		Licenses l = this.convertLicenseFromClientToLicense(licenseFromClient);
		licenseId = this.putLicenseInDatabase(l);
		if(licenseId == 0)
			throw new RuntimeException("failed to put license in database, licenseid returned: " + licenseId);
		
		for(QuestionsAndAnswers qaa : licenseFromClient.getQaas()){
			if(this.putAnswerInDatabase(qaa.getAnswer()) == 0)
				throw new RuntimeException("Failed to insert answer into database");
		}
		
		ArrayList<LicensesListElement> licenses = this.getAllLicensesForUserId(googleUserId);
		for(LicensesListElement lic : licenses){
			if(lic.getId() == licenseId)
				return lic;
		}
		throw new RuntimeException("couldnt find the license that was just inserted");
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
	
	@SuppressWarnings("finally")
	private int updateShowInUserListForLicenseId(int licenseId, int showInUserList){
		if(!(showInUserList == 1 || showInUserList == 0))
			throw new RuntimeException("updateShowInUserListForLicenseId failed because showInUserList was not 0 or 1");
		String sql = "UPDATE licenses SET showInUserList = ? WHERE id = ?";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, showInUserList);
			statement.setInt(2, licenseId);
			i = statement.executeUpdate();
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public boolean userOwnsLicenseId(int licenseId, String googleUserId) {
		String sql = "SELECT * FROM licenses WHERE id = ? AND userId = (SELECT id FROM users WHERE googleUserId = ?)";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			statement.setString(2, googleUserId);
			resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					if(results.size() > 0)
						return true;
					return false;
				}
			}
		}
	}


	@SuppressWarnings("finally")
	private Buckets getBucketForBucketId(Integer bucketId) {
		String sql = "SELECT * FROM buckets WHERE id = ?";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, bucketId);
			resultSet = statement.executeQuery();
			results = Buckets.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() != 1)
							throw new RuntimeException("expected number of buckets for bucket id to be 1, id: " + bucketId);
						return results.get(0);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	@Override
	public Licenses getLicenseForLicenseId(int licenseId) {
		String sql = "SELECT * FROM licenses WHERE id = ?";
		ArrayList<Licenses> results = new ArrayList<Licenses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = Licenses.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() > 0)
							return results.get(0);
						throw new RuntimeException("licenseId does not match any in the database");
					}
				}
			}
		}
	}


	@Override
	public File getLicenseImageForPhotoNameBucketId(String photoName,Integer bucketId) {
		Buckets bucket = this.getBucketForBucketId(bucketId);
		return this.getFileFromBucket(photoName, bucket.getName());
	}


	@SuppressWarnings("finally")
	@Override
	public Users getUserByGoogleId(String googleUserId) {
		String sql = "SELECT * FROM users WHERE googleUserId = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setString(1, googleUserId);
			resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() > 0)
							return results.get(0);
						return null;
					}
				}
			}
		}
	}


	@SuppressWarnings("finally")
	@Override
	public int createUser(Users user) {
		String sql = "INSERT INTO users (dealershipId, deviceType, type, googleUserId) VALUES(?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		int id = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, this.getAllDealerships().get(0).getId());// TODO fix this
			statement.setInt(2, user.getDeviceType());
			statement.setInt(3, 1);
			statement.setString(4, user.getGoogleUserId());
			i = statement.executeUpdate();
			if(i != 0)
				id = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					if(id == 0)
						throw new RuntimeException("failed inserting user");
					return id;
				}
			}
		}
	}


	@SuppressWarnings("finally")
	@Override
	public Users getUserById(int userId) {
		String sql = "SELECT * FROM users WHERE id = ?";
		ArrayList<Users> results = new ArrayList<Users>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			resultSet = statement.executeQuery();
			results = Users.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() > 0)
							return results.get(0);
						return null;
					}
				}
			}
		}
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
	public ArrayList<QuestionsAndAnswers> getQuestionsAndAnswersForLicenseId(int licenseId) {
		ArrayList<Answers> answers = this.getAnswersForLicenseId(licenseId);
		ArrayList<QuestionsAndAnswers> qas = new ArrayList<QuestionsAndAnswers>();
		for(Answers a : answers){
			QuestionsAndAnswers qa = new QuestionsAndAnswers();
			qa.setAnswer(a);
			qa.setQuestion(this.getQuestionById(a.getQuestionId()));
			qas.add(qa);
		}
		return qas;
	}
	
	@SuppressWarnings("finally")
	@Override
	public ArrayList<Answers> getAnswersForLicenseId(int licenseId) {
		String sql = "SELECT * FROM answers WHERE licenseId = ?";
		ArrayList<Answers> results = new ArrayList<Answers>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = Answers.parseResultSet(resultSet);
			for(Answers a : results){
				if(a.getAnswerType() == JDBCSalesmanBuddyDAO.isImage)
					a.setImageDetails(this.getImageDetailsForAnswerId(a.getId()));
			}
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						return results;
					}
				}
			}
		}
	}


	@SuppressWarnings("finally")
	private ImageDetails getImageDetailsForAnswerId(Integer answerId) {
		String sql = "SELECT * FROM imageDetails WHERE answerId = ?";
		ArrayList<ImageDetails> results = new ArrayList<ImageDetails>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		Connection connection = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, answerId);
			resultSet = statement.executeQuery();
			results = ImageDetails.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					try{
						if(connection != null)
							connection.close();
					}catch(SQLException sqle){
						throw new RuntimeException(sqle);
					}finally{
						if(results.size() == 1)
							return results.get(0);
						throw new RuntimeException("couldnt find imageDetails for answer id: " + answerId);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private int updateAnswerInDatabase(Answers answer) {
		String sql = "UPDATE answers SET answerBool = ?, answerType = ?, answerText = ?, licenseId = ?, questionId = ? WHERE id = ?";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, answer.getAnswerBool());
			statement.setInt(2, answer.getAnswerType());
			statement.setString(3, answer.getAnswerText());
			statement.setInt(4, answer.getLicenseId());
			statement.setInt(5, answer.getQuestionId());
			statement.setInt(6, answer.getId());
			i = statement.executeUpdate();
			if(i == 0)
				throw new RuntimeException("update answers failed for id: " + answer.getId());
			if(answer.getAnswerType() == JDBCSalesmanBuddyDAO.isImage)
				this.updateImageDetailsInDatabase(answer.getImageDetails());
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private int updateImageDetailsInDatabase(ImageDetails imageDetails) {
		String sql = "UPDATE imageDetails SET photoName = ?, bucketId = ? WHERE id = ?";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, imageDetails.getPhotoName());
			statement.setInt(2, imageDetails.getBucketId());
			statement.setInt(3, imageDetails.getId());
			i = statement.executeUpdate();
			if(i == 0)
				throw new RuntimeException("update imageDetails failed for id: " + imageDetails.getId());
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}


	@SuppressWarnings("finally")
	private int updateQuestionInDatabase(Questions q){
		String sql = "UPDATE questions SET version = ?, questionOrder = ?, questionTextEnglish = ?, questionTextSpanish = ?, required = ?, questionType = ? WHERE id = ?";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
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
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}
	
	
	@SuppressWarnings("finally")
	private int putQuestionInDatabase(Questions q){
		String sql = "INSERT INTO questions (version, questionOrder, questionTextEnglish, questionTextSpanish, required, questionType) VALUES (?, ?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
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
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}
	
	@SuppressWarnings("finally")
	private int putAnswerInDatabase(Answers answer) {
		String sql = "INSERT INTO answers (answerText, answerBool, licenseId, questionId, answerType) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			if(answer.getAnswerText() == null)
				answer.setAnswerText("");
			statement.setString(1, answer.getAnswerText());
			statement.setInt(2, answer.getAnswerBool());
			statement.setInt(3, answer.getLicenseId());
			statement.setInt(4, answer.getQuestionId());
			statement.setInt(5, answer.getAnswerType());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
			if(i == 0)
				throw new RuntimeException("failed to insert question into database");
			if(this.putImageDetailsInDatabase(answer.getImageDetails()) == 0)
				throw new RuntimeException("failed to insert image details into database");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
	}


	@SuppressWarnings("finally")
	@Override
	public Questions getQuestionById(Integer questionId) {
		String sql = "SELECT * FROM questions WHERE id = ?";
		ArrayList<Questions> results = new ArrayList<Questions>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, questionId);
			resultSet = statement.executeQuery();
			results = Questions.parseResultSet(resultSet);
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(resultSet != null)
					resultSet.close();
			}catch(SQLException e){
				throw new RuntimeException(e);
			}finally{
				try{
					if(statement != null)
						statement.close();
				}catch(SQLException se){
					throw new RuntimeException(se);
				}finally{
					if(results.size() == 1)
						return results.get(0);
					return null;
				}
			}
		}
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
	
	@SuppressWarnings("finally")
	private int putImageDetailsInDatabase(ImageDetails imageDetails){
		String sql = "INSERT INTO imageDetails (photoName, bucketId, answerId) VALUES (?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, imageDetails.getPhotoName());
			statement.setInt(2, imageDetails.getBucketId());
			statement.setInt(3, imageDetails.getAnswerId());
			statement.execute();
			i = this.parseFirstInt(statement.getGeneratedKeys(), "id");
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}finally{
			try{
				if(statement != null)
					statement.close();
			}catch(SQLException se){
				throw new RuntimeException(se);
			}finally{
				try{
					if(connection != null)
						connection.close();
				}catch(SQLException sqle){
					throw new RuntimeException(sqle);
				}finally{
					return i;
				}
			}
		}
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
}












































