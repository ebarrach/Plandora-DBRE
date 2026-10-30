package com.pandora.gui.struts.action;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.CompanyTO;
import com.pandora.CustomerTO;
import com.pandora.LeaderTO;
import com.pandora.MetaFieldTO;
import com.pandora.PreferenceTO;
import com.pandora.ProjectStatusTO;
import com.pandora.ProjectTO;
import com.pandora.RepositoryPolicyTO;
import com.pandora.ResourceTO;
import com.pandora.RootTO;
import com.pandora.TransferObject;
import com.pandora.UserTO;
import com.pandora.bus.repository.Repository;
import com.pandora.bus.repository.RepositoryBUS;
import com.pandora.delegate.CompanyDelegate;
import com.pandora.delegate.MetaFieldDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.ProjectStatusDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.exception.IncompatibleUsrsBetweenPrjException;
import com.pandora.exception.InvalidProjectUserReplaceException;
import com.pandora.exception.MandatoryMetaFieldBusinessException;
import com.pandora.exception.MetaFieldNumericTypeException;
import com.pandora.exception.ProjectCannotBeClosedException;
import com.pandora.gui.struts.form.GeneralStrutsForm;
import com.pandora.gui.struts.form.ProjectForm;
import com.pandora.gui.struts.form.UserForm;
import com.pandora.gui.taglib.decorator.ProjectGridCheckBoxDecorator;
import com.pandora.helper.DateUtil;
import com.pandora.helper.HtmlUtil;
import com.pandora.helper.SessionUtil;
import com.pandora.helper.StringUtil;

/**
 * This class handle the actions performed into Manage Project form
 */
public class ProjectAction extends GeneralStrutsAction {

	/**
	 * Get a list of user based on filter from html. The data returned by
	 * database must be displayed into combo box on form.
	 */
	public ActionForward searchUser(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		final String forward = "showProject";
		final UserDelegate udel = new UserDelegate();

		try {
			this.clearMessages(request);
			final ProjectForm frm = (ProjectForm) form;

			final Vector<String> kwList = StringUtil.getKeyworks(frm.getUserSearch());
			Vector<UserTO> userList = udel.getListByKeyword(kwList);
			if (userList == null) {
				this.setErrorFormSession(request, "error.project.userSearch", null);
				userList = new Vector<UserTO>();
			}
			request.getSession().setAttribute("UserList", userList);

		} catch (final BusinessException e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}
		return mapping.findForward(forward);
	}

	
	public ActionForward navigate(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return mapping.findForward("showProject");
	}

	
	public ActionForward changeRepository(ActionMapping mapping,
			ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final ProjectForm frm = (ProjectForm) form;
		final Repository rep = RepositoryBUS.getRepositoryClass(frm.getRepositoryClass());
		if (rep != null) {
			frm.setShowRepositoryUserPwd(rep.showUserPwdFields() ? "on" : "off");
		}
		return mapping.findForward("showProject");
	}
	
	
	/**
	 * Add a user into allocation user list.
	 */
	@SuppressWarnings("unchecked")
	public ActionForward addUser(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		final String forward = "showProject";
		final UserDelegate udel = new UserDelegate();

		try {
			// clear current form
			this.clearMessages(request);
			final ProjectForm frm = (ProjectForm) form;

			UserTO uto = new UserTO(frm.getSelectedUserId());
			uto = udel.getUser(uto);
			final CustomerTO cto = new CustomerTO(uto);
			cto.setProject(new ProjectTO(frm.getId()));
			
			@SuppressWarnings("rawtypes")
			Vector<UserTO> allocList = (Vector) request.getSession().getAttribute("allocList");
			if (allocList == null) {
				allocList = new Vector<UserTO>();
			} else {
				request.getSession().removeAttribute("allocList");
			}
			final Vector<UserTO> temp = new Vector<UserTO>();
			temp.addElement(cto);
			temp.addAll(allocList);
			request.getSession().setAttribute("allocList", temp);

		} catch (final BusinessException e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}
		return mapping.findForward(forward);
	}
	

	/**
	 * Remove a user object from session list of allocation users.
	 */
	@SuppressWarnings("unchecked")
	public ActionForward removeUser(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		final String forward = "showProject";

		try {
			// clear current form
			this.clearMessages(request);
			final ProjectForm frm = (ProjectForm) form;

			final Vector<UserTO> allocList = (Vector<UserTO>) request.getSession().getAttribute("allocList");
			if (allocList != null) {
				final Iterator<UserTO> i = allocList.iterator();
				int c = 0;
				while (i.hasNext()) {
					final UserTO uto = i.next();
					if (uto.getId().equals(frm.getRemovedUserId())) {
						request.getSession().removeAttribute("allocList");
						allocList.remove(c);
						final Vector<UserTO> temp = new Vector<UserTO>();
						temp.addAll(allocList);
						request.getSession().setAttribute("allocList", temp);
						break;
					}
					c++;
				}
			}

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}
		return mapping.findForward(forward);
	}

	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		return prepareProject(mapping, form, request,response);
	}
	
	/**
	 * Shows the Manage User form
	 */
	public ActionForward prepareProject(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		final String forward = "showProject";

		try {
			// clear current form
			this.clearForm(form, request);

			// setting current locale for struts form
			final Locale loc = SessionUtil.getCurrentLocale(request);
			final ProjectForm frm = (ProjectForm) form;
			frm.setUserLocale(loc);

			// get all open Projects and Project Status List
			this.refreshAuxLists(form, request);

			// get all projects from data base and put into http session (to be
			// displayed by grid)
			this.refreshList(request);

		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}

		return mapping.findForward(forward);
	}

	
	/**
	 * This method prepare the form for updating a project. This method get the
	 * information of specific project from data base and put the data into the
	 * html fields.
	 */
	public ActionForward editProject(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		final String forward = "showProject";
		try {
			// clear current message of form
			this.clearMessages(request);
			final ProjectForm frm = (ProjectForm) form;

			// set current operation status for Updating
			frm.setSaveMethod(UserForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));

			// get data of project from data base
			final ProjectDelegate pdel = new ProjectDelegate();
			ProjectTO pto = this.getTransferObjectFromActionForm(frm, request);
			pto = pdel.getProjectObject(pto, false);
			frm.setCreationDate(pto.getCreationDate());

			if (pto != null) {
				final Repository rep = RepositoryBUS.getRepositoryClass(pto.getRepositoryClass());
				if (rep != null) {
					frm.setShowRepositoryUserPwd(rep.showUserPwdFields() ? "on" : "off");
				} else {
					frm.setShowRepositoryUserPwd("off");
				}
			}
			
			// get all open Projects and Project Status List
			this.refreshAuxLists(form, request);

			// set data of project into html fields
			this.getActionFormFromTransferObject(pto, frm, request);

			// set data of allocated users and occurrences
			if (pto.getAllocUsers() != null) {
				request.getSession().setAttribute("allocList", this.getAllocUsersToViewLayer(pto));
			}
			
			if (request.getSession().getAttribute("projectList")==null) {
				refreshList(request);	
			}
			
			
		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}

		return mapping.findForward(forward);
	}
	

	private Object getAllocUsersToViewLayer(ProjectTO pto) {
		final Vector<UserTO> response = new Vector<UserTO>();
		if (pto!=null && pto.getAllocUsers()!=null) {
			for (final UserTO uto : pto.getAllocUsers()) {
				if (uto.getUsername()!=null && !uto.getUsername().equals(RootTO.ROOT_USER)) {
					response.add(uto);
				}
			}
		}
		return response;
	}


	/**
	 * This method is performed after Save button click event.<br>
	 * Is used to update or insert data of project into data base.
	 */
	public ActionForward saveProject(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {

		final String forward = "showProject";
		String errorMsg = "error.showProjForm";
		String succeMsg = "message.success";

		try {
			final ProjectForm frm = (ProjectForm) form;
			final ProjectDelegate pdel = new ProjectDelegate();
			this.clearMessages(request);

			// create a Project object based on html fields
			final ProjectTO pto = this.getTransferObjectFromActionForm(frm, request);

			if (frm.getSaveMethod().equals(GeneralStrutsForm.INSERT_METHOD)) {
				errorMsg = "error.insertProjForm";
				succeMsg = "message.insertProj";
				pdel.insertProject(pto);
				this.clearForm(form, request);
			} else {
				errorMsg = "error.updateProjForm";
				succeMsg = "message.updateProj";
				pdel.updateProject(pto);

				// refresh data into form...
				editProject(mapping, form, request, response);
			}

			// set success message into http session
			this.setSuccessFormSession(request, succeMsg);

			// get all projects from data base and put into http session (to be
			// displayed by grid)
			this.refreshList(request);
			if (frm.getSaveMethod().equals(GeneralStrutsForm.INSERT_METHOD)) {
				this.reloadParentProjectList(request, null);
			} else {
				this.reloadParentProjectList(request, pto);	
			}
			
		} catch(final MandatoryMetaFieldBusinessException e){
			this.setErrorFormSession(request, "errors.required", e.getAfto().getMetaField().getName(), null, null, null, null, e);
		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final ProjectCannotBeClosedException e) {
			this.setErrorFormSession(request, "error.project.projectCannotBeClosed", e);
		} catch (final InvalidProjectUserReplaceException e) {
			this.setErrorFormSession(request, "error.project.InvalidProjectUserReplace", e);
		} catch (final IncompatibleUsrsBetweenPrjException e) {
			this.setErrorFormSession(request, "error.project.imcompatibleCustomers", e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, errorMsg, e);
		}
		return mapping.findForward(forward);
	}

	
	/**
	 * Refresh requirement form
	 */
	public ActionForward refreshList(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		final String forward = "showProject";

		try {
			// clear current messages
			this.clearMessages(request);

			// get all projects from data base and put into http session (to be
			// displayed by grid)
			this.refreshList(request);

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}
		return mapping.findForward(forward);
	}

	
	/**
	 * Clear all values of current form.
	 */
	public ActionForward clear(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		final String forward = "showProject";
		try {
			request.getSession().removeAttribute("parentPrjStatusList");
			this.clearForm(form, request);
			this.clearMessages(request);

			// get all open Projects and Project Status List
			this.refreshAuxLists(form, request);
			this.reloadParentProjectList(request, null);

		} catch(final MetaFieldNumericTypeException e){
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}

		return mapping.findForward(forward);
	}

	/**
	 * Clear all values of current form.
	 */
	private void clearForm(ActionForm form, HttpServletRequest request) {
		final ProjectForm prjfrm = (ProjectForm) form;
		prjfrm.clear();
		prjfrm.setCreationDate(DateUtil.getNow());

		// set current operation status for Insertion
		prjfrm.setSaveMethod(ProjectForm.INSERT_METHOD, SessionUtil
				.getCurrentUser(request));

		// set empty to user list
		request.getSession().setAttribute("UserList", new Vector<UserTO>());
		request.getSession().setAttribute("allocList", new Vector<UserTO>());
	}

	/**
	 * Get information about all open Projects and Project Status List from data
	 * base.
	 */
	private void refreshAuxLists(ActionForm form, HttpServletRequest request) throws BusinessException {
		final MetaFieldDelegate mfdel = new MetaFieldDelegate();
		final UserDelegate udel = new UserDelegate();
		final ProjectDelegate prjdel = new ProjectDelegate();
		final CompanyDelegate comdel = new CompanyDelegate();

		final ProjectForm frm = (ProjectForm) form;
		ProjectTO pto = this.getTransferObjectFromActionForm(frm, request);
		if (pto.getProjectStatus() == null || pto.getProjectStatus().getId() == null) {
			pto = prjdel.getProjectObject(pto, true);
		}

		this.reloadParentProjectList(request, pto);

		// get Project Status List
		final ProjectStatusDelegate psdel = new ProjectStatusDelegate();
		if (pto != null) {
			final Vector<ProjectStatusTO> prjStsList = psdel.getProjectStatusListByProject(pto);
			request.getSession().setAttribute("parentPrjStatusList", prjStsList);
		} else {
			if (request.getSession().getAttribute("parentPrjStatusList")==null) {
				final Vector<ProjectStatusTO> prjStsList = new Vector<ProjectStatusTO>();
				prjStsList.add(psdel.getProjectStatus(ProjectStatusTO.STATE_MACHINE_OPEN));
				request.getSession().setAttribute("parentPrjStatusList", prjStsList);	
			}
		}

		// clear search user combo box
		request.getSession().setAttribute("UserList", new Vector<UserTO>());

		if (!frm.getId().equals("")) {
			final Vector<MetaFieldTO> list = mfdel.getListByProjectAndContainer(frm.getId(), null, MetaFieldTO.APPLY_TO_PROJECT);
			request.getSession().setAttribute("metaFieldList", list);
		} else {
			request.getSession().setAttribute("metaFieldList", new Vector<MetaFieldTO>());
		}
		
		Vector<CompanyTO> companyList = comdel.getCompanyList();
		companyList.add(0, new CompanyTO("-1"));
		request.getSession().setAttribute("projectCompanyList", companyList);
		

		final Vector<TransferObject> canAllocList = new Vector<TransferObject>();
		canAllocList.add(new TransferObject("1", this.getBundleMessage(request, "label.yes")));
		canAllocList.add(new TransferObject("0", this.getBundleMessage(request, "label.no")));
		request.getSession().setAttribute("canAllocList", canAllocList);

		final Vector<TransferObject> repositoryList = new Vector<TransferObject>();
		repositoryList.add(new TransferObject("-1", this.getBundleMessage(request, "label.none")));
		
		// get repository list by repository class names
		final UserTO root = udel.getRoot();
		final String repClasses = root.getPreference().getPreference(PreferenceTO.REPOSITORY_BUS_CLASS);
		if (repClasses != null) {
			final String[] classList = repClasses.split(";");
			if (classList != null && classList.length > 0) {
				for (int i = 0; i < classList.length; i++) {
					final String classStr = classList[i].trim();
					final Repository repbus = RepositoryBUS
							.getRepositoryClass(classStr);
					String repName = "[" + classStr + "] - Load Error"; // default
																		// value
					String repId = "-1";
					if (repbus != null) {
						repName = repbus.getUniqueName();
						repId = classStr;
					}
					final TransferObject rep2 = new TransferObject(repId, this
							.getBundleMessage(request, repName, true));
					repositoryList.add(rep2);
				}
			}
		}
		request.getSession().setAttribute("repositoryList", repositoryList);
	}

	
	private String renderHtmlQualifications(ProjectTO pto, HttpServletRequest request) {
		String response = "";
		int colNumber = 0;
	
		if (pto.getQualifications()!=null) {
			for (final MetaFieldTO mfto : pto.getQualifications()) {
				
				final String label = super.getBundleMessage(request, mfto.getName(), true);
				final String content = HtmlUtil.getComboBox("QUALI_" + mfto.getId(), mfto.getDomain(), "textBox", mfto.getGenericTag(), request.getSession(), 0, null, false);
				colNumber++;
				
				if (colNumber==1) {
					response = response + "<tr class=\"gapFormBody\"><td>&nbsp;</td>" +
										  "<td class=\"formTitle\">" + label + ":&nbsp;</td><td>" + content + "</td>";
				} else {
					response = response + "<td class=\"formTitle\">" + label + ":&nbsp;</td><td>" + content + "</td>";
					if (colNumber==3) {
						response = response + "</tr>\n";
						colNumber=0;	
					}
				}

			}			
		}
	
		return response;
	}


	/**
	 * get all open Projects from data base and put into http session (to be displayed by combo)
	 */
	private void reloadParentProjectList(HttpServletRequest request, ProjectTO pto) throws BusinessException {
		final ProjectDelegate prjdel = new ProjectDelegate();
		Vector<ProjectTO> prjList = null;
		
		// get current user
		final UserTO uto = SessionUtil.getCurrentUser(request);
		final LeaderTO lto = new LeaderTO(uto);
		
		if (lto.getUsername().equals(RootTO.ROOT_USER)) {
			prjList = prjdel.getProjectRoot();
		} else {
			prjList = prjdel.getProjectListByUser(lto);
			if (prjList!=null) {
				
				if (pto!=null) {
					for (final ProjectTO item : prjList) {
						if (item.getId().equals(pto.getId())) {
							prjList.remove(item);
							break;
						}
					}					
				}
				
				ProjectTO rootParent = new ProjectTO(ProjectTO.PROJECT_ROOT_ID);
				rootParent = prjdel.getProjectObject(rootParent, true);
				prjList.add(0, rootParent);				
			}
		}
		request.getSession().setAttribute("parentPrjList", prjList);
	}

	
	/**
	 * Refresh grid with list of all projects from data base.
	 */
	private void refreshList(HttpServletRequest request)
			throws BusinessException {
		final ProjectDelegate pdel = new ProjectDelegate();

		// get current user
		final UserTO uto = SessionUtil.getCurrentUser(request);
		final LeaderTO lto = new LeaderTO(uto);

		Vector<ProjectTO> projList = new Vector<ProjectTO>();
		if (uto.getUsername().toLowerCase().toLowerCase().equals(RootTO.ROOT_USER)) {
			projList = pdel.getProjectRoot();
		} else {
			projList = pdel.getProjectListByUser(lto);
		}
		request.getSession().setAttribute("projectList", projList);
	}

	/**
	 * Put data of TransferObject (from DB) into html fields (ActionForm)
	 * @throws BusinessException 
	 */
	private void getActionFormFromTransferObject(ProjectTO to, ProjectForm frm,
			HttpServletRequest request) throws BusinessException {
		final UserDelegate udel = new UserDelegate();
		final Locale loc = SessionUtil.getCurrentLocale(request);

		frm.setDescription(to.getDescription());
		frm.setId(to.getId());
		if (to.getParentProject() != null) {
			frm.setParentProject(to.getParentProject().getId());
		} else {
			frm.setParentProject("");
		}
		frm.setProjectStatus(to.getProjectStatus().getId());
		frm.setName(to.getName());
		frm.setCanAlloc(to.getCanAlloc());
		if (to.getAllowBillable() != null) {
			frm.setAllowBillable(to.getAllowBillable().booleanValue());
		}

		frm.setRepositoryClass(to.getRepositoryClass());
		frm.setRepositoryPass(to.getRepositoryPass());
		frm.setRepositoryURL(to.getRepositoryURL());
		frm.setRepositoryUser(to.getRepositoryUser());

		if (to.getEstimatedClosureDate() != null) {
			frm.setEstimatedClosureDate(DateUtil.getDate(to
					.getEstimatedClosureDate(), super.getCalendarMask(request),
					loc));
		} else {
			frm.setEstimatedClosureDate(null);
		}

		// setting current locale for struts form
		frm.setUserLocale(loc);

		// set the additional Fields to current form
		frm.setAdditionalFields(to.getAdditionalFields());
		frm.setRepositoryPolicies(to.getRepositoryPolicies());
		
		if (to.getCompany()!=null) {
			frm.setProjectCompany(to.getCompany().getId());			
		}
		
		if (to.getBudget()!=null) {
	        final double val = to.getBudget().doubleValue() / 100;
	        final String valStr = StringUtil.getDoubleToString(val, "0.00" , loc);
	        frm.setBudget(valStr);			
		}
		
        final Locale locCurrency = udel.getCurrencyLocale();
		final NumberFormat nf = NumberFormat.getCurrencyInstance(locCurrency);
		final String cs = nf.getCurrency().getSymbol(loc);
		frm.setBudgetCurrencySymbol(cs);
		
		if (to.getCompany()!=null && to.getCompany().getId()!=null) {
			frm.setProjectCompany(to.getCompany().getId());
		} else {
			frm.setProjectCompany("-1");			
		}
		
		frm.setHtmlQualifications(this.renderHtmlQualifications(to, request));
		
	}

	/**
	 * Put data of html fields into TransferObject
	 * @throws BusinessException 
	 */
	private ProjectTO getTransferObjectFromActionForm(ProjectForm frm, HttpServletRequest request) throws BusinessException {
		final ProjectTO pto = new ProjectTO();
		final Locale loc = SessionUtil.getCurrentLocale(request);
		final ProjectDelegate pdel = new ProjectDelegate();

		pto.setCreationDate(frm.getCreationDate());
		if (frm.getEstimatedClosureDate() != null && !frm.getEstimatedClosureDate().trim().equals("")) {
			pto.setEstimatedClosureDate(DateUtil.getDateTime(frm.getEstimatedClosureDate(), super.getCalendarMask(request),	loc));
		} else {
			pto.setEstimatedClosureDate(null);
		}

		pto.setDescription(frm.getDescription());
		pto.setFinalDate(null);
		pto.setName(frm.getName());
		pto.setParentProject(new ProjectTO(frm.getParentProject()));
		pto.setProjectStatus(new ProjectStatusTO(frm.getProjectStatus()));
		pto.setCanAlloc(frm.getCanAlloc());
		pto.setAllowBillable(new Boolean(frm.getAllowBillable()));

		pto.setRepositoryClass(frm.getRepositoryClass());
		pto.setRepositoryPass(frm.getRepositoryPass());
		pto.setRepositoryURL(frm.getRepositoryURL());
		pto.setRepositoryUser(frm.getRepositoryUser());

		// populate user objects allocated into project
		pto.setInsertCustomers(this.getAllocUserVector(request, CustomerTO.ROLE_CUSTOMER));
		pto.setInsertResources(this.getAllocUserVector(request, ResourceTO.ROLE_RESOURCE));
		pto.setInsertLeaders(this.getAllocUserVector(request, LeaderTO.ROLE_LEADER));

		// set the id of project AND dispatch the process to LINK project_id
		// with her allocUser objects...
		pto.setId(frm.getId());
		
		if (frm.getProjectCompany()!=null && !frm.getProjectCompany().equals("-1")) {
			pto.setCompany(new CompanyTO(frm.getProjectCompany()));
		}
		
		final Vector<MetaFieldTO> metaFieldList = (Vector) request.getSession().getAttribute("metaFieldList");
		super.setMetaFieldValuesFromForm(metaFieldList, request, pto, frm.getAdditionalFields());
		if (metaFieldList != null) {
			frm.setAdditionalFields(pto.getAdditionalFields());
		}
		
		pto.addRepositoryPolicy(getPolicy(pto, RepositoryPolicyTO.POLICY_COMMENT_MANDATORY, frm.getPolicyMandatoryComment()));
		pto.addRepositoryPolicy(getPolicy(pto, RepositoryPolicyTO.POLICY_OPEN_PROJ, frm.getPolicyAllowOnlyOpenProj()));
		pto.addRepositoryPolicy(getPolicy(pto, RepositoryPolicyTO.POLICY_PROJ_RESOURCE, frm.getPolicyAllowOnlyResources()));
		pto.addRepositoryPolicy(getPolicy(pto, RepositoryPolicyTO.POLICY_REPOS_SAME_PROJ, frm.getPolicyCheckRepositorySource()));
		pto.addRepositoryPolicy(getPolicy(pto, RepositoryPolicyTO.POLICY_ENTITY_REF, frm.getPolicyCheckEntityReference()));

		if (frm.getBudget()!=null) {
			pto.setBudget(StringUtil.getStringToCents(frm.getBudget(), loc));	
		}
		
		final Vector<MetaFieldTO> qualifications = new Vector<MetaFieldTO>();
		final ProjectTO currentPto = pdel.getProjectObject(pto, true);
		if (currentPto!=null && currentPto.getQualifications()!=null) {
			for (final MetaFieldTO qualif : currentPto.getQualifications()) {
				qualif.setName(super.getBundleMessage(request, qualif.getName(), true));
				final String fieldValue = request.getParameter("QUALI_" + qualif.getId());
				if (fieldValue!=null) {
					qualif.setGenericTag(fieldValue);					
				} else{
					qualif.setGenericTag("");
				}
				qualifications.add(qualif);
			}
		}
		pto.setQualifications(qualifications);		
		
		return pto;
	}

	private RepositoryPolicyTO getPolicy(ProjectTO pto, String type,
			boolean status) {
		final RepositoryPolicyTO response = new RepositoryPolicyTO();
		response.setProject(pto);
		response.setValue(status ? "on" : "off");
		response.setPolicyType(type);
		return response;
	}

	/**
	 * Get data from form object based on allocUser vector into session and
	 * convert into vector of user objects with the attributes set.
	 * @throws BusinessException 
	 */
	@SuppressWarnings("unchecked")
	private Vector getAllocUserVector(HttpServletRequest request, Integer role) throws BusinessException {
		final UserDelegate udel = new UserDelegate();
		Vector<UserTO> vlist = null;
		final Vector<UserTO> sessionUserList = (Vector) request.getSession().getAttribute("allocList");

		// define the appropriate column type based on role desired
		String colType = "";
		if (role.equals(CustomerTO.ROLE_CUSTOMER)) {
			colType = ProjectGridCheckBoxDecorator.CUSTOMER_COL;
		} else if (role.equals(ResourceTO.ROLE_RESOURCE)) {
			colType = ProjectGridCheckBoxDecorator.RESOURCE_COL;
		} else if (role.equals(LeaderTO.ROLE_LEADER)) {
			colType = ProjectGridCheckBoxDecorator.LEADER_COL;
		}

		// create a specific user object based on keys from array
		if (sessionUserList != null) {
			
			final UserTO root = udel.getRoot();
			boolean rootExists = false;
			
			for (int i = 0; i < sessionUserList.size(); i++) {
				if (vlist == null) {
					vlist = new Vector<UserTO>();
				}
					
				final UserTO uto = sessionUserList.elementAt(i);
				final String value = request.getParameter("cb_" + uto.getId() + "_" + colType);

				if (role.equals(CustomerTO.ROLE_CUSTOMER) && value != null) {
					final CustomerTO cto = new CustomerTO(uto.getId());
					this.getSpecificCustomerAttr(request, cto);
					vlist.addElement(cto);
				} else if (role.equals(ResourceTO.ROLE_RESOURCE) && value != null) {
					final ResourceTO rto = new ResourceTO(uto.getId());
					this.getSpecificResourceAttr(request, rto);
					vlist.addElement(rto);
				} else if (role.equals(LeaderTO.ROLE_LEADER) && value != null) {
					final LeaderTO eto = new LeaderTO(uto.getId());
					this.getSpecificLeaderAttr(request, eto);
					vlist.addElement(eto);
				}
				
				if (uto.getId().equals(root.getId())) {
					rootExists = true;
				}
			}
			
			//force the allocation of root user such as a resource and customer of this project...
			if (!rootExists) {
				if (vlist == null) {
					vlist = new Vector<UserTO>();
				}				
				if (role.equals(CustomerTO.ROLE_CUSTOMER)) {
					final CustomerTO cto = new CustomerTO(root.getId());
					vlist.addElement(cto);
				} else if (role.equals(ResourceTO.ROLE_RESOURCE)) {
					final ResourceTO rto = new ResourceTO(root.getId());
					vlist.addElement(rto);
				}				
			}
		}

		return vlist;
	}

	/**
	 * Get specific attributes of customer from form
	 */
	private void getSpecificCustomerAttr(HttpServletRequest request,
			CustomerTO cto) {
		final String isDisab = request.getParameter("cb_" + cto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.DISABLE_USER_COL);
		cto.setIsDisabled(new Boolean(isDisab != null));

		final String isReqAccept = request.getParameter("cb_" + cto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.REQ_ACCEPT_CUSTOMER_COL);
		cto.setIsReqAcceptable(new Boolean(isReqAccept != null));

		final String canSeeTechComm = request.getParameter("cb_" + cto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.ALLOW_SEE_TECH_COMMENTS);
		cto.setCanSeeTechComments(new Boolean(canSeeTechComm != null));

		final String canSeeDiscussion = request.getParameter("cb_" + cto.getId()
				+ "_" + ProjectGridCheckBoxDecorator.ALLOW_SEE_DISCUSSION_COL);
		cto.setCanSeeDiscussion(new Boolean(canSeeDiscussion != null));

		final String canSeeOtherReqs = request.getParameter("cb_" + cto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.CAN_SEE_OTHER_REQS_COL);
		cto.setCanSeeOtherReqs(new Boolean(canSeeOtherReqs != null));

		final String canOpenOtherReqs = request.getParameter("cb_" + cto.getId()
				+ "_"
				+ ProjectGridCheckBoxDecorator.CAN_OPEN_OTHEROWNER_REQS_COL);
		cto.setCanOpenOtherOwnerReq(new Boolean(canOpenOtherReqs != null));

		final String preApprove = request.getParameter("cb_" + cto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.ALLOW_PRE_APPROVE_COL);
		cto.setPreApproveReq(new Boolean(preApprove != null));
	}

	/**
	 * Get specific attributes of resource from form
	 */
	private void getSpecificResourceAttr(HttpServletRequest request,
			ResourceTO rto) {

		this.getSpecificCustomerAttr(request, rto);

		final String seeCust = request.getParameter("cb_" + rto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.ALLOW_SEE_CUSTOMER_COL);
		rto.setCanSeeCustomer(new Boolean(seeCust != null));

		final String selfAlloc = request.getParameter("cb_" + rto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.CAN_SELF_ALLOC_COL);
		rto.setCanSelfAlloc(new Boolean(selfAlloc != null));

		final String seeReposit = request.getParameter("cb_" + rto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.CAN_SEE_REPOSITORY_COL);
		rto.setCanSeeRepository(new Boolean(seeReposit != null));

		final String seeInvoice = request.getParameter("cb_" + rto.getId() + "_"
				+ ProjectGridCheckBoxDecorator.CAN_SEE_INVOICE_COL);
		rto.setCanSeeInvoice(new Boolean(seeInvoice != null));

	}

	/**
	 * Get specific attributes of leader from form
	 */
	private void getSpecificLeaderAttr(HttpServletRequest request, LeaderTO eto) {
		this.getSpecificResourceAttr(request, eto);
	}

}
