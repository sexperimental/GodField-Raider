package utils.tokengen;

import org.json.JSONObject;
import utils.StringUtils;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TokenGen {

    private static final String API_KEY = "AIzaSyCBvMvZkHymK04BfEaERtbmELhyL8-mtAg";
    private static final String URL = "https://www.googleapis.com/identitytoolkit/v3/relyingparty/signupNewUser?key=" + API_KEY;

    public static List<String> generateTokens(int numTokens) {
        List<String> generatedTokens = new ArrayList<>();
        try {
            for (int i = 0; i < numTokens; i++) {
                String userName = StringUtils.stringRandomizer(5);
                String jsonInputString = "{\"UserName\":\"" + userName + "\"}";

                HttpURLConnection connection = (HttpURLConnection) new URL(URL).openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonInputString.getBytes("UTF-8"));
                connection.getOutputStream().flush();
                connection.getOutputStream().close();

                if (connection.getResponseCode() == 200) {
                    String response = new Scanner(connection.getInputStream()).useDelimiter("\\A").next();
                    JSONObject jsonResponse = new JSONObject(response);
                    String idToken = jsonResponse.getString("idToken");
                    generatedTokens.add(idToken);
                } else {
                    System.out.println("Error: " + connection.getResponseCode());
                    System.out.println(new Scanner(connection.getErrorStream()).useDelimiter("\\A").next());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println( generatedTokens.size() + " Tokens generated.");
        return generatedTokens;
    }
}

