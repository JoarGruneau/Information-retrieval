/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ir;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author joar
 */
public class HashSerial<K, V> extends HashMap<K, V>
        implements Serializable {

    int pointer;

    public HashSerial() {
        super();
        this.pointer = 0;
    }

    public void merge(HashSerial other) {
        for (Object token : other.keySet()) {
            if (this.containsKey(token)) {
                ((PostingsList) get(token)).merge((PostingsList) other.get(token));
            } else {
                put((K) token, (V) other.get(token));
            }
        }
    }

    public void serialize(String outFile) {
        try {
            FileOutputStream fos = new FileOutputStream(
                    outFile, false);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.close();
            fos.close();
        } catch (Exception e) {
            Logger.getLogger(
                    HashSerial.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public static HashSerial deSerialize(String inFile) {
        try {
            FileInputStream fis = new FileInputStream(inFile);
            ObjectInputStream ois = new ObjectInputStream(fis);
            HashSerial hashMap = (HashSerial) ois.readObject();
            return hashMap;
        } catch (Exception e) {
            Logger.getLogger(
                    HashSerial.class.getName()).log(Level.SEVERE, null, e);
            return new HashSerial();
        }
    }

}
