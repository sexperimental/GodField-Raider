package utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReaderUtils {

    public static String extractRoomId(String responseBody) {
        String key = "\"roomId\":\"";
        int start = responseBody.indexOf(key);
        if (start != -1) {
            start += key.length();
            int end = responseBody.indexOf("\"", start);
            if (end != -1) {
                return responseBody.substring(start, end);
            }
        }
        return null;
    }

    public static List<String> readTokensFromFile(File file) {
        List<String> tokens = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                tokens.add(line.trim());
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return tokens;
    }
}
