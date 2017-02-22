package ir;

/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2012
 */
import java.util.*;
import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PageRank {

    /**
     * Maximal number of documents. We're assuming here that we don't have more
     * docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     * Mapping from document names to document numbers.
     */
    Hashtable<String, Integer> docNumber = new Hashtable<String, Integer>();

    /**
     * Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**
     * A memory-efficient representation of the transition matrix. The outlinks
     * are represented as a Hashtable, whose keys are the numbers of the
     * documents linked from.<p>
     *
     * The value corresponding to key i is a Hashtable whose keys are all the
     * numbers of documents j that i links to.<p>
     *
     * If there are no outlinks from i, then the value corresponding key i is
     * null.
     */
    HashMap<Integer, Hashtable<Integer, Boolean>> link = new HashMap<>();

    /**
     * The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     * The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     * The probability that the surfer will be bored, stop following links, and
     * take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     * Convergence criterion: Transition probabilities do not change more that
     * EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     * Never do more than this number of iterations regardless of whether the
     * transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    /* --------------------------------------------- */
    public PageRank(String filename) {
        int noOfDocs = readDocs(filename);
        computePageRank(noOfDocs);
    }


    /* --------------------------------------------- */
    /**
     * Reads the documents and creates the docs table. When this method finishes
     * executing then the @code{out} vector of outlinks is initialised for each
     * doc, and the @code{p} matrix is filled with zeroes (that indicate direct
     * links) and NO_LINK (if there is no direct link.
     * <p>
     *
     * @return the number of documents read.
     */
    int readDocs(String filename) {
        int fileIndex = 0;
        try {
            System.err.print("Reading file... ");
            BufferedReader in = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = in.readLine()) != null && fileIndex < MAX_NUMBER_OF_DOCS) {
                int index = line.indexOf(";");
                String title = line.substring(0, index);
                Integer fromdoc = docNumber.get(title);
                //  Have we seen this document before?
                if (fromdoc == null) {
                    // This is a previously unseen doc, so add it to the table.
                    fromdoc = fileIndex++;
                    docNumber.put(title, fromdoc);
                    docName[fromdoc] = title;
                }
                // Check all outlinks.
                StringTokenizer tok = new StringTokenizer(line.substring(index + 1), ",");
                while (tok.hasMoreTokens() && fileIndex < MAX_NUMBER_OF_DOCS) {
                    String otherTitle = tok.nextToken();
                    Integer otherDoc = docNumber.get(otherTitle);
                    if (otherDoc == null) {
                        // This is a previousy unseen doc, so add it to the table.
                        otherDoc = fileIndex++;
                        docNumber.put(otherTitle, otherDoc);
                        docName[otherDoc] = otherTitle;
                    }
                    // Set the probability to 0 for now, to indicate that there is
                    // a link from fromdoc to otherDoc.
                    if (link.get(fromdoc) == null) {
                        link.put(fromdoc, new Hashtable<Integer, Boolean>());
                    }
                    if (link.get(fromdoc).get(otherDoc) == null) {
                        link.get(fromdoc).put(otherDoc, true);
                        out[fromdoc]++;
                    }
                }
            }
            if (fileIndex >= MAX_NUMBER_OF_DOCS) {
                System.err.print("stopped reading since documents table is full. ");
            } else {
                System.err.print("done. ");
            }
            // Compute the number of sinks.
            for (int i = 0; i < fileIndex; i++) {
                if (out[i] == 0) {
                    numberOfSinks++;
                }
            }
        } catch (FileNotFoundException e) {
            System.err.println("File " + filename + " not found!");
        } catch (IOException e) {
            System.err.println("Error reading file " + filename);
        }
        System.err.println("Read " + fileIndex + " number of documents");
        return fileIndex;
    }


    /* --------------------------------------------- */
 /*
     *   Computes the pagerank of each document.
     */
    void computePageRank(int numberOfDocs) {
        double[][] list1 = new double[1][numberOfDocs];
        double[][] list2 = new double[1][numberOfDocs];
        list2[0][0] = 1;
        Matrix xLast = new Matrix(list1);
        Matrix xNew = new Matrix(list2);
        double newPageProb = BORED / numberOfDocs;
        double[][] listG = new double[numberOfDocs][numberOfDocs];

        for (int i = 0; i < numberOfDocs; i++) {
            if (link.containsKey(i)) {
                for (int j = 0; j < numberOfDocs; j++) {
                    if (link.get(i).containsKey(j) && link.get(i).get(j) == true) {
                        listG[i][j] = (1 - BORED) / out[i] + newPageProb;
                    }
                }
            } else {
                for (int j = 0; j < numberOfDocs; j++) {
                    listG[i][j] = (1 - BORED) / numberOfDocs + newPageProb;
                }
            }
        }
        int iteration = 0;
        Matrix gMatrix = new Matrix(listG);
        while (xLast.absDiff(xNew) > EPSILON
                && iteration < MAX_NUMBER_OF_ITERATIONS) {
            System.out.println(xLast.absDiff(xNew));
            iteration++;
            System.out.println(iteration);
            xLast = xNew;
            xNew = xNew.multiply(gMatrix);
        }
        Doc[] docs = sortScores(xNew);
        savePageRank(docs);
    }

    Doc[] sortScores(Matrix scores) {
        Doc[] docs = new Doc[scores.columns];
        for (int i = 0; i < scores.columns; i++) {
            docs[i] = new Doc(i, scores.matrix[0][i]);
        }
        Arrays.sort(docs);
        return docs;
    }

    void savePageRank(Doc[] docs) {
        try {
            PrintWriter writer = new PrintWriter("pageRanking.txt", "UTF-8");
            for (Doc doc : docs) {
                writer.println(doc.toString());
            }
            writer.close();
        } catch (FileNotFoundException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(PageRank.class.getName()).log(Level.SEVERE, null, ex);
        }
    }


    /* --------------------------------------------- */
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Please give the name of the link file");
        } else {
            new PageRank(args[0]);
        }
    }

    class Matrix {

        protected final double[][] matrix;
        protected final int rows;
        protected final int columns;

        public Matrix(double[][] in_matrix) {
            matrix = in_matrix;
            rows = matrix.length;
            columns = matrix[0].length;
        }

        public Matrix multiply(Matrix inMatrix) {//returns Matrix*inMatrix
            double[][] out_matrix = new double[rows][inMatrix.columns];
            for (int h = 0; h < inMatrix.columns; h++) {
                for (int i = 0; i < rows; i++) {
                    double tmp_sum = 0;
                    for (int j = 0; j < columns; j++) {
                        tmp_sum = tmp_sum + inMatrix.matrix[j][h] * matrix[i][j];
                    }
                    out_matrix[i][h] = tmp_sum;
                    /*same row index as Matrix, same column index as inMatrix*/
                }
            }
            return new Matrix(out_matrix);
        }

        public void print() {
            for (double[] row : matrix) {
                System.out.println(Arrays.toString(row));
            }
        }

        public double absDiff(Matrix other) {
            if (this.rows != 1 || other.rows != 1
                    || this.columns != other.columns) {
                throw new Error("Wrong size of matrix");
            }
            double[] listResult = new double[columns];
            for (int i = 0; i < columns; i++) {
                listResult[i] = Math.pow(matrix[0][i] - other.matrix[0][i], 2);
            }
            double result = 0;
            for (double entry : listResult) {
                result += entry;
            }
            result = Math.sqrt(result);
            return result;
        }

    }

    class Doc implements Comparable<Doc> {

        protected int docID;
        protected double score;

        public Doc(int docID, double score) {
            this.docID = docID;
            this.score = score;
        }

        @Override
        public String toString() {
            return docID + ": " + score;
        }

        @Override
        public int compareTo(Doc other) {
            return (int) Math.signum(other.score - this.score);
        }

    }
}
