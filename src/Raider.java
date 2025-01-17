import com.formdev.flatlaf.FlatDarkLaf;
import utils.StringUtils;
import utils.ReaderUtils;
import utils.tokengen.TokenGen;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class Raider {

    private static final String REMOVE_USER_API = "https://asia-northeast1-godfield.cloudfunctions.net/removeRoomUser";
    private static final String CREATE_ROOM_API = "https://asia-northeast1-godfield.cloudfunctions.net/createRoom";
    private static final String ADD_ROOM_USER_API = "https://asia-northeast1-godfield.cloudfunctions.net/addRoomUser";
    private static final String SET_COMMENT_API = "https://asia-northeast1-godfield.cloudfunctions.net/setComment";

    private static String username = "GodField";
    private static String password = "None";
    private static String message = "None";
    private static String roomId = "";
    private static boolean spamPermission = false;

    private static final List<String> tokens = new ArrayList<>();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FlatDarkLaf.setup();
            JFrame frame = createMainFrame();
            initialize(frame);
        });
    }

    private static JFrame createMainFrame() {
        JFrame frame = new JFrame();
        frame.setSize(405, 255);
        frame.setLayout(null);
        frame.setTitle("GodField Raider - b1.2");
        frame.setResizable(false);
        centreWindow(frame);
        frame.setVisible(true);
        return frame;
    }

    private static void initialize(JFrame frame) {
        JLabel nameLabel = createLabel("UserName - " + username, 10, 0, 250, 25);
        JTextField nameField = createTextField(10, 27, 300, 24);
        JButton nameButton = createButton("Set", 320, 27, 60, 24, e -> {
            username = nameField.getText();
            nameLabel.setText("UserName - " + username);
        });

        JLabel passwordLabel = createLabel("PassWord - " + password, 10, 55, 250, 25);
        JTextField passwordField = createTextField(10, 80, 300, 24);
        JButton passwordButton = createButton("Set", 320, 80, 60, 24, e -> {
            password = passwordField.getText();
            passwordLabel.setText("PassWord - " + password);
        });

        JLabel messageLabel = createLabel("Message - " + message, 10, 110, 250, 25);
        JTextField messageField = createTextField(10, 135, 300, 24);
        JButton messageButton = createButton("Set", 320, 135, 60, 24, e -> {
            message = messageField.getText();
            messageLabel.setText("Message - " + message);
        });

        JButton genButton = createButton("Token Gen", 290, 180, 90, 24, e -> {
            List<String> newTokens = TokenGen.generateTokens(10);
            if (newTokens != null && !newTokens.isEmpty()) {
                tokens.addAll(newTokens);
            }
        });

        JButton joinButton = createButton("Join", 10, 180, 60, 24, e -> sendJoinRequest());
        JButton leaveButton = createButton("Leave", 80, 180, 60, 24, e -> sendLeaveRequest());

        Timer timer = new Timer(500, e -> {
            if (spamPermission) {
                sendSpamRequest();
            } else {
                ((Timer) e.getSource()).stop();
            }
        });

        JButton spamButton = createButton("Spam", 150, 180, 60, 24, e -> {
            if (tokens.isEmpty()) {
                showErrorDialog("Token is nothing. | From SendRequest");
                return;
            }
            spamPermission = true;
            if (!timer.isRunning()) {
                timer.start();
            }
        });

        JButton stopButton = createButton("Stop", 220, 180, 60, 24, e -> spamPermission = false);

        frame.add(nameLabel);
        frame.add(nameField);
        frame.add(nameButton);
        frame.add(passwordLabel);
        frame.add(passwordField);
        frame.add(passwordButton);
        frame.add(messageLabel);
        frame.add(messageField);
        frame.add(messageButton);
        frame.add(genButton);
        frame.add(joinButton);
        frame.add(leaveButton);
        frame.add(spamButton);
        frame.add(stopButton);
        frame.setVisible(true);
    }

    private static JLabel createLabel(String text, int x, int y, int width, int height) {
        JLabel label = new JLabel(text);
        label.setBounds(x, y, width, height);
        return label;
    }

    private static JTextField createTextField(int x, int y, int width, int height) {
        JTextField textField = new JTextField();
        textField.setBounds(x, y, width, height);
        return textField;
    }

    private static JButton createButton(String text, int x, int y, int width, int height, ActionListener action) {
        JButton button = new JButton(text);
        button.setBounds(x, y, width, height);
        button.addActionListener(action);
        return button;
    }

    private static void centreWindow(Window frame) {
        Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation((dimension.width - frame.getWidth()) / 2, (dimension.height - frame.getHeight()) / 2);
    }

    private static void showErrorDialog(String message) {
        JOptionPane.showMessageDialog(null, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private static void sendJoinRequest() {
        if (tokens.isEmpty()) {
            showErrorDialog("Token is nothing. | From JoinRequest");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        tokens.forEach(token -> {
            String uniqueUsername = username + "_" + StringUtils.stringRandomizer(5);
            String createRoomData = String.format("{\"mode\": \"hidden\", \"password\": \"%s\", \"userName\": \"%s\"}", password, uniqueUsername);

            try {
                HttpResponse<String> response = client.send(buildPostRequest(CREATE_ROOM_API, token, createRoomData), HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    roomId = ReaderUtils.extractRoomId(response.body());
                    if (roomId != null) {
                        String addRoomData = String.format("{\"mode\": \"hidden\", \"roomId\": \"%s\", \"userName\": \"%s\"}", roomId, uniqueUsername);
                        client.send(buildPostRequest(ADD_ROOM_USER_API, token, addRoomData), HttpResponse.BodyHandlers.ofString());
                    }
                }
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void sendLeaveRequest() {
        if (tokens.isEmpty()) {
            showErrorDialog("Token is nothing. | From LeaveRequest");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        tokens.forEach(token -> {
            String leaveRoomData = String.format("{\"mode\": \"hidden\", \"roomId\": \"%s\"}", roomId);
            try {
                client.send(buildPostRequest(REMOVE_USER_API, token, leaveRoomData), HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static void sendSpamRequest() {
        if (tokens.isEmpty()) {
            showErrorDialog("Token is nothing. | From SendRequest");
            return;
        }

        HttpClient client = HttpClient.newHttpClient();
        tokens.forEach(token -> {
            String setCommentData = String.format("{\"mode\": \"hidden\", \"roomId\": \"%s\", \"text\": \"%s\"}", roomId, message);
            try {
                client.send(buildPostRequest(SET_COMMENT_API, token, setCommentData), HttpResponse.BodyHandlers.ofString());
            } catch (IOException | InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private static HttpRequest buildPostRequest(String url, String token, String body) {
        return HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();
    }
}
