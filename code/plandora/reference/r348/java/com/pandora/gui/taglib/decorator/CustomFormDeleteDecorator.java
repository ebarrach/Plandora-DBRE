package com.pandora.gui.taglib.decorator;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.CustomFormTO;
import com.pandora.UserTO;
import com.pandora.dao.CustomFormDAO;
import com.pandora.delegate.UserDelegate;
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
public class CustomFormDeleteDecorator extends ColumnDecorator {

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

		UserTO uto = (UserTO) this.getSession().getAttribute(UserDelegate.CURRENT_USER_SESSION);
		final CustomFormDAO dao = new CustomFormDAO();
		CustomFormTO cfto = null;
		try {
			cfto = (CustomFormTO) dao.getObject(new CustomFormTO(columnValue.toString()));
		} catch (DataAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		String image = "";
		if (cfto.getMetaForm().getConstraintEditDelete()) {
			if (cfto != null && (cfto.getCreationUser() == null || uto.getId().equals(cfto.getCreationUser().getId()))) {
				image += contentColumn(columnValue, tag);
			} else {
				image += "&nbsp;";
			}
		} else {
			image += contentColumn(columnValue, tag);
		}

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
	
	private String contentColumn(Object columnValue, String tag){
		String response = "";
		String altValue = this.getBundleMessage("label.grid.remove");
		response ="<a href=\"javascript:remove('" + columnValue + "'," + tag + ");\" border=\"0\"> \n";
		response += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/remove.gif\" />";
		response += "</a>"; 		
		return response;
	}

}
