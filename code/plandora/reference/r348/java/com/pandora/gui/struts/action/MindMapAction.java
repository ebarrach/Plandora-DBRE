package com.pandora.gui.struts.action;

import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.AttachmentTO;
import com.pandora.PlanningRelationTO;
import com.pandora.PlanningTO;
import com.pandora.RepositoryFilePlanningTO;
import com.pandora.RequirementTO;
import com.pandora.ResourceTaskTO;
import com.pandora.RiskTO;
import com.pandora.RootTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.delegate.PlanningDelegate;
import com.pandora.delegate.RepositoryDelegate;
import com.pandora.delegate.TaskDelegate;
import com.pandora.gui.struts.form.MindMapForm;
import com.pandora.helper.LogUtil;
import com.pandora.helper.StringUtil;

public class MindMapAction extends GeneralStrutsAction {

	private long nodeIdSeq = 1;
	
	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response){
		String forward = "viewMindMap";
	    RepositoryDelegate rdel = new RepositoryDelegate();
	    PlanningDelegate del = new PlanningDelegate();
	    
		try {
			MindMapForm frm = (MindMapForm)form;	

			String rootId = request.getParameter("entityid");

			
		    frm.setFormTitle(this.getBundleMessage(request, "title.mindMapForm"));
		    frm.setPlanningId(rootId);
		    frm.setMindMapNodes(this.getMindMapNodes(request).toString());
		    
		    PlanningTO pto = del.getSpecializedObject(new PlanningTO(rootId));
		    frm.setProjectId(pto.getPlanningProject().getId());
		    
			Vector<RepositoryFilePlanningTO> list = rdel.getFilesFromPlanning(pto);
			request.getSession().setAttribute("mindMapArtifactList", list);	    
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	    
		return mapping.findForward(forward);
	}


	private StringBuffer getMindMapNodes(HttpServletRequest request) {
		StringBuffer content = new StringBuffer("");
		PlanningDelegate del = new PlanningDelegate();
		TaskDelegate tdel = new TaskDelegate();
		
	    try {
	    	content.append("<map version='1.0.1'>");
	    	
	    	String root = request.getParameter("entityid");	   
	        if (root!=null && !root.trim().equals("")) {

		    	PlanningTO pto = del.getSpecializedObject(new PlanningTO(root));
		    	if (pto!=null) {
			    	content.append("<node ID='fm_" + this.getNextNodeId() + "' TEXT='" + this.getTitle(request, pto) + "'>");
		    		content.append(this.getNodeTip(request, pto));
		    		
					if (pto instanceof TaskTO) {
						TaskTO parentTask = ((TaskTO)pto).getParentTask();
				    	if (parentTask!=null) {
				    		parentTask = tdel.getTaskObject(parentTask);
				    		content.append(this.getNodeTag(request, parentTask, true, PlanningRelationTO.RELATION_PART_OF));
				    		content.append("</node>");
				    	}
				    	
				    	if (((TaskTO) pto).isParentTask()) {
				    		if (((TaskTO) pto).getChildTasks()!=null && ((TaskTO) pto).getChildTasks().size()>0) {
					    		for (TaskTO child : ((TaskTO) pto).getChildTasks().values()) {
						    		content.append(this.getNodeTag(request, child, true, PlanningRelationTO.RELATION_COMPOSED_BY));
								}				    			
				    		}
				    	}
					}
					
					
					if (pto.getAttachments()!=null && pto.getAttachments().size()>0){
						content.append(this.getAttachments(request, pto, true));	
					}
		    		
			    	content.append(this.getNodeConnection(request, pto, 0));
			    	content.append(this.getUserRelation(pto, true));		    	
			    	content.append("</node>");		
		    	}
	        }
	    	content.append("</map>");
	    	
	    	
		}catch(Exception e){
			LogUtil.log(this, LogUtil.LOG_ERROR, "A error occurs on MindMapAction", e);
		}
	   
	    

    	return content;
	}
	
	/*
	public ActionForward loadMindMap(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		StringBuffer content = new StringBuffer("");
		PlanningDelegate del = new PlanningDelegate();
		TaskDelegate tdel = new TaskDelegate();
		String userEncoding = SystemSingleton.getInstance().getDefaultEncoding();
		
	    try {
	    	String root = request.getParameter("entityid");	        

	    	UserTO uto = SessionUtil.getCurrentUser(request);
	    	userEncoding = uto.getBundle().getMessage(uto.getLocale(), "encoding");
	    
	        if (root!=null && !root.trim().equals("")) {
	        	this.nodeIdSeq = 1;

		    	//content.append("<?xml version='1.0' encoding='" + userEncoding + "'?>");
		    	content.append("<map version='1.0.1'>");
	        	
		    	PlanningTO pto = del.getSpecializedObject(new PlanningTO(root));
		    	if (pto!=null) {
			    	content.append("<node CREATED='1227270466718' ID='fm_" + this.getNextNodeId() + 
			    				"' MODIFIED='1227270466718' TEXT='" + this.getTitle(request, pto) + "'>");
					
		    		content.append(this.getNodeTip(request, pto));
					
					if (pto instanceof TaskTO) {
						TaskTO parentTask = ((TaskTO)pto).getParentTask();
				    	if (parentTask!=null) {
				    		parentTask = tdel.getTaskObject(parentTask);
				    		content.append(this.getNodeTag(request, parentTask, true, PlanningRelationTO.RELATION_PART_OF));
				    		content.append("</node>");
				    	}
				    	
				    	if (((TaskTO) pto).isParentTask()) {
				    		if (((TaskTO) pto).getChildTasks()!=null && ((TaskTO) pto).getChildTasks().size()>0) {
					    		for (TaskTO child : ((TaskTO) pto).getChildTasks().values()) {
						    		content.append(this.getNodeTag(request, child, true, PlanningRelationTO.RELATION_COMPOSED_BY));
								}				    			
				    		}
				    	}
					}
					
					if (pto.getAttachments()!=null && pto.getAttachments().size()>0){
						content.append(this.getAttachments(request, pto, true));	
					}
			    	
			    	content.append("<font BOLD='true' NAME='Verdana' SIZE='16'/>");
			    	content.append(this.getNodeConnection(request, pto, 0));
			    	content.append(this.getUserRelation(pto, true));			    	
			    	content.append("</node>");		    		
		    	}
		    	
		    	content.append("</map>");	
	
	        }
	    
		}catch(Exception e){
		} finally{
		}

		return null;
	}
	
	
	
	*/


	
	private String getActor(String key, boolean isRight, UserTO actor) {
		String response = "";
		if (!actor.getUsername().equals(RootTO.ROOT_USER)) {
			response = "<node ID='" + key + "' POSITION='" + (isRight?"right":"left") + "' TEXT='" + actor.getUsername() + "'></node>"; 
		}
		return response;
	}
	

	private String getUserRelation(PlanningTO node, boolean isRight) throws Exception {	
		String content = "";

		if (node instanceof RequirementTO) {
			RequirementTO req = (RequirementTO)node;
			String key = "fm_" + this.getNextNodeId();
			content = content + this.getActor(key, isRight, req.getRequester());
			
		} else if (node instanceof TaskTO) {
			TaskTO tsk = (TaskTO)node;
			if (tsk.getAllocResources()!=null && !tsk.isParentTask()) {
				Iterator<ResourceTaskTO> i = tsk.getAllocResources().iterator();
				while(i.hasNext()) {
					ResourceTaskTO rtto = i.next();
					String key = "fm_" + this.getNextNodeId();
					content = content + this.getActor(key, isRight, rtto.getResource());
				}
			}
			
		} else if (node instanceof RiskTO) {
			RiskTO rsk = (RiskTO)node;
			if (rsk.getResponsible()!=null && !rsk.getResponsible().trim().equals("")) {
				UserTO uto = new UserTO("0");
				uto.setUsername(rsk.getResponsible());
				String key = "fm_" + this.getNextNodeId();
				content = content + this.getActor(key, isRight, uto);				
			}
		}
		return content;
	}

	
	private String getNodeConnection(HttpServletRequest request, PlanningTO pto, int level) throws Exception {
	    String response = "";
	    PlanningDelegate del = new PlanningDelegate();
	    
    	Vector<PlanningRelationTO> list = pto.getRelationList();
    	if (list!=null && level<=2 ) {
    		for (PlanningRelationTO prto : list) {
    	        if (prto.getPlanning().getId().equals(pto.getId())) {
    	            PlanningTO right = del.getSpecializedObject(prto.getRelated());
    	            response = response + this.getNodeTag(request, right, true, prto.getRelationType());
    	            response = response + "</node>";
    	        } else {
    	            PlanningTO left = del.getSpecializedObject(prto.getPlanning());
    	            response = response + this.getNodeTag(request, left, false, prto.getRelationType());
    	            response = response + "</node>";		    	            
    	        }
    	    }
    	}
    	
    	return response;
	}
	
	private String getNodeTag(HttpServletRequest request, PlanningTO node, boolean isRight, String relationType) throws Exception {
		StringBuffer buff = new StringBuffer();
		
		String relationLabel = StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.relationTag.relation." + relationType));
		buff.append("<node ID='fm_" + this.getNextNodeId() + "' POSITION='" + (isRight?"right":"left") + "' TEXT='" +relationLabel + "'>");		
	
		buff.append("<node ID='fm_" + this.getNextNodeId() + "' POSITION='" + (isRight?"right":"left") + "' TEXT='" + this.getTitle(request, node) + "'>");	
		buff.append(this.getNodeTip(request, node));
		buff.append(this.getUserRelation(node, isRight));
		
		buff.append("</node>");
		
		return buff.toString();	
	}
	
	private Object getAttachments(HttpServletRequest request,PlanningTO pto, boolean isRight) {
		StringBuffer buff = new StringBuffer();
		Vector<AttachmentTO> list = pto.getAttachments();
		
		if (!list.isEmpty()) {
			buff.append("<node ID='fm_" + this.getNextNodeId() +
					"' POSITION='" + (isRight?"right":"left") + 
					"' TEXT='" +	StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "title.attachTagLib.List")) + "'>");
			for (AttachmentTO ato : list) {
				buff.append("<node ID='fm_" + this.getNextNodeId() +
						"' POSITION='" + (isRight?"right":"left") +
						//"' LINK='javascript:downloadAttachment('" + ato.getId() + "');" +
						"' TEXT='" +	StringUtil.formatWordToFreeMind(ato.getName()) + "'/>");
			}
			buff.append("</node>");			
		}
		
		return buff;
	}
	
	private String getNodeTip(HttpServletRequest request, PlanningTO node) throws Exception {
		String content = "";
		
		content = content + "<node ID='fm_" + this.getNextNodeId() + "' TEXT='info'>";
		
		if (!(node instanceof RequirementTO)) {
			content = content + "<node ID='fm_" + this.getNextNodeId() + "_req' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.manageTask.name")) + " : " + StringUtil.formatWordToFreeMind(node.getName()) + "' />";
		}
		
		if (node instanceof RequirementTO) {			
			RequirementTO req = (RequirementTO)node;
			if (req.getRequester()!=null) {
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_requester' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.requestPriority")) + " : " + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.requestPriority." + req.getPriority().intValue())) + "' />";				
				if (req.getRequirementStatus()!=null){
					String reqst = req.getRequirementStatus().getName();
					content = content + "<node ID='fm_" + this.getNextNodeId() + "_reqstat' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.requestStatus"))  + " : " +  reqst + "' />";					
				}
			}
		} else if (node instanceof RiskTO) {
			RiskTO rsk = (RiskTO)node;
			if (rsk.getProbability()!=null) {
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_rsk_prob' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.probability")) + 
						" : " + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.probability." + rsk.getProbability())) + "' />";	
			}
			if (rsk.getImpact()!=null) {
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_rsk_imp' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.impact")) + 
						" : " + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.impact." + rsk.getImpact())) + "' />";	
			}
			if (rsk.getTendency()!=null) {
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_rsk_tend' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.tendency")) + 
						" : " + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.formRisk.tendency." + rsk.getTendency())) + "' />";	
			}			
		} else if (node instanceof TaskTO) {
			TaskTO tsk = (TaskTO)node;
			
			if (tsk.isParentTask()) {
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_tsk_parent' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "title.mindMapForm.TSK")) + 
						" : " + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.manageTask.taskParent")) + "' />"; 
			}

			if (tsk.getAllocResources()!=null && tsk.getAllocResources().size()>0) {
				Iterator<ResourceTaskTO> i = tsk.getAllocResources().iterator();
				content = content + "<node ID='fm_" + this.getNextNodeId() + "_tsk_res' TEXT='" + StringUtil.formatWordToFreeMind(this.getBundleMessage(request, "label.manageTask.taskStatus")) + " : "; 
				while(i.hasNext()) {
					ResourceTaskTO rtto = (ResourceTaskTO)i.next();
					if (tsk.getAllocResources().size()==1) {
						content = content +  StringUtil.formatWordToFreeMind(rtto.getTaskStatus().getName()) + "' />";
					} else {
						content = content +  StringUtil.formatWordToFreeMind(rtto.getTaskStatus().getName() + " (" + rtto.getResource().getUsername() + ") ") + "' />";	
					}
				}
			}

		}	
		content = content + "</node>";

		if (node.getDescription()!=null && !node.getDescription().equals("")) {
			String desc = node.getDescription();
			if (desc.length()>400) {
				desc = desc.substring(0, 399) + "...";
			}
			content = content + "<node ID='fm_" + this.getNextNodeId() + "' TEXT='" + StringUtil.formatWordToFreeMind(desc) + "' />";
		}
		
		return content;
	}
	
	
	private String getTitle(HttpServletRequest request, PlanningTO node) {
		String content = this.getBundleMessage(request, "title.mindMapForm." + node.getEntityType()) + ": " + node.getId();
		content = StringUtil.formatWordToFreeMind(content);
	    return content;
	}
	
	private long getNextNodeId(){
		return ++nodeIdSeq;
	}
}
