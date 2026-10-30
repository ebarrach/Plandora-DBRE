package com.pandora.gui.struts.action;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.CustomerFunctionTO;
import com.pandora.CustomerTO;
import com.pandora.FunctionTO;
import com.pandora.ProjectTO;
import com.pandora.UserTO;
import com.pandora.delegate.CustomerFunctionDelegate;
import com.pandora.delegate.FunctionDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.gui.struts.form.UserRoleForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.SessionUtil;

public class UserRoleAction extends GeneralStrutsAction {

	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showUserRoleEdit";
		ProjectDelegate pdel = new ProjectDelegate();

		try {
			UserRoleForm frm = (UserRoleForm)form;
			
			ProjectTO pto = pdel.getProjectObject(new ProjectTO(frm.getProjectId()), false);
			UserDelegate udel = new UserDelegate();
			CustomerTO cto = new CustomerTO(frm.getUserId());
			cto.setProject(pto);
			cto = udel.getCustomer(cto);
			frm.setCurrentUserRole(cto.getUsername());
			
			String rolesHtml = this.getRoles(cto, frm.getProjectId());
			frm.setRoleHtmlBody(rolesHtml);
			
		} catch (Exception e){
			this.setErrorFormSession(request, "error.generic.showFormError", e);	
		}
		return mapping.findForward(forward);
	}
	
	
	public ActionForward changeRole(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response) {
		final CustomerFunctionDelegate del = new CustomerFunctionDelegate();

		try {
			final UserRoleForm frm = (UserRoleForm) form;

			final CustomerFunctionTO cfto = new CustomerFunctionTO();
			cfto.setCreationDate(DateUtil.getNow());
			final CustomerTO cto = new CustomerTO(frm.getUserId());
			cto.setProject(new ProjectTO(frm.getProjectId()));
			cfto.setCustomer(cto);

			FunctionDelegate fdel = new FunctionDelegate();
			FunctionTO fto = fdel.getFunction(new FunctionTO(frm.getFunctionId()));
			cfto.setFunct(fto);
			
			final CustomerFunctionTO cftoDB = del.getCustomerFunctionObject(cfto);
			if (cftoDB != null) {
				del.removeCustomerFunction(cfto);
			} else {
				del.insertCustomerFunction(cfto);
			}

			final UserTO uto = SessionUtil.getCurrentUser(request);
        	final String userEnconding = uto.getBundle().getMessage(uto.getLocale(), "encoding");
            response.setContentType("text/html; charset=" + userEnconding);   
	        response.setHeader("Cache-Control", "no-cache");
	        
            final PrintWriter out = response.getWriter();
            out.println(fto.getName() + " :: OK!");  
            out.flush();

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.showProjForm", e);
		}

		return null; //mapping.findForward("showProject");
	}

	
    private String getRoles(CustomerTO cto, String projectId){
    	String response = null;
    	
    	Vector<CustomerFunctionTO> list = cto.getRoles();
    	
    	if (list!=null) {
    		response = "";
        	Iterator<CustomerFunctionTO> i = list.iterator();
        	while(i.hasNext()) {
        		CustomerFunctionTO cfto = (CustomerFunctionTO)i.next();
        		String title = cfto.getFunct().getName();
        		response = response + "<input type='checkbox' onclick='checkUserRoleValue(" + cfto.getFunct().getId() + ", " + cto.getId() + ", " + projectId+ ");' id='" + cfto.getFunct().getId() + "' name='" + title + "'";
        		if (cfto.getCustomer()!=null) {
        			response = response + " checked";
        		}
        		response = response + " value='on'>" + title + "<br/>";    		
        	}    		
    	}
    	
        return response;
    }

	
}
