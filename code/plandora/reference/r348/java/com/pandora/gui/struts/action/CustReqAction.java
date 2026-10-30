package com.pandora.gui.struts.action;

import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.AttachmentTO;
import com.pandora.CategoryTO;
import com.pandora.CustomerTO;
import com.pandora.DiscussionTopicTO;
import com.pandora.LeaderTO;
import com.pandora.MetaFieldTO;
import com.pandora.OccurrenceTO;
import com.pandora.PlanningRelationTO;
import com.pandora.PreferenceTO;
import com.pandora.ProjectTO;
import com.pandora.RepositoryFilePlanningTO;
import com.pandora.RequirementTO;
import com.pandora.ResourceTO;
import com.pandora.RootTO;
import com.pandora.TransferObject;
import com.pandora.UserTO;
import com.pandora.bus.occurrence.Occurrence;
import com.pandora.delegate.CategoryDelegate;
import com.pandora.delegate.DiscussionTopicDelegate;
import com.pandora.delegate.MetaFieldDelegate;
import com.pandora.delegate.OccurrenceDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.RepositoryDelegate;
import com.pandora.delegate.RequirementDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.exception.MandatoryMetaFieldBusinessException;
import com.pandora.exception.MetaFieldNumericTypeException;
import com.pandora.gui.struts.form.CustReqForm;
import com.pandora.gui.struts.form.GeneralStrutsForm;
import com.pandora.gui.taglib.decorator.RepositoryEntityCheckBoxDecorator;
import com.pandora.helper.DateUtil;
import com.pandora.helper.SessionUtil;
import com.pandora.helper.StringUtil;


/**
 * This class handle the actions performed into Customer Request form
 */
public class CustReqAction extends GeneralStrutsAction {

	/**
	 * Shows the Manage Customer Request form
	 */
	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response){

		final String forward = "showCustRequest";
		
		try {

		    //clear current form
		    this.clearForm(form, request);
		    this.clearMessages(request);
		    
		    request.getSession().setAttribute("attachmentList", new Vector<AttachmentTO>());
		    
			//get all requests from data base and put into http session (to be displayed by grid)
			this.refreshList(request, form, null);
		    
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showReportForm", e);
		}

		return mapping.findForward(forward);
	}

	
	public ActionForward navigate(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		return mapping.findForward("showCustRequest");
	}	
	
	
	public ActionForward refreshAfterAttach(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showCustRequest";
		final RequirementDelegate rdel = new RequirementDelegate();		
		try {
		    final CustReqForm reqfrm = (CustReqForm)form;
		    
		    if (reqfrm.getReqNum()!=null) {
				
		    	//set current operation status for Updating	
				reqfrm.setSaveMethod(GeneralStrutsForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));
				
				//create a user object used such a filter
				final RequirementTO filter = new RequirementTO();
				filter.setId(reqfrm.getReqNum());
				
				//get a specific Requirement TO from data base
				final RequirementTO rto = rdel.getRequirement(filter);
				
				//put the data (from DB) into html fields
				this.getActionFormFromTransferObject(rto, reqfrm, request);

				//get all requests from data base and put into http session (to be displayed by grid)
				this.refreshList(request, form, rto);

				reqfrm.setIsPreApproveRequest("off");
				reqfrm.setEstimTime("");		    	
				
		    } else {
		    	request.getSession().setAttribute("attachmentList", new Vector<AttachmentTO>());
		    }
								
		} catch(final BusinessException e){
			this.setErrorFormSession(request, "error.showReportForm", e);
		}

		return mapping.findForward(forward);
	}

	
	/**
	 * Refresh requirement form 
	 */
	public ActionForward refreshRequest(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showCustRequest";
		
		try {
		    //clear current messages
		    this.clearMessages(request);
		    
			//get all requests from data base and put into http session (to be displayed by grid)
			this.refreshList(request, form, null);
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showReqForm", e);
		}
	    return mapping.findForward(forward);
	}

	
	/**
	 * Event called after change the project combo  
	 */
	public ActionForward refreshProject(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showCustRequest";

		try {
		    this.refreshDinamicLists(request, form);
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showReqForm", e);
		}
	    return mapping.findForward(forward);
	}
	
	
	/**
	 * Clear all values of current form.
	 */
	public ActionForward clear(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showCustRequest";

		try {
		    this.clearForm(form, request);
		    this.clearMessages(request);
		
            //get all requests from data base and put into http session (to be displayed by grid)
            this.refreshList(request, form, null);
            
        } catch (final BusinessException e) {
            this.setErrorFormSession(request, "error.showReqForm", e);
        }
		
		return mapping.findForward(forward);		
	}	
	
	
	/**
	 * Clear all values of current form.
	 */
	private void clearForm(ActionForm form, HttpServletRequest request){
	    final CustReqForm crfrm = (CustReqForm)form;
	    crfrm.clear();
		
		//set current requester
	    final UserTO uto = SessionUtil.getCurrentUser(request);
	    crfrm.setRequester(uto.getUsername());
	    crfrm.setRequesterId(uto.getId());

		//set current operation status for Insertion
	    crfrm.setSaveMethod(CustReqForm.INSERT_METHOD, uto);

		request.getSession().removeAttribute("metaFieldList");
		request.getSession().removeAttribute("discussionTopicList");
		request.getSession().removeAttribute("attachmentList");
	    request.getSession().setAttribute("reqRelationshipList", new Vector<PlanningRelationTO>());

	    final String defProj = uto.getPreference().getPreference(PreferenceTO.CUSTHOME_DEF_PROJ);
	    if (!defProj.equals("0")){
	        crfrm.setProjectRelated(defProj);  
	    }
	}

	
	/**
	 * This method prepare the form for updating a requirement. This method get the 
	 * information of specific requirement from data base and put the data into the
	 * html fields.
	 */
	public ActionForward editRequest(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showCustRequest";
		
	    try {
		    final CustReqForm reqfrm = (CustReqForm)form;
		    reqfrm.setComment("");
			final RequirementDelegate rdel = new RequirementDelegate();
			
			//clear messages of form
			this.clearMessages(request);
			
			//set current operation status for Updating	
			reqfrm.setSaveMethod(GeneralStrutsForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));
			
			//create a user object used such a filter
			final RequirementTO filter = new RequirementTO();
			filter.setId(reqfrm.getId());
			
			//get a specific Requirement TO from data base
			final RequirementTO rto = rdel.getRequirement(filter);
			
			//put the data (from DB) into html fields
			this.getActionFormFromTransferObject(rto, reqfrm, request);

			//get all requests from data base and put into http session (to be displayed by grid)
			this.refreshList(request, form, rto);
		    this.refreshArtifacts(mapping, form, request, response);
		    
			reqfrm.setIsPreApproveRequest("off");
			reqfrm.setEstimTime("");

		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.prepareUpdateReqForm", e);
		} catch(final NullPointerException e){
		    this.setErrorFormSession(request, "error.prepareUpdateReqForm", e);		    
	    }

		return mapping.findForward(forward);		
	}

	
	/**
	 * This method is performed after Save button click event.<br>
	 * Is used to update or insert data of requirement into data base.
	 */
	public ActionForward saveRequirement(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final UserDelegate udel = new UserDelegate();
	    
		final String forward = "showCustRequest";
		String errorMsg = "error.showReqForm";
		String succeMsg = "message.success";
		
		try {
			final CustReqForm reqfrm = (CustReqForm)form;
			final RequirementDelegate rdel = new RequirementDelegate();
			final UserTO uto = SessionUtil.getCurrentUser(request);
			
			//save last project selected
			final PreferenceTO prto = new PreferenceTO(PreferenceTO.CUSTHOME_DEF_PROJ, reqfrm.getProjectRelated(), uto); 
			uto.getPreference().addPreferences(prto);
			
			//create an RequirementTO object based on html fields
			final RequirementTO rto = this.getTransferObjectFromActionForm(reqfrm, request);
									
			if (rto.getRequester()!=null && !rto.getRequester().getId().equals("-1")) {
				
				//set the bundle into requester user
				CustomerTO requester = rto.getRequester();
				requester.setBundle(uto.getBundle());
				requester.setProject(rto.getProject());
				requester = udel.getCustomer(requester);
				
				if (requester!=null) {
					
					rto.setRequester(requester);

					if (reqfrm.getSaveMethod().equals(GeneralStrutsForm.INSERT_METHOD)){			    
					    errorMsg = "error.insertReqForm";
					    succeMsg = "message.insertReq";
					    rto.setCreationDate(DateUtil.getNow());
					    rdel.insertRequirement(rto);
					    this.clearForm(form, request);
					} else {
					    errorMsg = "error.updateReqForm";
					    succeMsg = "message.updateReq";
					    rdel.updateRequirement(rto);
					}
					
					//set success message into http session
					this.setSuccessFormSession(request, succeMsg);
									
				} else {
					this.setErrorFormSession(request, "validate.requestForm.adjustRoleCust", null);	
				}
				
			} else {
				this.setErrorFormSession(request, "validate.requestForm.requester", null);
			}
			
			//get all Requirements from data base and put into http session (to be displayed by grid)
			this.refreshList(request, form, rto);
			
		} catch(final MandatoryMetaFieldBusinessException e) {
			this.setErrorFormSession(request, "errors.required", e.getAfto().getMetaField().getName(), null, null, null, null, e);			
		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch(final Exception e){
		    this.setErrorFormSession(request, errorMsg, e);		    
		}
		return mapping.findForward(forward);		
	}
	

	public ActionForward refreshAfterTopicDiscussion(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showCustRequest";
		
	    try {
		    final CustReqForm reqfrm = (CustReqForm)form;
		    this.getDiscussionData(request, reqfrm);
		    
		} catch(final Exception e){
		    this.setErrorFormSession(request, "error.showReqForm", e);
		}
		return mapping.findForward(forward);		
	}


	public ActionForward refreshAfterMetaFieldUpdate(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){   
		final String forward = "showCustRequest";
		return mapping.findForward(forward);		
	}	
	
	/**
	 * Remove a selected Requirement from data base.
	 */
	public ActionForward giveUpRequest(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){   
		String forward = "showCustRequest";
		try {
			final CustReqForm reqfrm = (CustReqForm)form;
			final RequirementDelegate rdel = new RequirementDelegate();
			
			final String fwd = request.getParameter("nextFwd");
			if (fwd!=null) {
				forward = fwd;
			}
			
			//clear form and messages
			this.clearMessages(request);
			
			//create an RequirementTO object based on html fields
			RequirementTO rto = new RequirementTO();
			rto.setId(reqfrm.getId());
			rto = rdel.getRequirement(rto);
			
			//remove Requirement from data base
			rdel.removeRequirement(rto);
			
			//get all Requirements from data base and put into http session (to be displayed by grid)
			this.clearForm(reqfrm, request);			
			this.refreshList(request, reqfrm, rto);
			
			//set success message into http session
			this.setSuccessFormSession(request, "message.removeReq");
					
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.removeReqForm", e);
		}
		return mapping.findForward(forward);		
	}	

	
	public ActionForward refreshArtifacts(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showCustRequest";
	    final RepositoryDelegate rdel = new RepositoryDelegate();
		try {
			final CustReqForm reqfrm = (CustReqForm)form;		
		    if (reqfrm.getReqNum()!=null && !reqfrm.getReqNum().equals("")) {
		    	final RequirementTO rto = new RequirementTO(reqfrm.getReqNum());
				final Vector<RepositoryFilePlanningTO> repositoryList = rdel.getFilesFromPlanning(rto);
				request.getSession().setAttribute("repositoryList", repositoryList);
				request.getSession().setAttribute(RepositoryEntityCheckBoxDecorator.REPOSITORY_ENTITY_ID, rto.getId());
		    }			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showReqForm", e);
		}
		return mapping.findForward(forward);
	}
	
	
	/**
	 * Refresh all list of form.
	 */
	private void refreshList(HttpServletRequest request, ActionForm form, RequirementTO rto) throws BusinessException{
		final RequirementDelegate rdel = new RequirementDelegate();
		final ProjectDelegate pdel = new ProjectDelegate();
		final UserDelegate udel = new UserDelegate(); 
		final CustReqForm crfrm = (CustReqForm)form;

		//get filter from list
		final UserTO uto = SessionUtil.getCurrentUser(request);
		
		Vector<RequirementTO> reqList = new Vector<RequirementTO>();
		if (crfrm.getIsAdjustment().equals("off")){
		    reqList = rdel.getListByUser(uto, crfrm.getBoolCloseRequest(), true);
		} else if (crfrm.getIsAdjustmentWithoutProj().equals("off")) {
		    final UserTO currLeader = SessionUtil.getCurrentUser(request);
		    reqList = rdel.getPendingListByUser(currLeader, true);
		} else {
		    ProjectTO pto = new ProjectTO(crfrm.getProjectRelated());
		    pto = pdel.getProjectObject(pto, false);
		    reqList = rdel.getListByProject(pto, "-1", "-1", "-2", "-1", "-1");
		}
		request.getSession().setAttribute("requirementList", reqList);
		
		//get all Projects from data base and put into http session (to be displayed by combo)
		final ProjectDelegate prjdel = new ProjectDelegate();
		final Vector<ProjectTO> prjList = prjdel.getProjectListByUser(uto);
		if (prjList!=null) {
			final Iterator<ProjectTO> pi = prjList.iterator();
			final Vector<ProjectTO> newProjList = new Vector<ProjectTO>();
			while(pi.hasNext()){
			    final ProjectTO buff = pi.next();
			    if (buff.getBollCanAlloc()) {
			    	newProjList.add(buff); 
			    }
			}
			request.getSession().setAttribute("projectList", newProjList);			    
		} else {
			request.getSession().setAttribute("projectList", new Vector<ProjectTO>());			
		}
		
		//define the current project selected
		if (rto!=null) {
		    crfrm.setProjectRelated(rto.getProject().getId());
		} else {
			if (prjList!=null && !prjList.isEmpty()){
				final Iterator<ProjectTO> pi = prjList.iterator();
				while(pi.hasNext()){
					final ProjectTO buff = pi.next();
					if (buff.getCanAlloc()==null || buff.getCanAlloc().equals("1")) {
					    crfrm.setProjectRelated(buff.getId()); 	
				    	break;					    
					}
				}
			}		    
		}
	
		//assembly a list of priorities to be used by html combo		
		final Vector<TransferObject> priorList = this.getPriorityCombo(request);
		request.getSession().setAttribute("priorityList", priorList);
				
		//get all customer from DB based on user id (in order to get the preApproveReq status for each project)
		final Vector<CustomerTO> custList = udel.getCustomerByUser(uto);
		crfrm.setPreApproveList(this.getCustomerHidden(custList, prjList, true));

		//refresh the meta fields related with current project
		this.refreshDinamicLists(request, form);
	}
	
	
	private void refreshDinamicLists(HttpServletRequest request, ActionForm form) throws BusinessException{
	    final UserDelegate udel = new UserDelegate();
	    final MetaFieldDelegate mfdel = new MetaFieldDelegate();	    
	    final CustReqForm crfrm = (CustReqForm)form;

		final Vector<MetaFieldTO> list = mfdel.getListByProjectAndContainer(crfrm.getProjectRelated(), crfrm.getCategoryId(), MetaFieldTO.APPLY_TO_REQUIREMENT);
		request.getSession().setAttribute("metaFieldList", list);
		
		//reload the discussion topics related to the requirement
		this.getDiscussionData(request, crfrm);
		
	    //get all categories from data base and put into http session (to be displayed by combo)
		final ProjectTO pto = new ProjectTO(crfrm.getProjectRelated());
	    final CategoryDelegate cdel = new CategoryDelegate();
	    final Vector<CategoryTO> catlist = cdel.getCategoryListByType(CategoryTO.TYPE_REQUIREMENT, pto, false);
	    request.getSession().setAttribute("categoryList", catlist);	    

	    crfrm.setShowTechComments("off");
	    crfrm.setCanChangeRequester("off");	    
	    final UserTO uto = SessionUtil.getCurrentUser(request);
	    CustomerTO cto = new CustomerTO(uto);
	    cto.setProject(pto);
	    cto = udel.getCustomer(cto);
	    if (cto!=null) {
		    //check if current user could open a requirement of current project in the name of someone else	    	
	    	crfrm.setCanChangeRequester(cto.getBoolCanOpenOtherOwnerReq()?"on":"off");
	    	
	    	crfrm.setShowTechComments(cto.getBoolCanSeeTechComments()?"on":"off");
	    }
	    
	    //create a fake resource to be the first option into combo
		final ResourceTO dummy = new ResourceTO("-1");
		dummy.setName(this.getBundleMessage(request, "label.request.chooseRes"));
		final Vector<ResourceTO> projResList = new Vector<ResourceTO>();
		projResList.add(dummy);

		//get all resources of selected project		
		final Vector<ResourceTO> resourceList = udel.getResourceByProject(pto.getId(), false, true);
		projResList.addAll(resourceList);
		request.getSession().setAttribute("projResList", projResList);

		//get all customer of selected project		
		final Vector<CustomerTO> customerList = udel.getCustomerByProject(pto, false);
		for (final CustomerTO cuto : customerList) {
			if (cuto.getUsername().equals(RootTO.ROOT_USER)) {
				customerList.remove(cuto);
				break;
			}
		}
		request.getSession().setAttribute("projCustomerList", customerList);
		final CustomerTO cdummy = new CustomerTO("-1");
		cdummy.setName(this.getBundleMessage(request, "label.combo.select"));
		customerList.add(0, cdummy);

		
		if (crfrm.getSaveMethod().equals(CustReqForm.INSERT_METHOD)) {
		    crfrm.setIsPreApproveRequest("on");
		} else {
		    crfrm.setIsPreApproveRequest("off");
		}
		
	}

	
	private void getDiscussionData(HttpServletRequest request, CustReqForm crfrm) throws BusinessException{
	    final DiscussionTopicDelegate dtdel = new DiscussionTopicDelegate();		
		if (crfrm.getId()!=null) {
			final Vector<DiscussionTopicTO> listDt = dtdel.getListByPlanning(crfrm.getId());
			request.getSession().setAttribute("discussionTopicList", listDt);			
		}		
	}

	/**
	 * Define the value of reqAccept or PreApprove html hidden based on 
	 * current attribute status of customer for each project. 
	 */
	private String getCustomerHidden(Vector<CustomerTO> custList, Vector<ProjectTO> prjList, boolean isPreApproveAttribute){
	    String response = "";
	    final Iterator<ProjectTO> i = prjList.iterator();
	    while(i.hasNext()){
	        final ProjectTO pto = i.next();
	        
	        boolean value = false;
	        final Iterator<CustomerTO> j = custList.iterator();
	        while(j.hasNext()){
	            final CustomerTO cto = j.next();
	            if (cto.getProject().getId().equals(pto.getId())){
	                if (isPreApproveAttribute) {
	                    value = cto.getBoolPreApproveReq();
	                }
	                break;
	            }
	        }

            if (value){
                response = response + "1";
            } else {
                response = response + "0";
            }
	    }
	    return response;
	}

	
	/**
	 * Create a list of priority objects
	 */
	public Vector<TransferObject> getPriorityCombo(HttpServletRequest request){
	    final Vector<TransferObject> response = new Vector<TransferObject>();
	    for(int i=0; i<=5; i++){
	        final TransferObject to = new TransferObject();
	        to.setId(i+"");
	        to.setGenericTag(this.getResources(request).getMessage(request.getLocale(), "label.requestPriority." + i));
	        response.addElement(to);
	    }
	    return response;
	}

	
	/**
	 * Put data of html fields into TransferObject 
	 * @param frm
	 * @return
	 * @throws BusinessException 
	 */
	private RequirementTO getTransferObjectFromActionForm(CustReqForm frm, HttpServletRequest request) throws MetaFieldNumericTypeException{
	    final RequirementTO rto = new RequirementTO();
	    final UserTO uto = SessionUtil.getCurrentUser(request);
	    final Locale loc = SessionUtil.getCurrentLocale(request);
	    
		rto.setId(frm.getId());
		rto.setDescription(frm.getDescription());
		rto.setProject(new ProjectTO(frm.getProjectRelated()));
		rto.setCategory(new CategoryTO(frm.getCategoryId()));
		rto.setRequester(new CustomerTO(frm.getRequesterId()));
		rto.setSuggestedDate(DateUtil.getDateTime(frm.getSuggestedDate(), super.getCalendarMask(request), uto.getLocale()));
		if (frm.getDeadlineDate()!=null) {
		    rto.setDeadlineDate(DateUtil.getDateTime(frm.getDeadlineDate(), super.getCalendarMask(request), uto.getLocale()));    
		} else {
		    rto.setDeadlineDate(null);
		}
		rto.setPriority(new Integer(frm.getPriority()));
		rto.setIsAdjustment(!frm.getIsAdjustment().equals("off"));
		rto.setIteration(frm.getIteration());
		
		if (frm.getIsPreApproveRequest().equals("on")){
		    rto.setEstimatedTime(new Integer((int)(StringUtil.getStringToFloat(frm.getEstimTime(), loc) * 60)));
		    rto.setPreApprovedReqResource(frm.getSelectedResource());
		} else {
		    rto.setEstimatedTime(null);
		    rto.setPreApprovedReqResource(null);
		}
		rto.setCreationDate(frm.getCreationDate());		
		
		@SuppressWarnings("unchecked") final
		Vector<MetaFieldTO> metaFieldList = (Vector<MetaFieldTO>)request.getSession().getAttribute("metaFieldList");
		super.setMetaFieldValuesFromForm(metaFieldList, request, rto, frm.getAdditionalFields());
		if (metaFieldList!=null) {
		    frm.setAdditionalFields(rto.getAdditionalFields());
		}
		
		@SuppressWarnings("unchecked") final
		Vector<DiscussionTopicTO> discussionTopicList = (Vector<DiscussionTopicTO>)request.getSession().getAttribute("discussionTopicList");
		rto.setDiscussionTopics(discussionTopicList);
		
		//comment for reopening...
		if (frm.getIsReopenRequirement()!=null && frm.getIsReopenRequirement().equals("on")) {
		    rto.setReopening(true);
		    rto.setAdditionalComment(frm.getComment());
		    
		    try {
			    //retrieve from data base the number of reopening...
			    final RequirementDelegate rdel = new RequirementDelegate();
			    RequirementTO filter = new RequirementTO(rto.getId());
			    filter = rdel.getRequirement(filter);
			    rto.setReopeningOccurrences(filter.getReopeningOccurrences());		        
		    } catch (final Exception e){
		        rto.setReopeningOccurrences(new Integer(0));
		    }
		    
		} else {
		    rto.setReopening(false);
		}
		
		//the method bellow CANNOT be set here!!! because user cannot change current status
		//rto.setRequirementStatus()
		
	    return rto;
	}

	
	/**
	 * Put data of TransferObject (from DB) into html fields (ActionForm)
	 * @param uto
	 * @return
	 */
	private void getActionFormFromTransferObject(RequirementTO rto, CustReqForm frm, HttpServletRequest request) throws BusinessException{
		final OccurrenceDelegate ocdel = new OccurrenceDelegate();
		final UserDelegate udel = new UserDelegate();
		
	    final UserTO uto = SessionUtil.getCurrentUser(request);
	    frm.setId(rto.getId());
	    frm.setReqNum(rto.getId());
	    frm.setDescription(rto.getDescription());
	    frm.setProjectRelated(rto.getProject().getId());
	    frm.setCategoryId(rto.getCategory().getId());
	    frm.setRequester(rto.getRequester().getUsername());
	    frm.setRequesterId(rto.getRequester().getId());
	    
	    frm.setPriority(rto.getPriority()+"");
	    frm.setPreviousPriority(frm.getPriority());

	    frm.setCanSeeDiscussion(udel.checkCustomerViewDiscussion(uto, rto.getProject()));

	    if (uto instanceof ResourceTO) {
	    	frm.setCanSeeArtifacts("on");
	    } else {
	    	frm.setCanSeeArtifacts("off");
	    }
	    
	    if (rto.getSuggestedDate()!=null){
	        frm.setSuggestedDate(DateUtil.getDate(rto.getSuggestedDate(), uto.getCalendarMask(), uto.getLocale()));    
	    } else {
	        frm.setSuggestedDate("");   
	    }
	    if (rto.getDeadlineDate()!=null){
	        frm.setDeadlineDate(DateUtil.getDate(rto.getDeadlineDate(), uto.getCalendarMask(), uto.getLocale()));    
	    } else {
	        frm.setDeadlineDate("");   
	    }
	    frm.setCreationDate(rto.getCreationDate());
	    	    
	    //set the additional Fields to current form
	    frm.setAdditionalFields(rto.getAdditionalFields());
	    
	    //check if current user is the leader of requirement project
	    frm.setIsRequesterLeader("off");
	    final Vector<LeaderTO> allLeaders = udel.getLeaderByProject("'" + rto.getProject().getId() + "'");
	    if (allLeaders!=null) {
	        final Iterator<LeaderTO> i = allLeaders.iterator();
	        while(i.hasNext()) {
	            final LeaderTO lto = i.next();
	            if (lto.getId().equals(rto.getRequester().getId())) {
	                frm.setIsRequesterLeader("on");
	                break;
	            }
	        }
	    }
	    
	    
	    if (rto.getIteration()!=null && !rto.getIteration().trim().equals("") 
	    		&& !rto.getIteration().trim().equals("-1")) {
	    	//get the occurrence iteration from data base
	    	OccurrenceTO oto = new OccurrenceTO(rto.getIteration());
	    	oto = ocdel.getOccurrenceObject(oto);
	    	if (oto!=null) {
		    	if (!oto.getStatus().equals(Occurrence.STATE_START) &&
			    		!oto.getStatus().equals(Occurrence.STATE_FINAL_1) &&
			    		!oto.getStatus().equals(Occurrence.STATE_FINAL_2)) {
			    	frm.setIsPriorityChangeAllowed("off");
			    } else {
			    	frm.setIsPriorityChangeAllowed("on");
			    }	    		
	    	}
	    	frm.setIteration(rto.getIteration());
	    }
	    
		request.getSession().setAttribute("attachmentList", rto.getAttachments());
		
		
		request.getSession().setAttribute("reqRelationshipList", rto.getRelationList());
		
	}

}
