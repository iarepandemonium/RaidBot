package net.pandette.utils;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * This is a utility class. Only public static methods should remain here that have no association with other classes.
 */
public class Utility {

    public static String readFile(String path) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, Charset.defaultCharset());
    }

    public static void writeFile(String path, String json) throws IOException {
        BufferedWriter br = new BufferedWriter(new FileWriter(path, false));
        br.write(json);
        br.close();
    }

}
