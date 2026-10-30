package com.pandora.gui.struts.action;

import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.MetaFormTO;
import com.pandora.UserTO;
import com.pandora.delegate.MetaFormDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.gui.struts.form.MetaFormForm;
import com.pandora.helper.SessionUtil;

/**
 */
public class MetaFormAction extends GeneralStrutsAction {

	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
		
		final String forward = "showMetaForm";

		this.clearForm(form, request);
		
		final UserTO uto = SessionUtil.getCurrentUser(request);
		final MetaFormForm frm = (MetaFormForm)form;
		frm.setSaveMethod(MetaFormForm.INSERT_METHOD, uto);
	
	    this.refresh(mapping, form, request, response);    

	    return mapping.findForward(forward);		
	}
    
	
	public ActionForward saveMetaForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showMetaForm";
		String errorMsg = "error.metaform.showForm";
		String succeMsg = "message.success";
		
		try {
		    final MetaFormForm frm = (MetaFormForm)form;
		    final MetaFormDelegate del = new MetaFormDelegate();
		    final MetaFormTO to = this.getTransferObjectFromActionForm(frm, request);
			
			if (frm.getSaveMethod().equals(MetaFormForm.INSERT_METHOD)){
			    errorMsg = "error.metaform.insert";
			    succeMsg = "message.insertMetaForm";
			    del.insertMetaForm(to);
			} else {
			    errorMsg = "error.metaform.update";
			    succeMsg = "message.updateMetaForm";
			    del.updateMetaForm(to);
			}
		
			this.clear(mapping, form, request, response );				
			this.setSuccessFormSession(request, succeMsg);				

		    //set the current user connected
			final UserTO uto = SessionUtil.getCurrentUser(request);			
		    frm.setSaveMethod(MetaFormForm.INSERT_METHOD, uto);
		        			
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, errorMsg, e);
		} catch(final NullPointerException e){
		    this.setErrorFormSession(request, errorMsg, e);		    
		}	    
		return mapping.findForward(forward);	    
	}

	
	private void clearForm(ActionForm form, HttpServletRequest request){
	    final MetaFormForm frm = (MetaFormForm)form;		
	    frm.clear();
	    this.clearMessages(request);
	}
	
	
	public ActionForward refresh(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showMetaForm";
		try {	    		    
		    final MetaFormDelegate del = new MetaFormDelegate();
		    final Vector<MetaFormTO> mfList = del.getMetaFormList();
		    request.getSession().setAttribute("metaFormList", mfList);		    		    
		} catch(final BusinessException e){
		   this.setErrorFormSession(request, "error.metaform.showForm", e);
		}
	    
		return mapping.findForward(forward);	    
	}
	
	
    public ActionForward editMetaForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showMetaForm";
	    final MetaFormDelegate del = new MetaFormDelegate();
		
		try {
		    final MetaFormForm frm = (MetaFormForm)form;
		    final MetaFormTO mfto = del.getObject(new MetaFormTO(frm.getId()));
		    this.getActionFormFromTransferObject(mfto, frm, request);
		    frm.setSaveMethod(MetaFormForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));		    		    
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.metaform.showForm", e);
		}
		
		return mapping.findForward(forward);				
	}
	
    
	public ActionForward removeMetaForm(ActionMapping mapping, ActionForm form,
			HttpServletRequest request, HttpServletResponse response){
	    final String forward = "showMetaForm";
		try {
		    final MetaFormForm frm = (MetaFormForm)form;
		    final MetaFormDelegate del = new MetaFormDelegate();
			this.clearForm(frm, request);

			final MetaFormTO mfto = new MetaFormTO(frm.getId());
			del.removeMetaForm(mfto);
			
			//set success message into http session
			this.setSuccessFormSession(request, "message.removeMetaForm");
			this.refresh(mapping, form, request, response );
			
		    //set the current user connected
			final UserTO uto = SessionUtil.getCurrentUser(request);
		    frm.setSaveMethod(MetaFormForm.INSERT_METHOD, uto);
		
		} catch(final BusinessException e){
		    this.setErrorFormSession(request, "error.metaform.remove", e);
		}
	    
		return mapping.findForward(forward);	    
	}
	
    
	public ActionForward clear(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		final String forward = "showMetaForm";		
		this.prepareForm(mapping, form, request, response);
		return mapping.findForward(forward);		
	}
	
    
	private void getActionFormFromTransferObject(MetaFormTO to, MetaFormForm frm, HttpServletRequest request){
	    frm.setId(to.getId());
	    frm.setName(to.getName());
        frm.setFilterColId(to.getFilterColId());
        frm.setGridNumber(to.getGridNumber());	    
	    frm.setViewableCols(to.getViewableCols());
        frm.setJsAfterLoad(to.getJsAfterLoad());
        frm.setJsAfterSave(to.getJsAfterSave());
        frm.setJsBeforeSave(to.getJsBeforeSave());
        frm.setSaveBatch(to.getSaveBatch());
        frm.setConstraintEditDelete(to.getConstraintEditDelete());
	}
	
    
	private MetaFormTO getTransferObjectFromActionForm(MetaFormForm frm, HttpServletRequest request){
	    final MetaFormTO to = new MetaFormTO(frm.getId());
        to.setName(frm.getName());
        to.setFilterColId(frm.getFilterColId());
        to.setGridNumber(frm.getGridNumber());
        to.setViewableCols(frm.getViewableCols());
        to.setJsAfterLoad(frm.getJsAfterLoad());
        to.setJsAfterSave(frm.getJsAfterSave());
        to.setJsBeforeSave(frm.getJsBeforeSave());
        to.setSaveBatch(frm.getSaveBatch());
        to.setConstraintEditDelete(frm.getConstraintEditDelete());
	    return to;
	}
	
}
