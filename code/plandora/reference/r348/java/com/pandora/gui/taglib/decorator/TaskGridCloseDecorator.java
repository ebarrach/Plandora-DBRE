package com.pandora.gui.taglib.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.ResourceTaskTO;
import com.pandora.RootTO;
import com.pandora.TaskStatusTO;
import com.pandora.TaskTO;
import com.pandora.UserTO;
import com.pandora.delegate.TaskDelegate;
import com.pandora.delegate.UserDelegate;
import com.pandora.helper.HtmlUtil;

public class TaskGridCloseDecorator extends ColumnDecorator {

	@Override
	public String decorate(Object columnValue) {
		String image = "";
		UserTO uto = (UserTO)this.getSession().getAttribute(UserDelegate.CURRENT_USER_SESSION);        
		ResourceTaskTO rtto = (ResourceTaskTO)this.getObject();
		TaskTO tto = (TaskTO)rtto.getTask();

		if (!rtto.getTaskStatus().getStateMachineOrder().equals(TaskStatusTO.STATE_MACHINE_CLOSE)){
			String altValue = this.getBundleMessage("label.grid.requestform.closeTask");
			image ="<a href=\"javascript:closeTask('" + tto.getId() + "', '" + rtto.getResource().getId() +"');\" border=\"0\"> \n";
			image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/stop.gif\" >";
			image += "</a>";
		} else {
			image += "&nbsp;";
		}			

		return image;
	}

	@Override
	public String decorate(Object columnValue, String tag) {
		return decorate(columnValue);
	}

	@Override
	public String contentToSearching(Object columnValue) {
		return columnValue+"";
	}
	
	

}
