package com.enh.lmsclient;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
/**
 * A class for viewing leave applications of a logged-in employee.
 */
public class ViewMyLeaves {
    private static final Logger log = LogManager.getLogger(ViewMyLeaves.class);
 /**
     * Displays leave applications for the logged-in employee.
     *
     * @param loggedInEmployee The JSON object representing the logged-in employee.
     * @param reader           A BufferedReader to read user input.
     */
    public static void viewLeaves(JSONObject loggedInEmployee, BufferedReader reader) {
        int employeeId = loggedInEmployee.getInt("employeeId");
        AppConfig config = new AppConfig();
        String viewLeavesUrl = config.getViewLeavesEmployees() + employeeId;

        try {
            // Send an HTTP GET request to the API endpoint for viewing leaves
            URL url = new URL(viewLeavesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // Read and display the leave applications
                BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = apiReader.readLine()) != null) {
                    response.append(line);
                }
                apiReader.close();

                JSONArray leaveApplications = new JSONArray(response.toString());

                if (leaveApplications.length() > 0) {
                    log.info("Your Leave Applications:");

                    // Initialize arrays to store maximum column widths and headers
                    String[] headers = { "Leave ID", "Start Date", "End Date", "Leave Type", "Status", "Reason" };
                    int[] columnWidths = new int[headers.length];

                    // Update column widths based on header lengths
                    for (int i = 0; i < headers.length; i++) {
                        columnWidths[i] = headers[i].length();
                    }

                    // Process leave request data to find maximum column widths
                    for (int i = 0; i < leaveApplications.length(); i++) {
                        JSONObject leave = leaveApplications.getJSONObject(i);
                        String[] rowData = {
                                String.valueOf(leave.getInt("leaveId")),
                                leave.getString("startDate"),
                                leave.getString("endDate"),
                                leave.getString("leaveType"),
                                leave.getString("status"),
                                leave.getString("reason")
                        };

                        // Update column widths based on leave request data
                        for (int j = 0; j < headers.length; j++) {
                            columnWidths[j] = Math.max(columnWidths[j], rowData[j].length());
                        }
                    }

                    // Print headers
                    log.info(getFormattedTableRow(headers, columnWidths));

                    // Print a separator line
                    log.info(getFormattedSeparator(columnWidths));

                    // Print leave request data
                    for (int i = 0; i < leaveApplications.length(); i++) {
                        JSONObject leave = leaveApplications.getJSONObject(i);
                        String[] rowData = {
                                String.valueOf(leave.getInt("leaveId")),
                                leave.getString("startDate"),
                                leave.getString("endDate"),
                                leave.getString("leaveType"),
                                leave.getString("status"),
                                leave.getString("reason")
                        };
                        log.info(getFormattedTableRow(rowData, columnWidths));
                    }
                } else {
                    log.info("No leave applications found.");
                }
            } else {
                log.error("Failed to fetch leave applications. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
/**
     * Formats the data into a row for logging, ensuring proper alignment.
     *
     * @param data          The data to format into a row.
     * @param columnWidths  An array of column widths for alignment.
     * @return A formatted row for logging.
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
