package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LicensesListElement implements ResultSetParser<LicensesListElement>{
	// licenses
	protected Integer id;
    protected Date created;
    protected Integer stateId;
    //custom here
    protected List<QuestionsAndAnswers> qaas;
    
    @Override
	public List<LicensesListElement> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<LicensesListElement> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<LicensesListElement> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<LicensesListElement> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public LicensesListElement parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		LicensesListElement result = null;
		if(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public LicensesListElement parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet, "");
	}

	@Override
	public LicensesListElement parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		LicensesListElement response = new LicensesListElement();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setStateId(resultSet.getInt(prefix + "stateId"));
		return response;
	}
	
	public static String getStockNumberForLicensesListElement(LicensesListElement lle) {
//		return "IMPLEMENT_GETTING_STOCK_NUMBER";
		for(QuestionsAndAnswers qaa : lle.getQaas()){
			if(qaa.getQuestion().getQuestionTextEnglish().equalsIgnoreCase("Stock Number"))
				return qaa.getAnswer().getAnswerText();
		}
		return "No stock number found";
	}
	
	public String getReportString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.toString());
		sb.append("\n");
		return sb.toString();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Integer getStateId() {
		return stateId;
	}

	public void setStateId(Integer stateId) {
		this.stateId = stateId;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("LicensesListElement [id=");
		builder.append(id);
		builder.append(", created=");
		builder.append(created);
		builder.append(", stateId=");
		builder.append(stateId);
		builder.append(", qaas=");
		builder.append(qaas);
		builder.append("]");
		return builder.toString();
	}

	public List<QuestionsAndAnswers> getQaas() {
		return qaas;
	}

	public void setQaas(List<QuestionsAndAnswers> qaas) {
		this.qaas = qaas;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((qaas == null) ? 0 : qaas.hashCode());
		result = prime * result + ((stateId == null) ? 0 : stateId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LicensesListElement other = (LicensesListElement) obj;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (qaas == null) {
			if (other.qaas != null)
				return false;
		} else if (!qaas.equals(other.qaas))
			return false;
		if (stateId == null) {
			if (other.stateId != null)
				return false;
		} else if (!stateId.equals(other.stateId))
			return false;
		return true;
	}
}























