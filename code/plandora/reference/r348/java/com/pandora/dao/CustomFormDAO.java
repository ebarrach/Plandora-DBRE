package com.pandora.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Vector;

import com.pandora.AdditionalFieldTO;
import com.pandora.CustomFormTO;
import com.pandora.MetaFieldTO;
import com.pandora.MetaFormTO;
import com.pandora.PlanningTO;
import com.pandora.TransferObject;
import com.pandora.UserTO;
import com.pandora.exception.DataAccessException;
import com.pandora.helper.DateUtil;

/**
*/
public class CustomFormDAO extends PlanningDAO {
    
    AdditionalFieldDAO afdao = new AdditionalFieldDAO();
    
    public Vector<CustomFormTO> getRecords(String metaFormId, Timestamp iniRange) throws DataAccessException {
        Vector<CustomFormTO> response = null;
        Connection c = null;
		try {
			c = getConnection();
			response = this.getRecords(metaFormId, iniRange, c);
		} catch(Exception e) {
			throw new DataAccessException(e);
		} finally {
			this.closeConnection(c);
		}
        return response;
    }
    
    
    private Vector<CustomFormTO> getRecords(String metaFormId, Timestamp iniRange, Connection c) throws DataAccessException {
		Vector<CustomFormTO> response= new Vector<CustomFormTO>();
		ResultSet rs = null;
		PreparedStatement pstmt = null;
		try {
			pstmt = c.prepareStatement("select cf.id, cf.meta_form_id, cf.creation_user_id, cf.modification_user_id,  " +
									   "af.meta_field_id, af.value, af.date_value, mf.domain, mf.name, mf.type, af.numeric_value " +
									   "from custom_form cf " +
									   "inner join planning pl on (pl.id = cf.id and pl.creation_date > ?) " +
									   "inner join additional_field af on (af.planning_id = pl.id) " +
									   "inner join meta_field mf on (af.meta_field_id = mf.id) " +
									   "where cf.meta_form_id=? and mf.final_date is null " +
									   "order by cf.id, mf.field_order");
			pstmt.setTimestamp(1, iniRange);
			pstmt.setString(2, metaFormId);
			rs = pstmt.executeQuery();	
			
			CustomFormTO lastRow = null;
			while (rs.next()){
			    CustomFormTO cfto = this.populateObjectByResultSet(rs, iniRange, true, c);
				
				if (lastRow==null || !lastRow.getId().equals(cfto.getId()) ) {
					lastRow = cfto;
					response.addElement(lastRow);	
				}
				
			    //get the additional fields
				Vector<AdditionalFieldTO> newList = lastRow.getAdditionalFields();
				if (newList==null) {
					newList = new Vector<AdditionalFieldTO>();
					lastRow.setAdditionalFields(newList);
				}
 
				AdditionalFieldTO afto = new AdditionalFieldTO();
				final PlanningTO pto = new PlanningTO(cfto.getId());

				final MetaFieldTO mfto = new MetaFieldTO();
				mfto.setId(getString(rs, "meta_field_id"));
				mfto.setName(getString(rs, "name"));
				mfto.setDomain(getString(rs, "domain"));
				mfto.setType(getInteger(rs, "type"));
				afto.setMetaField(mfto);
				afto.setPlanning(pto);
				afto.setValue(getString(rs, "value"));
				afto.setDateValue(getTimestamp(rs, "date_value"));
				afto.setNumericValue(getFloat(rs, "numeric_value"));
				newList.addElement(afto);

			}
						
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(rs, pstmt);
		}	 
		return response;
    }
    
    
    public TransferObject getObject(TransferObject to, Connection c) throws DataAccessException {
    	CustomFormTO response= null;
		ResultSet rs = null;
		PreparedStatement pstmt = null; 
		try {
			CustomFormTO filter = (CustomFormTO)to;
		    pstmt = c.prepareStatement("select id, meta_form_id, creation_user_id, modification_user_id from custom_form where id=?");		
			pstmt.setString(1, filter.getId());
			rs = pstmt.executeQuery();						
			if (rs.next()){
				response = this.populateObjectByResultSet(rs, null, false, c);
			} 
						
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(rs, pstmt);
		}	 
		return response;
    }
    
    public void update(TransferObject to, Connection c)  throws DataAccessException {
		PreparedStatement pstmt = null; 
		try {
		    
			CustomFormTO cto = (CustomFormTO)to;
		    super.update(cto, c);
		    
		    pstmt = c.prepareStatement("update custom_form set meta_form_id=?, modification_user_id=? where id=?");
			pstmt.setString(1, cto.getMetaForm().getId());
			pstmt.setString(2, cto.getModificationUser().getId());
			pstmt.setString(3, cto.getId());
			pstmt.executeUpdate();
								
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}        
    }
    
    
    public void insert(TransferObject to, String newId, Connection c) throws DataAccessException {
		PreparedStatement pstmt = null;
		try {
		    
			CustomFormTO cto = (CustomFormTO)to;
			cto.setId(newId);
			cto.setCreationDate(DateUtil.getNow());

		    //insert data into parent entity (PlanningDAO)
		    super.insert(cto, c);
		    
		    pstmt = c.prepareStatement("insert into custom_form (id, meta_form_id, creation_user_id) values (?, ?, ?)");
			pstmt.setString(1, cto.getId());
			pstmt.setString(2, cto.getMetaForm().getId());
			pstmt.setString(3, cto.getCreationUser().getId());
			pstmt.executeUpdate();
						
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}     	
    }

    public void insert(TransferObject to, Connection c) throws DataAccessException {
		String newId = this.getNewId();
		this.insert(to, newId, c);
    }
    
    public void insertInBatch(CustomFormTO to, ArrayList<ArrayList<AdditionalFieldTO>> aftoList) throws DataAccessException {
		PreparedStatement pstmt = null;
		Connection c = null;
		try {
			c = new DataAccess().getConnection();
			for (ArrayList<AdditionalFieldTO> arrayList : aftoList) {
				String newId = this.getNewId();
				
				CustomFormTO cto = (CustomFormTO)to;
				cto.setId(newId);
				cto.setCreationDate(DateUtil.getNow());
				
				 //insert data into parent entity (PlanningDAO)
			    super.insert(cto, c);
				
				//insert Additional Fields
				for (AdditionalFieldTO additionalFieldTO : arrayList) {
					additionalFieldTO.setPlanning(new PlanningTO(newId));
					afdao.insert(additionalFieldTO, c);
				}
	
			    pstmt = c.prepareStatement("insert into custom_form (id, meta_form_id, creation_user_id) values (?, ?, ?)");
				pstmt.setString(1, cto.getId());
				pstmt.setString(2, cto.getMetaForm().getId());
				pstmt.setString(3, cto.getCreationUser().getId());
				pstmt.executeUpdate();
			}
						
		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
			super.closeConnection(c);
		}     	
    }

    public void remove(TransferObject to, Connection c)  throws DataAccessException {
		PreparedStatement pstmt = null;
		AdditionalFieldDAO adao = new AdditionalFieldDAO();
		
		try {
		    
			CustomFormTO cto = (CustomFormTO)to;
			pstmt = c.prepareStatement("delete from custom_form where id=?");
			pstmt.setString(1, cto.getId());
			pstmt.executeUpdate();
			
			//remove data related to the meta_field and current meta_form
			adao.removeByPlanning(new PlanningTO(cto.getId()), false, c);
			

		} catch (SQLException e) {
			throw new DataAccessException(e);
		}finally{
			super.closeStatement(null, pstmt);
		}        
    }
    
    
    private CustomFormTO populateObjectByResultSet(ResultSet rs, Timestamp iniRange, boolean filterCols, Connection c) throws DataAccessException{
    	MetaFormDAO mfdao = new MetaFormDAO();
    	UserDAO userDao = new UserDAO();
        CustomFormTO response = new CustomFormTO();
        response.setId(getString(rs, "id"));
        
        MetaFormTO mfto = new MetaFormTO(getString(rs, "meta_form_id"));
        mfto = (MetaFormTO) mfdao.getObject(mfto, c);
        response.setMetaForm(mfto);
        
        UserTO uto1 = new UserTO(getString(rs, "creation_user_id"));
        uto1 = (UserTO) userDao.getObject(uto1);
        response.setCreationUser(uto1);
        
        UserTO uto2 = new UserTO(getString(rs, "modification_user_id"));
        uto2 = (UserTO) userDao.getObject(uto2);
        response.setModificationUser(uto2);
    	        
	    //get the additional fields
        Vector<AdditionalFieldTO> list = afdao.getListByPlanning(response, iniRange, c);
    	if (list!=null && list.size()>0) {
    		
    		Vector<AdditionalFieldTO> newList = new Vector<AdditionalFieldTO>();
    		if (mfto.getViewableCols()!=null && filterCols && !mfto.getViewableCols().trim().equals("")) {
    			
    			String[] viewable = mfto.getViewableCols().split(";");
    			for(int i=0; i<list.size(); i++) {
    				if (this.contain(i, viewable)) {
    					newList.add(list.elementAt(i));
    				}
				}
    		} else {
    			newList = list;
    		}
    		
    	    response.setAdditionalFields(newList);
    	} else {
    		response = null;
    	}
    	
        return response;
    }


	private boolean contain(int i, String[] viewable) {
		boolean response = false;
		if (viewable!=null) {
			for (String col : viewable) {
				try {
					if (Integer.parseInt(col.trim())==(i+1)){
						response = true;
						break;
					}					
				}catch(Exception e) {
					e.printStackTrace();
					response = false;
				}
			}
		}
		return response;
	}

}
