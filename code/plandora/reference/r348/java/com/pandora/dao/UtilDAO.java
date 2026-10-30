package com.pandora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.pandora.exception.DataAccessException;

/**
 * This class contain all methods to handle data related with User entity into data base.
 */
public class UtilDAO extends DataAccess {


    /**
     * @return datetime from database
     * @throws DataAccessException
     */
    public Timestamp getActualDateFromDatabase() throws DataAccessException{
		Timestamp response= null;
		ResultSet rs = null;
		PreparedStatement pstmt = null; 
		Connection c = null;
		try {
			c = getConnection();
			pstmt = c.prepareStatement("select current_timestamp as actualDate");
			rs = pstmt.executeQuery();
			if (rs.next()){
				response = rs.getTimestamp("actualDate");
			} 

		} catch (final SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(rs, pstmt);
			super.closeConnection(c);
		}	 
		return response;
    }

}
