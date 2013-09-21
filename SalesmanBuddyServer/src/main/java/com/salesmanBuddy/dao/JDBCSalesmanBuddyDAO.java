package com.salesmanBuddy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.salesmanBuddy.dao.SalesmanBuddyDAO;
import com.salesmanBuddy.model.Dealerships;
import com.salesmanBuddy.model.LicensesFromClient;
import com.salesmanBuddy.model.LicensesListElement;
import com.salesmanBuddy.model.StateQuestions;
import com.salesmanBuddy.model.StateQuestionsResponses;
import com.salesmanBuddy.model.StateQuestionsSpecifics;
import com.salesmanBuddy.model.StateQuestionsWithResponses;
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
	public ArrayList<States> getAllStates(int getInactiveToo) {
		String sql = "SELECT * FROM states WHERE status = 1";
		if(getInactiveToo > 0)
			sql = "SELECT * FROM states";
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

	@SuppressWarnings("finally")
	@Override
	public ArrayList<Dealerships> getAllDealerships() {
		String sql = "SELECT * FROM dealerships";
		ArrayList<Dealerships> results = new ArrayList<Dealerships>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			resultSet = statement.executeQuery();
			results = Dealerships.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<LicensesListElement> getAllLicensesForUserId(int userId) {
		String sql = "SELECT * FROM licenses WHERE userId = ?";
		ArrayList<LicensesListElement> results = new ArrayList<LicensesListElement>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, userId);
			resultSet = statement.executeQuery();
			results = LicensesListElement.parseResultSet(resultSet);
			
			for(int i = 0; i < results.size(); i++){
				results.get(i).setStateQuestions(this.getStateQuestionsWithResponsesForLicenseId(results.get(i).getId()));
			}
			
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
					return results;
				}
			}
		}
	}

	@Override
	public ArrayList<LicensesListElement> putLicense(LicensesFromClient licenseFromClient) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ArrayList<LicensesListElement> deleteLicense(int licenseId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean userOwnsLicenseId(int licenseId) {
		return true;
//		String sql = "SELECT * FROM licenses WHERE id = ?";
//		ArrayList<Licenses> results = new ArrayList<Licenses>();
//		PreparedStatement statement = null;
//		ResultSet resultSet = null;
//		try{
//			Connection connection = dataSource.getConnection();
//			statement = connection.prepareStatement(sql);
//			statement.setInt(1, licenseId);
//			resultSet = statement.executeQuery();
//			results = Licenses.parseResultSet(resultSet);
//		}catch(SQLException sqle){
//			throw new RuntimeException(sqle);
//		}finally{
//			try{
//				if(resultSet != null)
//					resultSet.close();
//			}catch(SQLException e){
//				throw new RuntimeException(e);
//			}finally{
//				try{
//					if(statement != null)
//						statement.close();
//				}catch(SQLException se){
//					throw new RuntimeException(se);
//				}finally{
//					for(int i = 0; i < results.size(); i++){
////						if(results[i].userId == )
//					}
//					return results;
//				}
//			}
//		}
	}

	@SuppressWarnings("finally")
	@Override
	public List<StateQuestions> getStateQuestionsForStateId(int stateId) {
		String sql = "SELECT * FROM stateQuestions WHERE stateId = ?";
		List<StateQuestions> results = new ArrayList<StateQuestions>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			resultSet = statement.executeQuery();
			results = StateQuestions.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsResponses> getStateQuestionsResponsesForLicenseId(int licenseId) {
		String sql = "SELECT * FROM stateQuestionsResponses WHERE licenseId = ?";
		ArrayList<StateQuestionsResponses> results = new ArrayList<StateQuestionsResponses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = StateQuestionsResponses.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateQuestionId(int stateQuestionId) {
		String sql = "SELECT * FROM stateQuestionsSpecifics WHERE stateQuestionId = ?";
		ArrayList<StateQuestionsSpecifics> results = new ArrayList<StateQuestionsSpecifics>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateQuestionId);
			resultSet = statement.executeQuery();
			results = StateQuestionsSpecifics.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsWithResponses> getStateQuestionsWithResponsesForLicenseId(int licenseId) {
		String sql = "SELECT * FROM (SELECT * FROM stateQuestionsResponses WHERE licenceId = ?) LEFT JOIN stateQuestionsSpecifics ON stateQuestionsResponses.stateQuestionsSpecificsId = stateQuestionsSpecifics.id";
		ArrayList<StateQuestionsWithResponses> results = new ArrayList<StateQuestionsWithResponses>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, licenseId);
			resultSet = statement.executeQuery();
			results = StateQuestionsWithResponses.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}

	@SuppressWarnings("finally")
	@Override
	public ArrayList<StateQuestionsSpecifics> getStateQuestionsSpecificsForStateId(int stateId) {
		String sql = "SELECT * FROM stateQuestionsSpecifics WHERE stateQuestionId = (SELECT id FROM stateQuestions WHERE stateId = ?)";
		ArrayList<StateQuestionsSpecifics> results = new ArrayList<StateQuestionsSpecifics>();
		PreparedStatement statement = null;
		ResultSet resultSet = null;
		try{
			Connection connection = dataSource.getConnection();
			statement = connection.prepareStatement(sql);
			statement.setInt(1, stateId);
			resultSet = statement.executeQuery();
			results = StateQuestionsSpecifics.parseResultSet(resultSet);
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
					return results;
				}
			}
		}
	}
}












































