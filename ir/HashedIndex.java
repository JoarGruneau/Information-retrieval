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
    private HashSerial dictionary = new HashSerial();
    private String path = "/home/joar/Documents/lab/postingsLists/";
    private int lastDocID;
    private int pointer = 0;
    private int n;

    /**
     * Inserts this token in the index.
     */
    @Override
    public void insert(String token, int docID, int offset) {
//        if (docID % 6000 == 0 && docID != lastDocID) {
//            System.out.println("Saving large lists to disk....");
//            saveLargeLists();
//        }
//        if (Runtime.getRuntime().freeMemory() < 10000000 && docID != lastDocID) {
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

    /**
     * Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
        Iterator iterator = index.keySet().iterator();
        return iterator;
    }

    /**
     * Returns the postings for a specific term, or null if the term is not in
     * the index.
     */
    public PostingsList getPostings(String token) {
        if (index.containsKey(token)) {
            return index.get(token);
        } else {
            return new PostingsList();
        }
    }

    public void loadDictionary() {
        dictionary = HashSerial.deSerialize(Index.PATH + "dictionary");
    }

    @Override
    public void saveDictionary() {
        dictionary.serialize(Index.PATH + "dictionary");
        dictionary.clear();
    }

    public void calcScore() {
        for (String token : index.keySet()) {
            index.get(token).calcScore();
        }
    }

    @Override
    public void saveLargeLists() {
        boolean saved = false;
        ArrayList<String> removeList = new ArrayList();
        Iterator tokens = getDictionary();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            if (index.get(token).size() > 50) {
                saved = true;
                if (dictionary.containsKey(token)) {
                    PostingsList postingsList = PostingsList.deSerialize(
                            path + dictionary.get(token));
                    postingsList.merge(getPostings(token));
                    postingsList.serialize(path
                            + dictionary.get(token));
                } else {
                    dictionary.put(token, pointer);
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
        Iterator tokens = getDictionary();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            if (dictionary.containsKey(token)) {
                PostingsList postingsList
                        = PostingsList.deSerialize(
                                path + dictionary.get(token));
                postingsList.merge(getPostings(token));
                postingsList.serialize(path + dictionary.get(token));
            } else {
                dictionary.put(token, pointer);
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
        int postingsIndex;
        PostingsList postingsList;
        for (int i = 0; i < query.terms.size(); i++) {
            postingsList = getPostings(query.terms.get(i)).clone();
            for (PostingsEntry entry : postingsList.list) {
                postingsIndex = result.index(entry);
                if (postingsIndex == -1) {
                    result.add(entry.clone());
                }
            }
        }
        return result;
    }

    public PostingsList cosineScore(Query query) {
        PostingsList result = new PostingsList();
        PostingsList postingsList;
        HashMap<Integer, PostingsEntry> scores = new HashMap<>();
        for (int i = 0; i < query.terms.size(); i++) {
            postingsList = getPostings(query.terms.get(i)).clone();
            postingsList.calcScore();
            for (PostingsEntry entry : postingsList.list) {
                if (scores.containsKey(entry.docID)) {
                    PostingsEntry tmpEntry = scores.get(entry.docID);
                    tmpEntry.score += entry.score;
                    scores.put(tmpEntry.docID, tmpEntry);
                } else {
                    scores.put(entry.docID, entry);
                }
            }
        }
        for (int docID : scores.keySet()) {
            result.add(scores.get(docID));
        }
        result.normalizeScore();
        result.sort();
        return result;
    }

    public PostingsList pageRankScore(PostingsList postingsList) {
        HashSerial scores = HashSerial.deSerialize("pageRank/pageRank");
        for (PostingsEntry entry : postingsList.list) {
            entry.score += Double.parseDouble(
                    (String) scores.get(docIDs.get("" + entry.docID)));
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

        if (query.terms.isEmpty()) {
            return new PostingsList();
        }

        PostingsList result = getPostings(query.terms.getFirst());

        if (queryType == Index.INTERSECTION_QUERY) {
            System.out.println(queryType);

            for (int i = 1; i < query.terms.size(); i++) {
                result = intersect(result, getPostings(query.terms.get(i)));
            }
        } else if (queryType == Index.PHRASE_QUERY) {

            for (int i = 1; i < query.terms.size(); i++) {
                result = positionalIntersect(result,
                        getPostings(query.terms.get(i)), i);
            }
        } else if (queryType == Index.RANKED_QUERY) {
            if (rankingType == Index.TF_IDF) {
                result = cosineScore(query);
            } else if (rankingType == Index.PAGERANK) {
                result = unionQuery(query);
                pageRankScore(result);
            } else {

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
