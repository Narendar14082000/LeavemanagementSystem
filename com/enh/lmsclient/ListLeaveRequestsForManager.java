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
 * This class provides methods to list leave requests from employees reporting to a specific manager.
 */
public class ListLeaveRequestsForManager {
    private static final Logger log = LogManager.getLogger(ListLeaveRequestsForManager.class);
 /**
     * Lists leave requests from employees reporting to a specific manager and displays their details.
     *
     * @param managerId The ID of the manager for whom leave requests need to be listed.
     * @param reader    The BufferedReader used for user input.
     */
    public static void listLeaveRequestsForManager(int managerId, BufferedReader reader) {
        AppConfig config = new AppConfig();
        try {
            String leaveRequestsUrl = config.getLeaveRequestsForManagers() + managerId;

            // Send an HTTP GET request to get leave requests for the manager
            URL url = new URL(leaveRequestsUrl);
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

                // Parse the JSON data (leave applications)
                JSONArray leaveApplications = new JSONArray(response.toString());

                if (leaveApplications.length() > 0) {
                    log.info("Leave Requests from Your Employees:");

                    // Initialize arrays to store maximum column widths and headers
                    String[] headers = { "Leave ID", "Employee ID", "Start Date", "End Date", "Leave Type", "Status",
                            "Reason" };
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
                                String.valueOf(leave.getInt("employeeId")),
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
                                String.valueOf(leave.getInt("employeeId")),
                                leave.getString("startDate"),
                                leave.getString("endDate"),
                                leave.getString("leaveType"),
                                leave.getString("status"),
                                leave.getString("reason")
                        };
                        log.info(getFormattedTableRow(rowData, columnWidths));
                    }
                } else {
                    log.info("No leave requests from your employees.");
                }
            } else {
                log.error("Failed to fetch leave requests. Response code: " + responseCode);
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
     * Generates a separator line based on column widths for a formatted table.
     *
     * @param columnWidths An array of column widths.
     * @return A formatted separator line.
     */
    private static String getFormattedSeparator(int[] columnWidths) {
        StringBuilder separator = new StringBuilder("+");
        for (int width : columnWidths) {
            separator.append("-".repeat(width + 2) + "+");
        }
        return separator.toString();
    }
 /**
     * Pads the string to the right with spaces to the specified width.
     *
     * @param s     The string to be padded.
     * @param width The desired width of the padded string.
     * @return The padded string.
     */
    private static String padRight(String s, int width) {
        return String.format("%-" + width + "s", s);
    }
}
