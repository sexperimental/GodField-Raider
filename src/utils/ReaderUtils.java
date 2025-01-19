package utils;

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
}
