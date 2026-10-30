<%@ taglib uri="/WEB-INF/lib/struts-html" prefix="html" %>
<%@ taglib uri="/WEB-INF/lib/struts-bean" prefix="bean" %>
<%@ taglib uri="/WEB-INF/lib/display" prefix="display" %>
<%@ taglib uri="/WEB-INF/lib/struts-logic" prefix="logic" %>
<%@ taglib uri="/WEB-INF/lib/plandora-html" prefix="plandora-html" %>

<html>
	<head>
		<link href="../css/styleDefault.css" id="style" TYPE="text/css" rel="STYLESHEET">
		<link type="text/css" rel="stylesheet" href="//cdn.jsdelivr.net/npm/jsmind@0.8.5/style/jsmind.css" />
		
		<title><bean:write name="mindMapForm" property="formTitle" filter="false" /></title>
		<script type="text/javascript" language="JavaScript" src="../jscript/default.js"></script>
		
    	<script type="text/javascript" src="//cdn.jsdelivr.net/npm/jsmind@0.8.5/es6/jsmind.js"></script>
    
		
		<script type="text/javascript" language="JavaScript" src="../jscript/ajaxsync.js"></script>	
		<script type="text/JavaScript" language="JavaScript" src="../jscript/modal_panel.js" ></script>
		
		<script>
			messageObj = new DHTML_modalMessage();	// We only create one object of this class
			messageObj.setShadowOffset(5);	// Large shadow
				
			function displayMessage(url, w, h){
				messageObj.setSource(url + '&' + new Date().getTime() );
				messageObj.setCssClassMessageBox(false);
				messageObj.setSize(w, h);
				messageObj.setShadowDivVisible(true);
				messageObj.display();
			}
			function displayStaticMessage(messageContent, w, h) {
				messageObj.setHtmlContent(messageContent);
				messageObj.setSize(w, h);
				messageObj.setSource(false);
				messageObj.setShadowDivVisible(true);	
				messageObj.display();			
			}
			function closeMessage() {
				messageObj.close();	
			}
		</script>
		
		<style type="text/css">
			#popupfooter {
				position:absolute;
				bottom:0 !important;
				height:20px;
				width:100%;
			}
			
			html {
				overflow: hidden;
			}
			
			body {
				margin: 0 0 0 0;
				padding: 0;
				background-color: #ffffff;
			}
		</style>
	</head>
	
	<body>
		<html:form action="viewMindMap"> 
			<html:hidden name="mindMapForm" property="operation"/>
			<html:hidden name="mindMapForm" property="id"/>
		
			<table width="100%" height="20px" border="0" cellspacing="0" cellpadding="0">
			<tr class="formLabel">
			   <td width="10">&nbsp;</td>
			   <td><center>
					<bean:message key="title.mindMapForm"/>
			   </center></td>
			   <td width="10">&nbsp;</td>      
			</tr>
			</table> 		
			
			<br/>
			
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr><td width="10">&nbsp;</td><td>
			
				<display:headerfootergrid width="100%" type="HEADER">
					<bean:message key="title.mindMapForm.diagram"/>
				</display:headerfootergrid>
			
			  
			  	<div id="jsmind_container" style="width: 100%; height: 400px;"></div>

				<script type="text/javascript">
				   var mind = {
				        "meta": {
				          "name": "Plandora_mind_map",
				          "author": "Plandora",
				          "version": "0.1"
				        },
				        "format": "freemind",
				        "data": "<bean:write name="mindMapForm" property="mindMapNodes" filter="false" />"
     				};
				    var options = {                     
				        container:'jsmind_container', 
				        editable:false, 
				        theme:'pumpkin' 
				    };
				    var jm = new jsMind(options);
				    jm.show(mind);
				</script>
			
				<display:headerfootergrid type="FOOTER">	
					<table width="98%" border="0" cellspacing="0" cellpadding="0"><tr>
					  <td>&nbsp;</td>
					</tr></table>
				</display:headerfootergrid> 
				
			</td><td width="10">&nbsp;</td>
			</tr></table>
			
			<br/>
			
			<table width="100%" border="0" cellspacing="0" cellpadding="0">
			<tr><td width="10">&nbsp;</td><td>
			
				<display:headerfootergrid width="100%" type="HEADER">
					<bean:message key="title.mindMapForm.trace"/>
				</display:headerfootergrid>

				<table width="98%" border="0" cellspacing="0" cellpadding="0">
					<tr class="gapFormBody">
						<td colspan="4">&nbsp;</td>
					</tr>		
					<plandora-html:artifact name="mindMapForm" property="planningId" projectProperty="projectId" ajax="true" collection="mindMapArtifactList" onlyBody="true" height="152"/> 	
				</table> 

				<display:headerfootergrid type="FOOTER">	
					<table width="98%" border="0" cellspacing="0" cellpadding="0"><tr>
					  <td>&nbsp;</td>
					</tr></table>
				</display:headerfootergrid> 
				
			</td><td width="10">&nbsp;</td>
			</tr></table>

			<div id="popupfooter">						
				<table width="100%" border="0" cellspacing="0" cellpadding="0">
				<tr class="formLabel">
				   <td width="10">&nbsp;</td>
				   <td><center>
					  <html:button property="close" styleClass="button" onclick="javascript:window.close();">
						<bean:message key="button.close"/>
					  </html:button>    
				   </center></td>
				   <td width="10">&nbsp;</td>      
				</tr>
				</table> 
			</div>
		</html:form>
	</body>
</html>