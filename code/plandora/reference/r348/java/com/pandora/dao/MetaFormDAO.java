package com.pandora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import com.pandora.MetaFormTO;
import com.pandora.TransferObject;
import com.pandora.exception.DataAccessException;

/**
 * 
 */
public class MetaFormDAO extends DataAccess {

    
    public Vector<MetaFormTO> getList(Connection c) throws DataAccessException {
		final Vector<MetaFormTO> response= new Vector<MetaFormTO>();
		ResultSet rs = null;
		PreparedStatement pstmt = null; 
		try {
			pstmt = c.prepareStatement("select id, name, viewable_cols, grid_row_num, " +
									   "filter_col_id, js_before_save, js_after_save, js_after_load, save_batch, constraint_edit_delete " +
									   "from meta_form");
			rs = pstmt.executeQuery();
			while (rs.next()) {
			    final MetaFormTO to = this.populateObjectByResultSet(rs);
			    response.addElement(to);
			}
						
		} catch (final SQLException e) {
			throw new DataAccessException(e);
		} finally {
			super.closeStatement(rs, pstmt);
		}
		return response;
    }

    
    public TransferObject getObject(TransferObject to, Connection c)  throws DataAccessException {
        MetaFormTO response= null;
		ResultSet rs = null;
		PreparedStatement pstmt = null; 
		try {
		    final MetaFormTO mto = (MetaFormTO)to;
			pstmt = c.prepareStatement("select id, name, viewable_cols, grid_row_num, " +
									   "filter_col_id, js_before_save, js_after_save, js_after_load, save_batch, constraint_edit_delete " +
									   "from meta_form where id=?");
			pstmt.setString(1, mto.getId());
			rs = pstmt.executeQuery();
			if (rs.next()){
			    response = this.populateObjectByResultSet(rs);
			} 
						
		} catch (final SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(rs, pstmt);
		}	 
		return response;
    }
    
    public void update(TransferObject to, Connection c)  throws DataAccessException {
		PreparedStatement pstmt = null; 
		try {
		    
		    final MetaFormTO mfto = (MetaFormTO)to;
		    pstmt = c.prepareStatement("update meta_form set name=?, viewable_cols=?, grid_row_num=?, " +
									   "filter_col_id=?, js_before_save=?, js_after_save=?, js_after_load=?, save_batch=?, constraint_edit_delete = ? where id=?");
			pstmt.setString(1, mfto.getName());
			pstmt.setString(2, mfto.getViewableCols());
			pstmt.setInt(3, mfto.getGridNumber().intValue());
			pstmt.setString(4, mfto.getFilterColId());
			pstmt.setString(5, mfto.getJsBeforeSave());
			pstmt.setString(6, mfto.getJsAfterSave());
			pstmt.setString(7, mfto.getJsAfterLoad());
			pstmt.setBoolean(8, mfto.getSaveBatch());
			pstmt.setBoolean(9, mfto.getConstraintEditDelete());
			pstmt.setString(10, mfto.getId());
			pstmt.executeUpdate();
			
		} catch (final SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}        
    }
    

    public void insert(TransferObject to, Connection c) throws DataAccessException {
		PreparedStatement pstmt = null;
		try {
		    
		    final MetaFormTO mfto = (MetaFormTO)to;
		    mfto.setId(this.getNewId());
		    pstmt = c.prepareStatement("insert into meta_form (id, name, viewable_cols, grid_row_num, " +
									   "filter_col_id, js_before_save, js_after_save, js_after_load, save_batch, constraint_edit_delete) values (?,?,?,?,?,?,?,?,?,?)");
			pstmt.setString(1, mfto.getId());
			pstmt.setString(2, mfto.getName());
			pstmt.setString(3, mfto.getViewableCols());
			pstmt.setInt(4, mfto.getGridNumber().intValue());
			pstmt.setString(5, mfto.getFilterColId());
			pstmt.setString(6, mfto.getJsBeforeSave());
			pstmt.setString(7, mfto.getJsAfterSave());
			pstmt.setString(8, mfto.getJsAfterLoad());
			pstmt.setBoolean(9, mfto.getSaveBatch());
			pstmt.setBoolean(10, mfto.getConstraintEditDelete());
			pstmt.executeUpdate();

		} catch (final SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}               
    }
    

    public void remove(TransferObject to, Connection c)  throws DataAccessException {
		PreparedStatement pstmt = null; 
		try {
		    final MetaFormTO mfto = (MetaFormTO)to;
			pstmt = c.prepareStatement("delete from meta_form where id=?");
			pstmt.setString(1, mfto.getId());
			pstmt.executeUpdate();
		} catch (final SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}        
    }
    
    
    private MetaFormTO populateObjectByResultSet(ResultSet rs) throws DataAccessException{
        final String id = getString(rs, "id");
        final MetaFormTO response = new MetaFormTO(id);
        response.setName(getString(rs, "name"));
        response.setViewableCols(getString(rs, "viewable_cols"));
        response.setFilterColId(getString(rs, "filter_col_id"));
        response.setJsAfterLoad(getString(rs, "js_after_load"));
        response.setJsAfterSave(getString(rs, "js_after_save"));
        response.setJsBeforeSave(getString(rs, "js_before_save"));
        response.setSaveBatch(getBoolean(rs, "save_batch").booleanValue());
        response.setConstraintEditDelete(getBoolean(rs, "constraint_edit_delete").booleanValue());
        final Integer val = getInteger(rs, "grid_row_num");
        if (val==null) {
        	response.setGridNumber(new Integer("6"));
        } else {
        	response.setGridNumber(val);	
        }
        return response;
    }
    
}
