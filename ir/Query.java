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
    final double alpha = 1.0;
    final double beta = 0.75;

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

    public void updateTerms(HashMap newTerms) {
        terms = new LinkedList();
        weights = new LinkedList();

        for (Object term : newTerms.keySet()) {
            terms.add((String) term);
            weights.add((double) newTerms.get(term));
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

    /**
     * Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback(PostingsList results, boolean[] docIsRelevant, Indexer indexer) {
        HashMap<String, Double> hashTerms = new HashMap<>();
        HashMap<String, Integer> tmpTerms;
        int nRelevant = getDr(docIsRelevant);
        for (int i = 0; i < docIsRelevant.length; i++) {
            if (docIsRelevant[i]) {
                String filePath = "davisWiki/"
                        + indexer.docIDs.get("" + results.get(i).docID);
                tmpTerms = indexer.getTerms(new File(filePath));

                for (String term : tmpTerms.keySet()) {
                    if (!hashTerms.containsKey(term)) {
                        hashTerms.put(term, beta / (Indexer.docLengths.get(
                                results.get(i).docID + "")) * nRelevant);
                    } else {
                        hashTerms.put(term, beta * ((double) tmpTerms.get(term))
                                / (Indexer.docLengths.get(
                                        results.get(i).docID + "")) * nRelevant);
                    }
                }
            }
        }
        for (int i = 0; i < terms.size(); i++) {
            if (!hashTerms.containsKey(terms.get(i))) {
                hashTerms.put(terms.get(i), alpha * weights.get(i) / terms.size());
            } else {
                hashTerms.put(terms.get(i), hashTerms.get(terms.get(i))
                        + alpha * weights.get(i) / terms.size());
            }
        }
        updateTerms(hashTerms);
//        System.out.println(docIsRelevant.length);
        System.out.println(weights.toString());
        System.out.println("Terms:" + terms);
        // results contain the ranked list from the current search
        // docIsRelevant contains the users feedback on which of the 10 first hits are relevant

        //
        //  YOUR CODE HERE
        //
    }
}
