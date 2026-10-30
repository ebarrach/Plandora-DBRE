package com.tests.bus.alert;

import java.util.Vector;

import com.pandora.NotificationFieldTO;
import com.pandora.bus.SystemSingleton;
import com.pandora.bus.alert.ReplicateTableNotification;

import junit.framework.TestCase;

public class ReplicateTableNotificationTest extends TestCase {

	private ReplicateTableNotification n = new ReplicateTableNotification();
	
    public void testInsertReplicationWithOneRow(){
    	Vector<NotificationFieldTO> fields = getFields(false);
    	    	
		SystemSingleton.getInstance().setJdbcDriver("org.postgresql.Driver");
		SystemSingleton.getInstance().setJdbcHost("jdbc:postgresql://localhost:5432/sgu");		
		SystemSingleton.getInstance().setJdbcUser("postgres");
		SystemSingleton.getInstance().setJdbcPass("sgu");		
		SystemSingleton.getInstance().setDefaultEncoding("UTF-8");	
		
    	try {
			n.sendNotification(fields, null);
            assertTrue(true);
		} catch (Exception e) {
            assertTrue(false);
			e.printStackTrace();
		}
    }

    public void testUpdateReplicationWithOneRow(){
    	Vector<NotificationFieldTO> fields = getFields(true);
    	    	
		SystemSingleton.getInstance().setJdbcDriver("org.postgresql.Driver");
		SystemSingleton.getInstance().setJdbcHost("jdbc:postgresql://localhost:5432/sgu");		
		SystemSingleton.getInstance().setJdbcUser("postgres");
		SystemSingleton.getInstance().setJdbcPass("sgu");		
		SystemSingleton.getInstance().setDefaultEncoding("UTF-8");	
		
    	try {
			n.sendNotification(fields, null);
            assertTrue(true);
		} catch (Exception e) {
            assertTrue(false);
			e.printStackTrace();
		}
    }

    	
	private Vector<NotificationFieldTO> getFields(boolean isUpdate) {
		Vector<NotificationFieldTO> fields = new Vector<NotificationFieldTO>();
		String[] keys = new String[]{"REPLI_DVR", "REPLI_URL", "REPLI_USR", "REPLI_PSS", 
			"REPLI_TBL", "REPLI_SQL", "REPLI_INS", "REPLI_UPD", "REPLI_DEL", "REPLI_PK", 
			"REPLI_DBG"};
		String[] values = new String[]{"org.postgresql.Driver", "jdbc:postgresql://localhost:5432/sgu", "postgres", 
				"sgu", "company", "select 1 as id, 'teste' as name, 'teste' as full_name, 'linha inserida' as company_number, null as address, 'Campinas' as city, 'SP' as state_province, 'Brasil' as country", 
				"insert into company (id, name, full_name, company_number, address, city, state_province, country) values (?,?,?,?,?,?,?,?)", 
				"", "delete from company where id='1'", 
				"", "OK" };
		
		if (isUpdate) {
			values[5] = "select 1 as id, 'teste' as name, 'teste' as full_name, 'linha atualizada' as company_number, null as address, 'Campinas' as city, 'SP' as state_province, 'Brasil' as country";
			values[7] = "update company set id=?, name=?, full_name=?, company_number=?, address=?, city=?, state_province=?, country=? where id=?";
			values[8] = "";
			values[9] = "id";
		}
		
		for (int i=0; i<keys.length; i++) {
			NotificationFieldTO nfto = new NotificationFieldTO();
			nfto.setName(keys[i]);
			nfto.setValue(values[i]);
			fields.add(nfto);
		}
		return fields;
	}
	

}
