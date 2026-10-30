package com.pandora.bus.alert;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Vector;

import com.pandora.FieldValueTO;
import com.pandora.NotificationFieldTO;
import com.pandora.RootTO;
import com.pandora.TransferObject;
import com.pandora.bus.EventBUS;
import com.pandora.delegate.DbQueryDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.helper.LogUtil;

public class ReplicateTableNotification extends Notification {
	
	private static final String REPLI_DEST_DRIVER = "REPLI_DVR";
    private static final String REPLI_DEST_URL    = "REPLI_URL";
    private static final String REPLI_DEST_USER   = "REPLI_USR";
    private static final String REPLI_DEST_PASS   = "REPLI_PSS";
    private static final String REPLI_DEST_TABLE  = "REPLI_TBL";
    private static final String REPLI_SQL         = "REPLI_SQL";
    
    private static final String REPLI_INSERT_SQL  = "REPLI_INS";
    private static final String REPLI_UPDATE_SQL  = "REPLI_UPD";
    private static final String REPLI_DEL_SQL     = "REPLI_DEL";
    private static final String REPLI_PK          = "REPLI_PK";
    private static final String REPLI_DEBUG       = "REPLI_DBG";

	private String debug = "NOK";
	
	@Override
	public boolean sendNotification(Vector<NotificationFieldTO> fields, Vector<Vector<Object>> sqlData) throws Exception {
    	DbQueryDelegate dbQuery = new DbQueryDelegate();
    	boolean response = true;

    	PreparedStatement pstmt = null;
    	Connection c = null;
    	
    	try {
	    	this.debug = this.getParamByKey(REPLI_DEBUG, fields);
	    	if (this.debug==null) {
	    		this.debug = "KO"; 
	    	}
	    		    		    	
			String destTable = this.getParamByKey(REPLI_DEST_TABLE, fields);
			String insertSql = this.getParamByKey(REPLI_INSERT_SQL, fields);
			String updateSql = this.getParamByKey(REPLI_UPDATE_SQL, fields);
			String pkTable = this.getParamByKey(REPLI_PK, fields);

			String sql = this.getParamByKey(REPLI_SQL, fields);
			Vector<Vector<Object>> sourceData = dbQuery.performQuery(sql);
			if (sourceData!=null && destTable!=null && insertSql!=null && sourceData.size()>0) {
				
				traceLog("sourceData is loaded... [" + sourceData.size() + "] rows");
				
		    	//open the connection to destination database...
		    	c = this.getConnection(true, fields);
				traceLog("Connection to destination is ready [" + c + "]");
				
				//clean all data of destination table...
				String deleteSql = this.getParamByKey(REPLI_DEL_SQL, fields);
				if (deleteSql!=null && !deleteSql.trim().equals("") && !deleteSql.trim().equals("NOK") ) {
					pstmt = c.prepareStatement(deleteSql);
					pstmt.executeUpdate();
					traceLog("Rows has been removed from [" + destTable + "]... :: [" + deleteSql + "]");
				}
				
				for (int j=1; j<sourceData.size(); j++) {
					Vector<Object> item = sourceData.elementAt(j);
					
					boolean insertIt = true;
					if (pkTable!=null && updateSql!=null && !pkTable.trim().equals("") && !updateSql.trim().equals("") ) {
						insertIt = !this.checkRowExists(c, pkTable, destTable, item);
					}

					if (insertIt) {
						insertRow(c, insertSql, item);						
						traceLog("new row inserted into [" + destTable + "]...");
					} else {
						updateRow(c, updateSql, item, pkTable);
						traceLog("Row updated on [" + destTable + "]...");
					}

				}
				
			} else {
				traceLog("[" + sourceData + "] [" + destTable + "] [" + insertSql + "]");
			}
    		
		} catch(Exception e){
			e.printStackTrace();
			response = false;
			
		} finally{
			try {
				if (c!=null) {
					c.close();
				}				
			} catch (Exception e){
				e.printStackTrace();	
			}
		}		
		return response;
	}


	private void traceLog(String content) throws BusinessException {
		EventBUS bus = new EventBUS();
		if (this.debug.equals("OK")) {
			System.out.println(content);
	        bus.insertEvent(LogUtil.LOG_INFO, LogUtil.SUMMARY_NOTIFIC, this.getClass().getName() + " :: " + content, RootTO.ROOT_USER, null);			
		}
	}
	
	private void updateRow(Connection c, String updateSql, Vector<Object> item, String pkTable) throws SQLException {
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) c.prepareStatement(updateSql);
		
		populatePreparedStatement(item, pstmt, 0, item.size());

		String[] pks = pkTable.split(";");
		populatePreparedStatement(item, pstmt, item.size(), item.size() + pks.length);
		
		pstmt.executeUpdate();
	}


	private void populatePreparedStatement(Vector<Object> item, PreparedStatement pstmt, int offset, int size) throws SQLException {
		for (int k=offset; k<size; k++) {
			Object colItem = item.elementAt(k - offset);
			if (colItem instanceof Timestamp) {
				pstmt.setTimestamp(k+1, (Timestamp)colItem);
			} else if (colItem instanceof Integer) {
				pstmt.setInt(k+1, (Integer)colItem);
			} else if (colItem instanceof Float) {
				pstmt.setFloat(k+1, (Float)colItem);
			} else if (colItem instanceof Date) {
				pstmt.setDate(k+1, (Date)colItem);
			} else {
				pstmt.setString(k+1, (String)colItem);
			}							
		}
	}


	private boolean checkRowExists(Connection c, String pkTable, String destTable, Vector<Object> item) throws BusinessException {
		boolean exists = false;
    	DbQueryDelegate dbQuery = new DbQueryDelegate();

    	String pkStr = "";
    	String[] ids = pkTable.split(";");
    	for (int i=0; i<ids.length; i++) {
    		String pkName = ids[i];
    		if (!pkStr.equals("")) {
    			pkStr += " and ";
    		}
    		pkStr += pkName + "='" + item.get(i) + "'"; 
		}
		Vector<Vector<Object>> sourceData = dbQuery.performQuery("select count(*) from " + destTable + " where " + pkStr);
		if (sourceData!=null && sourceData.size()==2 && !sourceData.get(1).get(0).toString().equals("0")) {
			exists = true;	
		}
		
		return exists;
	}


	private void insertRow(Connection c, String insertSql, Vector<Object> item) throws SQLException {
		PreparedStatement pstmt;
		pstmt = (PreparedStatement) c.prepareStatement(insertSql);
		
		populatePreparedStatement(item, pstmt, 0, item.size());
		
		pstmt.executeUpdate();
	}

	
    /* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getFields()
     */        
    public Vector<FieldValueTO> getFields(){
        Vector<FieldValueTO> response = new Vector<FieldValueTO>();

        response.add(new FieldValueTO(REPLI_DEST_DRIVER, "notification.replicate.driver", FieldValueTO.FIELD_TYPE_TEXT, 100, 50));
        response.add(new FieldValueTO(REPLI_DEST_URL, "notification.replicate.url", FieldValueTO.FIELD_TYPE_TEXT, 100, 50));
        response.add(new FieldValueTO(REPLI_DEST_USER, "notification.replicate.user", FieldValueTO.FIELD_TYPE_TEXT, 30, 10));
        response.add(new FieldValueTO(REPLI_DEST_PASS, "notification.replicate.pass", FieldValueTO.FIELD_TYPE_PASS, 30, 10));

        response.add(new FieldValueTO(REPLI_SQL, "notification.replicate.selectSql", FieldValueTO.FIELD_TYPE_AREA, 85, 4));
        response.add(new FieldValueTO(REPLI_INSERT_SQL, "notification.replicate.insertSql", FieldValueTO.FIELD_TYPE_AREA, 85, 4));
        response.add(new FieldValueTO(REPLI_UPDATE_SQL, "notification.replicate.updateSql", FieldValueTO.FIELD_TYPE_AREA, 85, 4));
        response.add(new FieldValueTO(REPLI_PK, "notification.replicate.pk", FieldValueTO.FIELD_TYPE_PASS, 100, 50));
        response.add(new FieldValueTO(REPLI_DEST_TABLE, "notification.replicate.table", FieldValueTO.FIELD_TYPE_TEXT, 30, 20));
        response.add(new FieldValueTO(REPLI_DEL_SQL, "notification.replicate.removeFirst", FieldValueTO.FIELD_TYPE_AREA, 85, 4));

        Vector<TransferObject> boolList = new Vector<TransferObject>();
        boolList.add(new TransferObject("OK", "OK"));
        boolList.add(new TransferObject("NOK", "NOK"));
        response.add(new FieldValueTO(REPLI_DEBUG, "notification.replicate.verbose", boolList));

        return response;
    }
    

	/* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getUniqueName()
     */
    public String getUniqueName() {
        return "Replicate Table";
    }
    
    
    /* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getContextHelp()
     */
    public String getContextHelp() {
        return "notification.replicate.help";
    }
    
    
	public Connection getConnection(boolean isAutoCommit, Vector<NotificationFieldTO> fields) throws Exception{
		Connection con = null;

		try {
			String driver = this.getParamByKey(REPLI_DEST_DRIVER, fields);
	    	String url = this.getParamByKey(REPLI_DEST_URL, fields);
	    	String user = this.getParamByKey(REPLI_DEST_USER, fields);
	    	String pass = this.getParamByKey(REPLI_DEST_PASS, fields);
	    	
	    	//the workaround below make the BD access with no password because the GUI no allowed empty fields.
	    	if (pass!=null && pass.equals("!")) {
	    		pass = null;
	    	}

		    Class.forName(driver);
		    con = java.sql.DriverManager.getConnection(url, user, pass);
		    con.setAutoCommit(isAutoCommit);
		    
		    if(!con.isClosed()) {
		    	traceLog("Successfully connected to [" + driver + "] [" + url + "] using JDBC...");
		    }
		    
		}catch(Exception e)	{
		    throw new Exception(e);
		}

		return con;
	}

	
    /* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getFieldTypes()
     */    
    public Vector<String> getFieldTypes() {
        return null;
    }

    
    /* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getFieldKeys()
     */    
    public Vector<String> getFieldKeys() {
        return null;
    }

    
    /* (non-Javadoc)
     * @see com.pandora.bus.alert.Notification#getFieldLabels()
     */
    public Vector<String> getFieldLabels() {
        return null;
    }	
}
