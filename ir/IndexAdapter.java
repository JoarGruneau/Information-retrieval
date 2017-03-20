/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author joar
 */
public class IndexAdapter implements Index {

    HashedIndex monoGramIndex;
    BiWordIndex biWordIndex;

    public IndexAdapter() {
        monoGramIndex = new HashedIndex();
        biWordIndex = new BiWordIndex();
    }

    @Override
    public void insert(String token, int docID, int offset) {
        biWordIndex.insert(token, docID, offset);
        monoGramIndex.insert(token, docID, offset);
    }

    @Override
    public PostingsList search(
            Query query, int queryType, int rankingType, int structureType) {

        PostingsList result = new PostingsList();
        if (structureType == Index.BIGRAM) {
            result = biWordIndex.search(
                    query, queryType, rankingType, structureType);
        } else if (structureType == Index.SUBPHRASE
                && queryType == Index.RANKED_QUERY) {
            PostingsList biWordResult = biWordIndex.search(
                    query, queryType, rankingType, structureType);
            PostingsList monoGramResult = monoGramIndex.search(
                    query, queryType, rankingType, structureType);
            result = mergeResults(monoGramResult, biWordResult);
        } else if (structureType == Index.UNIGRAM) {
            result = monoGramIndex.search(
                    query, queryType, rankingType, structureType);
        }
        return result;
    }

    @Override
    public void saveAll() {
        Indexer.docIDs.serialize(Index.PATH + "docIDs");
        Indexer.docLengths.serialize(Index.PATH + "docLengths");
        monoGramIndex.saveAll();
        monoGramIndex.saveDiskInfo();
        biWordIndex.saveAll();
        biWordIndex.saveDiskInfo();
    }

    private PostingsList mergeResults(PostingsList p1, PostingsList p2) {
        HashMap<Integer, PostingsEntry> hashResult = new HashMap<>();
        PostingsList result = new PostingsList();

        for (PostingsEntry entry : p1.list) {
            hashResult.put(entry.docID, entry);
        }

        for (PostingsEntry entry : p2.list) {
            if (hashResult.containsKey(entry.docID)) {
                PostingsEntry hashEntry = hashResult.get(entry.docID);
                hashEntry.score += entry.score;
            } else {
                hashResult.put(entry.docID, entry);
            }
        }

        for (int docID : hashResult.keySet()) {
            result.add(hashResult.get(docID));
        }
        result.sort();
        return result;
    }

}
