package RafaelJ13.github_mighrator;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main {

    static String apiUrl = "https://api.github.com/";

    public static void main(String[] args) {
        String reset = "\u001B[0m";
        String titleColor = "\u001B[32m";
        String promptColor = "\u001B[34m";

        System.out.println(titleColor + "###########################");
        System.out.println("GitHub Repository Migrator");
        System.out.println("###########################" + reset);
        System.out.println("\nThis program allows you to migrate repositories from an old account to a new one.");
        System.out.println("You can choose to migrate public or private repositories.\n");

        Scanner scanner = new Scanner(System.in);

        System.out.print(promptColor + "Enter your GitHub token: " + reset);
        String token = scanner.nextLine();

        System.out.print(promptColor + "Enter the current username (where the repositories will be created): " + reset);
        String currentUsername = scanner.nextLine();

        System.out.print(promptColor + "Enter the old username (from which the repositories will be copied): " + reset);
        String oldUsername = scanner.nextLine();

        System.out.print(promptColor + "Do you want to migrate public or private repositories? (private / public): " + reset);
        String repoType = scanner.nextLine();

        if (!repoType.equalsIgnoreCase("public") && !repoType.equalsIgnoreCase("private")) {
            System.out.println("Invalid option. Considering public repositories.");
            repoType = "public";
        }

        String response = sendGetRequest("users/" + oldUsername + "/repos?type=" + repoType, token);

        if (response == null) {
            System.out.println("Error accessing the repositories of the old account.");
            return;
        }

        List<String> repoLinks = new ArrayList<>();
        JSONArray repos = new JSONArray(response);

        for (int i = 0; i < repos.length(); i++) {
            JSONObject repo = repos.getJSONObject(i);
            repoLinks.add(repo.getString("html_url"));
        }

        for (String repoUrl : repoLinks) {
            createRepo(repoUrl, currentUsername, token);
        }

        scanner.close();
    }

    public static String sendGetRequest(String path, String token) {
        try {
            URL url = new URL(apiUrl + path);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Authorization", "token " + token);

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error accessing the URL: " + url + " | Response code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            connection.disconnect();

            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void createRepo(String repoUrl, String currentUsername, String token) {
        try {
            String repoName = extractRepoNameFromUrl(repoUrl);
            if (repoName == null) {
                System.out.println("Could not extract repository name.");
                return;
            }

            URL url = new URL(apiUrl + "user/repos");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Authorization", "token " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String jsonBody = "{\"name\": \"" + repoName + "\", \"private\": true}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                System.out.println("Repository " + repoName + " created successfully.");
                cloneRepoContent(repoUrl, repoName, currentUsername, token);
            } else {
                System.out.println("Error creating repository: " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String extractRepoNameFromUrl(String repoUrl) {
        String regex = "https://github.com/.+/([^/]+)$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(repoUrl);

        if (matcher.find()) {
            return matcher.group(1);
        } else {
            System.out.println("Invalid URL or repository not found.");
            return null;
        }
    }

    public static void cloneRepoContent(String repoUrl, String newRepoName, String currentUsername, String token) {
        try {
            String repoOwner = "TheYurei";
            String repoName = extractRepoNameFromUrl(repoUrl);

            if (repoName == null) {
                System.out.println("Repository name not found. Check the URL.");
                return;
            }

            String apiEndpoint = "repos/" + repoOwner + "/" + repoName + "/contents";
            String response = sendGetRequest(apiEndpoint, token);

            if (response == null) {
                System.out.println("Error accessing the content of repository " + repoName);
                return;
            }

            JSONArray files = new JSONArray(response);

            for (int i = 0; i < files.length(); i++) {
                JSONObject file = files.getJSONObject(i);

                String filePath = file.getString("path");
                if (file.getString("type").equals("file")) {
                    String fileContentUrl = file.optString("download_url", null);

                    if (fileContentUrl != null) {
                        String fileContent = getFileContent(fileContentUrl);

                        if (fileContent != null && !fileContent.isEmpty()) {
                            uploadFileToNewRepo(filePath, fileContent, newRepoName, currentUsername, token);
                        } else {
                            System.out.println("Content of file " + filePath + " is empty or not found.");
                        }
                    } else {
                        System.out.println("Download URL for file " + filePath + " is not available.");
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getFileContent(String fileContentUrl) {
        try {
            URL url = new URL(fileContentUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode != 200) {
                System.out.println("Error accessing the file: " + fileContentUrl + " | Response code: " + responseCode);
                return null;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            StringBuilder fileContent = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                fileContent.append(line).append(System.lineSeparator());
            }
            reader.close();
            connection.disconnect();

            return fileContent.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void uploadFileToNewRepo(String filePath, String fileContent, String newRepoName, String currentUsername, String token) {
        try {
            String apiEndpoint = "repos/" + currentUsername + "/" + newRepoName + "/contents/" + filePath;
            URL url = new URL(apiUrl + apiEndpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("PUT");
            connection.setRequestProperty("Authorization", "token " + token);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);

            String encodedContent = Base64.getEncoder().encodeToString(fileContent.getBytes(StandardCharsets.UTF_8));

            String jsonBody = "{\"message\": \"Creating file: " + filePath + "\", \"content\": \"" + encodedContent + "\"}";

            try (OutputStream os = connection.getOutputStream()) {
                byte[] input = jsonBody.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);
            }

            int responseCode = connection.getResponseCode();
            if (responseCode == 201) {
                System.out.println("File " + filePath + " uploaded successfully.");
            } else {
                System.out.println("Error uploading file " + filePath + ": " + responseCode);
            }

            connection.disconnect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
