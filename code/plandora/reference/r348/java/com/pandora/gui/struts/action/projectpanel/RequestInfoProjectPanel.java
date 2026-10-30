package com.pandora.gui.struts.action.projectpanel;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.pandora.CategoryTO;
import com.pandora.CustomerTO;
import com.pandora.JFreeChartTO;
import com.pandora.ProjectTO;
import com.pandora.RequirementStatusTO;
import com.pandora.RequirementTO;
import com.pandora.UserTO;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.RequirementDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.gui.struts.action.ProjectPanelAction;
import com.pandora.gui.struts.form.ProjectPanelForm;
import com.pandora.helper.DateUtil;
import com.pandora.helper.JFreeChartUtil;
import com.pandora.helper.SessionUtil;

public class RequestInfoProjectPanel extends ProjectPanelAction {
	
	private static final int CATEGORY_CRITERIUM  = 1;
	
	private static final int PRIORITY_CRITERIUM  = 2;
	
	private static final int STATUS_CRITERIUM    = 3;
	
	private static final int REQUESTER_CRITERIUM = 4;
	
	private static final int CLOSURE_CRITERIUM   = 5;
	
	@Override
	public String getPanelId() {
		return "REQ_INFO";
	}

	@Override
	public String getPanelTitle() {
		return "title.projectPanelForm.req";
	}
	
	@Override
	public String renderPanel(HttpServletRequest request,
			ProjectPanelForm frm) throws BusinessException {
		StringBuffer sb = new StringBuffer("");
		ProjectDelegate pdel = new ProjectDelegate();
		RequirementDelegate rdel = new RequirementDelegate();

		if (frm.getProjectId()!=null && !frm.getProjectId().trim().equals("")) {
			ProjectTO pto = new ProjectTO(frm.getProjectId());
			pto = pdel.getProjectObject(pto, true);
			
			Vector<RequirementTO> reqList = rdel.getListByProject(pto, "-2", "-1", "-2", "-1", null);
			sb.append("<table width=\"95%\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\n");
			sb.append("<tr>\n");
			sb.append("<td width=\"20\">&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getReqByCriterium(pto, request, reqList, CATEGORY_CRITERIUM) + "\" />\n");
			sb.append("&nbsp;<img src=\"data:image/png;base64," + this.getReqByCriterium(pto, request, reqList, PRIORITY_CRITERIUM) + "\" />\n");
			sb.append("&nbsp;<img src=\"data:image/png;base64," + this.getReqByCriterium(pto, request, reqList, STATUS_CRITERIUM) + "\" />\n");
			sb.append("</td><td>&nbsp;</td>\n");
			sb.append("</tr><tr>\n");
			sb.append("<tr>\n");
			sb.append("<td colspan=\"3\">&nbsp;</td>\n");
			sb.append("</tr><tr>\n");
			sb.append("<td width=\"20\">&nbsp;</td>\n");
			sb.append("<td>&nbsp;<img src=\"data:image/png;base64," + this.getReqByCriterium(pto, request, reqList, REQUESTER_CRITERIUM) + "\" />\n");
			sb.append("&nbsp;<img src=\"data:image/png;base64," + this.getReqByCriterium(pto, request, reqList, CLOSURE_CRITERIUM) + "\" />\n");
			sb.append("&nbsp;<img src=\"data:image/png;base64," + this.getReqOpenCloseChart(pto, request, reqList) + "\" />\n</td>");
			sb.append("<td>&nbsp;</td>\n");
			sb.append("<td>&nbsp;</td>\n");
			sb.append("</tr>\n");
			sb.append("</table>");
			
		}
		return sb.toString();
	}
	
	
	private String getReqOpenCloseChart(ProjectTO pto, HttpServletRequest request, Vector<RequirementTO> reqList) {
		Vector<String> vPeriod = new Vector<String>();
		HashMap<String, Integer> hmOpenedReq = new HashMap<String, Integer>();
		HashMap<String, Integer> hmClosedReq = new HashMap<String, Integer>();
		
		UserTO uto = SessionUtil.getCurrentUser(request);
		JFreeChartTO jcto = new JFreeChartTO();
		jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.acc"));
		jcto.setRotateCategoryLabel(true);

		for (RequirementTO rto : reqList) {
			this.addElement(vPeriod, hmOpenedReq, uto, rto.getCreationDate());			
			if (rto.getFinalDate()!=null) {
				this.addElement(vPeriod, hmClosedReq, uto, rto.getFinalDate());
			}
		}

		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		Collections.sort(vPeriod);
		String lastPeriod = null;
		for (String period : vPeriod) {
			this.addValue(hmOpenedReq, uto, dset, lastPeriod, period, "label.manageOption.gadget.reqSummary.open");
			this.addValue(hmClosedReq, uto, dset, lastPeriod, period, "label.manageOption.gadget.reqSummary.closed");			
			lastPeriod = period;
		}
        jcto.setCategoryDataset(dset);
        
		byte[] ba = JFreeChartUtil.chartToImg(JFreeChartUtil.getLineChart(jcto, null), 320, 200);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);

	}

	
	private String getReqByCriterium(ProjectTO pto, HttpServletRequest request, Vector<RequirementTO> reqList, int type) {
		HashMap<String, Integer> hmReqsByCrit = new HashMap<String, Integer>();
		HashMap<String, String> hmCritName = new HashMap<String, String>();

		UserTO uto = SessionUtil.getCurrentUser(request);
		JFreeChartTO jcto = new JFreeChartTO();
		this.setupChart(type, uto, jcto);
		
		for (RequirementTO rto : reqList) {
			String key= "", name = "";
			boolean addVal = true;
			
			if (type==CATEGORY_CRITERIUM) {
				CategoryTO cto = rto.getCategory();
				key = cto.getId();
				name = cto.getName();
			} else if (type==PRIORITY_CRITERIUM) {
				key = rto.getPriority()+"";
				name = uto.getBundle().getMessage(uto.getLocale(), "label.requestPriority." + rto.getPriority());
			} else if (type==STATUS_CRITERIUM) {
				RequirementStatusTO sto = rto.getRequirementStatus();
				key = sto.getId();
				name = sto.getName();
			} else if (type==REQUESTER_CRITERIUM) {
				CustomerTO to = rto.getRequester();
				key = to.getId();
				name = to.getUsername();
			} else {
				RequirementStatusTO sto = rto.getRequirementStatus();
				Integer state = sto.getStateMachineOrder();
				if (rto.getFinalDate()!=null && state!=null && !state.equals(RequirementStatusTO.STATE_MACHINE_CANCEL) && 
						rto.getFinalDate().after(rto.getCreationDate())) {
					long ct = rto.getFinalDate().getTime() - rto.getCreationDate().getTime();
					if (ct <= 7200000) { // <= 2h
						key = name = "<2h";
					} else if (ct > 7200000 && ct <=259200000) { // >2h && <=3d
						key = name = ">2h <3d";
					} else if (ct > 259200000 && ct <= 604800000) { // >3d && <=7d
						key = name = ">3d <7d";
					} else { // >7d
						key = name = ">7d";
					}
				} else {
					addVal = false;
				}				
			}
			
			
			if (addVal) {
				hmCritName.put(key, name);
				Integer reqVal = hmReqsByCrit.get(key);
				if (reqVal==null) {
					reqVal = 1;
				} else {
					reqVal = reqVal + 1;
				}
				hmReqsByCrit.put(key, reqVal);				
			}
		}
		
		Vector<String> allValues = new Vector<String>();
		Set<String> criteria = hmReqsByCrit.keySet();
		for (String criId : criteria) {
			Integer val = hmReqsByCrit.get(criId);
			if (val!=null) { 
				allValues.add(String.format("%09d", val) + "_" + hmCritName.get(criId) + " ");
			}
		}
		
		DefaultCategoryDataset dset = new DefaultCategoryDataset();
		Collections.sort(allValues, Collections.reverseOrder());
		for (String tokens : allValues) {
			String[] token = tokens.split("_");
			dset.addValue(new Integer(token[0]), "", token[1].trim());
		}
        jcto.setCategoryDataset(dset);
        
		byte[] ba = JFreeChartUtil.chartToImg(JFreeChartUtil.getStackbar(jcto, false), 320, 200);
		byte[] encoded = Base64.encodeBase64(ba);
		return new String(encoded);
	}

	private void setupChart(int type, UserTO uto, JFreeChartTO jcto) {
		jcto.setShowLegend(false);
		jcto.setOrientation(PlotOrientation.HORIZONTAL);
		if (type==CATEGORY_CRITERIUM) {
			jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.cat"));
		} else if (type==PRIORITY_CRITERIUM) {
			jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.pri"));
		} else if (type==STATUS_CRITERIUM) {
			jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.stat"));
		} else if (type==REQUESTER_CRITERIUM) {
			jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.own"));
		} else if (type==CLOSURE_CRITERIUM) {
			jcto.setTitle(uto.getBundle().getMessage(uto.getLocale(), "title.projectPanelForm.req.tim"));
		}
	}

	private void addValue(HashMap<String, Integer> hmReq, UserTO uto, DefaultCategoryDataset dset, String lastPeriod, String period, String label) {
		Integer val = hmReq.get(period);
		if (val==null) {
			val = new Integer(0);
		}
		Integer previousVal = hmReq.get(lastPeriod);
		if (previousVal==null) {
			previousVal = new Integer(0);
		}
		dset.addValue(new Integer(val+previousVal), uto.getBundle().getMessage(uto.getLocale(), label), period);
		hmReq.put(period, new Integer(val+previousVal));
	}

	
	private void addElement(Vector<String> vPeriod, HashMap<String, Integer> hmReq, UserTO uto, Timestamp dtRef) {
		String strPeriod = DateUtil.getDateTime(dtRef, "yyyy-MM", uto.getLocale());
		Integer reqVal = hmReq.get(strPeriod);
		if (reqVal==null) {
			reqVal = 1;
		} else {
			reqVal = reqVal + 1;
		}
		hmReq.put(strPeriod, reqVal);				
		vPeriod.remove(strPeriod);
		vPeriod.addElement(strPeriod);
	}
	
}
