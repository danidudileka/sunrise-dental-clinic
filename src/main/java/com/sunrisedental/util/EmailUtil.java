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

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
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
        disableSslVerification();
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


    private static void disableSslVerification() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLContext.setDefault(sc);

            logger.info("SSL verification disabled for email");
        } catch (Exception e) {
            logger.error("Error disabling SSL verification", e);
        }
    }

    /**
     * Send appointment confirmation email to patient
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
        props.put("mail.smtp.ssl.trust", "*");  // Trust all hosts
        props.put("mail.smtp.ssl.checkserveridentity", "false");  // Disable server identity check

        // Additional properties for better compatibility
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");

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
        body.append("If you need to cancel or reschedule, please contact us at +94-11-2345678.\n\n");
        body.append("Thank you for choosing Sunrise Dental Clinic.\n\n");
        body.append("Best regards,\n");
        body.append("Sunrise Dental Clinic\n");
        body.append("123 Main Street, Colombo 07\n");

        return body.toString();
    }

    /**
     * Send patient registration email
     */
    public static boolean sendPatientRegistrationEmail(String toEmail, String patientName, String patientCode) {
        String subject = "Welcome to Sunrise Dental Clinic - Patient Registration";

        String body = createPatientRegistrationEmailBody(patientName, patientCode);

        return sendEmail(toEmail, subject, body);
    }

    /**
     * Create patient registration email body
     */
    private static String createPatientRegistrationEmailBody(String patientName, String patientCode) {
        StringBuilder body = new StringBuilder();

        body.append("Dear ").append(patientName).append(",\n\n");
        body.append("Welcome to Sunrise Dental Clinic!\n\n");
        body.append("You have been successfully registered in our system.\n\n");
        body.append("Your Patient Details:\n");
        body.append("----------------------------------------\n");
        body.append("Patient Name: ").append(patientName).append("\n");
        body.append("Patient ID: ").append(patientCode).append("\n");
        body.append("----------------------------------------\n\n");
        body.append("Please keep your Patient ID for future reference.\n");
        body.append("You will need this ID when booking appointments.\n\n");
        body.append("Our Services:\n");
        body.append("- General Dentistry\n");
        body.append("- Orthodontics\n");
        body.append("- Periodontics\n");
        body.append("- Endodontics\n");
        body.append("- Oral Surgery\n\n");
        body.append("To book an appointment, please call us at +94-11-2345678\n");
        body.append("or visit our clinic.\n\n");
        body.append("Thank you for choosing Sunrise Dental Clinic.\n\n");
        body.append("Best regards,\n");
        body.append("Sunrise Dental Clinic\n");
        body.append("123 Main Street, Colombo 07\n");

        return body.toString();
    }
}