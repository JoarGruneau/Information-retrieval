/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import static ir.PageRank.MAX_NUMBER_OF_DOCS;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author joar
 */
public class ReadFile {

    String seperator;
    File file;

    public ReadFile(String file, String separator) {
        this.seperator = separator;
        this.file = new File(file);
    }

    public HashMap DocIdTranslater(String filename, String seperator)
            throws IOException {
        HashMap<Integer, String> hashMap = new HashMap<>();
        BufferedReader in = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = in.readLine()) != null) {
            int index = line.indexOf(seperator);
            String docID = line.substring(0, index);
            String name = line.substring(index + 1);
            hashMap.put(Integer.parseInt(docID), name);
        }
        return hashMap;
    }
}
