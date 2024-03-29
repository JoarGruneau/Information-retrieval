/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Hedvig Kjellström, 2012
 */
package ir;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.StringTokenizer;

public class Query {

    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();
    final double alpha = 1;
    final double beta = 0.8;

    /**
     * Creates a new empty Query
     */
    public Query() {
    }

    /**
     * Creates a new Query from a string of words
     */
    public Query(String queryString) {
        StringTokenizer tok = new StringTokenizer(queryString);
        while (tok.hasMoreTokens()) {
            terms.add(tok.nextToken());
            weights.add(new Double(1));
        }
    }

    /**
     * Returns the number of terms
     */
    public int size() {
        return terms.size();
    }

    /**
     * Returns a shallow copy of the Query
     */
    public Query copy() {
        Query queryCopy = new Query();
        queryCopy.terms = (LinkedList<String>) terms.clone();
        queryCopy.weights = (LinkedList<Double>) weights.clone();
        return queryCopy;
    }

    public void updateTerms(HashMap<String, Double> newTerms) {
        terms = new LinkedList();
        weights = new LinkedList();

        for (String term : newTerms.keySet()) {
            terms.add(term);
            weights.add(newTerms.get(term));
            //System.out.println(term + " weight: " + newTerms.get(term));
        }
    }

    public int getDr(boolean[] relevant) {
        int dr = 0;
        for (boolean bol : relevant) {
            if (bol) {
                dr++;
            }
        }
        return dr;
    }

    public double length(Query query) {
        double length = 0;
        for (double weight : query.weights) {
            length += Math.pow(weight, 2);
        }
        length = Math.sqrt(length);
        return length;
    }

    public double lengthDoc(HashMap<String, Integer> doc) {
        double length = 0;
        for (String term : doc.keySet()) {
            length += Math.pow(doc.get(term), 2);
        }
        length = Math.sqrt(length);
        return length;
    }

    /**
     * Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback(PostingsList results,
            boolean[] docIsRelevant, Indexer indexer) {
        HashMap<String, Double> hashTerms = new HashMap<>();
        HashMap<String, Integer> tmpTerms;
        int nRelevant = getDr(docIsRelevant);
        double normWeight;
        for (int i = 0; i < docIsRelevant.length; i++) {

            if (docIsRelevant[i]) {

                String filePath = "davisWiki/"
                        + indexer.docIDs.get("" + results.get(i).docID);
                tmpTerms = indexer.getTerms(new File(filePath));
                double length = lengthDoc(tmpTerms);
                for (String term : tmpTerms.keySet()) {

                    normWeight = (beta * tmpTerms.get(term))
                            / (length * nRelevant);

                    if (!hashTerms.containsKey(term)) {
                        hashTerms.put(term, normWeight);
                    } else {
                        hashTerms.put(term, hashTerms.get(term) + normWeight);
                    }
                }
            }
        }
        double length = length(this);
        for (int i = 0; i < terms.size(); i++) {
            normWeight = alpha * weights.get(i) / length;
            if (!hashTerms.containsKey(terms.get(i))) {
                hashTerms.put(terms.get(i), normWeight);
            } else {
                hashTerms.put(terms.get(i), hashTerms.get(terms.get(i))
                        + normWeight);
            }
        }
        updateTerms(hashTerms);

    }
}
