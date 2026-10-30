package com.pandora.gui.struts.action;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import com.pandora.AdditionalFieldTO;
import com.pandora.AdditionalTableTO;
import com.pandora.CustomFormTO;
import com.pandora.MetaFieldTO;
import com.pandora.MetaFormTO;
import com.pandora.PreferenceTO;
import com.pandora.UserTO;
import com.pandora.bus.PreferenceBUS;
import com.pandora.delegate.MetaFieldDelegate;
import com.pandora.delegate.MetaFormDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.exception.MetaFieldNumericTypeException;
import com.pandora.gui.struts.form.CustomFormForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.SessionUtil;

public class CustomFormAction extends GeneralStrutsAction {

	private boolean saveBatch = false;

	public ActionForward prepareForm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {

		final String forward = "showCustomForm";
		try {
			this.loadPreferences(form, request);

			request.getSession().setAttribute("customList", new Vector<CustomFormTO>());

			// Important: the session key below must be metaFieldList because it
			// uses a shared action of GeneralStrutsAction (see method
			// metaTableAddRow)
			request.getSession().setAttribute("metaFieldList", new Vector<MetaFieldTO>());

			this.refresh(mapping, form, request, response);
			this.clear(mapping, form, request, response);

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.generic.showFormError", null);
		}
		return mapping.findForward(forward);
	}

	public ActionForward enableSaveBatch(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {

		final String forward = "showCustomForm";

		final CustomFormForm frm = (CustomFormForm) form;
		frm.setEnableBatch(true);
		frm.setSaveMethod(CustomFormForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));

		this.refresh(mapping, form, request, response);

		return mapping.findForward(forward);
	}

	public ActionForward navigate(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showCustomForm");
	}

	@SuppressWarnings("unchecked")
	public ActionForward clear(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final String forward = "showCustomForm";
		this.clearMessages(request);

		final CustomFormForm frm = (CustomFormForm) form;
		frm.setSaveMethod(CustomFormForm.INSERT_METHOD, SessionUtil.getCurrentUser(request));
		frm.setAfterSuccessfullySave("off");

		final Vector<MetaFieldTO> fieldList = (Vector<MetaFieldTO>) request.getSession().getAttribute("metaFieldList");
		final Iterator<MetaFieldTO> i = fieldList.iterator();
		while (i.hasNext()) {
			final MetaFieldTO mto = i.next();
			final AdditionalFieldTO afto = frm.getAdditionalField(mto.getId());
			if (afto != null) {
				afto.setValue("");
				afto.setNumericValue(null);
				afto.setDateValue(null);
				afto.setTableValues(new Vector<AdditionalTableTO>());
			}
		}

		return mapping.findForward(forward);
	}

	public ActionForward editRecord(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final String forward = "showCustomForm";
		final MetaFormDelegate mdel = new MetaFormDelegate();
		try {
			final CustomFormForm frm = (CustomFormForm) form;
			frm.setAfterSuccessfullySave("off");

			final CustomFormTO cfto = mdel.getRecord(new CustomFormTO(frm.getId()));
			this.getActionFormFromTransferObject(cfto, frm, request);

			// set current operation status for Updating
			frm.setSaveMethod(CustomFormForm.UPDATE_METHOD, SessionUtil.getCurrentUser(request));

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.generic.showFormError", null);
		}

		return mapping.findForward(forward);
	}

	public ActionForward saveCustomForm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final String forward = "showCustomForm";
		String errorMsg = "error.generic.showFormError";
		final String succeMsg = "message.success";

		try {
			final UserTO uto = SessionUtil.getCurrentUser(request);
			
			final CustomFormForm frm = (CustomFormForm) form;
			frm.setAfterSuccessfullySave("off");

			final MetaFormDelegate del = new MetaFormDelegate();
			final CustomFormTO to = this.getTransferObjectFromActionForm(frm, request);
			
			to.setCreationUser(uto);
			to.setModificationUser(uto);

			boolean validvalues = true;
			final Iterator<AdditionalFieldTO> itafto = to.getAdditionalFields().iterator();
			while (itafto.hasNext()) {
				final AdditionalFieldTO afto = itafto.next();
				if (afto.getMetaField().isMandatory() && (afto.getValue() == null || afto.getValue().equals(""))) {
					this.setErrorFormSession(request, "error.generic.invalidValue", afto.getMetaField().getName(), "",
							"", "", "", new Exception());
					validvalues = false;
				}
			}
			if (validvalues) {
				if (frm.getSaveMethod().equals(CustomFormForm.INSERT_METHOD)) {
					errorMsg = "error.generic.insertFormError";
					del.insertRecord(to);
				} else {
					errorMsg = "error.generic.updateFormError";
					del.updateRecord(to);
				}

				// this.clear(mapping, form, request, response );
				this.setSuccessFormSession(request, succeMsg);

				frm.setAfterSuccessfullySave("on");

				// set the current user connected
				
				frm.setSaveMethod(CustomFormForm.INSERT_METHOD, uto);

				this.refresh(mapping, form, request, response);
			}
		} catch (final MetaFieldNumericTypeException e) {
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, errorMsg, e);
		}
		return mapping.findForward(forward);
	}
	
	public ActionForward saveBatchCustomForm(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final String forward = "showCustomForm";
		final String errorMsg = "error.generic.showFormError";
		final String succeMsg = "message.success";

		try {
			final CustomFormForm frm = (CustomFormForm) form;
			frm.setAfterSuccessfullySave("off");

			final CustomFormTO to = this.getTransferObjectFromActionForm(frm, request);
			final HashMap<String, String> metaFieds = new HashMap<String, String>();

			boolean validvalues = true;
			final Iterator<AdditionalFieldTO> itafto = to.getAdditionalFields().iterator();
			while (itafto.hasNext()) {
				final AdditionalFieldTO afto = itafto.next();
				if (afto.getMetaField().isMandatory() && (afto.getValue() == null || afto.getValue().equals(""))) {
					this.setErrorFormSession(request, "error.generic.invalidValue", afto.getMetaField().getName(), "", "", "", "", new Exception());
					validvalues = false;
				}
				// Get the metafields checkboxs markeds
				if(request.getParameter("cb_" + afto.getMetaFieldId() + "_") != null && request.getParameter("cb_" + afto.getMetaFieldId() + "_").equalsIgnoreCase("on")){
					metaFieds.put(afto.getMetaFieldId(), afto.getValue());
				}
			}
			
			// Check if at least one checkbox has been checked
			if(metaFieds.isEmpty()){
				this.setErrorFormSession(request, "error.msg.updateBacthSelectError", "", "", "", "", "", new Exception());
				validvalues = false;
			}
			
			@SuppressWarnings("unchecked")
			final Vector<CustomFormTO> recs= (Vector<CustomFormTO>)request.getSession().getAttribute("customList");
			final ArrayList<String> plannnigIds = new ArrayList<String>();
			final Iterator<CustomFormTO> recsTo = recs.iterator();
			while (recsTo.hasNext()) {
				final CustomFormTO cfto = recsTo.next();
				// Get the list checkboxs markeds
				if(request.getParameter("cb_" + cfto.getId() + "_") != null && request.getParameter("cb_" + cfto.getId() + "_").equalsIgnoreCase("on")){
					plannnigIds.add(cfto.getId());
				}
			}
			
			// Check if at least one checkbox has been checked
			if(plannnigIds.isEmpty()){
				this.setErrorFormSession(request, "error.msg.updateBacthSelectError", "", "", "", "", "", new Exception());
				validvalues = false;
			}
			
			if (validvalues) {

				final MetaFormDelegate del = new MetaFormDelegate();
				
				del.updateInBatch(plannnigIds, metaFieds);
				
				this.clear(mapping, form, request, response);
				this.setSuccessFormSession(request, succeMsg);

				frm.setAfterSuccessfullySave("on");

				// set the current user connected
				final UserTO uto = SessionUtil.getCurrentUser(request);
				frm.setSaveMethod(CustomFormForm.INSERT_METHOD, uto);
				
				this.refresh(mapping, form, request, response);

			}
		} catch (final MetaFieldNumericTypeException e) {
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, errorMsg, e);
		}
		return mapping.findForward(forward);
	}

	public ActionForward removeRecord(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final String forward = "showCustomForm";
		try {
			final CustomFormForm frm = (CustomFormForm) form;
			final MetaFormDelegate del = new MetaFormDelegate();
			final CustomFormTO to = this.getTransferObjectFromActionForm(frm, request);
			del.removeRecord(to);

			this.setSuccessFormSession(request, "message.success");
			this.refresh(mapping, form, request, response);

		} catch (final MetaFieldNumericTypeException e) {
			this.setErrorFormSession(request, e.getMessage(), e.getMetaFieldName(), null, null, null, null, e);
		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.generic.removeFormError", e);
		}
		return mapping.findForward(forward);
	}

	public ActionForward refresh(ActionMapping mapping, ActionForm form, HttpServletRequest request,
			HttpServletResponse response) {
		final MetaFormDelegate mdel = new MetaFormDelegate();
		final MetaFieldDelegate del = new MetaFieldDelegate();
		final CustomFormForm frm = (CustomFormForm) form;
		final String forward = "showCustomForm";

		try {
			if (frm.getMetaFormId() != null && !frm.getMetaFormId().trim().equals("")) {

				final MetaFormTO metaForm = mdel.getObject(new MetaFormTO(frm.getMetaFormId()));
				frm.setFormTitle(metaForm.getName());
				frm.setJsAfterLoad(metaForm.getJsAfterLoad());
				frm.setJsAfterSave(metaForm.getJsAfterSave());
				frm.setJsBeforeSave(metaForm.getJsBeforeSave());
				frm.setSaveBatch(metaForm.getSaveBatch());

				final Vector<MetaFieldTO> list = del.getFieldByMetaForm(frm.getMetaFormId());
				if(frm.isEnableBatch()){
					for (final Iterator<MetaFieldTO> iterator = list.iterator(); iterator.hasNext();) {
						final MetaFieldTO metaFieldTO = (MetaFieldTO) iterator.next();
						metaFieldTO.getMetaform().setEnableBatch(true);
					}
				}
				request.getSession().setAttribute("metaFieldList", list);

				final int days = frm.getDaysToHide();
				final Timestamp iniRange = DateUtil.getChangedDate(DateUtil.getNow(), Calendar.DATE, -days);
				final Vector<CustomFormTO> recs = mdel.getRecords(frm.getMetaFormId(), iniRange);
				request.getSession().setAttribute("customList", recs);

				request.getSession().setAttribute("customFormRowNumber", metaForm.getGridNumber());

				if (metaForm.getFilterColId() != null && !metaForm.getFilterColId().equals("")) {
					request.getSession().setAttribute("customFormComboFilterId", metaForm.getFilterColId());
				} else {
					request.getSession().removeAttribute("customFormComboFilterId");
				}

				this.savePreferences(form, request);

			} else {
				this.setErrorFormSession(request, "error.generic.showFormError", null);
			}

		} catch (final Exception e) {
			this.setErrorFormSession(request, "error.generic.showFormError", null);
		}

		return mapping.findForward(forward);
	}

	private void getActionFormFromTransferObject(CustomFormTO to, CustomFormForm frm, HttpServletRequest request) {
		frm.setId(to.getId());
		frm.setCreationDate(to.getCreationDate());
		frm.setMetaFormId(to.getMetaForm().getId());
		frm.setAdditionalFields(to.getAdditionalFields());

		final Vector<MetaFieldTO> fieldList = new Vector<MetaFieldTO>();
		final Iterator<AdditionalFieldTO> i = to.getAdditionalFields().iterator();
		while (i.hasNext()) {
			final AdditionalFieldTO afto = i.next();
			if(frm.isEnableBatch()){
				final MetaFormTO mfto = new MetaFormTO(to.getMetaForm().getId());
				mfto.setEnableBatch(true);
				afto.getMetaField().setMetaform(mfto);
			} else{
				final MetaFormTO mfto = new MetaFormTO(to.getMetaForm().getId());
				mfto.setEnableBatch(false);
				afto.getMetaField().setMetaform(mfto);
			}
			fieldList.add(afto.getMetaField());
		}
		request.getSession().setAttribute("metaFieldList", fieldList);
	}

	@SuppressWarnings("unchecked")
	private CustomFormTO getTransferObjectFromActionForm(CustomFormForm frm, HttpServletRequest request)
			throws MetaFieldNumericTypeException {
		final CustomFormTO cto = new CustomFormTO();
		cto.setId(frm.getId());
		cto.setMetaForm(new MetaFormTO(frm.getMetaFormId()));
		cto.setDescription(null);
		cto.setCreationDate(DateUtil.getNow());

		final Vector<MetaFieldTO> metaFieldList = (Vector<MetaFieldTO>) request.getSession()
				.getAttribute("metaFieldList");
		super.setMetaFieldValuesFromForm(metaFieldList, request, cto, frm.getAdditionalFields());
		if (metaFieldList != null) {
			frm.setAdditionalFields(cto.getAdditionalFields());
		}

		return cto;
	}

	private void loadPreferences(ActionForm form, HttpServletRequest request) {
		final CustomFormForm frm = (CustomFormForm) form;
		frm.setEnableBatch(false);
		final UserTO uto = SessionUtil.getCurrentUser(request);
		final PreferenceTO pto = uto.getPreference();
		frm.setDaysToHideRecords(pto.getPreference(PreferenceTO.CUSTOM_FORM_HIDE_DAYS));
	}

	private void savePreferences(ActionForm form, HttpServletRequest request) throws BusinessException {
		final CustomFormForm frm = (CustomFormForm) form;

		final PreferenceBUS pbus = new PreferenceBUS();
		final UserTO uto = SessionUtil.getCurrentUser(request);
		final PreferenceTO pto = uto.getPreference();

		pto.addPreferences(new PreferenceTO(PreferenceTO.CUSTOM_FORM_HIDE_DAYS, frm.getDaysToHide() + "", uto));
		pbus.insertOrUpdate(pto);
	}

	/**
	 * @return the saveBatch
	 */
	public boolean isSaveBatch() {
		return saveBatch;
	}

	/**
	 * @param saveBatch
	 *            the saveBatch to set
	 */
	public void setSaveBatch(boolean saveBatch) {
		this.saveBatch = saveBatch;
	}
}
