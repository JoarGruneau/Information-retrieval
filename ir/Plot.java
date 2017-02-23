/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.ValueAxis;
import javax.swing.JFrame;
import javax.swing.JPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import static org.jfree.chart.demo.PieChartDemo1.createDemoPanel;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.function.Function2D;
import org.jfree.data.general.DatasetUtilities;
import org.jfree.data.xy.IntervalXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.ui.ApplicationFrame;
import org.jfree.ui.RectangleInsets;

/**
 *
 * @author joar
 */
public class Plot extends ApplicationFrame {

    public Plot(String title, double[] x, double[] y1, double[] y2) {
        super(title);
        final XYSeries high = new XYSeries("Square error top 30");
        for (int i = 0; i < x.length; i++) {
            high.add(x[i], y1[i]);
        }
        final XYSeriesCollection dataHigh = new XYSeriesCollection(high);

        final XYSeries low = new XYSeries("Square error last 30");
        for (int i = 0; i < x.length; i++) {
            low.add(x[i], y2[i]);
        }
        final XYSeriesCollection dataLow = new XYSeriesCollection(low);

        final JFreeChart chart = ChartFactory.createXYLineChart(
                "Square error",
                "X/numberOfDocs",
                "Sum of square error",
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
//
//        final XYSeries series = new XYSeries("Random Data");
//        series.add(1.0, 500.2);
//        series.add(5.0, 694.1);
//        series.add(4.0, 100.0);
//        series.add(12.5, 734.4);
//        series.add(17.3, 453.2);
//        series.add(21.2, 500.2);
//        series.add(21.9, null);
//        series.add(25.6, 734.4);
//        series.add(30.0, 453.2);
//        final XYSeriesCollection data = new XYSeriesCollection(series);
//        final JFreeChart chart = ChartFactory.createXYLineChart(
//                "XY Series Demo",
//                "X",
//                "Y",
//                data,
//                PlotOrientation.VERTICAL,
//                true,
//                true,
//                false
//        );
//
//        final XYSeries ser = new XYSeries("Random Data 2");
//        ser.add(5.0, 50.2);
//        ser.add(5.0, 64.1);
//        ser.add(4.0, 100.0);
//        ser.add(12.5, 74.4);
//        final XYSeriesCollection dat2 = new XYSeriesCollection(ser);
//        chart.getXYPlot().setDataset(1, dat2);
//        XYPlot plot = (XYPlot) chart.getPlot();
//        XYLineAndShapeRenderer renderer0 = new XYLineAndShapeRenderer();
//        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer();
//        plot.setRenderer(0, renderer0);
//        plot.setRenderer(1, renderer1);
//        plot.getRendererForDataset(plot.getDataset(0)).setSeriesPaint(0, Color.red);
//        plot.getRendererForDataset(plot.getDataset(1)).setSeriesPaint(0, Color.blue);
//        final ChartPanel chartPanel = new ChartPanel(chart);
//        chartPanel.setPreferredSize(new java.awt.Dimension(600, 500));
//        setContentPane(chartPanel);

    }
}
