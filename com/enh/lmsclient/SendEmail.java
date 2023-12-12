package com.enh.lmsclient;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
/**
 * A class for sending emails using Gmail's SMTP server.
 */
public class SendEmail {
    private static final Logger log = LogManager.getLogger(SendEmail.class);

     /**
     * Sends an email with the specified data to the given recipient.
     *
     * @param data The email content to be sent.
     * @param to   The recipient's email address.
     * @throws IOException If an error occurs while reading configuration data.
     */
    public static void optionForEmail(String data, String to) throws IOException {
        AppConfig config = new AppConfig();
        String from = config.getSmtpUsername(); 
        String password = config.getSmtpPassword(); 
        String host = config.getSmtpHost(); // Sending email from through Gmail's SMTP
        Properties properties = System.getProperties();

        properties.put("mail.smtp.host", host);
        properties.put("mail.smtp.port", "587");
        properties.put("mail.smtp.starttls.enable", "true");
        properties.put("mail.smtp.auth", "true");

        Session session = Session.getInstance(properties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(from, password);
            }
        });

        try {
            MimeMessage message = new MimeMessage(session);
            message.setFrom(new InternetAddress(from));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject("Leaves");
            message.setText(data + "\n");
            log.info("Sending mail please...");
            Transport.send(message);
            log.info("Sent message successfully.");
        } catch (MessagingException mex) {
            mex.printStackTrace();
        }
    }
}
