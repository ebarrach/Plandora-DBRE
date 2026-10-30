package com.pandora.gui.struts.action.projectpanel;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import com.pandora.JFreeChartTO;
import com.pandora.OccurrenceTO;
import com.pandora.ProjectTO;
import com.pandora.ResourceTaskAllocTO;
import com.pandora.ResourceTaskTO;
import com.pandora.RootTO;
import com.pandora.TaskStatusTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.bus.occurrence.IterationOccurrence;
import com.pandora.delegate.OccurrenceDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.TaskDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.gui.struts.action.ProjectPanelAction;
import com.pandora.gui.struts.form.ProjectPanelForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.JFreeChartUtil;
import com.pandora.helper.SessionUtil;

public class TaskInfoProjectPanel extends ProjectPanelAction {
	
	private static final int MAX_NUMBER_TO_SHOW_PER_WEEK = 60;
	
	private static final int CATEGORY_CRITERIUM = 1;
	
	private static final int RESOURCE_CRITERIUM = 2;
	
	@Override
	public String getPanelId() {
		return "TASK_INFO";
	}

	@Override
	public String getPanelTitle() {
		return "title.projectPanelForm.task";
	}
	
	@Override
	public String renderPanel(HttpServletRequest request,
			ProjectPanelForm frm) throws BusinessException {
		StringBuffer sb = new StringBuffer("");
		ProjectDelegate pdel = new ProjectDelegate();
		TaskDelegate tdel = new TaskDelegate();

		if (frm.getProjectId()!=null && !frm.getProjectId().trim().equals("")) {
			ProjectTO pto = new ProjectTO(frm.getProjectId());
			pto = pdel.getProjectObject(pto, true);
			
			Timestamp finalDate = pto.getEstimatedClosureDate();
			if (finalDate==null) {
				finalDate = DateUtil.getNow();
			}
			Vector<TaskTO> taskList = tdel.getTaskListByProject(pto, pto.getCreationDate(), finalDate, false, true);
			String periodMask = "yyyy-MM";
			int daySlots = DateUtil.getSlotBetweenDates(pto.getCreationDate(), finalDate);
			if (daySlots < MAX_NUMBER_TO_SHOW_PER_WEEK) {
				periodMask = "yyyy-w";
			}
			
			sb.append("<table width=\"95%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
			sb.append("<tr>\n");
			sb.append("<td width=\"20\">&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getTaskByCriterium(pto, request, taskList, periodMask, CATEGORY_CRITERIUM) + "\" />\n");
			sb.append("&nbsp;<img src=\"data:image/png;base64," + this.getIterationStatus(pto, request) + "\" /></td>\n");
			sb.append("<td>&nbsp;<td>\n");
			sb.append("</tr><tr>\n");
			sb.append("<tr>\n");
			sb.append("<td colspan=\"3\">&nbsp;</td>\n");
			sb.append("</tr><tr>\n");
			sb.append("<td width=\"20\">&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getTaskByCriterium(pto, request, taskList, periodMask, RESOURCE_CRITERIUM) + "\" /></td>\n");
			sb.append("<td>&nbsp;</td>\n");
			sb.append("<tr>&nbsp;<td>\n");
			sb.append("</tr>\n");
			sb.append("</table>");
		}
		
		return sb.toString();
	}

	
	private String getIterationStatus(ProjectTO pto, HttpServletRequest request) throws BusinessException {
		HashMap<String, Integer> hmOccStatus = new HashMap<String, Integer>();
		UserTO uto = SessionUtil.getCurrentUser(request);
		
		JFreeChartTO cto = new JFreeChartTO();
		cto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.task.iter"));
		
		OccurrenceDelegate odel = new OccurrenceDelegate();		
		Vector<OccurrenceTO> list = odel.getIterationListByProject(pto.getId(), true);
		for (OccurrenceTO oto : list) {
			String status = oto.getStatus();
			Integer qty = hmOccStatus.get(status);
			if (qty==null) {
				qty = 1;
			} else {
				qty = qty + 1;
			}
			hmOccStatus.put(status, qty);
		}
		
		DefaultPieDataset dset = new DefaultPieDataset();
		Set<String> statusList = hmOccStatus.keySet();
		for (String statusId : statusList) {
			Integer qty  = hmOccStatus.get(statusId);
			String statusName = "";
			if (statusId.equals(IterationOccurrence.STATE_START)) {
				statusName = uto.getBundle().getMessage(uto.getLocale(), "label.occurrence.iteration.status.planned");
			} else if (statusId.equals(IterationOccurrence.STATE_FINAL_1)) {
				statusName = uto.getBundle().getMessage(uto.getLocale(), "label.occurrence.iteration.status.Aborted");
			} else if (statusId.equals(IterationOccurrence.STATE_FINAL_2)) {
				statusName = uto.getBundle().getMessage(uto.getLocale(), "label.occurrence.iteration.status.successClosed");
			} else if (statusId.equals(IterationOccurrence.STATE_FINAL_3)) {
				statusName = uto.getBundle().getMessage(uto.getLocale(), "label.occurrence.iteration.status.pendClosed");
			} else {
				statusName = uto.getBundle().getMessage(uto.getLocale(), "label.occurrence.iteration.status.started");
			}
			dset.setValue(statusName, qty);
		}		
        cto.setCategoryDataset(dset);
        
        JFreeChart chart = JFreeChartUtil.getRingChart(cto);
		byte[] ba = JFreeChartUtil.chartToImg(chart, 320, 200);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);		
	}

	
	private String getTaskByCriterium(ProjectTO pto, HttpServletRequest request, Vector<TaskTO> taskList, String periodMask, int type) {
		HashMap<String, Integer> hmTaskByCrit = new HashMap<String, Integer>();
		HashMap<String, String> hmCriterium = new HashMap<String, String>();
		HashMap<String, String> hmPeriod = new HashMap<String, String>();
		Vector<String> orderedPeriod = new Vector<String>();
		UserTO uto = SessionUtil.getCurrentUser(request);
		
		JFreeChartTO cto = new JFreeChartTO();
		if (type==CATEGORY_CRITERIUM) {
			cto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.task.cat"));	
		} else if (type==RESOURCE_CRITERIUM) {
			cto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.task.res"));	
		}
		cto.setRotateCategoryLabel(true);
		
		for (TaskTO tto : taskList) {
			Vector<ResourceTaskTO> rttoList = tto.getAllocResources();
			for (ResourceTaskTO rtto : rttoList) {
				TaskStatusTO tsto = rtto.getTaskStatus();
				if (tsto!=null) {
					Integer state = tsto.getStateMachineOrder();
					if (state!=null && !state.equals(TaskStatusTO.STATE_MACHINE_OPEN)) {
						Vector<ResourceTaskAllocTO> allocList = rtto.getAllocList();
						for (ResourceTaskAllocTO rtato : allocList) {
							Timestamp prefDate = rtto.getActualDate();
							if (prefDate==null) {
								prefDate = rtto.getInitialDate();
							}
							prefDate = DateUtil.getChangedDate(prefDate, Calendar.DATE, rtato.getSequence());
							String strPeriod = DateUtil.getDateTime(prefDate, periodMask, uto.getLocale());
							
							String critId = "", critName = "";
							if (type==CATEGORY_CRITERIUM) {
								critId = tto.getCategory().getId();
								critName = tto.getCategory().getName();
							} else if (type==RESOURCE_CRITERIUM) {
								critId = rtto.getResource().getId();
								String username = rtto.getResource().getUsername();
								if (username.equals(RootTO.ROOT_USER)) {
									username = uto.getBundle().getMessage(uto.getLocale(), "label.manageTask.define");
								}
								critName = username;
							}
							String key = strPeriod + "_" + critId;
							hmCriterium.put(critId, critName);
							
							if (hmPeriod.get(strPeriod)==null) {
								orderedPeriod.add(strPeriod);
								hmPeriod.put(strPeriod, strPeriod);
							}
							
							Integer slotAllocVal = hmTaskByCrit.get(key);
							if (slotAllocVal==null) {
								slotAllocVal = rtato.getAllocTime();
							} else {
								slotAllocVal = slotAllocVal + rtato.getAllocTime();
							}
							hmTaskByCrit.put(key, slotAllocVal);
						}					
					}					
				}				
			}
		}
		
		Collections.sort(orderedPeriod);
		
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		Set<String> categories = hmCriterium.keySet();
		for (String period : orderedPeriod) {
			for (String catId : categories) {
				String key = period + "_" + catId;
				Integer alloc = hmTaskByCrit.get(key);
				if (alloc!=null) {
					double a = (double)((double)alloc/60); 
					dset.addValue(a, hmCriterium.get(catId), period);					
				}
			}
		}		
        cto.setCategoryDataset(dset);
        
		byte[] ba = JFreeChartUtil.chartToImg(JFreeChartUtil.getStackbar(cto, false), (orderedPeriod.size() * 20)+100, 200);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);
	}
	
	
}
