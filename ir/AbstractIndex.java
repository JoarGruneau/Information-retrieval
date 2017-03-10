/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author joar
 */
public abstract class AbstractIndex implements Index {

    private HashMap<String, Integer> dfCount = new HashMap<>();
    protected HashSerial<String, Double> inverseDF = new HashSerial<>();
    protected HashSerial diskNames = new HashSerial();
    protected HashMap<String, PostingsList> index = new HashMap();
    private int pointer = 0;

    /**
     * Returns all the words in the index.
     *
     * @return
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

    public void saveLargeLists(String savePATH) {
        boolean saved = false;
        ArrayList<String> removeList = new ArrayList();
        Iterator tokens = index.keySet().iterator();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            addCount(token, index.get(token).list.size());
            if (index.get(token).size() > 50) {
                saved = true;
                if (diskNames.containsKey(token)) {
                    PostingsList postingsList = PostingsList.deSerialize(savePATH + diskNames.get(token));
                    postingsList.merge(getPostings(token));
                    postingsList.serialize(savePATH
                            + diskNames.get(token));
                } else {
                    diskNames.put(token, pointer);
                    getPostings(token).serialize(savePATH + pointer);
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

    public void saveAll(String savePATH, String message) {
        System.out.println(message);
        Iterator tokens = index.keySet().iterator();
        while (tokens.hasNext()) {
            String token = (String) tokens.next();
            addCount(token, index.get(token).list.size());
            if (diskNames.containsKey(token)) {
                PostingsList postingsList
                        = PostingsList.deSerialize(savePATH + diskNames.get(token));
                postingsList.merge(getPostings(token));
                postingsList.serialize(savePATH + diskNames.get(token));
            } else {
                diskNames.put(token, pointer);
                getPostings(token).serialize(savePATH + pointer);
                pointer++;
            }
        }
        index.clear();
    }

    public void loadDiskInfo(String savePath) {
        diskNames = HashSerial.deSerialize(savePath + "dictionary");
        inverseDF = HashSerial.deSerialize(savePath + "inverseDF");

    }

    public void saveDiskInfo(String savePath) {
        diskNames.serialize(savePath + "dictionary");
        saveIDF(savePath);
        diskNames.clear();
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

    public PostingsList unionQuery(Query query, String savePath) {
        PostingsList result = new PostingsList();
        HashMap<Integer, PostingsEntry> union = new HashMap<>();
        PostingsList postingsList;
        for (int i = 0; i < query.terms.size(); i++) {

            if (!diskNames.containsKey(query.terms.get(i))) {
                postingsList = new PostingsList();
            } else {
                postingsList = PostingsList.deSerialize(savePath
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

    public PostingsList cosineScore(Query query, String savePath) {
        PostingsList result = new PostingsList();
        PostingsList postingsList;
        HashMap<Integer, PostingsEntry> scores = new HashMap<>();
        for (int i = 0; i < query.terms.size(); i++) {
            if (!diskNames.containsKey(query.terms.get(i))) {
                postingsList = new PostingsList();
            } else {
                postingsList = PostingsList.deSerialize(savePath
                        + diskNames.get(query.terms.get(i)));
            }
            postingsList.calcScore(inverseDF.get(query.terms.get(i)));
            for (PostingsEntry entry : postingsList.list) {
                if (scores.containsKey(entry.docID)) {
                    PostingsEntry tmpEntry = scores.get(entry.docID);
                    tmpEntry.score += query.weights.get(i) * entry.score;

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

    private void addCount(String token, int increment) {

        if (!dfCount.containsKey(token)) {
            dfCount.put(token, increment);
        } else {
            int count = dfCount.get(token);
            count += increment;
            dfCount.put(token, count);
        }
    }

    private void saveIDF(String savePath) {
        HashSerial serIDF = new HashSerial();
        for (String token : dfCount.keySet()) {
            serIDF.put(token, Math.log10(
                    ((double) Indexer.docIDs.size()) / dfCount.get(token)));
        }
        serIDF.serialize(savePath + "inverseDF");
    }

}
