package com.pandora.gui.struts.action.projectpanel;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jfree.data.category.DefaultCategoryDataset;

import com.pandora.CostInstallmentTO;
import com.pandora.CostTO;
import com.pandora.JFreeChartTO;
import com.pandora.ProjectTO;
import com.pandora.ResourceCapacityTO;
import com.pandora.ResourceTaskAllocTO;
import com.pandora.ResourceTaskTO;
import com.pandora.TaskStatusTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.delegate.CostDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.ResourceCapacityDelegate;
import com.pandora.delegate.TaskDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.gui.struts.action.ProjectPanelAction;
import com.pandora.gui.struts.action.ResCapacityPanelAction;
import com.pandora.gui.struts.form.ProjectPanelForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.JFreeChartUtil;
import com.pandora.helper.SessionUtil;

public class ActualCostProjectPanel extends ProjectPanelAction {

	@Override
	public String getPanelId() {
		return "ACTUAL_COST";
	}

	@Override
	public String getPanelTitle() {
		return "title.projectPanelForm.ac";
	}
	
	@Override
	public String renderPanel(HttpServletRequest request,
			ProjectPanelForm frm) throws BusinessException {
		StringBuffer sb = new StringBuffer("");
		ProjectDelegate pdel = new ProjectDelegate();
		TaskDelegate tdel = new TaskDelegate();
		CostDelegate cdel = new CostDelegate();

		if (frm.getProjectId()!=null && !frm.getProjectId().trim().equals("")) {
			ProjectTO pto = new ProjectTO(frm.getProjectId());
			pto = pdel.getProjectObject(pto, true);
			
			Timestamp finalDate = pto.getEstimatedClosureDate();
			if (finalDate==null) {
				finalDate = DateUtil.getNow();
			}
						
			Vector<TaskTO> taskList = tdel.getTaskListByProject(pto, pto.getCreationDate(), finalDate, false, true);
			Vector<CostTO> costList = cdel.getListByProject(pto, true, pto.getCreationDate(), finalDate);
			sb.append("<table width=\"95%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
			sb.append("<tr>\n");
			sb.append("<td width=\"20\">&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getChart(pto, request, taskList, costList, finalDate) + "\" />\n");
			sb.append("<td>&nbsp;<td>\n");
			sb.append("</tr><tr>\n");
			sb.append("<tr>\n");
			sb.append("</table>");
		}
			
		return sb.toString();
	}

	private String getChart(ProjectTO pto, HttpServletRequest request, Vector<TaskTO> taskList, Vector<CostTO> costList, Timestamp finalDate) throws BusinessException {
		HashMap<String, Float> hmCost = new HashMap<String, Float>();
		HashMap<String, Float> hmRefCost = new HashMap<String, Float>();
		HashMap<String, String> hmPeriod = new HashMap<String, String>();
		Vector<String> orderedPeriod = new Vector<String>();
		UserTO uto = SessionUtil.getCurrentUser(request);
		
		JFreeChartTO jcto = new JFreeChartTO();
		jcto.setTitle("");
		jcto.setRotateCategoryLabel(true);
		
		Long budget = pto.getBudget();
		if (budget==null) {
			budget = new Long(0);
		}
		
		//get human costs...
		this.getTaskCost(pto, taskList, finalDate, hmCost, hmPeriod, uto, budget);
		
		//get material costs...
		this.getMaterialCosts(costList, hmCost, hmPeriod, uto, budget);

		//get reference costs...
		this.getReferenceCost(pto, costList, finalDate, hmRefCost, hmPeriod, uto, budget);
		
		
		for (String period : hmPeriod.values()) {
			orderedPeriod.add(period);
		}
		Collections.sort(orderedPeriod);
		
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		String lastPeriod = null;		
		for (String period : orderedPeriod) {
			this.addValue(hmRefCost, "Reference", dset, null, period);
			this.addValue(hmCost, "Costs", dset, lastPeriod, period);
			lastPeriod = period;
		}		
		jcto.setCategoryDataset(dset);
        
		byte[] ba = JFreeChartUtil.chartToImg(JFreeChartUtil.getLineChart(jcto, new String[]{"a0a0a0", "FF0000", "E47833"}), 800, 300);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);
	}
	
	private void addValue(HashMap<String, Float> hmPerc, String label, DefaultCategoryDataset dset, String lastPeriod, String period) {
		Float val = hmPerc.get(period);
		if (val==null) {
			val = new Float(0);
		}
		Float previousVal = new Float(0);
		if (lastPeriod!=null) {
			previousVal = hmPerc.get(lastPeriod);
			if (previousVal==null) {
				previousVal = new Float(0);
			}			
		}
		dset.addValue(new Float(val+previousVal), label, period);
		hmPerc.put(period, new Float(val+previousVal));
	}

	
	private void getReferenceCost(ProjectTO pto, Vector<CostTO> costList, Timestamp finalDate, HashMap<String, Float> hmRefCost,
			HashMap<String, String> hmPeriod, UserTO uto, Long budget) {
		int days = DateUtil.getSlotBetweenDates(pto.getCreationDate(), DateUtil.getChangedDate(finalDate, Calendar.WEEK_OF_MONTH, 1));
		double smooth = days / 10; 
		for(int d = 1; d<days; d++) {
			Double val = (double)(((days / 2) - d) / smooth);
			Double refPerc = (double)(100 / (1 + (Math.exp(val))));
			
			String strPeriod = DateUtil.getDateTime(DateUtil.getChangedDate(pto.getCreationDate(), Calendar.DATE, (d-1)), "yyyy-MM-dd", uto.getLocale());					
			if (hmPeriod.get(strPeriod)==null) {
				hmPeriod.put(strPeriod, strPeriod);
			}
			
			Float costVal = hmRefCost.get(strPeriod);
			if (costVal==null) {
				costVal = refPerc.floatValue();
			} else {
				costVal = costVal + refPerc.floatValue();
			}
			hmRefCost.put(strPeriod, costVal);													
		}
	}

	private void getMaterialCosts(Vector<CostTO> costList, HashMap<String, Float> hmMaterialCost,
			HashMap<String, String> hmPeriod, UserTO uto, Long budget) {
		if (budget>0) {
			for (CostTO cto : costList) {
				for (CostInstallmentTO cito : cto.getInstallments()) {
					float materialCostPerc = (float)(cito.getValue() / ((float)budget.floatValue())) * 100;
					
					String strPeriod = DateUtil.getDateTime(cito.getDueDate(), "yyyy-MM-dd", uto.getLocale());					
					if (hmPeriod.get(strPeriod)==null) {
						hmPeriod.put(strPeriod, strPeriod);
					}
					
					Float costVal = hmMaterialCost.get(strPeriod);
					if (costVal==null) {
						costVal = materialCostPerc;
					} else {
						costVal = costVal + materialCostPerc;
					}
					hmMaterialCost.put(strPeriod, costVal);													
				}
			}			
		}
	}

	private void getTaskCost(ProjectTO pto, Vector<TaskTO> taskList, Timestamp finalDate, HashMap<String, Float> hmTaskCost, 
			HashMap<String, String> hmPeriod, UserTO uto, Long budget) throws BusinessException {
		HashMap<String, Vector<ResourceCapacityTO>> hmResCap = new HashMap<String, Vector<ResourceCapacityTO>>();
		ResourceCapacityDelegate rcdel = new ResourceCapacityDelegate();
		ResCapacityPanelAction rcpa = new ResCapacityPanelAction();
		
		JFreeChartTO cto = new JFreeChartTO();
		cto.setTitle("");
		
		for (TaskTO tto : taskList) {
			Vector<ResourceTaskTO> rttoList = tto.getAllocResources();
			for (ResourceTaskTO rtto : rttoList) {
				TaskStatusTO tsto = rtto.getTaskStatus();
				if (tsto!=null) {
					Integer state = tsto.getStateMachineOrder();
					if (state!=null && !state.equals(TaskStatusTO.STATE_MACHINE_OPEN)) {
						
						String keyRc = rtto.getResource().getId() + "|" + pto.getId();
						if (hmResCap.get(keyRc)==null) {
							hmResCap.put(keyRc, rcdel.getListByResourceProject(rtto.getResource().getId(), pto.getId(), null));	
						}
						Vector<ResourceCapacityTO> rclist = hmResCap.get(keyRc);
						
						Vector<ResourceTaskAllocTO> allocList = rtto.getAllocList();
						for (ResourceTaskAllocTO rtato : allocList) {
							Timestamp prefDate = rtto.getActualDate();
							if (prefDate==null) {
								prefDate = rtto.getInitialDate();
							}
							prefDate = DateUtil.getChangedDate(prefDate, Calendar.DATE, rtato.getSequence());
							String strPeriod = DateUtil.getDateTime(prefDate, "yyyy-MM-dd", uto.getLocale());
							
							if (hmPeriod.get(strPeriod)==null) {
								hmPeriod.put(strPeriod, strPeriod);
							}

							Float workingCostPerc = null;
							ResourceCapacityTO rcto = rcpa.getCapacity(prefDate, DateUtil.getChangedDate(prefDate, Calendar.DATE, 1), pto.getId(), rtto.getResource().getId(), rclist, 1, finalDate);
							if (rcto!=null && rcto.getCostPerHour()!=null && rtato.getAllocTime()!=null && budget>0) {
								float centsPerHour = (float)((float)((float)rcto.getCostPerHour())) * ((float)(rtato.getAllocTime()/60));
								workingCostPerc = (float)(centsPerHour / ((float)budget.floatValue())) * 100;
							}
							
							if (workingCostPerc!=null) {
								Float costVal = hmTaskCost.get(strPeriod);
								if (costVal==null) {
									costVal = workingCostPerc;
								} else {
									costVal = costVal + workingCostPerc;
								}
								hmTaskCost.put(strPeriod, costVal);								
							}							
						}
					}					
				}				
			}
		}
	}
}
