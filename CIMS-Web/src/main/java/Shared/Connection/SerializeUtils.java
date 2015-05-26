/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Shared.Connection;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;

/**
 *
 * @author Kargathia
 */
public class SerializeUtils {

    public static byte[] serialize(Object obj) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream os = new ObjectOutputStream(out)) {
            os.writeObject(obj);
            return out.toByteArray();
        } catch (IOException ex) {
            System.out.println("Unable to serialize due to IOException: " + ex.getMessage());
            ex.printStackTrace();
        }
        // only on caught exception
        return null;
    }

    public static Object deserialize(byte[] data) {
        try (ByteArrayInputStream in = new ByteArrayInputStream(data);
                ObjectInputStream is = new ObjectInputStream(in)) {
            return is.readObject();
        } catch (IOException ex) {
            System.out.println("Unable to deserialize due to IOException: " + ex.getMessage());
            ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            System.out.println("Class not found: " + ex.getMessage());
            ex.printStackTrace();
        }
        // only on caught exception
        return null;
    }

    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
