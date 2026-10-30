package com.pandora.bus.gadget;

import java.sql.Timestamp;
import java.sql.Types;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Vector;

import com.pandora.FieldValueTO;
import com.pandora.PreferenceTO;
import com.pandora.ProjectTO;
import com.pandora.RootTO;
import com.pandora.TransferObject;
import com.pandora.bus.PreferenceBUS;
import com.pandora.bus.occurrence.Occurrence;
import com.pandora.bus.occurrence.OccurrenceBUS;
import com.pandora.delegate.DbQueryDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.helper.DateUtil;

public class OccurrenceHighchartGadget extends HighchartGadget {

	private static final String OCC_PROJECT  = "PROJECT";
	
	private static final String OCC_TYPE     = "OCC_TYPE";
	
	private static final String OCC_INTERVAL = "OCC_INTERVAL";
	
	private HashMap<String, Integer> granularity = new HashMap<String, Integer>();
	

	public String getUniqueName(){
		return "label.manageOption.gadget.occ";
	}
	
	public String getId(){
		return "PROJECT_OCC_CHART";
	}

	@Override	
    public Vector<TransferObject> getFieldsId(){
    	Vector<TransferObject> response = new Vector<TransferObject>();
       	response.add(new TransferObject(OCC_PROJECT, "-1"));
       	response.add(new TransferObject(OCC_TYPE, "1"));
       	response.add(new TransferObject(OCC_INTERVAL, "1"));
        return response;
    }
	
    public int getPropertyPanelWidth(){
    	return 400;
    }

    public int getPropertyPanelHeight(){
    	return 140;
    }
        
	public String getCategory() {
		return "label.category.project";
	}
    
	public String getImgLogo() {
		return "../images/gdglogo-3.png";
	}
	
    @SuppressWarnings("rawtypes")
	public Vector getFields(){
    	Vector<FieldValueTO> response = new Vector<FieldValueTO>();
    	PreferenceBUS pbus = new PreferenceBUS();
    	OccurrenceBUS obus = new OccurrenceBUS();
    	
    	try {
    		Vector<TransferObject> projList = super.getProjectFromUser(true);    	
        	response.add(new FieldValueTO(OCC_PROJECT, "label.category.project", projList));
        	
            Vector<TransferObject> occTypeList = new Vector<TransferObject>();
            
            PreferenceTO pto = pbus.getObjectByUser(new RootTO(RootTO.ROLE_ROOT+""));
            String val = pto.getPreference(PreferenceTO.OCCURRENCE_BUS_CLASS);
            if (val!=null) {
            	String[] occs = val.split(";");
            	for (String occClass : occs) {
					Occurrence occ = obus.getOccurrenceClass(occClass.trim());
					if (occ!=null) {
						TransferObject to = new TransferObject(occClass.trim(), occ.getUniqueName());
						occTypeList.add(to);						
					}
				}
            	response.add(new FieldValueTO(OCC_TYPE, "label.manageOption.gadget.occ.name", occTypeList));
            }
            
            Vector<TransferObject> intervList = new Vector<TransferObject>();
            intervList.add(new TransferObject("1", "label.manageOption.gadget.occ.interval.1"));
            intervList.add(new TransferObject("2", "label.manageOption.gadget.occ.interval.2"));
            intervList.add(new TransferObject("3", "label.manageOption.gadget.occ.interval.3"));
        	response.add(new FieldValueTO(OCC_INTERVAL, "label.manageOption.gadget.occ.interval", intervList));
            
        	
    	} catch(Exception e){
    		e.printStackTrace();
    		response = null;
    	}
    	
        return response;
    }
	
	@SuppressWarnings("unchecked")
	public String generate(@SuppressWarnings("rawtypes") Vector selectedFields)  throws BusinessException {
        ProjectDelegate pdel = new ProjectDelegate();
        OccurrenceBUS obus = new OccurrenceBUS();
        String response = "";
        
        try {
			String occClass = super.getSelected(OCC_TYPE, selectedFields);
			Occurrence occ = obus.getOccurrenceClass(occClass.trim());
			if (occ!=null) {
				ProjectTO pto = pdel.getProjectObject(new ProjectTO(super.getSelected(OCC_PROJECT, selectedFields)), true);
	        	String intervType = super.getSelected(OCC_INTERVAL, selectedFields);
	        	String occName = super.getI18nMsg(occ.getUniqueName(), this.handler.getLocale());
	        	
	        	String mask = "yyyyMMdd";
	            Timestamp iniRange = DateUtil.getChangedDate(DateUtil.getNowWithoutTime(), Calendar.DATE, -6);            
	        	int slotNumber = 7;
	        	if (intervType.equals("1")) {
	        		iniRange = DateUtil.getChangedDate(DateUtil.getNowWithoutTime(), Calendar.DATE, -29);
	        		slotNumber = 30;
	        		mask = "yyyyMMdd";
	        	} else if (intervType.equals("2")) {
	        		iniRange = DateUtil.getChangedDate(DateUtil.getNowWithoutTime(), Calendar.DATE, -179);
	        		for (int w=0; w<=7; w++) {
	        			iniRange = DateUtil.getChangedDate(iniRange, Calendar.DATE, w);
	        			if (DateUtil.get(iniRange, Calendar.DAY_OF_WEEK)==Calendar.SUNDAY) {
	        				break;
	        			}
	        		}
	        		slotNumber = 26;
	        		mask = "w-yy";
	        	} else if (intervType.equals("3")) {
	        		iniRange = DateUtil.getChangedDate(DateUtil.getNowWithoutTime(), Calendar.DATE, -364);
	        		iniRange = DateUtil.getDateTime("01", DateUtil.get(iniRange, Calendar.MONTH)+"", 
	        				DateUtil.get(iniRange, Calendar.YEAR)+"", "00", "00", "00");
	        		slotNumber = 13;
	        		mask = "MMM-yyyy";
	        	}
	        	this.defineGranulary(intervType, slotNumber, iniRange, mask);
	    		float[][] dataVals = new float[slotNumber][2]; 
	            String xaxis[] = this.getXAxisLabel(intervType, slotNumber, iniRange);
	        	
	            if (pto!=null) {
	                DbQueryDelegate qdel = new DbQueryDelegate();
	                String sql = "select o.id, o.name, p.creation_date, max(h.creation_date) as end_date " +
	                "from occurrence o " +
	                "inner join planning p on (p.id = o.id) " +
	                "left outer join occurrence_history h on (h.occurrence_id = o.id and h.occurrence_status in ('100','99', '98')) " +
	                "where o.project_id=? and o.source=? " +
	                "group by o.id, o.name, p.creation_date";

	                int[] types = new int[] {Types.VARCHAR, Types.VARCHAR};
	                Vector<String> params = new Vector<String>();
	                params.addElement(pto.getId());
	                params.addElement(occ.getClass().getName());
	                
	                Vector<Vector<Object>> dbList = qdel.performQuery(sql, types, params);
	                if (dbList!=null) {                	
	                	for (int i=1; i<dbList.size(); i++) {
	                		Vector<Object> item = (Vector<Object>)dbList.elementAt(i);
	                    	Timestamp tmOpen = (Timestamp)item.elementAt(2);
	                    	Timestamp tmClose = (Timestamp)item.elementAt(3);
	                    	if (tmOpen!=null && !tmOpen.after(DateUtil.getNow())) {
	                			setTimestampOnGranularity(mask, dataVals, tmOpen, 0);
	                    	}
	                    	if (tmClose!=null && !tmClose.after(DateUtil.getNow())) {
	                			setTimestampOnGranularity(mask, dataVals, tmOpen, 1);
	                    	}

	                	}
	                }
	                		
	                String openLbl = super.getI18nMsg("label.manageOption.gadget.occ.open");
	                String closeLbl = super.getI18nMsg("label.manageOption.gadget.occ.close");
	                String[] labels = new String[] {openLbl, closeLbl};
	                
	        		//draw the bars
	                response = super.getHeader(this.getId(), "line", openLbl + " X " + closeLbl + " (" + occName + ")", pto.getName(), false) +
	                		   super.getLegend(false) +
	                		   super.getXAxis(xaxis, false) +
	                		   super.getYAxis(0, "(qtd)") +
	                		   super.getTooltip(null) +
	                		   super.getPlot(labels, dataVals, true, null, null, "line") +
	                		   super.getFooter();

	            }				
			}
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return response;
	}

	private void setTimestampOnGranularity(String mask, float[][] dataVals, Timestamp refTs, int idx) {
		SimpleDateFormat df = new SimpleDateFormat(mask, this.handler.getLocale());			
		Integer slot = (Integer)this.granularity.get(df.format(refTs));
		int index = 0;
		if (slot!=null) {
			index = slot.intValue();
		}
		try {
			if (index<dataVals.length) {
				for(int i=index; i<dataVals.length; i++) {
					dataVals[i][idx]++;
				}
			}
		} catch(ArrayIndexOutOfBoundsException e) {
			System.out.println("index:" + index + " slot: " + slot + " dt:" + refTs + " gran.list:" + this.granularity);
		}
	}
	
	private void defineGranulary(String intervType, int slotNumber, Timestamp iniRange, String mask) {
		Timestamp cursor = null;
		DateFormat df = null;
    	if (intervType!=null && !intervType.trim().equals("")) {
    		for (int i=0; i <slotNumber; i++) {
    			if (intervType.equals("1")) {
    				cursor = DateUtil.getChangedDate(iniRange, Calendar.DATE, i);
    	    	} else if (intervType.equals("2")) {
    				cursor = DateUtil.getChangedDate(iniRange, Calendar.WEEK_OF_YEAR, i);
    	    	} else if (intervType.equals("3")) {
    	    		cursor = DateUtil.getChangedDate(iniRange, Calendar.MONTH, i);							
    	    	}
    			df = new SimpleDateFormat(mask, this.handler.getLocale());
    			this.granularity.put(df.format(cursor), new Integer(i));			
    		}		    		
    	}
	}

	
	private String[] getXAxisLabel(String intervType, int slotNumber, Timestamp iniRange){
		String xaxis[] = new String[slotNumber];
		
        if (intervType.equals("1")) {
            for (int i=0; i<slotNumber;i++) {
            	DateFormat df = new SimpleDateFormat("dd-MMM", this.handler.getLocale());
            	String slotLabel = df.format(DateUtil.getChangedDate(iniRange, Calendar.DATE, i));
            	xaxis[i] = slotLabel;
            }        		
        	
        } else if (intervType.equals("2")) {
            for (int i=0; i<slotNumber;i++) {
            	DateFormat df = new SimpleDateFormat("w-yy", this.handler.getLocale());
            	String slotLabel = df.format(DateUtil.getChangedDate(iniRange, Calendar.WEEK_OF_YEAR, i));
            	xaxis[i] = slotLabel;
            }        		
        	
        } else if (intervType.equals("3")) {
            for (int i=0; i<slotNumber;i++) {
            	DateFormat df = new SimpleDateFormat("MMM-yyyy", this.handler.getLocale());
            	String slotLabel = df.format(DateUtil.getChangedDate(iniRange, Calendar.MONTH, i));
            	xaxis[i] = slotLabel;
            }        		
        }
        
        return xaxis;
	}

}
