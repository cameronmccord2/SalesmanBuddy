package com.salesmanBuddy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.joda.time.DateTime;

import com.salesmanBuddy.exceptions.NoBucketFoundException;
import com.salesmanBuddy.exceptions.NoSqlResultsException;
import com.salesmanBuddy.model.Answers;
import com.salesmanBuddy.model.Buckets;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.ErrorMessage;
import com.salesmanBuddy.model.GoogleRefreshTokenResponse;
import com.salesmanBuddy.model.ImageDetails;
import com.salesmanBuddy.model.Languages;
import com.salesmanBuddy.model.Licenses;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.Questions;
import com.salesmanBuddy.model.States;
import com.salesmanBuddy.model.StockNumbers;
import com.salesmanBuddy.model.UserTree;
import com.salesmanBuddy.model.Users;

public class SharedDAO extends AWSDAO {
	
	protected static final Integer QUESTION_STOCK_NUMBER = 2;
	protected final static Integer QUESTION_FIRST_NAME_TAG = 3;
	protected final static Integer QUESTION_LAST_NAME_TAG = 4;
	
	public SharedDAO(){
		super();
	}
	
	public List<Languages> getLanguages(int onlyMtcTaught) {
		String sql = "SELECT * FROM languages";
		if(onlyMtcTaught == 1)
			sql = "SELECT * FROM languages WHERE mtcTaught = 1";
		return this.getList(sql, Languages.class);
	}
	
	protected Buckets getBucketForStateId(int stateId) throws NoBucketFoundException{
		final String sql = "SELECT * FROM buckets WHERE stateId = ?";
		List<Buckets> results = this.getList(sql, Buckets.class, stateId);
		if(results.size() > 1)
			throw new RuntimeException("There is more than one bucket for state: " + stateId);
		if(results.size() == 1)
			return results.get(0);
		throw new NoBucketFoundException("No bucket found for stateId: " + stateId);
	}
	
	protected String getStateNameForStateId(int stateId) {
		States state = this.getStateForId(stateId);
		if(state == null)
			throw new RuntimeException("could not find state for id: " + stateId);
		return state.getName();
	}

	public List<States> getStates(int getInactiveToo) {
		String sql = "SELECT * FROM states WHERE status = 1";
		if(getInactiveToo > 0)
			sql = "SELECT * FROM states";
		return this.getList(sql, States.class);
	}
	
	public States getStateForId(Integer stateId) {
		try {
			return this.getRow("SELECT * FROM states WHERE id = ?", States.class, stateId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("StateId: " + stateId + ", error: " + e.getLocalizedMessage());
		}
	}

	public List<Dealerships> getAllDealerships() {
		return this.getList("SELECT * FROM dealerships", Dealerships.class);
	}
	
	public Dealerships getDealershipWithDealershipCode(String dealershipCode) {
		try {
			return this.getRow("SELECT * FROM dealerships WHERE dealershipCode = ?", Dealerships.class, dealershipCode);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("dealershipCode: " + dealershipCode + ", error: " + e);
		}
	}

	public List<LicensesListElement> getLicensesForGoogleUserId(String googleUserId) {
		final String sql = "SELECT * FROM licenses WHERE userId = (SELECT id FROM users WHERE googleUserId = ?) AND showInUserList = 1 ORDER BY created desc";
		return this.getList(sql, LicensesListElement.class, googleUserId);
	}
	
	public List<LicensesListElement> getAllLicenses() {
		return this.getList("SELECT * FROM licenses ORDER BY created DESC", LicensesListElement.class);
	}
	
	protected Integer putLicenseInDatabase(Licenses license) throws NoSqlResultsException{
		final String sql = "INSERT INTO licenses (longitude, latitude, userId, stateId) VALUES (?, ?, ?, ?)";
		return this.insertRow(sql, "id", license.getLongitude(), license.getLatitude(), license.getUserId(), license.getStateId());
	}
	
	protected Integer updateShowInUserListForLicenseId(int licenseId, int showInUserList){
		if(!(showInUserList == 1 || showInUserList == 0))
			throw new RuntimeException("updateShowInUserListForLicenseId failed because showInUserList was not 0 or 1");
		final String sql = "UPDATE licenses SET showInUserList = ? WHERE id = ?";
		return this.updateRow(sql, showInUserList, licenseId);
	}
	
	protected Buckets getBucketForBucketId(Integer bucketId) {
		try{
			return this.getRow("SELECT * FROM buckets WHERE id = ?", Buckets.class, bucketId);
		}catch(NoSqlResultsException e){
			throw new RuntimeException("Couldnt get bucket by id: " + bucketId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public Licenses getLicenseForLicenseId(int licenseId) {
		try{
			return this.getRow("SELECT * FROM licenses WHERE id = ?", Licenses.class, licenseId);
		}catch(NoSqlResultsException e){
			throw new RuntimeException("Couldnt get license by id: " + licenseId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public Users getUserByGoogleId(String googleUserId) {
		try {
			return this.getRow("SELECT * FROM users WHERE googleUserId = ?", Users.class, googleUserId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Couldnt get user by google id: " + googleUserId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public int createUser(Users user) {
		final String sql = "INSERT INTO users (deviceType, type, googleUserId, refreshToken) VALUES(?, ?, ?, ?)";
		try {
			return this.insertRow(sql, "id", user.getDeviceType(), 1, user.getGoogleUserId(), user.getRefreshToken());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed inserting user, user: " + user.toString());
		}
	}
	
	public Users getUserById(Integer userId) {
		try {
			return this.getRow("SELECT * FROM users WHERE id = ?", Users.class, userId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("cant find user by id: " + userId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public List<Answers> getAnswersForLicenseId(int licenseId) {
		return this.getList("SELECT * FROM answers WHERE licenseId = ?", Answers.class, licenseId);
	}

	protected ImageDetails getImageDetailsForAnswerId(Integer answerId) {
		try {
			return this.getRow("SELECT * FROM imageDetails WHERE answerId = ?", ImageDetails.class, answerId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant get image details for answer id: " + answerId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	protected Integer updateImageDetailsInDatabase(ImageDetails imageDetails) {
		final String sql = "UPDATE imageDetails SET photoName = ?, bucketId = ? WHERE id = ?";
		int i = this.updateRow(sql, imageDetails.getPhotoName(), imageDetails.getBucketId(), imageDetails.getId());
		if(i == 0)
			throw new RuntimeException("update imageDetails failed for id: " + imageDetails.getId());
		return i;
	}

	protected Integer updateQuestionInDatabase(Questions q){
		final String sql = "UPDATE questions SET version = ?, questionOrder = ?, questionTextEnglish = ?, questionTextSpanish = ?, required = ?, questionType = ? WHERE id = ?";
		return this.updateRow(sql, q.getVersion(), q.getQuestionOrder(), q.getQuestionTextEnglish(), q.getQuestionTextSpanish(), q.getRequired(), q.getQuestionType(), q.getId());
	}
	
	protected Integer putQuestionInDatabase(Questions q){
		final String sql = "INSERT INTO questions (version, questionOrder, questionTextEnglish, questionTextSpanish, required, questionType) VALUES (?, ?, ?, ?, ?, ?)";
		try {
			return this.insertRow(sql, "id", q.getVersion(), q.getQuestionOrder(), q.getQuestionTextEnglish(), q.getQuestionTextSpanish(), q.getRequired(), q.getQuestionType());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Insert question failed: " + q.toString() + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public Questions getQuestionById(Integer questionId) {
		try {
			return this.getRow("SELECT * FROM questions WHERE id = ?", Questions.class, questionId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant get question by id: " + questionId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public List<Questions> getAllQuestions() {
		return this.getList("SELECT * FROM questions ORDER BY questionOrder", Questions.class);
	}
	
	protected Integer putImageDetailsInDatabase(ImageDetails imageDetails){
		final String sql = "INSERT INTO imageDetails (photoName, bucketId, answerId) VALUES (?, ?, ?)";
		try {
			return this.insertRow(sql, "id", imageDetails.getPhotoName(), imageDetails.getBucketId(), imageDetails.getAnswerId());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert imageDetails failed, " + imageDetails.toString() + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public List<Users> getAllUsers() {
		return this.getList("SELECT * FROM users", Users.class);
	}

	public List<Users> getUsersForDealershipId(Integer dealershipId) {
		return this.getList("SELECT * FROM users WHERE dealershipId = ?", Users.class, dealershipId);
	}
	
	protected Dealerships getDealershipByDealershipCode(String dealershipCode) {
		try {
			return this.getRow("SELECT * FROM dealerships WHERE dealershipCode = ?", Dealerships.class, dealershipCode);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to get dealership by dealershipCode: " + dealershipCode + ", error: " + e.getLocalizedMessage());
		}
	}

	public List<LicensesListElement> getLicensesListElementForDealershipId(Integer dealershipId) {
		List<Users> users = this.getList("SELECT * FROM users WHERE dealershipId = ?", Users.class, dealershipId);
		List<LicensesListElement> licenses = new ArrayList<>();
		for(Users u : users){
			licenses.addAll(this.getLicensesForGoogleUserId(u.getGoogleUserId()));
		}
		return licenses;
	}
	
	public Dealerships getDealershipById(Integer dealershipId) {
		try {
			return this.getRow("SELECT * FROM dealerships WHERE id = ?", Dealerships.class, dealershipId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("failed to get dealership by dealershipId: " + dealershipId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public void updateRefreshTokenForUser(Users userFromClient) {
		if(userFromClient.getDeviceType() < 1 || userFromClient.getDeviceType() > 3)
			throw new RuntimeException("their device type is not within the range 1-3, user: " + userFromClient.toString());
		
		final String sql = "UPDATE users SET refreshToken = ?, deviceType = ? WHERE id = ?";
		int i = this.updateRow(sql, userFromClient.getRefreshToken(), userFromClient.getDeviceType(), userFromClient.getId());
		if(i == 0)
			throw new RuntimeException("failed to update user's refresh token, refreshToken length: " + userFromClient.getRefreshToken().length() + ", userFromClient: " + userFromClient.toString());
		return;
	}
	
	public int saveGoogleTokenInCache(GoogleRefreshTokenResponse grtr, Users user) {
		final String sql = "INSERT INTO tokens (userId, token, expiresAt, type) VALUES (?, ?, ?, ?)";
		DateTime expiresAt = new DateTime().plusSeconds((int)grtr.getExpiresIn());
		try {
			return this.insertRow(sql, "id", user.getId(), grtr.getTokenType() + " " + grtr.getAccessToken(), expiresAt.getMillis(), user.getType());
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert into tokens failed, sql: " + sql + ", expiresAt: " + expiresAt.getMillis() + ", grtr: " + grtr.toString() + ", user: " + user.toString() + ", error: " + e.getLocalizedMessage());
		}
	}
	
	protected List<Licenses> getLicensesWithStockNumber(String stockNumber) {
		final String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId ORDER BY l.created";
		return this.getList(sql, Licenses.class, stockNumber, QUESTION_STOCK_NUMBER);
	}
	
	protected List<Licenses> getLicensesWithStockNumberFromTo(String stockNumber, DateTime from, DateTime to) {
		final String sql = "SELECT l.* from answers a, questions q, licenses l WHERE a.answerText = ? AND q.tag = ? AND a.questionId = q.id AND l.id = a.licenseId AND l.created between ? and ? ORDER BY l.created";
		return this.getList(sql, Licenses.class, stockNumber, QUESTION_STOCK_NUMBER, from.toString(), to.toString());
	}
	
	protected List<Licenses> getLicensesForUserIdDateRange(Integer userId, DateTime to, DateTime from) {
		final String sql = "SELECT * FROM licenses WHERE userId = ? AND created BETWEEN ? AND ?";
		return this.getList(sql, Licenses.class, userId, from.toString(), to.toString());
	}
	
	protected List<Licenses> getLicensesForDealershipIdDateRange(Integer dealershipId, DateTime from, DateTime to) {
		final String sql = "SELECT * FROM licenses WHERE userId IN (SELECT id FROM users WHERE dealershipId = ?) AND created BETWEEN ? AND ?";
		return this.getList(sql, Licenses.class, dealershipId, from.toString(), to.toString());
	}
	
	public Integer insertUserTree(String googleUserId, String supervisorId, Integer type){
		final String sql = "INSERT INTO userTree (userId, supervisorId, type) VALUES(?, ?, ?)";
		try {
			return this.insertRow(sql, "id", googleUserId, supervisorId, type);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("insert userTree failed, sql: " + sql + ", googleUserId: " + googleUserId + ", supervisorId: " + supervisorId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	protected List<UserTree> getUserTreeForDealershipIdType(Integer dealershipId, Integer type) {
		String sql = "SELECT * FROM userTree ut WHERE type = ? AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ? AND (ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?))";
		return this.getList(sql, UserTree.class, type, dealershipId, dealershipId);
	}
	
	public UserTree getUserTreeById(Integer userTreeId){
		try {
			return this.getRow("SELECT * FROM userTree WHERE id = ?", UserTree.class, userTreeId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant find usertree by id: " + userTreeId + ", error: " + e.getLocalizedMessage());
		}
	}
	
	public List<UserTree> getUserTrees() {
		return this.getList("SELECT * FROM userTree ORDER BY userId", UserTree.class);
	}
	
	public List<UserTree> getUserTreesForGoogleUserId(String googleUserId){
		return this.getList("SELECT * FROM userTree WHERE userId = ?", UserTree.class, googleUserId);
	}

	public List<UserTree> getUserTreesForGoogleUserId(String googleUserId, Integer type){
		return this.getList("SELECT * FROM userTree WHERE userId = ? AND type = ?", UserTree.class, googleUserId, type);
	}
	
	public List<UserTree> getUserTreesForGoogleSupervisorId(String googleSupervisorId){
		return this.getList("SELECT * FROM userTree WHERE supervisorId = ?", UserTree.class, googleSupervisorId);
	}
	
	public List<UserTree> getUserTreesForDealershipId(Integer dealershipId) {
		final String sql = "SELECT * FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?);";
		return this.getList(sql, UserTree.class, dealershipId, dealershipId);
	}
	
	protected List<String> getUniqueStockNumbersForDealershipId(Integer dealershipId) {
		final String sql = "SELECT distinct a.answerText as stockNumber FROM answers a, licenses l, users u, questions q WHERE q.tag = ? AND len(a.answerText) > 0 AND a.questionId = q.id AND a.licenseId = l.id AND l.userId = u.id AND u.dealershipId = ?;";
		return this.getListOneColumn(sql, String.class, "stockNumber", QUESTION_STOCK_NUMBER, dealershipId);
	}
	
	public int updateUserTree(UserTree userTree){
		final String sql = "UPDATE userTree SET userId = ?, supervisorId = ?, type = ? WHERE id = ?";
		int i = this.updateRow(sql, userTree.getUserId(), userTree.getSupervisorId(), userTree.getType(), userTree.getId());
		if(i == 0)
			throw new RuntimeException("failed to update userTree: " + userTree.toString());
		return i;
	}
	
	public int deleteUserTreeById(Integer userTreeId){
		final String sql = "DELETE FROM userTree WHERE id = ?";
		int i = this.updateRow(sql, userTreeId);
		if(i == 0)
			throw new RuntimeException("failed to delete userTree, id: " + userTreeId);
		return i;
	}
	
	public int deleteUserTreesForGoogleSupervisorIdGoogleUserId(String googleSupervisorId, String googleUserId){
		final String sql = "DELETE FROM userTree WHERE supervisorId = ? OR userId = ?";
		int i = this.updateRow(sql, googleSupervisorId, googleUserId);
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreesForGoogleUserId(String googleUserId){
		final String sql = "DELETE FROM userTree WHERE userId = ?";
		int i = this.updateRow(sql, googleUserId);
		if(i == 0)
			throw new RuntimeException("failed to delete user's userTree for googleUserId: " + googleUserId);
		return i;
	}
	
	public int deleteUserTreesForSupervisorId(String supervisorId){
		final String sql = "DELETE FROM userTree WHERE supervisorId = ?";
		int i = this.updateRow(sql, supervisorId);
		if(i == 0)
			throw new RuntimeException("failed to delete supervisor's userTree for googleUserId: " + supervisorId);
		return i;
	}
	
	public ErrorMessage deleteUserTreesForDealershipId(Integer dealershipId) {
		final String sql = "DELETE FROM userTree ut WHERE ut.supervisorId IN (SELECT googleUserId FROM users WHERE dealershipId = ?) OR ut.userId IN (SELECT googleUserId FROM users WHERE dealershipId = ?)";
		int i = this.updateRow(sql, dealershipId, dealershipId);
		if(i == 0)
			throw new RuntimeException("failed to delete all userTree nodes for dealerhsipId: " + dealershipId);
		return new ErrorMessage("Not an error, successfully deleted " + i + " userTree nodes for dealerhsipId: " + dealershipId);
	}

	public ErrorMessage deleteAllUserTrees() {
		final String sql = "DELETE FROM userTree";
		int i = this.updateRow(sql);
		if(i == 0){
			i = this.getCount("SELECT count(*) count FROM userTree");
			if(i != 0)
				throw new RuntimeException("Unable to delete all userTree, found: " + i);
		}
		return new ErrorMessage("this isnt an error, successfully deleted all userTree nodes");
	}
	// TODO keep going from here
	protected StockNumbers getStockNumberByStockNumber(String stockNumber) throws NoSqlResultsException {
		return this.getRow("SELECT * FROM stockNumbers WHERE stockNumber = ?", StockNumbers.class, stockNumber);
	}
	
	public StockNumbers getStockNumberById(Integer stockNumberId) {
		try {
			return this.getRow("SELECT * FROM stockNumbers WHERE id = ?", StockNumbers.class, stockNumberId);
		} catch (NoSqlResultsException e) {
			throw new RuntimeException("Cant find stock number by id: " + stockNumberId + ", error: " + e.getLocalizedMessage());
		}
	}

	public List<StockNumbers> getAllStockNumbers() {
		return this.getList("SELECT * FROM stockNumbers", StockNumbers.class);
	}

	public List<StockNumbers> getStockNumbersForDealershipId(Integer dealershipId) {
		return this.getList("SELECT * FROM stockNumbers WHERE dealershipId = ?", StockNumbers.class, dealershipId);
	}
	
	protected List<StockNumbers> getStockNumbersForDealershipFromTo(Integer dealershipId, DateTime from, DateTime to) {
		final String sql = "SELECT * FROM stockNumbers WHERE dealershipId = ? AND soldOn between ? and ?";
		return this.getList(sql, StockNumbers.class, dealershipId, from.toString(), to.toString());
	}
	
	public Integer deleteStockNumberById(Integer id) {
		final String sql = "DELETE FROM stockNumbers WHERE id = ?";
		return this.updateRow(sql, id);
	}

	public StockNumbers updateStockNumber(StockNumbers stockNumber) {
		final String sql = "UPDATE stockNumbers SET dealershipId = ?, stockNumber = ?, status = ?, soldOn = ?, soldBy = ? WHERE id = ?";
		int i = this.updateRow(sql, stockNumber.getDealershipId(), stockNumber.getStockNumber(), stockNumber.getStatus(), stockNumber.getSoldOn(), stockNumber.getSoldBy(), stockNumber.getId());
		if(i == 0)
			throw new RuntimeException("update stockNumber failed for stockNumbers: " + stockNumber.toString());
		
		return stockNumber;
	}
	
	
}
