package com.enh.lmsclient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * This class provides the HR (Human Resources) menu and related actions.
 */
public class HrMenu {
    private static final Logger log = LogManager.getLogger(HrMenu.class);
 /**
     * Display the HR menu and perform actions based on user input.
     *
     * @param loggedInHr The JSON object representing the logged-in HR user.
     * @param reader     The BufferedReader used for user input.
     * @throws IOException If there's an issue with user input or any I/O operation.
     */
    public static void displayMenu(JSONObject loggedInHr, BufferedReader reader) throws IOException {
        int hrId = loggedInHr.getInt("hrId");
        log.info("Welcome to HRs portal");
        log.info("Your ID is: " + hrId);

        while (true) {
            log.info("HR Actions:");
            log.info("1. Get List of All Employees with Details");
            log.info("2. Get List of All Leaves with Details During the Specified Time Period");
            log.info("3. Logout");

            int choice;
            try {
                log.info("Enter the number corresponding to your choice: ");
                choice = Integer.parseInt(reader.readLine());
            } catch (NumberFormatException ex) {
                log.error("Invalid input. Please enter a valid number.");
                continue; // Continue the loop to ask for input again
            }

            switch (choice) {
                case 1:
                    GetAllEmployeesWithDetails.getAllEmployeesWithDetails(reader);
                    break;
                case 2:
                    GetLeavesDuringTimePeriod.getLeavesDuringTimePeriod(reader);
                    break;
                case 3:
                    log.info("Logging out...");
                    return; // Logout
                default:
                    log.error("Invalid choice. Please enter a valid option.");
            }
        }
    }
}
