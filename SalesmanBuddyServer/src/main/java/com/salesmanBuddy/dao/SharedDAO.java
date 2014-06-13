package com.salesmanBuddy.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.salesmanBuddy.model.Languages;

public class SharedDAO extends AWSDAO {
	
	public SharedDAO(){
		super();
	}
	
	public List<Languages> getAllLanguages(int onlyMtcTaught) {
		String sql = "SELECT * FROM languages";
		if(onlyMtcTaught == 1)
			sql = "SELECT * FROM languages WHERE mtcTaught = 1";
		List<Languages> results = new ArrayList<>();
		try(Connection connection = dataSource.getConnection(); PreparedStatement statement = connection.prepareStatement(sql)){
			ResultSet resultSet = statement.executeQuery();
			results = Languages.parseResultSet(resultSet);
			resultSet.close();
			
		}catch(SQLException sqle){
			throw new RuntimeException(sqle);
		}
		return results;
	}
}
