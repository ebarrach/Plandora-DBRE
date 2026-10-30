package com.pandora.gui.struts.form;

public class UserRoleForm extends GeneralStrutsForm {

	private static final long serialVersionUID = 1L;
	
	private String roleHtmlBody;

	private String currentUserRole;

	private String projectId;
	
	private String userId;
	
	private String functionId;

	public void clear(){
	}

	
	
	public String getCurrentUserRole() {
		return currentUserRole;
	}
	public void setCurrentUserRole(String currentUserRole) {
		this.currentUserRole = currentUserRole;
	}


	public String getProjectId() {
		return projectId;
	}
	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}


	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getRoleHtmlBody() {
		return roleHtmlBody;
	}

	public void setRoleHtmlBody(String roleHtmlBody) {
		this.roleHtmlBody = roleHtmlBody;
	}



	public String getFunctionId() {
		return functionId;
	}
	public void setFunctionId(String functionId) {
		this.functionId = functionId;
	}

	
	
}
