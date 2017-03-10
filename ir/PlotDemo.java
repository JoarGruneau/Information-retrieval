/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

/**
 *
 * @author joar
 */
public class PlotDemo {

    public static void main(String[] args) {
        double[] x = {10.0, 20.0, 30.0, 40.0, 50.0};
        double[] y1 = {0.3, 0.2, 0.17, 0.175, 0.18};
        double[] y2 = {0.03, 0.04, 0.05, 0.07, 0.09};
        new Plot("Precision and recal", x, y1, y2);
    }
}
