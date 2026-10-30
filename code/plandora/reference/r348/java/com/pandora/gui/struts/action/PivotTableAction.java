package com.pandora.gui.struts.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.gui.struts.form.PivotTableForm;
import com.pandora.helper.SessionUtil;

public class PivotTableAction extends GeneralStrutsAction {

	public ActionForward prepareForm(ActionMapping mapping, ActionForm form,
			 HttpServletRequest request, HttpServletResponse response){
		String forward = "showPivotTable";
		PivotTableForm frm = (PivotTableForm)form;
		
		if (frm.getSource().equals(PivotTableForm.PT_SOURCE_GRID)) {
			frm.setServerURI(SessionUtil.getUri(request) + request.getRequestURI() + "?operation=requestPTableJson&ajaxGridId=" + frm.getGridId());
		} else if (frm.getSource().equals(PivotTableForm.PT_SOURCE_CAPACITY_FORM)) {
			frm.setServerURI("../do/showResCapacityPanel?operation=requestJson");
		}
		
		return mapping.findForward(forward);
	}
}
