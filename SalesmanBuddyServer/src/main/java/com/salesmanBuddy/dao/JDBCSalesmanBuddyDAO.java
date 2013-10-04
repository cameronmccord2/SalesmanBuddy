package com.salesmanBuddy.dao;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.nio.file.StandardCopyOption;
import java.security.SecureRandom;
import java.sql.Array;
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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestions;
import com.salesmanBuddy.model.StateQuestionsResponses;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.StateQuestionsWithResponses;
import com.salesmanBuddy.model.States;

public class JDBCSalesmanBuddyDAO implements SalesmanBuddyDAO{
	// TODO do i need to close all of the connections too?
	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	private SecureRandom random = new SecureRandom();
	
	private enum Enums{
		
	}
	
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
		try{
			Connection connection = dataSource.getConnection();
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
					if(results.size() > 1)
						throw new RuntimeException("There is more than one bucket for state: " + stateId);
					if(results.size() == 1)
						return results.get(0);
					return null;
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
	
	private String saveFileToS3ForStateId(int stateId, File file){
		if(file == null)
			throw new RuntimeException("file trying to save to s3 is null");
		Buckets stateBucket = this.getBucketForStateId(stateId);
		if(stateBucket == null){
			stateBucket = this.makeBucketForStateId(stateId);
		}
		if(stateBucket == null){
			throw new RuntimeException("state bucket is null");
		}
		if(stateBucket.getName() == null){
			throw new RuntimeException("statebucket name is null");
		}
		return this.addFileToBucket(stateBucket.getName(), this.randomAlphaNumericOfLength(15), file);
	}
	
	private String randomAlphaNumericOfLength(Integer length){
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
	private Buckets makeBucketForStateId(int stateId){
		String bucketName = "state-" + this.getStateNameForStateId(stateId).toLowerCase() + "-uuid-" + UUID.randomUUID();
		System.out.println("sent: " + bucketName);
		bucketName = this.createS3Bucket(bucketName);
		System.out.println("got back: " + bucketName);
		
		String sql = "INSERT INTO buckets (stateId, name) VALUES (?, ?)";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			statement.setString(2, bucketName);
			resultSet = statement.executeQuery();
			results = Buckets.parseResultSet(resultSet);// TODO this probably isnt right
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
					if(results.size() > 1)
						throw new RuntimeException("There is more than one bucket for state: " + stateId);
					if(results.size() == 1)
						return results.get(0);
					return null;
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
		try{
			Connection connection = dataSource.getConnection();
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
					return states;
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
		try{
			Connection connection = dataSource.getConnection();
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<LicensesListElement> getAllLicensesForUserId(int userId) {
		String sql = "SELECT * FROM licenses WHERE userId = ?";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			
			for(int i = 0; i < results.size(); i++){
				results.get(i).setStateQuestions(this.getStateQuestionsWithResponsesForLicenseId(results.get(i).getId()));
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
					return results;
				}
			}
		}
	}

	@Override
	public String saveStringAsFileForStateId(String data, int stateId, String extension) {// working 10/3/13
		File f = null;
		Writer writer = null;
		String photoFileName = null;
		try {
			f = File.createTempFile(this.randomAlphaNumericOfLength(15), extension);
			f.deleteOnExit();
			writer = new OutputStreamWriter(new FileOutputStream(f));
			writer.write(data);
			writer.close();
			photoFileName = this.saveFileToS3ForStateId(stateId, f);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(f != null)
				f.delete();
		}
		if(photoFileName == null)
			throw new RuntimeException("failed to save data");
		return photoFileName;
	}
	
	@SuppressWarnings("finally")
	private int putLicenseInDatabase(Licenses license){
		String sql = "INSERT INTO licenses (photo, bucketId, longitude, latitude, userId) VALUES (?, ?, ?, ?, ?)";
		PreparedStatement statement = null;
		int i = 0;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setString(1, license.getPhoto());
			statement.setInt(2, license.getBucketId());
			statement.setFloat(3, license.getLongitude());
			statement.setFloat(4, license.getLatitude());
			statement.setInt(5, license.getUserId());
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
				return i;
			}
		}
	}
	
	private int parseFirstInt(ResultSet generatedKeys, String key) {
		try {
			while(generatedKeys.next())
				return generatedKeys.getInt(key);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		throw new RuntimeException("the generated keys didnt contain an int");
	}
	
	private Licenses convertLicenseFromClientToLicense(LicensesFromClient lfc){
		Licenses l = new Licenses();
		l.setBucketId(this.getBucketForStateId(lfc.getStateId()).getId());
		l.setLatitude(lfc.getLatitude());
		l.setLongitude(lfc.getLongitude());
		l.setUserId(lfc.getUserId());
		l.setPhoto(lfc.getPhoto());
		return l;
	}


	@Override
	public ArrayList<LicensesListElement> putLicense(LicensesFromClient licenseFromClient) {
		String filename = this.saveStringAsFileForStateId(licenseFromClient.getPhoto(), licenseFromClient.getStateId(), ".jpeg");
		if(filename != null){
			Licenses l = this.convertLicenseFromClientToLicense(licenseFromClient);
			l.setPhoto(filename);
			this.putLicenseInDatabase(l);
			return this.getAllLicensesForUserId(licenseFromClient.getUserId());
		}else
			throw new RuntimeException("error saving image to s3");
	}

	@SuppressWarnings("finally")
	@Override
	public int deleteLicense(int licenseId) {
		String sql = "UPDATE licenses SET showInUserList = 0 WHERE id = ?";
		PreparedStatement statement = null;
		int i = 0;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql,Statement.RETURN_GENERATED_KEYS);
			statement.setInt(1, licenseId);
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
				return i;
			}
		}
	}

	@Override
	public boolean userOwnsLicenseId(int licenseId) {
		return true;
//		String sql = "SELECT * FROM licenses WHERE id = ?";
//		ArrayList<Licenses> results = new ArrayList<Licenses>();
//		PreparedStatement statement = null;
//		ResultSet resultSet = null;
//		try{
//			Connection connection = dataSource.getConnection();
//			statement = connection.prepareStatement(sql);
//			statement.setInt(1, licenseId);
//			resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}finally{
//			try{
//				if(resultSet != null)
//					resultSet.close();
//			}catch(SQLException e){
//				throw new RuntimeException(e);
//			}finally{
//				try{
//					if(statement != null)
//						statement.close();
//				}catch(SQLException se){
//					throw new RuntimeException(se);
//				}finally{
//					for(int i = 0; i < results.size(); i++){
////						if(results[i].userId == )
//					}
//					return results;
//				}
//			}
//		}
	}

	@SuppressWarnings("finally")
	@Override
	public List<StateQuestions> getStateQuestionsForStateId(int stateId) {
		String sql = "SELECT * FROM stateQuestions WHERE stateId = ?";
		List<StateQuestions> results = new ArrayList<StateQuestions>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			resultSet = statement.executeQuery();
			results = StateQuestions.parseResultSet(resultSet);
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsResponses> getStateQuestionsResponsesForLicenseId(int licenseId) {
		String sql = "SELECT * FROM stateQuestionsResponses WHERE licenseId = ?";
		ArrayList<StateQuestionsResponses> results = new ArrayList<StateQuestionsResponses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = StateQuestionsResponses.parseResultSet(resultSet);
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateQuestionId(int stateQuestionId) {
		String sql = "SELECT * FROM stateQuestionsSpecifics WHERE stateQuestionId = ?";
		ArrayList<StateQuestionsSpecifics> results = new ArrayList<StateQuestionsSpecifics>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateQuestionId);
			resultSet = statement.executeQuery();
			results = StateQuestionsSpecifics.parseResultSet(resultSet);
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsWithResponses> getStateQuestionsWithResponsesForLicenseId(int licenseId) {
		String sql = "SELECT * FROM (SELECT * FROM stateQuestionsResponses WHERE licenceId = ?) LEFT JOIN stateQuestionsSpecifics ON stateQuestionsResponses.stateQuestionsSpecificsId = stateQuestionsSpecifics.id";
		ArrayList<StateQuestionsWithResponses> results = new ArrayList<StateQuestionsWithResponses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = StateQuestionsWithResponses.parseResultSet(resultSet);
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateId(int stateId) {// working 10/3/13
		String sql = "SELECT * FROM stateQuestionsSpecifics WHERE stateQuestionId = (SELECT id FROM stateQuestions WHERE stateId = ?)";
		ArrayList<StateQuestionsSpecifics> results = new ArrayList<StateQuestionsSpecifics>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			resultSet = statement.executeQuery();
			results = StateQuestionsSpecifics.parseResultSet(resultSet);
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
	public File getLicenseImageForPhotoNameBucketId(String photoName,Integer bucketId) {
		Buckets bucket = this.getBucketForBucketId(bucketId);
		return this.getFileFromBucket(photoName, bucket.getName());
	}


	@SuppressWarnings("finally")
	private Buckets getBucketForBucketId(Integer bucketId) {
		String sql = "SELECT * FROM buckets WHERE id = ?";
		ArrayList<Buckets> results = new ArrayList<Buckets>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
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
					if(results.size() != 1)
						throw new RuntimeException("expected number of buckets for bucket id to be 1, id: " + bucketId);
					return results.get(0);
				}
			}
		}
	}


	@Override
	public File getLicenseImageForPhotoNameBucketName(String photoName,
			String bucketName) {
		// TODO Auto-generated method stub
		return null;
	}
}












































