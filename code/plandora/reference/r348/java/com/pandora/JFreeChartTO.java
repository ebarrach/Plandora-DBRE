package com.pandora;

import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.general.Dataset;

public class JFreeChartTO extends TransferObject {

	private static final long	serialVersionUID	= 1L;
	
	private String title;
	
	private String categoryLabel;
	
	private String axisLabel;
	
	private PlotOrientation orientation = PlotOrientation.VERTICAL;
	
	private Dataset categoryDataset;
	
	private boolean showLegend = true;
	
	private boolean rotateCategoryLabel = false;

	
	///////////////////////////////////////////////
	public String getTitle() {
		return title;
	}
	public void setTitle(String newValue) {
		this.title = newValue;
	}

	
	///////////////////////////////////////////////
	public String getCategoryLabel() {
		return categoryLabel;
	}
	public void setCategoryLabel(String newValue) {
		this.categoryLabel = newValue;
	}

	
	///////////////////////////////////////////////
	public String getAxisLabel() {
		return axisLabel ;
	}
	public void setAxisLabel(String newValue) {
		this.axisLabel = newValue;
	}

	
	///////////////////////////////////////////////
	public PlotOrientation getOrientation() {
		return orientation;
	}
	public void setOrientation(PlotOrientation newValue) {
		this.orientation = newValue;
	}

	
	///////////////////////////////////////////////
	public Dataset getCategoryDataset() {
		return categoryDataset;
	}
	public void setCategoryDataset(Dataset newValue) {
		this.categoryDataset = newValue;
	}
	
	
	///////////////////////////////////////////////
	public boolean isShowLegend() {
		return showLegend;
	}
	public void setShowLegend(boolean newValue) {
		this.showLegend = newValue;
	}
	
	
	///////////////////////////////////////////////
	public boolean isRotateCategoryLabel() {
		return rotateCategoryLabel;
	}
	public void setRotateCategoryLabel(boolean newValue) {
		this.rotateCategoryLabel = newValue;
	}
	
	
}
