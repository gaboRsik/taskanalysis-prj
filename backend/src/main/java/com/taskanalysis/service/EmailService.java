package com.taskanalysis.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Send export file via email to user
     *
     * @param toEmail      Recipient email address
     * @param userName     User's name
     * @param taskName     Task name
     * @param categoryName Category name
     * @param fileBytes    Export file content
     * @param fileName     Export file name
     * @param format       File format (xlsx or pdf)
     * @throws MessagingException if email sending fails
     */
    public void sendExportEmail(
            String toEmail,
            String userName,
            String taskName,
            String categoryName,
            byte[] fileBytes,
            String fileName,
            String format
    ) throws MessagingException {
        
        log.info("Sending export email to: {}", toEmail);
        
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(toEmail);
        helper.setSubject("Task Analysis Export - " + taskName);

        String emailBody = buildEmailBody(userName, taskName, categoryName);
        helper.setText(emailBody, false);

        // Attach export file
        String contentType = format.equalsIgnoreCase("xlsx") 
                ? "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                : "application/pdf";
        
        helper.addAttachment(fileName, new ByteArrayResource(fileBytes), contentType);

        mailSender.send(message);
        log.info("Export email sent successfully to: {}", toEmail);
    }

    /**
     * Build email body text
     */
    private String buildEmailBody(String userName, String taskName, String categoryName) {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        
        return String.format("""
                Szia %s!
                
                Az exportált feladat adataid csatolva találod.
                
                Feladat: %s
                Kategória: %s
                Export dátum: %s
                
                Üdv,
                Task Analysis
                """, userName, taskName, categoryName, currentDate);
    }
}
