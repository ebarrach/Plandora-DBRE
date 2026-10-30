package com.pandora.gui.struts.form;

public class PivotTableForm extends GeneralStrutsForm {

	public static final String PT_SOURCE_GRID          = "GRID";
	public static final String PT_SOURCE_CAPACITY_FORM = "CAPC";
	
	private static final long	serialVersionUID	= 1L;
	
	private String serverURI;
	
	private String gridId;
	
	private String source;

	
	/////////////////////////////////////////////
	public String getServerURI() {
		return serverURI;
	}
	public void setServerURI(String newValue) {
		this.serverURI = newValue;
	}

	
	/////////////////////////////////////////////
	public String getGridId() {
		return gridId;
	}
	public void setGridId(String newValue) {
		this.gridId = newValue;
	}
	
	
	/////////////////////////////////////////////
	public String getSource() {
		return source;
	}
	public void setSource(String newValue) {
		this.source = newValue;
	}

	
}
