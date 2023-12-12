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
 * This class provides the functionality for employee login in the LMS client application.
 * It allows employees to securely log in by verifying their email and password with the server.
 */
public class EmployeeLogin {
    private static final Logger log = LogManager.getLogger(EmployeeLogin.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");
     /**
     * Attempt to log in an employee by verifying their email and password with the server.
     *
     * @param apiUrl The API endpoint URL for employee data.
     * @return A JSON object representing the logged-in employee or null if the login fails.
     * @throws IOException If there's an issue with I/O operations.
     */
    public static JSONObject login(String apiUrl) throws IOException {
        Console console = System.console();
        if (console == null) {
            log.info("Console not available. Cannot securely read the password.");
            return null;
        }

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
        URL url = new URL(apiUrl + "/getemployees");
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
            JSONArray employees = new JSONArray(response.toString());

            // Find the employee with the given email and hashed password
            JSONObject loggedInEmployee = null;
            for (int i = 0; i < employees.length(); i++) {
                JSONObject employee = employees.getJSONObject(i);
                if (employee.getString("email").equals(email)
                        && BCrypt.checkpw(password, employee.getString("password"))
                        && "Active".equalsIgnoreCase(employee.getString("accountStatus"))) {
                    loggedInEmployee = employee;
                    break;
                }
            }

            if (loggedInEmployee != null) {
                log.info("Login successful!");
                log.info("Employee ID: " + loggedInEmployee.getInt("employeeId"));
                log.info("First Name: " + loggedInEmployee.getString("firstName"));
                log.info("Last Name: " + loggedInEmployee.getString("lastName"));
                return loggedInEmployee;
            } else {
                log.error("Login failed. Invalid email, password, or account is inactive.");
            }
        } else {
            log.error("Failed to fetch data from the API. Response code: " + responseCode);
        }

        // Clear the password from memory
        Arrays.fill(passwordChars, ' ');

        return null;
    }
     /**
     * Check if an email address is in a valid format.
     *
     * @param email The email address to validate.
     * @return true if the email is valid, false otherwise.
     */
    private static boolean isValidEmail(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }
}
