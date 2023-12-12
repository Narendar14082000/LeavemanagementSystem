package com.enh.lmsclient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * This class represents the functionality for applying for leave in the LMS client application.
 * It provides methods to interact with the user, calculate leave quotas, and submit leave applications.
 */
public class ApplyForLeave {
    private static final Logger log = LogManager.getLogger(ApplyForLeave.class);
    private static final int MAX_LEAVES_PER_MONTH = 4;
    private static final int MAX_LEAVES_PER_YEAR = 20;
    private static final String DATE_FORMAT = "yyyy-MM-dd";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);
    private static final LocalDate currentDate = LocalDate.now();
    /**
     * Allows an employee to apply for leave by providing necessary information such as leave dates, type, and reason.
     * Validates the input and sends a leave application request to the server.
     *
     * @param loggedInEmployee A JSON object representing the currently logged-in employee.
     * @param reader           A BufferedReader for user input.
     */
    public static void applyForLeave(JSONObject loggedInEmployee, BufferedReader reader) {
        AppConfig config = new AppConfig();
        try {
            log.info("Leave Application Process:");
            log.info("Enter 'E' at any time to exit the leave application process.");

            // Check the number of leaves applied by the employee in the current month
            int currentMonthLeaves = getCurrentMonthLeaves(loggedInEmployee);

            if (currentMonthLeaves >= MAX_LEAVES_PER_MONTH) {
                log.info("You have already applied for the maximum allowed leaves this month.");
                return;
            }

            int leavesRemainingThisMonth = MAX_LEAVES_PER_MONTH - currentMonthLeaves;
            log.info("You have " + leavesRemainingThisMonth + " leave(s) remaining this month.");

            int leavesRemainingThisYear = MAX_LEAVES_PER_YEAR - getTotalLeavesThisYear(loggedInEmployee);
            if (leavesRemainingThisYear <= 0) {
                log.info("You have exhausted all your leaves for this year.");
                return;
            }

            log.info("You have " + leavesRemainingThisYear + " leave(s) remaining this year.");

            LocalDate startDate = promptForDate("Enter the start date (YYYY-MM-DD) (or 'E' to exit): ", reader);
            if (startDate == null) {
                log.info("Leave application process canceled.");
                return;
            }

            if (startDate.isBefore(currentDate)) {
                log.info("You have entered a past date. Please enter a current or future date.");
                return;
            }

            LocalDate endDate = promptForDate("Enter the end date (YYYY-MM-DD) (or 'E' to exit): ", reader);
            if (endDate == null) {
                log.info("Leave application process canceled.");
                return;
            }

            if (endDate.isBefore(currentDate)) {
                log.info("You have entered a past date for the end date. Please enter a current or future date.");
                return;
            }

            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);
            if (daysBetween >= 5) {
                log.info("You cannot apply for leave for 5 or more continuous days.");
                return;
            }

            if (endDate.isBefore(startDate)) {
                log.info("End date must be after the start date.");
                return;
            }

            String leaveType = "";
            while (true) {
                log.info("Enter the leave type (CasualLeave, SickLeave, or Comp-off) (or 'E' to exit): ");
                leaveType = reader.readLine().trim();

                if (leaveType.equalsIgnoreCase("E")) {
                    log.info("Leave application process canceled.");
                    return; // Return to the menu
                } else if (isValidLeaveType(leaveType)) {
                    break; // Exit the loop if a valid leave type is provided
                } else {
                    log.error("Invalid leave type. Please enter CasualLeave, SickLeave, or Comp-off.");
                }
            }

            log.info("Enter the reason (or 'E' to exit): ");
            String reason = reader.readLine().trim();

            if (reason.equalsIgnoreCase("E")) {
                log.info("Leave application process canceled.");
                return; // Return to the menu
            }

            String formattedStartDate = startDate + "T00:00:00.000Z";
            String formattedEndDate = endDate + "T00:00:00.000Z";

            JSONObject leaveApplication = new JSONObject();
            // Use the employee ID and manager ID from the loggedInEmployee
            int employeeId = loggedInEmployee.getInt("employeeId");
            int managerId = loggedInEmployee.getInt("managerId");
            leaveApplication.put("employeeId", employeeId);
            leaveApplication.put("managerId", managerId);
            leaveApplication.put("startDate", formattedStartDate);
            leaveApplication.put("endDate", formattedEndDate);
            leaveApplication.put("leaveType", leaveType);
            leaveApplication.put("reason", reason);
            leaveApplication.put("status", "pending");

            // Send a POST request to the API to apply for leave
            String applyLeaveUrl = config.getApiUrlLeave();
            URL leaveUrl = new URL(applyLeaveUrl);
            HttpURLConnection conn = (HttpURLConnection) leaveUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);

            try (OutputStream os = conn.getOutputStream()) {
                byte[] input = leaveApplication.toString().getBytes("utf-8");
                os.write(input, 0, input.length);
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                log.info("Leave application submitted successfully.");
                String mailto = "narendarreddypaindla@gmail.com";
                StringBuilder message = new StringBuilder();
                message.append("Leave application submitted successfully.\n\n");
                message.append("Start Date: ").append(formattedStartDate).append("\n");
                message.append("End Date: ").append(formattedEndDate).append("\n");
                message.append("Leave Type: ").append(leaveType).append("\n");
                message.append("Reason: ").append(reason).append("\n");
                message.append("Status: pending\n");
                SendEmail.optionForEmail(message.toString(), mailto);
            } else {
                log.error("Failed to submit leave application. Response code: " + responseCode);
            }

        } catch (Exception errorApplication) {
            log.error("Failed to submit leave application. Response code: ", errorApplication);
        }
    }
    /**
     * Calculate the number of leaves applied by the employee in the current month.
     * Makes an API request to fetch the leaves and counts them.
     *
     * @param loggedInEmployee A JSON object representing the currently logged-in employee.
     * @return The number of leaves applied by the employee in the current month.
     */
    private static int getCurrentMonthLeaves(JSONObject loggedInEmployee) {
        AppConfig config = new AppConfig();
        // Calculate the number of leaves applied by the employee in the current month
        int employeeId = loggedInEmployee.getInt("employeeId");
        String currentMonthStartDate = getCurrentMonthStartDate();
        String currentMonthEndDate = getCurrentMonthEndDate();

        // Replace this placeholder with the actual URL of your API endpoint
        String currentMonthLeavesUrl = config.getApiUrlViewLeaves() + "?employeeId="
                + employeeId
                + "&startDate=" + currentMonthStartDate + "&endDate=" + currentMonthEndDate;

        try {
            URL url = new URL(currentMonthLeavesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // Read and process the response
                BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = apiReader.readLine()) != null) {
                    response.append(line);
                }
                apiReader.close();

                // Parse the response JSON to count the leaves
                JSONArray leavesInCurrentMonth = new JSONArray(response.toString());
                int leavesCount = leavesInCurrentMonth.length();

                return leavesCount;
            } else {
                log.error("Failed to fetch leaves information. Response code: " + responseCode);
                return 0; // Return 0 leaves if there was an error
            }
        } catch (Exception errorLeaves) {
            log.error("Failed to fetch leaves information. Response code: ", errorLeaves);
            return 0; // Return 0 leaves in case of an exception
        }
    }
    /**
     * Get the start date of the current month.
     *
     * @return A string representing the start date of the current month in "YYYY-MM-DD" format.
     */
    private static int getTotalLeavesThisYear(JSONObject loggedInEmployee) {
        AppConfig config = new AppConfig();
        // Calculate the total number of leaves applied by the employee in the current
        // year
        int employeeId = loggedInEmployee.getInt("employeeId");
        String currentYearStartDate = getCurrentYearStartDate();
        String currentYearEndDate = getCurrentYearEndDate();

        // Replace this placeholder with the actual URL of your API endpoint
        String currentYearLeavesUrl = config.getApiUrlViewLeaves() + "?employeeId="
                + employeeId
                + "&startDate=" + currentYearStartDate + "&endDate=" + currentYearEndDate;

        try {
            URL url = new URL(currentYearLeavesUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            int responseCode = conn.getResponseCode();

            if (responseCode == 200) {
                // Read and process the response
                BufferedReader apiReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = apiReader.readLine()) != null) {
                    response.append(line);
                }
                apiReader.close();

                // Parse the response JSON to count the leaves
                JSONArray leavesInCurrentYear = new JSONArray(response.toString());
                int leavesCount = leavesInCurrentYear.length();

                return leavesCount;
            } else {
                log.error("Failed to fetch leaves information. Response code: " + responseCode);
                return 0; // Return 0 leaves if there was an error
            }
        } catch (Exception errorLeaves) {
            log.error("Failed to fetch leaves information. Response code: ", errorLeaves);
            return 0; // Return 0 leaves in case of an exception
        }
    }
     
    /**
     * Get the start date of the current month.
     *
     * @return A string representing the start date of the current month in "YYYY-MM-DD" format.
     */
    private static String getCurrentMonthStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return String.format("%tF", calendar);
    }
    /**
     * Get the end date of the current month.
     *
     * @return A string representing the end date of the current month in "YYYY-MM-DD" format.
     */
    private static String getCurrentMonthEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.add(Calendar.DAY_OF_MONTH, -1);
        return String.format("%tF", calendar);
    }
   /**
     * Get the start date of the current year.
     *
     * @return A string representing the start date of the current year in "YYYY-MM-DD" format.
     */
    private static String getCurrentYearStartDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        return String.format("%tF", calendar);
    }
     /**
     * Get the end date of the current year.
     *
     * @return A string representing the end date of the current year in "YYYY-MM-DD" format.
     */
    private static String getCurrentYearEndDate() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        return String.format("%tF", calendar);
    }
   /**
     * Prompt the user for a date input and validate it.
     *
     * @param message The message to display to the user.
     * @param reader  A BufferedReader for user input.
     * @return A LocalDate object representing the parsed date or null if the user cancels the process.
     */
    private static LocalDate promptForDate(String message, BufferedReader reader) {
        LocalDate date = null;
        boolean valid = false;
        while (!valid) {
            try {
                log.info(message);
                String dateInput = reader.readLine().trim();

                if (dateInput.equalsIgnoreCase("E")) {
                    return null; // Exit
                }

                date = LocalDate.parse(dateInput, DATE_FORMATTER);
                valid = true;
            } catch (Exception e) {
                log.error("Invalid date format. Please use YYYY-MM-DD.");
            }
        }
        return date;
    }
   /**
     * Check if the provided leave type is valid (CasualLeave, SickLeave, or Comp-off).
     *
     * @param leaveType The leave type to validate.
     * @return true if the leave type is valid, false otherwise.
     */
    private static boolean isValidLeaveType(String leaveType) {
        return leaveType.equalsIgnoreCase("CasualLeave") || leaveType.equalsIgnoreCase("SickLeave")
                || leaveType.equalsIgnoreCase("Comp-off");
    }
}
