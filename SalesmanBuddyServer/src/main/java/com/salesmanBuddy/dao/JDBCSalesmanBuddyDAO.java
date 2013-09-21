package com.salesmanBuddy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.States;

public class JDBCSalesmanBuddyDAO implements SalesmanBuddyDAO{
	
	static Log log = LogFactory.getLog(JDBCSalesmanBuddyDAO.class);
	protected DataSource dataSource;
	
	
	
	public JDBCSalesmanBuddyDAO(){
		try{
			Context initContext = new InitialContext();
			Context envContext = (Context)initContext.lookup("java:/comp/env");
			dataSource = (DataSource)envContext.lookup("jdbc/SalesmanBuddyDB");
		}catch(NamingException ne){
			throw new RuntimeException(ne);
		}
	}

	@Override
	public String getString() {
		return "From the dao";
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<States> getAllStates() {
		String sql = "SELECT * FROM states";
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
}












































