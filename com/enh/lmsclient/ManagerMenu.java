package com.enh.lmsclient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * A class that provides the menu and actions available to managers in the Leave Management System.
 */
public class ManagerMenu {
    private static final Logger log = LogManager.getLogger(ManagerMenu.class);
  /**
     * Displays the menu and actions available to a logged-in manager.
     *
     * @param loggedInManager A JSON object representing the logged-in manager.
     * @param reader          A BufferedReader for reading user input.
     * @throws IOException If an error occurs while reading user input.
     */
    public static void displayMenu(JSONObject loggedInManager, BufferedReader reader) throws IOException {
        int managerId = loggedInManager.getInt("managerId");
        log.info("Welcome to Managers portal");
        log.info("Your ID is: " + managerId);

        while (true) {
            log.info("Manager Actions:");
            log.info("1. Approve/Reject Leave Applications");
            log.info("2. See List of Employees Reporting to You");
            log.info("3. See Leave Requests from Your Employees");
            log.info("4. view leaves in specific time period");
            log.info("5. Logout");

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
                    ApproveOrRejectLeave.approveOrRejectLeave(managerId, reader);
                    break;
                case 2:
                    ListEmployeesReportingToManager.listEmployeesReportingToManager(managerId, reader);
                    break;
                case 3:
                    ListLeaveRequestsForManager.listLeaveRequestsForManager(managerId, reader);
                    break;
                case 4:
                    GetLeaveRequestesInTimePeriod.getLeaveRequestsInTimePeriod(managerId, reader);
                    break;
                case 5:
                    log.info("Logging out...");
                    return; // Logout
                default:
                    log.error("Invalid choice. Please enter a valid option.");
            }
        }
    }
}
