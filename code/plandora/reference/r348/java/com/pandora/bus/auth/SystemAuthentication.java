package com.pandora.bus.auth;

import com.pandora.UserTO;
import com.pandora.bus.PasswordEncrypt;
import com.pandora.dao.UserDAO;
import com.pandora.exception.BusinessException;
import com.pandora.exception.DataAccessException;


public class SystemAuthentication extends Authentication{

    /** The Data Access Object related with current business entity */    
    UserDAO dao = new UserDAO();
    
    
    public boolean authenticate(UserTO fullUTo, String formPassword, boolean isPassEncrypted) throws BusinessException{
        boolean response = true;
        
 
        String encPass = formPassword;
        
        
        if (!isPassEncrypted && !encPass.equals("")) {
            //encrypt the password
            encPass = PasswordEncrypt.getInstance().encrypt(formPassword);

            
        }
                
        //verify if password is correct...
        if (fullUTo!=null){
            if (!fullUTo.getPassword().equals(encPass)){
                response = false; 
            }

        }
        
        return response;
    }

    
	@Override
	public void changePassword(UserTO uto) throws Exception {
    	uto.setPassword(this.encrypt(uto.getPassword()));
    	try {
			dao.updatePassword(uto);
		} catch (DataAccessException e) {
            throw new  BusinessException(e);
		}	
    }


	public String getUniqueName() {
		return "label.authMode.DB";
	}
	
	@Override
	public String encrypt(String rawPassword) throws BusinessException {
		return PasswordEncrypt.getInstance().encrypt(rawPassword);
	}
	
	
}
