package com.enh.lmsclient;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
/**
 * The main class that provides a user interface to interact with the Leave Management System.
 */
public class Main {
    private static final Logger log = LogManager.getLogger(Main.class);
   /**
     * The main method to start the Leave Management System.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String[] args) {

        AppConfig config = new AppConfig();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            String apiUrl = config.getApiBaseUrl();

            while (true) {
                log.info("**************Welcome to Leave Management System****************");
                log.info("1. Employee Login");
                log.info("2. Manager Login");
                log.info("3. HR Login");
                log.info("4. Exit");

                int choice;
                try {
                    log.info("Enter the number corresponding to your choice: ");
                    choice = Integer.parseInt(reader.readLine());
                } catch (NumberFormatException ex) {
                    log.error("Invalid input. Please enter a valid number.");
                    continue;
                }

                JSONObject loggedInUser = null;

                switch (choice) {
                    case 1:
                        loggedInUser = EmployeeLogin.login(apiUrl + "/employees");
                        if (loggedInUser != null) {
                            EmployeeMenu.displayMenu(loggedInUser, reader);
                        }
                        break;
                    case 2:
                        loggedInUser = ManagerLogin.login(apiUrl + "/managers");
                        if (loggedInUser != null) {
                            ManagerMenu.displayMenu(loggedInUser, reader);
                        }
                        break;
                    case 3:
                        loggedInUser = HrLogin.login(apiUrl + "/hrs");
                        if (loggedInUser != null) {
                            HrMenu.displayMenu(loggedInUser, reader);
                        }
                        break;
                    case 4:
                        log.info("Exiting.");
                        return;
                    default:
                        log.error("Invalid choice.");
                }
            }
        } catch (IOException errorMain) {
            log.error("Error in Main", errorMain);
        }
    }
}
