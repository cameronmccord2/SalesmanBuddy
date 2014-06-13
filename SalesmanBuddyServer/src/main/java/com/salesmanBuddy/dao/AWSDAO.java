package com.salesmanBuddy.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.io.IOUtils;

import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;

public class AWSDAO extends BaseDAO {
	
	public AWSDAO(){
		super();
	}

	protected AmazonS3 getAmazonS3(Regions regionToUse){
		AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
		Region region = Region.getRegion(regionToUse);
		s3.setRegion(region);
		return s3;
	}
	
	protected String addFileToBucket(String bucketName, String fileName, File file){
		AmazonS3 s3 = this.getAmazonS3(Regions.US_WEST_2);
		s3.putObject(new PutObjectRequest(bucketName, fileName, file));
		return fileName;
	}
	
	protected File getFileFromBucket(String fileName, String bucketName, String extension, String realFilename, Regions region){
		System.out.println("Downloading an object");
		AmazonS3 s3 = this.getAmazonS3(Regions.US_WEST_2);
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
	
	protected String createS3Bucket(String bucketName, Regions region){
		AmazonS3 s3 = this.getAmazonS3(region);
		Bucket newBucket = s3.createBucket(bucketName);
		return newBucket.getName();
	}
}
