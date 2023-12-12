package com.enh.lmsclient;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class represents the configuration for the LMS client application. It
 * loads configuration properties from a "config.properties" file and provides
 * access methods to retrieve specific configuration values.
 */
public class AppConfig {
    private static final Logger log = LogManager.getLogger(AppConfig.class);
    private static final String CONFIG_FILE = "config.properties";
    private Properties properties;

    /**
     * Constructs an AppConfig instance and loads the configuration properties from
     * the "config.properties" file.
     */
    public AppConfig() {
        properties = new Properties();
        try (FileInputStream input = new FileInputStream(CONFIG_FILE)) {
            properties.load(input);
        } catch (IOException errorInput) {
            log.error("Error loading configuration", errorInput);
        }
    }

    /**
     * Get the API URL for leaving requests.
     *
     * @return The API URL for leaving requests.
     */
    public String getApiUrlLeave() {
        return properties.getProperty("api.url.leave");
    }

    /**
     * Get the API URL for viewing leaves.
     *
     * @return The API URL for viewing leaves.
     */
    public String getApiUrlViewLeaves() {
        return properties.getProperty("api.url.viewleaves");
    }

    /**
     * Get the SMTP host for sending emails.
     *
     * @return The SMTP host for sending emails.
     */
    public String getSmtpHost() {
        return properties.getProperty("smtp.host");
    }

    /**
     * Get the SMTP port for sending emails.
     *
     * @return The SMTP port for sending emails.
     */
    public int getSmtpPort() {
        return Integer.parseInt(properties.getProperty("smtp.port"));
    }

    /**
     * Get the SMTP username for email authentication.
     *
     * @return The SMTP username for email authentication.
     */
    public String getSmtpUsername() {
        return properties.getProperty("smtp.username");
    }

    /**
     * Get the SMTP password for email authentication.
     *
     * @return The SMTP password for email authentication.
     */
    public String getSmtpPassword() {
        return properties.getProperty("smtp.password");
    }

    /**
     * Get the recipient email address for sending emails.
     *
     * @return The recipient email address for sending emails.
     */
    public String getSmtpMailTo() {
        return properties.getProperty("smtp.mailto");
    }

    /**
     * Get the API URL for approving or rejecting leave requests.
     *
     * @return The API URL for approving or rejecting leave requests.
     */
    public String getApproveOrRejectLeave() {
        return properties.getProperty("api.url.approveorrejectleave");
    }

    /**
     * Get the API URL for submitting leave approval.
     *
     * @return The API URL for submitting leave approval.
     */
    public String getSubmitLeaveApproval() {
        return properties.getProperty("api.url.submitleaveapproval");
    }

    /**
     * Get the API URL for retrieving details of all employees.
     *
     * @return The API URL for retrieving details of all employees.
     */
    public String getAllEmployeeshr() {
        return properties.getProperty("api.url.getallemployeeswithdetails");
    }

    /**
     * Get the API URL for retrieving leaves of all employees.
     *
     * @return The API URL for retrieving leaves of all employees.
     */
    public String getLeavesAllEmployees() {
        return properties.getProperty("api.url.getleaveallemployees");
    }

    /**
     * Get the API URL for retrieving a list of employees reporting to a manager.
     *
     * @return The API URL for retrieving a list of employees reporting to a
     *         manager.
     */
    public String getListEmployeesReportingToManager() {
        return properties.getProperty("api.url.getlistemployeesreportingtomanager");
    }

    /**
     * Get the main API base URL.
     *
     * @return The main API base URL.
     */
    public String getApiBaseUrl() {
        return properties.getProperty("api.url.mainurl");
    }

    /**
     * Get the API URL for viewing leaves of specific employees.
     *
     * @return The API URL for viewing leaves of specific employees.
     */
    public String getViewLeavesEmployees() {
        return properties.getProperty("api.url.viewleavesemployees");
    }

    /**
     * Get the API URL for viewing leaves within a specified time period.
     *
     * @return The API URL for viewing leaves within a specified time period.
     */
    public String getViewLeavesInTimePeriod() {
        return properties.getProperty("api.url.viewleavesintimeperiod");
    }

    /**
     * Get the API URL for viewing leaves approved by a manager.
     *
     * @return The API URL for viewing leaves approved by a manager.
     */
    public String getLeavesInStpByManager() {
        return properties.getProperty("api.url.viewleavesbymanager");
    }

    /**
     * Get the API URL for leave requests for managers.
     *
     * @return The API URL for leave requests for managers.
     */
    public String getLeaveRequestsForManagers() {
        return properties.getProperty("api.url.leaverequestsformanager");
    }
}
