package com.enh.lmsclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.Console;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.mindrot.jbcrypt.BCrypt;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * This class provides HR (Human Resources) login functionality.
 */
public class HrLogin {
    private static final Logger log = LogManager.getLogger(HrLogin.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
    /**
     * Attempt to log in an HR user.
     *
     * @param apiUrl The API URL for HR data.
     * @return A JSON object representing the logged-in HR user, or null if login fails.
     * @throws IOException If there's an issue with the HTTP request.
     */
    public static JSONObject login(String apiUrl) throws IOException {
        Console console = System.console();
        if (console == null) {
            log.info("Console not available. Cannot securely read the password.");
            return null;
        }

        log.info("Welcome to HR's portal");

        String email;
        do {
            email = console.readLine("Enter your email: ");
            if (!isValidEmail(email)) {
                log.error("Invalid email format. Please enter a valid email address.");
            }
        } while (!isValidEmail(email));

        char[] passwordChars = console.readPassword("Enter your password: ");
        String password = new String(passwordChars);

        // Send an HTTP GET request to the API endpoint
        URL url = new URL(apiUrl + "/gethrs");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        int responseCode = conn.getResponseCode();

        if (responseCode == 200) {
            // Read the response from the API
            BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = apiReader.readLine()) != null) {
                response.append(line);
            }
            apiReader.close();

            // Parse the JSON data
            JSONArray hrs = new JSONArray(response.toString());

            // Find the HR with the given email
            JSONObject loggedInHr = null;
            for (int i = 0; i < hrs.length(); i++) {
                JSONObject hr = hrs.getJSONObject(i);
                if (hr.getString("email").equals(email) && BCrypt.checkpw(password, hr.getString("password"))
                        && "Active".equalsIgnoreCase(hr.getString("accountStatus"))) {
                    loggedInHr = hr;
                    break;
                }
            }

            if (loggedInHr != null) {
                log.info("Login successful!");
                return loggedInHr;
            } else {
                log.error("Login failed. Invalid email or password.");
            }
        } else {
            log.error("Failed to fetch data from the API. Response code: " + responseCode);
        }

        // Clear the password from memory
        Arrays.fill(passwordChars, ' ');

        return null;
    }
/**
     * Check if an email is in a valid format.
     *
     * @param email The email to validate.
     * @return True if the email is in a valid format, false otherwise.
     */
    private static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
