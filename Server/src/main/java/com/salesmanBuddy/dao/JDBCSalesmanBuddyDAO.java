package com.salesmanBuddy.dao;

import java.util.ArrayList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

	@Override
	public ArrayList<States> getAllStates() {
//		String sql = "SELECT * FROM states";
//		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
//			ResultSet resultSet = statement.executeQuery();
//			if(resultSet.next())
//				return 
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}
		return new ArrayList<States>();
	}

}












































