package com.pandora.gui.struts.form;

import java.sql.Timestamp;

import com.pandora.helper.StringUtil;

/**
 */
public class CustomFormForm extends GeneralStrutsForm {

	private static final long serialVersionUID = 1L;
	
    private String metaFormId;
    
    private String formTitle;
    
    private Timestamp creationDate;
    
    private String jsAfterSave;
    
    private String jsAfterLoad;
    
    private String jsBeforeSave;
    
    private String afterSuccessfullySave = "off";
    
    private String daysToHideRecords = "30";
    
    private boolean saveBatch;
    
    private boolean enableBatch;
    
    
    
    //////////////////////////////////////
    public String getFormTitle() {
        return formTitle;
    }
    public void setFormTitle(String newValue) {
        this.formTitle = newValue;
    }
    
    
    //////////////////////////////////////
    public String getMetaFormId() {
        return metaFormId;
    }
    public void setMetaFormId(String newValue) {
        this.metaFormId = newValue;
    }
    
    
    ////////////////////////////////////////////    
    public Timestamp getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Timestamp newValue) {
        this.creationDate = newValue;
    }
    
    
    ////////////////////////////////////////////       
	public String getJsAfterSave() {
		return jsAfterSave;
	}
	public void setJsAfterSave(String newValue) {
		this.jsAfterSave = newValue;
	}
	
	
    ////////////////////////////////////////////       	
	public String getJsAfterLoad() {
		return jsAfterLoad;
	}
	public void setJsAfterLoad(String newValue) {
		this.jsAfterLoad = newValue;
	}
	
	
    ////////////////////////////////////////////       	
	public String getJsBeforeSave() {
		return jsBeforeSave;
	}
	public void setJsBeforeSave(String newValue) {
		this.jsBeforeSave = newValue;
	}
	
	
    ////////////////////////////////////////////     	
	public String getAfterSuccessfullySave() {
		return afterSuccessfullySave;
	}
	public void setAfterSuccessfullySave(String newValue) {
		this.afterSuccessfullySave = newValue;
	}
	
	
    ////////////////////////////////////////////    	
	public String getDaysToHideRecords() {
		return daysToHideRecords;
	}
	public void setDaysToHideRecords(String newValue) {
		this.daysToHideRecords = newValue;
	}   
    

	public int getDaysToHide() {
	    int daysToHide = 30;
	    if (daysToHideRecords!=null && !daysToHideRecords.trim().equals("") && StringUtil.hasOnlyDigits(daysToHideRecords)) {
	    	daysToHide = Integer.parseInt(daysToHideRecords);
	    }
		return daysToHide;
	}
	/**
	 * @return the saveBatch
	 */
	public boolean isSaveBatch() {
		return saveBatch;
	}
	/**
	 * @param saveBatch the saveBatch to set
	 */
	public void setSaveBatch(boolean saveBatch) {
		this.saveBatch = saveBatch;
	}
	/**
	 * @return the enableBatch
	 */
	public boolean isEnableBatch() {
		return enableBatch;
	}
	/**
	 * @param enableBatch the enableBatch to set
	 */
	public void setEnableBatch(boolean enableBatch) {
		this.enableBatch = enableBatch;
	}

}
