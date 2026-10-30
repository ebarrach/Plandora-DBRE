package com.pandora.gui.taglib.decorator;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.ResourceTaskTO;
import com.pandora.helper.HtmlUtil;

public class ResourceTaskChangeProjectDecorator extends ColumnDecorator {

	@Override
	public String decorate(Object columnValue) {
		String image = "";
		ResourceTaskTO rtto = (ResourceTaskTO)this.getObject();
		if (rtto!=null && rtto.getTask()!=null && rtto.getResource()!=null && rtto.getResource().getProject()!=null){
		    String altValue = this.getBundleMessage("label.changeProject");
	    	
		    image ="<a href=\"javascript:showChangeProjectPopup('" + 
		    		rtto.getTask().getId() + "', '" + rtto.getResource().getId() + "', '" + 
		    		rtto.getResource().getProject().getId() + "');\" border=\"0\"> \n";	
			image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/prefresh.png \"></a>";

		}
		return image;
	}

	@Override
	public String decorate(Object columnValue, String tag) {
        return decorate(columnValue, "");
	}

	@Override
	public String contentToSearching(Object columnValue) {
    	return columnValue+"";
	}

}
