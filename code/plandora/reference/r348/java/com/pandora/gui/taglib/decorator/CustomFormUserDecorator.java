package com.pandora.gui.taglib.decorator;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.CustomFormTO;
import com.pandora.dao.CustomFormDAO;
import com.pandora.exception.DataAccessException;
import com.pandora.helper.HtmlUtil;

/**
 * This decorator formats a grid cell with a delete html image.<br>
 * It uses the information from table to assembly the a javascript call.<br>
 * 
 * Use 'property' and 'tag' attributes to set the fields that will be used by
 * decorator such as argument. In this case the decorator will be return a image
 * representing a 'remove' feature: <i>javascript:remove(id, tag);</i><br>
 * Ex.: &lt;display:column property="id" tag="myOperation" title="&nbsp;"
 * decorator="com.pandora.gui.taglib.decorator.GridRemoveDecorator"/&gt;</li>
 * 
 * <br>
 * Anyway, make sure that the image 'remove.gif' must be placed into /web/images
 */
public class CustomFormUserDecorator extends ColumnDecorator {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object)
	 */
	public String decorate(Object columnValue) {
		return decorate(columnValue, "remove");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object,
	 * java.lang.String)
	 */
	public String decorate(Object columnValue, String tag) {

		final CustomFormDAO dao = new CustomFormDAO();
		CustomFormTO cfto = null;
		try {
			cfto = (CustomFormTO) dao.getObject(new CustomFormTO(columnValue.toString()));
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		
		String createdBy = this.getBundleMessage("label.grid.createdBy");
		String modifiedBy = this.getBundleMessage("label.grid.modifiedBy");
		
		String altValue = "";
		String image = "";
		if (cfto.getCreationUser() == null && cfto.getModificationUser() == null){
			return "&nbsp;";
		} else if (cfto.getCreationUser() != null && cfto.getModificationUser() != null){
			altValue += createdBy + " : " + cfto.getCreationUser().getUsername();
			altValue += " / " + modifiedBy + " : " + cfto.getModificationUser().getUsername();	
		} else if (cfto.getCreationUser() != null && cfto.getModificationUser() == null){
			altValue += createdBy + " : " + cfto.getCreationUser().getUsername();
		} else if (cfto.getCreationUser() == null && cfto.getModificationUser() != null){
			altValue += createdBy + " : " + this.getBundleMessage("label.none");
			altValue += " / " + modifiedBy + " : " + cfto.getModificationUser().getUsername();
		} 

		image ="<a border=\"0\"> \n";
		image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/user.gif\" />";
		image += "</a>"; 		

		return image;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.apache.taglibs.display.ColumnDecorator#contentToSearching(java.lang.
	 * Object)
	 */
	public String contentToSearching(Object columnValue) {
		return columnValue + "";
	}
	
}
