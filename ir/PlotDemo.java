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
        double[] x = {3.0, 10.0};
        double[] y11 = {1.0, 0.8};
        double[] y12 = {0.15, 0.4};
        new Plot("Bigram query: zombie attack", x, y11, y12);

        double[] y21 = {0.666, 0.6};
        double[] y22 = {0.1, 0.3};
        new Plot("Ranked query: zombie attack", x, y21, y22);

        double[] y31 = {0.0, 0.0};
        double[] y32 = {0.0, 0.0};
        new Plot("Bigram query: money transfer", x, y31, y32);

        double[] y41 = {0.0, 0.2};
        double[] y42 = {0.0, 0.1};
        new Plot("Ranked query: money transfer", x, y41, y42);

        double[] y51 = {0.666, 0.7};
        double[] y52 = {0.1, 0.35};
        new Plot("Bigram query: open source software", x, y51, y52);

        double[] y61 = {0.0, 0.2};
        double[] y62 = {0.0, 0.1};
        new Plot("Ranked query: open source software", x, y61, y62);
    }
}
