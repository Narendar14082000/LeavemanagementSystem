package com.enh.lmsclient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * This class provides a menu for employees in the LMS client application. Employees can
 * choose various options, such as applying for leave, viewing their leaves, and more.
 */
public class EmployeeMenu {
    private static final Logger log = LogManager.getLogger(EmployeeMenu.class);
    /**
     * Display the menu for an employee and handle their menu choices.
     *
     * @param loggedInEmployee The JSON object representing the logged-in employee.
     * @param reader           A BufferedReader for user input.
     * @throws IOException If there's an issue with I/O operations.
     */
    public static void displayMenu(JSONObject loggedInEmployee, BufferedReader reader) throws IOException {
        while (true) {
            log.info("Employee Menu:");
            log.info("1. Apply for leave");
            log.info("2. View my leaves");
            log.info("3. View leaves in a specific time period");
            log.info("4. Logout");

            int choice;
            try {
                log.info("Enter the number corresponding to your choice: ");
                choice = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException ex) {
                log.info("Invalid input. Please enter a valid number.");
                continue; // Continue the loop to ask for input again
            }

            switch (choice) {
                case 1:
                    ApplyForLeave.applyForLeave(loggedInEmployee, reader);
                    break;
                case 2:
                    ViewMyLeaves.viewLeaves(loggedInEmployee, reader);
                    break;
                case 3:
                    ViewMyLeavesInTimePeriod.viewLeavesInTimePeriod(loggedInEmployee, reader);
                    break;
                case 4:
                    log.info("Logging out.");
                    return;
                default:
                    log.error("Invalid choice.");
            }
        }
    }
}
