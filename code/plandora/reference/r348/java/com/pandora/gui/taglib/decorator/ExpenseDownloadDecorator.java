package com.pandora.gui.taglib.decorator;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.CostInstallmentTO;
import com.pandora.helper.HtmlUtil;

public class ExpenseDownloadDecorator extends ColumnDecorator {

	public String decorate(Object columnValue) {
		String image = "&nbsp;";		
		CostInstallmentTO cto = (CostInstallmentTO)getObject();
		if (cto!=null && cto.getCost()!=null && cto.getCost().getExpense()!=null) {
			String numAttach = cto.getCost().getExpense().getGenericTag();
			if (numAttach!=null && !numAttach.equals("0")) {
				String altValue = this.getBundleMessage("label.expense.download.attach");
				String altFiles = this.getBundleMessage("label.files");
				altValue = altValue + " (" + numAttach + " " + altFiles + ")";
				image ="<a href=\"javascript:downloadExpenseFiles('" + columnValue + "');\" border=\"0\"> \n";
				image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/compress.png\" >";
				image += "</a>"; 				
				
			}
		}
		return image;

	}

	public String decorate(Object columnValue, String tag) {
		return decorate(columnValue);
	}

	public String contentToSearching(Object columnValue) {
		return null;
	}
}
