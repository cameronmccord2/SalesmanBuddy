package com.salesmanBuddy.model;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class StockNumbers implements ResultSetParser<StockNumbers> {
	
	protected Integer id;
	protected Integer dealershipId;
	protected String stockNumber;
	protected Integer status;
	protected Date created;
	protected Integer createdBy;
	protected Date soldOn;
	protected Integer soldBy;
	
	@Override
	public List<StockNumbers> parseResultSetAll(ResultSet resultSet) throws SQLException {
		List<StockNumbers> results = new ArrayList<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public Set<StockNumbers> parseResultSetAllSet(ResultSet resultSet) throws SQLException {
		Set<StockNumbers> results = new HashSet<>();
		while(resultSet.next())
			results.add(this.parseResultSetStepThrough(resultSet));
		resultSet.close();
		return results;
	}

	@Override
	public StockNumbers parseResultSetOneRow(ResultSet resultSet) throws SQLException {
		StockNumbers result = null;
		while(resultSet.next())
			result = this.parseResultSetStepThrough(resultSet);
		resultSet.close();
		return result;
	}

	@Override
	public StockNumbers parseResultSetStepThrough(ResultSet resultSet) throws SQLException {
		return this.parseResultSetStepThrough(resultSet);
	}

	@Override
	public StockNumbers parseResultSetStepThrough(ResultSet resultSet, String prefix) throws SQLException {
		StockNumbers response = new StockNumbers();
		response.setId(resultSet.getInt(prefix + "id"));
		response.setCreated(resultSet.getDate(prefix + "created"));
		response.setDealershipId(resultSet.getInt(prefix + "dealershipId"));
		response.setStockNumber(resultSet.getString(prefix + "stockNumber"));
		response.setStatus(resultSet.getInt(prefix + "status"));
		response.setCreatedBy(resultSet.getInt(prefix + "createdBy"));
		response.setSoldOn(resultSet.getDate(prefix + "soldOn"));
		response.setSoldBy(resultSet.getInt(prefix + "soldBy"));
		return response;
	}
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getDealershipId() {
		return dealershipId;
	}
	public void setDealershipId(Integer dealershipId) {
		this.dealershipId = dealershipId;
	}
	public String getStockNumber() {
		return stockNumber;
	}
	public void setStockNumber(String stockNumber) {
		this.stockNumber = stockNumber;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getCreated() {
		return created;
	}
	public void setCreated(Date created) {
		this.created = created;
	}
	public Integer getCreatedBy() {
		return createdBy;
	}
	public void setCreatedBy(Integer createdBy) {
		this.createdBy = createdBy;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((created == null) ? 0 : created.hashCode());
		result = prime * result
				+ ((createdBy == null) ? 0 : createdBy.hashCode());
		result = prime * result
				+ ((dealershipId == null) ? 0 : dealershipId.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((soldBy == null) ? 0 : soldBy.hashCode());
		result = prime * result + ((soldOn == null) ? 0 : soldOn.hashCode());
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		result = prime * result
				+ ((stockNumber == null) ? 0 : stockNumber.hashCode());
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
		StockNumbers other = (StockNumbers) obj;
		if (created == null) {
			if (other.created != null)
				return false;
		} else if (!created.equals(other.created))
			return false;
		if (createdBy == null) {
			if (other.createdBy != null)
				return false;
		} else if (!createdBy.equals(other.createdBy))
			return false;
		if (dealershipId == null) {
			if (other.dealershipId != null)
				return false;
		} else if (!dealershipId.equals(other.dealershipId))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (soldBy == null) {
			if (other.soldBy != null)
				return false;
		} else if (!soldBy.equals(other.soldBy))
			return false;
		if (soldOn == null) {
			if (other.soldOn != null)
				return false;
		} else if (!soldOn.equals(other.soldOn))
			return false;
		if (status == null) {
			if (other.status != null)
				return false;
		} else if (!status.equals(other.status))
			return false;
		if (stockNumber == null) {
			if (other.stockNumber != null)
				return false;
		} else if (!stockNumber.equals(other.stockNumber))
			return false;
		return true;
	}
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("StockNumbers [id=");
		builder.append(id);
		builder.append(", dealershipId=");
		builder.append(dealershipId);
		builder.append(", stockNumber=");
		builder.append(stockNumber);
		builder.append(", status=");
		builder.append(status);
		builder.append(", created=");
		builder.append(created);
		builder.append(", createdBy=");
		builder.append(createdBy);
		builder.append(", soldOn=");
		builder.append(soldOn);
		builder.append(", soldBy=");
		builder.append(soldBy);
		builder.append("]");
		return builder.toString();
	}

	public Date getSoldOn() {
		return soldOn;
	}

	public void setSoldOn(Date soldOn) {
		this.soldOn = soldOn;
	}

	public Integer getSoldBy() {
		return soldBy;
	}

	public void setSoldBy(Integer soldBy) {
		this.soldBy = soldBy;
	}
}





























