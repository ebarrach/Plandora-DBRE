package com.tests.bus.alert;

import java.util.Vector;

import junit.framework.TestCase;

import com.pandora.NotificationFieldTO;
import com.pandora.bus.SystemSingleton;
import com.pandora.bus.alert.FileToFTPNotification;

public class FileToFTPNotificationTest extends TestCase {

	
    private FileToFTPNotification n = new FileToFTPNotification();

    public void testSendNotification(){
    	
    	Vector<NotificationFieldTO> fields = getFields();
    	
    	Vector<Vector<Object>> sqlData = new Vector<Vector<Object>>();
    	Vector<Object> row = new Vector<Object>();
    	row.add("planning_id"); row.add("log_id"); row.add("project"); row.add("email"); row.add("emailalternative"); row.add("log"); row.add("filename");
    	sqlData.add(row);
    	
    	Vector<Object> row2 = new Vector<Object>();
    	row2.add("99489"); row2.add("43424"); row2.add("75890"); row2.add("pereto@hotmail.com"); row2.add(""); row2.add(""); row2.add("jre-8u281-windows-x64.exe");
    	sqlData.add(row2);
    	
		SystemSingleton.getInstance().setJdbcDriver("org.postgresql.Driver");
		SystemSingleton.getInstance().setJdbcHost("jdbc:postgresql://192.168.7.2:5432/sgu");		
		SystemSingleton.getInstance().setJdbcUser("postgres");
		SystemSingleton.getInstance().setJdbcPass("sgu");		
		SystemSingleton.getInstance().setDefaultEncoding("UTF-8");	
		
    	try {
			n.sendNotification(fields, sqlData);
            assertTrue(true);
		} catch (Exception e) {
            assertTrue(false);
			e.printStackTrace();
		}
    }

	private Vector<NotificationFieldTO> getFields() {
		Vector<NotificationFieldTO> fields = new Vector<NotificationFieldTO>();
		String[] keys = new String[]{"EMAIL_SENDER", "CHECK_TYPE", "CHECK_PATH", "FTP_HOST", 
			"FTP_USER", "FTP_PASSWORD", "FTP_PORT", "FTP_PATH", "EMAIL_USER", "EMAIL_PASSWORD", 
			"SUBJECT", "COMPACT", "EMAIL_HOST", "FTP_ENGINE" };
		String[] values = new String[]{"frubas@xxxx.com", "FILE", "T:\\", 
				"ftp.xxx.com.br", "xxxx", "xxxxx", "22", "www/arquivos/", "no_reply_web=xxx.com.br", 
				"xxxxxx", "TESTE - Arquivo disponível", "yes", "smtp.xxxx.com.br", "Apache_Commons_Project" };
		for (int i=0; i<keys.length; i++) {
			NotificationFieldTO nfto = new NotificationFieldTO();
			nfto.setName(keys[i]);
			nfto.setValue(values[i]);
			fields.add(nfto);
		}
		return fields;
	}
    
    
    
}
