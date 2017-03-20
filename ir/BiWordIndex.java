package ir;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author joar
 */
public class BiWordIndex extends AbstractIndex implements Index {

    public final String savePath = Index.PATH + "biGrams/";
    private int lastDocID;
    private String lastToken = "";

    /**
     * Inserts this token in the index.
     */
    @Override
    public void insert(String token, int docID, int offset) {
        if (docID % 3000 == 0 && docID != lastDocID) {
            saveAll();
//            System.out.println("Saving large bi-Grams lists to disk....");
//            saveLargeLists();
        }
//        if (Runtime.getRuntime().freeMemory() < 50000000 && docID != lastDocID) {
//            saveAll();
//        }
        if (lastDocID == docID) {
            String biWord = lastToken + " " + token;

            if (index.containsKey(biWord)) {
                PostingsList postingsList = index.get(biWord);
                if (postingsList.getLast().docID == docID) {
                    postingsList.getLast().addDFCount();
                } else {
                    PostingsEntry postingsEntry = new PostingsEntry(docID);
                    postingsList.add(postingsEntry);
                }
            } else {
                PostingsList postingsList = new PostingsList();
                postingsList.add(new PostingsEntry(docID));
                index.put(biWord, postingsList);
            }
        }
        lastDocID = docID;
        lastToken = token;
    }

    public void saveLargeLists() {
        saveLargeLists(savePath, false);
    }

    @Override
    public void saveAll() {
        saveAll(savePath, "Saving bi-Grams to disk....", false);
    }

    public void loadDiskInfo() {
        loadDiskInfo(savePath, false);

    }

    public void saveDiskInfo() {
        saveDiskInfo(savePath, false);
    }

    @Override
    public PostingsList search(Query query, int queryType, int rankingType, int structureType) {
        Query biWorfQuery = getBiWords(query);
        if (diskNames.isEmpty()) {
            loadDiskInfo();
        }
        System.out.println(biWorfQuery.terms);

        if (biWorfQuery.terms.isEmpty() || !diskNames.containsKey(biWorfQuery.terms.get(0))) {
            return new PostingsList();
        }

        PostingsList result = new PostingsList();

        if (queryType == Index.INTERSECTION_QUERY) {
            result = PostingsList.deSerialize(savePath
                    + diskNames.get(biWorfQuery.terms.get(0)));

            for (int i = 1; i < biWorfQuery.terms.size(); i++) {

                if (!diskNames.containsKey(biWorfQuery.terms.get(i))) {
                    return new PostingsList();
                }
                result = intersect(result,
                        PostingsList.deSerialize(savePath
                                + diskNames.get(biWorfQuery.terms.get(i))));
            }
        } else if (queryType == Index.RANKED_QUERY) {

            if (rankingType == Index.TF_IDF) {
                result = cosineScore(biWorfQuery, savePath);
            } else if (rankingType == Index.PAGERANK) {
                result = unionQuery(biWorfQuery, savePath);
                result = pageRankScore(result, 1);
            } else {
                result = cosineScore(biWorfQuery, savePath);
                result = pageRankScore(result, 10);
            }
        }

        return result;
    }

    private Query getBiWords(Query query) {
        Query biWords = new Query();
        for (int i = 0; i < query.terms.size() - 1; i++) {
            biWords.terms.add(query.terms.get(i) + " " + query.terms.get(i + 1));
            biWords.weights.add(new Double(1));
        }
        return biWords;
    }

}
