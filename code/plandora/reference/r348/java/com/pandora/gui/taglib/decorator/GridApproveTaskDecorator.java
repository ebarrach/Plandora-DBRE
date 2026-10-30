package com.pandora.gui.taglib.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.ResourceTaskTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.delegate.UserDelegate;
import com.pandora.helper.HtmlUtil;

/**
 * This decorator formats a grid cell with a detailed html image.<br>
 * It uses the information from table to assembly the a javascript call.<br>
 */
public class GridApproveTaskDecorator extends ColumnDecorator {
	
	 /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object)
     */
    public String decorate(Object columnValue) {
        String image = "";
		UserTO uto = (UserTO)this.getSession().getAttribute(UserDelegate.CURRENT_USER_SESSION);        
		ResourceTaskTO rtto = (ResourceTaskTO)this.getObject();
		TaskTO tto = (TaskTO)rtto.getTask();
		
		Vector<ResourceTaskTO> alloc = tto.getAllocResources();
		Iterator<ResourceTaskTO> i = alloc.iterator();
		while(i.hasNext()) {
			ResourceTaskTO rt = i.next();
			if (rt.getTaskStatus().isFinish()){
				String altValue = this.getBundleMessage("label.grid.approveTask");		
				image ="<a href=\"javascript:" + "approveTask" + "('" + columnValue + "','" + uto.getId() + "','" + rtto.getResource().getId() + "','" + tto.getProject().getId() + "',true);\" border=\"0\"> \n";
				image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/approve.gif\" >";
				image += "</a>"; 
			} else {
			    image += "&nbsp;";
			}	
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
