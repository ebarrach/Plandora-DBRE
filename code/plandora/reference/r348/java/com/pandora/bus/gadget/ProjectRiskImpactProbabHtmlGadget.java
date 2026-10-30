package com.pandora.bus.gadget;

import java.util.Iterator;
import java.util.Vector;

import com.pandora.FieldValueTO;
import com.pandora.ProjectTO;
import com.pandora.RiskStatusTO;
import com.pandora.RiskTO;
import com.pandora.TransferObject;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.RiskDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.helper.HtmlUtil;

public final class ProjectRiskImpactProbabHtmlGadget extends HtmlGadget {

	private static final String RISK_HTML_PROJECT  = "PROJECT";
	private static final String RISK_HTML_SHOW_ALL = "SHOW_ALL";

	private Vector<RiskTO> riskList = new Vector<RiskTO>();
	
	@SuppressWarnings("rawtypes")
	@Override
	protected String generate(Vector selectedFields) throws BusinessException {
        ProjectDelegate pdel = new ProjectDelegate();
        RiskDelegate rdel = new RiskDelegate();
	
        StringBuffer response = new StringBuffer();
        try {
        	@SuppressWarnings("unchecked")
			ProjectTO pto = pdel.getProjectObject(new ProjectTO(super.getSelected(RISK_HTML_PROJECT, selectedFields)), true);            
            if (pto!=null) {
            	Vector<RiskTO> tempList = rdel.getRiskList(pto.getId());
            	
            	@SuppressWarnings("unchecked")
				String hideClose = super.getSelected(RISK_HTML_SHOW_ALL, selectedFields);
            	if (hideClose.equals("0")) {
            		for (RiskTO rto : tempList) {
	            		RiskStatusTO rsto = rto.getStatus();
						if (rsto!=null && rsto.getStatusType()!=null 
								&& !rsto.getStatusType().equals(RiskStatusTO.MATERIALIZE_RISK_TYPE)
								&& !rsto.getStatusType().equals(RiskStatusTO.VOIDED_RISK_TYPE) ) {
							riskList.add(rto);
						}
					} 
            	} else {
            		riskList = tempList;
            	}
            }
            this.writeHeader(response, pto);
            this.writeBody(response);
            
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return response.toString();
	}

	
	public String getCategory() {
		return "label.manageOption.gadget.management";
	}

	
	public String getDescription() {
		return "label.manageOption.gadget.risktbl.desc";
	}

	@SuppressWarnings("rawtypes")
	@Override	
	public Vector getFields() {
    	Vector<FieldValueTO> response = new Vector<FieldValueTO>();
    	try {
    		Vector<TransferObject> projList = getProjectFromUser(false);
        	response.add(new FieldValueTO(RISK_HTML_PROJECT, "label.manageOption.gadget.risktbl.project", projList));
        	
            Vector<TransferObject> boolList = new Vector<TransferObject>();
            boolList.add(new TransferObject("1", "label.yes"));
            boolList.add(new TransferObject("0", "label.no"));
        	response.add(new FieldValueTO(RISK_HTML_SHOW_ALL, "label.manageOption.gadget.riskperi.showall", boolList));
        	
    	} catch(Exception e){
    		e.printStackTrace();
    		response = null;
    	}
        return response;
	}

	
	@Override	
	public Vector<TransferObject> getFieldsId() {
		Vector<TransferObject> response = new Vector<TransferObject>();  	
       	response.add(new TransferObject(RISK_HTML_PROJECT, "-1"));
       	response.add(new TransferObject(RISK_HTML_SHOW_ALL, "1"));
        return response;
	}

	@Override		
	public String getId() {
		return "RISK_TABLE_CHART";
	}

	@Override	
	public String getImgLogo() {
		return "../images/gdglogo-21.png";
	}
	
	
	public int getPropertyPanelHeight() {
		return 150;
	}

	
	public int getPropertyPanelWidth() {
		return 400;
	}

	
	public String getUniqueName() {
		return "label.manageOption.gadget.risktbl";
	}
	
	private void writeBody(StringBuffer response) {		
		for (int row = 1; row <= 5; row++) {
			response.append("<tr class=\"formBody\">");
			
	        String impactLevelLabel = this.getI18nMsg("label.formRisk.impact." + row);
			String img = "<img src='../images/level" + row + ".png' " + HtmlUtil.getHint(impactLevelLabel) + " border='0'>";			
			
			response.append("<td height=\"20%\" align=\"right\">" + img + "&nbsp;&nbsp;</td>");			
			for (int col = 1; col <= 5; col++) {
				
		        String background = "bgcolor='#8BE355'";
		        String m = row+"-"+col;
		        if (m.equals("4-3") || m.equals("5-3") || m.equals("3-4") || m.equals("3-5") || m.equals("4-4") || m.equals("4-5") || m.equals("5-4") || m.equals("5-5")) {
		        	background = "bgcolor='#ED5A19'";  	
		        } else if (m.equals("1-5") || m.equals("2-3") || m.equals("2-4") || m.equals("2-5") || m.equals("3-2") || m.equals("3-3") || m.equals("4-2") || m.equals("5-1") || m.equals("5-2")) {
		        	background = "bgcolor='#EDE51D'";  	
		        }
		                    
				response.append("<td height=\"20%\" " + background + "><center>");				
				getCell(response, row, col);
				response.append("</center></td>");
			}
			response.append("</tr>");
		}
		response.append("</table></br>");
	}


	private void writeHeader(StringBuffer response, ProjectTO pto) {
		String label = "";
		if (pto!=null) {
			label = pto.getName();
		}
        String probLabel = this.getI18nMsg("label.formRisk.probability");
        String impacLabel = this.getI18nMsg("label.formRisk.impact");  	
		
		response.append("<table class=\"table\" width=\"100%\" height=\"90%\" border=\"1\" bordercolor=\"#10389C\" cellspacing=\"1\" cellpadding=\"0\">");
		response.append("<tr class=\"formBody\">");
		response.append("<td colspan=\"6\" height=\"20\" ><center>" + label + "</center></td>");
		response.append("</tr>");

		response.append("<tr class=\"tableRowHeader\">");
		response.append("<td>&nbsp;</td><td colspan=\"5\" height=\"20\" ><b><center>" + probLabel + "</center></b></td>");
		response.append("</tr>");
		
		response.append("<tr class=\"tableRowHeader\">");
		response.append("<td height=\"25\"><b><center>" + impacLabel + "</center></b></td>");
		for (int i=1; i<=5; i++) {
			
	        String probLevelLabel = this.getI18nMsg("label.formRisk.probability." + i);
			String img = "<img src='../images/level" + i + ".png' " + HtmlUtil.getHint(probLevelLabel) + " border='0'>";					
					
			response.append("<td width=\"12%\" height=\"25\"><b><center>" + img + "</center></b></td>");
		}
		response.append("</tr>");
	}



	private void getCell(StringBuffer response, int impact, int prob) {
		int numberOfRisks = 0;
        if (riskList!=null) {
            Iterator<RiskTO> i = riskList.iterator();
            while(i.hasNext()) {
            	RiskTO rto = (RiskTO)i.next();
            	if (rto.getFinalDate()==null && rto.getImpact().equals(impact+"") && rto.getProbability().equals(prob+"")  ) {
            		numberOfRisks++;
            	}	
            }
        }
        
        String val = numberOfRisks+"";
        if (numberOfRisks>0) {
        	val = "<b>" + numberOfRisks + "</b>";
        }
        
		response.append("<center>" + val + "</center>");
	}
	

}
