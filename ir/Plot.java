/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.awt.Color;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;

/**
 *
 * @author joar
 */
public class Plot extends ApplicationFrame {

    public Plot(String title, double[] x, double[] y1, double[] y2) {
        super(title);
        final XYSeries high = new XYSeries("Top 30");
        for (int i = 0; i < x.length; i++) {
            high.add(x[i], y1[i]);
        }
        final XYSeriesCollection dataHigh = new XYSeriesCollection(high);

        final XYSeries low = new XYSeries("Last 30");
        for (int i = 0; i < x.length; i++) {
            low.add(x[i], y2[i]);
        }
        final XYSeriesCollection dataLow = new XYSeriesCollection(low);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                title,
                "X/numberOfDocs",
                "Sum of Square Error",
                dataHigh,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        chart.getXYPlot().setDataset(1, dataLow);
        XYPlot plot = (XYPlot) chart.getPlot();
        XYLineAndShapeRenderer renderer0 = new XYLineAndShapeRenderer();
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
        plot.setRenderer(0, renderer0);
        plot.setRenderer(1, renderer1);
        plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.green);
        plot.getRendererForDataset(plot.getDataset(1)).setSeriesPaint(0, Color.blue);
        final ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new java.awt.Dimension(600, 500));
        setContentPane(chartPanel);
        pack();
        setVisible(true);

    }
}
