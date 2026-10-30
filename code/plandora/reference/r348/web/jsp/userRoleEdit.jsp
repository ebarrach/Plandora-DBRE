<jsp:include page="encoding.jsp" />
<%@ taglib uri="/WEB-INF/lib/struts-html" prefix="html" %>
<%@ taglib uri="/WEB-INF/lib/struts-bean" prefix="bean" %>
<%@ taglib uri="/WEB-INF/lib/display" prefix="display" %>
<%@ taglib uri="/WEB-INF/lib/struts-logic" prefix="logic" %>
<%@ taglib uri="/WEB-INF/lib/plandora-html" prefix="plandora-html" %>

<html>
	<title>
   		<bean:message key="label.grid.project.role"/>    	
	</title>
	
	<head>
		<link href="../css/styleDefault.css" id="style" TYPE="text/css" rel="STYLESHEET">
		<link rel="shortcut icon" type="image/x-icon" href="../images/favicon.ico" />
	</head>
	<body bgColor="#ffffff" leftMargin="0" topMargin="0" marginheight="0" marginwidth="0" >

	<jsp:include page="validator.jsp" />
	        	
	<script language="JavaScript">
			
		function checkUserRoleValue(opt, ctoId, prjId) {
			with(document.forms["userRoleForm"]){
	   			operation.value = "changeRole";
	   			functionId.value = opt
				ajaxProcess(document.forms["userRoleForm"], callBackRefreshUserRole, '');			
	   		}
		}	

		function callBackRefreshUserRole(dummy) {  
		    if(isAjax()){
		       	document.getElementById("ajaxResponse").innerHTML = ""; //hide ajax icon
				var content = objRequest.responseText;
			   	(document.getElementById("user_role_response_body")).innerHTML = content;  
		    }  
		}


	</script>

	<html:form action="showUserRoleEdit">
		<html:hidden name="userRoleForm" property="id"/>	
		<html:hidden name="userRoleForm" property="operation"/>
		<html:hidden name="userRoleForm" property="functionId"/>
					
		<br>
		
		<table width="100%" height="80%" border="0" cellspacing="0" cellpadding="0">
		<tr><td width="10">&nbsp;</td><td>
			
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
		    <tr class="formLabel">
		      <td width="10">&nbsp;</td>
		      <td width="350">
		    		<bean:message key="label.grid.project.role.user"/>
					<bean:write name="userRoleForm" property="currentUserRole" filter="false" />
		    		<bean:message key="label.grid.project.role.prj"/>
		      </td>
		      <td>&nbsp;</td>
		      <td width="10">&nbsp;</td>
		    </tr>
			<tr class="gapFormBody">
				<td colspan="4">&nbsp;</td>
			</tr>
		  	</table>

			<table width="97%" border="0" cellspacing="0" cellpadding="0">
			<tr  class="formBody">
				<td>
					<bean:write name="userRoleForm" property="roleHtmlBody" filter="false" />
				</td>
			</tr>
			</table>



			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr class="gapFormBody">
					<td width="10">&nbsp;</td>
					<td>&nbsp;</td>
					<td width="10">&nbsp;</td>
				</tr>			
			</table>

		  	<table width="100%" border="0" cellspacing="0" cellpadding="0">			
		    <tr class="formLabel">
		      <td width="10">&nbsp;</td>
		      <td width="100">      
			      <html:button property="cancel" styleClass="button" onclick="closeMessage();">
		    	    &nbsp;&nbsp;<bean:message key="button.close"/>&nbsp;&nbsp;
			      </html:button>    
		      </td>      
		      <td>&nbsp;</td>      
		      <td width="10">&nbsp;</td>      
		    </tr>
			</table>  	

			<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr class="formBody">
					<td width="10">&nbsp;</td>
					<td><div id="user_role_response_body"></td>
					<td width="10">&nbsp;</td>
				</tr>			
			</table>
		
		</td><td width="20">&nbsp;</td>
		</tr>
		<tr><td colspan="3" height="50%">&nbsp;</td></tr>
		</table>

	</html:form>
</html>