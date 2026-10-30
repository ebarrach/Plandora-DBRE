package com.pandora.gui.struts.action;

import java.io.PrintWriter;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
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
import com.pandora.MetaFieldTO;
import com.pandora.OccurrenceTO;
import com.pandora.PreferenceTO;
import com.pandora.ProjectTO;
import com.pandora.ReportTO;
import com.pandora.RepositoryFilePlanningTO;
import com.pandora.RequirementTO;
import com.pandora.ResourceDateAllocTO;
import com.pandora.ResourceTO;
import com.pandora.ResourceTaskAllocTO;
import com.pandora.ResourceTaskTO;
import com.pandora.TaskHistoryTO;
import com.pandora.TaskStatusTO;
import com.pandora.TaskTO;
import com.pandora.TransferObject;
import com.pandora.UserTO;
import com.pandora.delegate.CategoryDelegate;
import com.pandora.delegate.DiscussionTopicDelegate;
import com.pandora.delegate.MetaFieldDelegate;
import com.pandora.delegate.OccurrenceDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.RepositoryDelegate;
import com.pandora.delegate.ResourceTaskAllocDelegate;
import com.pandora.delegate.ResourceTaskDelegate;
import com.pandora.delegate.TaskDelegate;
import com.pandora.delegate.TaskStatusDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.exception.InvalidTaskStateTransitionException;
import com.pandora.exception.InvalidTaskTimeException;
import com.pandora.exception.MandatoryMetaFieldBusinessException;
import com.pandora.exception.MetaFieldNumericTypeException;
import com.pandora.exception.ProjectTasksDiffRequirementException;
import com.pandora.exception.TasksDiffRequirementException;
import com.pandora.exception.WaitPredecessorUpdateException;
import com.pandora.gui.struts.form.GeneralStrutsForm;
import com.pandora.gui.struts.form.ResTaskForm;
import com.pandora.gui.taglib.decorator.HideProjectDecorator;
import com.pandora.gui.taglib.decorator.RepositoryEntityCheckBoxDecorator;
import com.pandora.helper.DateUtil;
import com.pandora.helper.HtmlUtil;
import com.pandora.helper.LogUtil;
import com.pandora.helper.SessionUtil;
import com.pandora.helper.StringUtil;

/**
 * This class handle the actions performed into Resource Task form
 */
public class ResTaskAction extends GeneralStrutsAction {
    
	private final ResourceTaskDelegate rtDeleg = new ResourceTaskDelegate();
    
	/**
	 * Shows the Manage Resource Task form
	 */
	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
		try {
		    //clear current form
		    final ResTaskForm trfrm = (ResTaskForm)form;
		    trfrm.clear();
		    		    
		    //clear the attachments of the task and update the session attribute taskAttachmentList
		    request.getSession().removeAttribute("taskAttachmentList");
		    request.getSession().setAttribute("taskAttachmentList", new Vector<AttachmentTO>());
		    
		    trfrm.setSaveMethod(ResTaskForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));
		    trfrm.setTitleDateMask(this.getBundleMessage(request, "label.resTaskForm.mask"));
		    
		    trfrm.setDateMask(super.getCalendarMask(request));
		    trfrm.setDefaultLocale(SessionUtil.getCurrentLocale(request));
		    
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    final String frmtInput = uto.getPreference().getPreference(PreferenceTO.INPUT_TASK_FORMAT);
		    trfrm.setDecimalInput(!frmtInput.equals("2"));
		    
		    //for adHoc tasks...get the current resource task	
			this.resetAdHocTask(trfrm, request);
		    
		    this.clearMessages(request);
		    this.getActionFormFromTransferObject(trfrm, request);
		    this.defineCursor(trfrm, request);
		    
			//refresh resource task
		    this.refresh(mapping, form, request, response);
		    
		    this.updateDateAllocTimeList(trfrm, null);
		    
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}

		return mapping.findForward(forward);
	}

	
	public ActionForward refreshAfterTopicDiscussion(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showResTask";
	    try {
	    	final ResTaskForm trfrm = (ResTaskForm)form;
		    this.getDiscussionData(request, trfrm);
		    
		} catch(final Exception e){
		    this.setErrorFormSession(request, "error.showReqForm", e);
		}
		return mapping.findForward(forward);		
	}
	
	
	public ActionForward showMTConfirmation(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
		final String forward = "showCloseMacroTaskConfirm";
		this.resetForm(form);		
		return mapping.findForward(forward);		
	}

	
	public ActionForward navigate(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		this.resetForm(form);	
		return mapping.findForward("showResTask");
	}
	
	
	public ActionForward showWorkflow(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		return mapping.findForward("showResTask");
	}
	
	
	public ActionForward grabTask(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		String forward = "showResTask";
		try {
			final UserDelegate udel = new UserDelegate();
			final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
		    final ResTaskForm trfrm = (ResTaskForm)form;
		    this.resetForm(form);
		    
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    
			final String fwd = request.getParameter("fwd");
			if (fwd!=null && !fwd.trim().equals("")) {
				forward = fwd;
			}
			
			final UserTO root = udel.getRoot();
			final ResourceTaskTO rtto = rtdel.getResourceTaskObject(trfrm.getTaskId(), root.getId());
			if (rtto!=null && rtto.getResource()!=null) {
				rtto.setHandler(uto);
				final String comment = this.getBundleMessage(request, "message.changeAssignment.comment");
				rtdel.changeAssignment(rtto, uto, comment);
				
				//refresh resource task
			    this.refresh(mapping, form, request, response);	
			    
			} else {
				this.setErrorFormSession(request, "error.assignmentError", null);	
			}
						
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}

		return mapping.findForward(forward);
	}


	private void resetAdHocTask(ResTaskForm trfrm, HttpServletRequest request) {
		if (trfrm.getIsAdHocTask().equals("on")) {
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    trfrm.setResourceId(uto.getId());
		    trfrm.setActualDate(DateUtil.getDate(DateUtil.getNow(), uto.getCalendarMask(), SessionUtil.getCurrentLocale(request)));
		}
	}


	public ActionForward refreshProject(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    final TaskDelegate tdel = new TaskDelegate();
	    final OccurrenceDelegate odel = new OccurrenceDelegate();
	    final UserDelegate udel = new UserDelegate();
	    
		try {
		    final ResTaskForm trfrm = (ResTaskForm)form;			
			this.resetForm(form);
		    this.loadBillableStatus(trfrm);
			final ProjectTO pto = new ProjectTO(trfrm.getProjectId());

			//get all categories from data base and put into http session (to be displayed by combo)
		    final CategoryDelegate cdel = new CategoryDelegate();
		    final Vector<CategoryTO> catlist = cdel.getCategoryListByType(CategoryTO.TYPE_TASK, pto, false);
		    request.getSession().setAttribute("categoryList", catlist);			
			
		    this.refreshMetaFieldList(request, trfrm);
		    this.getDiscussionData(request, trfrm);
		    
			//get macro-tasks from data base (filter by requirement) and put into http session (to be displayed by combo)
		    RequirementTO rto = null;
		    if (trfrm.getTaskId()!=null && !trfrm.getTaskId().equals("")) {
			    TaskTO tto = new TaskTO(trfrm.getTaskId());
			    tto = tdel.getTaskObject(tto);
			    if (tto!=null) {
			    	rto = tto.getRequirement();	
			    }
		    }
		    final Vector<TaskTO> parentList = tdel.getAvailableParentTaskList(rto, trfrm.getProjectId());
		    request.getSession().setAttribute("taskAvailable", parentList);
		    
			final Vector<OccurrenceTO> iterations = odel.getIterationListByProject(trfrm.getProjectId(), false);
		    request.getSession().setAttribute("iterationList", iterations);

		    final Vector<TransferObject> billableStatusList = new Vector<TransferObject>();
		    billableStatusList.add(new TransferObject("1", super.getBundleMessage(request, "label.yes")));
		    billableStatusList.add(new TransferObject("0", super.getBundleMessage(request, "label.no")));
		    request.getSession().setAttribute("billableStatusList", billableStatusList);
		    		    
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    trfrm.setCanSeeDiscussion(udel.checkCustomerViewDiscussion(uto, pto));
		    
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		
		return mapping.findForward(forward);
	}


	private void loadBillableStatus(ResTaskForm trfrm)	throws BusinessException {		
		final ProjectDelegate pdel = new ProjectDelegate();
		final CategoryDelegate cdel = new CategoryDelegate();
		
		//check if the resource could change the billable status
		
		ProjectTO pto = new ProjectTO(trfrm.getProjectId());
		pto = pdel.getProjectObject(pto, true);
		if (pto!=null) {
		    if (pto.getAllowBillable()!=null){
		    	trfrm.setAllowBillable(pto.getAllowBillable().booleanValue()?"on":"off");
		    } else {
		    	trfrm.setAllowBillable("off");
		    }

			if (trfrm.getIsAdHocTask().equals("on")) {
		    	final CategoryTO cto = cdel.getCategory(new CategoryTO(trfrm.getCategoryId()));
		    	if (cto.getIsBillable()!=null) {
		    		trfrm.setBillableStatus(cto.getIsBillable().booleanValue()?"1":"0");	
		    	}				
			}
		}
	}

	
	public ActionForward showTaskReport(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		ActionForward fwd = null;
		
		try {	    
			final ResTaskForm frm = (ResTaskForm)form;
			final UserDelegate udel = new UserDelegate();
			final UserTO root = udel.getRoot();
			String url = root.getPreference().getPreference(PreferenceTO.TASK_REPORT_URL);
			final String mask = super.getCalendarMask(request);
			
			Timestamp iniRange = null;
			final Locale loc = SessionUtil.getCurrentLocale(request);
			
		    if (frm.getActualDate()!=null) {
		    	iniRange = frm.getDate(false, loc);    
		    } else {
		    	iniRange = frm.getDate(true, loc);
		    }
		    
		    iniRange = DateUtil.getChangedDate(iniRange, Calendar.DATE, frm.getAllocCursor()-1);
			
			final Timestamp finalRange = DateUtil.getChangedDate(iniRange, Calendar.DATE, frm.getStepCursor()-1);
			url = url.replaceAll(ReportTO.PROJECT_ID, frm.getProjectId());
			url = url.replaceAll(ReportTO.INITIAL_RANGE, DateUtil.getDate(iniRange, mask, loc));
			url = url.replaceAll(ReportTO.FINAL_RANGE, DateUtil.getDate(finalRange, mask, loc));
			
			fwd = this.refresh(mapping, form, request, response); 
			frm.setReportTaskURL(url);
			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		return fwd;	
	}
	
	
	public ActionForward refreshCategory(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
		try {
		    final ResTaskForm trfrm = (ResTaskForm)form;			
			this.resetForm(form);
			this.loadBillableStatus(trfrm);
			this.refreshMetaFieldList(request, trfrm);
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		return mapping.findForward(forward);
	}

	
	public ActionForward jumpToSlot(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    final ResTaskForm trfrm = (ResTaskForm)form;
	    this.resetForm(form);
	    final String slot = request.getParameter("newslot");
	    trfrm.setAllocCursor(Integer.parseInt(slot));
		return mapping.findForward(forward);
	}

	
	/**
	 * Refresh list of current resource task
	 */
	public ActionForward refresh(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
	    final TaskDelegate tdel = new TaskDelegate();
	    final UserDelegate udel = new UserDelegate();
	    
		try {
		    final ResTaskForm trfrm = (ResTaskForm)form;
		    this.resetForm(form);
		    ResourceTO rto = new ResourceTO(trfrm.getResourceId());
		    rto.setProject(new ProjectTO(trfrm.getProjectId()));
		    rto = udel.getResource(rto);
		    
		    if (rto!=null) {

		    	//get all Tasks (of current resource) from data base and put 
			    //into http session (to be displayed by grid)		    
		    	final Vector<ResourceTaskTO> taskList = rtdel.getTaskListByResource(rto, trfrm.getBoolCloseRequest());
				request.getSession().setAttribute("taskList", taskList);
		    }
			
			request.getSession().setAttribute("reqAttachList", new Vector<AttachmentTO>());			
		    if (trfrm.getTaskId()!=null && !trfrm.getTaskId().equals("")) {
			    TaskTO tto = new TaskTO(trfrm.getTaskId());
			    tto = tdel.getTaskObject(tto);
			    if (tto!=null) {
				    final RequirementTO reqto = tto.getRequirement();
				    if (reqto!=null) {
				        request.getSession().setAttribute("reqAttachList", reqto.getAttachments());        
				    }
				    this.refreshArtifacts(mapping, form, request, response);
			    }
		    }

			final ProjectDelegate pdel = new ProjectDelegate();
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    Vector<ProjectTO> projtemp = pdel.getProjectListForWork(uto, true, true);
		    
		    final Vector<ProjectTO> projList = new Vector<ProjectTO>();

		    if (projtemp!=null && projtemp.size()>0) {
			    final Iterator<ProjectTO> i = projtemp.iterator();
			    while(i.hasNext()) {
			    	final ProjectTO pto = i.next();
			    	if (!HideProjectDecorator.isHideProject(uto, pto.getId())) {
			    		projList.add(pto);
			    	}
			    }
		    }

		    final String content = this.getHtmlCombo(request, projList);
			trfrm.setProjectHtmlList(content);

	    	projtemp = new Vector<ProjectTO>();
	    	 //insert a dummy project into top of the list
		    final ProjectTO dummy = new ProjectTO("-1");
		    dummy.setName(this.getBundleMessage(request, "validate.manageTask.selectProject"));
		    projtemp.add(dummy);
		    projtemp.addAll(projList);
		    
		    
			//get all projects (of current resource) from data base and put into http session
			request.getSession().setAttribute("projectList", projtemp);
			
		    if (trfrm.getIsAdHocTask().equals("on")) {
				trfrm.setProjectId("-1");
				
				//get all TasksStatus from data base and put into http session (to be displayed by combo)			
				request.getSession().setAttribute("taskStatusList", getTaskStatusList(null));

			} else {
				//get all TasksStatus from data base and put into http session (to be displayed by combo)			
				request.getSession().setAttribute("taskStatusList", getTaskStatusList(trfrm.getTaskStatus()));			    
			}			
			
			this.refreshProject(mapping, form, request, response);
					
		    UserTO root = null;
			try {
				root = udel.getRoot();
				PreferenceTO pto = root.getPreference();
				trfrm.setDisableMandatoryCommentWhenCloseTask(pto.getPreference(PreferenceTO.DISABLE_MANDATORY_COMMENT_WHEN_CLOSE_TASK).equalsIgnoreCase("TRUE")?"on":"off");
			} catch (BusinessException e) {
				e.printStackTrace();
			}
			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}

		return mapping.findForward(forward);
	}
	
	
	public ActionForward refreshArtifacts(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showResTask";
	    final RepositoryDelegate rdel = new RepositoryDelegate();
	    
		try {
		    final ResTaskForm trfrm = (ResTaskForm)form;		
		    if (trfrm.getTaskId()!=null && !trfrm.getTaskId().equals("")) {
		    	final TaskTO tto = new TaskTO(trfrm.getTaskId());
				final Vector<RepositoryFilePlanningTO> repositoryList = rdel.getFilesFromPlanning(tto);
				request.getSession().setAttribute("repositoryList", repositoryList);
				request.getSession().setAttribute(RepositoryEntityCheckBoxDecorator.REPOSITORY_ENTITY_ID, tto.getId());
		    }			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		return mapping.findForward(forward);
	}
	
	
	
	
	private void refreshMetaFieldList(HttpServletRequest request, ResTaskForm trfrm) throws BusinessException{
		final MetaFieldDelegate mfdel = new MetaFieldDelegate();
		if (trfrm.getProjectId()!=null && !trfrm.getProjectId().equals("")) {
			final Vector<MetaFieldTO> mflist = mfdel.getListByProjectAndContainer(trfrm.getProjectId(), trfrm.getCategoryId(), MetaFieldTO.APPLY_TO_TASK);
			request.getSession().setAttribute("metaFieldList", mflist);		    
		} else {
		    request.getSession().setAttribute("metaFieldList", new Vector<MetaFieldTO>());
		}		
	}
	
	
	public ActionForward saveResTaskPreProcess(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    ActionForward forward = mapping.findForward("showResTask");
	    try {
	    	this.preProcessForm(form, request, true);
	        forward = saveResTask(mapping, form, request, response, true);
	    } catch(final MetaFieldNumericTypeException e){
	    	this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch(final Exception e) {
	    	this.setErrorFormSession(request, "error.invalidTaskTransState", e);
	    }
	    return forward;
	}
	
	public String getHtmlCombo(HttpServletRequest request, Vector<ProjectTO> v) {
		String list = "<select id=\"selectedProjectId\" name=\"selectedProjectId\" class=\"textBox\">";
		list = list + "<option value=\"-1\">" + super.getBundleMessage(request, "label.combo.select") + "</option>";
		if (v!=null) {
			list = list + HtmlUtil.getComboOptions("selectedProjectId", v, "textBox", null);
		}
		list = list + "</select>";
		return list;
	}
	
	/**
	 * Save a resource task into data base. 
	 */
	private ActionForward saveResTask(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response, boolean isMTClosingAllowed){
	    final String forward = "showResTask";
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
	    final TaskStatusDelegate tsdel = new TaskStatusDelegate();
	    
		try {
		    final ResTaskForm trfrm = (ResTaskForm)form;
		    this.clearMessages(request);
	    
		    //get the user connected
		    final UserTO uto = SessionUtil.getCurrentUser(request);
		    trfrm.setSaveMethod(ResTaskForm.UPDATE_METHOD, uto);
		    
		    //create a Resource Task object based on html fields
		    final Locale loc = SessionUtil.getCurrentLocale(request);
		    final ResourceTaskTO rtto = this.getTransferObjectFromActionForm(trfrm, loc, request, true);

		    //get data of Task Status selected from data base
		    TaskStatusTO tsto = rtto.getTaskStatus(); 
		    tsto = tsdel.getTaskStatusObject(tsto);

		    //save new status and additional information into database
		    rtto.setHandler(uto);
		    rtdel.changeTaskStatus(rtto, tsto.getStateMachineOrder(), trfrm.getComment(), 
		    		rtto.getTask().getAdditionalFields(), isMTClosingAllowed);

			//set success message into http session
			this.setSuccessFormSession(request, "message.saveResTask");
		    trfrm.clear(); //clear current alloc hash
		    this.getActionFormFromTransferObject(trfrm, request);
		    this.defineCursor(trfrm, request);

			//get all resourceStatys from data base 
			this.refresh(mapping, form, request, response);
			this.resetAdHocTask(trfrm, request);	
			
			this.updateDateAllocTimeList(trfrm, null);

		} catch(final WaitPredecessorUpdateException e){
			this.setErrorFormSession(request, "error.manageTask.waitPredes", e);
		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch(final ProjectTasksDiffRequirementException e){
		    this.setErrorFormSession(request, "error.manageTask.diffPrjReq", e);			
		} catch(final TasksDiffRequirementException e){
			this.setErrorFormSession(request, "error.manageTask.diffReq", e);			
		} catch(final InvalidTaskTimeException e){
			this.setErrorFormSession(request, "validate.resTaskForm.nonOpenZeroTime", e);
		} catch(final InvalidTaskStateTransitionException e){
		    this.setErrorFormSession(request, "error.invalidTaskTransState", e);
		} catch(final MandatoryMetaFieldBusinessException e) {
			this.setErrorFormSession(request, "errors.required", e.getAfto().getMetaField().getName(), null, null, null, null, e);
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.saveResTaskForm", e);
		}

		return mapping.findForward(forward);
	}

	
	public ActionForward showHideAllocatedDays(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showResTask";
		try {
			final ResTaskForm trfrm = (ResTaskForm)form;
			this.resetForm(form);
			
			if (trfrm.getShowAllocatedDays().equals("off")) {
				trfrm.setShowAllocatedDays("on");	
			} else {
				trfrm.setShowAllocatedDays("off");
			}
			
        } catch (final Exception e) {
            this.setErrorFormSession(request, "error.showResTaskForm", e);
        }
	    
		return mapping.findForward(forward);
	}
	

	public ActionForward nextDay(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showResTask";
	    try {
	        final ResTaskForm trfrm = (ResTaskForm)form;	        
	        this.resetForm(form);
	        
		    //increment the cursor (day) of allocation grid 	        
	        this.refreshBeforeSlotJump(request, trfrm, 1);
            
        } catch(final MetaFieldNumericTypeException e){
        	this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
        } catch (final Exception e) {
            this.setErrorFormSession(request, "error.showResTaskForm", e);
        }
		return mapping.findForward(forward);
	}
		
	
	/**
	 * Perform the action after Next button clicking at allocation grid 
	 */
	public ActionForward next(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    try {
	        final ResTaskForm trfrm = (ResTaskForm)form;	        
	        this.resetForm(form);
	        
		    //increment the cursor (week) of allocation grid 	        
	        this.refreshBeforeSlotJump(request, trfrm, trfrm.getStepCursor());
            
        } catch(final MetaFieldNumericTypeException e){
        	this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
        } catch (final Exception e) {
            this.setErrorFormSession(request, "error.showResTaskForm", e);
        }
	    
		return mapping.findForward(forward);
	}


	private Timestamp refreshBeforeSlotJump(HttpServletRequest request, ResTaskForm trfrm, int jumpSize) throws BusinessException {
		final Locale loc = SessionUtil.getCurrentLocale(request);	        

		final String mask = super.getCalendarMask(request);
		String refDate = trfrm.getActualDate();
		if (refDate==null) {
		    refDate = trfrm.getEstimDate();   
		}

		Timestamp actualDate = DateUtil.getDateTime(refDate, mask, loc);	        
		ResourceTaskAllocTO rttoCopy = this.getAllocBySequence(trfrm, 1, true);
		if (rttoCopy==null) {
			this.getTransferObjectFromActionForm(trfrm, loc, request, true);
			rttoCopy = this.getAllocBySequence(trfrm, 1, true);
		}
		this.updateAllocationList(rttoCopy.getResourceTask(), trfrm, request, actualDate);
		
		this.calcActualTimeOnForm(trfrm, loc);

	    int cursor = trfrm.getAllocCursor();
	    cursor = cursor + jumpSize;
	    
	    if (cursor<1) {
	        
	        actualDate = DateUtil.getChangedDate(actualDate, Calendar.DATE, jumpSize);
	        trfrm.setActualDate(DateUtil.getDate(actualDate, mask, loc));
	        
	        //complement the allocation with new slots...	        
	        for (int i=2; i<=-jumpSize; i++) {
	            final ResourceTaskAllocTO rtato = getEmptyResTaskAlloc(rttoCopy.getResourceTask(), i);
	            trfrm.addAllocationList(actualDate, rtato, i, true);		            
	        }
	        
	        trfrm.reorderSequence(actualDate, rttoCopy.getResourceTask());
	        cursor = 1;
	    }
	    
	    
	    trfrm.setAllocCursor(cursor);
		
	    updateDateAllocTimeList(trfrm, DateUtil.getChangedDate(actualDate, Calendar.DATE, cursor-1));
	    
		return actualDate;
	}

	
	public ActionForward previousDay(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    try {
	        final ResTaskForm trfrm = (ResTaskForm)form;	        
	        this.resetForm(form);
	        
		    //decrement the cursor (day) of allocation grid 	        
	        this.refreshBeforeSlotJump(request, trfrm, -1);
	        
	    } catch(final MetaFieldNumericTypeException e){
	    	this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
	    } catch (final Exception e) {
	        this.setErrorFormSession(request, "error.showResTaskForm", e);
	    }
		
		return mapping.findForward(forward);
	}
	
	
	/**
	 * Perform the action after Previous button clicking at allocation grid 
	 */
	public ActionForward previous(ActionMapping mapping, ActionForm form,
								 HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showResTask";
	    try {
	        final ResTaskForm trfrm = (ResTaskForm)form;	        
	        this.resetForm(form);
	        
		    //increment the cursor (week) of allocation grid 	        
	        this.refreshBeforeSlotJump(request, trfrm, -trfrm.getStepCursor());
            
        } catch(final MetaFieldNumericTypeException e){
        	this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
        } catch (final Exception e) {
            this.setErrorFormSession(request, "error.showResTaskForm", e);
        }
	    
		return mapping.findForward(forward);
	}
	

	public ActionForward removeTask(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
	    String forward = "showResTask";
	    final TaskDelegate tdel = new TaskDelegate();
	    
		try {        
	        this.resetForm(form);

			final String taskId = request.getParameter("task_id");
			final String fwd = request.getParameter("nextFwd");
			if (fwd!=null) {
				forward = fwd;
			}
			
			final UserTO uto = SessionUtil.getCurrentUser(request);
			final TaskTO tto = tdel.getTaskObject(new TaskTO(taskId));
			tto.setHandler(uto);
			tdel.removeTaskByOwner(tto);
			
		    this.refresh(mapping, form, request, response);

			this.setSuccessFormSession(request, "message.removeTask");
			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		
		return mapping.findForward(forward);
	}
	
	public ActionForward closeTask(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response) {
		String forward = "showResTask";
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
	    
		try {        
	        this.resetForm(form);

			final String taskId = request.getParameter("task_id");
			final String fwd = request.getParameter("nextFwd");
			final String resourcId = request.getParameter("resource_id");
			
			if (fwd!=null) {
				forward = fwd;
			}
			final UserTO uto = SessionUtil.getCurrentUser(request);
			final ResourceTaskTO rTaskTO = rtdel.getResourceTaskObject(taskId, resourcId);
			
			rTaskTO.setHandler(uto);
			rtdel.changeTaskStatus(rTaskTO, TaskStatusTO.STATE_MACHINE_CLOSE, null, null, false);
			
		    this.refresh(mapping, form, request, response);

			this.setSuccessFormSession(request, "message.closeTask");
			
		} catch(final InvalidTaskTimeException e){
		    this.setErrorFormSession(request, "validate.resTaskForm.nonOpenZeroTime", e);
		} catch(final BusinessException e) {
			this.setErrorFormSession(request, "error.showResTaskForm", e);
		}
		
		return mapping.findForward(forward);
	}
	
	public ActionForward unlinkTask(ActionMapping mapping, ActionForm form,	
			 HttpServletRequest request, HttpServletResponse response){
		
	    String forward = "showResTask";
	    final TaskDelegate tdel = new TaskDelegate();
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
	    
		try {        
	        this.resetForm(form);

			final String taskId = request.getParameter("task_id");
			final String fwd = request.getParameter("nextFwd");
			//final String resourceId = request.getParameter("resource_id");
			if (fwd!=null) {
				forward = fwd;
			}
			
			final UserTO uto = SessionUtil.getCurrentUser(request);
			final TaskTO tto = tdel.getTaskObject(new TaskTO(taskId));
			ResourceTaskTO rtto = new ResourceTaskTO();
			rtto.setTask(tto);
			rtto.setResource((ResourceTO) uto);
			rtto.getResource().setProject(tto.getProject());
			
			tto.setHandler(uto);
			rtdel.unlink(rtto);
			
		    this.refresh(mapping, form, request, response);

			this.setSuccessFormSession(request, "message.unlinkTask");
			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		} 
		
		return mapping.findForward(forward);
	}
	
	public ActionForward approveTask(ActionMapping mapping, ActionForm form,	
			 HttpServletRequest request, HttpServletResponse response){
		
	    String forward = "showResTask";
	    final TaskDelegate tdel = new TaskDelegate();
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
	    
		try {        
	        this.resetForm(form);

			final String taskId = request.getParameter("task_id");
			final String fwd = request.getParameter("nextFwd");
			final String approverId = request.getParameter("approver_id");
			final String resourceId = request.getParameter("resource_id");
			//final String projectId = request.getParameter("project_id");
			final Boolean approved = Boolean.parseBoolean(request.getParameter("approved"));
			if (fwd!=null) {
				forward = fwd;
			}
			
			final UserTO uto = SessionUtil.getCurrentUser(request);
			final TaskTO tto = tdel.getTaskObject(new TaskTO(taskId));
			ResourceTaskTO rtto = new ResourceTaskTO();
			rtto.setTask(tto);
			rtto.setResource(new ResourceTO(resourceId));
			rtto.getResource().setProject(tto.getProject());
			rtto = rtdel.getResourceTaskObject(rtto);
			rtto.setApproved(approved);
			rtto.setApprover(new UserTO(approverId));
			
			rtto.setHandler(uto);
			tto.setHandler(uto);
			
			rtdel.aprroveTask(rtto);
			
		    this.refresh(mapping, form, request, response);

			this.setSuccessFormSession(request, "message.approveTask");
			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.showResTaskForm", e);
		} 
		
		return mapping.findForward(forward);
	}
	

	private Vector<TaskStatusTO> getTaskStatusList(Integer currentStateMachine) throws BusinessException{
	    Vector<TaskStatusTO> response = new Vector<TaskStatusTO>();
	    final TaskStatusDelegate tsdel = new TaskStatusDelegate();
	    final UserDelegate udel = new UserDelegate();
	    final UserTO root = udel.getRoot();
	    PreferenceTO pto = root.getPreference();
	    
	    boolean disableStateMachineSeqTaskStatus = (pto.getPreference(PreferenceTO.DISABLE_STATE_MACHINE_SEQ_TASK_STATUS).equalsIgnoreCase("TRUE")?true:false);
	    																		
	    if (currentStateMachine==null || disableStateMachineSeqTaskStatus) {
	        final TaskStatusTO tsto1 = tsdel.getObjectByStateMachine(TaskStatusTO.STATE_MACHINE_OPEN);
	        response.add(tsto1);
	        final TaskStatusTO tsto2 = tsdel.getObjectByStateMachine(TaskStatusTO.STATE_MACHINE_PROGRESS);
	        response.add(tsto2);
	        final TaskStatusTO tsto3 = tsdel.getObjectByStateMachine(TaskStatusTO.STATE_MACHINE_CLOSE);
	        response.add(tsto3);
	    } else {
		    response = tsdel.getTaskStatusList(currentStateMachine);	        
	    }
	 
	    return response;
	}
	
	
	/**
	 * Put data of html fields into TransferObject 
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	private ResourceTaskTO getTransferObjectFromActionForm(ResTaskForm frm, Locale loc, HttpServletRequest request, boolean isPopulateAlloc) throws BusinessException{
	    final UserDelegate udel = new UserDelegate();
	    final ResourceTaskTO resTask = new ResourceTaskTO();
	    final TaskStatusDelegate tsdel = new TaskStatusDelegate();
	    final TaskDelegate tdel = new TaskDelegate();
	    final String mask = super.getCalendarMask(request);
	    
	    if (frm.getEstimDate()!=null) {
	        resTask.setStartDate(DateUtil.getDateTime(frm.getEstimDate(), mask, loc));

	        int intEstimatedTime = 0;
		    if (frm.isDecimalInput()) {
		    	intEstimatedTime = (int)(StringUtil.getStringToFloat(frm.getEstimTime(), loc) * 60) ;	
		    } else {
		    	intEstimatedTime = StringUtil.getHHMMToInteger(frm.getEstimTime());		    	
		    }
	        resTask.setEstimatedTime(new Integer(intEstimatedTime));
	    }
	    
	    TaskTO tto = new TaskTO(frm.getTaskId());
	    if (frm.getTaskId()!=null && !frm.getTaskId().trim().equals("")) {
	    	tto = tdel.getTaskObject(tto);	
	    }
	    final ProjectTO pto = new ProjectTO(frm.getProjectId());
	    tto.setProject(pto);
	    
	    ResourceTO rto = new ResourceTO(frm.getResourceId());
	    rto.setProject(pto);
	    rto = udel.getResource(rto);
	    if (rto==null && !pto.getId().equals("-1")) {
	        throw new BusinessException("The resource is not part of project id [" + frm.getProjectId() + "]");
	    }
	    
	    resTask.setResource(rto);
	    resTask.setTask(tto);
        
	    final TaskStatusTO tsto = tsdel.getObjectByStateMachine(frm.getTaskStatus());
	    resTask.setTaskStatus(tsto);

	    if (frm.getIsAdHocTask().equals("on")) {
	    	resTask.setAdHocTask(true);
	    }
	    
	    if (frm.getIsCurrentTaskCreator().equals("on")) {
	    	resTask.setCurrentTaskCreator(true);
	        resTask.setAdHocTaskDescription(frm.getTaskDesc());
	        resTask.setAdHocTaskName(frm.getTaskName());
	        resTask.setAdHocCategoryId(frm.getCategoryId());
	        resTask.setResource(rto);
	        tto.setCreatedBy(SessionUtil.getCurrentUser(request));
	    }
	    
	    //update allocation hash into session
	    if (frm.getActualDate()==null) {
	        frm.setActualDate(frm.getEstimDate());
	    }
	    resTask.setActualDate(DateUtil.getDateTime(frm.getActualDate(), mask, loc));
	    if (resTask.getStartDate()==null) {
	    	resTask.setStartDate(resTask.getActualDate());
	    }
	    
	    if (isPopulateAlloc) {
	        this.populateAllocIntoMap(resTask, frm, request);	    	
	    }
        
        resTask.setAllocList(frm.getAllocationInVectorFormat());
	    
	    //recalc the actual time based to the time set into slots
	    resTask.setActualTime(frm.sumarizeEstimatedTimeInMinutes());
	    	    
	    //crop the highest slot that contain a value = zero
	    this.rTrim(frm, resTask);
	    
	    //re-process the sequencing of slots and get the new actualDate (the first slot non-empty)
	    final Timestamp newActualDate = this.recalcActualDate(frm, resTask);
	    resTask.setActualDate(newActualDate);
	    frm.reorderSequence(newActualDate, resTask);
	    
	    if (tsto.getStateMachineOrder().equals(TaskStatusTO.STATE_MACHINE_CLOSE) && 
	    		(resTask.getEstimatedTime()==null || resTask.getEstimatedTime().intValue()==0)) {
	    	resTask.setEstimatedTime(resTask.getActualTime());	    	
	    }
	    
	    resTask.setQuestionAnswer(frm.getQuestionAnswer());
	    
		@SuppressWarnings("rawtypes") final
		Vector<DiscussionTopicTO> discussionTopicList = (Vector)request.getSession().getAttribute("discussionTopicList");
		tto.setDiscussionTopics(discussionTopicList);
	    
		final Vector<MetaFieldTO> metaFieldList = (Vector<MetaFieldTO>)request.getSession().getAttribute("metaFieldList");
		super.setMetaFieldValuesFromForm(metaFieldList, request, tto, frm.getAdditionalFields());
		if (metaFieldList!=null) {
		    frm.setAdditionalFields(tto.getAdditionalFields());
		}
	    
	    if (frm.getParentTaskId().equals("-1")) {
	        tto.setParentTask(null);    
	    } else {
	        tto.setParentTask(new TaskTO(frm.getParentTaskId()));
	    }
		
	    if (frm.getIterationId()!=null && !frm.getIterationId().equals("-1")) {
	    	tto.setIteration(frm.getIterationId());	
	    } else {
	    	tto.setIteration(null);
	    }

	    resTask.setBillableStatus(new Boolean(frm.getBillableStatus().equals("1")));
	    
        return resTask;
	}

	
	/**
	 * Put data of TransferObject (from DB) into html fields (ActionForm)
	 */
	private void getActionFormFromTransferObject(ResTaskForm trfrm, HttpServletRequest request) throws BusinessException{
	    final TaskDelegate tdel = new TaskDelegate();
	    final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();	    
	    final Locale loc = SessionUtil.getCurrentLocale(request);
	    final UserTO uto = SessionUtil.getCurrentUser(request);
	    final String mask = uto.getCalendarMask();
	    
	    //create resource, project and task objects related with form...
	    final ResourceTO rto = new ResourceTO(trfrm.getResourceId());
	    if (trfrm.getTaskId()!=null && !trfrm.getTaskId().equals("")) {
		    TaskTO tto = new TaskTO(trfrm.getTaskId());
		    final ProjectTO pto = new ProjectTO(trfrm.getProjectId());
		    rto.setProject(pto);
		    
		    //set data of related task into form     
		    tto = tdel.getTaskObject(tto);
		    trfrm.setTaskName(tto.getName());
		    trfrm.setTaskDesc(tto.getDescription());
		    
		    //set the additional Fields to current form
		    trfrm.setAdditionalFields(tto.getAdditionalFields());

		    //set the relationship of task
		    request.getSession().setAttribute("relationshipList", tto.getRelationList());
		    request.getSession().setAttribute("discussionTopicList", new Vector<DiscussionTopicTO>());
		    if (tto.getAttachments()!=null) {
		    	request.getSession().setAttribute("taskAttachmentList", tto.getAttachments());
		    }
		    
		    ResourceTaskTO rtto = new ResourceTaskTO();
		    rtto.setTask(tto);	    
		    rtto.setResource(rto);
		    rtto = rtdel.getResourceTaskObject(rtto);
		    if (rtto.getStartDate()!=null) {
		    	trfrm.setEstimDate(DateUtil.getDate(rtto.getStartDate(), mask, loc));	
		    }
		    if (trfrm.isDecimalInput()) {
		    	trfrm.setEstimTime(StringUtil.getFloatToString(rtto.getEstimatedTimeInHours(), loc));	
		    } else {
		    	trfrm.setEstimTime(StringUtil.getIntegerToHHMM(rtto.getEstimatedTime(), loc));		    	
		    }
		    if (tto.getCategory()!=null) {
		    	trfrm.setCategoryId(tto.getCategory().getId());	
		    }
		    
	    	trfrm.setShowDecisionQuestion(rtto.isTheWIPTask()? "on": "off");
	    	if (rtto.getTask()!=null && rtto.getTask().getDecisionNode()!=null) {
	    		trfrm.setQuestionText(rtto.getTask().getDecisionNode().getQuestionContent());	
	    	}
	    	
		    final Timestamp prefFirstDate = rtDeleg.getPreferedDate(rtto);
			trfrm.setFirstAllocSlot(DateUtil.getDate(prefFirstDate, mask, loc));	        
						
			if (rtto.getTaskStatus().isOpenNotReopened()) {
			    trfrm.setActualDate(null);
			} else {
				final Timestamp tsbuff = rtto.getActualDate()!=null?rtto.getActualDate():rtto.getStartDate();
			    trfrm.setActualDate(DateUtil.getDate(tsbuff, mask, loc));
			}
		    
			if (tdel.isUserTaskOwner(tto, uto)){
				trfrm.setIsCurrentTaskCreator("on");
			} else {
				trfrm.setIsCurrentTaskCreator("off");
			}
			
		    //get the last history object of task in order to get the last comment
		    final TaskHistoryTO thto = tdel.getLastHistoryByTask(rtto);
		    if (thto!=null){
		        trfrm.setComment(thto.getComment());
		    } else {
		        LogUtil.log(this, LogUtil.LOG_ERROR, "Error. The resourceTask: [" + rtto.getId() + "] does not contain any history record.");
		    }
		    
		    trfrm.setTaskStatus(rtto.getTaskStatus().getStateMachineOrder());
		    
		    //refreh allocation hash
		    this.populateAllocIntoMap(rtto, trfrm, request);

		    //check if current task is connected to a requirement
		    if (tto.getRequirement()!=null){		        
		        final CustomerTO requester = tto.getRequirement().getRequester();
		        
			    //if allowed, get the name of customer related with requirement	        
		        trfrm.setReqCustomerName(null);
		        if (this.checkIfCanSeeCustomer(pto, uto)){
		            trfrm.setReqCustomerName(requester.getName());    
		        }	        
		    }
		    
		    if (!rtto.getTaskStatus().getStateMachineOrder().equals(TaskStatusTO.STATE_MACHINE_CLOSE)) {
			    if (tdel.isBlocked(tto)) {
			    	final String errorMsg = getBundleMessage(request, "error.blockedTask");
			    	throw new BusinessException(errorMsg);
			    }
		    }
		    
		    if (tto.getParentTask()!=null){
		        trfrm.setParentTaskId(tto.getParentTask().getId());   
		    }else {
		        trfrm.setParentTaskId("-1");
		    }
		    
		    trfrm.setIterationId(tto.getIteration());
		    
		    if (rtto.getBillableStatus()!=null) {
		    	trfrm.setBillableStatus(rtto.getBillableStatus().booleanValue()?"1":"0");
		    } else {
		    	//get default value (copy the billable status of task category...)
		    	trfrm.setBillableStatus(tto.getCategory().getIsBillable().booleanValue()?"1":"0");	
		    }		    
	    }
	}
	
	
	/**
	 * Assembly the allocation hashMap to populate the grid of allocation time.
	 */
	private void populateAllocIntoMap(ResourceTaskTO rtto, ResTaskForm trfrm, HttpServletRequest request) throws BusinessException{
	    //get the prefered date (may be estimated or actual date)	    
	    final Timestamp prefDate = rtDeleg.getPreferedDate(rtto); 
	    
	    if (trfrm.getAllocationList()!=null) {
	        this.updateAllocationList(rtto, trfrm, request, prefDate);
	    } else {
		    //get current allocation list from data base 
		    this.initAllocationHash(rtto, trfrm);
	    }
	    
	    //calculate actual time with values from data base
	    if (!rtto.getTaskStatus().isOpenNotReopened()) {
		    final Locale loc = SessionUtil.getCurrentLocale(request);
	        this.calcActualTimeOnForm(trfrm, loc);		        	        
	    } else {
	        trfrm.setActualTime("0");
	    }
	}


    private void calcActualTimeOnForm(ResTaskForm trfrm, Locale loc) {
		final Integer timeInMinutes = trfrm.sumarizeEstimatedTimeInMinutes();
	    if (trfrm.isDecimalInput()) {
	        final float actualTime = ((float)timeInMinutes.intValue() / 60);	    	
	    	trfrm.setActualTime(StringUtil.getFloatToString(actualTime, loc));
	    } else {
	    	trfrm.setActualTime(StringUtil.getIntegerToHHMM(timeInMinutes, loc));		    	
	    }    		
    }


    private void updateAllocationList(ResourceTaskTO rtto, ResTaskForm trfrm, HttpServletRequest request, Timestamp prefDate) {
        final Locale loc = SessionUtil.getCurrentLocale(request);
        
        //set data from current displayable week into hash 
        for (int i=1; i<=7; i++) {
            final String topValue = request.getParameter("slotTop"+ i);
            final String botValue = request.getParameter("slotBotton"+ i);
            trfrm.updateAllocationList(prefDate, topValue, botValue, (i + trfrm.getAllocCursor() - 1), rtto, loc);
        }
    }


    /**
	 * Get current allocation list from data base, and setup the 
	 * hashMap into current form object.
	 */
    private void initAllocationHash(ResourceTaskTO rtto, ResTaskForm trfrm) throws BusinessException {
        final ResourceTaskAllocDelegate rtadel = new ResourceTaskAllocDelegate();
        
        Vector<ResourceTaskAllocTO> allocFromDB = null;
        if (rtto.getTask()!=null && !rtto.getTask().getId().equals("")) {
            allocFromDB = rtadel.getListByResourceTask(rtto);
        }

        if (allocFromDB!=null && allocFromDB.size()>0) {
            for (int i = 0 ; i<allocFromDB.size(); i++) {
                final ResourceTaskAllocTO rtato = allocFromDB.elementAt(i);
                rtato.setResourceTask(rtto);
                trfrm.addAllocationList(null, rtato, rtato.getSequence().intValue(), false);
            }
        } else {
            final ResourceTaskAllocTO rtato = this.getEmptyResTaskAlloc(rtto, 1);
            trfrm.addAllocationList(null, rtato, 1, true);
        }
        
    }
    
    private void updateDateAllocTimeList(ResTaskForm trfrm, Timestamp actualDate) throws BusinessException{
    	if(actualDate == null){
    		final Locale loc =  trfrm.getCurrentUser().getLocale();
		    
		    if (trfrm.getActualDate()!=null) {
		    	actualDate = trfrm.getDate(false, loc);    
		    } else {
		    	actualDate = trfrm.getDate(true, loc);
		    }
		    
		    actualDate = DateUtil.getChangedDate(actualDate, Calendar.DATE,  (trfrm.getAllocCursor()-1));
    	}
    	final ResourceTaskAllocDelegate rtadel = new ResourceTaskAllocDelegate();
		final ResourceTO rto = new ResourceTO(trfrm.getResourceId());
		final Vector<ResourceDateAllocTO> dateAllocTimeList = rtadel.getResourceDateAllocList(rto, actualDate, DateUtil.getChangedDate(actualDate, Calendar.DATE, 7));
		trfrm.setDateAllocTimeList(dateAllocTimeList);
    }
    
    
    private ResourceTaskAllocTO getEmptyResTaskAlloc(ResourceTaskTO rtto, int seq){
        final ResourceTaskAllocTO response = new ResourceTaskAllocTO();
        response.setAllocTime(new Integer(0));
        response.setResourceTask(rtto);
        response.setSequence(new Integer(seq));
    	return response;
    }

    
    /**
     * Remove the empty alloc unit values on "right to left" direction. 
     * In other words: crop the rightest slot that contain a empty value
     */
    private void rTrim(ResTaskForm frm, ResourceTaskTO rtto) throws BusinessException {
        if (frm.getAllocationList()!=null) {
            for (int i = frm.getAllocationList().size(); i>0; i--){
                final ResourceTaskAllocTO rtato = this.getAllocBySequence(frm, i, true);
                if (rtato!=null) {
                    final Integer value = rtato.getAllocTime();
                    if (value==null || value.intValue()==0){
                        rtto.getAllocList().remove(rtato);
                    } else {
                        break;
                    }                
                }
            }            
        }
    }
    

    /**
     * Re-process the sequencing of slots and return the new actualDate (the first slot non-empty)
     */
    private Timestamp recalcActualDate(ResTaskForm frm, ResourceTaskTO rtto) throws BusinessException {
        Timestamp newActualDate = rtto.getActualDate();
        int seq = 0;
        
        //crop the fist empty slots
        if (frm.getAllocationList()!=null) {
            for (int i=1; i<=frm.getAllocationList().size(); i++){
                final ResourceTaskAllocTO rtato = this.getAllocBySequence(frm, i, true);
                if (rtato!=null) {
                    final Integer value = rtato.getAllocTime();
                    if (value==null || value.intValue()==0){
                        rtto.getAllocList().remove(rtato);
                        seq++;
                        newActualDate = DateUtil.getChangedDate(rtto.getActualDate(), Calendar.DATE, i);
                    } else {
                        break;
                    }
                }
            }

            //if there is not any alloc into list..set the actual date to the previous value
            if (rtto.getAllocList().size()==0) {
                newActualDate = rtto.getActualDate();
            }
            
            //if the process has cropped the alloc list, then rebuild the sequence...
            if (seq>0) {
                final Iterator<ResourceTaskAllocTO> i = rtto.getAllocList().iterator();
                while(i.hasNext()) {
                    final ResourceTaskAllocTO rtato = i.next();
                    final Integer sequence = rtato.getSequence();
                    rtato.setSequence(new Integer((sequence.intValue() - seq + 1)));
                }
            }            
        }
                
        return newActualDate;
    }
    
    /**
	 * Check if current resource can see the owner of requirement (has permission).  
	 */
	private boolean checkIfCanSeeCustomer(ProjectTO pto, UserTO uto) throws BusinessException{
	    final UserDelegate udel = new UserDelegate();
	    final ResourceTO filter = new ResourceTO();
	    filter.setId(uto.getId());
	    filter.setProject(pto);
	    final ResourceTO rto = udel.getResource(filter);
	    return rto.getBoolCanSeeCustomer();
	}	
	
	
    /**
     * Return a allocation object (specific slot) based on sequence
     */
    private ResourceTaskAllocTO getAllocBySequence(ResTaskForm trfrm, int seq, boolean forceAddIntoActual){
        ResourceTaskAllocTO response = null;
        
        HashMap<String,ResourceTaskAllocTO> list = null;
        if (trfrm.getTaskStatus()!=null && trfrm.getTaskStatus().equals(TaskStatusTO.STATE_MACHINE_OPEN) && !forceAddIntoActual) {
        	list = trfrm.getEstimAllocList();
        } else {
        	list = trfrm.getAllocationList();
        }
        
        if (list!=null) {
            final Iterator<ResourceTaskAllocTO> i = list.values().iterator();
            while(i.hasNext()){
                final ResourceTaskAllocTO token = i.next();
                if (token.getSequence().intValue()==seq) {
                    response = token;
                    break;
                }
            }            
        }
        return response;
    }

    
	/**
	 * Define a initial value for the cursor
	 */
	private void defineCursor(ResTaskForm trfrm, HttpServletRequest request) {
		if (trfrm.getFirstAllocSlot()!=null && !trfrm.getFirstAllocSlot().trim().equals("")) {
		    final Timestamp firstDate = DateUtil.getDateTime(trfrm.getFirstAllocSlot(), super.getCalendarMask(request), SessionUtil.getCurrentLocale(request));
		    final int slots = DateUtil.getSlotBetweenDates(firstDate, DateUtil.getNow());
		    final int cursorIdx = (new Double(Math.ceil(slots/7))).intValue();
		    trfrm.setAllocCursor((cursorIdx * 7)+1);
		}
	}

	
	private ResourceTaskTO preProcessForm(ActionForm form, HttpServletRequest request, boolean isPopulateAlloc) throws BusinessException{  
	    final ResTaskForm trfrm = (ResTaskForm)form;
	    this.resetForm(form);
        final Locale loc = this.getLocale(request);
	    final ResourceTaskTO rtto = this.getTransferObjectFromActionForm(trfrm, loc, request, isPopulateAlloc);
	    this.rTrim(trfrm, rtto);
	    
	    return rtto;
	}
	
	
	private void resetForm(ActionForm form){
	    final ResTaskForm trfrm = (ResTaskForm)form;
    	trfrm.setReportTaskURL("");
    	trfrm.setShowWorkflowDiagram("off");
	}
	
	
	public ActionForward clickNodeTemplate(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		try {
			final ShowAllTaskAction showAllAction = new ShowAllTaskAction();
			final ResTaskForm trfrm = (ResTaskForm)form;
	        response.setContentType("text/xml");  
	        response.setHeader("Cache-Control", "no-cache");  
	        
	        final PrintWriter out = response.getWriter();
	        final String content = showAllAction.createDiagramNodeTip(request, trfrm.getPlanningId()).toString();
	        out.println(content);
	        
	        out.flush();
	        
		} catch(final Exception e){
		    this.setErrorFormSession(request, "error.showAllReqForm", e);
		}

		return null;
	}	
	
	private void getDiscussionData(HttpServletRequest request, ResTaskForm rtfrm) throws BusinessException{
	    final DiscussionTopicDelegate dtdel = new DiscussionTopicDelegate();		
		if (rtfrm.getTaskId()!=null) {
			final Vector<DiscussionTopicTO> listDt = dtdel.getListByPlanning(rtfrm.getTaskId());
			request.getSession().setAttribute("discussionTopicList", listDt);			
		}		
	}	
	
	public ActionForward showProjectPopup(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		return mapping.findForward("showChangeProjResTask");
	}
	
	public ActionForward changeProject(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
		try{
			final ResTaskForm trfrm = (ResTaskForm)form;
			final String projectId = request.getParameter("id");
			ResourceTaskTO rtto = this.preProcessForm(trfrm, request, false);
			
			rtto = rtdel.changeProject(projectId, rtto, SessionUtil.getCurrentUser(request));
			
			trfrm.setProjectId(projectId);
			trfrm.setCategoryId(rtto.getTask().getCategory().getId());
			
			this.refresh(mapping, form, request, response);
			
			//set success message into http session
			this.setSuccessFormSession(request, "message.saveResTask");

		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch(final Exception e){
			this.setErrorFormSession(request, "error.showAllTaskForm", e);
		}
		return mapping.findForward("showResTask");
	}
	
	public ActionForward refreshAfterAttach(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		
		final String forward = "showResTask";	
		try {
			final ResTaskForm trfrm = (ResTaskForm)form;
		    
		    if (trfrm.getTaskId()!=null && !trfrm.getTaskId().equals("")) {
		    	trfrm.setSaveMethod(GeneralStrutsForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));
				this.getActionFormFromTransferObject(trfrm, request);		    	
		    } else {
		    	request.getSession().setAttribute("attachmentList", new Vector<AttachmentTO>());
		    }
								
		} catch(final BusinessException e){
			this.setErrorFormSession(request, "error.showReportForm", e);
		}

		return mapping.findForward(forward);
	}	
	
}
