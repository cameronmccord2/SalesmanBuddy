package com.salesmanBuddy.dao;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.salesmanBuddy.exceptions.MalformedSBEmailException;
import com.salesmanBuddy.model.SBEmail;

public class BaseDAO {
	
	private SecureRandom random = new SecureRandom();
//	static Logger log = Logger.getLogger("log.dao");
//	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	public BaseDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
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
	
	public String getString() {
		return "From the dao";
	}
	
	protected int parseFirstInt(ResultSet generatedKeys, String key) {
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
	
	public static void sendErrorToMe(String errorString){
		List<String> to = new ArrayList<>();
		to.add("cameronmccord2@gmail.com");
		try {
			SBEmail.newPlainTextEmail("logging@salesmanbuddy.com", to, "error", errorString, true).send();
		} catch (MalformedSBEmailException e) {
			e.printStackTrace();
			System.out.println(new StringBuilder().append("BIGERROR, there was an error sending an error email, message: ").append(e.getLocalizedMessage()));
		}
	}
	
	protected DateTime getNowTime(){
		return new DateTime(DateTimeZone.UTC);
	}
}




































