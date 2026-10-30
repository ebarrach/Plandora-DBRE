package com.pandora.bus.gadget;

import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Vector;

import com.pandora.CategoryTO;
import com.pandora.FieldValueTO;
import com.pandora.OccurrenceTO;
import com.pandora.ProjectTO;
import com.pandora.RequirementTO;
import com.pandora.TransferObject;
import com.pandora.bus.occurrence.IterationOccurrence;
import com.pandora.delegate.CategoryDelegate;
import com.pandora.delegate.OccurrenceDelegate;
import com.pandora.delegate.ProjectDelegate;
import com.pandora.delegate.RequirementDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.helper.DateUtil;

public class IterationBurndownHighchartGadget extends HighchartGadget {

	public static final String ITERATION_BURNDOWN_PROJECT  = "PROJECT";
	public static final String ITERATION_BURNDOWN_CATEGORY = "CATEGORY";

    private float[][] valLine = null;

	
	public String getUniqueName(){
		return "label.manageOption.gadget.iterationBurndown";
	}
	
	public String getId(){
		return "ITERATION_BURNDOWN_CHART";
	}
	
	public String getImgLogo() {
		return "../images/gdglogo-6.png";
	}	

	public String getCategory() {
		return "label.manageOption.gadget.agile";
	}
	
	public String getDescription() {
		return "label.manageOption.gadget.burndown.desc";
	}
	
	@Override	
    public Vector<TransferObject> getFieldsId(){
    	Vector<TransferObject> response = new Vector<TransferObject>();
       	response.add(new TransferObject(ITERATION_BURNDOWN_PROJECT, "-1"));
       	response.add(new TransferObject(ITERATION_BURNDOWN_CATEGORY, "-1"));
        return response;
    }
	
	@Override
    public Vector getFields(){
		return getFields(null);
    }
	
	@Override
	public Vector<FieldValueTO> getFields(Vector<TransferObject> currentValues) {
		Vector<FieldValueTO> response = new Vector<FieldValueTO>();
    	ProjectDelegate pdel = new ProjectDelegate();
    	CategoryDelegate cdel = new CategoryDelegate();

    	try {
        	Vector<ProjectTO> buff = pdel.getProjectListByUser(super.handler);
        	
        	Vector<TransferObject> projList = new Vector<TransferObject>();
        	TransferObject defaultOpt = new TransferObject("-1", "label.combo.select");
        	projList.addElement(defaultOpt);
        	if (buff!=null) {
        		projList.addAll(buff);
        	}
        	response.add(new FieldValueTO(ITERATION_BURNDOWN_PROJECT, "label.manageOption.gadget.burndown.project", projList));

        	
        	Vector<TransferObject> projCategory = new Vector<TransferObject>();
        	TransferObject defcat = new TransferObject("-1", "label.all2");
        	projCategory.addElement(defcat);
        	
        	ProjectTO pto = new ProjectTO(super.getSelected(ITERATION_BURNDOWN_PROJECT, currentValues));
        	Vector<CategoryTO> catlist = cdel.getCategoryListByType(CategoryTO.TYPE_REQUIREMENT, pto, false);
        	projCategory.addAll(catlist);
        	
        	response.add(new FieldValueTO(ITERATION_BURNDOWN_CATEGORY, "label.manageOption.gadget.burndown.category", projCategory));

    	} catch(Exception e){
    		e.printStackTrace();
    		response = null;
    	}
    	
        return response;
    }
    
    public int getPropertyPanelWidth(){
    	return 400;
    }

    public int getPropertyPanelHeight(){
    	return 160;
    }
    
	@Override
	public boolean canReloadFields() {
		return true;
	}	    
   
	public String generate(Vector selectedFields)  throws BusinessException {		
	    String response = null;
	    ProjectDelegate pdel = new ProjectDelegate();
        RequirementDelegate rdel = new RequirementDelegate();
        String xaxis[] = null;
        String barLabels[] = null;
        
        try {
            ProjectTO pto = pdel.getProjectObject(new ProjectTO(super.getSelected(ITERATION_BURNDOWN_PROJECT, selectedFields)), true);
            if (pto!=null) {
            	String categoryId = super.getSelected(ITERATION_BURNDOWN_CATEGORY, selectedFields);            	
                
                Vector<RequirementTO> allReqs = rdel.getThinListByProject(pto, categoryId);
                if (allReqs!=null) {
                    int remainingReqs = allReqs.size();
                    int createdReqs = 0;
                    int closedReqs = remainingReqs;
                    int barsUntilProjectEnd = 0;
                    String endLabel = super.getI18nMsg("label.manageOption.gadget.burndown.end");
                    String burnLabel = super.getI18nMsg("label.manageOption.gadget.burndown.label") + " " + pto.getName();
                    
                    Vector<Object[]> list = this.getIterations(pto);
                    this.initArray(list.size());
                    xaxis = new String[list.size()];
                    barLabels = new String[list.size()];
                    
                    int c = 0, itc = 1;
                    for (Object[] item : list) {
                        if (item[0]!=null) {
                            Timestamp deadline = (Timestamp)item[0];
                            int[] reqsNum = this.getReqsByDeadLine(allReqs, deadline);
                            closedReqs = reqsNum[0];
                            createdReqs = reqsNum[1];
                            boolean isIteration = ((Boolean)item[2]).booleanValue();
                            xaxis[c] = (isIteration ?  "I" + (itc)  : endLabel); 
                            barLabels[c] = (String)item[1] + " : " + DateUtil.getDate(deadline, super.handler.getCalendarMask(), super.handler.getLocale());
                            
                            if (isIteration) {
                            	this.valLine[c][1]= remainingReqs - closedReqs;	
                            	itc ++;
                            } else {
                            	barsUntilProjectEnd = c + 1;
                            }
                            this.valLine[c][2] = createdReqs;
                            
                        } else {
                        	if (c>0) {
                        		boolean isIerationBeforeNow = ((Boolean)item[2]).booleanValue();
                        		if (isIerationBeforeNow) {
                        			this.valLine[c][1] = this.valLine[c-1][1];	
                        		}
                        	}
                            xaxis[c] = "";
                            barLabels[c] = "";
                        }
                        
                        c++;
                    }
                    
                    if (pto.getEstimatedClosureDate()!=null) {
                    	this.generateRefLine(remainingReqs, barsUntilProjectEnd);	
                    }

                    
            		//draw the bars                    
                    response = super.getHeader(this.getId(), "column", burnLabel, " ", false) +
                 		   super.getLegend(false) +
                 		   super.getXAxis(xaxis, false) +
                 		   super.getYAxis(0, " ") +
                 		   super.getTooltip(null) +
                 		   super.getPlot(new String[]{"Reference", "Burndown", "Num.Requests"}, this.valLine, false, 
                 				   new String[]{"c0c0c0", "800000", "000040"}, new String[]{"spline","spline","column"}, "column") + 
                 		   super.getFooter();

                    
                }
            }
                    	
        } catch (Exception e) {
        	e.printStackTrace();
        }
        
        return response;
	}

	
	private void generateRefLine(int remainingReqs, int size) {
		if (remainingReqs>0 && size>1) { 
			float reqs = (float)remainingReqs;
			float diff = (float)((float)remainingReqs / (float)(size-1)); 
			for (int i=0; i<size; i++) {
				this.valLine[i][0] = reqs;
				reqs = reqs - diff;				
			}
		}
	}

	
	private Vector<Object[]> getIterations(ProjectTO pto) throws BusinessException{
	    OccurrenceDelegate odel = new OccurrenceDelegate();
	    Vector<Object[]> response = new Vector<Object[]>();
	    Vector<Object[]> buff = new Vector<Object[]>();
	    
        Timestamp cursor = null;
        int gap = 1000000; //contain the small difference (in days) between two iterations
        
        Vector<OccurrenceTO> list = odel.getIterationListByProject(pto.getId(), true);
        if (list!=null) {
            Iterator<OccurrenceTO> i = list.iterator();
            while(i.hasNext()) {
                OccurrenceTO oto = i.next();
                
        		//check if iteration deadline exists and make sure that must be considered only iterations non-aborted and already started                
    			if (oto.getStatus()!=null && !oto.getStatus().equals(IterationOccurrence.STATE_FINAL_1) && !oto.getStatus().equals(IterationOccurrence.STATE_START)) {
                    Timestamp deadline = oto.getFieldDateByKey(IterationOccurrence.ITERATION_FINAL_DATE, new IterationOccurrence());
                    if (deadline!=null) {
                        Object[] item = new Object[4];
                        item[0] = deadline;
                        item[1] = oto.getName();
                        item[2] = new Boolean(true);
                        buff.addElement(item);
                        
                        if (cursor!=null) {
                            int between = DateUtil.getSlotBetweenDates(cursor, deadline);
                            if (between>0 && between<=gap) {
                                gap = between;
                            }
                        }
                        
                        cursor = deadline;                        
                    }
    			}
            }
            
            //if necessary, include the estimated project closure date into the list (at ordered position)
            if (pto.getEstimatedClosureDate()!=null) {
            	int z = 0;
            	String endLabel = super.getI18nMsg("label.projEstimClosure");
            	boolean lastOne = true;
            	for (Object[] item : buff) {
                    Timestamp deadline = (Timestamp)item[0];
                    if (!pto.getEstimatedClosureDate().after(deadline)) {
                    	buff.add(z, new Object[] {pto.getEstimatedClosureDate(), endLabel, new Boolean(false)});
                    	lastOne = false;
                    	break;
                    }
                    z++;
                }
                
                if (lastOne) {
                	buff.addElement(new Object[] {pto.getEstimatedClosureDate(), endLabel, new Boolean(false)});
                }
            }

            
            cursor = null;
        	for (Object[] item : buff) {
                Timestamp deadline = (Timestamp)item[0];
                if (cursor!=null) {
                    int between = DateUtil.getSlotBetweenDates(cursor, deadline);
            	    for (int k=1; k<(int)(between/gap); k++) {
            	    	Object[] gapItem = new Object[3];
            	    	gapItem[2] = new Boolean(deadline.before(DateUtil.getNow()));
            	        response.addElement(gapItem); 
            	    }
            	    response.addElement(item);
            	    
                } else {
                    response.addElement(item);
                }
                cursor = deadline;
            }                    
        }
        
        return response;
	}
	

	
	private int[] getReqsByDeadLine(Vector<RequirementTO> allReqs, Timestamp deadline) {
	    int[] response = new int[2];
	    for (RequirementTO rto : allReqs) {
	        if (rto.getFinalDate()!=null && rto.getFinalDate().before(deadline)) {
	            response[0]++;
	        }
	        if (rto.getCreationDate().before(deadline)) {
	            response[1]++;
	        }
	    }
	    return response;
	}

	
	private void initArray(int size){
		
		if (size<=0) {
			size = 1;
		}
	
		this.valLine = new float[size][3];
	
        for (int i=0; i<size; i++) {
        	this.valLine[i][0] = 0;
        	this.valLine[i][1] = 0;
        	this.valLine[i][2] = 0;
        }
	}

}
