package com.pandora.gui.taglib.decorator;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.CustomerTO;
import com.pandora.helper.HtmlUtil;

public class CustomerRoleDecorator extends ColumnDecorator {


	public String decorate(Object columnValue, String tag) {
		String image = "&nbsp;";
		if (this.getObject() instanceof CustomerTO) {
			CustomerTO cto = (CustomerTO) this.getObject();
			String altValue = this.getBundleMessage("label.grid.project.role");

			image ="<a href=\"javascript:changeUserRole('" + cto.getId() + "','" + cto.getProject().getId() + "');\" border=\"0\"> \n";
			image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/roles.gif\" >";
			image += "</a>"; 				
			return image;
		}
		
		return image;
	}

	
	public String decorate(Object columnValue) {
		return decorate(columnValue, "");
	}

	
	public String contentToSearching(Object columnValue) {
		return null;
	}

}
