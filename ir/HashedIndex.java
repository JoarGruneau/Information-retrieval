/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */
package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /**
     * The index as a hashtable.
     */
    private HashMap<String, PostingsList> index
            = new HashMap<>();
    private HashSerial diskNames = new HashSerial();
    private String path = "/home/joar/Documents/lab/postingsLists/";
    private int lastDocID;
    private int pointer = 0;
    private int n;

    /**
     * Inserts this token in the index.
     */
    @Override
    public void insert(String token, int docID, int offset) {
        if (docID % 6000 == 0 && docID != lastDocID) {
            System.out.println("Saving large lists to disk....");
            saveLargeLists();
        }
        if (Runtime.getRuntime().freeMemory() < 10000000 && docID != lastDocID) {
            saveAll();
        }
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

    /**
     * Returns all the words in the index.
     *
     * @return
     */
    @Override
    public Iterator<String> getDictionary() {
        Iterator iterator = index.keySet().iterator();
        return iterator;
    }

    /**
     * Returns the postings for a specific term, or null if the term is not in
     * the index.
     */
    @Override
    public PostingsList getPostings(String token) {
        if (index.containsKey(token)) {
            return index.get(token);
        } else {
            return new PostingsList();
        }
    }

    public void loadDiskNames() {
        diskNames = HashSerial.deSerialize(Index.PATH + "dictionary");
    }

    @Override
    public void saveDiskNames() {
        diskNames.serialize(Index.PATH + "dictionary");
        diskNames.clear();
    }

    @Override
    public void saveLargeLists() {
        boolean saved = false;
        ArrayList<String> removeList = new ArrayList();
        Iterator tokens = index.keySet().iterator();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            if (index.get(token).size() > 50) {
                saved = true;
                if (diskNames.containsKey(token)) {
                    PostingsList postingsList = PostingsList.deSerialize(path + diskNames.get(token));
                    postingsList.merge(getPostings(token));
                    postingsList.serialize(path
                            + diskNames.get(token));
                } else {
                    diskNames.put(token, pointer);
                    getPostings(token).serialize(
                            path + pointer);
                    pointer++;
                }
                removeList.add(token);
            }
        }
        if (!saved) {
            saveAll();
        } else {
            removeList.stream().forEach((token) -> {
                index.remove(token);
            });
        }
    }

    @Override
    public void saveAll() {
        System.out.println("Saving to disk....");
        Iterator tokens = index.keySet().iterator();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            if (diskNames.containsKey(token)) {
                PostingsList postingsList
                        = PostingsList.deSerialize(path + diskNames.get(token));
                postingsList.merge(getPostings(token));
                postingsList.serialize(path + diskNames.get(token));
            } else {
                diskNames.put(token, pointer);
                getPostings(token).serialize(
                        path + pointer);
                pointer++;
            }
        }
        index.clear();
    }

    public PostingsList intersect(PostingsList p1, PostingsList p2) {
        PostingsList result = new PostingsList();
        int p1Index = 0;
        int p2Index = 0;

        while (p1Index < p1.size() && p2Index < p2.size()) {
            if (p1.get(p1Index).docID == p2.get(p2Index).docID) {
                result.add(p2.get(p2Index).clone());
                p1Index++;
                p2Index++;
            } else if (p1.get(p1Index).docID < p2.get(p2Index).docID) {
                p1Index++;
            } else {
                p2Index++;
            }
        }
        return result;
    }

    public PostingsList positionalIntersect(PostingsList p1,
            PostingsList p2, int k) {
        boolean firstFound;
        PostingsList result = new PostingsList();
        int p1Index = 0;
        int p2Index = 0;

        while (p1Index < p1.size() && p2Index < p2.size()) {
            firstFound = true;
            if (p1.get(p1Index).docID == p2.get(p2Index).docID) {
                for (int position1 : p1.get(p1Index).possitions) {
                    for (int position2 : p2.get(p2Index).possitions) {
                        if (position2 - position1 == k) {
                            if (firstFound) {
                                result.add(new PostingsEntry(
                                        p1.get(p1Index).docID, position1));
                                firstFound = false;
                            } else {
                                result.getLast().possitions.add(position1);
                            }
                        } else if (position2 - position1 > k) {
                            break;
                        }
                    }
                }
                p1Index++;
                p2Index++;
            } else if (p1.get(p1Index).docID < p2.get(p2Index).docID) {
                p1Index++;
            } else {
                p2Index++;
            }
        }
        return result;
    }

    public PostingsList unionQuery(Query query) {
        PostingsList result = new PostingsList();
        HashMap<Integer, PostingsEntry> union = new HashMap<>();
        PostingsList postingsList;
        for (int i = 0; i < query.terms.size(); i++) {

            if (!diskNames.containsKey(query.terms.get(i))) {
                postingsList = new PostingsList();
            } else {
                postingsList = PostingsList.deSerialize(path
                        + diskNames.get(query.terms.get(i)));
            }

            for (PostingsEntry entry : postingsList.list) {
                if (!union.containsKey(entry.docID)) {
                    union.put(entry.docID, entry);
                }
            }
        }

        for (int docID : union.keySet()) {
            result.add(union.get(docID));
        }
        return result;
    }

    public PostingsList cosineScore(Query query) {
        PostingsList result = new PostingsList();
        PostingsList postingsList;
        HashMap<Integer, PostingsEntry> scores = new HashMap<>();
        for (int i = 0; i < query.terms.size(); i++) {
            if (!diskNames.containsKey(query.terms.get(i))) {
                postingsList = new PostingsList();
            } else {
                postingsList = PostingsList.deSerialize(path
                        + diskNames.get(query.terms.get(i)));
            }
            postingsList.calcScore();
            for (PostingsEntry entry : postingsList.list) {
                if (scores.containsKey(entry.docID)) {
                    PostingsEntry tmpEntry = scores.get(entry.docID);
                    tmpEntry.score += entry.score;
                } else {
                    scores.put(entry.docID, entry);
                }
            }
        }

        for (int docID : scores.keySet()) {
            result.add(scores.get(docID));
        }
        result.divideScore();
        result.sort();
        return result;
    }

    public PostingsList pageRankScore(PostingsList postingsList, double c) {
        HashSerial scores = HashSerial.deSerialize("pageRank/pageRank");
        for (PostingsEntry entry : postingsList.list) {
            if (scores.containsKey(Indexer.docIDs.get("" + entry.docID))) {
                entry.score = entry.score / c + c * (double) scores.get(
                        Indexer.docIDs.get("" + entry.docID));
            }
        }
        postingsList.sort();
        return postingsList;
    }

    /**
     * Searches the index for postings matching the query.
     */
    @Override
    public PostingsList search(Query query, int queryType, int rankingType,
            int structureType) {

        if (diskNames.isEmpty()) {
            loadDiskNames();
        }

        if (query.terms.isEmpty() || !diskNames.containsKey(query.terms.get(0))) {
            return new PostingsList();
        }

        PostingsList result = new PostingsList();

        if (queryType == Index.INTERSECTION_QUERY) {
            result = PostingsList.deSerialize(path
                    + diskNames.get(query.terms.get(0)));

            for (int i = 1; i < query.terms.size(); i++) {

                if (!diskNames.containsKey(query.terms.get(i))) {
                    return new PostingsList();
                }
                result = intersect(result,
                        PostingsList.deSerialize(path
                                + diskNames.get(query.terms.get(i))));
            }
        } else if (queryType == Index.PHRASE_QUERY) {
            result = PostingsList.deSerialize(path
                    + diskNames.get(query.terms.get(0)));

            for (int i = 1; i < query.terms.size(); i++) {

                if (!diskNames.containsKey(query.terms.get(i))) {
                    return new PostingsList();
                }
                result = positionalIntersect(result,
                        PostingsList.deSerialize(path
                                + diskNames.get(query.terms.get(i))), i);
            }
        } else if (queryType == Index.RANKED_QUERY) {

            if (rankingType == Index.TF_IDF) {
                result = cosineScore(query);
            } else if (rankingType == Index.PAGERANK) {
                result = unionQuery(query);
                result = pageRankScore(result, 1);
            } else {
                result = cosineScore(query);
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
}
