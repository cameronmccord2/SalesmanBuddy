package com.salesmanBuddy.dao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.xml.bind.DatatypeConverter;

import org.apache.commons.io.IOUtils;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.salesmanBuddy.model.BucketsCE;
import com.salesmanBuddy.model.Captions;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.MaxValue;
import com.salesmanBuddy.model.Media;
import com.salesmanBuddy.model.MediaForApp;
import com.salesmanBuddy.model.Popups;
import com.salesmanBuddy.model.SubPopups;

public class CaptionEditorDAO extends SharedDAO {
	
	public CaptionEditorDAO(){
		super();
	}

	public List<Captions> putCaptions(List<Captions> captions) {
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
		final String sql = "SELECT MAX(version) AS maxValue FROM captions WHERE mediaId = ? AND languageId = ?";
		Integer maxValue = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			
			ResultSet resultSet = statement.executeQuery();
			maxValue = MaxValue.parseResultSetForMaxValue(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return maxValue;
	}

	private int putCaption(Captions caption){
		final String sql = "INSERT INTO captions (version, caption, mediaId, startTime, endTime, type, languageId) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert captions failed, i == 0");
		return i;
	}

	public List<Captions> getAllCaptionsForMediaIdLanguageId(int mediaId, int languageId) {
		Integer latestVersion = this.getLatestCaptionVersionForMediaIdLanguageId(mediaId, languageId);
		
		final String sql = "SELECT * FROM captions WHERE mediaId = ? AND languageId = ? AND version = ? ORDER BY startTime";
		List<Captions> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);
			statement.setInt(2, languageId);
			statement.setInt(3, latestVersion);
			
			ResultSet resultSet = statement.executeQuery();
			results = Captions.parseResultSet(resultSet);
			resultSet.close();
			
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
		final String sql = "UPDATE media SET name = ?, filename = ?, type = ?, audioLanguageId = ? WHERE id = ?";
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
	}
	
	public int deleteMediaById(int mediaId) {
		this.deletePopupsWithMediaId(mediaId);
		this.deleteCaptionsWithMediaId(mediaId);
		final String sql = "DELETE FROM media WHERE id = ?";
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
		final String sql = "DELETE FROM captions WHERE mediaId = ?";
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
		final String sql = "DELETE FROM popups WHERE mediaId = ?";
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
		final String sql = "INSERT INTO media (name, filename, type, audioLanguageId) VALUES (?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, media.getName());
			statement.setString(2, media.getFilename());
			statement.setInt(3, media.getType());
			statement.setInt(4, media.getAudioLanguageId());
			statement.execute();
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
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
		
		String filenameInBucket = this.saveFileToS3ForCaptionEditor(file, extension, media.getId(), 0, 0);// file from this is usable everywhere else, works in chrome
		file.delete();
		Media newMedia = this.updateMediaForFileUpload(filenameInBucket, this.getCaptionEditorBucket().getId(), extension, media.getId());
		return newMedia;
	}

	public Media getMediaById(int id) {
		final String sql = "SELECT * FROM media WHERE id = ?";
		List<Media> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, id);
			
			ResultSet resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle.getLocalizedMessage() + ", mediaId: " + id);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt find media by id: " + id + ", result set size was: " + results.size());
	}

	public List<Media> getAllMedia() {
		final String sql = "SELECT * FROM media";
		List<Media> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			ResultSet resultSet = statement.executeQuery();
			results = Media.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public List<Languages> putLanguages(List<Languages> languages) {
		for(Languages l : languages){
			this.putLanguage(l);
		}
		return this.getAllLanguages(0);
	}
	
	private int putLanguage(Languages language){
		final String sql = "INSERT INTO languages (mtcId, code1, code2, name, mtcTaught, alternateName, nativeName) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert language failed, i == 0, object: " + language.toString());
		return i;
	}

	public List<Popups> getAllPopups() {
		final String sql = "SELECT * FROM popups ORDER BY startTime";
		List<Popups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public List<Popups> getAllPopupsForLanguageId(int languageId) {
		final String sql = "SELECT * FROM popups WHERE languageId = ? ORDER BY startTime";
		List<Popups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, languageId);

			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public List<Popups> getAllPopupsForMediaId(int mediaId) {
		final String sql = "SELECT * FROM popups WHERE mediaId = ? ORDER BY startTime";
		List<Popups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, mediaId);

			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
	
	public List<Popups> getPopupsForMediaIdLanguageId(int languageId, int mediaId) {
		final String sql = "SELECT * FROM popups WHERE languageId = ? AND mediaId = ? ORDER BY startTime";
		List<Popups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, languageId);
			statement.setInt(2, mediaId);
			
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		BucketsCE b = this.getCaptionEditorBucket();
		for(Popups p : results){
			p.setBucketName(b.getName());
			p.setSubPopups(this.getAllSubPopupsForPopupId(p.getId()));
		}
		return results;
	}

	public Popups newPopup(Popups popup) {
		final String sql = "INSERT INTO popups (displayName, popupText, mediaId, languageId, startTime, endTime, filename) VALUES (?, ?, ?, ?, ?, ?, ?)";
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
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert media failed, i == 0");
		return this.getPopupById(i);
	}
	
	public List<Popups> putPopups(List<Popups> popups) {
		List<Popups> newList = new ArrayList<>();
		for(Popups popup : popups){
			Popups p = null;
			if(popup.getId() == 0){
				p = this.newPopup(popup);
				newList.add(p);
				
			}else{
				p = this.updatePopup(popup);
				newList.add(p);
			}
			for (SubPopups sub : popup.getSubPopups()) {// read popups off of original list
				SubPopups newSub = null;
				if(sub.getId() == 0){
					sub.setPopupId(p.getId());
					newSub = this.newSubPopup(sub);
				}else{
					newSub = this.updateSubPopup(sub);
				}
				if(p.getSubPopups() == null)
					p.setSubPopups(new ArrayList<SubPopups>());
				p.getSubPopups().add(newSub);
			}
		}
		return newList;
	}

	private Popups getPopupById(int popupId) {
		final String sql = "SELECT * FROM popups WHERE id = ?";
		List<Popups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, popupId);
			
			ResultSet resultSet = statement.executeQuery();
			results = Popups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt get popup by id: " + popupId);
	}
	
	public Popups updatePopup(Popups popup) {
		final String sql = "UPDATE popups SET displayName = ?, popupText = ?, startTime = ?, endTime = ?, filename = ? WHERE id = ?";
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
		final String sql = "UPDATE popups SET bucketId = ?, filenameInBucket = ?, extension = ? WHERE id = ?";
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
		final String sql = "DELETE FROM popups WHERE id = ?";
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
		final String sql = "DELETE FROM captions WHERE id = ?";
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
			filename = this.saveFileToS3ForCaptionEditor(f, extension, 0, 0, 0);
			
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
		final String sql = "SELECT * FROM bucketsCE";
		List<BucketsCE> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = BucketsCE.parseResultSet(resultSet);
			resultSet.close();
			
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
	
//	private AmazonS3 getAmazonS3CaptionEditor(){
//		AmazonS3 s3 = new AmazonS3Client(new ClasspathPropertiesFileCredentialsProvider());
//		Region usWest2 = Region.getRegion(Regions.US_WEST_2);
//		s3.setRegion(usWest2);
//		return s3;
//	}
	
	private String addFileToCaptionEditorBucket(String bucketName, String fileName, File file){
		AmazonS3 s3 = this.getAmazonS3(Regions.US_WEST_2);
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
	
	public String saveFileToS3ForCaptionEditor(File file, String extension, Integer mediaId, Integer popupId, Integer subPopupId){
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
		else if(subPopupId != 0)
			this.updateSubPopupWithUploadedFile(newFilename, captionEditorBucket.getId(), extension, subPopupId);
		else
			throw new RuntimeException("File that was just uploaded had 0 for popupId and mediaId, needs one of them");
		return newFilename;
	}
	
	public Media updateMediaName(int mediaId, String name) {
		final String sql = "UPDATE media SET name = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, name);
			statement.setInt(2, mediaId);
			
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("update media for new name failed for id: " + mediaId + ", name: " + name);
		return this.getMediaById(mediaId);
	}

	private Media updateMediaForFileUpload(String filenameInBucket, Integer bucketId, String extension, int mediaId) {
		final String sql = "UPDATE media SET filenameInBucket = ?, bucketId = ?, extension = ? WHERE id = ?";
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
		bucketName = this.createS3Bucket(bucketName, Regions.US_WEST_2);
		int i = 0;
		final String sql = "INSERT INTO bucketsCE (name) VALUES (?)";
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			statement.setString(1, bucketName);
			statement.execute();
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i != 1)
			throw new RuntimeException("failed to make bucket for caption editor, returned: " + i);
		return bucketName;
	}

	public File getFileForMediaId(int mediaId) {
		Media media = this.getMediaById(mediaId);
		return this.getFileFromBucket(media.getFilenameInBucket(), this.getCaptionEditorBucket().getName(), media.getExtension(), media.getFilename(), Regions.US_WEST_2);
	}

	public List<MediaForApp> getMediasForAppV1() {
		final String sql = "SELECT * FROM media WHERE id IN (1062, 1064, 1063, 1049, 1048)";
		List<MediaForApp> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = MediaForApp.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		BucketsCE b = this.getCaptionEditorBucket();
		List<Languages> ls = this.getAllLanguages(0);
		for(MediaForApp m : results){
			m.setBucketName(b.getName());
			m.setCaptions(this.getAllCaptionsForMediaIdLanguageId(m.getId(), m.getAudioLanguageId()));
			m.setPopups(this.getPopupsForMediaIdLanguageId(m.getAudioLanguageId(), m.getId()));
			for(Languages l : ls){
				m.setLanguage(l);
				if(l.getId().equals(m.getAudioLanguageId())){
					m.setLanguage(l);
					break;
				}
			}
		}
		return results;
	}
	
	public List<MediaForApp> getMediasForAppV2() {
		final String sql = "SELECT * FROM media ORDER BY name";
		List<MediaForApp> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = MediaForApp.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		BucketsCE b = this.getCaptionEditorBucket();
		List<Languages> ls = this.getAllLanguages(0);
		List<MediaForApp> finalResults = new ArrayList<MediaForApp>();
		for(MediaForApp m : results){
			m.setBucketName(b.getName());
			m.setCaptions(this.getAllCaptionsForMediaIdLanguageId(m.getId(), m.getAudioLanguageId()));
			m.setPopups(this.getPopupsForMediaIdLanguageId(m.getAudioLanguageId(), m.getId()));
			for(Languages l : ls){
				m.setLanguage(l);
				if(l.getId().equals(m.getAudioLanguageId())){
					m.setLanguage(l);
					break;
				}
			}
			if(!(m.getCaptions().size() == 0 && m.getPopups().size() == 0))
				finalResults.add(m);
		}
		return finalResults;
	}

	public List<SubPopups> getAllSubPopups() {
		final String sql = "SELECT * FROM subpopups ORDER BY startTime";
		List<SubPopups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = SubPopups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public List<SubPopups> getAllSubPopupsForPopupId(Integer popupId) {
		final String sql = "SELECT * FROM subpopups WHERE popupId = ? ORDER BY startTime";
		List<SubPopups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, popupId);

			ResultSet resultSet = statement.executeQuery();
			results = SubPopups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}

	public List<SubPopups> putSubPopups(List<SubPopups> subPopups) {
		List<SubPopups> newList = new ArrayList<>();
		for(SubPopups subPopup : subPopups){
			if(subPopup.getId() == 0)
				newList.add(this.newSubPopup(subPopup));
			else
				newList.add(this.updateSubPopup(subPopup));
		}
		return newList;
	}
	
	public SubPopups updateSubPopup(SubPopups subPopup) {
		final String sql = "UPDATE SubPopups SET popupText = ?, startTime = ?, endTime = ?, filename = ?, assetPosition = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setString(1, subPopup.getPopupText());
			statement.setInt(2, subPopup.getStartTime());
			statement.setInt(3, subPopup.getEndTime());
			statement.setString(4, subPopup.getFilename());
			statement.setInt(5, subPopup.getAssetPosition());
			statement.setInt(6, subPopup.getId());
			
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle.getLocalizedMessage() + ", object: " + subPopup.toString());
		}
		if(i == 0)
			throw new RuntimeException("update SubPopups failed for id: " + subPopup.getId() + ", object: " + subPopup.toString());
		return this.getSubPopupById(subPopup.getId());
	}

	public int deleteSubPopup(int subPopupId) {
		final String sql = "DELETE FROM subPopups WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, subPopupId);
			
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	
	public SubPopups newSubPopup(SubPopups subPopup) {
		final String sql = "INSERT INTO subPopups (popupText, popupId, startTime, endTime, filename, assetPosition) VALUES (?, ?, ?, ?, ?, ?)";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)){
			statement.setString(1, subPopup.getPopupText());
			statement.setInt(2, subPopup.getPopupId());
			statement.setInt(3, subPopup.getStartTime());
			statement.setInt(4, subPopup.getEndTime());
			statement.setString(5, subPopup.getFilename());
			statement.setInt(6, subPopup.getAssetPosition());
			statement.execute();
			
			ResultSet resultSet = statement.getGeneratedKeys();
			i = this.parseFirstInt(resultSet, "id");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		if(i == 0)
			throw new RuntimeException("insert SubPopup failed, i == 0");
		return this.getSubPopupById(i);
	}
	
	private SubPopups getSubPopupById(int subPopupId) {
		final String sql = "SELECT * FROM subPopups WHERE id = ?";
		List<SubPopups> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, subPopupId);
			
			ResultSet resultSet = statement.executeQuery();
			results = SubPopups.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle.getLocalizedMessage() + ", id: " + subPopupId);
		}
		if(results.size() == 1)
			return results.get(0);
		throw new RuntimeException("couldnt get subPopup by id: " + subPopupId);
	}

	public SubPopups updateSubPopupWithUploadedFile(String newFilename, Integer bucketId, String extension, Integer subPopupId){
		final String sql = "UPDATE subPopups SET bucketId = ?, filenameInBucket = ?, extension = ? WHERE id = ?";
		int i = 0;
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			statement.setInt(1, bucketId);
			statement.setString(2, newFilename);
			statement.setString(3, extension);
			statement.setInt(4, subPopupId);
			
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle.getLocalizedMessage());
		}
		if(i == 0)
			throw new RuntimeException("updateSubPopupWithUploadedFile failed, i: " + i);
		return this.getSubPopupById(subPopupId);
	}
}















































