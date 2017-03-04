/*
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 *
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */
package ir;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.util.LinkedList;
import java.io.Serializable;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A list of postings for a given word.
 */
public class PostingsList implements Serializable {

    protected LinkedList<PostingsEntry> list;

    public PostingsList() {
        list = new LinkedList<PostingsEntry>();
    }

    /**
     * Number of postings in this list
     */
    public int size() {
        return list.size();
    }

    public void divideScore() {
        for (int i = 0; i < list.size(); i++) {
            PostingsEntry entry = get(i);
            entry.score = entry.score
                    / ((double) Indexer.docLengths.get(entry.docID + ""));
        }
    }

    public void calcScore() {
        for (int i = 0; i < list.size(); i++) {
            PostingsEntry entry = get(i);
            entry.score = entry.possitions.size()
                    * Math.log10((double) (Indexer.docIDs.size()) / list.size());
        }
    }

    /**
     * Returns the ith posting
     */
    public PostingsEntry get(int i) {
        return list.get(i);
    }

    public PostingsEntry getLast() {
        return list.getLast();
    }

    public int index(PostingsEntry postingsEntry) {
        int index = -1;
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).sameDoc(postingsEntry)) {
                index = i;
                break;
            }
        }
        return index;
    }

    public void add(PostingsEntry postingsEntry) {
        list.add(postingsEntry);
    }

    public void merge(PostingsList postingsList) {
        list.addAll(postingsList.list);
    }

    public void sort() {
        Collections.sort(list);
    }

    public PostingsList clone() {
        PostingsList clone = new PostingsList();
        for (PostingsEntry entry : list) {
            clone.add(entry.clone());
        }
        return clone;
    }

    public void serialize(String outFile) {
        try {
            FileOutputStream fos = new FileOutputStream(outFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
        } catch (Exception e) {
            Logger.getLogger(
                    PostingsList.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static PostingsList deSerialize(String inFile) {
        try {
            FileInputStream fis = new FileInputStream(inFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            PostingsList returnList = (PostingsList) ois.readObject();
            return returnList;
        } catch (Exception e) {
            Logger.getLogger(
                    PostingsList.class.getName()).log(Level.SEVERE, null, e);
            return new PostingsList();
        }
    }

}
