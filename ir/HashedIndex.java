/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */
package ir;

import java.util.HashMap;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex extends AbstractIndex implements Index {

    /**
     * The index as a hashtable.
     */
//    public static HashSerial inverseDF = new HashSerial();
    public String savePath = Index.PATH + "monoGrams/";
    private int lastDocID;
    private double treshHold = 0.8;

    /**
     * Inserts this token in the index.
     */
    @Override
    public void insert(String token, int docID, int offset) {
        if (docID % 6000 == 0 && docID != lastDocID) {
            saveAll();
//            System.out.println("Saving large mono-gram lists to disk....");
//            saveLargeLists();
        }
//        if (Runtime.getRuntime().freeMemory() < 50000000 && docID != lastDocID) {
//            saveAll();
//        }
        if (index.containsKey(token)) {
            PostingsList postingsList = index.get(token);
            if (postingsList.getLast().docID == docID) {
                postingsList.getLast().addPossition(offset);
            } else {
                PostingsEntry postingsEntry = new PostingsEntry(docID, offset);
                postingsList.add(postingsEntry);
            }
        } else {
            PostingsList postingsList = new PostingsList();
            postingsList.add(new PostingsEntry(docID, offset));
            index.put(token, postingsList);
        }
        lastDocID = docID;
    }

    public void loadDiskInfo() {
        loadDiskInfo(savePath);
    }

    public void saveDiskInfo() {
        saveDiskInfo(savePath);
    }

    public void saveLargeLists() {
        saveLargeLists(savePath);
    }

    @Override
    public void saveAll() {
        saveAll(savePath, "Saving mono-grams to disk....");
    }

    /**
     * Searches the index for postings matching the query.
     */
    @Override
    public PostingsList search(Query query, int queryType, int rankingType,
            int structureType) {

        if (diskNames.isEmpty()) {
            loadDiskInfo();
        }

        if (query.terms.isEmpty() || !diskNames.containsKey(query.terms.get(0))) {
            return new PostingsList();
        }

        PostingsList result = new PostingsList();

        if (queryType == Index.INTERSECTION_QUERY) {
            result = PostingsList.deSerialize(savePath
                    + diskNames.get(query.terms.get(0)));

            for (int i = 1; i < query.terms.size(); i++) {

                if (!diskNames.containsKey(query.terms.get(i))) {
                    return new PostingsList();
                }
                result = intersect(result,
                        PostingsList.deSerialize(savePath
                                + diskNames.get(query.terms.get(i))));
            }
        } else if (queryType == Index.PHRASE_QUERY) {
            result = PostingsList.deSerialize(savePath
                    + diskNames.get(query.terms.get(0)));

            for (int i = 1; i < query.terms.size(); i++) {

                if (!diskNames.containsKey(query.terms.get(i))) {
                    return new PostingsList();
                }
                result = positionalIntersect(result,
                        PostingsList.deSerialize(savePath
                                + diskNames.get(query.terms.get(i))), i);
            }
        } else if (queryType == Index.RANKED_QUERY) {
            query = washQuery(query);
            if (rankingType == Index.TF_IDF) {
                result = cosineScore(query, savePath);
            } else if (rankingType == Index.PAGERANK) {
                result = unionQuery(query, savePath);
                result = pageRankScore(result, 1);
            } else {
                result = cosineScore(query, savePath);
                result = pageRankScore(result, 10);
            }
        }

        return result;

    }

    /**
     * No need for cleanup in a HashedIndex.
     */
    public void cleanup() {
    }

    public Query washQuery(Query query) {
        Query washedQuery = new Query();
        for (int i = 0; i < query.terms.size(); i++) {
            if ((double) inverseDF.get(query.terms.get(i)) > treshHold) {
                washedQuery.terms.add(query.terms.get(i));
                washedQuery.weights.add(query.weights.get(i));
            } else {
                System.out.println("washed:" + query.terms.get(i)
                        + "with idf:" + inverseDF.get(query.terms.get(i)));
            }
        }
        return washedQuery;
    }
//
//    private void addCount(String token, int increment) {
//
//        if (!dfCount.containsKey(token)) {
//            dfCount.put(token, increment);
//        } else {
//            int count = dfCount.get(token);
//            count += increment;
//            dfCount.put(token, count);
//        }
//    }
//
//    private void saveIDF() {
//        HashSerial serIDF = new HashSerial();
//        for (String token : dfCount.keySet()) {
//            serIDF.put(token, Math.log10(
//                    ((double) Indexer.docIDs.size()) / dfCount.get(token)));
//        }
//        serIDF.serialize(savePath + "inverseDF");
//    }

}
