package com.pandora.gui.struts.form;

public class MindMapForm extends GeneralStrutsForm {

	private static final long serialVersionUID = 1L;
	
    private String formTitle;
    
    private String planningId;
    
    private String projectId;
    
    private String mindMapNodes;

    
    //////////////////////////////////////
    public String getFormTitle() {
		return formTitle;
	}
	public void setFormTitle(String newValue) {
		this.formTitle = newValue;
	}
	
	
    //////////////////////////////////////
	public String getPlanningId() {
		return planningId;
	}
	public void setPlanningId(String newValue) {
		this.planningId = newValue;
	}
	
	
    //////////////////////////////////////
	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String newValue) {
		this.projectId = newValue;
	}
	
	

    //////////////////////////////////////
	public String getMindMapNodes() {
		return mindMapNodes;
	}
	public void setMindMapNodes(String newValue) {
		this.mindMapNodes = newValue;
	}
    
	
	
}
