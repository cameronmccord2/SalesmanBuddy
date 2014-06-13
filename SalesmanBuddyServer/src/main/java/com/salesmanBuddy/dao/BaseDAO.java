package com.salesmanBuddy.dao;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import com.salesmanBuddy.exceptions.MalformedSBEmailException;
import com.salesmanBuddy.exceptions.NoSqlResultsException;
import com.salesmanBuddy.model.ResultSetParser;
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
	
	protected <U extends ResultSetParser<U>> U getRow(String sql, Class<U> c) throws NoSqlResultsException {
		List<U> list = this.getList(sql, c);
		if(list == null || list.size() == 0)
			throw new NoSqlResultsException("None for sql" + sql + ", class: " + c.getName());
		return list.get(0);
	}

	protected <U extends ResultSetParser<U>> Set<U> getSet(String tableName, String orderColumn, String orderDirection, Class<U> c) {
		Set<U> list = new HashSet<>();
		this.getRowsForColumnAndValue(tableName, "", "", orderColumn, orderDirection, c, list);
		return list;
	}
	
	protected <U extends ResultSetParser<U>> Set<U> getSet(String tableName, String columnName, Integer columnValue, String orderColumn, String orderDirection, Class<U> c) {
		Set<U> list = new HashSet<>();
		this.getRowsForColumnAndValue(tableName, columnName, columnValue, orderColumn, orderDirection, c, list);
		return list;
	}
	
	protected <U extends ResultSetParser<U>> List<U> getList(String tableName, String orderColumn, String orderDirection, Class<U> c) {
		List<U> list = new ArrayList<>();
		this.getRowsForColumnAndValue(tableName, "", "", orderColumn, orderDirection, c, list);
		return list;
	}
	
	protected <U extends ResultSetParser<U>> List<U> getList(String tableName, String columnName, Integer columnValue, String orderColumn, String orderDirection, Class<U> c) {
		List<U> list = new ArrayList<>();
		this.getRowsForColumnAndValue(tableName, columnName, columnValue, orderColumn, orderDirection, c, list);
		return list;
	}
	
	protected <U extends ResultSetParser<U>> U getRow(String tableName, String columnName, String columnValue, Class<U> c) throws NoSqlResultsException {
		List<U> list = this.getList(tableName, columnName, columnValue, c);
		if(list == null || list.size() == 0)
			throw new NoSqlResultsException("None for tablename: " + tableName + ", columnName: " + columnName + ", columnValue: " + columnValue + ", class: " + c.getName());
		return list.get(0);
	}
	
	
	protected <U extends ResultSetParser<U>> U getRow(String tableName, String columnName, Integer columnValue, Class<U> c) throws NoSqlResultsException {
		List<U> list = this.getList(tableName, columnName, columnValue, null, null, c);
		if(list == null || list.size() == 0)
			throw new NoSqlResultsException("None for tablename: " + tableName + ", columnName: " + columnName + ", columnValue: " + columnValue + ", class: " + c.getName());
		return list.get(0);
	}
	
	private <U extends ResultSetParser<U>> void getRowsForColumnAndValue(String tableName, String columnName, Object columnValue, String orderColumn, String orderDirection, Class<U> c, Collection<U> results) {
		StringBuilder sb = new StringBuilder();
		sb.append("SELECT * FROM ").append(tableName);
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sb.toString())){
			int index = 1;
			if(columnValue.getClass().equals(String.class)){
				if(columnName != null && columnValue != null && columnName.length() != 0 && ((String)columnValue).length() != 0){
					sb.append(" WHERE ").append(columnName).append(" = ? ");
					statement.setString(index++, (String)columnValue);
				}
			}else if(columnValue.getClass().equals(Integer.class)){
				if(columnName != null && columnValue != null && columnName.length() != 0){
					sb.append(" WHERE ").append(columnName).append(" = ? ");
					statement.setInt(index++, (Integer)columnValue);
				}
			}
			if(orderColumn != null && orderColumn.length() != 0){
				sb.append(" ORDER BY ").append(orderColumn);
				if(orderDirection != null && orderDirection.length() > 2)
					sb.append(" ").append(orderDirection);
			}
			
			ResultSet resultSet = statement.executeQuery();
			results = c.newInstance().parseResultSetAll(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected <U extends ResultSetParser<U>> List<U> getList(String sql, Class<U> c) {
		List<U> results = new ArrayList<>();
		this.getRowsInCollectionForSql(sql, c, results);
		return results;
	}
	
	protected <U extends ResultSetParser<U>> Set<U> getSet(String sql, Class<U> c) {
		Set<U> results = new HashSet<>();
		this.getRowsInCollectionForSql(sql, c, results);
		return results;
	}
	
	private <U extends ResultSetParser<U>> void getRowsInCollectionForSql(String sql, Class<U> c, Collection<U> results) {
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			
			ResultSet resultSet = statement.executeQuery();
			results.addAll(c.newInstance().parseResultSetAll(resultSet));
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		} catch (InstantiationException e) {
			throw new RuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}




































