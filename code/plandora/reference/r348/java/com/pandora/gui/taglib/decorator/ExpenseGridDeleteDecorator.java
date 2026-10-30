package com.pandora.gui.taglib.decorator;

import java.util.Iterator;
import java.util.Vector;

import org.apache.taglibs.display.ColumnDecorator;

import com.pandora.CostInstallmentTO;
import com.pandora.CostStatusTO;
import com.pandora.CostTO;
import com.pandora.ExpenseTO;
import com.pandora.helper.HtmlUtil;

public class ExpenseGridDeleteDecorator extends ColumnDecorator {
	 /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object)
     */
    public String decorate(Object columnValue) {
        return decorate(columnValue, "remove");
    }

    /* (non-Javadoc)
     * @see org.apache.taglibs.display.ColumnDecorator#decorate(java.lang.Object, java.lang.String)
     */
    public String decorate(Object columnValue, String tag) {
    	final ExpenseTO eto = (ExpenseTO)this.getObject();
    	String image = "";
    	
    	final Vector<CostTO> costList = eto.getExpensesItems();
		final Iterator<CostTO> i = costList.iterator();
		while(i.hasNext()) {
			final CostTO cost = i.next();
			final Vector<CostInstallmentTO> installmentsList = cost.getInstallments();
			final Iterator<CostInstallmentTO> it = installmentsList.iterator();
			while(it.hasNext()) {
				final CostInstallmentTO installments = it.next();
				//final CostStatusTO csto = installments.getCostStatus();
				//final int status = csto.getStateMachineOrder().intValue();
				if(installments.getCostStatus().getStateMachineOrder().intValue() == CostStatusTO.STATE_MACHINE_PAID.intValue()){
					return "&nbsp;";
				}
			}
		}
		
		final String altValue = this.getBundleMessage("label.grid.remove");
		image ="<a href=\"javascript:remove('" + columnValue + "'," + tag + ");\" border=\"0\"> \n";
		image += "<img border=\"0\" " + HtmlUtil.getHint(altValue) + " src=\"../images/remove.gif\" />\n";
		image += "</a>\n"; 				
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
