package com.salesmanBuddy.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class MaxValue {
	protected Integer maxValue;
	
	public static Integer parseResultSetForMaxValue(ResultSet resultSet){
		Integer maxValue = 0;
    	try{
			while(resultSet.next()){
				maxValue = resultSet.getInt("maxValue");
				
			}
    	}catch(SQLException e){
    		throw new RuntimeException(e);
    	}
    	return maxValue;
    }

	public Integer getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(Integer maxValue) {
		this.maxValue = maxValue;
	}
}
