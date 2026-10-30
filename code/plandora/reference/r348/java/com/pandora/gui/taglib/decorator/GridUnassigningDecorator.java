package com.pandora.gui.taglib.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.ResourceTaskTO;
import com.pandora.RootTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.delegate.ResourceTaskDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.exception.BusinessException;
import com.pandora.helper.HtmlUtil;

/**
 * This decorator formats a grid cell with a detailed html image.<br>
 * It uses the information from table to assembly the a javascript call.<br>
 */
public class GridUnassigningDecorator extends ColumnDecorator {
	
	 /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object)
     */
    public String decorate(Object columnValue) {
        String image = "";
		ResourceTaskDelegate rtdel = new ResourceTaskDelegate();
		UserTO uto = (UserTO)this.getSession().getAttribute(UserDelegate.CURRENT_USER_SESSION);        
		ResourceTaskTO rtto = (ResourceTaskTO)this.getObject();
		TaskTO tto = (TaskTO)rtto.getTask();
		
		boolean unlinkEnabled = false;
		Vector<ResourceTaskTO> alloc = tto.getAllocResources();
		Iterator<ResourceTaskTO> i = alloc.iterator();
		while(i.hasNext()) {
			ResourceTaskTO rt = i.next();
			if (rt.getResource()!=null && rt.getResource().getUsername().equals(RootTO.ROOT_USER)){
				unlinkEnabled = true;
				break;
			}
			if (rt.getResource()!=null && rt.getResource().getUsername().equals(uto.getUsername()) && rt.getActualTime() != null && rt.getActualTime() > 0){
				unlinkEnabled = true;
				break;
			}
		}
		
		try {
			if (!unlinkEnabled && rtdel.getListByTask(tto).size() > 1){
				String altValue = this.getBundleMessage("label.grid.unassigning");		
				image ="<a href=\"javascript:" + "unlinkTask" + "('" + columnValue + "');\" border=\"0\"> \n";
				image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/unlink.gif\" >";
				image += "</a>"; 				
			} else {
			    image += "&nbsp;";
			}
		} catch (BusinessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		
		return image;
    }

    
    /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object, java.lang.String)
     */
    public String decorate(Object columnValue, String tag) {
        return decorate(columnValue);    	
    }
    
    
    /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#contentToSearching(java.lang.Object)
     */
    public String contentToSearching(Object columnValue) {
    	return columnValue+"";
    }    
    
}
