package com.pandora.gui.struts.action.projectpanel;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.pandora.CategoryTO;
import com.pandora.FieldValueTO;
import com.pandora.JFreeChartTO;
import com.pandora.ProjectTO;
import com.pandora.ResourceTaskTO;
import com.pandora.TaskStatusTO;
import com.pandora.TaskTO;
import com.pandora.TransferObject;
import com.pandora.UserTO;
import com.pandora.delegate.CategoryDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.TaskDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.gui.struts.action.ProjectPanelAction;
import com.pandora.gui.struts.form.ProjectPanelForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.JFreeChartUtil;
import com.pandora.helper.SessionUtil;
import com.pandora.helper.StringUtil;

public class EstimatedActualTimeProjectPanel extends ProjectPanelAction {

	@Override
	public String getPanelId() {
		return "ESTIM_X_ACTUAL_TIME";
	}

	@Override
	public String getPanelTitle() {
		return "title.projectPanelForm.estact";
	}
	
	@Override
	public Vector<FieldValueTO> getPanelFields(HttpServletRequest request, ProjectPanelForm frm) throws BusinessException{
		CategoryDelegate cdel = new CategoryDelegate();
		Vector<FieldValueTO> response = new Vector<FieldValueTO>();
		HashMap<String, String> hmCat = new HashMap<String, String>();
		Vector<TransferObject> options = new Vector<TransferObject>();
		
		if (frm.getProjectId()!=null && !frm.getProjectId().trim().equals("")) {
			ProjectTO pto = new ProjectTO(frm.getProjectId());
			Vector<CategoryTO> cat = cdel.getCategoryListByType(CategoryTO.TYPE_TASK, pto, true);
			for (CategoryTO cto : cat) {
				if (hmCat.get(cto.getId())==null) {
					options.add(new TransferObject(cto.getId(), cto.getName()));
					hmCat.put(cto.getId(), cto.getId());
				}
			}
		}
		FieldValueTO field = new FieldValueTO("CATEGORY", FieldValueTO.FIELD_TYPE_COMBO, options);
		field.setCurrentValue(request.getParameter(this.getPanelId() + "_CATEGORY"));
		
		response.add(field);
		
		return response;
	}

	
	@Override
	public String renderPanel(HttpServletRequest request,
			ProjectPanelForm frm) throws BusinessException {
		StringBuffer sb = new StringBuffer("");
		ProjectDelegate pdel = new ProjectDelegate();
		TaskDelegate tdel = new TaskDelegate();
		UserTO uto = SessionUtil.getCurrentUser(request);
		
		if (frm.getProjectId()!=null && !frm.getProjectId().trim().equals("")) {
			ProjectTO pto = new ProjectTO(frm.getProjectId());
			pto = pdel.getProjectObject(pto, true);

			Timestamp finalDate = pto.getFinalDate();
			if (finalDate==null) {
				finalDate = DateUtil.getNow();
			}
			Vector<TaskTO> taskList = tdel.getTaskListByProject(pto, pto.getCreationDate(), finalDate, false, true);
			sb.append("<table width=\"95%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
			sb.append("<tr><td width=\"20\">&nbsp;</td><td>\n");
			sb.append("<table border=\"0\" cellspacing=\"0\" cellpadding=\"0\">");
			sb.append(this.getFilterComboBox(request, frm, this, uto, 3));
			sb.append("</table></td><td>&nbsp;<td></tr>\n");
			sb.append("<tr>\n");
			sb.append("<td>&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getChart(pto, request, taskList) + "\" /></td>");
			sb.append("<td>&nbsp;<td>\n");
			sb.append("</tr>");
			sb.append("</table>");
		}
		
		return sb.toString();
	}

	
	private String getChart(ProjectTO pto, HttpServletRequest request, Vector<TaskTO> taskList) {
		JFreeChartTO cto = new JFreeChartTO();
		UserTO uto = SessionUtil.getCurrentUser(request);
		
		String catFilterId = request.getParameter(this.getPanelId() + "_CATEGORY");
		if (catFilterId==null) {
			catFilterId = "";
		}
		
		cto.setAxisLabel(uto.getBundle().getMessage(uto.getLocale(), "label.manageTask.grid.estTime.desc") + " (h)");
		cto.setCategoryLabel(uto.getBundle().getMessage(uto.getLocale(), "label.manageTask.grid.actualTime.desc") + " (h)");
		
		XYSeriesCollection dset = new XYSeriesCollection();
	    XYSeries data = new XYSeries("data");
	    dset.addSeries(data);
	    
	    int count = 0;
	    float diffAcc = 0;
	    float squareAcc = 0;
	    float variance = 0;
	    double stdDeviation = 0;
	    double stdError = 0;
	    ArrayList<Float> vals = new ArrayList<Float>(); 
	    
		for (TaskTO tto : taskList) {
			if (tto.getCategory().getId()!=null && 
					(catFilterId.trim().equals("") || catFilterId.equals(tto.getCategory().getId()))) {
				Vector<ResourceTaskTO> rttoList = tto.getAllocResources();
				for (ResourceTaskTO rtto : rttoList) {
					TaskStatusTO tsto = rtto.getTaskStatus();
					if (tsto!=null) {
						Integer state = tsto.getStateMachineOrder();
						if (state!=null && !state.equals(TaskStatusTO.STATE_MACHINE_OPEN) && 
								rtto.getEstimatedTime()!=null && rtto.getActualTime()!=null) {
							count++;
							float diff = ((float)rtto.getEstimatedTime()/60f - (float)rtto.getActualTime()/60f);
							diffAcc += diff;
							vals.add(diff);
							
							data.add((rtto.getActualTime()/60),(rtto.getEstimatedTime()/60));
						}					
					}				
				}				
			}
		}
		
		if (count>0) {
			//calculate the average of 'estim X actual' accumulated and the number of elements 
			float avg = diffAcc / count;
			
			//calculate the square of sum of difference between each 'estim X actual' val and the average 
			for (Float val : vals) {
				squareAcc+= ((val - avg) * (val - avg)); 	
			}
			
			//calculate the variance
			if (count - 1>0) {
				variance = (float)((float)squareAcc / ((float)(count - 1)));	
			}
			
			//calculate the standard deviation
			stdDeviation = Math.sqrt(variance); 
			
			//calculate the standard error
			stdError = stdDeviation / Math.sqrt(count);
			
			cto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.stdErr") + ": " + 
						StringUtil.getFloatToString(new Float(stdError), uto.getLocale()) + "  -  " +
						uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.stdDev") + ": " +
					    StringUtil.getFloatToString(new Float(stdDeviation), uto.getLocale()) + "  -  " +
					    uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.varian") + ": " +
					    StringUtil.getFloatToString(variance, uto.getLocale()));
		} else {
			cto.setTitle("");
		}
        
        cto.setCategoryDataset(dset);
        cto.setShowLegend(false);
		byte[] ba = JFreeChartUtil.chartToImg(JFreeChartUtil.getScatterPlot(cto), 820, 300);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);
	}
		
}
