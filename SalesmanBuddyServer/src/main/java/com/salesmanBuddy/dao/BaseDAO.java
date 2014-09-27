package com.salesmanBuddy.dao;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
	
	private final int BATCH_SIZE = 100000;
	private SecureRandom random = new SecureRandom();
//	static Logger log = Logger.getLogger("log.dao");
//	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	public BaseDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			this.dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
	}

	public String randomAlphaNumericOfLength(Integer length){
		switch(length.intValue()){
		case 15:
			int tries = 0;
			while(true){
				String s = new BigInteger(130, this.random).toString(32);
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
	
	
	
	
	
	
	
	// SQL Gets
	
	
	// SQL
	protected <U extends ResultSetParser<U>> U getRow(String sql, Class<U> c) throws NoSqlResultsException {
		List<U> list = this.getList(sql, c);
		if(list == null || list.size() == 0)
			throw new NoSqlResultsException("None for sql" + sql + ", class: " + c.getName());
		return list.get(0);
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
	protected <U> U getRowOneColumn(String sql, Class<U> c, String columnToReturn, Object... args) throws NoSqlResultsException {
		List<U> results = new ArrayList<>();
		this.getRowsInCollectionOneColumnForSql(sql, c, columnToReturn, results, args);
		if(results == null || results.size() == 0)
			throw new NoSqlResultsException("None for getRowOneColumn, sql: " + sql + ", class: " + c.getClass().toString() + ", columnToReturn: " + columnToReturn + ", args: " + args.toString());
		return results.get(0);
	}
	// SQL Multiple Params
	protected <U extends ResultSetParser<U>> U getRow(String sql, Class<U> c, Object... args) throws NoSqlResultsException {
		List<U> list = new ArrayList<>();
		this.getRowsInCollectionForSql(sql, c, list, args);
		if(list == null || list.size() == 0)
			throw new NoSqlResultsException("None for sql" + sql + ", class: " + c.getName());
		return list.get(0);
	}
	protected <U extends ResultSetParser<U>> List<U> getList(String sql, Class<U> c, Object... args) {
		List<U> results = new ArrayList<>();
		this.getRowsInCollectionForSql(sql, c, results);
		return results;
	}
	protected <U extends ResultSetParser<U>> Set<U> getSet(String sql, Class<U> c, Object... args) {
		Set<U> results = new HashSet<>();
		this.getRowsInCollectionForSql(sql, c, results);
		return results;
	}
	protected <U> List<U> getListOneColumn(String sql, Class<U> c, String columnToReturn, Object... args) {
		List<U> results = new ArrayList<>();
		this.getRowsInCollectionOneColumnForSql(sql, c, columnToReturn, results, args);
		return results;
	}
	protected Integer getCount(String sql){
		return this.getCount(sql, (Object[])null);
	}
	protected Integer getCount(String sql, Object... args){
		Integer count = 0;
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			
			int index = 1;
			for (Object param : args) {
				if(param.getClass().equals(String.class))
					statement.setString(index++, (String)param);
				else if(param.getClass().equals(Integer.class))
					statement.setInt(index++, (Integer)param);
				else
					throw new RuntimeException("Unsupported datatype sent in getRows with variable number of params, sql: " + sql + ", params: " + args.toString());
			}
			
			ResultSet resultSet = statement.executeQuery();
			count = resultSet.getInt("count");
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return count;
	}
	
	private <U extends ResultSetParser<U>> void getRowsInCollectionForSql(String sql, Class<U> c, Collection<U> results, Object... args) {
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			
			int index = 1;
			for (Object param : args) {
				if(param.getClass().equals(String.class))
					statement.setString(index++, (String)param);
				else if(param.getClass().equals(Integer.class))
					statement.setInt(index++, (Integer)param);
				else
					throw new RuntimeException("Unsupported datatype sent in getRows with variable number of params, sql: " + sql + ", class: " + c.getClass().toString() + ", params: " + args.toString());
			}
			
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
	
	@SuppressWarnings("unchecked")
	private <U> void getRowsInCollectionOneColumnForSql(String sql, Class<U> c, String columnToReturn, Collection<U> results, Object... args) {
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			
			int index = 1;
			for (Object param : args) {
				if(param.getClass().equals(String.class))
					statement.setString(index++, (String)param);
				else if(param.getClass().equals(Integer.class))
					statement.setInt(index++, (Integer)param);
				else
					throw new RuntimeException("Unsupported datatype sent in getRows with variable number of params, sql: " + sql + ", class: " + c.getClass().toString() + ", params: " + args.toString());
			}
			
			ResultSet resultSet = statement.executeQuery();
			while(resultSet.next()){
				if(c.getClass().equals(String.class))
					results.add((U)resultSet.getString(columnToReturn));
				else if(c.getClass().equals(Integer.class))
					results.add((U)new Integer(resultSet.getInt(columnToReturn)));
				else
					throw new RuntimeException("getRowsInCollectionOneColumnForSql is not implemented to get type: " + c.getClass().toString() + ", column: " + columnToReturn + ", args: " + args.toString() + ", sql: " + sql);
			}
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
	}
	
	
	
	// SQL Insert
	protected Integer insertRow(String sql, String idColumn) throws NoSqlResultsException {
		return this.insertRow(sql, idColumn, (Object[])null);
	}
	protected Integer insertRow(String sql, String idColumn, Object... args) throws NoSqlResultsException {
		List<Integer> generatedIds = new ArrayList<>();
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
			int index = 1;
			for (Object param : args) {
				if(param.getClass().equals(String.class))
					statement.setString(index++, (String)param);
				else if(param.getClass().equals(Integer.class))
					statement.setInt(index++, (Integer)param);
				else
					throw new RuntimeException("Unsupported datatype sent in insertRows with variable number of params, sql: " + sql + ", params: " + args.toString());
			}
			statement.execute();
			
			ResultSet generatedKeys = statement.getGeneratedKeys();
			while(generatedKeys.next())
				generatedIds.add((int) generatedKeys.getLong(idColumn));
			generatedKeys.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle.getLocalizedMessage() + ", idColumn: " + idColumn + ", args: " + args.toString() + ", sql: " + sql);
		}
		if(generatedIds.size() == 0)
			throw new NoSqlResultsException("Nothing was inserted for sql: " + sql + ", idColumn: " + idColumn + ", args: " + args.toString());
		return generatedIds.get(0);
	}
//	protected List<Integer> insertRows(String sql, String idColumn) {
//		
//	}
//	protected List<Integer> insertRows(String sql, String idColumn, List<List<Object>> args) {
//
//	}
	
	
	// SQL Update
	protected Integer updateRow(String sql){
		return this.updateRow(sql, (Object[])null);
	}
	protected Integer updateRow(String sql, Object... args) {
		int i = 0;
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			int index = 1;
			for (Object param : args) {
				if(param.getClass().equals(String.class))
					statement.setString(index++, (String)param);
				else if(param.getClass().equals(Integer.class))
					statement.setInt(index++, (Integer)param);
				else
					throw new RuntimeException("Unsupported datatype sent in updateRow with variable number of params, sql: " + sql + ", params: " + args.toString());
			}
			
			i = statement.executeUpdate();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
	protected Integer updateRowBatch(String sql, List<List<Object>> args){
		int i = 0;
		try(Connection connection = this.dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			int count = 0;
			for (List<Object> params : args) {
				int index = 1;
				for (Object param : params) {
					if(param.getClass().equals(String.class))
						statement.setString(index++, (String)param);
					else if(param.getClass().equals(Integer.class))
						statement.setInt(index++, (Integer)param);
					else
						throw new RuntimeException("Unsupported datatype sent in updateRow with variable number of params, sql: " + sql + ", params: " + args.toString());
				}
				statement.addBatch();
				
				if(++count % this.BATCH_SIZE == 0)
					statement.executeBatch();
				if(count % 1000000 == 0)
					connection.commit();
			}
			
			if(count % this.BATCH_SIZE != 0)
				statement.executeBatch();
			
			connection.commit();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return i;
	}
}




































