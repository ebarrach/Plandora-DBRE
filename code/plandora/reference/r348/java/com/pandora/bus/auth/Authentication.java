package com.pandora.bus.auth;

import javax.naming.NamingException;

import com.pandora.UserTO;
import com.pandora.exception.BusinessException;

public abstract class Authentication {

	public abstract String encrypt(String rawPassword) throws BusinessException;
	
	public abstract boolean authenticate(UserTO fullUTo, String formPassword, boolean isPassEncrypted) throws BusinessException, NamingException;

	public abstract String getUniqueName();	
	
	
	@SuppressWarnings("rawtypes")
	public static Authentication getClass(String authClass){
        Authentication auth = null;
		try {
			Class busClass = Class.forName(authClass);

            auth = (Authentication)busClass.newInstance();					
		} catch (InstantiationException e) {
			e.printStackTrace();
			auth = null;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			auth = null;					
		} catch (ClassNotFoundException e) {
			auth = null;
		}

		return auth;
		
	}

	public void changePassword(UserTO uto) throws Exception {
		throw new BusinessException("This method cannot be called. Use the subclasses"); 
	}
}
