package com.pandora;

import java.sql.Timestamp;

public class AdditionalTableTO extends TransferObject {

	private static final long serialVersionUID = 1L;
	
    private Integer line;
    
    private Integer col;
    
    private String value;

    private Timestamp dateValue;
    
    private Timestamp creationDate;
    
    private Timestamp modificationDate;
    
    	
	public AdditionalTableTO(AdditionalTableTO clone, int newRow) {
		this.line = new Integer(newRow);
		this.col = clone.getCol();
		this.value = clone.getValue();
		this.dateValue = clone.getDateValue();
		this.setId(clone.getId());
		this.setGenericTag(clone.getId());
		this.setGridRowNumber(clone.getGridRowNumber());
	}
	
	
	public AdditionalTableTO() {
		super();
	}
	
	///////////////////////////////////////////
	public Integer getLine() {
		return line;
	}
	public void setLine(Integer newValue) {
		this.line = newValue;
	}

	
	///////////////////////////////////////////
	public Integer getCol() {
		return col;
	}
	public void setCol(Integer newValue) {
		this.col = newValue;
	}

	
	///////////////////////////////////////////
	public String getValue() {
		return value;
	}
	public void setValue(String newValue) {
		this.value = newValue;
	}
	
    /////////////////////////////////////////////    
	public Timestamp getDateValue() {
		return dateValue;
	}
	public void setDateValue(Timestamp newValue) {
		this.dateValue = newValue;
	}


	/**
	 * @return the creationDate
	 */
	public Timestamp getCreationDate() {
		return creationDate;
	}


	/**
	 * @param creationDate the creationDate to set
	 */
	public void setCreationDate(Timestamp creationDate) {
		this.creationDate = creationDate;
	}


	/**
	 * @return the modificationDate
	 */
	public Timestamp getModificationDate() {
		return modificationDate;
	}


	/**
	 * @param modificationDate the modificationDate to set
	 */
	public void setModificationDate(Timestamp modificationDate) {
		this.modificationDate = modificationDate;
	}	
    
    
}
