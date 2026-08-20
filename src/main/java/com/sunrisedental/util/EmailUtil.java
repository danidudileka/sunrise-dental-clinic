package com.sunrisedental.util;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Utility class for sending emails.
 * Uses Gmail SMTP for sending notifications.
 */
public class EmailUtil {
    private static final Logger logger = LogManager.getLogger(EmailUtil.class);
    private static Properties emailProperties;

    static {
        loadProperties();
    }

    /**
     * Load email properties
     */
    private static void loadProperties() {
        emailProperties = new Properties();
        try (InputStream input = EmailUtil.class.getClassLoader()
                .getResourceAsStream("email.properties")) {
            if (input != null) {
                emailProperties.load(input);
                logger.info("Email properties loaded successfully");
            } else {
                logger.warn("email.properties not found, using defaults");
                setDefaultProperties();
            }
        } catch (IOException e) {
            logger.error("Error loading email properties", e);
            setDefaultProperties();
        }
    }

    /**
     * Set default properties if file not found
     */
    private static void setDefaultProperties() {
        emailProperties.put("email.host", "smtp.gmail.com");
        emailProperties.put("email.port", "587");
        emailProperties.put("email.username", "");
        emailProperties.put("email.password", "");
        emailProperties.put("email.from", "Sunrise Dental Clinic");
        emailProperties.put("email.auth", "true");
        emailProperties.put("email.starttls", "true");
    }

    /**
     * Send appointment confirmation email
     */
    public static boolean sendAppointmentConfirmation(String toEmail, String patientName,
                                                      String appointmentNumber, String dentistName,
                                                      String treatmentType, String appointmentDate,
                                                      String appointmentTime) {

        String subject = "Appointment Confirmation - " + appointmentNumber;

        String body = createAppointmentEmailBody(patientName, appointmentNumber, dentistName,
                treatmentType, appointmentDate, appointmentTime);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Send generic email
     */
    public static boolean sendEmail(String toEmail, String subject, String body) {
        String username = emailProperties.getProperty("email.username");
        String password = emailProperties.getProperty("email.password");

        // Check if email is configured
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            logger.warn("Email not configured. Skipping email notification.");
            return false;
        }

        Properties props = new Properties();
        props.put("mail.smtp.host", emailProperties.getProperty("email.host"));
        props.put("mail.smtp.port", emailProperties.getProperty("email.port"));
        props.put("mail.smtp.auth", emailProperties.getProperty("email.auth"));
        props.put("mail.smtp.starttls.enable", emailProperties.getProperty("email.starttls"));

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(username));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject(subject);
            message.setText(body);

            Transport.send(message);

            logger.info("Email sent successfully to: {}", toEmail);
            return true;

        } catch (MessagingException e) {
            logger.error("Error sending email to: {}", toEmail, e);
            return false;
        }
    }

    /**
     * Create appointment email body
     */
    private static String createAppointmentEmailBody(String patientName, String appointmentNumber,
                                                     String dentistName, String treatmentType,
                                                     String appointmentDate, String appointmentTime) {
        StringBuilder body = new StringBuilder();

        body.append("Dear ").append(patientName).append(",\n\n");
        body.append("Your appointment has been confirmed at Sunrise Dental Clinic.\n\n");
        body.append("Appointment Details:\n");
        body.append("----------------------------------------\n");
        body.append("Appointment Number: ").append(appointmentNumber).append("\n");
        body.append("Patient Name: ").append(patientName).append("\n");
        body.append("Dentist: ").append(dentistName).append("\n");
        body.append("Treatment: ").append(treatmentType).append("\n");
        body.append("Date: ").append(appointmentDate).append("\n");
        body.append("Time: ").append(appointmentTime).append("\n");
        body.append("----------------------------------------\n\n");
        body.append("Please arrive 10 minutes before your appointment time.\n\n");
        body.append("If you need to cancel or reschedule, please contact us at +94112345678.\n\n");
        body.append("Thank you for choosing Sunrise Dental Clinic.\n\n");
        body.append("Best regards,\n");
        body.append("Sunrise Dental Clinic\n");
        body.append("No.123, Main Street, Colombo 07\n");

        return body.toString();
    }
}