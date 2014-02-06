package com.salesmanBuddy.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
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
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.DeleteLicenseResponse;
import com.salesmanBuddy.model.FinishedPhoto;
import com.salesmanBuddy.model.ImageDetails;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.MaxValue;
import com.salesmanBuddy.model.Media;
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
		String sql = "INSERT INTO licenses (longitude, latitude, userId, stateId) VALUES (?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int id = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setFloat(1, license.getLongitude());
			statement.setFloat(2, license.getLatitude());
			statement.setInt(3, license.getUserId());
			statement.setInt(4, license.getStateId());
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
		
//		ArrayList<LicensesListElement> licenses = this.getAllLicensesForUserId(googleUserId);
//		for(LicensesListElement lic : licenses){
//			if(lic.getId() == licenseId)// TODO change this to only get one
//				return lic;
//		}
		return this.getLicenseListElementForLicenseId(licenseId);
//		throw new RuntimeException("couldnt find the license that was just inserted");
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
			statement.setInt(1, 1);// TODO fix this to actually use dealerships
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
				if(a.getAnswerType() == JDBCSalesmanBuddyDAO.isImage) {
					a.setImageDetails(this.getImageDetailsForAnswerId(a.getId()));
				}
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
//							throw new RuntimeException("got image details for answerId: " + answerId + ", id: " + results.get(0).getId());
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
	
	/*
	 * CREATE TABLE answers (
    id                         int                     IDENTITY(1,1) NOT NULL PRIMARY KEY,
    answerText                 NVARCHAR(500)                         NOT NULL,
    answerBool                 BIT          default 0                NOT NULL,
    answerType                 int          default 0                NOT NULL,
    licenseId                  int                                   NOT NULL FOREIGN KEY REFERENCES licenses(id),
    questionId                 int                                   NOT NULL FOREIGN KEY REFERENCES questions(id),
    created                    DATETIME2    default SYSUTCDATETIME() NOT NULL
);
	 */
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
			if(answer.getImageDetails() != null){
				answer.getImageDetails().setAnswerId(i);
				if(this.putImageDetailsInDatabase(answer.getImageDetails()) == 0)
					throw new RuntimeException("failed to insert image details into database");
			}
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
	
	@SuppressWarnings("finally")
	@Override
	public ArrayList<Questions> getAllQuestions() {
		String sql = "SELECT * FROM questions ORDER BY questionOrder";
		ArrayList<Questions> results = new ArrayList<Questions>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
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
					return results;
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
					if(i == 0)
						throw new RuntimeException("insert image failed");
					throw new RuntimeException("inserted image");
//					return i;
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
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			resultSet = statement.executeQuery();
			maxValue = MaxValue.parseResultSetForMaxValue(resultSet);
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
				}
			}
		}
		return maxValue;
	}


	private int putCaption(Captions caption){
		String sql = "INSERT INTO captions (version, caption, mediaId, startTime, endTime, type, languageId) VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
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
					if(i == 0)
						throw new RuntimeException("insert captions failed, i == 0");
				}
			}
		}
		return i;
	}


	@Override
	public ArrayList<Captions> getAllCaptionsForMediaIdLanguageId(int mediaId, int languageId) {
		Integer latestVersion = this.getLatestCaptionVersionForMediaIdLanguageId(mediaId, languageId);
		
		String sql = "SELECT * FROM captions WHERE mediaId = ? AND languageId = ? AND version = ? ORDER BY startTime";
		ArrayList<Captions> results = new ArrayList<Captions>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			statement.setInt(3, latestVersion);
			resultSet = statement.executeQuery();
			results = Captions.parseResultSet(resultSet);
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
				}
			}
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
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		Media updatedMedia = null;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
			statement.setInt(5, media.getId());
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
					if(i == 0)
						throw new RuntimeException("update media failed for id: " + media.getId() + ", object: " + media.toString());
					else
						updatedMedia = this.getMediaById(media.getId());
				}
			}
		}
		return updatedMedia;
	}
	
	private Media putNewMedia(Media media){
		String sql = "INSERT INTO media (name, filename, type, audioLanguageId) VALUES (?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
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
					if(i == 0)
						throw new RuntimeException("insert media failed, i == 0");
				}
			}
		}
		return this.getMediaById(i);
	}

	@Override
	public Media getMediaById(int id) {
		String sql = "SELECT * FROM media WHERE id = ?";
		ArrayList<Media> results = new ArrayList<Media>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, id);
			resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
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
				}
			}
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt find media by id: " + id + ", result set size was: " + results.size());
	}


	@Override
	public ArrayList<Media> getAllMedia() {
		String sql = "SELECT * FROM media";
		ArrayList<Media> results = new ArrayList<Media>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);

			resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
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
				}
			}
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
		/*
		 * mtcId                    NVARCHAR(5)                            NOT NULL,
    code1                    NVARCHAR(5)                            NULL,
    code2                    NVARCHAR(5)                            NULL,
    name                     NVARCHAR(30)                           NOT NULL,
    mtcTaught                NUMERIC(2) default 0                   NOT NULL,
    alternateName            NVARCHAR(30)                           NULL,
    nativeName               NVARCHAR(30)                           NULL
		 */
		
		String sql = "INSERT INTO languages (mtcId, code1, code2, name, mtcTaught, alternateName, nativeName) VALUES (?, ?, ?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		Connection connection = null;
		int i = 0;
		try{
			connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);
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
					if(i == 0)
						throw new RuntimeException("insert language failed, i == 0, object: " + language.toString());
				}
			}
		}
		return i;
	}


	@Override
	public ArrayList<Languages> getAllLanguages(int onlyMtcTaught) {
		String sql = "SELECT * FROM languages";
		if(onlyMtcTaught == 1)
			sql = "SELECT * FROM languages WHERE mtcTaught = 1";
		ArrayList<Languages> results = new ArrayList<Languages>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);

			resultSet = statement.executeQuery();
			results = Languages.parseResultSet(resultSet);
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
				}
			}
		}
		return results;
	}
}












































