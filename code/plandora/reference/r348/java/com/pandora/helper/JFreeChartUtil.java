package com.pandora.helper;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.List;

import javax.imageio.ImageIO;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.StandardPieSectionLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PieLabelLinkStyle;
import org.jfree.chart.plot.RingPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.chart.renderer.category.GroupedStackedBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.title.LegendTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.PieDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.util.ShapeUtilities;

import com.pandora.JFreeChartTO;
import com.pandora.gui.struts.action.projectpanel.SparselyLabeledCategoryAxis;


public class JFreeChartUtil {


	
	private static final String[] barColors = new String[]{"000040", "8080FF", "0080FF", "8000FF", "80FF80", "800040", "800000",  "FF0000", "FF8C37", "FFDC39", "FFFF00", "BEF01E", "5AFF28", "008000", "00FF00", "808000", "808040", "808080", "C0C0C0", "00FFFF", "000040", "8080FF", "0080FF", "8000FF", "80FF80", "800040", "800000",  "FF0000", "FF8C37", "FFDC39", "FFFF00", "BEF01E", "5AFF28", "008000", "00FF00", "808000", "808040", "808080", "C0C0C0", "00FFFF", "000040", "8080FF", "0080FF", "8000FF", "80FF80", "800040", "800000",  "FF0000", "FF8C37", "FFDC39", "FFFF00", "BEF01E", "5AFF28", "008000", "00FF00", "808000", "808040", "808080", "C0C0C0", "00FFFF"};
	

	/**
	 * The constructor was set to private to avoid instancing  
	 */
	private JFreeChartUtil(){}

	
	public static JFreeChart getScatterPlot(JFreeChartTO cto){
		Font font = new Font("Tahoma", Font.PLAIN, 9);
		JFreeChart chart = null;
		
		chart = ChartFactory.createScatterPlot(cto.getTitle(), cto.getCategoryLabel(),
				 cto.getAxisLabel(),((XYDataset)cto.getCategoryDataset()),
		         cto.getOrientation(), cto.isShowLegend(),
		         false,// tooltips
		         false // urls
		     );		

		chart.setTitle(
			    new org.jfree.chart.title.TextTitle(cto.getTitle(), new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12))
			);
		chart.setBackgroundPaint(new Color(Integer.parseInt("EFEFEF", 16)));
		if (cto.isShowLegend()) {
			LegendTitle legend = (LegendTitle) chart.getLegend();
			legend.setItemFont(font);			
		}

		
		XYPlot plot = (XYPlot) chart.getXYPlot();
		plot.setBackgroundPaint(new GradientPaint(0, 0, Color.gray, 200,200, new Color(Integer.parseInt("efefef", 16)), false));
		plot.setBackgroundAlpha(0.5f);
		plot.setNoDataMessageFont(font);
		
		ValueAxis yaxis = plot.getRangeAxis();
        yaxis.setTickLabelFont(font);
        yaxis.setLabelFont(font);
		
        ValueAxis xaxis = plot.getDomainAxis();  
        xaxis.setTickLabelFont(font);
        xaxis.setLabelFont(font);
 
        Shape shape = ShapeUtilities.createDiamond(1);
        XYItemRenderer renderer = plot.getRenderer();
        renderer.setSeriesShape(0, shape);
        
		return chart;
	}
	
	
	public static JFreeChart getStackbar(JFreeChartTO cto, boolean isAreaChart){
		
		Font font = new Font("Tahoma", Font.PLAIN, 9);
		JFreeChart chart = null;
		
		if (isAreaChart) {
			chart = ChartFactory.createAreaChart(cto.getTitle(), cto.getCategoryLabel(),
					 cto.getAxisLabel(),((CategoryDataset)cto.getCategoryDataset()),
			         cto.getOrientation(), cto.isShowLegend(),
			         false,// tooltips
			         false // urls
			     );						
		} else {
			chart = ChartFactory.createStackedBarChart(cto.getTitle(), cto.getCategoryLabel(),
					 cto.getAxisLabel(),((CategoryDataset)cto.getCategoryDataset()),
			         cto.getOrientation(), cto.isShowLegend(),
			         false,// tooltips
			         false // urls
			     );			
		}
		 
		chart.setTitle(
		    new org.jfree.chart.title.TextTitle(cto.getTitle(), new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12))
		);

		chart.setBackgroundPaint(new Color(Integer.parseInt("EFEFEF", 16)));
		
		CategoryPlot plot = (CategoryPlot) chart.getPlot();	     
		plot.setBackgroundPaint(new GradientPaint(0, 0, Color.gray, 200,200, new Color(Integer.parseInt("efefef", 16)), false));
		plot.setBackgroundAlpha(0.5f);
		plot.setNoDataMessageFont(font);
		 
		CategoryAxis axis = (CategoryAxis) plot.getDomainAxis();
		axis.setLabelFont(font);
		axis.setTickLabelFont(font);
		axis.setCategoryMargin(0);
		axis.setLowerMargin(0);
		axis.setUpperMargin(0);
		
		if (cto.isRotateCategoryLabel()) {
			axis.setCategoryLabelPositions(CategoryLabelPositions.createDownRotationLabelPositions(Math.PI / 4.0));	
		}
		 
		NumberAxis vaxis = (NumberAxis)plot.getRangeAxis();
		vaxis.setTickLabelFont(font);
		
		GroupedStackedBarRenderer render = new GroupedStackedBarRenderer();
		render.setShadowVisible(false);
		plot.setRenderer(render);
		//render.setMaximumBarWidth(.1);
		
		int items = plot.getRenderer().getLegendItems().getItemCount();
		for(int i=0; i<items; i++) {
			Paint barColor = new GradientPaint(
		               0.0f, 0.0f, new Color(Integer.parseInt(barColors[i], 16)), 0.0f, 0.0f, new Color(Integer.parseInt(barColors[i], 16))
		           );			
			render.setSeriesPaint(i, barColor);
		}
		
		if (cto.isShowLegend()) {
			LegendTitle legend = (LegendTitle) chart.getLegend();
			legend.setItemFont(font);			
		}
		
		return chart;		
	}
	
	
	public static JFreeChart getLineChart(JFreeChartTO cto, String[] customColors){
		
		Font font = new Font("Tahoma", Font.PLAIN, 9);
		JFreeChart chart = ChartFactory.createLineChart(cto.getTitle(), cto.getCategoryLabel(),
					 cto.getAxisLabel(),((CategoryDataset)cto.getCategoryDataset()),
			         cto.getOrientation(), cto.isShowLegend(),
			         false,// tooltips
			         false // urls
			     );						
		 
		chart.setTitle(
		    new org.jfree.chart.title.TextTitle(cto.getTitle(), new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12))
		);
		chart.setBackgroundPaint(new Color(Integer.parseInt("EFEFEF", 16)));
		
		CategoryPlot plot = (CategoryPlot) chart.getPlot();	     
		plot.setBackgroundPaint(new GradientPaint(0, 0, Color.gray, 200,200, new Color(Integer.parseInt("efefef", 16)), false));
		plot.setBackgroundAlpha(0.5f);
		plot.setNoDataMessageFont(font);
		
        CategoryAxis axis = new SparselyLabeledCategoryAxis(10); 
        axis.setTickMarksVisible(true);
		axis.setLabelFont(font);
		axis.setTickLabelFont(font);
		axis.setCategoryMargin(0);
		axis.setLowerMargin(0);
		axis.setUpperMargin(0);
		plot.setDomainAxis(axis);
				
		if (cto.isRotateCategoryLabel()) {
			axis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
		}

		 
		ValueAxis vaxis = (ValueAxis)plot.getRangeAxis();
		vaxis.setTickLabelFont(font);
		
		CategoryItemRenderer render = plot.getRenderer();
		int items = plot.getRenderer().getLegendItems().getItemCount();
		for(int i=0; i<items; i++) {
			Paint barColor = new Color(Integer.parseInt(barColors[i], 16));
			if (customColors!=null && customColors.length>i) {
				barColor = new Color(Integer.parseInt(customColors[i], 16));
			}
			render.setSeriesPaint(i, barColor);
		}
		
		if (cto.isShowLegend()) {
			LegendTitle legend = (LegendTitle) chart.getLegend();
			legend.setItemFont(font);			
		}
	
		return chart;		
	}
	
	
	public static JFreeChart getRingChart(JFreeChartTO cto){
		
		Font font = new Font("Tahoma", Font.PLAIN, 9);
		
		JFreeChart chart = ChartFactory.createRingChart(
				 cto.getTitle(), ((PieDataset)cto.getCategoryDataset()),cto.isShowLegend(),
		         false,// tooltips
		         false // urls
		     );
		 
		chart.setTitle(
		    new org.jfree.chart.title.TextTitle(cto.getTitle(), new java.awt.Font("Tahoma", java.awt.Font.BOLD, 12))
		);
		 
		chart.setBackgroundPaint(new Color(Integer.parseInt("EFEFEF", 16)));
		
		RingPlot plot = (RingPlot) chart.getPlot();	     
		plot.setBackgroundPaint(new GradientPaint(0, 0, Color.gray, 200,200, new Color(Integer.parseInt("efefef", 16)), false));
		plot.setBackgroundAlpha(0.5f);
		plot.setNoDataMessageFont(font);
		plot.setSectionDepth(0.5);
		plot.setLabelLinkStyle(PieLabelLinkStyle.CUBIC_CURVE);
		plot.setLabelFont(font);
		plot.setLabelBackgroundPaint(new Color(Integer.parseInt("efefef", 16)));
        plot.setLabelGenerator(new StandardPieSectionLabelGenerator(
                "{2} [{1}]", NumberFormat.getNumberInstance(), NumberFormat.getPercentInstance()
            ));
		
		List<Comparable> keys = ((PieDataset)cto.getCategoryDataset()).getKeys();
		int items = plot.getLegendItems().getItemCount();
		for(int i=0; i<items; i++) {
			plot.setSectionPaint(keys.get(i), new Color(Integer.parseInt(barColors[i], 16)));
		}
		 
		if (cto.isShowLegend()) {
			LegendTitle legend = (LegendTitle) chart.getLegend();
			legend.setItemFont(font);
		}
		
		return chart;		
	}
	

	public static byte[] chartToImg(JFreeChart chart, int width, int height){
		BufferedImage objBufferedImage = chart.createBufferedImage(width, height);
		ByteArrayOutputStream bas = new ByteArrayOutputStream();
        try {
            ImageIO.write(objBufferedImage, "png", bas);
        } catch (IOException e) {
            e.printStackTrace();
        }
		return bas.toByteArray();	
	}
}
