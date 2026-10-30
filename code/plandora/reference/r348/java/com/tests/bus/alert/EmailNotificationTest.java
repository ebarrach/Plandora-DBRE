package com.tests.bus.alert;

import java.util.Vector;

import com.pandora.NotificationFieldTO;
import com.pandora.bus.SystemSingleton;
import com.pandora.bus.alert.EmailNotification;

import junit.framework.TestCase;

public class EmailNotificationTest extends TestCase {

	
    private EmailNotification n = new EmailNotification();
    /*
    public void testSendNotificationWithTemplateSingleSending(){
    	
    	Vector<NotificationFieldTO> fields = getFields(true, true);
    	
    	Vector<Vector<Object>> sqlData = new Vector<Vector<Object>>();
    	Vector<Object> row = new Vector<Object>();
    	row.add("username"); row.add("email"); 
    	sqlData.add(row);
    	
    	Vector<Object> row2 = new Vector<Object>();
    	row2.add("AlbertoPereto"); row2.add("pereto@hotmail.com, alberto.pereto@gmail.com");
    	sqlData.add(row2);
    	
    	Vector<Object> row3 = new Vector<Object>();
    	row3.add("Teste"); row3.add("frubas@xxxx.com");
    	sqlData.add(row3);
    	
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


    public void testSendNotificationWithTemplateBunchSending(){
    	
    	Vector<NotificationFieldTO> fields = getFields(true, false);
    	
    	Vector<Vector<Object>> sqlData = new Vector<Vector<Object>>();
    	Vector<Object> row = new Vector<Object>();
    	row.add("username"); row.add("email"); 
    	sqlData.add(row);
    	
    	Vector<Object> row2 = new Vector<Object>();
    	row2.add("AlbertoPereto"); row2.add("pereto@hotmail.com");
    	sqlData.add(row2);
    	Vector<Object> row3 = new Vector<Object>();
    	row3.add("Teste"); row3.add("alberto.pereto@gmail.com");
    	sqlData.add(row3);
    	
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
    
    
    public void testSendNotificationNoTemplateSingleSending(){
    	
    	Vector<NotificationFieldTO> fields = getFields(false, true);
    	
    	Vector<Vector<Object>> sqlData = new Vector<Vector<Object>>();
    	Vector<Object> row = new Vector<Object>();
    	row.add("username"); row.add("email"); 
    	sqlData.add(row);
    	
    	Vector<Object> row2 = new Vector<Object>();
    	row2.add("AlbertoPereto"); row2.add("pereto@hotmail.com");
    	sqlData.add(row2);
    	Vector<Object> row3 = new Vector<Object>();
    	row3.add("Teste"); row3.add("alberto.pereto@gmail.com");
    	sqlData.add(row3);
    	
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
    */
        
    
    
}
