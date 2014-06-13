package com.salesmanBuddy.model;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
public interface ResultSetParser<T>{

	
	 /**
 	 * Parses the result set without you needing to do resultSet.next(). This will close the result set.
 	 *
 	 * @param resultSet the result set
 	 * @return the list< t>
	 * @throws SQLException 
 	 */
 	public abstract List<T> parseResultSetAll(ResultSet resultSet) throws SQLException;
 	
 	public abstract Set<T> parseResultSetAllSet(ResultSet resultSet) throws SQLException;

	 /**
 	 * Parses the result set to get the first row without you needing to do resultSet.next(). This will close the result set.
 	 *
 	 * @param resultSet the result set
 	 * @return the t
	 * @throws SQLException 
 	 */
 	public abstract T parseResultSetOneRow(ResultSet resultSet) throws SQLException;
	
	 /**
 	 * Parses the result set one row at a time but requires you to wrap it in resultSet.next(); This allows you to do things with each row. Will NOT close the result set.
 	 *
 	 * @param resultSet the result set
 	 * @return the t
	 * @throws SQLException 
 	 */
 	public abstract T parseResultSetStepThrough(ResultSet resultSet) throws SQLException;
 	
 	public abstract T parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException;

}











































