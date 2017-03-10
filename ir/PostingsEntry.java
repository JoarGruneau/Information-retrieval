/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */
package ir;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {

    public int docID;
    public double score = 0;
    public ArrayList<Integer> possitions = null;
    private int idfCount = 0;

    public PostingsEntry(int docID) {
        this.docID = docID;
    }

    public PostingsEntry(int docID, int offset) {
        this.docID = docID;
        possitions = new ArrayList();
        possitions.add(offset);
    }

    public PostingsEntry(int docID, int offset, double score) {
        this(docID, offset);
        this.score = score;
    }

    public void addPossition(int offset) {
        possitions.add(offset);
    }

    public void addDFCount() {
        idfCount++;
    }

    @Override
    public PostingsEntry clone() {
        PostingsEntry clone = new PostingsEntry(docID,
                possitions.get(0), score);
        for (int i = 1; i < possitions.size(); i++) {
            clone.addPossition(possitions.get(i));
        }
        return clone;
    }

    public boolean sameDoc(PostingsEntry other) {
        if (other.docID == this.docID) {
            return true;
        }
        return false;
    }

    public int getIDF() {
        if (possitions == null) {
            return idfCount;
        } else {
            return possitions.size();
        }
    }

//    public void serialize(ObjectOutputStream objectOutputStream)
//            throws IOException {
//        objectOutputStream.writeObject(this);
//    }
    /**
     * PostingsEntries are compared by their score (only relevant in ranked
     * retrieval).
     *
     * The comparison is defined so that entries will be put in descending
     * order.
     *
     * @param other
     */
    @Override
    public int compareTo(PostingsEntry other) {
        return (int) Math.signum(other.score - this.score);
    }

    //
    //  YOUR CODE HERE
    //
}
