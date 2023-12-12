package com.enh.lmsclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

/**
 * This class provides the functionality to retrieve and display details of all employees.
 */
public class GetAllEmployeesWithDetails {
    private static final Logger log = LogManager.getLogger(GetAllEmployeesWithDetails.class);
     /**
     * Retrieve and display details of all employees from the API.
     *
     * @param reader A BufferedReader for user input.
     */
    public static void getAllEmployeesWithDetails(BufferedReader reader) {
        AppConfig config = new AppConfig();
        try {
            String getAllEmployeesUrl = config.getAllEmployeeshr();

            URL url = new URL(getAllEmployeesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = apiReader.readLine()) != null) {
                    response.append(line);
                }
                apiReader.close();

                JSONArray employees = new JSONArray(response.toString());

                if (employees.length() > 0) {
                    log.info("List of All Employees with Details:");

                    // Initialize arrays to store maximum column widths and headers
                    String[] headers = { "Employee ID", "First Name", "Last Name", "Email", "Date of Birth",
                            "Contact Number", "Account Status", "Manager Id" };
                    int[] columnWidths = new int[headers.length];

                    // Update column widths based on header lengths
                    for (int i = 0; i < headers.length; i++) {
                        columnWidths[i] = headers[i].length();
                    }

                    // Process employee data to find maximum column widths
                    for (int i = 0; i < employees.length(); i++) {
                        JSONObject employee = employees.getJSONObject(i);
                        String[] rowData = {
                                String.valueOf(employee.getInt("employeeId")),
                                employee.getString("firstName"),
                                employee.getString("lastName"),
                                employee.getString("email"),
                                employee.getString("dob"),
                                employee.getString("contactNumber"),
                                employee.getString("accountStatus"),
                                String.valueOf(employee.getInt("managerId"))
                        };

                        // Update column widths based on employee data
                        for (int j = 0; j < headers.length; j++) {
                            columnWidths[j] = Math.max(columnWidths[j], rowData[j].length());
                        }
                    }

                    // Print headers
                    log.info(getFormattedTableRow(headers, columnWidths));

                    // Print a separator line
                    log.info(getFormattedSeparator(columnWidths));

                    // Print employee data
                    for (int i = 0; i < employees.length(); i++) {
                        JSONObject employee = employees.getJSONObject(i);
                        String[] rowData = {
                                String.valueOf(employee.getInt("employeeId")),
                                employee.getString("firstName"),
                                employee.getString("lastName"),
                                employee.getString("email"),
                                employee.getString("dob"),
                                employee.getString("contactNumber"),
                                employee.getString("accountStatus"),
                                String.valueOf(employee.getInt("managerId"))
                        };
                        log.info(getFormattedTableRow(rowData, columnWidths));
                    }
                } else {
                    log.info("No employees found.");
                }
            } else {
                log.error("Failed to fetch employee information. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
     /**
     * Format and return a table row with data and specified column widths.
     *
     * @param data         An array of data elements.
     * @param columnWidths An array of column widths.
     * @return A formatted table row as a string.
     */
    private static String getFormattedTableRow(String[] data, int[] columnWidths) {
        StringBuilder row = new StringBuilder("|");
        for (int i = 0; i < data.length; i++) {
            row.append(" " + padRight(data[i], columnWidths[i]) + " |");
        }
        return row.toString();
    }
    /**
     * Generate a formatted separator line based on column widths.
     *
     * @param columnWidths An array of column widths.
     * @return A formatted separator line as a string.
     */
    private static String getFormattedSeparator(int[] columnWidths) {
        StringBuilder separator = new StringBuilder("+");
        for (int width : columnWidths) {
            separator.append("-".repeat(width + 2) + "+");
        }
        return separator.toString();
    }
    /**
     * Pad a string to the right with spaces to the specified width.
     *
     * @param s     The input string.
     * @param width The desired width.
     * @return The padded string.
     */
    private static String padRight(String s, int width) {
        return String.format("%-" + width + "s", s);
    }
}
