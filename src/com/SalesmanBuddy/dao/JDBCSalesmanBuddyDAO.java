package com.SalesmanBuddy.dao;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Date;
import java.util.List;
import java.util.Random;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcDaoSupport;

public class JDBCSalesmanBuddyDAO extends NamedParameterJdbcDaoSupport implements SalesmanBuddyDAO {
	// TODO do tests for specific statuses, number truncation? convert things to using queryforobject
	// TODO on updates, add SELECT * FROM () WHERE rownum = 1
	protected final Log log = LogFactory.getLog(this.getClass());
//	protected final String DB_TIMESTAMP = "systimestamp AT TIME ZONE 'UTC'";
//	protected final int SUBJECT_LIMIT = 50;
//	protected final int BODY_LIMIT = 2000;
//	protected final int BODY_PART_LIMIT = 800;
//	protected final int ERROR_VARCHAR2_LIMIT = 2000;
//	protected final int WHO_CHANGED_ID_LIMIT = 18;
//	protected final int AOPT_OUTPUTS_VARCHAR2_LIMIT = 200;
//	protected final int URL_LIMIT = 100;
//	protected final int F_L_NAME_LIMIT = 50;
//	protected final int EMAIL_LIMIT = 254;
//	protected final int VARCHAR2_ID_LIMIT = 18;
//	
////	private String backupHost = "";
////	private String aoptAPIURL = "";
////	private String localPathToAudio = "";
//	// from eidao*******************************************************************************************************************************
////	private JDBCLangEvalDAO(){
////		super();
////		try {
////			Context env = (Context)new InitialContext().lookup("java:comp/env");
////			backupHost = (String)env.lookup("backUpContentHost");
////			aoptAPIURL = (String)env.lookup("aoptAPIURL");
////			localPathToAudio = (String)env.lookup("localPathToAudio");
////		} catch (NamingException e) {
////			// TODO Auto-generated catch block
////			e.printStackTrace();
////		}
////	
////	}
//
//	
//	
//	// Settings
//	@Override
//	public Settings getSettings() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM settings WHERE id=(SELECT id FROM (SELECT id, max(whenChanged) FROM settings GROUP BY id ORDER BY max(whenChanged) desc) WHERE rownum = 1)", params, new BeanPropertyRowMapper<Settings>(Settings.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getting latest settings");
//		}
//	}
//
//	@Override
//	public Settings updateSettings(Settings updatedSettings) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("ug", updatedSettings.getUncategorizedGroup());
//		params.addValue("ecsg", updatedSettings.getEmailChangeStatusGroup());
//		params.addValue("esg", updatedSettings.getEmailSchedulingGroup());
//		params.addValue("epsg", updatedSettings.getEmailPrefieldServicesGroup());
//		params.addValue("ter", updatedSettings.getTimeExtendRecording());
//		params.addValue("erm", updatedSettings.getEmailReminderMissionary());
//		params.addValue("ermr", updatedSettings.getEmailReminderMissionaryR());
//		params.addValue("ere", updatedSettings.getEmailReminderEcclesiastical());
//		params.addValue("erer", updatedSettings.getEmailReminderEcclesiasticalR());
//		params.addValue("cram", updatedSettings.getCallReminderAutoMissionary());
//		params.addValue("cramr", updatedSettings.getCallReminderAutoMissionaryR());
//		params.addValue("crm", updatedSettings.getCallReminderMissionary());
//		params.addValue("crmr", updatedSettings.getCallReminderMissionaryR());
//		params.addValue("wat", updatedSettings.getWhenArchivedType());
//		params.addValue("watime", updatedSettings.getWhenArchivedTime());
//		params.addValue("wcr", updatedSettings.getWhenCheckReminders());
//		params.addValue("hcr", updatedSettings.getHourCheckReminders());
//		params.addValue("eds", updatedSettings.getEcclesiasticalDefaultSubject().subSequence(0, (updatedSettings.getEcclesiasticalDefaultSubject().length() > SUBJECT_LIMIT) ? SUBJECT_LIMIT : updatedSettings.getEcclesiasticalDefaultSubject().length()));
//		params.addValue("edb1", updatedSettings.getEcclesiasticalDefaultBody1().subSequence(0, (updatedSettings.getEcclesiasticalDefaultBody1().length() > BODY_PART_LIMIT) ? BODY_PART_LIMIT : updatedSettings.getEcclesiasticalDefaultBody1().length()));
//		params.addValue("edb2", updatedSettings.getEcclesiasticalDefaultBody2().subSequence(0, (updatedSettings.getEcclesiasticalDefaultBody2().length() > BODY_PART_LIMIT) ? BODY_PART_LIMIT : updatedSettings.getEcclesiasticalDefaultBody2().length()));
//		params.addValue("wcid", updatedSettings.getWhoChangedId().subSequence(0, (updatedSettings.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : updatedSettings.getWhoChangedId().length()));
//		params.addValue("id", updatedSettings.getId());
//		if(getNamedParameterJdbcTemplate().update(
//				"INSERT INTO settings(uncategorizedGroup, emailChangeStatusGroup, emailSchedulingGroup, emailPrefieldServicesGroup, timeExtendRecording, emailReminderMissionary, emailReminderMissionaryR, " +
//						"emailReminderEcclesiastical, emailReminderEcclesiasticalR, callReminderAutoMissionary, callReminderAutoMissionaryR, callReminderMissionary, callReminderMissionaryR, whenArchivedType, " +
//						"whenArchivedTime, whenCheckReminders, hourCheckReminders, ecclesiasticalDefaultSubject, ecclesiasticalDefaultBody1, ecclesiasticalDefaultBody2, whoChangedId) " +
//							"VALUES (:ug, :ecsg, :esg, :epsg, :ter, :erm, :ermr, :ere, :erer, :cram, :cramr, :crm, :crmr, :wat, :watime, :wcr, :hcr, :eds, :edb1, :edb2, :wcid)", params) != 1)
//			throw new RuntimeException("insert into settings failed");
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM settings WHERE uncategorizedGroup = :ug AND emailChangeStatusGroup = :ecsg AND emailSchedulingGroup = :esg AND " +
//					"emailPrefieldServicesGroup = :epsg AND timeExtendRecording = :ter AND emailReminderMissionary = :erm AND emailReminderMissionaryR = :ermr AND emailReminderEcclesiastical = :ere AND " +
//					"emailReminderEcclesiasticalR = :erer AND callReminderAutoMissionary = :cram AND callReminderAutoMissionaryR = :cramr AND callReminderMissionary = :crm AND callReminderMissionaryR = :crmr AND " +
//					"whenArchivedType = :wat AND whenArchivedTime = :watime AND whenCheckReminders = :wcr AND hourCheckReminders = :hcr AND ecclesiasticalDefaultSubject = :eds AND " +
//					"ecclesiasticalDefaultBody1 = :edb1 AND ecclesiasticalDefaultBody2 = :edb2 AND whoChangedId = :wcid ORDER BY id desc", params, new BeanPropertyRowMapper<Settings>(Settings.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for updateSettings");
//		}
//	}
//
//	@Override
//	public List<Settings> getSettingsHistory() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM (SELECT * FROM settings ORDER BY whenChanged desc) WHERE rownum <= 10 ORDER BY rownum", params, new BeanPropertyRowMapper<Settings>(Settings.class));
//	}
//	
//	
//	
//	
//	
//	// Email Groups
//	@Override
//	public EmailGroup updateEmailGroup(EmailGroup updatedEmailGroup) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("db1", updatedEmailGroup.getDefaultBody1().subSequence(0, (updatedEmailGroup.getDefaultBody1().length() > BODY_PART_LIMIT) ? BODY_PART_LIMIT : updatedEmailGroup.getDefaultBody1().length()));
//		params.addValue("db2", updatedEmailGroup.getDefaultBody2().subSequence(0, (updatedEmailGroup.getDefaultBody2().length() > BODY_PART_LIMIT) ? BODY_PART_LIMIT : updatedEmailGroup.getDefaultBody2().length()));
//		params.addValue("ds", updatedEmailGroup.getDefaultSubject().subSequence(0, (updatedEmailGroup.getDefaultSubject().length() > SUBJECT_LIMIT) ? SUBJECT_LIMIT : updatedEmailGroup.getDefaultSubject().length()));
//		params.addValue("n", updatedEmailGroup.getName().subSequence(0, (updatedEmailGroup.getName().length() > 50) ? 50 : updatedEmailGroup.getName().length()));
//		params.addValue("wcid", updatedEmailGroup.getWhoChangedId().subSequence(0, (updatedEmailGroup.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : updatedEmailGroup.getWhoChangedId().length()));
//		if(updatedEmailGroup.getId() == 0){// new group
//			if(getNamedParameterJdbcTemplate().update("INSERT INTO emailGroups (defaultBody1, defaultBody1, defaultSubject, name, whoChangedId) VALUES (:db1, :db2, :ds, :n, :wcid)", params) != 1)
//				throw new RuntimeException("insert into emailgroups failed");
//			try{
//				return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM emailGroups WHERE defaultBody1 = :db1 AND defaultBody2 = :db2 AND defaultSubject = :ds AND name = :n AND whoChangedId = :wcid ORDER BY id desc", params, new BeanPropertyRowMapper<EmailGroup>(EmailGroup.class));
//			}catch(EmptyResultDataAccessException e){
//				throw new RuntimeException("EmptyResultDataAccessException was thrown for getting new email group");
//			}
//		}else{// existing group
//			params.addValue("id", updatedEmailGroup.getId());
//			getNamedParameterJdbcTemplate().update("UPDATE emailGroups SET defaultBody1 = :db1, defaultBody2 = :db2, defaultSubject = :ds, name = :n, whoChangedId = :wcid WHERE id = :id", params);
//			return getEmailGroupById(updatedEmailGroup.getId());
//		}
//	} 
//
//	@Override
//	public EmailGroup getEmailGroupById(int emailGroupId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", emailGroupId);
//		try{
//			List<EmailGroup> tempEmailGroupList = getNamedParameterJdbcTemplate().query("SELECT * FROM emailGroups WHERE id=:id", params, new BeanPropertyRowMapper<EmailGroup>(EmailGroup.class));
//			EmailGroup temp = tempEmailGroupList.get(tempEmailGroupList.size() - 1);
//			temp.setEmails(getNamedParameterJdbcTemplate().query("SELECT * FROM emails WHERE groupId = :id", params, new BeanPropertyRowMapper<Email>(Email.class)));
//			return temp;
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getting email group with emails attached");
//		}
//	}
//
//	@Override
//	public List<EmailGroup> deleteEmailGroupById(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		getNamedParameterJdbcTemplate().update("DELETE FROM groupEmails WHERE id=:id", params);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM emailGroups", params, new BeanPropertyRowMapper<EmailGroup>(EmailGroup.class));
//	}
//
//	@Override
//	public List<EmailGroup> getEmailGroups() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		List<EmailGroup> tempEmailGroups = getNamedParameterJdbcTemplate().query("SELECT * FROM emailGroups ORDER BY id", params, new BeanPropertyRowMapper<EmailGroup>(EmailGroup.class));
//		for(int i = 0; i < tempEmailGroups.size(); i++){
//			params.addValue("id", tempEmailGroups.get(i).getId());
//			tempEmailGroups.get(i).setEmails(getNamedParameterJdbcTemplate().query("SELECT * FROM emails WHERE groupId = :id", params, new BeanPropertyRowMapper<Email>(Email.class)));
//		}
//		return tempEmailGroups;
//	}
//	
//	
//	
//	
//	
//	
//	// Emails
//	@Override
//	public Email getEmailById(int emailId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", emailId);
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM emails WHERE id=:id", params, new BeanPropertyRowMapper<Email>(Email.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getting email by id");
//		}
//	}
//
//	@Override
//	public Email updateEmail(Email updatedEmail) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("e", updatedEmail.getEmail().subSequence(0, (updatedEmail.getEmail().length() > EMAIL_LIMIT) ? EMAIL_LIMIT : updatedEmail.getEmail().length()));
//		params.addValue("gid", updatedEmail.getGroupId());
//		params.addValue("n", updatedEmail.getName().subSequence(0, (updatedEmail.getName().length() > 50) ? 50 : updatedEmail.getName().length()));
//		params.addValue("uid", updatedEmail.getUserId());
//		params.addValue("wcid", updatedEmail.getWhoChangedId().subSequence(0, (updatedEmail.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : updatedEmail.getWhoChangedId().length()));
//		if(updatedEmail.getId() == 0){// new
//			if(getNamedParameterJdbcTemplate().update("INSERT INTO emails (email, groupId, name, userId, whoChangedId) VALUES (:e, :gid, :n, :uid, :wcid)", params) != 1)
//				throw new RuntimeException("insert into emails failed for new email");
//			try{
//				return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM emails WHERE email = :e AND groupId = :gid AND name = :n AND userId = :uid AND whoChangedId = :wcid ORDER BY id desc", params, new BeanPropertyRowMapper<Email>(Email.class));
//			}catch(EmptyResultDataAccessException e){
//				throw new RuntimeException("EmptyResultDataAccessException was thrown for getting newly inserted email");
//			}
//		}else{// existing
//			params.addValue("id", updatedEmail.getId());
//			if(getNamedParameterJdbcTemplate().update("UPDATE emails SET pageName = :pn, description = :d, whoChangedId = :wcid WHERE id = :id", params) != 1)
//				throw new RuntimeException("update email failed");
//			return getEmailById(updatedEmail.getId());
//		}
//	}
//
//	@Override
//	public int deleteEmailById(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		return getNamedParameterJdbcTemplate().update("DELETE FROM emails WHERE id = :id", params);
//	}
//
//	@Override
//	public List<Email> getEmailsOfGroupByGroupId(int emailGroupId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("gid", emailGroupId);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM emails WHERE groupId = :gid ORDER BY id", params, new BeanPropertyRowMapper<Email>(Email.class));
//	}
//
//	@Override
//	public List<Email> getAllEmails() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM emails ORDER BY email", params, new BeanPropertyRowMapper<Email>(Email.class));
//	}
//	
//	
//	
//	
//	
//	
//	// Sent Notifications
//	@Override
//	public int howManySentNotificationsByType(String type) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		// TODO check types
//		params.addValue("type", type);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM sentnotifications WHERE type = :type", params, new BeanPropertyRowMapper<Settings>(Settings.class)).size();
//	}
//
//	@Override
//	public List<SentNotification> getSentNotificationsBetweenindexes(int starting, int ending, String type) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("start", starting);
//		params.addValue("end", ending);
//		// TODO check types
//		params.addValue("type", type);
//		if(type.equals("all"))
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM (SELECT * FROM sentNotifications ORDER BY whenSent desc) WHERE rownum <= :end AND rownum >= :start ORDER BY rownum", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//		else
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM (SELECT * FROM sentNotifications WHERE type=:type ORDER BY whenSent desc) WHERE rownum <= :end AND rownum >= :start ORDER BY rownum", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//	}
//
//	@Override
//	public List<SentNotification> getSentNotificationsAboutMissionary(String missionaryId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("mid", missionaryId);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM sentNotifications WHERE missionaryId=:mid ORDER BY whenSent desc", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//	}
//
//	@Override
//	public List<SentNotification> getSentNotificationBetweenDates(java.sql.Date starting, java.sql.Date ending) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("start", starting);
//		params.addValue("end", ending);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM sentNotifications WHERE whenSent <= :end AND whenSent >= :start", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//	}
//
//	@Override
//	public List<SentNotification> getSentNotificationsBySenderId(String senderId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("sid", senderId);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM sentNotifications WHERE senderId = :sid ORDER BY whenSent desc", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//	}
//
//	@Override
//	public List<SentNotification> getAllSentNotifications() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM sentNotifications ORDER BY whenSent desc", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//	}
//
//	@SuppressWarnings("unused")
//	@Override
//	public int sentNotifications(SentNotificationFromClient notification) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("sid", notification.getSenderId());
//		params.addValue("sEmail", notification.getSenderEmail().subSequence(0, (notification.getSenderEmail().length() > EMAIL_LIMIT) ? EMAIL_LIMIT : notification.getSenderEmail().length()));
//		params.addValue("t", notification.getType().subSequence(0, (notification.getType().length() > 20) ? 20 : notification.getType().length()));
//		// TODO check types
//		params.addValue("s", notification.getSubject().subSequence(0, (notification.getSubject().length() > SUBJECT_LIMIT) ? SUBJECT_LIMIT : notification.getSubject().length()));
//		params.addValue("m", notification.getMessage().subSequence(0, (notification.getMessage().length() > BODY_LIMIT) ? BODY_LIMIT : notification.getMessage().length()));
//		params.addValue("ws", notification.getWhenSent());
//		List<String> recipients = notification.getRecipients();
//		List<String> missionaryIds = notification.getMissionaryIds(); 
//		Random rgen = new Random(new Date().getTime());
//		params.addValue("gid", rgen.nextInt(999999999));
//		for(int i = 0; i < recipients.size(); i++){
//			params.addValue("r", recipients.get(i).subSequence(0, (recipients.get(i).length() > EMAIL_LIMIT) ? EMAIL_LIMIT : recipients.get(i).length()));
//			return getNamedParameterJdbcTemplate().update("INSERT INTO recipients (recipient, groupId) VALUES (:r, :gid)", params);
//		}
//		for(int i = 0; i < missionaryIds.size(); i++){
//			params.addValue("mid", missionaryIds.get(i));
//			return getNamedParameterJdbcTemplate().update("INSERT INTO sentNotifications (senderId, type, recipientGroupId, missionaryId, subject, message, whenSent) VALUES (:sid, :t, :gid, :mid, :s, :m, :ws)", params);
//		}
//		throw new RuntimeException("either recipients or missionary ids were empty");
//	}
//	
//	@Override
//	public SentNotification getSentNotificationById(int id){
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM sentNotifications WHERE id = :id", params, new BeanPropertyRowMapper<SentNotification>(SentNotification.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getting sent notification by id");
//		}
//	}
//
//	
//	
//	
//
//	
//	
//	
//	// Prompts
//	@Override
//	public Prompt getPromptById(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM prompts WHERE id = :id ORDER BY promptId", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getting prompt by id");
//		}
//	}
//
//	@Override
//	public Prompt updatePrompt(Prompt updatedPrompt) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("wcid", updatedPrompt.getWhoChangedId().subSequence(0, (updatedPrompt.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : updatedPrompt.getWhoChangedId().length()));
//		if(updatedPrompt.getPromptId() == 0){//new
//			params.addValue("l", updatedPrompt.getSeconds());
//			params.addValue("p", updatedPrompt.getPrompt().subSequence(0, (updatedPrompt.getPrompt().length() > 2000) ? 2000 : updatedPrompt.getPrompt().length()));
//			params.addValue("si", updatedPrompt.getSortIndex()); 
//			if(getNamedParameterJdbcTemplate().update("INSERT INTO prompts (length, prompt, sortIndex, whoChangedId) VALUES (:l, :p, :si, :wcid)", params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM prompts WHERE length = :l AND prompt = :p AND sortIndex = :si AND whoChangedId = :wcid ORDER BY id desc", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for getting newly inserted prompt");
//				}
//			}else
//				throw new RuntimeException("Insert into prompts failed");
//		}else{// existing
//			params.addValue("id", updatedPrompt.getPromptId());
//			params.addValue("wc", DB_TIMESTAMP);
//			if(getNamedParameterJdbcTemplate().update("UPDATE prompts SET length = :l, prompt = :p, sortIndex = :si, whenChanged = :wc, whoChangedId = :wcid WHERE id = :id", params) == 1)
//				return getPromptById(updatedPrompt.getPromptId());
//			else
//				throw new RuntimeException("update prompts failed");
//		}
//	}
//
//	@Override
//	public List<Prompt> getAllPrompts() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM prompts ORDER BY promptId", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//	}
//
//	@Override
//	public List<Prompt> getAllPromptsByLanguage(String language) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("language", language);
//		List<Prompt> test = getNamedParameterJdbcTemplate().query(
//				"SELECT promptid, sortindex, prompt, seconds, languageid, whochangedid, whenchanged " +
//				"FROM (SELECT p.*, rank() OVER (PARTITION BY p.sortindex ORDER BY dbms_random.value) rank " +
//				"		FROM prompts p " +
//				"		WHERE p.languageid=(" +
//				"			SELECT id FROM languages WHERE shortLanguage = :language OR longlanguage = :language " +
//				"			) " +
//				"		) " +
//				"WHERE rank<=1 ", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//		//Select random from each level of difficulty
//		//REMOVED AND COMBINED
//		/*if(language.length() == 2)
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM prompts WHERE languageId = (SELECT id FROM languages WHERE shortLanguage = :language) ORDER BY sortIndex", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//		else if(language.length() > 2)
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM prompts WHERE languageId = (SELECT id FROM languages WHERE longLanguage = :language) ORDER BY sortIndex", params, new BeanPropertyRowMapper<Prompt>(Prompt.class));
//		else*/
//		if (test.size()==0)
//			throw new RuntimeException("invalid language: " + language);
//		else 
//			return test;
//	}
//
//	@Override
//	public List<Prompt> getTestByLanguage(String language) {
//		return getAllPromptsByLanguage(language);
//	}
//
//	
//	
//	// TODO change things over to using the already build get by id functions and make sure RuntimeException error messages are correct from here down. Also, remove unneeded brackets everywhere
//	
//	
//	// Languages
//	@Override
//	public List<Language> getAllTestLanguages(int use, String missionaryId) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("mid", missionaryId);
//		params.addValue("use", use);
//		if(use == 1){
//			return getNamedParameterJdbcTemplate().query(
//					"SELECT l.*, DECODE(a.missionaryid,null,0,1) as alreadytaken " +
//					"FROM languages l, (SELECT DISTINCT languageid, missionaryid FROM assessments WHERE missionaryid=:mid) a " +
//					"WHERE use = :use AND l.id=a.languageid(+) " +
//					"ORDER BY longLanguage", params, new BeanPropertyRowMapper<Language>(Language.class));
//			//REMOVED, MADE PART OF QUERY
//			/*for(int i = 0; i < languages.size(); i++){
//				int taken = 0;
//				params.addValue("lid", languages.get(i).getId());
//				if(getNamedParameterJdbcTemplate().query("SELECT * FROM assessments WHERE languageId = :lid AND missionaryId = :mid", params, new BeanPropertyRowMapper<Language>(Language.class)).size() != 0)
//					languages.get(i).setAlreadyTaken(taken);
//			}
//			return languages;*/
//		}
//		else if(use == 0)
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM languages ORDER BY longLanguage", params, new BeanPropertyRowMapper<Language>(Language.class));
//		else
//			throw new RuntimeException("use: " + use + " is not a valid value. Should be 0 or 1");
//	}
//	
//	protected int id;
//	protected int use;
//	protected String shortLanguage;
//	protected String longLanguage;
//	protected String nativeLanguage;
//	protected Date whenChanged;
//	
//	@Override
//	public Language updateLanguage(Language language) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("u", language.getUse());
//		params.addValue("sl", language.getShortLanguage().subSequence(0, (language.getShortLanguage().length() > 4) ? 4 : language.getShortLanguage().length()));
//		params.addValue("ll", language.getLongLanguage().subSequence(0, (language.getLongLanguage().length() > 50) ? 50 : language.getLongLanguage().length()));
//		params.addValue("nl", language.getNativeLanguage().subSequence(0, (language.getNativeLanguage().length() > 50) ? 50 : language.getNativeLanguage().length()));
//		params.addValue("wcid", language.getWhoChangedId().subSequence(0, (language.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : language.getWhoChangedId().length()));
//		if(language.getId() == 0){//new
//			// TODO make sure they arent putting in a language that already exists
//			if(getNamedParameterJdbcTemplate().update("INSERT INTO languages (use, shortLanguage, longLanguage, nativeLanguage, whoChangedId) VALUES (:u, :sl, :ll, :nl, :wcid)", params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM languages WHERE use = :u AND shortLanguage = :sl AND longLanguage = :ll AND nativeLanguage = :nl AND whoChangedId = :wcid ORDER BY longLanguage", params, new BeanPropertyRowMapper<Language>(Language.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for updateHandScore, getting the modified item");
//				}
//			}else
//				throw new RuntimeException("Insert into languages failed");
//		}else{// existing
//			params.addValue("id", language.getId());
//			params.addValue("wc", DB_TIMESTAMP);
//			if(getNamedParameterJdbcTemplate().update("UPDATE languages SET use = :u, shortLanguage = :sl, longLanguage = :ll, nativeLanguage = :nl, whoChangedId = :wcid WHERE id = :id", params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM languages WHERE id = :id", params, new BeanPropertyRowMapper<Language>(Language.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for updateHandScore, getting the modified item");
//				}
//			}else
//				throw new RuntimeException("update languages failed");
//		}
//	}
//	
//	
//	// Assessments
//	@Override
//	public Assessment updateAssessment(Assessment assessment) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		if(assessment.getId() == 0){//new
//			params.addValue("mid", assessment.getMissionaryId());
//			params.addValue("lid", assessment.getLanguageId());
//			params.addValue("fName", assessment.getfName().subSequence(0, (assessment.getfName().length() > 50) ? 50 : assessment.getfName().length()));
//			params.addValue("lName", assessment.getlName().subSequence(0, (assessment.getlName().length() > 50) ? 50 : assessment.getlName().length()));
//			params.addValue("area", assessment.getArea().subSequence(0, (assessment.getArea().length() > 50) ? 50 : assessment.getArea().length()));
//			params.addValue("emtcd", assessment.getEnterMtcDate());
//			if(getNamedParameterJdbcTemplate().update(
//					"INSERT INTO assessments (languageId, missionaryId, fName, lName, area, status, enterMtcDate) " +
//					"VALUES (:lid, :mid, :fName, :lName, :area, 'Created', :emtcd)", params) == 1){ //also set whenchanged? auto db?
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject(
//							"SELECT * FROM (" +
//							"	SELECT * FROM assessments " +
//							"	WHERE languageId = :lid AND missionaryId = :mid AND fName = :fName AND lName = :lName AND area = :area " +
//							"		AND status = 'Created' " +
//							"	ORDER BY id desc) " +
//							"WHERE rownum = 1", params, new BeanPropertyRowMapper<Assessment>(Assessment.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown, getting the modified item");
//				}
//			}else
//				throw new RuntimeException("Insert into assessments failed");
//		}else{// existing
//			params.addValue("id", assessment.getId());
//			params.addValue("status", assessment.getStatus());
//			String sql = "";
//			if(assessment.getStatus().equals("Taken")) //ADDED
//				sql = "UPDATE assessments SET status = :status, whenChanged = "+DB_TIMESTAMP+", takendate = "+DB_TIMESTAMP+" WHERE id = :id";
//			else
//				sql = "UPDATE assessments SET status = :status, whenChanged = "+DB_TIMESTAMP+" WHERE id = :id";
//			if(getNamedParameterJdbcTemplate().update(sql, params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject(
//							"SELECT * FROM assessments WHERE id = :id", params, new BeanPropertyRowMapper<Assessment>(Assessment.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown, getting the modified item");
//				}
//			}else
//				throw new RuntimeException("update assessments failed");
//		}
//	}
//	
//	@Override
//	public List<Assessment> getAssessments(int archived) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("a", archived);
//		if(archived == 1 || archived == 0)
//			return getNamedParameterJdbcTemplate().query("SELECT * FROM assessments WHERE archived = :a ORDER BY lName", params, new BeanPropertyRowMapper<Assessment>(Assessment.class));
//		else
//			throw new RuntimeException("Archived must be 0 or 1");
//	}
//
//	@Override
//	public Assessment getAssessment(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		try{
//			Assessment assessment = getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM assessments WHERE id = :id", params, new BeanPropertyRowMapper<Assessment>(Assessment.class));
//			params.addValue("assessmentId", assessment.getId());
//			// TODO score is messing up this next call
//			assessment.setItems(getNamedParameterJdbcTemplate().query("SELECT id, assessmentId, promptId, type, status, unrateable, url, whenChanged FROM items WHERE assessmentId = :assessmentId ORDER BY id", params, new BeanPropertyRowMapper<Item>(Item.class)));
//			for(int i = 0; i < assessment.getItems().size(); i++){
//				params.addValue("itemId", assessment.getItems().get(i).getId());
//				assessment.getItems().get(i).setHandScores(getNamedParameterJdbcTemplate().query("SELECT * FROM handScores WHERE itemId = :itemId", params, new BeanPropertyRowMapper<HandScore>(HandScore.class)));
//				params.addValue("pid", assessment.getItems().get(i).getPromptId());
//				assessment.getItems().get(i).setPromptText(getNamedParameterJdbcTemplate().queryForObject("SELECT prompt FROM prompts WHERE promptId = :pid", params, String.class));
//			}
//			return assessment;
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for updateHandScore, getting the modified item");
//		}
//	}
//	
//	@Override
//	public Assessment getPartialAssessment(String id, int lang){
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		params.addValue("lang", lang);
//		try{
//			Assessment assessment = getNamedParameterJdbcTemplate().queryForObject(
//					"SELECT * FROM assessments " +
//					"WHERE missionaryid = :id AND languageid=:lang AND status='Created'", params, new BeanPropertyRowMapper<Assessment>(Assessment.class));
//			params.addValue("assessmentId", assessment.getId());
//			assessment.setItems(getNamedParameterJdbcTemplate().query(
//					"SELECT id, assessmentId, promptId, type, status, unrateable, url, whenChanged " +
//					"FROM items WHERE assessmentId = :assessmentId ORDER BY id", params, new BeanPropertyRowMapper<Item>(Item.class)));
//			return assessment;
//		} catch(EmptyResultDataAccessException e){
//			return null;
//		}
//	}
//	
//	
//	// Items
//	@Override
//	public Item updateItem(Item item) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("url", item.getUrl());
//		if(item.getId() == 0){//new
//			params.addValue("aid", item.getAssessmentId());
//			params.addValue("pid", item.getPromptId());
//			params.addValue("wc", new Date());
//			// TODO remove url from this list once the db is changed
//			if(getNamedParameterJdbcTemplate().update(
//					"INSERT INTO items (url, assessmentId, promptId, status, type, whenChanged) " +
//					"VALUES (:url, :aid, :pid, 'Created', 'sle', :wc)", params) == 1){ 
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject(
//							"SELECT id, assessmentId, promptId, status, type, unrateable, url, whenChanged " +
//							"FROM (SELECT * FROM items " +
//							"		WHERE assessmentId = :aid AND promptId = :pid AND status = 'Created' AND type = 'sle' " +
//							"			AND url = :url ORDER BY whenChanged desc) " +
//							"WHERE rownum = 1", params, new BeanPropertyRowMapper<Item>(Item.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for getting latest item");
//				}
//			}else{
//				throw new RuntimeException("Insert into items failed");
//			}
//		}else{// existing
//			params.addValue("id", item.getId());
//			if(item.getUrl().length() > 1000)
//				throw new RuntimeException("url was too long: " + item.getUrl());
//			if(getNamedParameterJdbcTemplate().update(
//					"UPDATE items SET url = :url, status = 'Taken', whenChanged = systimestamp AT TIME ZONE 'UTC' WHERE id = :id", params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject(
//							"SELECT id, assessmentId, promptId, status, type, unrateable, url, whenChanged " +
//							"FROM items WHERE id = :id", params, new BeanPropertyRowMapper<Item>(Item.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for getting the item");
//				}
//			}else{
//				throw new RuntimeException("update items failed");
//			}
//		}
//	}
//	
//	
//	// Hand Scores
//	@Override
//	public Item updateHandScore(HandScore handScore) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("iid", handScore.getItemId());
//		params.addValue("r", handScore.getRater().subSequence(0, (handScore.getRater().length() > 20) ? 20 : handScore.getRater().length()));
//		params.addValue("s", handScore.getScore());
//		params.addValue("ct", handScore.getCommentText().subSequence(0, (handScore.getCommentText().length() > 500) ? 500 : handScore.getCommentText().length()));
//		params.addValue("wc", new Date());
//		if(handScore.getId() != 0)
//			throw new RuntimeException("Cannot edit an existing hand score");
//		if(getNamedParameterJdbcTemplate().update(
//				"INSERT INTO handScores (itemId, rater, score, commentText, whenChanged) " +
//				"VALUES (:iid, :r, :s, :ct, :wc)", params) == 1){
//			if(getNamedParameterJdbcTemplate().update(
//					"UPDATE items SET score = (SELECT Avg(score) FROM handScores WHERE itemId = :iid)/7*100 WHERE id = :iid", params) == 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject(
//							"SELECT * FROM items WHERE id = :iid", params, new BeanPropertyRowMapper<Item>(Item.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for updateHandScore, getting the modified item");
//				}
//			}else
//				throw new RuntimeException("Update into handScores failed");
//		}else{
//			throw new RuntimeException("Insert into handScores failed");
//		}
//	}
//	
//	// Other stuff
//	protected static String outboundRequest(String Url, String sPost, String sMethod, int attempts, String contextType) {
//	    if (sMethod == null) {
//	        sMethod = "GET";
//	    }
//
//	    URLConnection connection;
//	    try {
//	        URL f = new URL(Url);
//
//	        //connection = uri.toURL().openConnection();
//	        connection = f.openConnection();
//	    } catch (NullPointerException e) {
//	        return "Error: Must Authenticate";
//	    } catch (MalformedURLException e) {
//	        return "Error: " + e.getMessage();
//	    } catch (IOException e) {
//	        return "Error: " + e.getMessage();
//	    }
//
//	    StringBuilder sb = new StringBuilder();
//	    try {
//	        // cast the connection to a HttpURLConnection so we can examine the status code
//	        HttpURLConnection httpConnection = (HttpURLConnection) connection;
//	        httpConnection.setRequestMethod(sMethod);
//	        httpConnection.setConnectTimeout(20000);
//	        httpConnection.setReadTimeout(20000);
//	        httpConnection.setUseCaches(false);
//	        httpConnection.setDefaultUseCaches(false);
//	        httpConnection.setDoOutput(true);
//
//	        if (!"".equals(sPost)) {
//	            //setup connection
//	            httpConnection.setDoInput(true);
//	            httpConnection.setRequestProperty("Content-Type", contextType);
//
//	            //execute connection and send xml to server
//	            OutputStreamWriter writer = new OutputStreamWriter(httpConnection.getOutputStream());
//	            writer.write(sPost);
//	            writer.flush();
//	            writer.close();
//	        }
//	        BufferedReader in;
//	        // if the status code is success then the body is read from the input stream
//	        if (httpConnection.getResponseCode() == 200) {
//	            in = new BufferedReader(new InputStreamReader(
//	                    httpConnection.getInputStream()));
//	            // otherwise the body is read from the output stream
//	        }else{
//	            in = new BufferedReader(new InputStreamReader(
//	                    httpConnection.getErrorStream()));
//	        }
//	        String inputLine;
//	        while ((inputLine = in.readLine()) != null) {
//	            sb.append(inputLine);
//	        }
//	        in.close();
//	        // Determine the result of the rest call and automatically adjusts the user context in case the timestamp was invalid
////		        int result = userContext.interpretResult(
////		                httpConnection.getResponseCode(), sb.toString());
////		        if (result == ID2LUserContext.RESULT_OKAY) {
////		            return sb.toString();
////		            // if the timestamp is invalid and we haven't exceeded the retry
////		            // limit then the call is made again with the adjusted timestamp
////		        } else if (result == userContext.RESULT_INVALID_TIMESTAMP
////		                && attempts > 0) {
////		            return getValanceResult(userContext, uri, query, sPost, sMethod, attempts - 1);
////		        } else {
////		            sError = sb + " " + result;
////		        }
//	        return sb.toString();
//	    } catch (IllegalStateException e) {
//	        return "Error: Exception while parsing";
//	    } catch (FileNotFoundException e) {
//	        // 404
//	        return "Error: URI Incorrect";
//	    } catch (IOException e) {
//	    	return "Error: General Exception";
//	    }
//	}
//
//	
//
//	
//	
//	// Errors
//	@Override
//	public ErrorMtc updateError(ErrorMtc error) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("d", error.getData().subSequence(0, (error.getData().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getData().length()));
//		params.addValue("s", error.getStatus().subSequence(0, (error.getStatus().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getStatus().length()));
//		params.addValue("h", error.getHeaders().subSequence(0, (error.getHeaders().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getHeaders().length()));
//		params.addValue("c", error.getConfiguration().subSequence(0, (error.getConfiguration().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getConfiguration().length()));
//		params.addValue("p", error.getPath().subSequence(0, (error.getPath().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getPath().length()));
//		params.addValue("state", error.getState().subSequence(0, (error.getState().length() > 50) ? 50 : error.getState().length()));
//		params.addValue("o1", error.getOther1().subSequence(0, (error.getOther1().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getOther1().length()));
//		params.addValue("o2", error.getOther2().subSequence(0, (error.getOther2().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getOther2().length()));
//		params.addValue("o3", error.getOther3().subSequence(0, (error.getOther3().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getOther3().length()));
//		params.addValue("o4", error.getOther4().subSequence(0, (error.getOther4().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getOther4().length()));
//		params.addValue("o5", error.getOther5().subSequence(0, (error.getOther5().length() > ERROR_VARCHAR2_LIMIT) ? ERROR_VARCHAR2_LIMIT : error.getOther5().length()));
//		params.addValue("wcid", error.getWhoChangedId().subSequence(0, (error.getWhoChangedId().length() > VARCHAR2_ID_LIMIT) ? VARCHAR2_ID_LIMIT : error.getWhoChangedId().length()));
//		if(error.getId() == 0){
//			int i = getNamedParameterJdbcTemplate().update("INSERT INTO errors (data, status, headers, configuration, path, state, other1, other2, other3, other4, other5, whoChangedId) VALUES (:d, :s, :h, :c, :p, :state, :o1, :o2, :o3, :o4, :o5, :wcid)", params);
//			if(i >= 1){
//				try{
//					return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM errors WHERE data = :d AND status = :s AND headers = :h AND configuration = :c AND path = :p AND state = :state AND other1 = :o1 AND other2 = :o2 AND other3 = :o3 AND other4 = :o4 AND other5 = :o5 AND whoChangedId = :wcid ORDER BY id desc", params, new BeanPropertyRowMapper<ErrorMtc>(ErrorMtc.class));
//				}catch(EmptyResultDataAccessException e){
//					throw new RuntimeException("EmptyResultDataAccessException was thrown for updateError");
//				}
//			}else{
//				throw new RuntimeException("nothing was inserted for updateError");
//			}
//		}else{
//			params.addValue("id", error.getId());
//			params.addValue("wc", DB_TIMESTAMP);
//			getNamedParameterJdbcTemplate().update("UPDATE errors SET data = :d, status = :s, headers = :h, configuration = :c, path = :p, state = :state, other1 = :o1, other2 = :o2, other3 = :o3, other4 = :o4, other5 = :o5, whoChangedId = :wcid, whenChanged = :wc WHERE id = :id", params);
//			try{
//				return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM errors WHERE id = :id", params, new BeanPropertyRowMapper<ErrorMtc>(ErrorMtc.class));
//			}catch(EmptyResultDataAccessException e){
//				throw new RuntimeException("EmptyResultDataAccessException was thrown for updateError");
//			}
//		}
//	}
//
//	@Override
//	public ErrorMtc getErrorById(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		try{
//			return getNamedParameterJdbcTemplate().queryForObject("SELECT * FROM errors WHERE id = :id", params, new BeanPropertyRowMapper<ErrorMtc>(ErrorMtc.class));
//		}catch(EmptyResultDataAccessException e){
//			throw new RuntimeException("EmptyResultDataAccessException was thrown for getErrorById");
//		}
//	}
//
//	@Override
//	public List<ErrorMtc> deleteErrorById(int id) {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		params.addValue("id", id);
//		getNamedParameterJdbcTemplate().update("DELETE FROM errors WHERE id = :id", params);
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM errors ORDER BY id desc", params, new BeanPropertyRowMapper<ErrorMtc>(ErrorMtc.class));
//	}
//
//	@Override
//	public List<ErrorMtc> getErrors() {
//		MapSqlParameterSource params = new MapSqlParameterSource();
//		return getNamedParameterJdbcTemplate().query("SELECT * FROM errors ORDER BY id desc", params, new BeanPropertyRowMapper<ErrorMtc>(ErrorMtc.class));
//	}
}