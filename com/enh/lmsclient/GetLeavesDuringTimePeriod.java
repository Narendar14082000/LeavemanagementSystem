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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This class provides the functionality to retrieve and display leave requests within a specific time period.
 */
public class GetLeavesDuringTimePeriod {
    private static final Logger log = LogManager.getLogger(GetLeavesDuringTimePeriod.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
 /**
     * Retrieve and display leave requests within a specified time period.
     *
     * @param reader A BufferedReader for user input.
     */
    public static void getLeavesDuringTimePeriod(BufferedReader reader) {
        try {
            LocalDate startDate = promptForDate("Enter the start date (YYYY-MM-DD): ", reader);

            LocalDate endDate;
            do {
                endDate = promptForDate("Enter the end date (YYYY-MM-DD): ", reader);
                if (endDate.isBefore(startDate)) {
                    log.error("End date should be after the start date. Please enter a valid end date.");
                }
            } while (endDate.isBefore(startDate));
            AppConfig config = new AppConfig();
            String viewLeavesUrl = config.getLeavesAllEmployees() + startDate + "&endDate=" + endDate;

            // Send an HTTP GET request to the API endpoint for viewing leaves in the
            // specified time period
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
                    log.info("Leave Applications in the specified time period:");

                    // Initialize arrays to store maximum column widths and headers
                    String[] headers = { "Leave ID", "Employee ID", "Start Date", "End Date", "Leave Type", "Status",
                            "Reason" };
                    int[] columnWidths = new int[headers.length];

                    // Update column widths based on header lengths
                    for (int i = 0; i < headers.length; i++) {
                        columnWidths[i] = headers[i].length();
                    }

                    // Process leave application data to find maximum column widths
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

                        // Update column widths based on leave application data
                        for (int j = 0; j < headers.length; j++) {
                            columnWidths[j] = Math.max(columnWidths[j], rowData[j].length());
                        }
                    }

                    // Print headers
                    log.info(getFormattedTableRow(headers, columnWidths));

                    // Print a separator line
                    log.info(getFormattedSeparator(columnWidths));

                    // Print leave application data
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
                    log.info("No leave applications in the specified time period.");
                }
            } else {
                log.error("Failed to fetch leave applications. Response code: " + responseCode);
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
 /**
     * Prompt the user for a date input.
     *
     * @param message The message to display to the user.
     * @param reader  A BufferedReader for user input.
     * @return The parsed LocalDate object based on the user's input.
     */
    private static LocalDate promptForDate(String message, BufferedReader reader) {
        LocalDate date = null;
        boolean valid = false;
        while (!valid) {
            try {
                log.info(message);
                String dateInput = reader.readLine().trim();
                date = LocalDate.parse(dateInput, DATE_FORMATTER);
                valid = true;
            } catch (Exception e) {
                log.error("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }
}
