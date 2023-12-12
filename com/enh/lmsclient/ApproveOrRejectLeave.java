package com.enh.lmsclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import java.util.ArrayList;
import java.util.List;
/**
 * This class provides functionality for approving or rejecting leave requests by a manager
 * in the LMS client application. It allows managers to view pending leave requests, take action on them,
 * and update the leave status.
 */
public class ApproveOrRejectLeave {
    private static final Logger log = LogManager.getLogger(ApproveOrRejectLeave.class);
    /**
     * View and process leave applications, allowing a manager to approve or reject them.
     *
     * @param managerId The ID of the manager reviewing the leave requests.
     * @param reader    A BufferedReader for user input.
     */
    public static void approveOrRejectLeave(int managerId, BufferedReader reader) {
        AppConfig config = new AppConfig();
        try {
            String leaveRequestsUrl = config.getApproveOrRejectLeave() + managerId;

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
                    log.info("Leave Applications for Approval/Rejection:");

                    // Create a list to store the data for formatting
                    List<String[]> tableData = new ArrayList<>();

                    // Initialize column headers
                    String[] headers = { "Leave ID", "Employee ID", "Start Date", "End Date", "Leave Type", "Reason",
                            "Status" };
                    tableData.add(headers);

                    for (int i = 0; i < leaveApplications.length(); i++) {
                        JSONObject leave = leaveApplications.getJSONObject(i);

                        // Create an array for the leave details
                        String[] row = {
                                String.valueOf(leave.getInt("leaveId")),
                                String.valueOf(leave.getInt("employeeId")),
                                leave.getString("startDate"),
                                leave.getString("endDate"),
                                leave.getString("leaveType"),
                                leave.getString("reason"),
                                leave.getString("status")
                        };

                        // Add the row to the table data
                        tableData.add(row);
                    }

                    // Format and print the table
                    printFormattedTable(tableData);

                    while (true) {
        log.info("Enter the Leave ID you want to approve/reject (0 to cancel): ");
        try {
            int leaveId = Integer.parseInt(reader.readLine());

            if (leaveId == 0) {
                log.info("Operation canceled.");
                return;
            } else if (!isValidLeaveId(leaveId, leaveApplications)) {
                log.warn("Invalid Leave ID. Please enter a valid Leave ID or 0 to cancel.");
            } else {
                log.info("Enter 'A' to approve or 'R' to reject: ");
                String action = reader.readLine().toUpperCase();

                if (action.equals("A") || action.equals("R")) {
                    submitLeaveApproval(managerId, leaveId, action);
                    return;
                } else {
                    log.warn("Invalid action. Please enter 'A' to approve or 'R' to reject.");
                }
            }
        } catch (NumberFormatException | IOException e) {
            log.error("Invalid input. Please enter a valid Leave ID or 0 to cancel.");
        }
    }

                } else {
                    log.info("No leave applications for approval/rejection.");
                }
            } else {
                log.error("Failed to fetch leave applications. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Submit leave approval or rejection for a specific leave request.
     *
     * @param managerId The ID of the manager reviewing the leave request.
     * @param leaveId   The ID of the leave request to approve or reject.
     * @param action    The action to take ('A' for approve, 'R' for reject).
     */
    private static void submitLeaveApproval(int managerId, int leaveId, String action) {
        try {
            AppConfig config = new AppConfig();
            String approveRejectLeaveUrl = config.getSubmitLeaveApproval() + leaveId;
            URL url = new URL(approveRejectLeaveUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = action.getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                String status = (action.equalsIgnoreCase("A")) ? "approved" : "rejected"; // Determine status
                // Send the email with the status to the employee
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                log.info("Enter the message (Optional):");
                String customMessage = reader.readLine();
                String message = "Leave has been " + status + " by your manager.\n\n" + customMessage;
                String mailto = config.getSmtpMailTo();
                SendEmail.optionForEmail(message, mailto);
                log.info("Leave status updated successfully.");
            } else {
                log.error("Failed to update leave status. Response code: " + responseCode);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    /**
     * Print a formatted table of leave applications for approval/rejection.
     *
     * @param data A list of String arrays containing leave application data for display.
     */
    private static void printFormattedTable(List<String[]> data) {
        int[] columnWidths = new int[data.get(0).length];

        // Calculate maximum column widths
        for (String[] row : data) {
            for (int i = 0; i < row.length; i++) {
                if (row[i].length() > columnWidths[i]) {
                    columnWidths[i] = row[i].length();
                }
            }
        }

        for (String[] row : data) {
            StringBuilder formattedRow = new StringBuilder("| ");
            for (int i = 0; i < row.length; i++) {
                formattedRow.append(String.format("%-" + (columnWidths[i] + 2) + "s | ", row[i]));
            }
            log.info(formattedRow.toString());
        }
    }
    /**
     * Check if a provided Leave ID is valid within a list of leave applications.
     *
     * @param leaveId           The Leave ID to validate.
     * @param leaveApplications The list of leave applications to check against.
     * @return true if the Leave ID is valid, false otherwise.
     */
    private static boolean isValidLeaveId(int leaveId, JSONArray leaveApplications) {
    for (int i = 0; i < leaveApplications.length(); i++) {
        JSONObject leave = leaveApplications.getJSONObject(i);
        if (leaveId == leave.getInt("leaveId")) {
            return true;
        }
    }
    return false;
}
}
